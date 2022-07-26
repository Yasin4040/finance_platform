package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetProduct;
import com.jtyjy.finance.manager.bean.BudgetUnit;
import com.jtyjy.finance.manager.vo.BudgetUnitSubjectVO;
import com.jtyjy.finance.manager.vo.BudgetUnitVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

/**
 * @author Admin
 */
public interface BudgetUnitMapper extends BaseMapper<BudgetUnit> {

    /**
     * 是否省区预算单位
     */
    Integer isProvinceUnit(@Param("baseUnitParentId") Long baseUnitParentId, @Param("baseUnitId") Long baseUnitId, @Param("unitId") Long unitId);

    List<BudgetUnitVO> getBudgetUnit(Long yearId, String unitName, String authSql, String userId);

    @MapKey("USER_ID")
    Map<String, Map<String, Object>> queryAllUserName();

    @MapKey("DEPT_ID")
    Map<String, Map<String, Object>> queryAllDeptName();

    List<Map<String, Object>> getUnitSubByUnit(Long unitId);

    List<BudgetUnitSubjectVO> getSubInfoByYear(Long yearId);

    /**
     * 查询年度审核列表
     *
     * @param pageBean 分页
     * @param paramMap 查询条件
     * @return 结果集
     */
    List<BudgetUnit> listYearAuditPage(@Param("pageBean") Page<BudgetUnit> pageBean, @Param("paramMap") HashMap<String, Object> paramMap);

    /**
     * 获取预算科目的产品一类
     * @param unitId
     * @return
     */
    List<Map<String, Object>> getSubProductByUnit(Long unitId);

    /**
     * 获取预算单位子单位的预算科目
     * @param unitId
     * @return
     */
    List<Map<String, Object>> getUnitSonSub(Long parentId, Long unitId, Long yearId);

    /**
     * 获取产品分类下所有产品
     * @param cid 产品分类id
     * @param name 
     * @return
     */
    List<BudgetProduct> getProductByCid(String cid, String name);
    
    /**
     * 根据用户id查询包含预算员包括此用户的所有预算单位
     * @param userId 用户id
     * @return
     */
    List<BudgetUnit> getUnitByUserId(String userId);

	List<BudgetUnitVO> getBudgetUnitNoAuth(Long yearId, String unitName);	

//    @MapKey("id")
//    Map<Long, BudgetUnitVO> queryAllUnitByYearId(Long yearId);
	
	List<BudgetUnitVO> queryAllUnitByYearId(Long yearId);

    List<Map<String,Object>> getBudgetUnitForAsset(@Param("yearId") Long yearId);
}
