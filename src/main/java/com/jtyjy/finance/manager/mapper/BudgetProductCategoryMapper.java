package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetProductCategory;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author Admin
 */
public interface BudgetProductCategoryMapper extends BaseMapper<BudgetProductCategory> {
	
    List<BudgetProductCategory> getPdCategoryInfo(String name, String pids, Integer stopflag);
    
    List<String> getPidListByUnitId(Long unitId);
    
    /**
     * 查询产品分类关联的预算科目
     * @param pcId
     * @return
     */
    List<Map<String, Object>> getSubjectByPcId(String pcId);
}
