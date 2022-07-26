package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "budget_jstormhandleinfo")
@Data
public class BudgetJstormhandleinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 1:年度追加  2:年度拆借 3：月度追加 4：月度拆借 5：报销执行
     */
    @ApiParam(hidden = true)
    @TableField(value = "type")
    private Integer type;

}
