package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Admin
 */
@TableName(value = "budget_month_subject_view")
@Data
public class BudgetMonthSubjectView implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 月度id
     */
    @NotNull(message = "月度id不能为空")
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
     * 月度预算金额(可编辑)
     */
    @NotNull(message = "月度预算金额(可编辑)不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthagentmoney")
    private BigDecimal monthagentmoney;

    /**
     * 月度追加金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthaddmoney")
    private BigDecimal monthaddmoney;

    /**
     * 月度执行金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexecutemoney")
    private BigDecimal monthexecutemoney;

    /**
     * 月度拆进金额（目前同科目里面的动因可以拆借）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthlendinmoney")
    private BigDecimal monthlendinmoney;

    /**
     * 月度拆出金额（目前同科目里面的动因可以拆借）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthlendoutmoney")
    private BigDecimal monthlendoutmoney;

}
