package com.jtyjy.finance.manager.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @Author minzhq
 * @Date 2022/8/4
 */
@Data
public class YearAgentLendDetailDTO {

    @ApiModelProperty(value = "修改时必传")
    private Long id;

    @NotNull(message = "拆进预算单位不能为空")
    @ApiModelProperty(value = "拆进预算单位Id")
    private Long inUnitId;

    @NotNull(message = "拆出预算单位不能为空")
    @ApiModelProperty(value = "拆出预算单位Id")
    private Long outUnitId;

    @NotNull(message = "拆进科目不能为空")
    @ApiModelProperty(value = "拆进科目Id")
    private Long inSubjectId;

    @NotNull(message = "拆出科目不能为空")
    @ApiModelProperty(value = "拆出科目Id")
    private Long outSubjectId;

    @ApiModelProperty(value = "拆进年度动因Id（选择已存在动因时必传）")
    @NotNull(message = "拆进年度动因不能为空")
    private Long inAgentId;

    @ApiModelProperty(value = "拆借原因")
    @NotBlank(message = "拆借原因不能为空")
    private String remark;

    @NotNull(message = "拆出年度动因不能为空")
    @ApiModelProperty(value = "拆出年度动因Id")
    private Long outAgentId;

    @NotNull(message = "拆借金额不能为空")
    @ApiModelProperty(value = "拆借金额")
    private BigDecimal total;

    @ApiModelProperty(value = "是否免罚 false否 true是")
    @NotNull(message = "是否免罚不能为空")
    private Boolean isExemptFine = false;

    @ApiModelProperty(value = "免罚原因")
    private String exemptFineReason;

}
