package com.jtyjy.finance.manager.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.finance.manager.vo.application.CommissionImportDetailPowerVO;
import com.jtyjy.finance.manager.vo.application.CommissionImportDetailVO;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetExtractImportdetail;
import com.jtyjy.finance.manager.vo.ExtractImportDetailVO;

/**
 * @author Admin
 */
public interface BudgetExtractImportdetailMapper extends BaseMapper<BudgetExtractImportdetail> {

	void clearExtractDetail(@Param("ids")String ids);

	List<ExtractImportDetailVO> getExtractImportDetails(Page<ExtractImportDetailVO> pageCond, @Param("params")Map<String, Object> params);

    IPage<CommissionImportDetailPowerVO> selectCommissionPageForCommercialCommission(Page<Object> objectPage, @Param("employeeName") String employeeName, @Param("departmentName") String departmentName
			, @Param("yearId") String yearId, @Param("monthId") String monthId, @Param("extractMonth") String extractMonth, @Param("empNo") String empNo, @Param("deptId") String deptId);
	IPage<CommissionImportDetailPowerVO> selectCommissionPageForManager(Page<Object> objectPage,@Param("employeeName") String employeeName,@Param("departmentName") String departmentName
			,@Param("yearId") String yearId,@Param("monthId") String monthId,@Param("extractMonth") String extractMonth,@Param("empNo") String empNo,@Param("deptId") String deptId);
	IPage<CommissionImportDetailPowerVO> selectCommissionPageForBigManager(Page<Object> objectPage,@Param("employeeName") String employeeName,@Param("departmentName") String departmentName
			,@Param("yearId") String yearId,@Param("monthId") String monthId,@Param("extractMonth") String extractMonth,@Param("empNo") String empNo,@Param("deptId") String deptId);

    List<BudgetExtractImportdetail> getAllByExtractMonth(String extractMonth);
}
