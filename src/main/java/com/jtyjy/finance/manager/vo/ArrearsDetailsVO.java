package com.jtyjy.finance.manager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author User
 */
@ApiModel(description = "员工台账明细")
@Data
public class ArrearsDetailsVO {

    @ApiModelProperty(value = "员工工号")
    private String empNo;

    @ApiModelProperty(value = "员工姓名")
    private String empName;

    @ApiModelProperty(value = "当时金额")
    private BigDecimal curMoney;

    @ApiModelProperty(value = "类型")
    private Integer orderType;

    @ApiModelProperty(value = "类型描述")
    private String orderTypeDesc;

    @ApiModelProperty(value = "单号")
    private String orderCode;

    @ApiModelProperty(value = "金额")
    private String orderMoney;

    @ApiModelProperty(value = "利息")
    private BigDecimal orderInterest;

    @ApiModelProperty(value = "时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderDate;

    @ApiModelProperty(value = "备注")
    private String orderRemark;

}
