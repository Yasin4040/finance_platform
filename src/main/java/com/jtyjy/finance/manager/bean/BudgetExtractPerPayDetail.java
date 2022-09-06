package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 提成每笔发放明细表
 * @TableName budget_extract_per_pay_detail
 */
@TableName(value ="budget_extract_per_pay_detail")
public class BudgetExtractPerPayDetail implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提成批次
     */
    private String extractMonth;

    /**
     * 提成单号
     */
    private String extractCode;

    /**
     * 付款单位
     */
    private Long billingUnitId;

    /**
     * 付款单位账户
     */
    private String billingUnitAccount;

    /**
     * 付款单位账户电子银联号
     */
    private String billingUnitBranchCode;

    /**
     * 付款单位银行类型
     */
    private String billingUnitBankName;

    /**
     * 付款单位开户行
     */
    private String billingUnitOpenBank;

    /**
     * 付款单位名称
     */
    private String billingUnitName;

    /**
     * 发放金额
     */
    private BigDecimal payMoney;

    /**
     * 是否是公司员工
     */
    private Boolean isCompanyEmp;

    /**
     * 员工个体户id
     */
    private Long personalityId;

    /**
     * 收款人标识
     */
    private String receiverCode;

    /**
     * 收款人户名
     */
    private String receiverName;

    /**
     * 收款人银行账号
     */
    private String receiverBankAccountr;

    /**
     * 收款人银行账号电子银联号
     */
    private String receiverBankAccountBranchCoder;

    /**
     * 收款人银行类型
     */
    private String receiveBankAccountBankName;

    /**
     * 收款人开户行
     */
    private String receiverOpenBank;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 排序值
     */
    private Integer orderNo;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public Long getId() {
        return id;
    }

    /**
     * 
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 提成批次
     */
    public String getExtractMonth() {
        return extractMonth;
    }

    /**
     * 提成批次
     */
    public void setExtractMonth(String extractMonth) {
        this.extractMonth = extractMonth;
    }

    /**
     * 提成单号
     */
    public String getExtractCode() {
        return extractCode;
    }

    /**
     * 提成单号
     */
    public void setExtractCode(String extractCode) {
        this.extractCode = extractCode;
    }

    /**
     * 付款单位
     */
    public Long getBillingUnitId() {
        return billingUnitId;
    }

    /**
     * 付款单位
     */
    public void setBillingUnitId(Long billingUnitId) {
        this.billingUnitId = billingUnitId;
    }

    /**
     * 付款单位账户
     */
    public String getBillingUnitAccount() {
        return billingUnitAccount;
    }

    /**
     * 付款单位账户
     */
    public void setBillingUnitAccount(String billingUnitAccount) {
        this.billingUnitAccount = billingUnitAccount;
    }

    /**
     * 付款单位账户电子银联号
     */
    public String getBillingUnitBranchCode() {
        return billingUnitBranchCode;
    }

    /**
     * 付款单位账户电子银联号
     */
    public void setBillingUnitBranchCode(String billingUnitBranchCode) {
        this.billingUnitBranchCode = billingUnitBranchCode;
    }

    /**
     * 付款单位银行类型
     */
    public String getBillingUnitBankName() {
        return billingUnitBankName;
    }

    /**
     * 付款单位银行类型
     */
    public void setBillingUnitBankName(String billingUnitBankName) {
        this.billingUnitBankName = billingUnitBankName;
    }

    /**
     * 付款单位开户行
     */
    public String getBillingUnitOpenBank() {
        return billingUnitOpenBank;
    }

    /**
     * 付款单位开户行
     */
    public void setBillingUnitOpenBank(String billingUnitOpenBank) {
        this.billingUnitOpenBank = billingUnitOpenBank;
    }

    /**
     * 付款单位名称
     */
    public String getBillingUnitName() {
        return billingUnitName;
    }

    /**
     * 付款单位名称
     */
    public void setBillingUnitName(String billingUnitName) {
        this.billingUnitName = billingUnitName;
    }

    /**
     * 发放金额
     */
    public BigDecimal getPayMoney() {
        return payMoney;
    }

    /**
     * 发放金额
     */
    public void setPayMoney(BigDecimal payMoney) {
        this.payMoney = payMoney;
    }

    /**
     * 是否是公司员工
     */
    public Boolean getIsCompanyEmp() {
        return isCompanyEmp;
    }

    /**
     * 是否是公司员工
     */
    public void setIsCompanyEmp(Boolean isCompanyEmp) {
        this.isCompanyEmp = isCompanyEmp;
    }

    /**
     * 员工个体户id
     */
    public Long getPersonalityId() {
        return personalityId;
    }

    /**
     * 员工个体户id
     */
    public void setPersonalityId(Long personalityId) {
        this.personalityId = personalityId;
    }

    /**
     * 收款人标识
     */
    public String getReceiverCode() {
        return receiverCode;
    }

    /**
     * 收款人标识
     */
    public void setReceiverCode(String receiverCode) {
        this.receiverCode = receiverCode;
    }

    /**
     * 收款人户名
     */
    public String getReceiverName() {
        return receiverName;
    }

    /**
     * 收款人户名
     */
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    /**
     * 收款人银行账号
     */
    public String getReceiverBankAccountr() {
        return receiverBankAccountr;
    }

    /**
     * 收款人银行账号
     */
    public void setReceiverBankAccountr(String receiverBankAccountr) {
        this.receiverBankAccountr = receiverBankAccountr;
    }

    /**
     * 收款人银行账号电子银联号
     */
    public String getReceiverBankAccountBranchCoder() {
        return receiverBankAccountBranchCoder;
    }

    /**
     * 收款人银行账号电子银联号
     */
    public void setReceiverBankAccountBranchCoder(String receiverBankAccountBranchCoder) {
        this.receiverBankAccountBranchCoder = receiverBankAccountBranchCoder;
    }

    /**
     * 收款人银行类型
     */
    public String getReceiveBankAccountBankName() {
        return receiveBankAccountBankName;
    }

    /**
     * 收款人银行类型
     */
    public void setReceiveBankAccountBankName(String receiveBankAccountBankName) {
        this.receiveBankAccountBankName = receiveBankAccountBankName;
    }

    /**
     * 收款人开户行
     */
    public String getReceiverOpenBank() {
        return receiverOpenBank;
    }

    /**
     * 收款人开户行
     */
    public void setReceiverOpenBank(String receiverOpenBank) {
        this.receiverOpenBank = receiverOpenBank;
    }

    /**
     * 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 排序值
     */
    public Integer getOrderNo() {
        return orderNo;
    }

    /**
     * 排序值
     */
    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        BudgetExtractPerPayDetail other = (BudgetExtractPerPayDetail) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getExtractMonth() == null ? other.getExtractMonth() == null : this.getExtractMonth().equals(other.getExtractMonth()))
            && (this.getExtractCode() == null ? other.getExtractCode() == null : this.getExtractCode().equals(other.getExtractCode()))
            && (this.getBillingUnitId() == null ? other.getBillingUnitId() == null : this.getBillingUnitId().equals(other.getBillingUnitId()))
            && (this.getBillingUnitAccount() == null ? other.getBillingUnitAccount() == null : this.getBillingUnitAccount().equals(other.getBillingUnitAccount()))
            && (this.getBillingUnitBranchCode() == null ? other.getBillingUnitBranchCode() == null : this.getBillingUnitBranchCode().equals(other.getBillingUnitBranchCode()))
            && (this.getBillingUnitBankName() == null ? other.getBillingUnitBankName() == null : this.getBillingUnitBankName().equals(other.getBillingUnitBankName()))
            && (this.getBillingUnitOpenBank() == null ? other.getBillingUnitOpenBank() == null : this.getBillingUnitOpenBank().equals(other.getBillingUnitOpenBank()))
            && (this.getBillingUnitName() == null ? other.getBillingUnitName() == null : this.getBillingUnitName().equals(other.getBillingUnitName()))
            && (this.getPayMoney() == null ? other.getPayMoney() == null : this.getPayMoney().equals(other.getPayMoney()))
            && (this.getIsCompanyEmp() == null ? other.getIsCompanyEmp() == null : this.getIsCompanyEmp().equals(other.getIsCompanyEmp()))
            && (this.getPersonalityId() == null ? other.getPersonalityId() == null : this.getPersonalityId().equals(other.getPersonalityId()))
            && (this.getReceiverCode() == null ? other.getReceiverCode() == null : this.getReceiverCode().equals(other.getReceiverCode()))
            && (this.getReceiverName() == null ? other.getReceiverName() == null : this.getReceiverName().equals(other.getReceiverName()))
            && (this.getReceiverBankAccountr() == null ? other.getReceiverBankAccountr() == null : this.getReceiverBankAccountr().equals(other.getReceiverBankAccountr()))
            && (this.getReceiverBankAccountBranchCoder() == null ? other.getReceiverBankAccountBranchCoder() == null : this.getReceiverBankAccountBranchCoder().equals(other.getReceiverBankAccountBranchCoder()))
            && (this.getReceiveBankAccountBankName() == null ? other.getReceiveBankAccountBankName() == null : this.getReceiveBankAccountBankName().equals(other.getReceiveBankAccountBankName()))
            && (this.getReceiverOpenBank() == null ? other.getReceiverOpenBank() == null : this.getReceiverOpenBank().equals(other.getReceiverOpenBank()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getOrderNo() == null ? other.getOrderNo() == null : this.getOrderNo().equals(other.getOrderNo()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getExtractMonth() == null) ? 0 : getExtractMonth().hashCode());
        result = prime * result + ((getExtractCode() == null) ? 0 : getExtractCode().hashCode());
        result = prime * result + ((getBillingUnitId() == null) ? 0 : getBillingUnitId().hashCode());
        result = prime * result + ((getBillingUnitAccount() == null) ? 0 : getBillingUnitAccount().hashCode());
        result = prime * result + ((getBillingUnitBranchCode() == null) ? 0 : getBillingUnitBranchCode().hashCode());
        result = prime * result + ((getBillingUnitBankName() == null) ? 0 : getBillingUnitBankName().hashCode());
        result = prime * result + ((getBillingUnitOpenBank() == null) ? 0 : getBillingUnitOpenBank().hashCode());
        result = prime * result + ((getBillingUnitName() == null) ? 0 : getBillingUnitName().hashCode());
        result = prime * result + ((getPayMoney() == null) ? 0 : getPayMoney().hashCode());
        result = prime * result + ((getIsCompanyEmp() == null) ? 0 : getIsCompanyEmp().hashCode());
        result = prime * result + ((getPersonalityId() == null) ? 0 : getPersonalityId().hashCode());
        result = prime * result + ((getReceiverCode() == null) ? 0 : getReceiverCode().hashCode());
        result = prime * result + ((getReceiverName() == null) ? 0 : getReceiverName().hashCode());
        result = prime * result + ((getReceiverBankAccountr() == null) ? 0 : getReceiverBankAccountr().hashCode());
        result = prime * result + ((getReceiverBankAccountBranchCoder() == null) ? 0 : getReceiverBankAccountBranchCoder().hashCode());
        result = prime * result + ((getReceiveBankAccountBankName() == null) ? 0 : getReceiveBankAccountBankName().hashCode());
        result = prime * result + ((getReceiverOpenBank() == null) ? 0 : getReceiverOpenBank().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getOrderNo() == null) ? 0 : getOrderNo().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", extractMonth=").append(extractMonth);
        sb.append(", extractCode=").append(extractCode);
        sb.append(", billingUnitId=").append(billingUnitId);
        sb.append(", billingUnitAccount=").append(billingUnitAccount);
        sb.append(", billingUnitBranchCode=").append(billingUnitBranchCode);
        sb.append(", billingUnitBankName=").append(billingUnitBankName);
        sb.append(", billingUnitOpenBank=").append(billingUnitOpenBank);
        sb.append(", billingUnitName=").append(billingUnitName);
        sb.append(", payMoney=").append(payMoney);
        sb.append(", isCompanyEmp=").append(isCompanyEmp);
        sb.append(", personalityId=").append(personalityId);
        sb.append(", receiverCode=").append(receiverCode);
        sb.append(", receiverName=").append(receiverName);
        sb.append(", receiverBankAccountr=").append(receiverBankAccountr);
        sb.append(", receiverBankAccountBranchCoder=").append(receiverBankAccountBranchCoder);
        sb.append(", receiveBankAccountBankName=").append(receiveBankAccountBankName);
        sb.append(", receiverOpenBank=").append(receiverOpenBank);
        sb.append(", createTime=").append(createTime);
        sb.append(", orderNo=").append(orderNo);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}