package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetLendmoneyUselog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Admin
 */
public interface BudgetLendmoneyUselogMapper extends BaseMapper<BudgetLendmoneyUselog> {

    /**
     * 锁定明细
     *
     * @param id       借款Id
     * @param pageBean 分页
     * @return 结果集
     */
    List<BudgetLendmoneyUselog> lendByBxLocked(@Param("pageBean") Page<BudgetLendmoneyUselog> pageBean, @Param("id") Long id);
}
