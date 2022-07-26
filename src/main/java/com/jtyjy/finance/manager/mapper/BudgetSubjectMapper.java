package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetSubject;
import com.jtyjy.finance.manager.vo.SubjectInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
public interface BudgetSubjectMapper extends BaseMapper<BudgetSubject> {

    /**
     * 获取不同类型的科目Id
     *
     * @param yearId     届别Id
     * @param baseUnitId 单位Id
     * @param type       1一般动因 2产品动因 3分解动因
     * @return 科目Id集合
     */
    List<String> listSubjectIds(@Param("yearId") Long yearId, @Param("baseUnitId") Long baseUnitId, @Param("type") Integer type);

    /**
     * 获取预算科目（产品）
     *
     * @param yearId       届别Id
     * @param budgetUnitId 预算单位Id
     * @param type         类型（1普通 2产品 3分解）
     * @return 预算科目集合
     */
    List<BudgetSubject> listSubjectByType(@Param("yearId") Long yearId, @Param("budgetUnitId") Long budgetUnitId, @Param("type") Integer type);

    /**
     * 根据预算单位Id和科目名称查找科目
     *
     * @param budgetUnitId 预算单位Id
     * @param subjectName  预算科目名称
     * @param type         类型（1普通 2产品 3分解）
     * @return 预算科目对象
     */
    BudgetSubject getSubjectByUnitIdAndSubjectName(@Param("budgetUnitId") Long budgetUnitId, @Param("subjectName") String subjectName, @Param("type") Integer type);

    /**
     * 查询分解科目中遗漏的预算单位科目
     *
     * @param budgetUnitId 预算单位Id
     * @return 预算科目集合
     */
    List<BudgetSubject> listMissingSubject(@Param("budgetUnitId") Long budgetUnitId);

    /**
     * 查询预算单位下预算产品科目
     *
     * @param budgetUnitId 预算单位Id
     * @return 预算科目集合
     */
    List<BudgetSubject> listProductSubject(@Param("budgetUnitId") Long budgetUnitId);

    /**
     * 查询预算单位下所有科目
     *
     * @param yearId       届别Id
     * @param budgetUnitId 预算单位Id
     * @return 预算科目集合
     */
    List<BudgetSubject> listSubjectByUnitId(@Param("yearId") Long yearId, @Param("budgetUnitId") Long budgetUnitId);

    /**
     * 查询届别下的预算科目信息
     *
     * @param yearId 届别Id
     * @param subName 
     * @return 结果集
     */
    List<SubjectInfoVO> getSubjectByYearId(@Param("yearId") Long yearId, @Param("subName") String subName, @Param("stopFlag") Integer stopFlag);

    List<BudgetSubject> querySubByIds(@Param("subjectIds") String subjectIds);

    /**
     * 查询预算单位下所有科目
     *
     * @param paramMap 查询条件
     * @return 结果集
     */
    List<BudgetSubject> listSubjectByMap(@Param("map") Map<String, Object> paramMap);

    /**
     * 获取年度(月度)可追加科目
     *
     * @param budgetUnitId 预算单位Id
     * @return 结果集
     */
    List<BudgetSubject> listCanAddSubjects(@Param("budgetUnitId") Long budgetUnitId);

    /**
     * 获取可拆借预算科目
     *
     * @param budgetUnitId 预算单位Id
     * @return 结果集
     */
    List<BudgetSubject> listLendSubjects(@Param("budgetUnitId") Long budgetUnitId);
  
    /**
     * 获取当前届别所有科目的金蝶科目代码
     * @param yearId
     * @return
     */
    List<Map<String, Object>> getJindieCodeByYearId(Long yearId);

    /**
     * 获取科目信息（固定资产）
     * @param
     * @return
     */
    List<BudgetSubject> getAssetSubjectInfo(@Param("map")HashMap<String, Object> paramMap);
}
