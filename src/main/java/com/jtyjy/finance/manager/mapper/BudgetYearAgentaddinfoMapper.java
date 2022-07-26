package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetYearAgentaddinfo;
import com.jtyjy.finance.manager.vo.BudgetYearAddInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
public interface BudgetYearAgentaddinfoMapper extends BaseMapper<BudgetYearAgentaddinfo> {

    /**
     * 查询年度追加（分页）
     *
     * @param pageBean 分页
     * @param paramMap 条件参数
     * @return 年度追加集合
     */
    List<BudgetYearAddInfoVO> listYearAgentAddInfoPage(@Param("pageBean") Page<BudgetYearAddInfoVO> pageBean, @Param("map") HashMap<String, Object> paramMap);

    /**
     * 查询年度动因追加列表
     *
     * @param infoId infoId
     * @return 年度追加集合
     */
    List<Map<String, Object>> listYearAgentAddByInfoId(@Param("infoId") Long infoId);

    /**
     * 查询条件查询年度预算追加记录
     *
     * @param paramMap 条件
     * @return 年度追加集合
     */
    List<BudgetYearAgentaddinfo> listYearAgentAddInfoByMap(Map<String, Object> paramMap);
}
