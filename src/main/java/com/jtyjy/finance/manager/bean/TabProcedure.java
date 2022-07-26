package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author shubo
 */
@TableName(value = "tab_procedure")
@Data
@ApiModel(description = "流程模板表")
public class TabProcedure implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "流程id")
    private Long id;

    /**
     * 届别主键
     */
    @NotNull(message = "届别主键不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    @ApiModelProperty(value = "届别id")
    private Long yearid;

    /**
     * 届别名称
     */
    @TableField(exist = false)
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "届别名称", hidden = true)
    private String yearName;

    /**
     * 流程名称
     */
    @NotEmpty(message = "流程名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "procedure_name")
    @ApiModelProperty(value = "流程名称")
    private String procedureName;

    /**
     * 流程类型 1报销 2其他
     */
    @NotEmpty(message = "流程类型 1报销 2其他不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "procedure_type")
    @ApiModelProperty(value = "流程类型 1报销 2其他")
    private String procedureType;

    /**
     * 环节顺序
     */
    @NotEmpty(message = "环节顺序不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "procedure_link_order")
    @ApiModelProperty(value = "环节顺序")
    private String procedureLinkOrder;

    /**
     * 环节名称（逗号分隔）
     */
    @TableField(exist = false)
    @ApiParam(hidden = true)
    @ApiModelProperty(value = "环节名称（逗号分隔）", hidden = true)
    private String linkOrderName;

    /**
     * 是否启用 0否 1是
     */
    @NotEmpty(message = "是否启用 0否 1是不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "is_active")
    @ApiModelProperty(value = "是否启用 0否 1是")
    private String isActive;

    /**
     * 是否删除 0否 1是
     */
    @ApiParam(hidden = true)
    @TableField(value = "is_delete")
    @ApiModelProperty(value = "是否删除 0否 1是")
    private String isDelete;
}
