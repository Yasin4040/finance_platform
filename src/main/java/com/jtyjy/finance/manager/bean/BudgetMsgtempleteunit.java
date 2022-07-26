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

@TableName(value = "budget_msgtempleteunit")
@Data
public class BudgetMsgtempleteunit implements Serializable{
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(type = IdType.AUTO)
	private Long id;
    
    @ApiModelProperty(value = "模板id")
    @TableField(value = "templeteid")
    @NotNull(message = "模板id不能为空")
    private Long templeteid;
    
    @ApiModelProperty(value = "预算单位id")
    @TableField(value = "unitid")
    private Long unitid;
    
    @ApiModelProperty(value = "还款户名")
    @TableField(value = "hkhm")
    @NotBlank(message = "还款户名不能为空")
    private String hkhm;
    
    @ApiModelProperty(value = "还款账号")
    @TableField(value = "hkzs")
    @NotBlank(message = "还款账号")
    private String hkzs;
    
    @ApiModelProperty(value = "还款开户行")
    @TableField(value = "hkkhh")
    @NotBlank(message = "还款开户行")
    private String hkkhh;
}
