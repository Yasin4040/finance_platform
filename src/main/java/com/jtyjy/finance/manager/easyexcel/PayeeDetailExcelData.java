package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收款明细信息
 * @author shubo
 *
 */
@Data
@HeadStyle()
@ContentStyle()
@NoArgsConstructor
@AllArgsConstructor
public class PayeeDetailExcelData {
	@ExcelProperty("收款人帐号")
	@ColumnWidth(20)
	private String payeeAccount;
	
	@ExcelProperty("收款人名称")
	@ColumnWidth(20)
	private String payeeName;
	
	@ExcelProperty("收方开户支行")
	@ColumnWidth(20)
	private String payeeBankName;
	
	@ExcelProperty("收款人所在省")
	@ColumnWidth(20)
	private String payeeProvice;
	
	@ExcelProperty("收款人所在市")
	@ColumnWidth(20)
	private String payeeCity;
	
	@ExcelProperty("付款金额")
	@ColumnWidth(20)
	private BigDecimal payMoney;
	
	@ExcelProperty("收方电子联行号")
	@ColumnWidth(20)
	private String payeeBankCode;
	
	@ExcelProperty("收方开户银行类型")
	@ColumnWidth(20)
	private String payeeBankType;

}
