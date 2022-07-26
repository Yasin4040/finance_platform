package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetContract;
import com.jtyjy.finance.manager.vo.BudgetContractLendVO;
import com.jtyjy.finance.manager.vo.BudgetContractVO;
import com.jtyjy.finance.manager.vo.BudgetStrikeMoneyDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * @author Admin
 */
public interface BudgetContractMapper extends BaseMapper<BudgetContract> {

    /**
     * 查询合同列表（分页）
     *
     * @param pageBean 分页
     * @param paramMap 查询条件
     * @return 结果集
     */
    List<BudgetContractVO> listContractPage(@Param("pageBean") Page<BudgetContractVO> pageBean, @Param("paramMap") HashMap<String, Object> paramMap);

    /**
     * 合同冲账明细
     *
     * @param id       合同Id
     * @param pageBean 分页
     * @return 结果集
     */
    List<BudgetStrikeMoneyDetailVO> getStrikeMoneyDetail(@Param("pageBean") Page<BudgetStrikeMoneyDetailVO> pageBean, @Param("id") Long id);

    /**
     * 查询合同借款列表（分页）
     *
     * @param pageBean 分页
     * @param paramMap 查询条件
     * @return 结果集
     */
    List<BudgetContractLendVO> listContractLendPage(@Param("pageBean") Page<BudgetContractLendVO> pageBean, @Param("paramMap") HashMap<String, Object> paramMap);
}
