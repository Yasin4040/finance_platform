package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "budget_product")
@Data
public class BudgetProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 产品id
     */
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "产品id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 产品名称
     */
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "产品名称")
    @TableField(value = "name")
    private String name;

    /**
     * 名称首拼
     */
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "名称首拼")
    @TableField(value = "firstspell")
    private String firstspell;

    /**
     * 名称全拼
     */
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "名称全拼")
    @TableField(value = "fullspell")
    private String fullspell;

    /**
     * 产品分类id
     */
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "产品分类id")
    @TableField(value = "procategoryid")
    private Long procategoryid;

    /**
     * 产品分类名称
     */
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "产品分类名称")
    @TableField(exist = false)
    private String categoryname;

    /**
     * 停用标识 0：启用【默认】 1：停用
     */
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "停用标识 0：启用【默认】 1：停用")
    @TableField(value = "stopflag")
    private Integer stopflag;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "备注")
    @TableField(value = "remark")
    private String remark;

    /**
     * 排序号
     */
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "排序号")
    @TableField(value = "orderno")
    private Integer orderno;

    /**
     * 产品编号
     */
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "产品编号")
    @TableField(value = "productno")
    private String productno;

    // ------------------------------ 扩展字段 ------------------------------

    @ApiParam(hidden = true)
    @TableField(exist = false)
    private String pids;

}
