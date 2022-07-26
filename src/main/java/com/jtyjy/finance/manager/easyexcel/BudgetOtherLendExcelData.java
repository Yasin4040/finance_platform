package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@Data
public class BudgetOtherLendExcelData {

    @ExcelProperty(value = "员工Id")
    private String empId;

    @ExcelProperty(value = "工号")
    private String empNo;

    @ExcelProperty(value = "姓名")
    private String empName;

    @ExcelProperty(value = "借款金额")
    private BigDecimal lendMoney;

    @ExcelProperty(value = "借款事由")
    private String remark;

    @ExcelProperty(value = "导入批次号")
    private String importBatchNumber;

    @ExcelProperty(value = "借款日期")
    private Date lendDate;

    @ExcelProperty(value = "还款日期")
    private Date planPayDate;

    @ExcelProperty(value = "借款类型")
    private Integer lendType;

    // 合同借款 --------------------------------------------------

    @ExcelProperty(value = "合同编号")
    private String contractNo;

    @ExcelProperty(value = "合同名称")
    private String contractName;

    @ExcelProperty(value = "合同Id")
    private Long contractId;

    // 非合同借款 --------------------------------------------------

    @ExcelProperty(value = "账户Id")
    private String bankAccountId;

    @ExcelProperty(value = "账户名称")
    private String bankAccountName;

    // 项目借款 --------------------------------------------------

    @ExcelProperty(value = "届别")
    private String yearName;

    @ExcelProperty(value = "预算单位名称")
    private String unitName;

    @ExcelProperty(value = "项目名称")
    private String projectName;

    @ExcelProperty(value = "项目类型")
    private int projectType;

    @ExcelProperty(value = "项目借款类型")
    private String projectLendType;

    @ExcelProperty(value = "项目借款主键Id")
    private Long projectLendSumId;

    @ExcelProperty(value = "届别Id")
    private Long yearId;

    @ExcelProperty(value = "预算单位Id")
    private Long unitId;

}
