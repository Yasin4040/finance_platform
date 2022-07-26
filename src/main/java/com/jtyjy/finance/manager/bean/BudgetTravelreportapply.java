package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_travelreportapply")
@Data
public class BudgetTravelreportapply implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工id
     */
    @ApiParam(hidden = true)
    @TableField(value = "empid")
    private String empid;

    /**
     * 工号
     */
    @ApiParam(hidden = true)
    @TableField(value = "empno")
    private String empno;

    /**
     * 员工姓名
     */
    @ApiParam(hidden = true)
    @TableField(value = "empname")
    private String empname;

    /**
     * 部门id
     */
    @ApiParam(hidden = true)
    @TableField(value = "deptid")
    private String deptid;

    /**
     * 部门名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "deptname")
    private String deptname;

    /**
     * 出差报告申请时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "travelstarttime")
    private Date travelstarttime;

    /**
     * 实际总费用
     */
    @ApiParam(hidden = true)
    @TableField(value = "actualtotalmoney")
    private BigDecimal actualtotalmoney;

    /**
     * 超预估费用金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "exceedpredictmoney")
    private BigDecimal exceedpredictmoney;

    /**
     * 超支说明
     */
    @ApiParam(hidden = true)
    @TableField(value = "exceedpredictdescrption")
    private String exceedpredictdescrption;

    /**
     * 流程编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "requestid")
    private String requestid;

    /**
     * 活动日期
     */
    @ApiParam(hidden = true)
    @TableField(value = "activitytime")
    private Date activitytime;

    /**
     * 工作内容及任务完成情况
     */
    @ApiParam(hidden = true)
    @TableField(value = "situation")
    private String situation;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 出差申请id
     */
    @ApiParam(hidden = true)
    @TableField(value = "travelapplyid")
    private String travelapplyid;

    /**
     * 出差申请人
     */
    @ApiParam(hidden = true)
    @TableField(value = "travelapplyor")
    private String travelapplyor;

    /**
     * 出差陪同人
     */
    @ApiParam(hidden = true)
    @TableField(value = "travelcompanion")
    private String travelcompanion;

    /**
     * 出差开始时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "travelstartdate")
    private Date travelstartdate;

    /**
     * 出差结束时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "travelenddate")
    private Date travelenddate;

    /**
     * 出差原因
     */
    @ApiParam(hidden = true)
    @TableField(value = "travelreason")
    private String travelreason;

    /**
     * 流程编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "processnumber")
    private String processnumber;

}
