package com.jtyjy.finance.manager.dto.bxExcel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.jtyjy.finance.manager.vo.BankAccountVO;
import com.jtyjy.finance.manager.vo.BillingUnitAccountVO;
import com.klcwqy.easyexcel.anno.Location;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferDetailDto {

	@Location(column = 0)
    @NotBlank(message = "收款人名称（户名）不能为空")
	private String name;
	@Location(column = 1)
    @NotBlank(message = "收款人账号（银行卡号）不能为空")
	private String account;
	@Location(column = 2)
    @NotNull(message = "转账金额不能为空")
	private Double money;
	@Location(column = 3)
    @NotBlank(message = "付款单位不能为空")
	private String unit;
	
	private BankAccountVO bankAccountInfo;
	
	private BillingUnitAccountVO unitAccountInfo;
	@Override
	public String toString() {
		return "Transfer [name=" + name + ", account=" + account + ", money=" + money + ", unit=" + unit + "]";
	}
}
