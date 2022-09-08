package com.jtyjy.finance.manager.dto.commission;

import com.alibaba.excel.annotation.ExcelProperty;
import com.jtyjy.finance.manager.annotation.ExcelDecimalValid;
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
    private Integer employeeJobNum;
    @ExcelProperty(value = "*姓名")
    private String employeeName;
    @ExcelDecimalValid(min = 0, max = 1*0000*0000)
    @ExcelProperty(value = "*实发金额")
    private BigDecimal copeextract;
    @ExcelProperty(value = "*发放单位")
    private String issuedUnit;
    @ExcelProperty(value = "*费用发放金额")
    @ExcelDecimalValid(min = 0, max = 1*0000*0000)
    private BigDecimal paymentAmount;


}
