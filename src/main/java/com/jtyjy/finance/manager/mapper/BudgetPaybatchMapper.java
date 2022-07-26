package com.jtyjy.finance.manager.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.result.KVBean;
import com.jtyjy.finance.manager.bean.BudgetPaybatch;

/**
 * @author Admin
 */
@Mapper
public interface BudgetPaybatchMapper extends BaseMapper<BudgetPaybatch> {

	/**
	 * 根据报销单号查询预算单位下预算管理员：k:预算单位主键 v:预算管理员主键
	 * @param paymoneyobjectcode
	 * @return
	 */
	List<KVBean> getManagerOfBudgetUnitByBxCode(@Param("code") String code);

	/**
	 * 查询每一个预算单位划拨单总额
	 * @param code
	 * @return
	 */
	List<KVBean> getUnitAllocateMoney(@Param("code") String code);
	
}
