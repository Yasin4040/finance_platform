package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetExtractPersonalityPayDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.vo.ExtractPersonalityPayDetailQueryVO;
import com.jtyjy.finance.manager.vo.ExtractPersonalityPayDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.jtyjy.finance.manager.bean.BudgetExtractPersonalityPayDetail
 */
public interface BudgetExtractPersonalityPayDetailMapper extends BaseMapper<BudgetExtractPersonalityPayDetail> {

	String  getValidLastExtractBatch(@Param("extractBatch") String extractBatch);

	List<ExtractPersonalityPayDetailVO> getExtractPersonalityPayDetailVO(Page<ExtractPersonalityPayDetailVO> pageCond,@Param("params") ExtractPersonalityPayDetailQueryVO params, @Param("extractBatch")String extractBatch);
}




