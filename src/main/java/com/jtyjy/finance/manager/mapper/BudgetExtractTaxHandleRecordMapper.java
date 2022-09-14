package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetExtractTaxHandleRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.jtyjy.finance.manager.bean.BudgetExtractTaxHandleRecord
 */
public interface BudgetExtractTaxHandleRecordMapper extends BaseMapper<BudgetExtractTaxHandleRecord> {

	Integer getOldBatchUnHandleCount(@Param("extractBatch") String extractBatch);
}




