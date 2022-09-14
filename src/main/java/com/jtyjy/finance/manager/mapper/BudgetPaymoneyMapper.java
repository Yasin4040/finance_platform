package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetPaymoney;

import java.util.List;
import java.util.Map;

import com.jtyjy.finance.manager.vo.BudgetExtractPayQueryVO;
import com.jtyjy.finance.manager.vo.BudgetExtractPayResponseVO;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author Admin
 */
public interface BudgetPaymoneyMapper extends BaseMapper<BudgetPaymoney> {
	
	/**
	 * 批量保存付款单
	 * @author minzhq
	 * @param paymoneyList
	 */
	int batchSavePaymoney(@Param("paymoneyList")List<BudgetPaymoney> paymoneyList);
	
	/**
	 * 分页查询报销付款明细
	 * @param pageCond
	 * @param conditionMap
	 * @param authSql
	 * @return
	 */
    List<BudgetPaymoney> getBxPaymoneyPageInfo(Page pageCond, Map<String, Object> conditionMap, String authSql);

    /**
     * 分页查询异常报销付款明细
     * @param pageCond
     * @param ids 
     * @param authSql
     * @return
     */
    List<BudgetPaymoney> getErrorPaymoneyPageInfo(Page pageCond, Map<String, Object> conditionMap, String authSql);
    
    /**
     * 出纳付款分页查询
     * @param pageCond
     * @param conditionMap
     * @param authSql
     * @return
     */
    List<BudgetPaymoney> getCashPayMoneyPageInfo(Page pageCond, Map<String, Object> conditionMap, String authSql);

    /**
     * 查询正常的付款列表（可设置失败付款）
     * @param pageCond
     * @param conditionMap
     * @param authSql
     * @return
     */
    List<BudgetPaymoney> getNaturalPayPageInfo(Page pageCond, Map<String, Object> conditionMap, String authSql);
    
    /**
     * 查询未付款的报销单号
     * @param pageCond
     * @param condition 查询条件（报销单号/报销人/报销金额）
     * @param authSql
     * @return reimcode：报销单号，query：文本框显示内容
     */
    List<Map<String, String>> getReimcodePage(Page pageCond, String condition, String authSql);
 
    /**
     * 查询未付款的提成编号
     * @param pageCond
     * @param condition 提成编号
     * @param authSql
     * @return 提成编号
     */
    List<String> getTccodePage(Page pageCond, String condition, String authSql);

    /**
     * 查询未付款的项目转账付款
     * @param pageCond
     * @param condition 查询条件（项目名称/项目编号）
     * @param authSql
     * @return xmcode：项目编号，query：文本框显示内容
     */
    List<Map<String, String>> getXmcodePage(Page pageCond, String condition, String authSql);

    /**
     * 根据付款单类型查询未付款信息（可准备付款）
     * @param paymoneytype
     * @param objectcode
     * @param paytype 
     * @return
     */
    List<BudgetPaymoney> getCanPayMoneyByPmtype(Integer paymoneytype, String objectcode, Integer paytype);

    /**
     * 根据付款方式查询未付款信息（可准备付款）
     * @param paytype 
     * @param objectcode
     * @param ids 
     * @return
     */
    List<BudgetPaymoney> getCanPayMoneyByFkType(Integer paytype, String objectcode, String bankaccountname, String ids);

    /**
     * 待添加付款列表查询
     * @param pageCond
     * @param conditionMap
     * @param authSql
     * @return
     */
    List<BudgetPaymoney> otherAddQuery(Page pageCond, Map<String, Object> conditionMap, String authSql);
	/**
	 * <p>提成付款单列表</p>
	 * @author minzhq
	 * @date 2022/9/14 11:21
	 * @param pageCond
	 * @param params
	 */
	List<BudgetExtractPayResponseVO> getExtractPayMoneyList(Page<BudgetExtractPayResponseVO> pageCond, @Param("params") BudgetExtractPayQueryVO params);
}
