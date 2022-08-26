package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 员工个体户收票信息维护档案
 * @TableName budget_individual_employee_ticket_receipt_info
 */
@TableName(value ="budget_individual_employee_ticket_receipt_info")
@Data
public class IndividualEmployeeTicketReceiptInfo implements Serializable {
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
     * 员工档案id
     */
    @TableField(value = "individual_employee_info_id")
    private Integer individualEmployeeInfoId;

    /**
     * 个体户名称
     */
    @TableField(value = "individual_name")
    private String individualName;

    /**
     * 年份
     */
    @TableField(value = "year")
    private Integer year;

    /**
     * 月份
     */
    @TableField(value = "month")
    private Integer month;

    /**
     * 发票金额
     */
    @TableField(value = "invoice_amount")
    private Integer invoiceAmount;

    /**
     * 备注
     */
    @TableField(value = "remarks")
    private Integer remarks;

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
}