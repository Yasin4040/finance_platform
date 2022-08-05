package com.jtyjy.finance.manager.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 年度预算追加明细
 *
 * @author User
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YearAgentAddInfoExcelData {

    @ExcelProperty(value = "序号")
    private Integer num;

    @ExcelProperty(value = "状态 -1：退回，0：保存，1：已提交（待审核），2：审核通过")
    private String requestStatus;

    @ExcelProperty(value = "单据号")
    private String yearAddCode;

    @ExcelProperty(value = "届别")
    private String period;

    @ExcelProperty(value = "预算单位")
    private String unitName;

    @ExcelProperty(value = "预算科目")
    private String subjectName;

    @ExcelProperty(value = "预算动因")
    private String agentName;

    @ExcelProperty(value = "追加类型")
    private String type;

    @ExcelProperty(value = "追加金额")
    private BigDecimal total;

    @ExcelProperty(value = "追加原因")
    private String remark;

    @ExcelProperty(value = "年初预算")
    private String agentMoney;

    @ExcelProperty(value = "年度余额-追加前")
    private String addBefore;

    @ExcelProperty(value = "年度余额-追加后")
    private String addAfter;

    @ExcelProperty(value = "申请人")
    private String creatorName;

    @ExcelProperty(value = "申请日期")
    private String createTime;

    @ExcelProperty(value = "审核日期")
    private String auditTime;

    @ExcelProperty(value = "是否申请免罚")
    private String isExemptFine;

    @ExcelProperty(value = "免罚理由说明")
    private String exemptFineReason;

    @ExcelProperty(value = "免罚结果")
    private String exemptResult;

    @ExcelProperty(value = "罚款理由说明")
    private String fineRemark;
}
