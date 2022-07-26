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
public class BudgetOtherLendSumVO {

    @ApiModelProperty(value = "主键Id")
    private Long id;

    @ApiModelProperty(value = "审核状态 0未审核 1已审核")
    private Integer status;

    @ApiModelProperty(value = "导入批次号")
    private String importBatchNumber;

    @ApiModelProperty(value = "借款总额")
    private BigDecimal totalLendMoney;

    @ApiModelProperty(value = "还款总额")
    private BigDecimal totalRepayMoney;

    @ApiModelProperty(value = "未还总额")
    private BigDecimal totalUnRepayMoney;

    @ApiModelProperty(value = "导入人")
    private String importName;

    @ApiModelProperty(value = "导入时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date importTime;

    @ApiModelProperty(value = "审核人")
    private String verifyName;

    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date verifyTime;

}
