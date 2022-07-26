package com.jtyjy.finance.manager.easyexcel;

import java.util.Map;

import javax.validation.constraints.NotBlank;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 付款失败修改导入
 * @author shubo
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayErrorImportExcelData{
	
    @NotBlank(message = "付款单号不能为空")
	@ExcelProperty(value="付款单号")
	@ColumnWidth(30)
	private String payMoneyCode;
	
    @NotBlank(message = "单据号不能为空")
	@ExcelProperty(value="单据号")
	private String payObjectCode;
	
    @NotBlank(message = "付款批次号不能为空")
	@ExcelProperty(value="付款批次号")
	private String payBatchCode;
	
    @NotBlank(message = "收款人帐号不能为空")
    @ColumnWidth(30)
	@ExcelProperty(value="收款人帐号")
	private String bankAccount;
	
    @NotBlank(message = "收款人名称不能为空")
	@ExcelProperty(value="收款人名称")
	@ColumnWidth(30)
	private String bankAccountName;
	
    @NotBlank(message = "收方开户支行不能为空")
	@ExcelProperty(value="收方开户支行")
	@ColumnWidth(30)
	private String bankBranchName;
	
    @NotBlank(message = "付款金额不能为空")
	@ExcelProperty(value="付款金额")
	@ColumnWidth(30)
	private String payMoney;	
	   
    @NotBlank(message = "收方电子联行号不能为空")
    @ExcelProperty(value="收方电子联行号")
    @ColumnWidth(30)
    private String bankBranchCode;
    
    @NotBlank(message = "收方开户银行类型不能为空")
    @ExcelProperty(value="收方开户银行类型")
    @ColumnWidth(30)
    private String openBankType;
    
	@ExcelProperty(value="错误明细")
	@ColumnWidth(30)
	private String errMsg;
	
	public PayErrorImportExcelData(Map<Integer, String> data) {
	    this.payMoneyCode = data.get(0);
	    this.payObjectCode = data.get(1);
	    this.payBatchCode = data.get(2);
	    this.bankAccount = data.get(3);
	    this.bankAccountName = data.get(4);
	    this.bankBranchName = data.get(5);
	    this.payMoney = data.get(6);
	    this.bankBranchCode = data.get(7);
	    this.openBankType = data.get(8);
	    if (data.size() > 8 && StringUtils.isNotBlank(data.get(data.size()-1))) {
	        this.errMsg = data.get(data.size()-1);
	    }
	}
}
