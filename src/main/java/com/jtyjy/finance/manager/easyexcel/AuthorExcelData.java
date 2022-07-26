package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

import io.swagger.annotations.ApiModel;
import lombok.Data;
/**
 * 稿费作者excel
 * @author shubo
 *
 */
@ApiModel
@Data
@ContentRowHeight(20) // 设置 Cell 高度 为12
@HeadRowHeight(24) // 设置表头 高度 为 12
public class AuthorExcelData {
    
    @ExcelProperty(value = "作者名字")
    @ColumnWidth(12)
    private String author;

    @ExcelProperty(value = "身份证号（个人作者必填）")
    @ColumnWidth(20)
    private String idnumber;
    
    @ExcelProperty(value = "纳税人识别号（单位作者必填）")
    @ColumnWidth(20)
    private String taxpayernumber;

    @ExcelProperty(value = "是否公司员工（是或否）")
    @ColumnWidth(20)
    private String authortype;

    @ExcelProperty(value = "所在单位")
    @ColumnWidth(12)
    private String company;

    @ExcelProperty(value = "收款银行账号")
    @ColumnWidth(12)
    private String bankaccount;

    @ExcelProperty(value = "电子联行号")
    @ColumnWidth(12)
    private String branchcode;


    @ExcelProperty(value = "银行")
    @ColumnWidth(12)
    private String bankName;

    @ExcelProperty(value = "省份")
    @ColumnWidth(12)
    private String province;

    @ExcelProperty(value = "城市")
    @ColumnWidth(12)
    private String city;

    @ExcelProperty(value = "支行")
    @ColumnWidth(12)
    private String childBankName;


    @ExcelProperty(value = "备注")
    @ColumnWidth(20)
    private String remark;
    
    @ExcelProperty(value = "错误信息")
    @ColumnWidth(20)
    private String errMsg;
}
