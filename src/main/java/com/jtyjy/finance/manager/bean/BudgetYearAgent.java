package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 年度动因表
 *
 * @author Admin
 */
@TableName(value = "budget_year_agent")
@Data
public class BudgetYearAgent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @NotNull(message = "主键Id不能为空")
    @ApiModelProperty(value = "主键Id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiModelProperty(value = "届别id")
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 预算单位id
     */
    @NotNull(message = "预算单位id不能为空")
    @ApiModelProperty(value = "预算单位id")
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 6月
     */
    @NotNull(message = "6月不能为空")
    @ApiModelProperty(value = "6月")
    @TableField(value = "m6")
    private BigDecimal m6;

    /**
     * 7月
     */
    @NotNull(message = "7月不能为空")
    @ApiModelProperty(value = "7月")
    @TableField(value = "m7")
    private BigDecimal m7;

    /**
     * 8月
     */
    @NotNull(message = "8月不能为空")
    @ApiModelProperty(value = "8月")
    @TableField(value = "m8")
    private BigDecimal m8;

    /**
     * 9月
     */
    @NotNull(message = "9月不能为空")
    @ApiModelProperty(value = "9月")
    @TableField(value = "m9")
    private BigDecimal m9;

    /**
     * 10月
     */
    @NotNull(message = "10月不能为空")
    @ApiModelProperty(value = "10月")
    @TableField(value = "m10")
    private BigDecimal m10;

    /**
     * 11月
     */
    @NotNull(message = "11月不能为空")
    @ApiModelProperty(value = "11月")
    @TableField(value = "m11")
    private BigDecimal m11;

    /**
     * 12月
     */
    @NotNull(message = "12月不能为空")
    @ApiModelProperty(value = "12月")
    @TableField(value = "m12")
    private BigDecimal m12;

    /**
     * 1月
     */
    @NotNull(message = "1月不能为空")
    @ApiModelProperty(value = "1月")
    @TableField(value = "m1")
    private BigDecimal m1;

    /**
     * 2月
     */
    @NotNull(message = "2月不能为空")
    @ApiModelProperty(value = "2月")
    @TableField(value = "m2")
    private BigDecimal m2;

    /**
     * 3月
     */
    @NotNull(message = "3月不能为空")
    @ApiModelProperty(value = "3月")
    @TableField(value = "m3")
    private BigDecimal m3;

    /**
     * 4月
     */
    @NotNull(message = "4月不能为空")
    @ApiModelProperty(value = "4月")
    @TableField(value = "m4")
    private BigDecimal m4;

    /**
     * 5月
     */
    @NotNull(message = "5月不能为空")
    @ApiModelProperty(value = "5月")
    @TableField(value = "m5")
    private BigDecimal m5;

    /**
     * 本届总金额(12个月)
     */
    @NotNull(message = "本届总金额(12个月)不能为空")
    @ApiModelProperty(value = "本届总金额(12个月)")
    @TableField(value = "total")
    private BigDecimal total;

    /**
     * 上届执行总金额
     */
    @NotNull(message = "上届执行总金额不能为空")
    @ApiModelProperty(value = "上届执行总金额")
    @TableField(value = "pretotal")
    private BigDecimal pretotal;

    /**
     * 上届预估
     */
    @NotNull(message = "上届预估不能为空")
    @ApiModelProperty(value = "上届预估")
    @TableField(value = "preestimate")
    private BigDecimal preestimate;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 预算科目id
     */
    @NotNull(message = "预算科目id不能为空")
    @ApiModelProperty(value = "预算科目id")
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 动因类型：0:年初（月初），1：追加
     */
    @NotNull(message = "动因类型：0:年初（月初），1：追加不能为空")
    @ApiModelProperty(value = "动因类型：0:年初（月初），1：追加")
    @TableField(value = "agenttype")
    private Integer agenttype;

    /**
     * 动因名称
     */
    @NotBlank(message = "动因名称不能为空")
    @ApiModelProperty(value = "动因名称")
    @TableField(value = "name")
    private String name;

    /**
     * 发生次数
     */
    @ApiModelProperty(value = "发生次数")
    @TableField(value = "happencount")
    private String happencount;

    /**
     * 计算过程
     */
    @ApiModelProperty(value = "计算过程")
    @TableField(value = "computingprocess")
    private String computingprocess;

    /**
     * 动因内容
     */
    @NotBlank(message = "动因内容不能为空")
    @ApiModelProperty(value = "动因内容")
    @TableField(value = "remark")
    private String remark;

    /**
     * 弹性标识 true表示弹性
     */
    @NotNull(message = "弹性标识 true表示弹性不能为空")
    @ApiModelProperty(value = "弹性标识 true表示弹性")
    @TableField(value = "elasticflag")
    private Boolean elasticflag;

    /**
     * 弹性动因占比上限(null or < 0 不受控制)
     */
    @ApiModelProperty(value = "弹性动因占比上限(null or < 0 不受控制)")
    @TableField(value = "elasticmax")
    private BigDecimal elasticmax;

    /**
     * 弹性率
     */
    @ApiModelProperty(value = "弹性率")
    @TableField(value = "elasticratio")
    private BigDecimal elasticratio;

    /**
     * 占比科目id
     */
    @ApiModelProperty(value = "占比科目id")
    @TableField(value = "budgetsubjectid")
    private Long budgetsubjectid;

    /**
     * 产品id
     */
    @ApiModelProperty(value = "产品id")
    @TableField(value = "productid")
    private Long productid;

    /**
     * 累计追加金额
     */
    @NotNull(message = "累计追加金额不能为空")
    @ApiModelProperty(value = "累计追加金额")
    @TableField(value = "addmoney")
    private BigDecimal addmoney;

    /**
     * 累计拆出金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "累计拆出金额（同科目里面的动因可以拆借）不能为空")
    @ApiModelProperty(value = "累计拆出金额（同科目里面的动因可以拆借）")
    @TableField(value = "lendoutmoney")
    private BigDecimal lendoutmoney;

    /**
     * 累计拆进金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "累计拆进金额（同科目里面的动因可以拆借）不能为空")
    @ApiModelProperty(value = "累计拆进金额（同科目里面的动因可以拆借）")
    @TableField(value = "lendinmoney")
    private BigDecimal lendinmoney;

    /**
     * 累计执行金额
     */
    @NotNull(message = "累计执行金额不能为空")
    @ApiModelProperty(value = "累计执行金额")
    @TableField(value = "executemoney")
    private BigDecimal executemoney;

    // ---------- 扩展字段 ----------

    @ApiModelProperty(value = "预算单位名称")
    @TableField(exist = false)
    private String unitName;

    @ApiModelProperty(value = "年度剩余金额")
    @TableField(exist = false)
    private BigDecimal balance;

}
