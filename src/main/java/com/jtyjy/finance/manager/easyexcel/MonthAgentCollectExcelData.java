package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Author 袁前兼
 * @Date 2021/6/1 10:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthAgentCollectExcelData {

    @ExcelProperty(value = "月度预算时间")
    private String budgetTime;

    @ExcelProperty(value = "预算单位名称")
    private String unitName;

    @ExcelProperty(value = "预算科目名称")
    private String subjectName;

    @ExcelProperty(value = "年度预算-年初预算")
    private BigDecimal yearMoney;

    @ExcelProperty(value = "年度预算-累计追加")
    private BigDecimal yearAddMoney;

    @ExcelProperty(value = "年度预算-预算合计")
    private BigDecimal yearTotalMoney;

    @ExcelProperty(value = "年度预算-收入占比")
    private String yearRevenueFormula;

    @ExcelProperty(value = "累计执行-金额")
    private BigDecimal yearExecuteMoney;

    @ExcelProperty(value = "累计执行-执行率")
    private String zxl;

    @ExcelProperty(value = "年度预算结余-金额")
    private BigDecimal yearSurplusMoney;

    @ExcelProperty(value = "年度预算结余-执行率")
    private String syZxl;

    @ExcelProperty(value = "月度预算金额")
    private BigDecimal monthMoney;

    @ExcelProperty(value = "月度预算活动说明")
    private String monthBusiness;

    @ExcelIgnore
    private Integer orderNo;

}
