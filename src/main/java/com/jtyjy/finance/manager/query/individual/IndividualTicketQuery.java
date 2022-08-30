package com.jtyjy.finance.manager.query.individual;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jtyjy.finance.manager.query.PageQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 11:57
 */
@Data
public class IndividualTicketQuery extends PageQuery {
    @ApiModelProperty(value = "批次")
    @ExcelProperty(value = "*批次")
    private String batchNo;


    @ApiModelProperty(value = "部门")
    @ExcelProperty(value = "*事业群")
    private String departmentName;

    @ApiModelProperty(value = "省区/大区")
    @ExcelProperty(value = "*省区/大区")
    private String provinceOrRegion;

    @ApiModelProperty(value = "工号")
    @ExcelProperty(value = "*工号")
    private Integer employeeJobNum;

    @ApiModelProperty(value = "姓名")
    @ExcelProperty(value = "*姓名")
    private String employeeName;

    @ApiModelProperty(value = "年份")
    private Integer year;

    @ApiModelProperty(value = "月份")
    private Integer month;

    @ApiModelProperty(value = "个体户名称")
    private String individualName;

    @ApiModelProperty(value = "发票金额")
    private BigDecimal invoiceAmount;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @ApiModelProperty(value = "录入日期 开始日期 yyyy-MM-dd")
    private String startDate;

    @ApiModelProperty(value = "录入日期 结束日期")
    private String endDate;
}
