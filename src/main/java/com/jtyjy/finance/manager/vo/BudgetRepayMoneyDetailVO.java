package com.jtyjy.finance.manager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author 袁前兼
 * @Date 2021/6/21 13:47
 */
@Data
public class BudgetRepayMoneyDetailVO {

    @ApiModelProperty(value = "借款单号")
    private String lendMoneyCode;

    @ApiModelProperty(value = "还款单号")
    private String repayMoneyCode;

    @ApiModelProperty(value = "还款主键Id")
    private Long id;

    @ApiModelProperty(value = "借款Id")
    private Long lendMoneyId;

    @ApiModelProperty(value = "当时借款金额")
    private BigDecimal curLendMoney;

    @ApiModelProperty(value = "现在还剩还款金额")
    private BigDecimal nowLendMoney;

    @ApiModelProperty(value = "还款时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date repayMoneyDate;

    @ApiModelProperty(value = "已还金额（已还本金 + 已还利息）")
    private BigDecimal repayMoney;

}
