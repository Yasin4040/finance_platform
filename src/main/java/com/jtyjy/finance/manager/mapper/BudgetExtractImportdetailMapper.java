package com.jtyjy.finance.manager.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetExtractImportdetail;
import com.jtyjy.finance.manager.vo.ExtractImportDetailVO;

/**
 * @author Admin
 */
public interface BudgetExtractImportdetailMapper extends BaseMapper<BudgetExtractImportdetail> {

	void clearExtractDetail(@Param("ids")String ids);

	List<ExtractImportDetailVO> getExtractImportDetails(Page<ExtractImportDetailVO> pageCond, @Param("params")Map<String, Object> params);
	
}
