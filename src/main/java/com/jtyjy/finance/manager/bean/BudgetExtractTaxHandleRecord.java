package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 提成税务处理状态表
 * @TableName budget_extract_tax_handle_record
 */
@TableName(value ="budget_extract_tax_handle_record")
@Data
public class BudgetExtractTaxHandleRecord implements Serializable {
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
     * 是否计算完成
     */
    private Boolean isCalComplete;

    /**
     * 是否设置超额完成
     */
    private Boolean isSetExcessComplete;

    /**
     * 员工个体户是否设置完成
     */
    private Boolean isPersonalityComplete;

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
        BudgetExtractTaxHandleRecord other = (BudgetExtractTaxHandleRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getExtractMonth() == null ? other.getExtractMonth() == null : this.getExtractMonth().equals(other.getExtractMonth()))
            && (this.getIsCalComplete() == null ? other.getIsCalComplete() == null : this.getIsCalComplete().equals(other.getIsCalComplete()))
            && (this.getIsSetExcessComplete() == null ? other.getIsSetExcessComplete() == null : this.getIsSetExcessComplete().equals(other.getIsSetExcessComplete()))
            && (this.getIsPersonalityComplete() == null ? other.getIsPersonalityComplete() == null : this.getIsPersonalityComplete().equals(other.getIsPersonalityComplete()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getExtractMonth() == null) ? 0 : getExtractMonth().hashCode());
        result = prime * result + ((getIsCalComplete() == null) ? 0 : getIsCalComplete().hashCode());
        result = prime * result + ((getIsSetExcessComplete() == null) ? 0 : getIsSetExcessComplete().hashCode());
        result = prime * result + ((getIsPersonalityComplete() == null) ? 0 : getIsPersonalityComplete().hashCode());
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
        sb.append(", isCalComplete=").append(isCalComplete);
        sb.append(", isSetExcessComplete=").append(isSetExcessComplete);
        sb.append(", isPersonalityComplete=").append(isPersonalityComplete);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}