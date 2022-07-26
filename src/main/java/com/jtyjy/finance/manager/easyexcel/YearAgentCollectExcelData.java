package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 年度动因汇总
 *
 * @author User
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YearAgentCollectExcelData {

    @ExcelProperty(value = "预算科目")
    private String subjectName;

    @ExcelProperty(value = "科目代码")
    private String subjectCode;

    @ExcelProperty(value = "上届预算")
    private BigDecimal preTotal;

    @ExcelProperty(value = "上届预估-金额")
    private BigDecimal preEstimate;

    @ExcelProperty(value = "上届预估-占比码洋")
    private String preCcRatioFormula;

    @ExcelProperty(value = "上届预估-占比收入")
    private String preRevenueFormula;

    @ExcelProperty(value = "本届预算-金额")
    private BigDecimal total;

    @ExcelProperty(value = "本届预算-占比码洋")
    private String ccRatioFormula;

    @ExcelProperty(value = "本届预算-占比收入")
    private String revenueFormula;

}
