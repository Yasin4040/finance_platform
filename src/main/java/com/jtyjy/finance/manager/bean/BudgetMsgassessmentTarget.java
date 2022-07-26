package com.jtyjy.finance.manager.bean;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@TableName(value = "budget_msgassessmenttarget")
@Data
public class BudgetMsgassessmentTarget implements Serializable{
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(type = IdType.AUTO)
	private Long id;
    
    @ApiModelProperty(value = "模板id")
    @TableField(value = "templeteid")
    @NotNull(message = "模板id不能为空")
    private Long templeteid;
    
    @ApiModelProperty(value = "考核日期")
    @TableField(value = "date")
    @NotBlank(message = "考核日期不能为空")
    private String date;
    
    @ApiModelProperty(value = "考核目标")
    @TableField(value = "target")
    @NotNull(message = "考核目标不能为空")
    private BigDecimal target;
    
}
