package com.jtyjy.finance.manager.dto.commission;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.jtyjy.finance.manager.annotation.ExcelDecimalValid;
import com.jtyjy.finance.manager.annotation.ExcelValid;
import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 13:59
 */
@Data
public class IndividualIssueExportDTO {

    @ExcelProperty(value = "*工号")
    @ExcelValid(message = "工号不能为空")
    private String employeeJobNum;
    @ExcelProperty(value = "*姓名")
    @ExcelValid(message = "姓名不能为空")
    private String employeeName;
    @ExcelDecimalValid(min = 0, max = 100000000,message = "实发金额大于0小于1亿")
    @ExcelProperty(value = "*实发金额")
    @ExcelValid(message = "实发金额不能为空")
    private BigDecimal copeextract;
    @ExcelProperty(value = "*发放单位")
//    @ExcelValid(message = "发放单位不能为空")
    private String issuedUnit;
    @ExcelProperty(value = "*费用发放金额")
    @ExcelValid(message = "费用发放金额不能为空")
    @ExcelDecimalValid(min = 0, max = 100000000,message = "实发金额大于0小于1亿")
    private BigDecimal paymentAmount;

    @ExcelIgnore
    private Integer businessType;


}
