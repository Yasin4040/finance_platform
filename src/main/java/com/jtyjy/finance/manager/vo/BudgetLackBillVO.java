package com.jtyjy.finance.manager.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author User
 */
@ApiModel(description = "预算报销欠票vo")
@Data
public class BudgetLackBillVO {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    @ExcelIgnore
    private String id;

    /**
     * 报销单id
     */
    @ApiModelProperty(value = "报销单主键")
    @ExcelIgnore
    private String reimbursementid;

    /**
     * 开票单位id
     */
    @ApiModelProperty(value = "开票单位id")
    @ExcelIgnore
    private Long bunitid;

    @ApiModelProperty(value = "收票状态 （0：未签收 1：已签收）")
    @ExcelIgnore
    private Integer billStatus;

    /**
     * 报销单号
     */
    @ApiModelProperty(value = "报销单号")
    @ExcelProperty(value = "报销单号")
    @ColumnWidth(14)
    private String reimcode;

    /**
     * 报销人
     */
    @ApiModelProperty(value = "报销人")
    @ExcelProperty(value = "报销人")
    @ColumnWidth(12)
    private String reimperonsname;

    /**
     * 预算单位
     */
    @ApiModelProperty(value = "预算单位")
    @ExcelProperty(value = "预算单位")
    @ColumnWidth(20)
    private String unitName;

    /**
     * 届别
     */
    @ApiModelProperty(value = "届别")
    @ExcelProperty(value = "届别")
    @ColumnWidth(12)
    private String yearName;

    /**
     * 月份
     */
    @ApiModelProperty(value = "月份")
    @ExcelProperty(value = "月份")
    @ColumnWidth(12)
    private String monthName;

    /**
     * 开票公司
     */
    @ApiModelProperty(value = "开票公司")
    @ExcelProperty(value = "开票公司")
    @ColumnWidth(20)
    private String bunitname;

    /**
     * 开票项目
     */
    @ApiModelProperty(value = "开票项目")
    @ExcelProperty(value = "开票项目")
    @ColumnWidth(12)
    private String project;

    /**
     * 金额
     */
    @ApiModelProperty(value = "金额")
    @ExcelProperty(value = "金额")
    @ColumnWidth(12)
    private BigDecimal money;

    /**
     * 预计还票时间
     */
    @ApiModelProperty(value = "预计还票时间")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    @ExcelProperty(value = "预计还票时间")
    @ColumnWidth(12)
    private String estimatedReturnTime;

    @ApiModelProperty(value = "收票状态")
    @ExcelProperty(value = "收票状态")
    @ColumnWidth(12)
    private String billStatusName;
}
