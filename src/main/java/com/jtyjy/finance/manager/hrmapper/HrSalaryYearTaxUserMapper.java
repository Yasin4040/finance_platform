package com.jtyjy.finance.manager.hrmapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.hrbean.HrSalaryYearTaxUser;

public interface HrSalaryYearTaxUserMapper extends BaseMapper<HrSalaryYearTaxUser>{

	List<Map<String, Object>> getSalary(@Param("year")String year, @Param("salaryMonth")String salaryMonth);

	List<Map<String, Object>> getTaxratelist();

	List<Map<String, Object>> gettaxratedetaillist();

	List<Map<String, Object>> getSalaryCompanyByMonth(@Param("month") String month);

	List<Map<String, Object>> getSyncUserList();

	List<Map<String, Object>> getSyncBankAccountList();
	
	List<Map<String, Object>> getTravelEmpPage(Page pageCond, String date, String queryText, String authSql);

	List<Map<String, Object>> getSyncDeptList();

	List<Map<String, Object>> getHrUserList();
}
