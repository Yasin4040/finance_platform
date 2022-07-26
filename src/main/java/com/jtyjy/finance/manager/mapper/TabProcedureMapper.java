package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.TabProcedure;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface TabProcedureMapper extends BaseMapper<TabProcedure> {
	
    List<TabProcedure> getProcedureInfo(String isActive, String procedureType, String linkOrder, Long yearId, String name);

}
