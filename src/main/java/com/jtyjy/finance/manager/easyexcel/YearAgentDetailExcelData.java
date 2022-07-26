package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 年度动因明细
 *
 * @author User
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YearAgentDetailExcelData {

    @ExcelProperty(value = "届别")
    private String yearName;

    @ExcelProperty(value = "预算单位名称")
    private String unitName;

    @ExcelProperty(value = "预算科目名称")
    private String budgetSubjectName;

    @ExcelProperty(value = "动因名称")
    private String name;

    @ExcelProperty(value = "动因内容")
    private String remark;

    @ExcelProperty(value = "弹性标识")
    private String elasticFlag;

    @ExcelProperty(value = "弹性率")
    private String elasticRatio;

    @ExcelProperty(value = "弹性动因占比上限(null or < 0 不受控制)")
    private String elasticMax;

    @ExcelProperty(value = "占比科目名称")
    private String proportionSubjectName;

    @ExcelProperty(value = "上届预估")
    private BigDecimal preEstimate;

    @ExcelProperty(value = "本届总金额(12个月)")
    private BigDecimal total;

    @ExcelProperty(value = "发生次数")
    private String happenCount;

    @ExcelProperty(value = "计算过程")
    private String computingProcess;

    @ExcelProperty(value = "6月")
    private BigDecimal m6;

    @ExcelProperty(value = "7月")
    private BigDecimal m7;

    @ExcelProperty(value = "8月")
    private BigDecimal m8;

    @ExcelProperty(value = "9月")
    private BigDecimal m9;

    @ExcelProperty(value = "10月")
    private BigDecimal m10;

    @ExcelProperty(value = "11月")
    private BigDecimal m11;

    @ExcelProperty(value = "12月")
    private BigDecimal m12;

    @ExcelProperty(value = "1月")
    private BigDecimal m1;

    @ExcelProperty(value = "2月")
    private BigDecimal m2;

    @ExcelProperty(value = "3月")
    private BigDecimal m3;

    @ExcelProperty(value = "4月")
    private BigDecimal m4;

    @ExcelProperty(value = "5月")
    private BigDecimal m5;

}
