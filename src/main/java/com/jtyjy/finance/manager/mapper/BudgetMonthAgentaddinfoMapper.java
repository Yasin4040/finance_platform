package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetMonthAgentaddinfo;
import com.jtyjy.finance.manager.vo.BudgetMonthAddInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * @author Admin
 */
public interface BudgetMonthAgentaddinfoMapper extends BaseMapper<BudgetMonthAgentaddinfo> {

    /**
     * 查询年度追加（分页）
     *
     * @param pageBean 分页
     * @param paramMap 查询条件
     * @return 结果集
     */
    List<BudgetMonthAddInfoVO> listMonthAgentAddInfoPage(@Param("bean") Page<BudgetMonthAddInfoVO> pageBean, @Param("map") HashMap<String, Object> paramMap);

    /**
     * 查询条件查询月度预算追加记录
     *
     * @param paramMap 条件
     * @return 月度追加集合
     */
    List<BudgetMonthAgentaddinfo> listMonthAgentAddInfoByMap(HashMap<String, Object> paramMap);
}
