package com.jtyjy.finance.manager.constants;

/**
 * 报销审核的时间节点记录常量
 */
public class ReimbursentTimeDetailConstant {
	// 1:票面接收 - 单据提交
	public final static Integer ONE = 1;

	//2：票面审核-票面接收
	public final static Integer TWO = 2;

	//3:预算审核-票面审核
	public final static Integer THREE = 3;

	//4扫描分单-预算审核 (财务总监、总经理)
	public final static Integer FOUR = 4;

	//5 做账 - 扫描分单
	public final static Integer FIVE = 5;

	//6 出纳接收-做账
	public final static Integer SIX = 6;

	//财务总监 - 预算审核
	public final static Integer SEVEN = 7;

	//总经理 - 财务总监
	public final static Integer EIGHT = 8;

}
