package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Admin
 */
@TableName(value = "budget_projectlendbxtrans_new")
@Data
public class BudgetProjectlendbxtrans implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @ApiModelProperty(value = "主键Id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目借款主表id
     */
    @NotNull(message = "项目借款主表id不能为空")
    @ApiModelProperty(value = "项目借款主表id")
    @TableField(value = "projectlendsumid")
    private Long projectlendsumid;

    /**
     * 收款人编号
     */
    @NotBlank(message = "收款人编号不能为空")
    @ApiModelProperty(value = "收款人编号")
    @TableField(value = "payeecode")
    private String payeecode;

    /**
     * 收款人姓名（户名）
     */
    @NotBlank(message = "收款人姓名（户名）不能为空")
    @ApiModelProperty(value = "收款人姓名（户名）")
    @TableField(value = "payeename")
    private String payeename;

    /**
     * 收款人账户
     */
    @NotBlank(message = "收款人账户不能为空")
    @ApiModelProperty(value = "收款人账户")
    @TableField(value = "payeebankaccount")
    private String payeebankaccount;

    /**
     * 收款人开户行
     */
    @NotBlank(message = "收款人开户行不能为空")
    @ApiModelProperty(value = "收款人开户行")
    @TableField(value = "payeebankname")
    private String payeebankname;

    /**
     * 转账金额
     */
    @NotNull(message = "转账金额不能为空")
    @ApiModelProperty(value = "转账金额")
    @TableField(value = "transmoney")
    private BigDecimal transmoney;

    /**
     * 付款单位账户id
     */
    @NotNull(message = "付款单位账户id不能为空")
    @ApiModelProperty(value = "付款单位账户id")
    @TableField(value = "draweeunitaccountid")
    private Long draweeunitaccountid;

    /**
     * 付款单位名字
     */
    @NotBlank(message = "付款单位名字不能为空")
    @ApiModelProperty(value = "付款单位名字")
    @TableField(value = "draweeunitname")
    private String draweeunitname;

    /**
     * 付款单位账户
     */
    @NotBlank(message = "付款单位账户不能为空")
    @ApiModelProperty(value = "付款单位账户")
    @TableField(value = "draweebankaccount")
    private String draweebankaccount;

    /**
     * 付款单位账户开户行
     */
    @NotBlank(message = "付款单位账户开户行不能为空")
    @ApiModelProperty(value = "付款单位账户开户行")
    @TableField(value = "draweebankname")
    private String draweebankname;

}
