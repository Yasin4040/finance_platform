package com.jtyjy.finance.manager.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-22 09:46
 */
@Data
public class BudgetDTO {

    @ApiModelProperty(value = "预算单位Id",required = true)
    private Long budgetUnitId;

    @ApiModelProperty(value = "月份Id",required = true)
    private Long monthId;
}
