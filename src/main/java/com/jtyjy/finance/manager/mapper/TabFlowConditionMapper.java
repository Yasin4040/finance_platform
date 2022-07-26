package com.jtyjy.finance.manager.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.TabFlowCondition;

/**
 * @author Admin
 */
@Mapper
public interface TabFlowConditionMapper extends BaseMapper<TabFlowCondition> {
	
    List<TabFlowCondition> getConditionPageInfo(Page pageCond, TabFlowCondition conditionBean, String authSql);
}
