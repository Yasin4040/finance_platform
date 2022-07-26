package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 月度预算追加明细
 *
 * @author User
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthAgentAddInfoExcelData {

    @ExcelProperty(value = "序号")
    private Integer num;

    @ExcelProperty(value = "状态 -1：退回，0：保存，1：已提交（待审核），2：审核通过")
    private String requestStatus;

    @ExcelProperty(value = "单据号")
    private String monthAddCode;

    @ExcelProperty(value = "届别")
    private String period;

    @ExcelProperty(value = "月份")
    private Long monthId;

    @ExcelProperty(value = "预算单位")
    private String unitName;

    @ExcelProperty(value = "预算科目")
    private String subjectName;

    @ExcelProperty(value = "预算动因")
    private String agentName;

    @ExcelProperty(value = "追加金额")
    private BigDecimal total;

    @ExcelProperty(value = "追加原因")
    private String remark;

    @ApiModelProperty(value = "年初预算")
    private BigDecimal yearMoney;

    @ApiModelProperty(value = "年度余额")
    private BigDecimal yearBalance;

    @ApiModelProperty(value = "月初预算")
    private BigDecimal monthMoney;

    @ExcelProperty(value = "月度余额-追加前")
    private BigDecimal addBefore;

    @ExcelProperty(value = "月度余额-追加后")
    private BigDecimal addAfter;

    @ExcelProperty(value = "申请人")
    private String creatorName;

    @ExcelProperty(value = "申请日期")
    private String createTime;

    @ExcelProperty(value = "审核日期")
    private String auditTime;

}
