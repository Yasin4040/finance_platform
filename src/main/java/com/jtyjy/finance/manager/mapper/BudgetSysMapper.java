package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetMonthAgent;
import com.jtyjy.finance.manager.bean.BudgetYearAgent;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


public interface BudgetSysMapper {

    List<Map<String, Object>> getSplitSubjectData(@Param("yearid") Long yearid);

    List<Map<String, Object>> getNewSplitYearAgent(@Param("unitId") Long unitId);

    List<Map<String, Object>> getCheckedUnitProductList(@Param("unitId") Long unitId);

    List<Map<String, Object>> getCurUnitProductSubjectList(@Param("unitId") Long unitId);

    List<Map<String, Object>> getAllSubjects(@Param("yearid") Long yearid, @Param("unitId") Long unitId);

    void clearYearSubject(@Param("yearId") Long yearId, @Param("unitId") Long unitId);

    void clearYearSubjectHis(@Param("yearId") Long yearId, @Param("unitId") Long unitId);

    List<Map<String, Object>> getYearSubectList(@Param("yearid") Long yearid, @Param("unitId") Long unitId);

    List<Map<String, Object>> getParentUnitSubjectDataList(@Param("unitId") Long unitId);

    void resetMonthStartFlag();

    void clearMonthData(@Param("unitId") Long unitId, @Param("monthid") String monthid);
    
    void clearHisMonthData(@Param("unitId") Long unitId, @Param("monthid") String monthid);

    List<Map<String, Object>> getExecuteData(@Param("unitId") Long unitid, @Param("monthids") String monthids,
                                             @Param("bxStatus") Integer bxStatus);

    List<Map<String, Object>> getYearAddData(@Param("unitId") Long unitid, @Param("enddate") String enddate);

    List<Map<String, Object>> getYearLendingData(@Param("unitId") Long unitid, @Param("enddate") String enddate);

    List<Map<String, Object>> getYearLendoutData(@Param("unitId") Long unitid, @Param("enddate") String enddate);

    List<Map<String, Object>> getERPLendData(@Param("unitId") Long unitid, @Param("enddate") String enddate);

    List<Map<String, Object>> getYearSubjectMap(@Param("unitId") Long unitid);

    List<BudgetMonthAgent> getAllAgents(@Param("yearid") Long yearid, @Param("unitId") Long unitid, @Param("monthid") Long monthid);

    void setMonthBusiness(@Param("monthbusiness") String monthbusiness, @Param("yearid") Long yearid, @Param("unitId") Long unitid, @Param("monthid") Long monthid, @Param("subjectid") Long subjectid);

    List<Map<String, Object>> getMonthBussinessList(@Param("yearid") Long yearid, @Param("unitId") Long unitid, @Param("monthid") Long monthid);

    List<Map<String, Object>> getMonthSubjectMaps(@Param("yearid") Long yearid, @Param("unitId") Long unitid, @Param("monthid") Long monthid);

    List<Map<String, Object>> getPsubjectList(@Param("unitId") Long unitid, @Param("monthid") Long monthid);

    @Select("SELECT _yearagent.* FROM budget_year_agent _yearagent INNER JOIN budget_unit _unit ON _unit.yearid = _yearagent.yearid AND _unit.id = _yearagent.unitid WHERE _unit.yearid=#{yearid} AND (_unit.id=#{unitid} or _unit.parentid=#{pid}) ")
	List<BudgetYearAgent> getYearAgents(@Param("yearid")Long yearid, @Param("unitid")Long unitid, @Param("pid")Long pid);

	void refreshTotal(@Param("total") BigDecimal total, @Param("ids")List<Long> ids);

	void modifyUpdateflagOfunitmonth(@Param("date")Date date, @Param("unitid")Long unitid,@Param("monthid") Long monthid);

	List<Map<String, Object>> getUnitExecuteData(@Param("unitId")Long unitId, @Param("status")Integer status);

	List<Map<String, Object>> getUnitYearAddData(@Param("unitId")Long unitId);

	List<Map<String, Object>> getUnitMonthAddData(@Param("unitId")Long unitId,@Param("monthId") Long monthId);

	List<Map<String, Object>> getUnitYearMonthAddData(@Param("unitId")Long unitId, @Param("monthId")Long monthId);

	List<Map<String, Object>> getUnitYearLendData(@Param("unitId")Long unitId);

}
