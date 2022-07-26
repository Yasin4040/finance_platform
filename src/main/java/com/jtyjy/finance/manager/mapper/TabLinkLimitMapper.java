package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.TabLinkLimit;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
public interface TabLinkLimitMapper extends BaseMapper<TabLinkLimit> {
    
    List<TabLinkLimit> getLinkLimitInfo(Long procedureId, Long subjectId, String linkDm, String subjectName, Page pageCond);

	List<Map<String, Object>> list1();

	List<Map<String, Object>> list2();

	void updateLimit(String max_limit, String subject_id, String financial_manage_check);
}
