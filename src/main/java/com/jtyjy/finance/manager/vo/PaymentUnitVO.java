package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author 袁前兼
 * @Date 2021/7/14 15:58
 */
@Data
public class PaymentUnitVO {

    @ApiModelProperty(value = "开票单位Id")
    private Long bUnitId;

    @ApiModelProperty(value = "主键Id")
    private Long unitAccountId;

    @ApiModelProperty(value = "付款单位名称")
    private String unitName;

    @ApiModelProperty(value = "付款账户")
    private String bankAccount;

    @ApiModelProperty(value = "开户行")
    private String bankName;

}
