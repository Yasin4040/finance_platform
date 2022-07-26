package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_base_unit")
@Data
@ApiModel(description = "基础单位表")
public class BudgetBaseUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 基础单位主键
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "基础单位id")
    private Long id;

    /**
     * 基础单位名称
     */
    @NotBlank(message = "基础单位名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "name")
    @ApiModelProperty(value = "基础单位名称")
    private String name;

    /**
     * name首拼
     */
    @ApiParam(hidden = true)
    @TableField(value = "firstspell")
    @ApiModelProperty(value = "name首拼")
    private String firstspell;

    /**
     * name全拼
     */
    @ApiParam(hidden = true)
    @TableField(value = "fullspell")
    @ApiModelProperty(value = "数据集合")
    private String fullspell;

    /**
     * 停用标识 true：停用，false 不停用
     */
    @NotNull(message = "停用标识不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "stopflag")
    @ApiModelProperty(value = "停用标识 1：停用，0：启用")
    private Integer stopflag;

    /**
     * 排序号
     */
    @NotNull(message = "排序号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "orderno")
    @ApiModelProperty(value = "排序号")
    private Integer orderno;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    @ApiModelProperty(value = "创建时间")
    private Date createtime;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 更新时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    @ApiModelProperty(value = "更新时间")
    private Date updatetime;

}
