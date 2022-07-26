package com.jtyjy.finance.manager.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author User
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BudgetUnitTree extends TreeNode<BudgetUnitTree> {

    @ApiModelProperty(value = "预算单位名称")
    private String name;

    @ApiModelProperty(value = "基础单位Id")
    private Long baseUnitId;

}
