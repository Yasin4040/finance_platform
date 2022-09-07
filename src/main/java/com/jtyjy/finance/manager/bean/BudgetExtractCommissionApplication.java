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

//    /**
//     * 员工工号
//     */
//    @TableField(value = "employee_job_num")
//    private Integer employeeJobNum;

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

//    /**
//     * 年额度
//     */
//    @TableField(value = "annual_quota")
//    private Integer annualQuota;

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

    /**
     * -2 作废（退回才可以作废）,-1(退回，仍可以修改。可以作废)  ，0 草稿（撤回）,1已提交,2审核通过
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 提成id主表
     */
    @TableField(value = "extract_sum_id")
    private Long extractSumId;
    /**
     * 报销单id
     */
    @TableField(value = "reimbursement_id")
    private Long reimbursementId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}