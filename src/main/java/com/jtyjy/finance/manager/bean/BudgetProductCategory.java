package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;

/**
 * 产品分类表
 *
 * @author shubo
 */
@TableName(value = "budget_product_category")
@Data
@ApiModel(description = "产品分类表")
public class BudgetProductCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 产品分类id
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "产品分类id")
    private Long id;

    /**
     * 名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "name")
    @ApiModelProperty(value = "产品分类名称")
    private String name;

    /**
     * 树名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "fullname")
    @ApiModelProperty(value = "树名称")
    private String fullname;

    /**
     * 层级
     */
    @ApiParam(hidden = true)
    @TableField(value = "level")
    @ApiModelProperty(value = "层级")
    private Integer level;

    /**
     * 父id
     */
    @ApiParam(hidden = true)
    @TableField(value = "pid")
    @ApiModelProperty(value = "父id")
    private Long pid;

    /**
     * 树id
     */
    @ApiParam(hidden = true)
    @TableField(value = "pids")
    @ApiModelProperty(value = "树id")
    private String pids;

    /**
     * 停用标识 0：启用【默认】 1：停用
     */
    @ApiParam(hidden = true)
    @TableField(value = "stopflag")
    @ApiModelProperty(value = "停用标识 0：启用【默认】 1：停用")
    private Integer stopflag;

    /**
     * 排序号
     */
    @ApiParam(hidden = true)
    @TableField(value = "orderno")
    @ApiModelProperty(value = "排序号")
    private Integer orderno;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    @ApiModelProperty(value = "备注")
    private String remark;

}
