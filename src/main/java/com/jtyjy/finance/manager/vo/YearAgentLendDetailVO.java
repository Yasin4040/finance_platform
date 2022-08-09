package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author minzhq
 * @Date 2022/8/4
 */
@Data
public class YearAgentLendDetailVO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "拆进预算单位Id")
    private Long inUnitId;

    @ApiModelProperty(value = "拆进预算单位")
    private String inUnitName;

    @ApiModelProperty(value = "拆出预算单位Id")
    private Long outUnitId;
    @ApiModelProperty(value = "拆出预算单位")
    private String outUnitName;

    @ApiModelProperty(value = "拆进科目Id")
    private Long inSubjectId;

    @ApiModelProperty(value = "拆进科目")
    private String inSubjectName;

    @ApiModelProperty(value = "拆出科目Id")
    private Long outSubjectId;

    @ApiModelProperty(value = "拆出科目")
    private String outSubjectName;

    @ApiModelProperty(value = "拆进年度动因Id")
    private Long inAgentId;

    @ApiModelProperty(value = "拆进年度动因")
    private String inAgentName;

    @ApiModelProperty(value = "拆进年度动因预算")
    private BigDecimal inAgentMoney;

    @ApiModelProperty(value = "拆进年度动因已执行金额")
    private BigDecimal inAgentExecuteMoney;

    @ApiModelProperty(value = "拆借原因")
    private String remark;

    @ApiModelProperty(value = "拆出年度动因Id")
    private Long outAgentId;

    @ApiModelProperty(value = "拆出年度动因")
    private String outAgentName;

    @ApiModelProperty(value = "拆出年度动因预算")
    private BigDecimal outAgentMoney;

    @ApiModelProperty(value = "拆出年度动因可拆出预算")
    private BigDecimal outAgentBalance;

    @ApiModelProperty(value = "拆借金额")
    private BigDecimal total;

    @ApiModelProperty(value = "是否免罚 false否 true是")
    private Boolean isExemptFine;

    @ApiModelProperty(value = "免罚原因")
    private String exemptFineReason;

    @ApiModelProperty(value = "免罚结果")
    private String exemptFineResult;

    @ApiModelProperty(value = "罚款理由说明")
    private String fineReasonRemark;

}
