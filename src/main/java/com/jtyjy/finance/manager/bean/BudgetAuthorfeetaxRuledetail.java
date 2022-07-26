package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Admin
 */
@TableName(value = "budget_authorfeetax_ruledetail")
@Data
public class BudgetAuthorfeetaxRuledetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "id(修改时需传)")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 计税规则id
     */
    @NotNull(message = "计税规则id不能为空")
    @ApiModelProperty(value = "计税规则主表id")
    @TableField(value = "taxruleid")
    private Long taxruleid;

    /**
     * 最小金额
     */
    @NotNull(message = "最小金额不能为空")
    @ApiModelProperty(value = "最小金额")
    @TableField(value = "min")
    private BigDecimal min;

    /**
     * 最大金额
     */
    @NotNull(message = "最大金额不能为空")
    @ApiModelProperty(value = "最大金额")
    @TableField(value = "max")
    private BigDecimal max;

    /**
     * 计算公式，如果公式为空 表示不计税
     */
    @ApiModelProperty(value = "计算公式")
    @TableField(value = "formula")
    private String formula;

}
