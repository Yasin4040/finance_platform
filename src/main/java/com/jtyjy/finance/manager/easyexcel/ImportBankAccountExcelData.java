package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 银行账号导入
 * @author shubo
 *
 */
@ApiModel
@Data
@ContentRowHeight(20) // 设置 Cell 高度 为12
@HeadRowHeight(24) // 设置表头 高度 为 12
public class ImportBankAccountExcelData {
    
    @ExcelProperty(value = "编号")
    @ColumnWidth(20)
    private String code;

    @ExcelProperty(value = "名称")
    @ColumnWidth(20)
    private String pname;
    
    @ExcelProperty(value = "账户类型")
    @ColumnWidth(12)
    private String accountType;

    @ExcelProperty(value = "户名")
    @ColumnWidth(20)
    private String accountName;

    @ExcelProperty(value = "银行账号")
    @ColumnWidth(20)
    private String bankAccount;

    @ExcelProperty(value = "开户行联行号")
    @ColumnWidth(20)
    private String branchCode;

    @ExcelProperty(value = "工资卡")
    @ColumnWidth(12)
    private String wagesFlag;
    
    @ExcelProperty(value = "备注")
    @ColumnWidth(20)
    private String remark;

    @ExcelProperty(value = "错误信息")
    @ColumnWidth(12)
    private String errMsg;


}
