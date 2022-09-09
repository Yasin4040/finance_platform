package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 员工个体户收票信息主表 维护档案
 * @TableName budget_individual_employee_ticket_receipt
 */
@TableName(value ="budget_individual_employee_ticket_receipt")
@Data
public class IndividualEmployeeTicketReceipt implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 收票单号
     */
    @TableField(value = "ticket_code")
    private String ticketCode;

    /**
     * 员工工号
     */
    @TableField(value = "employee_job_num")
    private Integer employeeJobNum;

    /**
     * 员工档案id
     */
    @TableField(value = "individual_employee_info_id")
    private Long individualEmployeeInfoId;

    /**
     * 个体户名称
     */
    @TableField(value = "individual_name")
    private String individualName;

    /**
     * 发票总金额
     */
    @TableField(value = "invoice_amount")
    private BigDecimal invoiceAmount;

    /**
     * 备注
     */
    @TableField(value = "remarks")
    private String remarks;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @JsonFormat(timezone = "UTC+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    @JsonFormat(timezone = "UTC+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

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
        IndividualEmployeeTicketReceipt other = (IndividualEmployeeTicketReceipt) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTicketCode() == null ? other.getTicketCode() == null : this.getTicketCode().equals(other.getTicketCode()))
            && (this.getEmployeeJobNum() == null ? other.getEmployeeJobNum() == null : this.getEmployeeJobNum().equals(other.getEmployeeJobNum()))
            && (this.getIndividualEmployeeInfoId() == null ? other.getIndividualEmployeeInfoId() == null : this.getIndividualEmployeeInfoId().equals(other.getIndividualEmployeeInfoId()))
            && (this.getIndividualName() == null ? other.getIndividualName() == null : this.getIndividualName().equals(other.getIndividualName()))
            && (this.getInvoiceAmount() == null ? other.getInvoiceAmount() == null : this.getInvoiceAmount().equals(other.getInvoiceAmount()))
            && (this.getRemarks() == null ? other.getRemarks() == null : this.getRemarks().equals(other.getRemarks()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTicketCode() == null) ? 0 : getTicketCode().hashCode());
        result = prime * result + ((getEmployeeJobNum() == null) ? 0 : getEmployeeJobNum().hashCode());
        result = prime * result + ((getIndividualEmployeeInfoId() == null) ? 0 : getIndividualEmployeeInfoId().hashCode());
        result = prime * result + ((getIndividualName() == null) ? 0 : getIndividualName().hashCode());
        result = prime * result + ((getInvoiceAmount() == null) ? 0 : getInvoiceAmount().hashCode());
        result = prime * result + ((getRemarks() == null) ? 0 : getRemarks().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", ticketCode=").append(ticketCode);
        sb.append(", employeeJobNum=").append(employeeJobNum);
        sb.append(", individualEmployeeInfoId=").append(individualEmployeeInfoId);
        sb.append(", individualName=").append(individualName);
        sb.append(", invoiceAmount=").append(invoiceAmount);
        sb.append(", remarks=").append(remarks);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}