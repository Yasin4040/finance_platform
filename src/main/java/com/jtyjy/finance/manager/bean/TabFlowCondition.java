package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "tab_flow_condition")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TabFlowCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键", hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 流程类型：1：报销  2：其他
     */
    @NotBlank(message = "流程类型：1：报销  2：其他不能为空")
    @ApiModelProperty(value = "流程类型：1：报销  2：其他", hidden = false)
    @TableField(value = "flow_type")
    private String flowType;

    /**
     * 环节代码
     */
    @NotBlank(message = "环节代码不能为空")
    @ApiModelProperty(value = "环节代码", hidden = false)
    @TableField(value = "step_dm")
    private String stepDm;

    @ApiModelProperty(value = "环节名称", hidden = true)
    @TableField(exist = false)
    private String stepDmName;
    
    /**
     * 条件环节代码
     */
    @NotBlank(message = "前置条件环节代码不能为空")
    @ApiModelProperty(value = "前置条件环节代码", hidden = false)
    @TableField(value = "condition_step_dm")
    private String conditionStepDm;

    @ApiModelProperty(value = "前置条件环节名称", hidden = true)
    @TableField(exist = false)
    private String conditionStepDmName;
    
    /**
     * 条件 报销条件（1：已接收 2：审核通过） 其他自定义
     */
    @NotBlank(message = "条件 报销条件（1：已接收 2：审核通过） 其他自定义不能为空")
    @ApiModelProperty(value = "条件 报销条件（1：已接收 2：审核通过） 其他自定义", hidden = false)
    @TableField(value = "the_condition")
    private String theCondition;

    /**
     * 版本
     */
    @ApiModelProperty(value = "版本（流程模板id）", hidden = false)
    @TableField(value = "the_version")
    private Integer theVersion;


    @ApiModelProperty(value = "模板名称", hidden = false)
    @TableField(exist = false)
    private String procedureName;
    
    @TableField(exist = false)
    private Long yearId;
    
    @ApiModelProperty(value = "届别名称", hidden = false)
    @TableField(exist = false)
    private String yearName;
}
