package com.jtyjy.finance.manager.bean;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@TableName(value = "budget_msgtemplete_detail_new")
@Data
public class BudgetMsgtempleteDetailNew implements Serializable{
	private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(type = IdType.AUTO)
	private Long id;
    
    @ApiModelProperty(value = "模板id")
    @TableField(value = "templeteid")
    private Long templeteid;
    
    @ApiModelProperty(value = "是否发送")
    @TableField(value = "issend")
    private Boolean issend;
    
    @ApiModelProperty(value = "是否预览")
    @TableField(value = "ispreview")
    private Boolean ispreview;
    
    @ApiModelProperty(value = "是否异议")
    @TableField(value = "isobjection")
    private Boolean isobjection;
    
    @ApiModelProperty(value = "操作时间")
    @TableField(value = "operatetime")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date operatetime;
    
    @ApiModelProperty(value = "异议详情")
    @TableField(value = "objectdesc")
    private String objectdesc;
    
    @ApiModelProperty(hidden = true)
    @TableField(value = "msgcontent")
    private String msgcontent;
           
    @ApiModelProperty(value = "发送人")
    @TableField(value = "empno")
    private String empno;
    
    @ApiModelProperty(value = "消息内容",hidden = false)
    @TableField(exist = false)
    private String content;
    
    @ApiModelProperty(value = "消息类型",hidden = false)
    @TableField(exist = false)
    private Integer msgType;
    
}
