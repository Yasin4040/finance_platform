package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Admin
 */
@TableName(value = "budget_projectlendbxpayment_new")
@Data
public class BudgetProjectlendbxpayment implements Serializable {

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
    @ApiModelProperty(value = "项目借款主表id")
    @TableField(value = "projectlendsumid")
    private Long projectlendsumid;

    /**
     * 借款id
     */
    @NotNull(message = "借款id不能为空")
    @ApiModelProperty(value = "借款id")
    @TableField(value = "lendmoneyid")
    private Long lendmoneyid;

    /**
     * 借款人工号
     */
    @ApiModelProperty(value = "借款人工号")
    @TableField(value = "empno")
    private String empno;

    /**
     * 借款人名字
     */
    @ApiModelProperty(value = "借款人名字")
    @TableField(value = "lendmoneyname")
    private String lendmoneyname;

    /**
     * 借款编号
     */
    @ApiModelProperty(value = "借款编号")
    @TableField(value = "lendcode")
    private String lendcode;

    /**
     * 借款金额
     */
    @ApiModelProperty(value = "借款金额")
    @TableField(value = "lendmoney")
    private BigDecimal lendmoney;

    /**
     * 未还金额
     */
    @ApiModelProperty(value = "未还金额")
    @TableField(value = "unrepaidmoney")
    private BigDecimal unrepaidmoney;

    /**
     * 借款说明
     */
    @ApiModelProperty(value = "借款说明")
    @TableField(value = "lendmoneyremark")
    private String lendmoneyremark;

    /**
     * 冲账金额
     */
    @ApiModelProperty(value = "冲账金额")
    @TableField(value = "paymentmoney")
    private BigDecimal paymentmoney;

}
