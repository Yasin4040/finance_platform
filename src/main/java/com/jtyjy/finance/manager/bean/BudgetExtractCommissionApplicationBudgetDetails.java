package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 提成支付申请单  附表 预算明细
 * @TableName budget_extract_commission_application_budget_details
 */
@TableName(value ="budget_extract_commission_application_budget_details")
@Data
public class BudgetExtractCommissionApplicationBudgetDetails implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申请单id
     */
    @TableField(value = "application_id")
    private Long applicationId;

    /**
     * 科目编码
     */
    @TableField(value = "subject_code")
    private String subjectCode;

    /**
     * 科目名称
     */
    @TableField(value = "subject_name")
    private String subjectName;

    /**
     * 金额 根据提成类型+届别取提成明细所在行的“申请提成”
     */
    @TableField(value = "deduction_amount")
    private BigDecimal deductionAmount;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "udpate_time")
    private Date udpateTime;

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
        BudgetExtractCommissionApplicationBudgetDetails other = (BudgetExtractCommissionApplicationBudgetDetails) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getApplicationId() == null ? other.getApplicationId() == null : this.getApplicationId().equals(other.getApplicationId()))
            && (this.getSubjectCode() == null ? other.getSubjectCode() == null : this.getSubjectCode().equals(other.getSubjectCode()))
            && (this.getSubjectName() == null ? other.getSubjectName() == null : this.getSubjectName().equals(other.getSubjectName()))
            && (this.getDeductionAmount() == null ? other.getDeductionAmount() == null : this.getDeductionAmount().equals(other.getDeductionAmount()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUdpateTime() == null ? other.getUdpateTime() == null : this.getUdpateTime().equals(other.getUdpateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getApplicationId() == null) ? 0 : getApplicationId().hashCode());
        result = prime * result + ((getSubjectCode() == null) ? 0 : getSubjectCode().hashCode());
        result = prime * result + ((getSubjectName() == null) ? 0 : getSubjectName().hashCode());
        result = prime * result + ((getDeductionAmount() == null) ? 0 : getDeductionAmount().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUdpateTime() == null) ? 0 : getUdpateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", applicationId=").append(applicationId);
        sb.append(", subjectCode=").append(subjectCode);
        sb.append(", subjectName=").append(subjectName);
        sb.append(", deductionAmount=").append(deductionAmount);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", udpateTime=").append(udpateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}