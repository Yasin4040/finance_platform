package com.jtyjy.finance.manager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author 袁前兼
 * @Date 2021/6/3 14:32
 */
@Data
public class BudgetYearAddInfoVO {

    @ApiModelProperty(value = "主键Id")
    private Long id;

    @ApiModelProperty(value = "追加状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过")
    private Long requestStatus;

    @ApiModelProperty(value = "单据号")
    private String yearAddCode;

    @ApiModelProperty(value = "届别Id")
    private Long yearId;

    @ApiModelProperty(value = "届别名称")
    private String yearPeriod;

    @ApiModelProperty(value = "预算单位Id")
    private Long budgetUnitId;

    @ApiModelProperty(value = "预算单位名称")
    private String budgetUnitName;

//    @ApiModelProperty(value = "预算科目Id")
//    private Long budgetSubjectId;
//
//    @ApiModelProperty(value = "预算科目名称")
//    private String budgetSubjectName;
//
//    @ApiModelProperty(value = "预算科目代码")
//    private String budgetSubjectCode;

    @ApiModelProperty(value = "追加金额")
    private BigDecimal addMoney;

//    @ApiModelProperty(value = "追加前-年初预算")
//    private BigDecimal preYearMoney;
//
//    @ApiModelProperty(value = "追加前-年度余额")
//    private BigDecimal preYearBalance;
//
//    @ApiModelProperty(value = "追加后-年初预算")
//    private BigDecimal yearMoney;
//
//    @ApiModelProperty(value = "追加后-年度余额")
//    private BigDecimal yearBalance;

    @ApiModelProperty(value = "文件名称")
    private String fileOriginName;

    @ApiModelProperty(value = "文件地址")
    private String fileUrl;

    @ApiModelProperty(value = "密码")
    private String oaPassword;

    @ApiModelProperty(value = "申请人")
    private String creatorName;

    @ApiModelProperty(value = "申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    @ApiModelProperty(value = "是否免罚 false否 true是")
    private Boolean isExemptFine;

    @ApiModelProperty(value = "免罚原因")
    private String exemptFineReason;

}
