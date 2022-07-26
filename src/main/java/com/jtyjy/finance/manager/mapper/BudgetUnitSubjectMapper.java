package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetUnitSubject;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author Admin
 */
public interface BudgetUnitSubjectMapper extends BaseMapper<BudgetUnitSubject> {
	List<String> getUnitNameBySubId(@Param("subjectId") Long subjectId);
	
	Map<String, Object> countYearAgent(String subjectIds, Long yearId, Long unitId);

	List<Long> getUnitIdsBySubjectId(Long subjectId);
}
