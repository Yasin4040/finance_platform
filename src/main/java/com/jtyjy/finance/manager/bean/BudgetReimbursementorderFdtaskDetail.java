package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_reimbursementorder_fdtask_detail")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetReimbursementorderFdtaskDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键", hidden = false, required = false)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务id
     */
    @ApiModelProperty(value = "任务id", hidden = false, required = false)
    @TableField(value = "taskid")
    private Long taskid;

    /**
     * 报销单号
     */
    @ApiModelProperty(value = "报销单号", hidden = false, required = false)
    @TableField(value = "reimcode")
    private String reimcode;

    /**
     * 报销单id
     */
    @ApiModelProperty(value = "报销单id", hidden = false, required = false)
    @TableField(value = "reimbursementid")
    private Long reimbursementid;

    /**
     * 分单人工号
     */
    @ApiModelProperty(value = "分单人工号", hidden = false, required = false)
    @TableField(value = "fder")
    private String fder;

    /**
     * 分单人姓名
     */
    @ApiModelProperty(value = " 分单人姓名", hidden = false, required = false)
    @TableField(value = "fdername")
    private String fdername;

    /**
     * 分单时间
     */
    @ApiModelProperty(value = "分单时间", hidden = false, required = false)
    @TableField(value = "fdtime")
    private Date fdtime;

    /**
     * 分单 主开票单位id
     */
    @ApiModelProperty(value = "分单 主开票单位id", hidden = false, required = false)
    @TableField(value = "bunitid")
    private Long bunitid;

    /**
     * 分单 主开票单位名称
     */
    @ApiModelProperty(value = "分单 主开票单位名称", hidden = false, required = false)
    @TableField(value = "bunitname")
    private String bunitname;

    /**
     * 分单 计划开票单位id
     */
    @ApiModelProperty(value = "分单 计划开票单位id", hidden = false, required = false)
    @TableField(value = "planbunitid")
    private Long planbunitid;

    /**
     * 分单 计划开票单位名称
     */
    @ApiModelProperty(value = "分单 计划开票单位名称", hidden = false, required = false)
    @TableField(value = "planbunitname")
    private String planbunitname;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", hidden = false, required = false)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 分配任务时间
     */
    @ApiModelProperty(value = "分配任务时间", hidden = false, required = false)
    @TableField(value = "tasktime")
    private Date tasktime;

    /**
     * 计划做账人（多个人的工号）
     */
    @ApiModelProperty(value = "计划做账人（多个人的工号）", hidden = false, required = false)
    @TableField(value = "planaccounters")
    private String planaccounters;

    /**
     * 计划做账人（多个人的姓名）
     */
    @ApiModelProperty(value = "计划做账人（多个人的姓名）", hidden = false, required = false)
    @TableField(value = "planaccounternames")
    private String planaccounternames;

    /**
     * 接收人(工号)
     */
    @ApiModelProperty(value = "接收人(工号)", hidden = false, required = false)
    @TableField(value = "receiver")
    private String receiver;

    /**
     * 接收人（姓名）
     */
    @ApiModelProperty(value = "接收人（姓名）", hidden = false, required = false)
    @TableField(value = "receivername")
    private String receivername;

    /**
     * 第一次扫描接收时间
     */
    @ApiModelProperty(value = " 第一次扫描接收时间", hidden = false, required = false)
    @TableField(value = "receivetime")
    private Date receivetime;

    /**
     * 做账时间
     */
    @ApiModelProperty(value = "做账时间", hidden = false, required = false)
    @TableField(value = "accounttime")
    private Date accounttime;

    /**
     * 做账状态 0:还未做账；1：已做账
     */
    @ApiModelProperty(value = " 做账状态 0:还未做账；1：已做账", hidden = false, required = false)
    @TableField(value = "accountstatus")
    private Boolean accountstatus;

    /**
     * 实际做账人（工号，登录账号）
     */
    @ApiModelProperty(value = "实际做账人（工号，登录账号）", hidden = false, required = false)
    @TableField(value = "accounter")
    private String accounter;

    /**
     * 实际做账人（姓名，显示名）
     */
    @ApiModelProperty(value = "实际做账人（姓名，显示名）", hidden = false, required = false)
    @TableField(value = "accountername")
    private String accountername;

    /**
     * 根据分单任务创建分单详情
     *
     * @param task
     * @return
     */
    public static BudgetReimbursementorderFdtaskDetail createFromTask(BudgetReimbursementorderFdtask task) {
        BudgetReimbursementorderFdtaskDetail detail = new BudgetReimbursementorderFdtaskDetail();
        detail.setReimcode(task.getReimcode());
        detail.setReimbursementid(task.getReimbursementid());
        detail.setFder(task.getFder());
        detail.setFdername(task.getFdername());
        detail.setFdtime(task.getFdtime());
        detail.setBunitid(task.getBunitid());
        detail.setBunitname(task.getBunitname());
        detail.setCreatetime(task.getCreatetime());
        return detail;
    }

}
