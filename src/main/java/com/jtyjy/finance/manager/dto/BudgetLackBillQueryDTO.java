package com.jtyjy.finance.manager.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author User
 */
@ApiModel(description = "预算报销欠票vo")
@Data
public class BudgetLackBillQueryDTO {

    @ApiModelProperty(value = "页码")
    private Integer page;

    @ApiModelProperty(value = "行数")
    private Integer rows;

    @ApiModelProperty(value = "收票状态 （0：未签收 1：已签收）")
    private Integer billStatus;

    /**
     * 报销单号
     */
    @ApiModelProperty(value = "报销单号")
    private String reimcode;

    /**
     * 报销人
     */
    @ApiModelProperty(value = "报销人")
    private String reimperonsname;

    /**
     * 预算单位
     */
    @ApiModelProperty(value = "预算单位")
    private String unitName;

    /**
     * 届别
     */
    @ApiModelProperty(value = "届别id")
    private Integer yearid;

    /**
     * 月份
     */
    @ApiModelProperty(value = "月份id")
    private Integer monthid;

    /**
     * 开票公司
     */
    @ApiModelProperty(value = "开票公司")
    private String bunitname;

    /**
     * 开票项目
     */
    @ApiModelProperty(value = "开票项目")
    @ExcelProperty(value = "开票项目")
    @ColumnWidth(12)
    private String project;

    /**
     * 最小金额
     */
    @ApiModelProperty(value = "最小金额")
    private BigDecimal minMoney;

    /**
     * 最大金额
     */
    @ApiModelProperty(value = "最大金额")
    private BigDecimal maxMoney;

    /**
     * 预计还票时间
     */
    @ApiModelProperty(value = "预计还票时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date estimatedReturnTime;
}
