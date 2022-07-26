package com.jtyjy.finance.manager.mapper.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报销校验信息
 * @author User
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReimbursementValidateMoney {
	/**
	 * 订单主键
	 */
	private Long orderId;
	/**
	 * 界别主键
	 */
	private Long yearId;
	/**
	 * 月份主键
	 */
	private Long monthId;
	/**
	 * 预算单位主键
	 */
	private Long unitId;
	/**
	 * 动因主键
	 */
	private Long agentId;
	/**
	 * 预算科目主键
	 */
	private Long subjectId;
	/**
	 * 预算总额
	 */
	private BigDecimal total = BigDecimal.ZERO;
	/**
	 * 追加总额
	 */
	private BigDecimal addmoney = BigDecimal.ZERO;
	/**
	 * 拆进总额
	 */
	private BigDecimal lendinmoney= BigDecimal.ZERO;
	/**
	 * 拆出总额
	 */
	private BigDecimal lendoutmoney= BigDecimal.ZERO;
	/**
	 * 已报销总额
	 */
	private BigDecimal bxmoney= BigDecimal.ZERO;
	/**
	 * 已锁定总额
	 */
	private BigDecimal sdmoney= BigDecimal.ZERO;
	/**
	 * 划拨总额
	 */
	private BigDecimal hbmoney= BigDecimal.ZERO;
	/**
	 * 划拨锁定总额
	 */
	private BigDecimal hbsdmoney= BigDecimal.ZERO;
	
	/**
	 * 查询条件构造器
	 * @param orderId
	 * @param yearId
	 * @param monthId
	 * @param unitId
	 * @param agentId
	 * @param subjectId
	 */
	public ReimbursementValidateMoney(Long orderId, Long yearId, Long monthId, Long unitId, Long agentId,Long subjectId) {
		super();
		this.orderId = orderId;
		this.yearId = yearId;
		this.monthId = monthId;
		this.unitId = unitId;
		this.agentId = agentId;
		this.subjectId = subjectId;
	}
	
	/**
	 * 获取可执行金额
	 * @return
	 */
	public BigDecimal execMoney() {
		BigDecimal all = this.total.add(this.addmoney).add(this.lendinmoney);
		BigDecimal subtract = this.bxmoney.add(this.lendoutmoney).add(this.hbmoney).add(this.hbsdmoney).add(this.sdmoney);
		return all.subtract(subtract);
	}

}
