package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_reimbursementorder_cash_log")
@Data
public class BudgetReimbursementorderCashLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "cashid")
    private Long cashid;

    /**
     * 报销单id
     */
    @NotNull(message = "报销单id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "reimbursementid")
    private Long reimbursementid;

    /**
     * 收款人编号
     */
    @NotBlank(message = "收款人编号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "payeecode")
    private String payeecode;

    /**
     * 收款人姓名（户名）
     */
    @NotBlank(message = "收款人姓名（户名）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "payeename")
    private String payeename;

    /**
     * 现金金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "cashmoney")
    private BigDecimal cashmoney;

    /**
     * 修改之前的付款单位id
     */
    @ApiParam(hidden = true)
    @TableField(value = "olddraweeunitaccountid")
    private Long olddraweeunitaccountid;

    /**
     * 付款单位id
     */
    @ApiParam(hidden = true)
    @TableField(value = "draweeunitaccountid")
    private Long draweeunitaccountid;

    /**
     * 付款单位名字
     */
    @NotBlank(message = "付款单位名字不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "draweeunitname")
    private String draweeunitname;

    /**
     * 付款单位账户
     */
    @ApiParam(hidden = true)
    @TableField(value = "draweebankaccount")
    private String draweebankaccount;

    /**
     * 付款单位账户开户行
     */
    @ApiParam(hidden = true)
    @TableField(value = "draweebankname")
    private String draweebankname;

    /**
     * 付款单id
     */
    @ApiParam(hidden = true)
    @TableField(value = "paymoneyid")
    private Long paymoneyid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "creater")
    private String creater;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "creatername")
    private String creatername;

}
