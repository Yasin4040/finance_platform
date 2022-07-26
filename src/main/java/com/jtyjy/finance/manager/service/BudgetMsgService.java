package com.jtyjy.finance.manager.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetMsgtemplete;
import com.jtyjy.finance.manager.bean.BudgetMsgtempleteDetailNew;
import com.jtyjy.finance.manager.bean.BudgetMsgtempleteParameter;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.enmus.MsgTypeEnum;
import com.jtyjy.finance.manager.enmus.TemplateCategoryEnum;
import com.jtyjy.finance.manager.enmus.TemplateTypeEnum;
import com.jtyjy.finance.manager.mapper.BudgetMsgtempleteDetailNewMapper;
import com.jtyjy.finance.manager.mapper.BudgetMsgtempleteMapper;
import com.jtyjy.finance.manager.mapper.BudgetMsgtempleteParameterMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.mapper.WbUserMapper;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetMsgService extends DefaultBaseService<BudgetMsgtempleteMapper, BudgetMsgtemplete>{
	private final TabChangeLogMapper loggerMapper;
	
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_msgtemplete"));
	}
	
	@Autowired
	private BudgetMsgtempleteMapper msgtempleteMapper;
	
	@Autowired
	private BudgetYearPeriodMapper yearPeriodMapper;
	
	@Autowired
	private BudgetMsgtempleteDetailNewMapper detailMapper;
	
	@Autowired
	private BudgetMsgtempleteParameterMapper parameterMapper;
	
	@Autowired
	private WbUserMapper userMapper;
	
	public PageResult<BudgetMsgtemplete> getMsgTemplateList(Integer page, Integer rows) {
		Page<BudgetMsgtemplete> pageCond = new Page<BudgetMsgtemplete>(page, rows);
		pageCond = msgtempleteMapper.selectPage(pageCond, new QueryWrapper<BudgetMsgtemplete>().orderByDesc("yearid"));
		List<BudgetMsgtemplete> records = pageCond.getRecords();
		records.stream().forEach(e->{
			e.setYearName(yearPeriodMapper.selectById(e.getYearid()).getPeriod());
			e.setTempletecategoryName(TemplateCategoryEnum.getValue(e.getTempletecategory()));
			e.setTempletetypeName(TemplateTypeEnum.getValue(e.getTempletetype()));
		});
		return PageResult.apply(pageCond.getTotal(), records);
	}
	
	/**
	 * 获取回款消息
	 * @param page
	 * @param rows
	 * @param params
	 * @return
	 */
	public PageResult<BudgetMsgtempleteDetailNew> getMsgTemplateDetailForBackMoneyList(Integer page, Integer rows,
			Map<String, Object> params) {
		Long templateId = Long.valueOf(params.get("templateId").toString());
		List<BudgetMsgtempleteParameter> parameters = getParameters(templateId,null);
		Page<BudgetMsgtempleteDetailNew> pageCond = new Page<BudgetMsgtempleteDetailNew>(page, rows);
		QueryWrapper<BudgetMsgtempleteDetailNew> qw = new QueryWrapper<>();
		qw.eq("templeteid", templateId);
		if(params.containsKey("empno")) qw.eq("empno", params.get("empno"));
		if(params.containsKey("msgType")) qw.like("msgcontent", "\"msgtype\":"+params.get("msgType"));
		if(params.containsKey("isSend")) qw.eq("issend", params.get("isSend"));
		pageCond = detailMapper.selectPage(pageCond, qw);
		List<BudgetMsgtempleteDetailNew> records = pageCond.getRecords();
		records.forEach(e->{
			String msgcontent = e.getMsgcontent();
			Map<String,Object> msgcontentMap = JSONObject.parseObject(msgcontent);
			String msgtype = msgcontentMap.get("msgtype").toString();
			Map<String,Object> resultMap = new HashMap<>();
			msgcontentMap.forEach((k,v)->{
				BudgetMsgtempleteParameter parameter = parameters.stream().filter(p->p.getTempleteid().toString().equals(templateId.toString())
										&& p.getMsgtype().toString().equals(msgtype) && p.getEnglishname().equals(k)).findFirst().orElse(null);
				if(parameter == null) return;
				resultMap.put(parameter.getChinesename(), v);
			});
			resultMap.remove("msgtype");
			e.setContent(JSON.toJSONString(resultMap));
			e.setMsgType(Integer.valueOf(msgtype));
		});
		return PageResult.apply(pageCond.getTotal(), records);
	}
	private List<BudgetMsgtempleteParameter> getParameters(Long templateId,Integer msgType){
		QueryWrapper<BudgetMsgtempleteParameter> qw = new QueryWrapper<BudgetMsgtempleteParameter>().eq("templeteid", templateId).orderByAsc("orderno");
		if(msgType!=null) qw.eq("msgtype", msgType);
		return parameterMapper.selectList(qw);
	}
	/**
	 * 导入消息明细
	 * @param datas
	 */
	public void saveMsgDetail(List<List<String>> datas,Long templateId) {
		if(!datas.isEmpty()) {
			String msgType = datas.get(0).get(0);
			Integer code = MsgTypeEnum.getCode(msgType);
			datas.forEach(row->{
				String empNo = row.get(1);
				WbUser user = userMapper.selectOne(new QueryWrapper<WbUser>().eq("USER_NAME", empNo));
				BudgetMsgtempleteDetailNew detail = new BudgetMsgtempleteDetailNew();
				detail.setEmpno(empNo);
				detail.setIssend(false);
				detail.setTempleteid(templateId);
				
				List<BudgetMsgtempleteParameter> parameters = getParameters(templateId, code);
				int size = row.size();
				Map<String,Object> contentMap = new HashMap<>();
				contentMap.put("msgtype", code);
				contentMap.put("empname", user.getDisplayName());
				for(int i=0;i<parameters.size();i++) {
					if(i+2 >= size) break;
				    String value = row.get(i+2);
				    contentMap.put(parameters.get(i).getEnglishname(), value);
				}
				detail.setMsgcontent(JSON.toJSONString(contentMap));
				this.detailMapper.insert(detail);
			});
		}
	}
	
}
