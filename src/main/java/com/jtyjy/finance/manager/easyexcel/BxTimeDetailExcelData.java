package com.jtyjy.finance.manager.easyexcel;

import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报销时间节点导出数据
 * @author shubo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BxTimeDetailExcelData {
    
    @ExcelProperty(value="报销单号")
	private String reimcode;

    @ExcelProperty(value="部门")
	private String unitname;

    @ExcelProperty(value="报销金额")
	private String bxje;

    @ExcelProperty(value="预算科目")
	private String subjectnames;

    @ExcelProperty(value="主开票单位")
    private String bunitname;

    @ExcelProperty(value="单据接收-单据提交")
    private BigDecimal t1;
    
    @ExcelProperty(value="票面审核-单据接收")
    private BigDecimal t2;
    
    @ExcelProperty(value="预算审核-票面审核")
    private BigDecimal t3;

    @ExcelProperty(value="分单扫描-预算审核")
    private BigDecimal t4;

    @ExcelProperty(value="分单确认-分单扫描")
    private BigDecimal t5;
    
    @ExcelProperty(value="出纳付款(接收)-分单确认")
    private BigDecimal t6;
    
    @ExcelProperty(value="出纳付款(付款)-单据接收")
    private BigDecimal t7;

    @ExcelProperty(value="单据提交")
    private String submittime;

    @ExcelProperty(value="单据接收")
    private String tt1;
    
    @ExcelProperty(value="票面审核")
    private String tt2;
    
    @ExcelProperty(value="预算审核")
    private String tt3;

    @ExcelProperty(value="分单扫描")
    private String tt4;

    @ExcelProperty(value="分单确认")
    private String tt5;
    
    @ExcelProperty(value="出纳付款(接收)")
    private String tt6; 
    
    @ExcelProperty(value="出纳付款(付款)")
    private String tt7;
}
