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
public class BudgetProjectLendSumVO {

    @ApiModelProperty(value = "主键Id")
    private Long id;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "项目编号")
    private String projectNo;

    @ApiModelProperty(value = "届别Id")
    private Long yearId;

    @ApiModelProperty(value = "届别名称")
    private String yearName;

    @ApiModelProperty(value = "预算单位Id")
    private Long budgetUnitId;

    @ApiModelProperty(value = "预算单位名称")
    private String budgetUnitName;

    @ApiModelProperty(value = "基础单位Id")
    private Long baseUnitId;

    @ApiModelProperty(value = "1项目预领 2项目借支")
    private Integer type;

    @ApiModelProperty(value = "借款总额")
    private BigDecimal totalLendMoney;

    @ApiModelProperty(value = "现金")
    private BigDecimal cashMoney;

    @ApiModelProperty(value = "转账")
    private BigDecimal transferMoney;

    @ApiModelProperty(value = "礼品")
    private BigDecimal giftMoney;

    @ApiModelProperty(value = "报销人Id")
    private String bxUserId;

    @ApiModelProperty(value = "报销人名称")
    private String bxUserName;

    @ApiModelProperty(value = "报销单Id")
    private Long bxOrderId;

    @ApiModelProperty(value = "报销月份")
    private Long monthId;

    @ApiModelProperty(value = "报销状态")
    private Integer bxStatus;

    @ApiModelProperty(value = "报销时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date bxDate;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "创建人")
    private String creatorName;

    @ApiModelProperty(value = "审核状态 0未审核 1已审核")
    private Integer verifyFlag;

    @ApiModelProperty(value = "审核人名称")
    private String verifyName;

    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date verifyTime;

    @ApiModelProperty(value = "转账付款单位Id")
    private String bUnitId;

    @ApiModelProperty(value = "转账付款单位名称")
    private String bUnitName;

    @ApiModelProperty(value = "借款人数")
    private String lendCount;

    @ApiModelProperty(value = "已还金额")
    private String repaidMoney;

    @ApiModelProperty(value = "未还金额")
    private String unRepaidMoney;

    @ApiModelProperty(value = "报销明细-状态 -1：退回，0：保存，1：已提交（待审核），2：审核通过")
    private Integer requestStatus;

    @ApiModelProperty(value = "报销明细-冲账金额")
    private BigDecimal paymentMoney;

    @ApiModelProperty(value = "报销明细-转账金额")
    private BigDecimal transMoney;

    @ApiModelProperty(value = "报销明细-报销金额")
    private BigDecimal bxMoney;
}
