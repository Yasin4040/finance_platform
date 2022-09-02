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
     * 科目id
     */
    @TableField(value = "subject_Id")
    private Long subjectId;
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
    @TableField(value = "budget_amount")
    private BigDecimal budgetAmount;
    /**
     * 动因id
     */
    @TableField(value = "motivation_id")
    private Long motivationId;

    /**
     * 动因名称
     */
    @TableField(value = "motivation_name")
    private String motivationName;

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
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}