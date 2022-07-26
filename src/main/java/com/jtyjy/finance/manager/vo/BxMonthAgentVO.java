package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Admin
 */
@Data
public class BxMonthAgentVO {

    @ApiModelProperty(value = "月度动因Id")
    private Long agentId;

    @ApiModelProperty(value = "月度动因名称")
    private String agentName;

    @ApiModelProperty(value = "预算科目名称")
    private String subjectName;

    @ApiModelProperty(value = "月度预算")
    private BigDecimal monthMoney;

    @ApiModelProperty(value = "月度余额")
    private BigDecimal monthBalance;

    @ApiModelProperty(value = "年度预算")
    private BigDecimal yearMoney;

    @ApiModelProperty(value = "年度余额")
    private BigDecimal yearBalance;

    @ApiModelProperty(value = "说明")
    private String remark;
}
