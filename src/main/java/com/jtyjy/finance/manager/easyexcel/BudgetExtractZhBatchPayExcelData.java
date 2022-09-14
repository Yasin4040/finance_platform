package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 描述：<p></p>
 *
 * @author minzhq
 * @since 2022/9/14
 */
@Data
public class BudgetExtractZhBatchPayExcelData {
	@ExcelProperty("业务参考号")
	private String ywckh;

	@ExcelProperty("收款人编号")
	private String skrbh;

	@ExcelProperty("收款人账号")
	private String bankAccount;

	@ExcelProperty("收款人名称")
	private String bankAccountName;
	@ExcelProperty("收方开户支行")
	private String openBank;
	@ExcelProperty("收款人所在省")
	private String province;
	@ExcelProperty("收款人所在市")
	private String city;
	@ExcelProperty("收方邮件地址")
	private String yj;
	@ExcelProperty("收方移动电话")
	private String dh;
	@ExcelProperty("币种")
	private String bz = "人民币";
	@ExcelProperty("付款分行")
	private String fh = "南昌";
	@ExcelProperty("结算方式")
	private String jsfs = "快速";
	@ExcelProperty("业务种类")
	private String ywzl = "普通汇兑";
	//业务参考号	收款人编号	收款人账号	收款人名称											付方虚拟户编号	期望日	期望时间	用途	金额	收方联行号	收方开户银行	业务摘要
	//{.ywckh}	{.skrbh}	{.bankAccount}	{.}	{.}	{.}	{.}	{.}	{.}	{.}	{.}	{.}	{.}	{.bunitAccount}	{.xnbh}	{.qwr}	{.qwsj}	{.yt}	{.payMoney}	{.branchCode}	{.bankName}	{.remark}

	@ExcelProperty("付方账号")
	private String bunitAccount;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
//	@ExcelProperty("收款人所在省")
//	private String province;
}
