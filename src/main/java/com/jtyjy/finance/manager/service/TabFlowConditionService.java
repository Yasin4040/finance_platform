package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.TabFlowCondition;
import com.jtyjy.finance.manager.bean.TabLinkLimit;
import com.jtyjy.finance.manager.bean.TabProcedure;
import com.jtyjy.finance.manager.mapper.TabFlowConditionMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JdbcSelector(value = "defaultJdbcTemplateService")
public class TabFlowConditionService extends DefaultBaseService<TabFlowConditionMapper, TabFlowCondition> {

	private final TabChangeLogMapper loggerMapper;

	private final TabFlowConditionMapper mapper;
	
	private final TabDmService dmService;
	
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("tab_flow_condition"));
	}
	
	public Page<TabFlowCondition> pageInfo(Integer page, Integer rows, TabFlowCondition conditionBean) {
	    Page<TabFlowCondition> pageCond = new Page<TabFlowCondition>(page, rows);
	    try {
            List<TabFlowCondition> list = this.mapper.getConditionPageInfo(pageCond, conditionBean, JdbcSqlThreadLocal.get());

            List<TabDm> dmList = this.dmService.selectAllSubType("link");
            for (TabFlowCondition bean : list) {
                bean.setStepDmName(getLinkName(bean.getStepDm(), dmList));
                bean.setConditionStepDmName(getLinkName(bean.getConditionStepDm(), dmList));
            }
            pageCond.setRecords(list);
            
	    } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    return pageCond;
	}
	
	public int copyInit(Long sourceId, Long targetId, StringBuffer errMsg) {
	    int success = 0;
	    List<TabFlowCondition> sourceList = this.list(new QueryWrapper<TabFlowCondition>().eq("the_version", sourceId));
	    if (null == sourceList || sourceList.isEmpty()) {
	        errMsg.append("源模板未配置前置条件！");
	        return 0;
	    }
	    if (sourceId.intValue() == targetId.intValue()) {
	        errMsg.append("源模板和目标模板不能为同一个！");
            return 0;
	    }
	    //删除目标模板的前置条件
	    this.mapper.delete(new UpdateWrapper<TabFlowCondition>().eq("the_version", targetId));
	    for (TabFlowCondition sourceBean : sourceList) {
	        sourceBean.setTheVersion(targetId.intValue());
	        sourceBean.setId(null);
	        success ++;
	    }
	    this.saveBatch(sourceList);
	    return success;
	}
	
	private String getLinkName(String linkStr, List<TabDm> dmList) {
	    if (StringUtils.isBlank(linkStr)) {
            return "";
        }
        String[] linkArr = linkStr.split(",");
        StringJoiner linkName = new StringJoiner(",");
        Map<String, String> linkMap = dmList.stream().collect(Collectors.toMap(e->{return e.getDm();}, TabDm::getDmName,(ele1,ele2) -> ele1));
        
        for (String linkCode : linkArr) {
            if (StringUtils.isNotBlank(linkCode)) {
                String dmName = linkMap.get(linkCode);
                if (StringUtils.isNotBlank(dmName)) {
                    linkName.add(dmName);
                }
            }
        }
        return linkName.toString();
	    
	}
}
