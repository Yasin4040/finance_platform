package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 提成支付申请单  主表 
 * @TableName budget_extract_commission_application
 */
@TableName(value ="budget_extract_commission_application")
@Data
public class BudgetExtractCommissionApplication implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工工号
     */
    @TableField(value = "employee_job_num")
    private Integer employeeJobNum;

    /**
     * 部门
     */
    @TableField(value = "department_no")
    private String departmentNo;

    /**
     * 部门名称
     */
    @TableField(value = "department_name")
    private String departmentName;

    /**
     * 支付事由 支付+“届别”+“月份”+“批次”+“提成/坏账”
届别取“届别”字段；月份取“提成期间”中的月份；“提成/坏账”根据“坏账（是/否）”判断，若是则显示“坏账”；否则显示“提成”。
     */
    @TableField(value = "payment_reason")
    private String paymentReason;

    /**
     * 备注
     */
    @TableField(value = "remarks")
    private String remarks;

    /**
     * 年额度
     */
    @TableField(value = "annual_quota")
    private Integer annualQuota;

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

    /**
     * 状态 1 正常  2停用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 提成id主表
     */
    @TableField(value = "extract_sum_id")
    private Long extractSumId;

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
        BudgetExtractCommissionApplication other = (BudgetExtractCommissionApplication) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getEmployeeJobNum() == null ? other.getEmployeeJobNum() == null : this.getEmployeeJobNum().equals(other.getEmployeeJobNum()))
            && (this.getDepartmentNo() == null ? other.getDepartmentNo() == null : this.getDepartmentNo().equals(other.getDepartmentNo()))
            && (this.getDepartmentName() == null ? other.getDepartmentName() == null : this.getDepartmentName().equals(other.getDepartmentName()))
            && (this.getPaymentReason() == null ? other.getPaymentReason() == null : this.getPaymentReason().equals(other.getPaymentReason()))
            && (this.getRemarks() == null ? other.getRemarks() == null : this.getRemarks().equals(other.getRemarks()))
            && (this.getAnnualQuota() == null ? other.getAnnualQuota() == null : this.getAnnualQuota().equals(other.getAnnualQuota()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUdpateTime() == null ? other.getUdpateTime() == null : this.getUdpateTime().equals(other.getUdpateTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getExtractSumId() == null ? other.getExtractSumId() == null : this.getExtractSumId().equals(other.getExtractSumId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getEmployeeJobNum() == null) ? 0 : getEmployeeJobNum().hashCode());
        result = prime * result + ((getDepartmentNo() == null) ? 0 : getDepartmentNo().hashCode());
        result = prime * result + ((getDepartmentName() == null) ? 0 : getDepartmentName().hashCode());
        result = prime * result + ((getPaymentReason() == null) ? 0 : getPaymentReason().hashCode());
        result = prime * result + ((getRemarks() == null) ? 0 : getRemarks().hashCode());
        result = prime * result + ((getAnnualQuota() == null) ? 0 : getAnnualQuota().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUdpateTime() == null) ? 0 : getUdpateTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getExtractSumId() == null) ? 0 : getExtractSumId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", employeeJobNum=").append(employeeJobNum);
        sb.append(", departmentNo=").append(departmentNo);
        sb.append(", departmentName=").append(departmentName);
        sb.append(", paymentReason=").append(paymentReason);
        sb.append(", remarks=").append(remarks);
        sb.append(", annualQuota=").append(annualQuota);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", udpateTime=").append(udpateTime);
        sb.append(", status=").append(status);
        sb.append(", extractSumId=").append(extractSumId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}