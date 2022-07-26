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
public class BudgetProjectLendDetailVO {

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "项目编号")
    private String projectNo;

    @ApiModelProperty(value = "借款Id")
    private Long lendMoneyId;

    @ApiModelProperty(value = "项目预领Id")
    private Long projectLendSumId;

    @ApiModelProperty(value = "项目借款确认状态")
    private Boolean confirmFlag;

    @ApiModelProperty(value = "员工工号")
    private String empNo;

    @ApiModelProperty(value = "员工姓名")
    private String empName;

    @ApiModelProperty(value = "借款金额")
    private BigDecimal lendMoney;

    @ApiModelProperty(value = "已还金额")
    private BigDecimal repaidMoney;

    @ApiModelProperty(value = "未还金额")
    private BigDecimal unpaidMoney;

    @ApiModelProperty(value = "利息")
    private BigDecimal interestMoney;

    @ApiModelProperty(value = "是否达标/完成")
    private Boolean flushingFlag;

    @ApiModelProperty(value = "还款标识")
    private Boolean chargeBillFlag;

    @ApiModelProperty(value = "借款单号")
    private String lendMoneyCode;

    @ApiModelProperty(value = "项目借款类型 1：现金;2：转账；3：礼品")
    private String projectLendType;

    @ApiModelProperty(value = "借款时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date lendDate;

    @ApiModelProperty(value = "计划还款时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planPayDate;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    @ApiModelProperty(value = "借款事由")
    private String remark;

    @ApiModelProperty(value = "付款状态 0:等待付款；1：接收付款；2：正在付款；3：已经付款")
    private Integer payMoneyStatus;
}
