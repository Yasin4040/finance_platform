package com.jtyjy.finance.manager.dto.individual;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 13:59
 */
@Data
public class IndividualTicketImportDTO {


    @ExcelProperty(value = "工号")
    private Integer employeeJobNum;
    @ExcelProperty(value = "姓名")
    private String employeeName;

    @ExcelProperty(value = "个体户名称")
    private String individualName;

    @ExcelProperty(value = "年份")
    private Integer year;

    @ExcelProperty(value = "月份")
    private Integer month;

    @ExcelProperty(value = "发票金额")
    private BigDecimal invoiceAmount;
}
