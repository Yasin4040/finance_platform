package com.jtyjy.finance.manager.easyexcel;


import javax.validation.constraints.NotBlank;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel
@Data
@ContentRowHeight(20) // 设置 Cell 高度 为12
@HeadRowHeight(24) // 设置表头 高度 为 12
public class PayVerifyExcelData {
    
    @NotBlank(message = "付款单号不能为空")
    @ExcelProperty(value = "付款单号")
    @ColumnWidth(20)
    private String paymoneycode;
    
    @NotBlank(message = "收款人账户不能为空")
    @ExcelProperty(value = "收款人账户")
    @ColumnWidth(20)
    private String bankaccount;
    
    @NotBlank(message = "收款人名称不能为空")
    @ExcelProperty(value = "收款人名称")
    @ColumnWidth(20)
    private String bankaccountname;
    
    @NotBlank(message = "收方开户支行不能为空")
    @ExcelProperty(value = "收方开户支行")
    @ColumnWidth(30)
    private String openbank;
    
    @NotBlank(message = "付款金额不能为空")
    @ExcelProperty(value = "付款金额")
    @ColumnWidth(20)
    private String paymoney;
    
    @NotBlank(message = "收方电子联行号不能为空")
    @ExcelProperty(value = "收方电子联行号")
    @ColumnWidth(20)
    private String bankaccountbranchcode;
    
    @NotBlank(message = "收方开户银行类型不能为空")
    @ExcelProperty(value = "收方开户银行类型")
    @ColumnWidth(20)
    private String bankaccountbranchname;
    
    @NotBlank(message = "付款失败原因不能为空")
    @ExcelProperty(value = "付款失败原因")
    @ColumnWidth(20)
    private String verifyremark;
    
    @ExcelProperty(value = "错误信息")
    @ColumnWidth(20)   
    private String errMsg;
}
