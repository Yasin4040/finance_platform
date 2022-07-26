package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetBaseSubject;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author Admin
 */
public interface BudgetBaseSubjectMapper extends BaseMapper<BudgetBaseSubject> {
	
 // 查询开票单位信息
    List<BudgetBaseSubject> getBaseSubjectPageList(Page<BudgetBaseSubject> pageCond,
                                                 String name, Integer stopflag,
                                                 String authSql);
}
