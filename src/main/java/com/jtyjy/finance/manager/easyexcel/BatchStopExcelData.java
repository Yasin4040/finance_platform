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
public class BatchStopExcelData {

    @ExcelProperty(value = "银行账号")
    @ColumnWidth(20)
    private String bankAccount;

}
