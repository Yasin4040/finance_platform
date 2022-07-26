package com.jtyjy.finance.manager.mapper;

import com.jtyjy.finance.manager.bean.BudgetReimbursementorderScanlog;
import com.jtyjy.finance.manager.vo.BxLiuZhuanVO;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author Admin
 */
public interface BudgetReimbursementorderScanlogMapper extends BaseMapper<BudgetReimbursementorderScanlog> {
    /**
     * 分页查询
     * @param pageCond
     * @param reimcode 报销单号
     * @param authSql
     * @return
     */
    List<BxLiuZhuanVO> getLzPageInfo(Page pageCond, String reimcode, String authSql);
}
