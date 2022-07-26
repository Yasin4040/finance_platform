package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetOtherlendsum;
import com.jtyjy.finance.manager.vo.BudgetLendMoneyVO;
import com.jtyjy.finance.manager.vo.BudgetOtherLendSumVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * @author Admin
 */
public interface BudgetOtherlendsumMapper extends BaseMapper<BudgetOtherlendsum> {

    /**
     * 查询其它借款（分页）
     *
     * @param pageBean 分页
     * @param paramMap 查询条件
     * @return 结果集
     */
    List<BudgetOtherLendSumVO> listOtherLendPage(@Param("pageBean") Page<BudgetOtherLendSumVO> pageBean, @Param("paramMap") HashMap<String, Object> paramMap);

    /**
     * 借款明细
     *
     * @param id       其它借款Id
     * @param pageBean 分页
     * @return 结果集
     */
    List<BudgetLendMoneyVO> listLendDetail(@Param("pageBean") Page<BudgetLendMoneyVO> pageBean, @Param("id") Long id);
}
