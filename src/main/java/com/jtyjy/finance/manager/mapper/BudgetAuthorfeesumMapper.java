package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetAuthorfeesum;
import com.jtyjy.finance.manager.easyexcel.AuthorFeeCalTaxDetailExcelData;
import com.jtyjy.finance.manager.vo.AuthorFeeDetailVO;
import com.jtyjy.finance.manager.vo.AuthorFeeMainVO;
import com.jtyjy.finance.manager.vo.AuthorFeeReportVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author Admin
 */
public interface BudgetAuthorfeesumMapper extends BaseMapper<BudgetAuthorfeesum> {

	List<Map<String, Object>> getBudgetSubjectInUnit(@Param("unitId")Long unitId, @Param("subjectNameList")Collection<String> subjectName);

	List<AuthorFeeMainVO> getAuthorSumList(@Param("params")Map<String, Object> params);

	List<AuthorFeeDetailVO> getAuthorDetailList(Page<AuthorFeeDetailVO> pageCond, @Param("params") Map<String, Object> params);

	List<Map<String, Object>> getExecuteDatas(@Param("yearid")Long yearid);

	List<AuthorFeeReportVO> getAuthorfeeReportList(Page<AuthorFeeReportVO> pageCond, @Param("yearid")Long yearid);

	List<AuthorFeeCalTaxDetailExcelData> getAuthorFeeCalTaxDetailList(Page<AuthorFeeCalTaxDetailExcelData> pageCond,
			@Param("params")Map<String, Object> params);

	void setAuthormergeidIsNull(@Param("detailIdList")List<Long> detailIdList);
	
}
