package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_reimbursementorder_cash_copy")
@Data
public class BudgetReimbursementorderCashCopy implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "cashid")
    private Long cashid;

    @TableField(value = "reimbursementid")
    private Long reimbursementid;

    @TableField(value = "payeecode")
    private String payeecode;

    @TableField(value = "payeename")
    private String payeename;

    @TableField(value = "cashmoney")
    private BigDecimal cashmoney;

    @TableField(value = "olddraweeunitaccountid")
    private Long olddraweeunitaccountid;

    @TableField(value = "draweeunitaccountid")
    private Long draweeunitaccountid;

    @TableField(value = "draweeunitname")
    private String draweeunitname;

    @TableField(value = "draweebankaccount")
    private String draweebankaccount;

    @TableField(value = "draweebankname")
    private String draweebankname;

    @TableField(value = "paymoneyid")
    private Long paymoneyid;

    @TableField(value = "createtime")
    private Date createtime;

}
