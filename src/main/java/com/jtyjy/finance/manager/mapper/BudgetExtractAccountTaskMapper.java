package com.jtyjy.finance.manager.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetExtractAccountTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.vo.ExtractAccountTaskDetailVO;
import com.jtyjy.finance.manager.vo.ExtractAccountTaskQueryVO;
import com.jtyjy.finance.manager.vo.ExtractAccountTaskResponseVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity com.jtyjy.finance.manager.bean.BudgetExtractAccountTask
 */
public interface BudgetExtractAccountTaskMapper extends BaseMapper<BudgetExtractAccountTask> {

	List<ExtractAccountTaskResponseVO> getExtractAccountTaskHistoryList(Page<ExtractAccountTaskResponseVO> pageCond,@Param("params") ExtractAccountTaskQueryVO params);

	List<ExtractAccountTaskResponseVO> getExtractAccountTaskList(Page<ExtractAccountTaskResponseVO> pageCond,@Param("params") ExtractAccountTaskQueryVO params);

	List<ExtractAccountTaskDetailVO> getExtractAccountTaskDetail(Page<ExtractAccountTaskDetailVO> pageCond, @Param("params")Map<String,Object> params);
}




