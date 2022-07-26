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
public class BudgetProjectRepayDetailVO {

    @ApiModelProperty(value = "主键Id")
    private Long id;

    @ApiModelProperty(value = "借款Id")
    private Long lendMoneyId;

    @ApiModelProperty(value = "项目预领Id")
    private Long projectLendSumId;

    @ApiModelProperty(value = "借款单号")
    private String lendMoneyCode;

    @ApiModelProperty(value = "还款单号")
    private String repayMoneyCode;

    @ApiModelProperty(value = "工号")
    private String empNo;

    @ApiModelProperty(value = "姓名")
    private String empName;

    @ApiModelProperty(value = "借款金额")
    private BigDecimal lendMoney;

    @ApiModelProperty(value = "已还金额")
    private BigDecimal repaidMoney;

    @ApiModelProperty(value = "未还金额")
    private BigDecimal unpaidMoney;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

}
