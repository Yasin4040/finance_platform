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
public class BudgetStrikeMoneyDetailVO {

    @ApiModelProperty(value = "合同Id")
    private Long id;

    @ApiModelProperty(value = "冲账编号")
    private String orderCode;

    @ApiModelProperty(value = "冲账方式 1:现金还款，2：手机支付还款 3：工资还款，4：提成还款，5：报销冲账还款 6:项目借款抵消还款 7:入库冲账")
    private String orderType;

    @ApiModelProperty(value = "冲账金额")
    private BigDecimal orderMoney;

    @ApiModelProperty(value = "冲账时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date orderDate;

    @ApiModelProperty(value = "员工工号")
    private String empNo;

    @ApiModelProperty(value = "员工姓名")
    private String empName;

}
