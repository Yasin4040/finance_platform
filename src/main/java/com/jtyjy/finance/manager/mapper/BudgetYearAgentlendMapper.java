package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetYearAgentlend;
import com.jtyjy.finance.manager.vo.BudgetYearAgentLendVO;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * @author Admin
 */
public interface BudgetYearAgentlendMapper extends BaseMapper<BudgetYearAgentlend> {

    /**
     * 查询预算拆借列表（分页）
     *
     * @param pageBean 分页
     * @param paramMap 条件
     * @return 结果集
     */
    List<BudgetYearAgentLendVO> listYearAgentLendPage(@Param("pageBean") Page<BudgetYearAgentLendVO> pageBean, @Param("paramMap") HashMap<String, Object> paramMap);
}
