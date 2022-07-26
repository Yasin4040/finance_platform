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
 * @author Admin
 */
@TableName(value = "budget_month_agent")
@Data
public class BudgetMonthAgent implements Serializable {

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
     * 月度id
     */
    @NotNull(message = "月度id不能为空")
    @ApiModelProperty(value = "月度id")
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 当时的年度动因金额
     */
    @NotNull(message = "当时的年度动因金额不能为空")
    @ApiModelProperty(value = "当时的年度动因金额")
    @TableField(value = "yearagentmoney")
    private BigDecimal yearagentmoney;

    /**
     * 当时的年度追加金额
     */
    @NotNull(message = "当时的年度追加金额 不能为空")
    @ApiModelProperty(value = "当时的年度追加金额")
    @TableField(value = "yearaddmoney")
    private BigDecimal yearaddmoney;

    /**
     * 当时的年度拆出金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "当时的年度拆出金额（同科目里面的动因可以拆借）不能为空")
    @ApiModelProperty(value = "当时的年度拆出金额（同科目里面的动因可以拆借）")
    @TableField(value = "yearlendoutmoney")
    private BigDecimal yearlendoutmoney;

    /**
     * 当时的年度当时的年度拆进金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "当时的年度当时的年度拆进金额（同科目里面的动因可以拆借）不能为空")
    @ApiModelProperty(value = "当时的年度当时的年度拆进金额（同科目里面的动因可以拆借）")
    @TableField(value = "yearlendinmoney")
    private BigDecimal yearlendinmoney;

    /**
     * 当时的年度执行数量
     */
    @NotNull(message = "当时的年度执行数量不能为空")
    @ApiModelProperty(value = "当时的年度执行数量")
    @TableField(value = "yearexecutemoney")
    private BigDecimal yearexecutemoney;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiModelProperty(value = "未知参数")
    @TableField(value = "m")
    private BigDecimal m;

    /**
     * 月度预算金额(可编辑)
     */
    @NotNull(message = "月度预算金额(可编辑)不能为空")
    @ApiModelProperty(value = "月度预算金额(可编辑)")
    @TableField(value = "total")
    private BigDecimal total;

    /**
     * 动因类型：0:年初（月初），1：追加
     */
    @NotNull(message = "动因类型：0:年初（月初），1：追加不能为空")
    @ApiModelProperty(value = "动因类型：0:年初（月初），1：追加")
    @TableField(value = "agenttype")
    private Integer agenttype;

    /**
     * 预算科目id
     */
    @NotNull(message = "预算科目id不能为空")
    @ApiModelProperty(value = "预算科目id")
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 动因名称
     */
    @NotBlank(message = "动因名称不能为空")
    @ApiModelProperty(value = "动因名称")
    @TableField(value = "name")
    private String name;

    /**
     * 弹性动因标识 true表示弹性
     */
    @ApiModelProperty(value = "弹性动因标识 true表示弹性")
    @TableField(value = "elasticflag")
    private Boolean elasticflag;

    /**
     * 弹性动因占比上限(null or < 0 不受控制)
     */
    @ApiModelProperty(value = "弹性动因占比上限(null or < 0 不受控制)")
    @TableField(value = "elasticmax")
    private BigDecimal elasticmax;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "未知参数")
    @TableField(value = "elasticratio")
    private BigDecimal elasticratio;

    /**
     * 占比科目id
     */
    @ApiModelProperty(value = "主键Id")
    @TableField(value = "budgetsubjectid")
    private Long budgetsubjectid;

    /**
     * 动因内容
     */
    @ApiModelProperty(value = "动因内容")
    @TableField(value = "remark")
    private String remark;

    /**
     * 产品id
     */
    @ApiModelProperty(value = "产品id")
    @TableField(value = "productid")
    private Long productid;

    /**
     * 月度追加金额
     */
    @ApiModelProperty(value = "主键Id")
    @TableField(value = "addmoney")
    private BigDecimal addmoney;

    /**
     * 月度拆出金额（同科目里面的动因可以拆借
     */
    @ApiModelProperty(value = "月度拆出金额（同科目里面的动因可以拆借")
    @TableField(value = "lendoutmoney")
    private BigDecimal lendoutmoney;

    /**
     * 月度拆进金额（同科目里面的动因可以拆借）
     */
    @ApiModelProperty(value = "月度拆进金额（同科目里面的动因可以拆借）")
    @TableField(value = "lendinmoney")
    private BigDecimal lendinmoney;

    /**
     * 累计执行金额
     */
    @ApiModelProperty(value = "累计执行金额")
    @TableField(value = "executemoney")
    private BigDecimal executemoney;

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
     * 年度动因id
     */
    @ApiModelProperty(value = "年度动因id")
    @TableField(value = "yearagentid")
    private Long yearagentid;

    /**
     * 月度预算活动说明
     */
    @ApiModelProperty(value = "月度预算活动说明")
    @TableField(value = "monthbusiness")
    private String monthbusiness;

}
