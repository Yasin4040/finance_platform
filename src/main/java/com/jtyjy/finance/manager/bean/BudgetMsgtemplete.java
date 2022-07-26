package com.jtyjy.finance.manager.bean;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@TableName(value = "budget_msgtemplete")
@Data
public class BudgetMsgtemplete implements Serializable{
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(type = IdType.AUTO)
	private Long id;
    
    @ApiModelProperty(value = "届别id")
    @TableField(value = "yearid")
    @NotNull(message = "届别不能为空")
    private Long yearid;
    
    @ApiModelProperty(value = "届别名称。新增修改时不必传。")
    @TableField(exist = false)
    private String yearName;
    
    @ApiModelProperty(value = "模板类别")
    @TableField(value = "templetecategory")
    @NotNull(message = "模板类别不能为空")
    private Integer templetecategory;
    
    @ApiModelProperty(value = "模板类别名称。新增修改时不必传。")
    @TableField(exist = false)
    private String templetecategoryName;
    
    @ApiModelProperty(value = "模板类型")
    @TableField(value = "templetetype")
    @NotNull(message = "模板类型不能为空")
    private Integer templetetype;
    
    @ApiModelProperty(value = "模板类型名称。新增修改时不必传。")
    @TableField(exist = false)
    private String templetetypeName;
    
    @ApiModelProperty(value = "预警信息模板")
    @TableField(value = "warnmsg")
    private String warnmsg;
    
    @ApiModelProperty(value = "公示信息模板")
    @TableField(value = "publicitymsg")
    private String publicitymsg;
    
    @ApiModelProperty(value = "结果信息模板")
    @TableField(value = "resultmsg")
    private String resultmsg;
    
    @ApiModelProperty(value = "下年度提成预留比例")
    @TableField(value = "percent")
    @NotNull(message = "下年度提成预留比例不能为空")
    private BigDecimal percent;
    
    @ApiModelProperty(value = "备注")
    @TableField(value = "remark")
    private String remark;
}
