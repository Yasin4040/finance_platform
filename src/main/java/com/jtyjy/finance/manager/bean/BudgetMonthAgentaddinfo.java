package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_month_agentaddinfo")
@Data
public class BudgetMonthAgentaddinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @NotNull(message = "主键Id不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 单据号
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthaddcode")
    private String monthaddcode;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 预算单位id
     */
    @NotNull(message = "预算单位id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 月份id
     */
    @NotNull(message = "月份id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

//    /**
//     * 预算科目id
//     */
//    @NotNull(message = "预算科目id不能为空")
//    @ApiParam(hidden = true)
//    @TableField(value = "subjectid")
//    private Long subjectid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "total")
    private BigDecimal total;

    /**
     * 追加前动因月度预算金额（年初预算）
     */
    @NotNull(message = "追加前动因月度预算金额（年初预算）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentmoney")
    private BigDecimal yearagentmoney;

    /**
     * 追加前动因累计追加金额
     */
    @NotNull(message = "追加前动因累计追加金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentaddmoney")
    private BigDecimal yearagentaddmoney;

    /**
     * 追加前拆进金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "追加前拆进金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentlendinmoney")
    private BigDecimal yearagentlendinmoney;

    /**
     * 追加前拆出金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "追加前拆出金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentlendoutmoney")
    private BigDecimal yearagentlendoutmoney;

    /**
     * 追加前动因累计执行金额
     */
    @NotNull(message = "追加前动因累计执行金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentexcutemoney")
    private BigDecimal yearagentexcutemoney;

    /**
     * 追加前动因年度预算金额（月初预算）
     */
    @NotNull(message = "追加前动因年度预算金额（月初预算）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "agentmoney")
    private BigDecimal agentmoney;

    /**
     * 追加前动因累计追加金额
     */
    @NotNull(message = "追加前动因累计追加金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "agentaddmoney")
    private BigDecimal agentaddmoney;

    /**
     * 追加前拆出金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "追加前拆出金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "agentlendoutmoney")
    private BigDecimal agentlendoutmoney;

    /**
     * 追加前拆进金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "追加前拆进金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "agentlendinmoney")
    private BigDecimal agentlendinmoney;

    /**
     * 追加前动因累计执行金额
     */
    @NotNull(message = "追加前动因累计执行金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "agentexcutemoney")
    private BigDecimal agentexcutemoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * oa流程创建人id(oa系统id)
     */
    @ApiParam(hidden = true)
    @TableField(value = "oacreatorid")
    private String oacreatorid;

    /**
     * 流程id
     */
    @ApiParam(hidden = true)
    @TableField(value = "requestid")
    private String requestid;

    /**
     * //追加状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过
     */
    @ApiParam(hidden = true)
    @TableField(value = "requeststatus")
    private Integer requeststatus;

    /**
     * 申请人id
     */
    @NotBlank(message = "申请人id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "creatorid")
    private String creatorid;

    /**
     * 申请人名字
     */
    @ApiParam(hidden = true)
    @TableField(value = "creatorname")
    private String creatorname;

    /**
     * 审核时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "audittime")
    private Date audittime;

    /**
     * 处理状态  true :已处理，false：未处理（默认）
     */
    @NotNull(message = "处理状态  true :已处理，false：未处理（默认）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "handleflag")
    private Boolean handleflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "fileurl")
    private String fileurl;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "fileoriginname")
    private String fileoriginname;

    /**
     * oa密码。上传附件时需使用
     */
    @ApiParam(hidden = true)
    @TableField(value = "oapassword")
    private String oapassword;

    // --------------------------------------------------

    /**
     * 届别名称
     */
    @TableField(exist = false)
    private String period;

    /**
     * 预算单位名称
     */
    @TableField(exist = false)
    private String unitName;

}
