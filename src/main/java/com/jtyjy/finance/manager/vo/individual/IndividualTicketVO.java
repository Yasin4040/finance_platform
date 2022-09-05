package com.jtyjy.finance.manager.vo.individual;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 13:59
 */
@Data
public class IndividualTicketVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    @ExcelIgnore
    private Long id;
    @ApiModelProperty(value = "个体户档案id")
    @ExcelIgnore
    private Long individualEmployeeInfoId;

    @ApiModelProperty(value = "批次")
    @ExcelProperty(value = "批次")
    private String batchNo;


    @ApiModelProperty(value = "部门")
    @ExcelProperty(value = "部门")
    private String departmentName;

    @ApiModelProperty(value = "省区/大区")
    @ExcelProperty(value = "省区/大区")
    private String provinceOrRegion;

    @ApiModelProperty(value = "工号")
    @ExcelProperty(value = "工号")
    private Integer employeeJobNum;

    @ApiModelProperty(value = "姓名")
    @ExcelProperty(value = "姓名")
    private String employeeName;
    @ApiModelProperty(value = "个体户名称")
    @ExcelProperty(value = "个体户名称")
    private String individualName;
    @ApiModelProperty(value = "年份")
    @ExcelProperty(value = "年份")
    private Integer year;

    @ApiModelProperty(value = "月份")
    @ExcelProperty(value = "月份")
    private Integer month;

    @ApiModelProperty(value = "发票金额")
    @ExcelProperty(value = "发票金额")
    private BigDecimal invoiceAmount;

    @ApiModelProperty(value = "备注")
    @ExcelProperty(value = "备注")
    private String remarks;

    @ApiModelProperty(value = "录入日期")
    @ExcelProperty(value = "录入日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "UTC+8")
    @DateTimeFormat(value = "yyyy-MM-dd")
    private Date createTime;

}
