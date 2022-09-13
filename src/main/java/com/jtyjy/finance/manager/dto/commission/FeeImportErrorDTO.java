package com.jtyjy.finance.manager.dto.commission;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.jtyjy.finance.manager.dto.individual.ImportErrorDTO;
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
public class FeeImportErrorDTO extends ImportErrorDTO {
    @ExcelProperty(value = "*工号")
    private String employeeJobNum;
    @ExcelProperty(value = "*姓名")
    private String employeeName;
    @ExcelProperty(value = "*实发金额")
    private BigDecimal copeextract;
    @ExcelProperty(value = "*发放单位")
    private String issuedUnit;
    @ExcelProperty(value = "*费用发放金额")
    private BigDecimal paymentAmount;
}
