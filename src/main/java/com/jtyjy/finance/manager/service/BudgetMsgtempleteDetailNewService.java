package com.jtyjy.finance.manager.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetMsgtemplete;
import com.jtyjy.finance.manager.bean.BudgetMsgtempleteDetailNew;
import com.jtyjy.finance.manager.bean.BudgetMsgtempleteParameter;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.enmus.MsgTypeEnum;
import com.jtyjy.finance.manager.enmus.TemplateParameterFieldTypeEnum;
import com.jtyjy.finance.manager.mapper.BudgetMsgtempleteDetailNewMapper;
import com.jtyjy.finance.manager.mapper.BudgetMsgtempleteMapper;
import com.jtyjy.finance.manager.mapper.BudgetMsgtempleteParameterMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.mapper.TabDmMapper;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import com.jtyjy.weixin.message.TextcardMessage;
import com.jtyjy.weixin.message.component.TextcardDetail;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetMsgtempleteDetailNewService extends DefaultBaseService<BudgetMsgtempleteDetailNewMapper,BudgetMsgtempleteDetailNew>{
private final TabChangeLogMapper loggerMapper;
	
	@Autowired
	private BudgetMsgtempleteParameterMapper parameterMapper;

	@Autowired
	private BudgetMsgtempleteMapper msgtempleteMapper;
	
	@Autowired
	private MessageSender sender;
	
	@Autowired
	private TabDmMapper dmMapper;

	@Value("${service.domain}")
	private String serverUrl;
	
	private final static String MSG = "MSG";
	
	private final static String OBJECTION_NOTICE = "OBJECTION_NOTICE";
	
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_msgtemplete_detail_new"));
	}

	public void sendMsg(String ids) throws Exception{
		List<BudgetMsgtempleteDetailNew> list = this.listByIds(Arrays.asList(ids.split(",")));
		BudgetMsgtemplete msgtemplete = msgtempleteMapper.selectById(list.get(0).getTempleteid());
		Map<Integer, List<BudgetMsgtempleteParameter>> parameterMap = getParameters(list.get(0).getTempleteid(), null).stream().collect(Collectors.groupingBy(e->e.getMsgtype()));
		list.forEach(detail->{
			Map<String,Object> msgDataMap = JSONObject.parseObject(detail.getMsgcontent());
			Integer msgtype = Integer.valueOf(msgDataMap.get("msgtype").toString());
			List<BudgetMsgtempleteParameter> parameters = parameterMap.get(msgtype);
			String msg = "";

			if(msgtype == MsgTypeEnum.WARNING.getCode()) {
				msg = replaceMsg(msgtemplete.getWarnmsg(),parameters,msgDataMap);
				try {
					if(StringUtils.isNotBlank(msg))sender.sendQywxMsgSyn(new QywxTextMsg(detail.getEmpno(), null, null, 0, msg, null));
					detail.setIssend(true);
				}catch(Exception e) {detail.setIssend(false);throw e;}
			}else if(msgtype == MsgTypeEnum.PUBLIC.getCode()) {
				msg = replaceMsg(msgtemplete.getPublicitymsg(),parameters,msgDataMap);
				try {
					if(StringUtils.isNotBlank(msg)) this.sender.sendQywxMsgSyn(new TextcardMessage(detail.getEmpno(), 
							null, "公示消息通知", serverUrl+"/api/msg/redirectPublicMsgPage?id="+detail.getId(), "详情",
								TextcardDetail.apply(TextcardDetail.GRAY, Constants.FULL_FORMAT.format(new Date())+"<br>", true),
								TextcardDetail.apply(TextcardDetail.GRAY,"您收到一条公示消息,请尽快处理。",false)
					));
					detail.setIssend(true);
				}catch(Exception e) {detail.setIssend(false);}
			}else if(msgtype == MsgTypeEnum.RESULT.getCode()) {
				msg = replaceMsg(msgtemplete.getResultmsg(),parameters,msgDataMap);
				try {
					if(StringUtils.isNotBlank(msg)) sender.sendQywxMsgSyn(new QywxTextMsg(detail.getEmpno(), null, null, 0, msg, null));
					detail.setIssend(true);
				}catch(Exception e) {detail.setIssend(false);throw e;}
			}
		});
		this.updateBatchById(list);
	}
	
	private String replaceMsg(String msg, List<BudgetMsgtempleteParameter> parameters,Map<String,Object> msgDataMap) {
		if(parameters!=null) {
			msg = msg==null?"":msg;
			for(BudgetMsgtempleteParameter p : parameters) {
				if(p.getType() == TemplateParameterFieldTypeEnum.TEXT.getType()) {
					msg = msg.replace("${"+p.getEnglishname()+"}",msgDataMap.get(p.getEnglishname())==null?"":msgDataMap.get(p.getEnglishname()).toString());
				}else if(p.getType() == TemplateParameterFieldTypeEnum.MONEY.getType()) {
					BigDecimal b = new BigDecimal(msgDataMap.get(p.getEnglishname())==null?"0":msgDataMap.get(p.getEnglishname()).toString());
					msg = msg.replace("${"+p.getEnglishname()+"}",b.setScale(2,BigDecimal.ROUND_DOWN).stripTrailingZeros().toPlainString());
				}else if(p.getType() == TemplateParameterFieldTypeEnum.PERCENT.getType()) {
					String data = "";
					if(msgDataMap.get(p.getEnglishname())!=null) {
						data = new BigDecimal(msgDataMap.get(p.getEnglishname()).toString()).multiply(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_DOWN).toString();
					}
					msg = msg.replace("${"+p.getEnglishname()+"}",data);
				}		
			}
		}
		return msg;
	}

	private List<BudgetMsgtempleteParameter> getParameters(Long templateId,Integer msgType){
		QueryWrapper<BudgetMsgtempleteParameter> qw = new QueryWrapper<BudgetMsgtempleteParameter>().eq("templeteid", templateId).orderByAsc("orderno");
		if(msgType!=null) qw.eq("msgtype", msgType);
		return parameterMapper.selectList(qw);
	}

	public Map<String,Object> getPublicMsg(Long id) {
		BudgetMsgtempleteDetailNew detail = this.getById(id);
		BudgetMsgtemplete msgtemplete = msgtempleteMapper.selectById(detail.getTempleteid());
		List<BudgetMsgtempleteParameter> parameters = getParameters(detail.getTempleteid(), MsgTypeEnum.PUBLIC.getCode());
		Map<String,Object> msgDataMap = JSONObject.parseObject(detail.getMsgcontent());
		
		detail.setIspreview(true);
		detail.setOperatetime(new Date());
		this.updateById(detail);
		Map<String,Object> map = new HashMap<>();
		map.put("msg", replaceMsg(msgtemplete.getPublicitymsg(),parameters,msgDataMap));
		map.put("isObjection", detail.getIsobjection());
		return map;
	}

	public void objection(Long id, String remark) {
		BudgetMsgtempleteDetailNew detail = this.getById(id);
		Optional.ofNullable(detail.getIsobjection()).ifPresent(e->{
			if(e) throw new RuntimeException("您已申请过异议！请勿重复操作！");
		});
		detail.setIsobjection(true);
		detail.setOperatetime(new Date());
		detail.setObjectdesc(remark);
		this.updateById(detail);
		
		TabDm dm = this.dmMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", MSG).eq("dm", OBJECTION_NOTICE));
		Map<String, Object> msgContentMap = JSONObject.parseObject(detail.getMsgcontent());
		//不用捕获异常
		sender.sendQywxMsgSyn(new QywxTextMsg(dm.getDmValue(), null, null, 0, msgContentMap.get("empname").toString()+"经理对回款率公示存在异议,请及时对接处理!\n异议详情:"+remark, null));					
	}
}
