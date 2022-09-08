package com.jtyjy.finance.manager.mapper;

import java.util.List;
import java.util.Map;

import com.jtyjy.finance.manager.dto.commission.IndividualIssueExportDTO;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetExtractsum;
import com.jtyjy.finance.manager.easyexcel.ExtractCCLPayExcelData;
import com.jtyjy.finance.manager.vo.ExtractDeductionDetailVO;
import com.jtyjy.finance.manager.vo.ExtractInfoVO;
import com.jtyjy.finance.manager.vo.ExtractPayDetailVO;
import com.jtyjy.finance.manager.vo.ExtractWithholdDetailVO;

/**
 * @author Admin
 */
public interface BudgetExtractsumMapper extends BaseMapper<BudgetExtractsum> {

	List<ExtractDeductionDetailVO> getExtractDeductionReport(Page<ExtractDeductionDetailVO> pageCond, @Param("empNo") String empNo,@Param("lendTypeList") List<Integer> lendTypeList);

	List<ExtractInfoVO> getExtractInfoList(Page<ExtractInfoVO> pageCond, @Param("params")Map<String, Object> params);

	List<ExtractWithholdDetailVO> getExtractWithholdDetails(Page<ExtractWithholdDetailVO> pageCond,
			@Param("params")Map<String, Object> params);

	List<ExtractPayDetailVO> getExtractPayDetails(Page<ExtractPayDetailVO> pageCond, @Param("params")Map<String, Object> params);

	List<String> getIncorporatedcompanyPayedExtract(@Param("idnumber")String idnumber, 
				@Param("billingUnitId")Long billingUnitId, @Param("curExtractBatch")String curExtractBatch,
				@Param("curYearStartExtractBatch")String curYearStartExtractBatch);

	List<Map<String, Object>> getRepaymoneymsg(@Param("repaymoneyid")Long repaymoneyid);

	List<ExtractCCLPayExcelData> getCCLPayDetailList(@Param("extractBatch")String extractBatch);

	List<IndividualIssueExportDTO> selectAllDetailList(@Param("extractMonth")String extractMonth);
}
