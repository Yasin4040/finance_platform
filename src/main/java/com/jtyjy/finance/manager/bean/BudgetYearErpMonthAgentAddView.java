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
@TableName(value = "budget_year_erp_month_agent_add_view")
@Data
public class BudgetYearErpMonthAgentAddView implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 当前月id
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

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
    @TableField(value = "basesubjectid")
    private Long basesubjectid;

    /**
     * 所有上级id,用"-"隔开
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectpids")
    private String subjectpids;

    /**
     * 上级id
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectpid")
    private Long subjectpid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentid")
    private Long yearagentid;

    /**
     * 本届总金额(12个月)
     */
    @NotNull(message = "本届总金额(12个月)不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentmoney")
    private BigDecimal yearagentmoney;

    /**
     * 月度预算金额(可编辑)
     */
    @NotNull(message = "月度预算金额(可编辑)不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthagentmoney")
    private BigDecimal monthagentmoney;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthagentid")
    private Long monthagentid;

    /**
     * 动因名称
     */
    @NotBlank(message = "动因名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentname")
    private String yearagentname;

    /**
     * 动因名称
     */
    @NotBlank(message = "动因名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthagentname")
    private String monthagentname;

    /**
     * 当前月金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "addmoney")
    private BigDecimal addmoney;

    /**
     * 追加状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过
     */
    @ApiParam(hidden = true)
    @TableField(value = "requeststatus")
    private Integer requeststatus;

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
