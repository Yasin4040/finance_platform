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
@TableName(value = "tab_link_limit")
@Data
@ApiModel(description = "环节限制表")
public class TabLinkLimit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键id")
    private Long id;

    /**
     * 过程id
     */
    @NotNull(message = "流程id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "procedure_id")
    @ApiModelProperty(value = "流程id")
    private Long procedureId;

    @TableField(exist = false)
    @ApiModelProperty(value = "流程模板名称")
    private String procedureName;

    /**
     * 预算科目id
     */
    @ApiParam(hidden = true, required = false)
    @TableField(value = "subject_id")
    @ApiModelProperty(value = "预算科目id", required = false)
    private Long subjectId;

    @TableField(exist = false)
    @ApiModelProperty(value = "预算科目ids（多个用，隔开）")
    private String subjectIds;
    
    @TableField(exist = false)
    @ApiModelProperty(value = "预算科目名称")
    private String subjectName;

    /**
     * 环节代码
     */
    @NotEmpty(message = "环节代码不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "link_dm")
    @ApiModelProperty(value = "环节代码")
    private String linkDm;

    /**
     * 环节名称
     */
    @ApiParam(hidden = true)
    @TableField(exist = false)
    @ApiModelProperty(value = "环节名称")
    private String linkName;
    
    /**
     * 最小限度
     */
    @ApiParam(hidden = true)
    @TableField(value = "min_limit")
    @ApiModelProperty(value = "最小限度")
    private Double minLimit;

    /**
     * 最大限度
     */
    @NotNull(message = "最大限度不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "max_limit")
    @ApiModelProperty(value = "最大限度")
    private Double maxLimit;

    /**
     * 是否启用 0否 1是
     */
    @NotEmpty(message = "是否启用 0否 1是不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "is_active")
    @ApiModelProperty(value = "是否启用 0否 1是")
    private String isActive;
    
    @TableField(exist = false)
    private Long yearId;
    
    @TableField(exist = false)
    private String yearName;
}
