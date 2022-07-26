package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Admin
 */
@Data
public class YearAgentLendExcelData {

    @ExcelProperty(value = "主键Id")
    private Integer num;

    @ExcelProperty(value = "是否免罚")
    private String mf;

    @ExcelProperty(value = "拆借状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过")
    private String requestStatus;

    @ExcelProperty(value = "单据号")
    private String orderNumber;

    @ExcelProperty(value = "届别名称")
    private String yearPeriod;

    @ExcelProperty(value = "拆进预算单位名称")
    private String inBudgetUnitName;

    @ExcelProperty(value = "拆出预算单位名称")
    private String outBudgetUnitName;

    @ExcelProperty(value = "拆进金额")
    private BigDecimal total;

    @ExcelProperty(value = "拆进科目名称")
    private String inSubjectName;

    @ExcelProperty(value = "拆进年度动因名称")
    private String inAgentName;

    @ExcelProperty(value = "拆进年初预算（拆进前）")
    private BigDecimal inYearTotal;

    @ExcelProperty(value = "拆进年初预算（拆进后）")
    private BigDecimal inYearBalance;

    @ExcelProperty(value = "拆出科目名称")
    private String outSubjectName;

    @ExcelProperty(value = "拆出年度动因名称")
    private String outAgentName;

    @ExcelProperty(value = "拆出年初预算（拆进前）")
    private BigDecimal outYearTotal;

    @ExcelProperty(value = "拆出年初预算（拆进后）")
    private BigDecimal outYearBalance;

    @ExcelProperty(value = "说明")
    private String remark;

    @ExcelProperty(value = "申请人")
    private String creatorName;

    @ExcelProperty(value = "申请时间")
    private String createTime;

    @ExcelProperty(value = "审核时间")
    private String auditTime;

}
