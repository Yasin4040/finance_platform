package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetArrears;
import com.jtyjy.finance.manager.bean.BudgetLendmoney;
import com.jtyjy.finance.manager.mapper.response.LendmoneyUseBean;
import com.jtyjy.finance.manager.vo.ArrearsDetailsVO;
import com.jtyjy.finance.manager.vo.BudgetLendMoneyVO;
import com.jtyjy.finance.manager.vo.BudgetPayMoneyDetailVO;
import com.jtyjy.finance.manager.vo.BudgetRepayMoneyDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
public interface BudgetLendmoneyMapper extends BaseMapper<BudgetLendmoney> {


    /**
     * 获取借款单冲账信息
     *
     * @param ids
     * @return
     */
    List<LendmoneyUseBean> getUseInfo(@Param("ids") String ids);

    /**
     * 查询员工台账明细
     *
     * @param pageBean 分页
     * @param empNo    工号
     * @return 结果集
     */
    List<ArrearsDetailsVO> getArrearsDetails(@Param("pageBean") Page<BudgetArrears> pageBean, @Param("empNo") String empNo);

    /**
     * 查询员工借款（分页）
     *
     * @param pageBean 分页
     * @param paramMap 条件
     * @return 结果集
     */
    List<BudgetLendMoneyVO> listLendMoneyPage(@Param("pageBean") Page<BudgetLendMoneyVO> pageBean, @Param("paramMap") HashMap<String, Object> paramMap);

    /**
     * 查询员工还款明细
     *
     * @param pageBean 分页
     * @param id       借款Id
     * @return 结果集
     */
    List<BudgetRepayMoneyDetailVO> getRepayMoneyDetail(@Param("pageBean") Page<BudgetRepayMoneyDetailVO> pageBean, @Param("id") Long id);

    /**
     * 查询付款明细
     *
     * @param pageBean 分页
     * @param paramMap 查询条件
     * @return 结果集
     */
    List<BudgetPayMoneyDetailVO> getPayMoneyDetail(@Param("pageBean") Page<BudgetPayMoneyDetailVO> pageBean, @Param("paramMap") HashMap<String, Object> paramMap);

    /**
     * 通过报销人获取借款信息
     *
     * @param pageBean 分页
     * @param name     搜索关键字
     * @return 结果集
     */
    List<BudgetLendMoneyVO> getUserLendMoneyByBxr(@Param("pageBean") Page<BudgetLendMoneyVO> pageBean, @Param("name") String name);

    List<Map<String, Object>> getBudgetLendMoneyList(@Param("paramMap") Map<String, Object> paramMap);

    List<Map<String, Object>> getRepayMoneyList(@Param("lendMoneyId") Integer lendMoneyId, @Param("startDate") String startDate, @Param("endDate") String endDate);
}
