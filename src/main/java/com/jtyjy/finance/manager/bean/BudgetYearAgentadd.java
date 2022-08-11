package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_year_agentadd")
@Data
public class BudgetYearAgentadd implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @ApiModelProperty(value = "主键Id（新增时不传, 更新时必传）", required = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiModelProperty("届别id")
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 预算单位id
     */
    @NotNull(message = "预算单位id不能为空")
    @ApiModelProperty("预算单位id")
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 预算科目id
     */
    @NotNull(message = "预算科目id不能为空")
    @ApiModelProperty("预算科目id")
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 年度动因id
     */
    @ApiModelProperty("年度动因id")
    @TableField(value = "yearagentid")
    private Long yearagentid;

    /**
     * 年度预算追加Id
     */
    @NotNull(message = "年度预算追加Id不能为空")
    @TableField(value = "infoid")
    private Long infoid;

    /**
     * 0:追加金额，1：追加动因
     */
    @NotNull(message = "0:追加金额，1：追加动因不能为空")
    @ApiModelProperty("0:追加金额，1：追加动因")
    @TableField(value = "type")
    private Integer type;

    /**
     * 修改时间
     */
    @ApiModelProperty("修改时间")
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 创建时间
     */
    @NotNull(message = "创建时间不能为空")
    @ApiModelProperty("创建时间")
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 追加前动因年度预算金额（年初预算）
     */
    @NotNull(message = "追加前动因年度预算金额（年初预算）不能为空")
    @ApiModelProperty("追加前动因年度预算金额（年初预算）")
    @TableField(value = "agentmoney")
    private BigDecimal agentmoney;

    /**
     * 追加前动因累计追加金额
     */
    @NotNull(message = "追加前动因累计追加金额不能为空")
    @ApiModelProperty("追加前动因累计追加金额")
    @TableField(value = "agentaddmoney")
    private BigDecimal agentaddmoney;

    /**
     * 追加前拆出金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "追加前拆出金额（同科目里面的动因可以拆借）不能为空")
    @ApiModelProperty("追加前拆出金额（同科目里面的动因可以拆借）")
    @TableField(value = "agentlendoutmoney")
    private BigDecimal agentlendoutmoney;

    /**
     * 追加前拆进金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "追加前拆进金额（同科目里面的动因可以拆借）不能为空")
    @ApiModelProperty("追加前拆进金额（同科目里面的动因可以拆借）")
    @TableField(value = "agentlendinmoney")
    private BigDecimal agentlendinmoney;

    /**
     * 追加前动因累计执行金额
     */
    @NotNull(message = "追加前动因累计执行金额不能为空")
    @ApiModelProperty("追加前动因累计执行金额")
    @TableField(value = "agentexcutemoney")
    private BigDecimal agentexcutemoney;

    /**
     * 追加金额
     */
    @NotNull(message = "追加金额不能为空")
    @ApiModelProperty("追加金额")
    @TableField(value = "total")
    private BigDecimal total;

    /**
     * 1月分解
     */
    @NotNull(message = "1月分解不能为空")
    @ApiModelProperty("1月分解")
    @TableField(value = "m1")
    private BigDecimal m1;

    /**
     * 2月分解
     */
    @NotNull(message = "2月分解不能为空")
    @ApiModelProperty("2月分解")
    @TableField(value = "m2")
    private BigDecimal m2;

    /**
     * 3月分解
     */
    @NotNull(message = "3月分解不能为空")
    @ApiModelProperty("3月分解")
    @TableField(value = "m3")
    private BigDecimal m3;

    /**
     * 4月分解
     */
    @NotNull(message = "4月分解不能为空")
    @ApiModelProperty("4月分解")
    @TableField(value = "m4")
    private BigDecimal m4;

    /**
     * 5月分解
     */
    @NotNull(message = "5月分解不能为空")
    @ApiModelProperty("5月分解")
    @TableField(value = "m5")
    private BigDecimal m5;

    /**
     * 6月分解
     */
    @NotNull(message = "6月分解不能为空")
    @ApiModelProperty("6月分解")
    @TableField(value = "m6")
    private BigDecimal m6;

    /**
     * 7月分解
     */
    @NotNull(message = "7月分解不能为空")
    @ApiModelProperty("7月分解")
    @TableField(value = "m7")
    private BigDecimal m7;

    /**
     * 8月分解
     */
    @NotNull(message = "8月分解不能为空")
    @ApiModelProperty("8月分解")
    @TableField(value = "m8")
    private BigDecimal m8;

    /**
     * 9月分解
     */
    @NotNull(message = "9月分解不能为空")
    @ApiModelProperty("9月分解")
    @TableField(value = "m9")
    private BigDecimal m9;

    /**
     * 10月分解
     */
    @NotNull(message = "10月分解不能为空")
    @ApiModelProperty("10月分解")
    @TableField(value = "m10")
    private BigDecimal m10;

    /**
     * 11月分解
     */
    @NotNull(message = "11月分解不能为空")
    @ApiModelProperty("11月分解")
    @TableField(value = "m11")
    private BigDecimal m11;

    /**
     * 12月分解
     */
    @NotNull(message = "12月分解不能为空")
    @ApiModelProperty("12月分解")
    @TableField(value = "m12")
    private BigDecimal m12;

    /**
     * 当前月金额
     */
    @ApiModelProperty("当前月金额")
    @TableField(value = "curmonthmoney")
    private BigDecimal curmonthmoney;

    /**
     * 当前月
     */
    @ApiModelProperty("当前月")
    @TableField(value = "curmonthid")
    private Long curmonthid;

    /**
     * 月度动因id（审核通过后添加一个月度动因）
     */
    @ApiModelProperty("月度动因id（审核通过后添加一个月度动因）")
    @TableField(value = "monthagentid")
    private Long monthagentid;

    /**
     * 追加原因
     */
    @ApiModelProperty("追加原因")
    @TableField(value = "remark")
    private String remark;

    /**
     * 动因名称
     */
    @ApiModelProperty("动因名称")
    @TableField(value = "name")
    private String name;

    // --------------------------------------------------

    @ApiModelProperty(value = "追加前-年度余额")
    @TableField(exist = false)
    private BigDecimal preYearBalance;

    @ApiModelProperty(value = "追加后-年度余额")
    @TableField(exist = false)
    private BigDecimal yearBalance;

    /**
     * 是否免罚 0 否 1是
     */
    @NotNull(message = "是否免罚不能为空")
    @ApiModelProperty(value = "是否免罚 false否 true是", required = true)
    private Boolean isExemptFine;

    /**
     * 免罚原因
     */
    @ApiModelProperty(value = "免罚原因")
    private String exemptFineReason;

    /**
     * 免罚结果 0 免罚 1 罚款
     */
    @ApiModelProperty(value = "免罚结果 0 免罚 1 罚款（新增修改不用传）")
    private Integer exemptResult;

    @ApiModelProperty(value = "免罚结果")
    @TableField(exist = false)
    private String showExemptResult;

    /**
     * 罚款理由说明
     */
    @ApiModelProperty(value = "罚款理由说明（新增修改不用传）")
    private String fineRemark;
}
