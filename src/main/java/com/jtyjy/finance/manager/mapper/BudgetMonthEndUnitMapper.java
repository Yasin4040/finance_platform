package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetMonthEndUnit;
import com.jtyjy.finance.manager.vo.BudgetUnitVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * @author Admin
 */
public interface BudgetMonthEndUnitMapper extends BaseMapper<BudgetMonthEndUnit> {
    /**
     * 获取月结单位信息
     *
     * @param yearId
     * @param monthId
     * @return
     */
    List<BudgetUnitVO> getUnitMonthEndTime(Long yearId, Long monthId);

    /**
     * 获取届别下所有单位
     *
     * @param yearId
     * @return allunitids（以逗号给开的单位id字符串）
     */
    String getUnitGroup(Long yearId);

    /**
     * 查询月度审核列表（分页）
     *
     * @param pageBean 分页
     * @param paramMap 查询条件
     * @return 结果集
     */
    List<BudgetMonthEndUnit> listMonthAuditPage(@Param("pageBean") Page<BudgetMonthEndUnit> pageBean, @Param("paramMap") HashMap<String, Object> paramMap);
}
