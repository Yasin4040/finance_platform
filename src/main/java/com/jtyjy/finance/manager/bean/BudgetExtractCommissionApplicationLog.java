package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 申请单 oa 审批日志记录
 * @TableName budget_extract_commission_application_log
 */
@TableName(value ="budget_extract_commission_application_log")
@Data
public class BudgetExtractCommissionApplicationLog implements Serializable {
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
     * 操作节点    枚举
     */
    @TableField(value = "node")
    private Integer node;

    /**
     * 操作状态  1 同意 2拒绝
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 备注 操作信息
     */
    @TableField(value = "remarks")
    private String remarks;

    /**
     *  操作时间
     */
    @TableField(value = "financial_director_node_operate_time")
    private Date financialDirectorNodeOperateTime;

    /**
     * 操作人
     */
    @TableField(value = "create_by")
    private String createBy;

    /**
     * 操作时间
     */
    @TableField(value = "create_time")
    private Date createTime;

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
        BudgetExtractCommissionApplicationLog other = (BudgetExtractCommissionApplicationLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getApplicationId() == null ? other.getApplicationId() == null : this.getApplicationId().equals(other.getApplicationId()))
            && (this.getNode() == null ? other.getNode() == null : this.getNode().equals(other.getNode()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getRemarks() == null ? other.getRemarks() == null : this.getRemarks().equals(other.getRemarks()))
            && (this.getFinancialDirectorNodeOperateTime() == null ? other.getFinancialDirectorNodeOperateTime() == null : this.getFinancialDirectorNodeOperateTime().equals(other.getFinancialDirectorNodeOperateTime()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getApplicationId() == null) ? 0 : getApplicationId().hashCode());
        result = prime * result + ((getNode() == null) ? 0 : getNode().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getRemarks() == null) ? 0 : getRemarks().hashCode());
        result = prime * result + ((getFinancialDirectorNodeOperateTime() == null) ? 0 : getFinancialDirectorNodeOperateTime().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
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
        sb.append(", node=").append(node);
        sb.append(", status=").append(status);
        sb.append(", remarks=").append(remarks);
        sb.append(", financialDirectorNodeOperateTime=").append(financialDirectorNodeOperateTime);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}