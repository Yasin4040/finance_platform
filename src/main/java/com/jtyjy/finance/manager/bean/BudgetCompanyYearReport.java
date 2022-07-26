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
@TableName(value = "budget_company_year_report")
@Data
public class BudgetCompanyYearReport implements Serializable {

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
     * 年度追加（自身的）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearaddmoney")
	private BigDecimal yearaddmoney;

    /**
     * 年度追加次数（自身的）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearaddcount")
	private Long yearaddcount;

    /**
     * 年度拆进金额（自身的）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearlendinmoney")
	private BigDecimal yearlendinmoney;

    /**
     * 年度拆进次数（自身的）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearlendincount")
	private Long yearlendincount;

    /**
     * 年度拆出金额（自身的）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearlendoutmoney")
	private BigDecimal yearlendoutmoney;

    /**
     * 年度拆出次数（自身的）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearlendoutcount")
	private Long yearlendoutcount;

    /**
     * 年度执行金额（自身的）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearexecutemoney")
	private BigDecimal yearexecutemoney;

    /**
     * 年度执行次数（自身的）
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearexecutecount")
	private Long yearexecutecount;

    /**
     * 年度预算收入（销售收入的年度预算）占比
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearrevenueshare")
	private BigDecimal yearrevenueshare;

    /**
     * 年度预算码洋（销售码洋的年度预算）占比
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearcodeoceanshare")
	private BigDecimal yearcodeoceanshare;

    /**
     * 年初预算（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearmoney")
	private BigDecimal Yearmoney;

    /**
     * 年度追加金额（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearaddmoney")
	private BigDecimal Yearaddmoney;

    /**
     * 年度追加次数（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearaddcount")
	private Long Yearaddcount;

    /**
     * 年度拆进金额（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearlendinmoney")
	private BigDecimal Yearlendinmoney;

    /**
     * 年度拆进次数（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearlendincount")
	private Long Yearlendincount;

    /**
     * 年度拆出金额（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearlendoutmoney")
	private BigDecimal Yearlendoutmoney;

    /**
     * 年度拆出次数（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearlendoutcount")
	private Long Yearlendoutcount;

    /**
     * 年度执行金额包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearexecutemoney")
	private BigDecimal Yearexecutemoney;

    /**
     * 年度执行次数（包含子预算单位）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearexecutecount")
	private Long Yearexecutecount;

    /**
     * 年度预算收入（销售收入的年度预算）占比（包含子）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearrevenueshare")
	private BigDecimal Yearrevenueshare;

    /**
     * 年度预算码洋（销售码洋的年度预算）占比（包含子）
     */
    @ApiParam(hidden = true)
    @TableField(value = "_yearcodeoceanshare")
	private BigDecimal Yearcodeoceanshare;

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
