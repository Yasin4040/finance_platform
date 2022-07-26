package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "budget_unit_product")
@Data
public class BudgetUnitProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预算单位id
     */
    @NotNull(message = "预算单位id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 产品id
     */
    @NotNull(message = "产品id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "productid")
    private Long productid;

}
