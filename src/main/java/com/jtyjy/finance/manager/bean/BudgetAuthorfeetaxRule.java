package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_authorfeetax_rule")
@Data
public class BudgetAuthorfeetaxRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "id(修改时需传)")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    @NotEmpty(message = "规则名称不能为空")
    @ApiModelProperty(value = "规则名称")
    @TableField(value = "name")
    private String name;

    /**
     * 是否生效 true 生效 , 生效后不可修改
     */
    @NotNull(message = "是否生效不能为空")
    @ApiModelProperty(value = "是否生效（true,false）")
    @TableField(value = "effectflag")
    private Boolean effectflag;

    /**
     * 是否终止 true 终止 ， 可以提前终止
     */
    @NotNull(message = "是否终止不能为空")
    @ApiModelProperty(value = "是否终止（true,false）")
    @TableField(value = "endflag")
    private Boolean endflag;

    /**
     * 生效开始日期
     */
    @NotNull(message = "生效日期不能为空")
    @ApiModelProperty(hidden = false, value = "生效日期（yyyy-mm-dd）")
    @TableField(value = "effectdate")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date effectdate;

    /**
     * 未知参数
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 未知参数
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 备注
     */
    @ApiModelProperty(hidden = false, value = "备注")
    @TableField(value = "remark")
    private String remark;

}
