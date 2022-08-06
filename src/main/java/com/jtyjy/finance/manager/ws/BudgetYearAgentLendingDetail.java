package com.jtyjy.finance.manager.ws;

import com.jtyjy.ecology.webservice.workflow.WorkflowBase;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 描述：<p>拆借明细</p>
 *
 * @author minzhq
 * @since 2022/8/5
 */
@Data
public class BudgetYearAgentLendingDetail extends WorkflowBase {

	private Long sjid;

	//拆进预算单位
	private String cjysdw;

	//拆进科目
	private String cjkm;

	//拆进动因
	private String cjdy;

	//拆进后年度预算
	private BigDecimal cjhndys;

	//拆出预算单位
	private String ccyysdw;

	//拆出科目
	private String cckm;

	//拆出动因
	private String ccdy;

	//拆出后年度预算
	private BigDecimal cjhndysu;

	//拆借金额
	private BigDecimal cjje;

	//拆借原因
	private String cjyy;

	//是否申请免罚
	private String sfsqmf;

	//免罚理由说明
	private String mflysm;

	//免罚结果
	private String mfjg;

	//罚款理由说明
	private String mflysmi;
}
