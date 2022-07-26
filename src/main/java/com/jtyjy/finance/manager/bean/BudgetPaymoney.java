package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jtyjy.finance.manager.easyexcel.PayErrorImportExcelData;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_paymoney")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BudgetPaymoney implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键", hidden = false, required = false)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 付款批次id
     */
    @ApiModelProperty(value = "付款批次id", hidden = false, required = false)
    @TableField(value = "paybatchid")
    private Long paybatchid;

    /**
     * 付款单号
     */
    @ApiModelProperty(value = "付款单号", hidden = false, required = false)
    @TableField(value = "paymoneycode")
    private String paymoneycode;

    /**
     * 付款单类型：1：报销转账付款 2：提成发放付款 3：(日常)借款付款 4：资金调拨付款 5:项目现金付款 6:项目转账付款（借款）
     */
    @ApiModelProperty(value = "付款单类型：1：报销转账付款 2：提成发放付款 3：(日常)借款付款 4：资金调拨付款 5:项目现金付款 6:项目转账付款（借款）", hidden = false, required = false)
    @TableField(value = "paymoneytype")
    private Integer paymoneytype;

    /**
     * 付款对象编号（报销单号等等）(提成主表单号） 项目转账付款 存的是项目编号
     */
    @ApiModelProperty(value = "付款对象编号", hidden = false, required = false)
    @TableField(value = "paymoneyobjectcode")
    private String paymoneyobjectcode;

    /**
     * 付款对象id(报销转账id,提成发放id,借款id,工资发放id,稿费id,资金挑拨id)
     */
    @ApiModelProperty(value = "付款对象id", hidden = false, required = false)
    @TableField(value = "paymoneyobjectid")
    private Long paymoneyobjectid;

    /**
     * 付款金额
     */
    @ApiModelProperty(value = "付款金额不能为空", hidden = false, required = false)
    @TableField(value = "paymoney")
    private BigDecimal paymoney;

    /**
     * 支付类型：0:现金；1:转账
     */
    @ApiModelProperty(value = "支付类型：0:现金；1:转账", hidden = false, required = false)
    @TableField(value = "paytype")
    private Integer paytype;

    @ApiModelProperty(value = "付款状态 0:等待付款；1：接收付款；2：正在付款；3：已经付款", hidden = false, required = false)
    @TableField(value = "paymoneystatus")
    private Integer paymoneystatus;

    @ApiModelProperty(value = "日常借款付款 对应的单据类型（类似于借款类型）", hidden = false, required = false)
    @TableField(value = "lendtype")
    private Integer lendtype;

    /**
     * 支付时间
     */
    @ApiModelProperty(value = "支付时间", hidden = false, required = false)
    @TableField(value = "paytime")
    private Date paytime;

    @ApiModelProperty(value = "验证状态 -1:支付失败 0：等待验证 1：支付成功", hidden = false, required = false)
    @TableField(value = "verifystatus")
    private Integer verifystatus;

    @ApiModelProperty(value = "验证时间", hidden = false, required = false)
    @TableField(value = "verifytime")
    private Date verifytime;

    /**
     * 验证人工号
     */
    @ApiModelProperty(value = "验证人工号", hidden = false, required = false)
    @TableField(value = "verifyer")
    private String verifyer;

    /**
     * 验证人名字
     */
    @ApiModelProperty(value = "验证人名字", hidden = false, required = false)
    @TableField(value = "verifyername")
    private String verifyername;

    /**
     * 验证信息
     */
    @ApiModelProperty(value = "验证信息", hidden = false, required = false)
    @TableField(value = "verifyremark")
    private String verifyremark;

    /**
     * 接收时间
     */
    @ApiModelProperty(value = "接收时间", hidden = false, required = false)
    @TableField(value = "receivetime")
    private Date receivetime;

    /**
     * 接收人工号
     */
    @ApiModelProperty(value = "接收人工号", hidden = false, required = false)
    @TableField(value = "receiver")
    private String receiver;

    /**
     * 接收人名字
     */
    @ApiModelProperty(value = "接收人名字", hidden = false, required = false)
    @TableField(value = "receivername")
    private String receivername;

    /**
     * //月份 201809、201810
     */
    @ApiModelProperty(value = "月份", hidden = false, required = false)
    @TableField(value = "month")
    private String month;

    @ApiModelProperty(value = "创建时间", hidden = false, required = false)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 开票单位名称(户名) - 支付方
     */
    @ApiModelProperty(value = " 开票单位名称(户名) - 支付方", hidden = false, required = false)
    @TableField(value = "bunitname")
    private String bunitname;

    /**
     * 开票单位账户（银行账号）-支付方
     */
    @ApiModelProperty(value = "开票单位账户（银行账号）-支付方", hidden = false, required = false)
    @TableField(value = "bunitbankaccount")
    private String bunitbankaccount;

    /**
     * 开票单位账户 - 银行编号-支付方
     */
    @ApiModelProperty(value = "开票单位账户 - 银行编号-支付方", hidden = false, required = false)
    @TableField(value = "bunitaccountbranchcode")
    private String bunitaccountbranchcode;

    /**
     * 开票单位账户 - 银行名称-支付方
     */
    @ApiModelProperty(value = "开票单位账户 - 银行名称-支付方", hidden = false, required = false)
    @TableField(value = "bunitaccountbranchname")
    private String bunitaccountbranchname;

    /**
     * 银行账户 名称(户名)-收款方
     */
    @ApiModelProperty(value = "银行账户 名称(户名)-收款方", hidden = false, required = false)
    @TableField(value = "bankaccountname")
    private String bankaccountname;

    /**
     * 银行账户 -收款方
     */
    @ApiModelProperty(value = "银行账户 -收款方", hidden = false, required = false)
    @TableField(value = "bankaccount")
    private String bankaccount;

    /**
     * 银行账户 - 银行编号 - 收款方
     */
    @ApiModelProperty(value = "银行账户 - 银行编号 - 收款方", hidden = false, required = false)
    @TableField(value = "bankaccountbranchcode")
    private String bankaccountbranchcode;

    /**
     * 银行账户 - 银行名称-收款方
     */
    @ApiModelProperty(value = "银行账户 - 银行名称-收款方", hidden = false, required = false)
    @TableField(value = "bankaccountbranchname")
    private String bankaccountbranchname;

    /**
     * 开户行(收款)
     */
    @ApiModelProperty(value = "开户行(收款)", hidden = false, required = false)
    @TableField(value = "openbank")
    private String openbank;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注", hidden = false, required = false)
    @TableField(value = "remark")
    private String remark;

    /**
     * 备注
     */
    @ApiModelProperty(value = "付款批次号", hidden = false, required = false)
    @TableField(exist = false)
    private String paybatchcode;
    

    /**
     * 支付时间字符串
     */
    @ApiModelProperty(value = "支付时间字符串", hidden = false, required = false)
    @TableField(exist = false)
    private String paytimeStr;
    

    /**
     * 接收时间字符串
     */
    @ApiModelProperty(value = "接收时间字符串", hidden = false, required = false)
    @TableField(exist = false)
    private String receivetimeStr;

    @ApiModelProperty(value = "来源类型", hidden = false, required = false)
    @TableField(exist = false)
    private String sourceTypeName;
    
    public BudgetPaymoney(PayErrorImportExcelData excelData) {
        this.paymoneycode = excelData.getPayMoneyCode();
        this.paymoneyobjectcode = excelData.getPayObjectCode();
        this.bankaccount = excelData.getBankAccount();
        this.bankaccountname = excelData.getBankAccountName();
        this.openbank = excelData.getBankBranchName();
        this.paymoney = new BigDecimal(excelData.getPayMoney());
        this.bankaccountbranchcode = excelData.getBankBranchCode();
        this.bankaccountbranchname = excelData.getBankBranchName();
        
    }
}
