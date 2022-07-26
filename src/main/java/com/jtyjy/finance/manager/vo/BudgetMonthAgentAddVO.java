package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author 袁前兼
 * @Date 2021/6/29 8:36
 */
@Data
public class BudgetMonthAgentAddVO {

    @ApiModelProperty(value = "追加Id")
    private Long id;

    @ApiModelProperty(value = "月度动因名称")
    private String name;

    @ApiModelProperty(value = "月度动因id")
    private Long monthAgentId;

    @ApiModelProperty(value = "预算科目Id")
    private Long subjectId;

    @ApiModelProperty(value = "追加主表主键Id")
    private Long infoId;

    @ApiModelProperty(value = "追加金额")
    private BigDecimal addMoney;

    @ApiModelProperty(value = "追加原因")
    private String remark;

    @ApiModelProperty(value = "年初预算")
    private BigDecimal yearMoney;

    @ApiModelProperty(value = "年度余额")
    private BigDecimal yearBalance;

    @ApiModelProperty(value = "追加前-月初预算")
    private BigDecimal preMonthMoney;

    @ApiModelProperty(value = "追加前-月度余额")
    private BigDecimal preMonthBalance;

    @ApiModelProperty(value = "追加后-月初预算")
    private BigDecimal monthMoney;

    @ApiModelProperty(value = "追加后-月度余额")
    private BigDecimal monthBalance;

}
