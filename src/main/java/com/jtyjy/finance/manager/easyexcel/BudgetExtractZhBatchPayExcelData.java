package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

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
	@ExcelProperty("付方账号")
	private String bunitAccount;
	@ExcelProperty("付方虚拟户编号")
	private String xnbh = "0000000001";
	@ExcelProperty("期望日")
	private String qwr = "20080808";
	@ExcelProperty("期望时间")
	private String qwsj = "080000";
	@ExcelProperty("用途")
	private String yt = "JJ";
	@ExcelProperty("金额")
	private BigDecimal payMoney;
	@ExcelProperty("收方联行号")
	private String branchCode;
	@ExcelProperty("收方开户银行")
	private String bankName;
	@ExcelProperty("业务摘要")
	private String remark;

}
