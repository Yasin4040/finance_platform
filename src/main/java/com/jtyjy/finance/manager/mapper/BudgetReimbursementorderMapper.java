package com.jtyjy.finance.manager.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorder;
import com.jtyjy.finance.manager.mapper.response.ReimbursementValidateMoney;
import com.jtyjy.finance.manager.vo.BxDetailVO;
import com.jtyjy.finance.manager.vo.BxLiuZhuanVO;
import com.jtyjy.finance.manager.vo.MakeAccountTaskVO;
import com.jtyjy.finance.manager.vo.ReimbursementInfoVO;

/**
 * @author Admin
 */
@Mapper
public interface BudgetReimbursementorderMapper extends BaseMapper<BudgetReimbursementorder> {

	/**
	 * 获取年度动因金额
	 * @param query
	 * @param ids  月度动因主键 类似于 1,2,3,4
	 * @return
	 */
	List<ReimbursementValidateMoney> getYearAgentValidateMoney(@Param("bean") ReimbursementValidateMoney query, @Param("ids") String ids);

	/**
	 * 获取月度科目金额
	 * @param query
	 * @param ids  月度动因主键 类似于 1,2,3,4
	 * @return
	 */
	List<ReimbursementValidateMoney> getMonthCourseValidateMoney(@Param("bean")ReimbursementValidateMoney query, @Param("ids")String ids);

	/**
	 * 获取年度科目金额
	 * @param query
	 * @param ids  月度动因主键 类似于 1,2,3,4
	 * @return
	 */
	List<ReimbursementValidateMoney> getYearCourseValidateMoney(@Param("bean")ReimbursementValidateMoney query, @Param("ids")String ids);
	
	/**
	 * 分页查询报销列表
	 * @param pageCond
	 * @param conditionMap
	 * @param authSql
	 * @return
	 */
	List<ReimbursementInfoVO> getReimbursementPageInfo(Page<ReimbursementInfoVO> pageCond, Map<String, Object> conditionMap, String authSql);

	/**
	 * 获取借款付款+提成付款列表
	 * @return empid 用户id，pids 用户所在部门树，paymoneyid 付款记录id
,	 */
	List<Map<String, Object>> getErrorLendAndExtractList();
	
	List<ReimbursementInfoVO> queryBxProgressPageInfo(Page<ReimbursementInfoVO> pageCond, String curscaner, String reimcode, Double reimmoney, String whereSql, String authSql);

    /**
     * 分页查询做账任务
     * @param pageCond
     * @param empNo 工号
     * @param userId 会计员id
     * @param reimcode 报销单号
     * @param authSql
     * @return
     */
    List<MakeAccountTaskVO> getAccountTaskPageInfo(Page pageCond, String empNo, String userId, String reimcode, String authSql);

    List<Map<String, Object>> getQrCodeByReimcode(String reimcode);
    
    List<Map<String, Object>> getBxReturnReason(Long yearId, Long monthId);

    List<String> listBackType();
}
