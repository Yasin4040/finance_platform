package com.jtyjy.finance.manager.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-17 10:34
 */
@Data
public class BudgetContractExcelVO {

    @ExcelProperty("合同终止")
    private String terminationFlagName;

    @ExcelProperty("合同名称")
    private String contractName;

    @ExcelProperty("合同编号")
    private String contractCode;

    @ExcelProperty("合同金额")
    private BigDecimal contractMoney;

    @ExcelProperty("已支付")
    private BigDecimal paidMoney;

    @ExcelProperty("未支付")
    private BigDecimal unPaidMoney;

    @ExcelProperty("已冲账")
    private BigDecimal repaidMoney;

    @ExcelProperty("未冲账")
    private BigDecimal unRepaidMoney;

    @ExcelProperty("签订日期")
    private String signDate;

    @ExcelProperty("合同终止日期")
    private String terminationDate;

    @ExcelProperty("对方单位名称")
    private String otherPartyUnit;

}
