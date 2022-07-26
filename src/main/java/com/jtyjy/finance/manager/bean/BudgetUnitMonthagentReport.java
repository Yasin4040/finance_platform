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
@TableName(value = "budget_unit_monthagent_report")
@Data
public class BudgetUnitMonthagentReport implements Serializable {

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
     * 未知参数
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
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitname")
    private String unitname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthname")
    private String monthname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "leafsubjectid")
    private Long leafsubjectid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "leafsubjectname")
    private String leafsubjectname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentid")
    private Long yearagentid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthagentid")
    private Long monthagentid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "agentname")
    private String agentname;

    /**
     * 年度动因年初预算
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentmoney")
    private BigDecimal yearagentmoney;

    /**
     * 年度动因追加（本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearaddmoney")
    private BigDecimal yearaddmoney;

    /**
     * 年度动因追加（累计）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearaddsmoney")
    private BigDecimal yearaddsmoney;

    /**
     * 年度动因拆进（本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearlendinmoney")
    private BigDecimal yearlendinmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearlendinsmoney")
    private BigDecimal yearlendinsmoney;

    /**
     * 年度动因拆出（本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearlendoutmoney")
    private BigDecimal yearlendoutmoney;

    /**
     * 年度动因拆出（累计）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearlendoutsmoney")
    private BigDecimal yearlendoutsmoney;

    /**
     * 年度动因追加次数（本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentaddcount")
    private Integer yearagentaddcount;

    /**
     * 年度动因追加次数（累计）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentaddscount")
    private Integer yearagentaddscount;

    /**
     * 年度动因拆进次数（本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentlendincount")
    private Integer yearagentlendincount;

    /**
     * 年度动因拆进次数（累计）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentlendinscount")
    private Integer yearagentlendinscount;

    /**
     * 年度动因拆出次数（本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentlendoutcount")
    private Integer yearagentlendoutcount;

    /**
     * 年度动因拆出次数（累计）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentlendoutscount")
    private Integer yearagentlendoutscount;

    /**
     * 月度动因月初预算
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthagentmoney")
    private BigDecimal monthagentmoney;

    /**
     * 月度动因追加（本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthaddmoney")
    private BigDecimal monthaddmoney;

    /**
     * 月度动因追加（累计）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthaddsmoney")
    private BigDecimal monthaddsmoney;

    /**
     * 月度动因拆进（本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthlendinmoney")
    private BigDecimal monthlendinmoney;

    /**
     * 月度动因拆进（累计）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthlendinsmoney")
    private BigDecimal monthlendinsmoney;

    /**
     * 月度动因拆出（本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthlendoutmoney")
    private BigDecimal monthlendoutmoney;

    /**
     * 月度动因拆出（累计）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthlendoutsmoney")
    private BigDecimal monthlendoutsmoney;

    /**
     * 月度执行（本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexecutemoney")
    private BigDecimal monthexecutemoney;

    /**
     * 月度执行（累计）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexecutesmoney")
    private BigDecimal monthexecutesmoney;

    /**
     * 预提 本月
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthwithholdingmoney")
    private BigDecimal monthwithholdingmoney;

    /**
     * 预提 累计
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthwithholdingsmoney")
    private BigDecimal monthwithholdingsmoney;

    /**
     * 金蝶 （本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthkingdeemoney")
    private BigDecimal monthkingdeemoney;

    /**
     * 金蝶  累计
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthkingdeesmoney")
    private BigDecimal monthkingdeesmoney;

    /**
     * 月度动因追加次数（本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthagentaddcount")
    private Integer monthagentaddcount;

    /**
     * 月度动因追加次数（累计）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthagentaddscount")
    private Integer monthagentaddscount;

    /**
     * 月度动因执行次数（本月）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthagentexecutecount")
    private Integer monthagentexecutecount;

    /**
     * 月度动因执行次数（累计）
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthagentexecutescount")
    private Integer monthagentexecutescount;

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
