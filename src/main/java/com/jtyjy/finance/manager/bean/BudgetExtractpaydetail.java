package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_extractpaydetail")
@Data
public class BudgetExtractpaydetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value="id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 未知参数
     */
    @ApiModelProperty(value="提成批次",hidden = true)
    @TableField(value = "extractmonth")
    private String extractmonth;

    /**
     * 未知参数
     */
    @ApiModelProperty(value="是否公司员工")
    @TableField(value = "iscompanyemp")
    private Boolean iscompanyemp;

    /**
     * 未知参数
     */
    @ApiModelProperty(value="身份证号")
    @TableField(value = "idnumber")
    private String idnumber;

    /**
     * 工号
     */
    @ApiModelProperty(value="工号")
    @TableField(value = "empno")
    private String empno;

    /**
     * 员工名称
     */
    @ApiModelProperty(value="姓名")
    @TableField(value = "empname")
    private String empname;

    /**
     * 实发工资(同月只加一次)
     */
    @ApiModelProperty(value="工资")
    @TableField(value = "salary")
    private BigDecimal salary = BigDecimal.ZERO;

    /**
     * 累计实发工资
     */
    @ApiModelProperty(value="累计工资")
    @TableField(value = "salarylj")
    private BigDecimal salarylj = BigDecimal.ZERO;

    /**
     * 工资个税
     */
    @ApiModelProperty(value="工资个税")
    @TableField(value = "salarytax")
    private BigDecimal salarytax = BigDecimal.ZERO;

    /**
     * 累计工资个税
     */
    @ApiModelProperty(value="累计工资个税")
    @TableField(value = "salarytaxlj")
    private BigDecimal salarytaxlj = BigDecimal.ZERO;

    /**
     * 待发提成(应发提成-综合税)
     */
    @ApiModelProperty(value="待发提成")
    @TableField(value = "copeextract")
    private BigDecimal copeextract = BigDecimal.ZERO;

    /**
     * 累计待发提成(含本次)
     */
    @ApiModelProperty(value="累计待发提成")
    @TableField(value = "copeextractlj")
    private BigDecimal copeextractlj = BigDecimal.ZERO;

    @ApiModelProperty(value="本月额外税")
    @TableField(value = "freetax")
    private BigDecimal freetax = BigDecimal.ZERO;  //本月额外税

    @ApiModelProperty(value="累计额外税(不含本月)")
    @TableField(value = "freetaxs")
    private BigDecimal freetaxs = BigDecimal.ZERO; //累计额外税(不含本月)

    /**
     * 综合税
     */
    @ApiModelProperty(value="综合税")
    @TableField(value = "consotax")
    private BigDecimal consotax = BigDecimal.ZERO;

    /**
     * 累计综合税(含本次)
     */
    @ApiModelProperty(value="累计综合税")
    @TableField(value = "consotaxlj")
    private BigDecimal consotaxlj = BigDecimal.ZERO;

    /**
     * 实扣个税 (工资个税(同月多次只加一次)+综合税)
     */
    @ApiModelProperty(value="实扣个税")
    @TableField(value = "realesalarytax")
    private BigDecimal realesalarytax = BigDecimal.ZERO;

    /**
     * 累计实扣个税(含本次)
     */
    @ApiModelProperty(value="累计实扣个税")
    @TableField(value = "realesalarytaxlj")
    private BigDecimal realesalarytaxlj = BigDecimal.ZERO;

    /**
     * 五险一金总和
     */
    @ApiModelProperty(value="社保")
    @TableField(value = "fiveriskonefund")
    private BigDecimal fiveriskonefund = BigDecimal.ZERO;

    /**
     * 累计五险一金总和(含本次)
     */
    @ApiModelProperty(value="累计社保")
    @TableField(value = "fiveriskonefundlj")
    private BigDecimal fiveriskonefundlj = BigDecimal.ZERO;

    /**
     * 专项扣除
     */
    @ApiModelProperty(value="专项扣除")
    @TableField(value = "specialdeduction")
    private BigDecimal specialdeduction = BigDecimal.ZERO;

    /**
     * 累计专项扣除(含本次)
     */
    @ApiModelProperty(value="累计专项扣除")
    @TableField(value = "specialdeductionlj")
    private BigDecimal specialdeductionlj = BigDecimal.ZERO;

    /**
     * 起征点(工资发放单位若为：西藏、西藏新知雅、乐学，起征点为9000；否则，为5000)
     */
    @ApiModelProperty(value="起征点")
    @TableField(value = "threshold")
    private BigDecimal threshold = BigDecimal.ZERO;

    /**
     * 累计起征点(含本次)
     */
    @ApiModelProperty(value="累计起征点")
    @TableField(value = "thresholdlj")
    private BigDecimal thresholdlj = BigDecimal.ZERO;

    /**
     * 累计倒推金额(累计实扣个税反推)+累计起征点+累计专项扣除
     */
    @ApiModelProperty(value="倒推金额")
    @TableField(value = "dtje")
    private BigDecimal dtje = BigDecimal.ZERO;

    /**
     * 可发提成(本次倒推金额-累计实扣个税(同月多次只加一次)-累计实扣工资(同月多次只加一次)-法人公司累计实发(不包括本次，同月多次需加上前几次))
     */
    @ApiModelProperty(value="可发提成")
    @TableField(value = "renewableextract")
    private BigDecimal renewableextract = BigDecimal.ZERO;

    /**
     * 临界金额(工资发放单位若为:西藏、新之雅、乐学：20785；其他发放公司：14560)
     */
    @ApiModelProperty(value="临界金额")
    @TableField(value = "criticalmoney")
    private BigDecimal criticalmoney = BigDecimal.ZERO;

    /**
     * 累计临界金额(含本次)
     */
    @ApiModelProperty(value="累计临界金额")
    @TableField(value = "criticalmoneylj")
    private BigDecimal criticalmoneylj = BigDecimal.ZERO;

    /**
     * 累计5.39%释放可发提成【累计临界金额(同月多次只加一次,包含本月) - 累计实发工资(含本月) + 累计专项扣除(含本月) -法人公司累计实发(不包括本次，同月多次需加上前几次)】
     */
    @ApiModelProperty(value="5.39%释放可发提成")
    @TableField(value = "renewableextract539")
    private BigDecimal renewableextract539 = BigDecimal.ZERO;

    /**
     * 公司可发【Max(本次可发提成,本次累计5.39%释放可发提成)】
     */
    @ApiModelProperty(value="公司可发")
    @TableField(value = "companyableextract")
    private BigDecimal companyableextract = BigDecimal.ZERO;

    /**
     * 法人公司实发【Min(本次应发提成,本次公司可发)】
     */
    @ApiModelProperty(value="法人公司实发")
    @TableField(value = "incorporatedcompany")
    private BigDecimal incorporatedcompany = BigDecimal.ZERO;

    /**
     * 法人公司实发累计(不含本次)
     */
    @ApiModelProperty(value="累计法人公司实发(不含本次)")
    @TableField(value = "incorporatedcompanylj")
    private BigDecimal incorporatedcompanylj = BigDecimal.ZERO;

    /**
     * 避税发放【本次应发提成 - 本次法人公司实发】
     */
    @ApiModelProperty(value="避税发放")
    @TableField(value = "aviodtax")
    private BigDecimal aviodtax = BigDecimal.ZERO;

    /**
     * 实发提成合计【实发工资 + 本次法人公司实发】
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "realeextract")
    private BigDecimal realeextract = BigDecimal.ZERO;

    /**
     * 累计实发提成(包括本次)
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "realeextractsum")
    private BigDecimal realeextractsum = BigDecimal.ZERO;

    /**
     * 实发提成倒推金额
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "sfdtje")
    private BigDecimal sfdtje = BigDecimal.ZERO;

    /**
     * 上交个税，不体现负值【 （实发提成倒推 - 累计专项扣除(包含本次) - 累计起征点(包含本次)）*税率 - 扣除数 - 累计上交个税（不含本次）】
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "payabletax")
    private BigDecimal payabletax = BigDecimal.ZERO;

    /**
     * 累计上交个税(含本次)
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "payabletaxlj")
    private BigDecimal payabletaxlj = BigDecimal.ZERO;

    /**
     * 个税差异，正数为公司需补贴个税【本次上交个税-本次实扣个税+上月个税差异(如果为本月第2次，还需加上第1次)】
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "taxdiffrence")
    private BigDecimal taxdiffrence = BigDecimal.ZERO;

    /**
     * 释放比例 【累计上次个税(包含本次) / (累计五险(包含本次) + 本次实发提成倒推)】
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "releasepercent")
    private BigDecimal releasepercent = BigDecimal.ZERO;

    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    @TableField(value = "createtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createtime;

    /**
     * 总待发提成(参与公式计算+超额不参与计算)
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "totalextract")
    private BigDecimal totalextract = BigDecimal.ZERO;

    /**
     * 总避税(参与公式计算+不参与计算)
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "totalaviodtax")
    private BigDecimal totalaviodtax = BigDecimal.ZERO;

    /**
     * 法人公司实发费用
     */
    @ApiModelProperty(value="法人公司实发费用")
    @TableField(value = "incorporatedcompanyfee")
    private BigDecimal incorporatedcompanyfee = BigDecimal.ZERO;

}
