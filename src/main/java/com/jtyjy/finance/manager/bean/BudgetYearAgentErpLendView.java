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
@TableName(value = "budget_year_agent_erp_lend_view")
@Data
public class BudgetYearAgentErpLendView implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 追加状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过
     */
    @ApiParam(hidden = true)
    @TableField(value = "requeststatus")
    private Integer requeststatus;

    /**
     * 届别id
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "baseunitid")
    private Long baseunitid;

    /**
     * 所有上级id
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitpids")
    private String unitpids;

    /**
     * 上级id
     */
    @NotNull(message = "上级id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unitpid")
    private Long unitpid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

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
     * 动因名称
     */
    @NotBlank(message = "动因名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "outyearagentname")
    private String outyearagentname;

    /**
     * 年度预算动因id(拆出动因，已存在的)
     */
    @ApiParam(hidden = true)
    @TableField(value = "outyearagentid")
    private Long outyearagentid;

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
     * 拆进动因，流程完成后新生成的
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
     * 追加金额
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
