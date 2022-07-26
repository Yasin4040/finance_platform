package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
/**
 * 银行账户excel
 * @author shubo
 *
 */
@Data
@ContentRowHeight(20) // 设置 Cell 高度 为12
@HeadRowHeight(24) // 设置表头 高度 为 12
public class BankAccountExcelData {
   
    @ExcelProperty(value = "工号")
    private String code;

    @ExcelProperty(value = "名称")
    private String pname;
    
    private String deptId;
    
    @ExcelProperty(value = "部门名称")
    private String deptName;
    
    @ExcelProperty(value = "户名")
    private String accountName;
    
    @ExcelProperty(value = "银行账号")
    private String bankAccount;

    @ExcelProperty(value = "银行类型")
    private String bankName;

    @ExcelProperty(value = "省")
    private String province;

    @ExcelProperty(value = "市")
    private String city;

    @ExcelProperty(value = "开户行")
    private String subBranchName;
}
