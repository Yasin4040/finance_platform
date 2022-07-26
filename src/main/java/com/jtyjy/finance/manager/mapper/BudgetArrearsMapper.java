package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetArrears;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * @author Admin
 */
public interface BudgetArrearsMapper extends BaseMapper<BudgetArrears> {

    /**
     * 查询员工台账（分页）
     *
     * @param pageBean 分页
     * @param paramMap 查询条件
     * @return 结果集
     */
    List<BudgetArrears> listArrearsPage(@Param("pageBean") Page<BudgetArrears> pageBean, @Param("paramMap") HashMap<String, Object> paramMap);
}
