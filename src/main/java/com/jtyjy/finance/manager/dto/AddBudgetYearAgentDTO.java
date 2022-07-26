package com.jtyjy.finance.manager.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @Author 袁前兼
 * @Date 2021/4/20 14:32
 */
@Data
public class AddBudgetYearAgentDTO {

    @NotNull(message = "预算科目Id不能为空")
    @ApiModelProperty(value = "预算科目Id", required = true)
    private Long budgetSubjectId;

    @NotNull(message = "预算单位Id不能为空")
    @ApiModelProperty(value = "预算单位Id", required = true)
    private Long budgetUnitId;

    @ApiModelProperty(value = "动因名称", required = true)
    private String name;

    @ApiModelProperty(value = "发生次数", required = true)
    private String happenCount;

    @ApiModelProperty(value = "上届预估", required = true)
    private BigDecimal preEstimate;

    @ApiModelProperty(value = "上届预算", required = true)
    private BigDecimal pretotal;

    @ApiModelProperty(value = "动因内容", required = true)
    private String remark;

    @ApiModelProperty(value = "计算过程", required = true)
    private String computingProcess;

}
