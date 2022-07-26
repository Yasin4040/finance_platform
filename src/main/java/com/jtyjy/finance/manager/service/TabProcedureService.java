package com.jtyjy.finance.manager.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.finance.manager.bean.TabFlowCondition;
import com.jtyjy.finance.manager.bean.TabLinkLimit;
import com.jtyjy.finance.manager.bean.TabProcedure;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.mapper.TabDmMapper;
import com.jtyjy.finance.manager.mapper.TabFlowConditionMapper;
import com.jtyjy.finance.manager.mapper.TabLinkLimitMapper;
import com.jtyjy.finance.manager.mapper.TabProcedureMapper;

import lombok.RequiredArgsConstructor;

/**
 * <p></p>
 * <p>作者 konglingcheng</p>
 * <p>date 2020年4月28日</p>
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("all")
public class TabProcedureService extends DefaultBaseService<TabProcedureMapper,TabProcedure>{

    private final TabChangeLogMapper loggerMapper;
    
    private final TabProcedureMapper pdMapper;
    
    private final TabDmService dmService;
    
    private final TabLinkLimitService llService;
    
    private final TabFlowConditionService fcService;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }
    
    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("tab_procedure"));
    }
	
    /**
     * 获取过程信息
     * @param isActive 是否启用 0否 1是
     * @param procedureType 过程类型 1报销 2其他
     * @param linkOrder 环节代码（包含此环节）
     * @param name 
     * @return
     */
	public List<TabProcedure> getProcedureInfo(String isActive, String procedureType, String linkOrder, Long yearId, String name){
	    if (0 == yearId.intValue()) yearId = null;
	    List<TabProcedure> list = pdMapper.getProcedureInfo(isActive, procedureType, linkOrder, yearId, name);	
	    List<TabDm> dmList = this.dmService.selectAllSubType("link");
        for (TabProcedure procedure : list) {
	        procedure.setLinkOrderName(getLinkName(procedure.getProcedureLinkOrder(), dmList));
	    }
	    
	    return list;
	}
		   
    /**
     * 获取流程的环节代码过程信息
     * @param id 流程主键
     * @return
     */
    public List<TabDm> getLinkCodeByPid(Long id, String linkCode){
        if (StringUtils.isBlank(linkCode)) {
            linkCode = "";
        }
        TabProcedure procedure = this.getById(id);
        List<TabDm> dmList = this.dmService.selectAllSubType("link");
        List<TabDm> resultList = new ArrayList<TabDm>();
        String linkOrder = "";
        if (null != procedure) {
            linkOrder = procedure.getProcedureLinkOrder();
            for (TabDm dm :dmList) {
                if (linkOrder.contains(dm.getDm()) && !linkCode.equals(dm.getDm())) {
                    resultList.add(dm);
                }
            }
            return resultList;
        }else {
            return dmList;
        }
    }
    
	/**
	 * 获取环节名称
	 * @param linkStr
	 * @return
	 */
	private String getLinkName(String linkStr, List<TabDm> dmList) {
	    if (StringUtils.isBlank(linkStr)) {
            return "";
        }
	    String[] linkArr = linkStr.split(",");
	    String linkName = "";
	    Map<String, String> linkMap = dmList.stream().collect(Collectors.toMap(e->{return e.getDm();}, TabDm::getDmName,(ele1,ele2) -> ele1));
        
        for (String linkCode : linkArr) {
            if (StringUtils.isNotBlank(linkCode)) {
                String dmName = linkMap.get(linkCode);
                if (StringUtils.isNotBlank(dmName)) {
                    linkName += dmName + ",";
                }
            }
        }
        if (StringUtils.isNotBlank(linkName)) {
            return linkName.substring(0, linkName.length() - 1);
        }else {
            return linkStr;
        }
	}
	
	public void deleteProcedure(String ids) throws Exception {
	    UpdateWrapper<TabProcedure> wrapper = new UpdateWrapper<TabProcedure>();
        wrapper.set("is_delete", "1");
        List<String> idList = Arrays.asList(ids.split(","));
        for (String id : idList) {
            Page<TabLinkLimit> linkLimit = this.llService.getLinkLimitInfo(1, 100, Long.valueOf(id), null, null, null);
            List<TabLinkLimit> linkLimitList = linkLimit.getRecords();
            if (null != linkLimitList && !linkLimitList.isEmpty()) {
                throw new Exception("模板【" + linkLimitList.get(0).getProcedureName() + "】已设置节点条件，需先删除节点条件；");
            }
            TabFlowCondition conditionBean = new TabFlowCondition();
            conditionBean.setTheVersion(Integer.valueOf(id));
            Page<TabFlowCondition> fcPageInfo = this.fcService.pageInfo(1, 100, conditionBean);
            if (null != fcPageInfo && fcPageInfo.getTotal() > 0) {
                throw new Exception("模板【" + fcPageInfo.getRecords().get(0).getProcedureName() + "】已设置前置条件，需先删除前置条件；");
            }
        }
        wrapper.in("id", idList);
        this.update(wrapper);
	}
	
    public boolean checkData(TabProcedure bean, StringBuffer errMsg) {
        if (null == bean) {
            errMsg.append("数据不能为空");
            return false;
        }
        TabProcedure sameName = this.getOne(new QueryWrapper<TabProcedure>().eq("yearid", bean.getYearid()).eq("procedure_name", bean.getProcedureName()));
        
        if(null == bean.getId() || 0 == bean.getId().intValue()) {
            if (null != sameName && bean.getProcedureName().equals(sameName.getProcedureName())) {
                errMsg.append("当前届别" + bean.getProcedureName() + "名称已存在！");
                return false;
            }
            
        }else {
            if (null != sameName && !sameName.getId().equals(bean.getId())) {
                errMsg.append("当前届别" + bean.getProcedureName() + "名称已存在！");
                return false;
            }
        }
        return true;
    }
    
    /**
     * 查询届别激活的流程信息
     * @param yearId
     * @param procedureType
     * @return
     * @throws Exception 
     */
    public TabProcedure getCurrentProcedure(Long yearId,String procedureType) throws Exception {
    	QueryWrapper<TabProcedure> wrapper = new QueryWrapper<TabProcedure>();
    	if (null != yearId) {
            wrapper.eq("yearid", yearId);
    	}
    	wrapper.eq("procedure_type", procedureType);
    	wrapper.eq("is_active", "1");
    	wrapper.eq("is_delete", "0");
    	wrapper.orderByDesc("yearid");
    	List<TabProcedure> list = this.list(wrapper);
    	if(list == null || list.size() != 1) {
    		throw new Exception("无法确认届别的审核模板！");
    	}
    	return list.get(0);
    }
}
