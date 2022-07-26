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
@TableName(value = "budget_unit_reimbursement_report")
@Data
public class BudgetUnitReimbursementReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 届别名
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearname")
    private String yearname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 部门名
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitname")
    private String unitname;

    /**
     * 月
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 批次号
     */
    @ApiParam(hidden = true)
    @TableField(value = "patch")
    private Integer patch;

    /**
     * 报销单号
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimcode")
    private String reimcode;

    /**
     * 报销单提交时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "submittime")
    private Date submittime;

    /**
     * 票面审核接收时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "parverifyreceivetime")
    private Date parverifyreceivetime;

    /**
     * 票面审核时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "parverifyverifytime")
    private Date parverifyverifytime;

    /**
     * 预算审核时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "budgetverifytime")
    private Date budgetverifytime;

    /**
     * 分单扫描时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "splitordertime")
    private Date splitordertime;

    /**
     * 分单主开票单位
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitname")
    private String bunitname;

    /**
     * 分单确认完成时间 (做账)
     */
    @ApiParam(hidden = true)
    @TableField(value = "splitorderensuretime")
    private Date splitorderensuretime;

    /**
     * 出纳接收时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "cashierpaymentreceivetime")
    private Date cashierpaymentreceivetime;

    /**
     * 出纳付款时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "cashierpaymenttime")
    private Date cashierpaymenttime;

    /**
     * 票面审核接收 - 报销提交（工作时长【小时】）
     */
    @ApiParam(hidden = true)
    @TableField(value = "hour1")
    private BigDecimal hour1;

    /**
     * 票面审核时间 - 票面审核接收 （工作时长【小时】）
     */
    @ApiParam(hidden = true)
    @TableField(value = "hour2")
    private BigDecimal hour2;

    /**
     * 预算审核时间 - 票面审核时间 （工作时长【小时】）
     */
    @ApiParam(hidden = true)
    @TableField(value = "hour3")
    private BigDecimal hour3;

    /**
     * 分单扫描时间 - 预算审核时间 （工作时长【小时】）
     */
    @ApiParam(hidden = true)
    @TableField(value = "hour4")
    private BigDecimal hour4;

    /**
     * 分单确认时间 - 分单扫描时间 （工作时长【小时】）
     */
    @ApiParam(hidden = true)
    @TableField(value = "hour5")
    private BigDecimal hour5;

    /**
     * 出纳接收时间 - 分单确认时间 （工作时长【小时】）
     */
    @ApiParam(hidden = true)
    @TableField(value = "hour6")
    private BigDecimal hour6;

    /**
     * 出纳付款时间 - 单据接收时间 （工作时长【小时】）
     */
    @ApiParam(hidden = true)
    @TableField(value = "hour7")
    private BigDecimal hour7;

    /**
     * 退回纸质次数
     */
    @ApiParam(hidden = true)
    @TableField(value = "backpapercount")
    private Long backpapercount;

    /**
     * 全部退回次数
     */
    @ApiParam(hidden = true)
    @TableField(value = "allbackcount")
    private Long allbackcount;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

}
