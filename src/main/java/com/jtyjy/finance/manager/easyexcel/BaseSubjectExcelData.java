package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

import io.swagger.annotations.ApiModel;
import lombok.Data;
/**
 * 基础科目excel
 * @author shubo
 *
 */
@ApiModel
@Data
@ContentRowHeight(20) // 设置 Cell 高度 为12
@HeadRowHeight(24) // 设置表头 高度 为 12
public class BaseSubjectExcelData {
    
    @ExcelProperty(value = "科目代码")
    @ColumnWidth(20)
    private String subCode;
    
    @ExcelProperty(value = "科目名称")
    @ColumnWidth(20)
    private String subName;

    @ExcelProperty(value = "排序号")
    @ColumnWidth(12)
    private String orderNo;
    
    @ExcelProperty(value = "备注")
    @ColumnWidth(12)
    private String remark;

    @ExcelProperty(value = "错误信息")
    @ColumnWidth(20)
    private String errMsg;
}
