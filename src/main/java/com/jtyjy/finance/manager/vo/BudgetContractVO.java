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
public class BudgetContractVO {

    @ApiModelProperty(value = "合同Id")
    private Long id;

    @ApiModelProperty(value = "合同终止 0进行中 1已终止")
    private Integer terminationFlag;

    @ApiModelProperty(value = "合同名称")
    private String contractName;

    @ApiModelProperty(value = "合同编号")
    private String contractCode;

    @ApiModelProperty(value = "合同金额")
    private BigDecimal contractMoney;

    @ApiModelProperty(value = "对方单位名称")
    private String otherPartyUnit;

    @ApiModelProperty(value = "签订日期")
    private String signDate;

    @ApiModelProperty(value = "合同终止日期")
    private String terminationDate;

    @ApiModelProperty(value = "已支付")
    private BigDecimal paidMoney;

    @ApiModelProperty(value = "未支付")
    private BigDecimal unPaidMoney;

    @ApiModelProperty(value = "已冲账")
    private BigDecimal repaidMoney;

    @ApiModelProperty(value = "未冲账")
    private BigDecimal unRepaidMoney;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
