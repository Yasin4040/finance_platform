package com.jtyjy.finance.manager.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @Author 袁前兼
 * @Date 2021/6/11 14:32
 */
@Data
public class YearAgentLendDTO {

    @NotNull
    @ApiModelProperty(value = "是否跨部门")
    private Boolean isAcross;

    @ApiModelProperty(value = "主键Id")
    private Long id;

    @NotNull(message = "拆借预算单位Id不能为空")
    @ApiModelProperty(value = "拆借预算单位Id")
    private Long inUnitId;

    @NotNull(message = "拆出预算单位Id不能为空")
    @ApiModelProperty(value = "拆出预算单位Id")
    private Long outUnitId;

    @NotNull(message = "拆借科目Id不能为空")
    @ApiModelProperty(value = "拆借科目Id")
    private Long inSubjectId;

    @NotNull(message = "拆出科目Id不能为空")
    @ApiModelProperty(value = "拆出科目Id")
    private Long outSubjectId;

    @ApiModelProperty(value = "拆借年度动因Id（选择已存在动因时必传）")
    private Long inAgentId;

    @ApiModelProperty(value = "拆借年度动因Id（不选择已存在动因，新建动因名称时必传）")
    private String inAgentName;

    @NotNull(message = "拆出年度动因Id不能为空")
    @ApiModelProperty(value = "拆出年度动因Id")
    private Long outAgentId;

    @NotNull(message = "拆借金额不能为空")
    @ApiModelProperty(value = "拆借金额")
    private BigDecimal total;

    @ApiModelProperty(value = "说明")
    private String remark;

    @ApiModelProperty(value = "文件URL")
    private String fileUrl;

    @ApiModelProperty(value = "文件原名称")
    private String fileOriginName;

    @ApiModelProperty(value = "名称")
    private String oaPassword;

    @ApiModelProperty(value = "是否提交")
    private Boolean isSubmit;

    /**
     * 是否免罚 0 否 1是
     */
    //@NotNull(message = "是否免罚不能为空")
    @ApiModelProperty(value = "是否免罚 false否 true是")
    private Boolean isExemptFine;

    /**
     * 免罚原因
     */
    @ApiModelProperty(value = "免罚原因")
    private String exemptFineReason;

}
