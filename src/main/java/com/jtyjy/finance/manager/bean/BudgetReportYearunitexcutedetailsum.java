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
@TableName(value = "budget_report_yearunitexcutedetailsum")
@Data
public class BudgetReportYearunitexcutedetailsum implements Serializable {

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
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectname")
    private String subjectname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjecttrrname")
    private String subjecttrrname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "assistflag")
    private Boolean assistflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "upsumflag")
    private Boolean upsumflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "costaddflag")
    private Boolean costaddflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "costsplitflag")
    private Boolean costsplitflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "costlendflag")
    private Boolean costlendflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "jointproductflag")
    private Boolean jointproductflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearplantype")
    private Integer yearplantype;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "formulaflag")
    private Boolean formulaflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "formula")
    private String formula;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "procategoryid")
    private String procategoryid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "formulaorderno")
    private Integer formulaorderno;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "level")
    private Integer level;

    /**
     * 年度预算含追加
     */
    @ApiParam(hidden = true)
    @TableField(value = "yeartotalmoney")
    private BigDecimal yeartotalmoney;

    /**
     * 年度执行
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearexcutemoney")
    private BigDecimal yearexcutemoney;

    /**
     * 年度余额
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearbalancemoney")
    private BigDecimal yearbalancemoney;

    /**
     * 6月预算含追加
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthtotalmoney6")
    private BigDecimal monthtotalmoney6;

    /**
     * 6月执行数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexcutemoney6")
    private BigDecimal monthexcutemoney6;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthtotalmoney7")
    private BigDecimal monthtotalmoney7;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexcutemoney7")
    private BigDecimal monthexcutemoney7;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthtotalmoney8")
    private BigDecimal monthtotalmoney8;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexcutemoney8")
    private BigDecimal monthexcutemoney8;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthtotalmoney9")
    private BigDecimal monthtotalmoney9;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexcutemoney9")
    private BigDecimal monthexcutemoney9;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthtotalmoney10")
    private BigDecimal monthtotalmoney10;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexcutemoney10")
    private BigDecimal monthexcutemoney10;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthtotalmoney11")
    private BigDecimal monthtotalmoney11;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexcutemoney11")
    private BigDecimal monthexcutemoney11;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthtotalmoney12")
    private BigDecimal monthtotalmoney12;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexcutemoney12")
    private BigDecimal monthexcutemoney12;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthtotalmoney1")
    private BigDecimal monthtotalmoney1;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexcutemoney1")
    private BigDecimal monthexcutemoney1;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthtotalmoney2")
    private BigDecimal monthtotalmoney2;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexcutemoney2")
    private BigDecimal monthexcutemoney2;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthtotalmoney3")
    private BigDecimal monthtotalmoney3;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexcutemoney3")
    private BigDecimal monthexcutemoney3;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthtotalmoney4")
    private BigDecimal monthtotalmoney4;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexcutemoney4")
    private BigDecimal monthexcutemoney4;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthtotalmoney5")
    private BigDecimal monthtotalmoney5;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthexcutemoney5")
    private BigDecimal monthexcutemoney5;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "orderno")
    private Integer orderno;

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
