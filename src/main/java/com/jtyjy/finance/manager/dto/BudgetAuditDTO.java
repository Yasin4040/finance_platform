package com.jtyjy.finance.manager.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-22 09:49
 */
@Data
public class BudgetAuditDTO {

    @ApiModelProperty(value = "登录唯一标识",required = true)
    private String token;

    @ApiModelProperty("备注")
    private String remark;

    private List<BudgetDTO> budgetDto;
}
