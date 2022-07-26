package com.jtyjy.finance.manager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@Data
public class BudgetYearAgentLendVO {

    @ApiModelProperty(value = "主键Id")
    private Long id;

    @ApiModelProperty(value = "拆借状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过")
    private Integer requestStatus;

    @ApiModelProperty(value = "拆借单号")
    private String orderNumber;

    @ApiModelProperty(value = "届别Id")
    private Long yearId;

    @ApiModelProperty(value = "届别名称")
    private String yearPeriod;

    @ApiModelProperty(value = "拆进预算单位Id")
    private Long inBudgetUnitId;

    @ApiModelProperty(value = "拆进预算单位名称")
    private String inBudgetUnitName;

    @ApiModelProperty(value = "拆进基础单位Id")
    private Long inBaseUnitId;

    @ApiModelProperty(value = "拆出预算单位Id")
    private Long outBudgetUnitId;

    @ApiModelProperty(value = "拆出预算单位名称")
    private String outBudgetUnitName;

    @ApiModelProperty(value = "拆出基础单位Id")
    private Long outBaseUnitId;

    @ApiModelProperty(value = "拆进金额")
    private BigDecimal total;

    @ApiModelProperty(value = "拆进科目Id")
    private Long inSubjectId;

    @ApiModelProperty(value = "拆进科目名称")
    private String inSubjectName;

    @ApiModelProperty(value = "拆进年度动因Id")
    private Long inAgentId;

    @ApiModelProperty(value = "拆进年度动因名称")
    private String inAgentName;

    @ApiModelProperty(value = "拆进前年初预算")
    private BigDecimal inYearTotal;

    @ApiModelProperty(value = "拆进前年度拆进金额")
    private BigDecimal inAgentLendInMoney;

    @ApiModelProperty(value = "拆进前年度拆出金额")
    private BigDecimal inAgentLendOutMoney;

    @ApiModelProperty(value = "拆进前年度执行金额")
    private BigDecimal inYearExecute;

    @ApiModelProperty(value = "拆进前年度剩余金额")
    private BigDecimal inYearBalance;

    @ApiModelProperty(value = "拆出科目Id")
    private Long outSubjectId;

    @ApiModelProperty(value = "拆出科目名称")
    private String outSubjectName;

    @ApiModelProperty(value = "拆出年度动因Id")
    private Long outAgentId;

    @ApiModelProperty(value = "拆出年度动因名称")
    private String outAgentName;

    @ApiModelProperty(value = "拆出前年初预算")
    private BigDecimal outYearTotal;

    @ApiModelProperty(value = "拆出前年度拆进金额")
    private BigDecimal outAgentLendInMoney;

    @ApiModelProperty(value = "拆出前年度拆出金额")
    private BigDecimal outAgentLendOutMoney;

    @ApiModelProperty(value = "拆出前年度执行金额")
    private BigDecimal outYearExecute;

    @ApiModelProperty(value = "拆出前年度剩余金额")
    private BigDecimal outYearBalance;

    @ApiModelProperty(value = "说明")
    private String remark;

    @ApiModelProperty(value = "创建人")
    private String creatorName;

    @ApiModelProperty(value = "文件URL")
    private String fileUrl;

    @ApiModelProperty(value = "文件原名称")
    private String fileOriginName;

    @ApiModelProperty(value = "名称")
    private String oaPassword;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

    @ApiModelProperty(value = "是否免罚 false否 true是")
    private Boolean isExemptFine;

    @ApiModelProperty(value = "免罚原因")
    private String exemptFineReason;

}
