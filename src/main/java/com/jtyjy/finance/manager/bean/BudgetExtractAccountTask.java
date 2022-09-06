package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 提成会计做账
 * @TableName budget_extract_account_task
 */
@TableName(value ="budget_extract_account_task")
@Data
public class BudgetExtractAccountTask implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提成单号
     */
    private String extractCode;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 计划做账会计
     */
    private String planAccountantEmpNos;

    /**
     * 发放单位
     */
    private Long billingUnitId;

    /**
     * 实际做账人(工号)
     */
    private String accountantEmpNo;

    /**
     * 做账完成时间
     */
    private Date accountantTime;

    /**
     * 做账状态。0：未完成 1：已完成
     */
    private Integer accountantStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

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
        BudgetExtractAccountTask other = (BudgetExtractAccountTask) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getExtractCode() == null ? other.getExtractCode() == null : this.getExtractCode().equals(other.getExtractCode()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getPlanAccountantEmpNos() == null ? other.getPlanAccountantEmpNos() == null : this.getPlanAccountantEmpNos().equals(other.getPlanAccountantEmpNos()))
            && (this.getBillingUnitId() == null ? other.getBillingUnitId() == null : this.getBillingUnitId().equals(other.getBillingUnitId()))
            && (this.getAccountantEmpNo() == null ? other.getAccountantEmpNo() == null : this.getAccountantEmpNo().equals(other.getAccountantEmpNo()))
            && (this.getAccountantTime() == null ? other.getAccountantTime() == null : this.getAccountantTime().equals(other.getAccountantTime()))
            && (this.getAccountantStatus() == null ? other.getAccountantStatus() == null : this.getAccountantStatus().equals(other.getAccountantStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getExtractCode() == null) ? 0 : getExtractCode().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getPlanAccountantEmpNos() == null) ? 0 : getPlanAccountantEmpNos().hashCode());
        result = prime * result + ((getBillingUnitId() == null) ? 0 : getBillingUnitId().hashCode());
        result = prime * result + ((getAccountantEmpNo() == null) ? 0 : getAccountantEmpNo().hashCode());
        result = prime * result + ((getAccountantTime() == null) ? 0 : getAccountantTime().hashCode());
        result = prime * result + ((getAccountantStatus() == null) ? 0 : getAccountantStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", extractCode=").append(extractCode);
        sb.append(", createTime=").append(createTime);
        sb.append(", planAccountantEmpNos=").append(planAccountantEmpNos);
        sb.append(", billingUnitId=").append(billingUnitId);
        sb.append(", accountantEmpNo=").append(accountantEmpNo);
        sb.append(", accountantTime=").append(accountantTime);
        sb.append(", accountantStatus=").append(accountantStatus);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}