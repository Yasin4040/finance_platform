package com.jtyjy.finance.manager.mapper.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 借款单冲账使用信息
 * @author User
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LendmoneyUseBean {

	/**
	 * 借款单主键
	 */
	private Long id;
	/**
	 * 借款单类型
	 */
	private Long lendtype;
	/**
	 * 借款金额
	 */
	private BigDecimal lendmoney;
	/**
	 * 已还金额
	 */
	private BigDecimal repaidmoney;
	/**
	 * 未还金额
	 */
	private BigDecimal unrepaymoney;
	/**
	 * 使用次数
	 */
	private Integer usecount;
	
	/**
	 * 生效标志
	 */
	private Integer effectflag;
	
	/**
	 * 达标状态,true为达标
	 */
	private Integer flushingflag;
}
