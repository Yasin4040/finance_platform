package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Admin
 */
@TableName(value = "budget_year_agent_lend_view")
@Data
public class BudgetYearAgentLendView implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 拆借状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过
     */
    @ApiParam(hidden = true)
    @TableField(value = "requeststatus")
    private Integer requeststatus;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "inbaseunitid")
    private Long inbaseunitid;

    /**
     * 所有上级id
     */
    @ApiParam(hidden = true)
    @TableField(value = "inunitpids")
    private String inunitpids;

    /**
     * 上级id
     */
    @NotNull(message = "上级id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "inunitpid")
    private Long inunitpid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "inunitid")
    private Long inunitid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "outbaseunitid")
    private Long outbaseunitid;

    /**
     * 所有上级id
     */
    @ApiParam(hidden = true)
    @TableField(value = "outunitpids")
    private String outunitpids;

    /**
     * 上级id
     */
    @NotNull(message = "上级id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "outunitpid")
    private Long outunitpid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "outunitid")
    private Long outunitid;

    /**
     * 基础科目id
     */
    @ApiParam(hidden = true)
    @TableField(value = "outbasesubjectid")
    private Long outbasesubjectid;

    /**
     * 所有上级id,用"-"隔开
     */
    @ApiParam(hidden = true)
    @TableField(value = "outsubjectpids")
    private String outsubjectpids;

    /**
     * 上级id
     */
    @ApiParam(hidden = true)
    @TableField(value = "outsubjectpid")
    private Long outsubjectpid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "outsubjectid")
    private Long outsubjectid;

    /**
     * 拆出动因id
     */
    @ApiParam(hidden = true)
    @TableField(value = "outyearagentid")
    private Long outyearagentid;

    /**
     * 动因名称
     */
    @NotBlank(message = "动因名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "outyearagentname")
    private String outyearagentname;

    /**
     * 基础科目id
     */
    @ApiParam(hidden = true)
    @TableField(value = "inbasesubjectid")
    private Long inbasesubjectid;

    /**
     * 所有上级id,用"-"隔开
     */
    @ApiParam(hidden = true)
    @TableField(value = "insubjectpids")
    private String insubjectpids;

    /**
     * 上级id
     */
    @ApiParam(hidden = true)
    @TableField(value = "insubjectpid")
    private Long insubjectpid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "insubjectid")
    private Long insubjectid;

    /**
     * 拆进动因id
     */
    @ApiParam(hidden = true)
    @TableField(value = "inyearagentid")
    private Long inyearagentid;

    /**
     * 动因名称
     */
    @NotBlank(message = "动因名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "inyearagentname")
    private String inyearagentname;

    /**
     * 拆进金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "lendmoney")
    private BigDecimal lendmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "audittime")
    private String audittime;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private String createtime;

}
