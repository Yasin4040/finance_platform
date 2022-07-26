package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_paymoney_log_new")
@Data
public class BudgetPaymoneyLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 付款单id
     */
    @ApiParam(hidden = true)
    @TableField(value = "paymoneyid")
    private Long paymoneyid;

    /**
     * 付款批次id
     */
    @ApiParam(hidden = true)
    @TableField(value = "paybatchid")
    private Long paybatchid;

    /**
     * 付款单号
     */
    @ApiParam(hidden = true)
    @TableField(value = "paymoneycode")
    private String paymoneycode;

    /**
     * //付款单类型：1：报销转账付款 2：提成发放付款 3：日常借款付款 4：工资发放付款 5：资金调拨付款
     */
    @NotNull(message = "//付款单类型：1：报销转账付款 2：提成发放付款 3：日常借款付款 4：工资发放付款 5：资金调拨付款不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "paymoneytype")
    private Integer paymoneytype;

    /**
     * 付款对象编号（报销单号等等）
     */
    @ApiParam(hidden = true)
    @TableField(value = "paymoneyobjectcode")
    private String paymoneyobjectcode;

    /**
     * 付款对象id(报销单id,提成发放id,借款id,工资发放id,稿费id,资金挑拨id)
     */
    @ApiParam(hidden = true)
    @TableField(value = "paymoneyobjectid")
    private Long paymoneyobjectid;

    /**
     * 付款金额
     */
    @NotNull(message = "付款金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "paymoney")
    private BigDecimal paymoney;

    /**
     * //支付类型：false:现金；true:转账
     */
    @NotNull(message = "//支付类型：false:现金；true:转账不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "paytype")
    private Boolean paytype;

    /**
     * //付款状态 0:等待付款；1：接收付款；2：正在付款；3：已经付款。
     */
    @NotNull(message = "//付款状态 0:等待付款；1：接收付款；2：正在付款；3：已经付款。不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "paymoneystatus")
    private Integer paymoneystatus;

    /**
     * //日常借款付款 对应的单据类型（类似于借款类型）
     */
    @ApiParam(hidden = true)
    @TableField(value = "lendtype")
    private Integer lendtype;

    /**
     * 支付时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "paytime")
    private Date paytime;

    /**
     * //月份 201809、201810
     */
    @ApiParam(hidden = true)
    @TableField(value = "month")
    private String month;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 开票单位名称(户名) - 支付方
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitname")
    private String bunitname;

    /**
     * 开票单位账户（银行账号）-支付方
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitbankaccount")
    private String bunitbankaccount;

    /**
     * 开票单位账户 - 银行编号-支付方
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccountbranchcode")
    private String bunitaccountbranchcode;

    /**
     * 开票单位账户 - 银行名称-支付方
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccountbranchname")
    private String bunitaccountbranchname;

    /**
     * 银行账户 名称(户名)-收款方
     */
    @ApiParam(hidden = true)
    @TableField(value = "bankaccountname")
    private String bankaccountname;

    /**
     * 银行账户 -收款方
     */
    @ApiParam(hidden = true)
    @TableField(value = "bankaccount")
    private String bankaccount;

    /**
     * 银行账户 - 银行编号 - 收款方
     */
    @ApiParam(hidden = true)
    @TableField(value = "bankaccountbranchcode")
    private String bankaccountbranchcode;

    /**
     * 银行账户 - 银行名称-收款方
     */
    @ApiParam(hidden = true)
    @TableField(value = "bankaccountbranchname")
    private String bankaccountbranchname;

    /**
     * 开户行(收款)
     */
    @ApiParam(hidden = true)
    @TableField(value = "openbank")
    private String openbank;

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
