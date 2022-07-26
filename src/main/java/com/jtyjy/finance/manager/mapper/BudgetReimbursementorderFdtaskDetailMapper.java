package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetReimbursementorderFdtaskDetail;
import com.jtyjy.finance.manager.vo.BxDetailVO;
import com.jtyjy.finance.manager.vo.MakeAccountTaskVO;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author Admin
 */
public interface BudgetReimbursementorderFdtaskDetailMapper extends BaseMapper<BudgetReimbursementorderFdtaskDetail> {
    /**
     * 分页查询分单任务
     * @param pageCond
     * @param reimcode 报销单号
     * @param authSql
     * @return
     */
	List<MakeAccountTaskVO> getFdDetailPageInfo(Page<MakeAccountTaskVO> pageCond, String reimcode, String authSql);
}
