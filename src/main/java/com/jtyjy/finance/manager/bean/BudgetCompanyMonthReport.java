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
@TableName(value = "budget_company_month_report")
@Data
public class BudgetCompanyMonthReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 届别id
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 届别
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearname")
    private String yearname;

    /**
     * 月份id
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 月份
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthname")
    private String monthname;

    /**
     * 预算科目id
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 预算科目
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectname")
    private String subjectname;

    /**
     * 年初预算（自身的）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearmoney")
    private BigDecimal yearmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearaddmoney")
    private BigDecimal yearaddmoney;

    /**
     * 截止到本月的年度追加
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearaddsmoney")
    private BigDecimal yearaddsmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearlendinmoney")
    private BigDecimal yearlendinmoney;

    /**
     * 截止到本月的年度拆进
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearlendinsmoney")
    private BigDecimal yearlendinsmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearlendoutmoney")
    private BigDecimal yearlendoutmoney;

    /**
     * 截止到本月的年度拆出
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearlendoutsmoney")
    private BigDecimal yearlendoutsmoney;

    /**
     * 截止到本月的年度执行
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearexecutesmoney")
    private BigDecimal yearexecutesmoney;

    /**
     * 年度预算收入（销售收入的年度预算）占比
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearrevenueshare")
    private BigDecimal yearrevenueshare;

    /**
     * 月初预算
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthmoney")
    private BigDecimal monthmoney;

    /**
     * 本月月度追加
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthaddmoney")
    private BigDecimal monthaddmoney;

    /**
     * 本月追加次数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthaddcount")
    private Long monthaddcount;

    /**
     * 本月累计追加
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthaddsmoney")
    private BigDecimal monthaddsmoney;

    /**
     * 本月累计追加次数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthaddscount")
    private Long monthaddscount;

    /**
     * 预提金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "prepaidmoney")
    private BigDecimal prepaidmoney;

    /**
     * 本月执行数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexecutemoney")
    private BigDecimal monthexecutemoney;

    /**
     * 本月执行次数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexecutecount")
    private Long monthexecutecount;

    /**
     * 本月执行收入（销售收入的年度预算）占比
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexecuterevenueshare")
    private BigDecimal monthexecuterevenueshare;

    /**
     * 本月累计执行
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexecutesmoney")
    private BigDecimal monthexecutesmoney;

    /**
     * 预提
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthwithholdingmoney")
    private BigDecimal monthwithholdingmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthwithholdingsmoney")
    private BigDecimal monthwithholdingsmoney;

    /**
     * 金蝶
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthkingdeemoney")
    private BigDecimal monthkingdeemoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthkingdeesmoney")
    private BigDecimal monthkingdeesmoney;

    /**
     * 本月累计执行次数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexecutescount")
    private Long monthexecutescount;

    /**
     * 本月累计执行收入（销售收入的年度预算）占比
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexecutesrevenueshare")
    private BigDecimal monthexecutesrevenueshare;

    /**
     * 年初预算（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearmoney")
    private BigDecimal Yearmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearaddmoney")
    private BigDecimal Yearaddmoney;

    /**
     * 截止到本月的年度追加（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearaddsmoney")
    private BigDecimal Yearaddsmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearlendinmoney")
    private BigDecimal Yearlendinmoney;

    /**
     * 截止到本月的年度拆进（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearlendinsmoney")
    private BigDecimal Yearlendinsmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearlendoutmoney")
    private BigDecimal Yearlendoutmoney;

    /**
     * 截止到本月的年度拆出（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearlendoutsmoney")
    private BigDecimal Yearlendoutsmoney;

    /**
     * 截止到本月的年度执行（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearexecutesmoney")
    private BigDecimal Yearexecutesmoney;

    /**
     * 月初预算（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthmoney")
    private BigDecimal Monthmoney;

    /**
     * 本月月度追加（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthaddmoney")
    private BigDecimal Monthaddmoney;

    /**
     * 本月追加次数（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthaddcount")
    private Long Monthaddcount;

    /**
     * 本月累计追加（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthaddsmoney")
    private BigDecimal Monthaddsmoney;

    /**
     * 本月累计追加次数（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthaddscount")
    private Long Monthaddscount;

    /**
     * 预提金额（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_prepaidmoney")
    private BigDecimal Prepaidmoney;

    /**
     * 本月执行数（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthexecutemoney")
    private BigDecimal Monthexecutemoney;

    /**
     * 本月执行次数（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthexecutecount")
    private Long Monthexecutecount;

    /**
     * 本月执行收入（销售收入的年度预算）占比（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthexecuterevenueshare")
    private BigDecimal Monthexecuterevenueshare;

    /**
     * 本月累计执行（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthexecutesmoney")
    private BigDecimal Monthexecutesmoney;

    /**
     * 预提
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthwithholdingmoney")
    private BigDecimal Monthwithholdingmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthwithholdingsmoney")
    private BigDecimal Monthwithholdingsmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthkingdeesmoney")
    private BigDecimal Monthkingdeesmoney;

    /**
     * 金蝶
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthkingdeemoney")
    private BigDecimal Monthkingdeemoney;

    /**
     * 本月累计执行次数（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthexecutescount")
    private Long Monthexecutescount;

    /**
     * 本月累计执行收入（销售收入的年度预算）占比（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_monthexecutesrevenueshare")
    private BigDecimal Monthexecutesrevenueshare;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 更新时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "level")
    private Integer level;

}
