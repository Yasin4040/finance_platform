package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author User
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BudgetSubjectVO extends TreeNode<BudgetSubjectVO> {

    @ApiModelProperty(value = "预算单位Id")
    private Long budgetUnitId;

    @ApiModelProperty(value = "预算科目名称")
    private String name;

    @ApiModelProperty(value = "排序号")
    private Integer orderNo;

}
