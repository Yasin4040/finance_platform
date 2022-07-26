package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetProjectlendsum;
import com.jtyjy.finance.manager.vo.BudgetProjectLendDetailVO;
import com.jtyjy.finance.manager.vo.BudgetProjectLendSumVO;
import com.jtyjy.finance.manager.vo.BudgetProjectRepayDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * @author Admin
 */
public interface BudgetProjectlendsumMapper extends BaseMapper<BudgetProjectlendsum> {

    /**
     * 查询项目借款（分页）
     *
     * @param pageBean 分页
     * @param paramMap 查询条件
     * @return 结果集
     */
    List<BudgetProjectLendSumVO> listProjectLendPage(@Param("pageBean") Page<BudgetProjectLendSumVO> pageBean, @Param("paramMap") HashMap<String, Object> paramMap);

    /**
     * 查询借款明细
     *
     * @param id       主键Id
     * @param pageBean 分页
     * @return 结果集
     */
    List<BudgetProjectLendDetailVO> listLendMoneyDetail(@Param("pageBean") Page<BudgetProjectLendDetailVO> pageBean, @Param("id") Long id);

    /**
     * 查询还款明细
     *
     * @param id       主键Id
     * @param pageBean 分页
     * @return 结果集
     */
    List<BudgetProjectRepayDetailVO> listRepayMoneyDetail(@Param("pageBean") Page<BudgetProjectRepayDetailVO> pageBean, @Param("id") Long id);
}
