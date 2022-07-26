package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 年度动因汇总
 *
 * @author User
 */
@Data
public class BudgetYearSubjectVO {

    @ApiModelProperty(value = "主键Id")
    private Long primaryKeyId;

    @ApiModelProperty(value = "基础科目Id")
    private Long baseSubjectId;

    @ApiModelProperty(value = "基础科目名称")
    private String baseSubjectName;

    @ApiModelProperty(value = "预算科目Id")
    private Long id;

    @ApiModelProperty(value = "预算科目父Id")
    private Long parentId;

    @ApiModelProperty(value = "预算科目名称")
    private String budgetSubjectName;

    @ApiModelProperty(value = "预算科目代码")
    private String budgetSubjectCode;

    @ApiModelProperty(value = "是否叶子节点")
    private Boolean leaf;

    @ApiModelProperty(value = "上届预算")
    private BigDecimal preTotal;

    @ApiModelProperty(value = "上届预估-金额")
    private BigDecimal preEstimate;

    @ApiModelProperty(value = "上届预估-占比码洋")
    private BigDecimal preCcRatioFormula;

    @ApiModelProperty(value = "上届预估-占比公式")
    private String preCcRatioFormulaStr;

    @ApiModelProperty(value = "上届预估-占比收入")
    private BigDecimal preRevenueFormula;

    @ApiModelProperty(value = "本届预算-金额")
    private BigDecimal total;

    @ApiModelProperty(value = "本届预算-占比码洋")
    private BigDecimal ccRatioFormula;

    @ApiModelProperty(value = "本届预算-占比公式")
    private String ccRatioFormulaStr;

    @ApiModelProperty(value = "本届预算-占比收入")
    private BigDecimal revenueFormula;

    @ApiModelProperty(value = "本届预算-占比收入公式")
    private String revenueFormulaStr;

    @ApiModelProperty(value = "本届预算-占比收入")
    private String formula;

}
