package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.finance.manager.mapper.BudgetPaymoneyMapper;
import com.jtyjy.finance.manager.vo.BudgetExtractPayQueryVO;
import com.jtyjy.finance.manager.vo.BudgetExtractPayResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/14
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JdbcSelector(value = "defaultJdbcTemplateService")
@SuppressWarnings("all")
public class BudgetExtractPayService {

	@Autowired
	private BudgetPaymoneyMapper paymoneyMapper;

	/**
	 * <p>获取提成付款单</p>
	 * @author minzhq
	 * @date 2022/9/14 10:51
	 * @param params
	 * @param page
	 * @param rows
	 */
	public PageResult<BudgetExtractPayResponseVO> getExtractPayMoneyList(BudgetExtractPayQueryVO params, Integer page, Integer rows) {
		Page<BudgetExtractPayResponseVO> pageCond = new Page<>(page, rows);
		List<BudgetExtractPayResponseVO> resultList = paymoneyMapper.getExtractPayMoneyList(pageCond, params);
		return PageResult.apply(pageCond.getTotal(), resultList);
	}
}
