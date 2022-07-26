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

@TableName(value = "budget_msgtemplete_parameter")
@Data
public class BudgetMsgtempleteParameter implements Serializable{
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(type = IdType.AUTO)
	private Long id;
    
    @ApiModelProperty(value = "模板id")
    @TableField(value = "templeteid")
    @NotNull(message = "模板id不能为空")
    private Long templeteid;
    
    @ApiModelProperty(value = "中文名称")
    @TableField(value = "chinesename")
    @NotBlank(message = "中文名称不能为空")
    private String chinesename;
    
    @ApiModelProperty(value = "英文名称")
    @TableField(value = "englishname")
    @NotBlank(message = "英文名称不能为空")
    private String englishname;
    
    @ApiModelProperty(value = "字段类型  1.文本  2.金钱 3.百分比 ")
    @TableField(value = "type")
    @NotNull(message = "字段类型不能为空")
    private Integer type;
    
    @ApiModelProperty(value = "字段类型名称。新增修改时不必传。")
    @TableField(exist = false)
    private String typeName;
    
    @ApiModelProperty(value = "排序号")
    @TableField(value = "orderno")
    @NotNull(message = "排序号不能为空")
    private Integer orderno;
    
    @ApiModelProperty(value = "消息类型")
    @TableField(value = "msgtype")
    @NotNull(message = "消息类型不能为空")
    private Integer msgtype;
    
    @ApiModelProperty(value = "消息类型名称。新增修改时不必传。")
    @TableField(exist = false)
    private String msgtypeName;
    
    @ApiModelProperty(value = "excel对应的列")
    @TableField(value = "excelcolumn")
    @NotBlank(message = "excel对应的列不能为空")
    private String excelcolumn;
}
