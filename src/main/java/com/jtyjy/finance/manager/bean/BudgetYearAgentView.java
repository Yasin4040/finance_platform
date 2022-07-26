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
@TableName(value = "budget_year_agent_view")
@Data
public class BudgetYearAgentView implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 动因类型：0:年初（月初），1：追加
     */
    @NotNull(message = "动因类型：0:年初（月初），1：追加不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "agenttype")
    private Integer agenttype;

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
     * 累计追加金额
     */
    @NotNull(message = "累计追加金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearaddmoney")
    private BigDecimal yearaddmoney;

    /**
     * 累计执行金额
     */
    @NotNull(message = "累计执行金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearexecutemoney")
    private BigDecimal yearexecutemoney;

    /**
     * 累计拆进金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "累计拆进金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearlendinmoney")
    private BigDecimal yearlendinmoney;

    /**
     * 累计拆出金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "累计拆出金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearlendoutmoney")
    private BigDecimal yearlendoutmoney;

}
