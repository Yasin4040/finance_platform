package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author 袁前兼
 * @Date 2021/6/1 10:33
 */
@Data
public class BudgetMonthSubjectVO {

    @ApiModelProperty(value = "预算科目Id")
    private Long id;

    @ApiModelProperty(value = "预算科目父Id")
    private Long parentId;

    @ApiModelProperty(value = "预算科目代码")
    private String subjectCode;

    @ApiModelProperty(value = "预算科目名称")
    private String subjectName;

    @ApiModelProperty(value = "是否叶子节点")
    private Boolean leaf;

    @ApiModelProperty(value = "月度预算金额")
    private BigDecimal monthMoney;

    @ApiModelProperty(value = "月度预算活动说明")
    private String monthBusiness;

    @ApiModelProperty(value = "年度预算金额")
    private BigDecimal yearMoney;

    @ApiModelProperty(value = "年度追加金额")
    private BigDecimal yearAddMoney;

    @ApiModelProperty(value = "年度拆进金额")
    private BigDecimal yearLendInMoney;

    @ApiModelProperty(value = "年度拆出金额")
    private BigDecimal yearLendOutMoney;

    @ApiModelProperty(value = "年度执行金额")
    private BigDecimal yearExecuteMoney;

    @ApiModelProperty(value = "年度占比收入")
    private BigDecimal yearRevenueFormula;

    @ApiModelProperty(value = "预算单位Id")
    private Long unitId;

    @ApiModelProperty(value = "预算单位名称")
    private String unitName;

    @ApiModelProperty(value = "月份Id")
    private Long monthId;

    @ApiModelProperty(value = "月份")
    private String monthPeriod;

    @ApiModelProperty(value = "届别")
    private String yearPeriod;

    @ApiModelProperty(value = "届别")
    private String yearCode;

    @ApiModelProperty(value = "年度合计金额")
    private BigDecimal yearTotalMoney;

    @ApiModelProperty(value = "年度剩余金额")
    private BigDecimal yearSurplusMoney;

    @ApiModelProperty(value = "年度执行率")
    private BigDecimal zxl;

    @ApiModelProperty(value = "年度剩余执行率")
    private BigDecimal syZxl;

    private Integer orderNo;

}
