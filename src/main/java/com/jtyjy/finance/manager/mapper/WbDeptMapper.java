package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.WbDept;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author Admin
 */
public interface WbDeptMapper extends BaseMapper<WbDept> {
	
    List<WbDept> getDeptInfo(String deptName);
}
