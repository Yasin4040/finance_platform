package com.jtyjy.finance.manager.dto.individual;

import com.alibaba.excel.annotation.ExcelProperty;
import com.jtyjy.finance.manager.annotation.ExcelValid;
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
    @ExcelValid(message = "工号不能为空")
    private Integer employeeJobNum;
    @ExcelProperty(value = "姓名")
    @ExcelValid(message = "姓名不能为空")
    private String employeeName;

    @ExcelProperty(value = "个体户名称")
    @ExcelValid(message = "个体户名称不能为空")
    private String individualName;

    @ExcelProperty(value = "年份")
    @ExcelValid(message = "年份不能为空")
    private Integer year;

    @ExcelProperty(value = "月份")
    @ExcelValid(message = "月份不能为空")
    private Integer month;

    @ExcelProperty(value = "发票金额")
    @ExcelValid(message = "发票金额不能为空")
    private BigDecimal invoiceAmount;
}
