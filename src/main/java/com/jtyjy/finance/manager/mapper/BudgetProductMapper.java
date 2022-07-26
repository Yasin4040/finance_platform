package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetProduct;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
public interface BudgetProductMapper extends BaseMapper<BudgetProduct> {

    /**
     * 查询预算单位下关联产品
     *
     * @param budgetUnitId 预算单位Id
     * @return 产品集合
     */
    List<BudgetProduct> listProduct(@Param("budgetUnitId") Long budgetUnitId);
    
    List<BudgetProduct> getProductPageInfo(Page<BudgetProduct> pageCond,
            Map<String, Object> conditionMap,
            String authSql);
    
    List<BudgetProduct> getPdInfoByCid(String cid);
    
}
