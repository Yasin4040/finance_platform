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
     * 提成批次
     */
    private String extractMonth;

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
    /**
     * 类型。1：提成支付申请单 3：延期支付申请单
     */
    private Integer taskType;

    /**
     * 延期支付申请单批次
     */
    private Integer batch;

    /**
     * 延期支付申请单号
     */
    private String delayExtractCode;

    /**
     * 员工个体户id列表
     */
    private String personalityIds;

    /**
     * （延期任务时需要使用，留存记录）是否需要做账
     */
    private Boolean isShouldAccount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}