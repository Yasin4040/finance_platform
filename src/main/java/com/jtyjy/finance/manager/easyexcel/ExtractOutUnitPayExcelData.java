package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 提成外部户发放明细
 * @author minzhq
 * date 2022-08-30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractOutUnitPayExcelData {
	@ExcelProperty(value="户名")
	private String accountName;

	@ExcelProperty(value="卡号")
	private String bankAccount;

	@ExcelProperty(value="银行")
	private String bankName;
	
	@ExcelProperty(value="开户行")
	private String openBank;

	@ExcelProperty(value="省份")
	private String province;

	@ExcelProperty(value="城市")
	private String city;

	@ExcelProperty(value="发放金额")
	private BigDecimal payMoney;

}
