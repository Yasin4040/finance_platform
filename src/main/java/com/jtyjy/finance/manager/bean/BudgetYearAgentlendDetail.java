package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_year_agentlend_detail")
@Data
public class BudgetYearAgentlendDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 拆借单号
     */
    @TableField(value = "year_agent_lend_id")
    private Long yearAgentLendId;


    /**
     * 预算单位id
     */
    @TableField(value = "inunitid")
    private Long inunitid;

    /**
     * 未知参数
     */
    @TableField(value = "outunitid")
    private Long outunitid;

    /**
     * 拆出科目名
     */
    @TableField(value = "outsubjectname")
    private String outsubjectname;

    /**
     * 拆出科目id
     */
    @TableField(value = "outsubjectid")
    private Long outsubjectid;

    /**
     * 拆进科目名
     */
    @TableField(value = "insubjectname")
    private String insubjectname;

    /**
     * 拆进科目id
     */
    @TableField(value = "insubjectid")
    private Long insubjectid;

    /**
     * 拆出动因id
     */
    @TableField(value = "outyearagentid")
    private Long outyearagentid;

    /**
     * 拆出动因名称
     */
    @TableField(value = "outname")
    private String outname;

    /**
     * 拆进动因id
     */
    @ApiParam(hidden = true)
    @TableField(value = "inyearagentid")
    private Long inyearagentid;

    /**
     * 拆进动因名称
     */
    @TableField(value = "inname")
    private String inname;

    /**
     * 拆进金额
     */
    @TableField(value = "total")
    private BigDecimal total;

    /**
     * 拆借原因
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 拆借状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过
     */
    @TableField(value = "requeststatus")
    private Integer requeststatus;


    /**
     * 是否免罚 0 否 1是
     */
    @TableField(value = "is_exempt_fine")
    private Boolean isExemptFine;

    /**
     * 免罚原因
     */
    @TableField(value = "exempt_fine_reason")
    private String exemptFineReason;

    @TableField(value = "exempt_fine_result")
    private String exemptFineResult;

    @TableField(value = "fine_reason_remark")
    private String fineReasonRemark;


    /**
     * 拆借前动因年度预算金额（年初预算）
     */
    @TableField(value = "outagentmoney")
    private BigDecimal outagentmoney;

    /**
     * 拆借前动因累计追加金额
     */
    @TableField(value = "outagentaddmoney")
    private BigDecimal outagentaddmoney;

    /**
     * 拆借前拆出金额（同科目里面的动因可以拆借）
     */
    @TableField(value = "outagentlendoutmoney")
    private BigDecimal outagentlendoutmoney;

    /**
     * 拆借前拆进金额（同科目里面的动因可以拆借）
     */
    @TableField(value = "outagentlendinmoney")
    private BigDecimal outagentlendinmoney;

    /**
     * 拆借前动因累计执行金额
     */
    @TableField(value = "outagentexcutemoney")
    private BigDecimal outagentexcutemoney;

    /**
     * 拆借前动因年度预算金额（年初预算
     */
    @TableField(value = "inagentmoney")
    private BigDecimal inagentmoney;

    /**
     * 拆借前动因累计追加金额
     */
    @TableField(value = "inagentaddmoney")
    private BigDecimal inagentaddmoney;

    /**
     * 拆借前拆出金额（同科目里面的动因可以拆借）
     */
    @TableField(value = "inagentlendoutmoney")
    private BigDecimal inagentlendoutmoney;

    /**
     * 拆借前拆进金额（同科目里面的动因可以拆借）
     */
    @TableField(value = "inagentlendinmoney")
    private BigDecimal inagentlendinmoney;

    /**
     * 拆借前动因累计执行金额
     */
    @TableField(value = "inagentexcutemoney")
    private BigDecimal inagentexcutemoney;

    /**
     * 审核时间
     */
    @TableField(value = "audittime")
    private Date audittime;

}
