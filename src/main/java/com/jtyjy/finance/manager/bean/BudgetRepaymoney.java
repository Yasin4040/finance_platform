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
@TableName(value = "budget_repaymoney_new")
@Data
public class BudgetRepaymoney implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键", hidden = false, required = false)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * （还款人）员工id
     */
    @NotBlank(message = "（还款人）员工id不能为空")
    @ApiModelProperty(value = "还款人）员工id", hidden = false, required = false)
    @TableField(value = "empid")
    private String empid;

    /**
     * （还款人）员工工号
     */
    @NotBlank(message = "（还款人）员工工号不能为空")
    @ApiModelProperty(value = "（还款人）员工工号", hidden = false, required = false)
    @TableField(value = "empno")
    private String empno;

    /**
     * （还款人）员工姓名
     */
    @NotBlank(message = "（还款人）员工姓名不能为空")
    @ApiModelProperty(value = "（还款人）员工姓名", hidden = false, required = false)
    @TableField(value = "empname")
    private String empname;

    /**
     * 还款单号
     */
    @ApiModelProperty(value = "还款单号", hidden = false, required = false)
    @TableField(value = "repaymoneycode")
    private String repaymoneycode;

    /**
     * 还款金额
     */
    @NotNull(message = "还款金额不能为空")
    @ApiModelProperty(value = "还款金额", hidden = false, required = false)
    @TableField(value = "repaymoney")
    private BigDecimal repaymoney;

    /**
     * 还款方式 1:现金还款，2：手机支付还款 3：工资还款，4：提成还款，5：报销冲账还款 6:项目借款抵消还款 7:入库冲账
     */
    @NotNull(message = "还款方式 1:现金还款，2：手机支付还款 3：工资还款，4：提成还款，5：报销冲账还款 6:项目借款抵消还款 7:入库冲账不能为空")
    @ApiModelProperty(value = "还款方式", hidden = false, required = false)
    @TableField(value = "repaytype")
    private Integer repaytype;

    /**
     * 生效标识 true:为生效
     */
    @NotNull(message = "生效标识 true:为生效不能为空")
    @ApiModelProperty(value = "生效标识", hidden = false, required = false)
    @TableField(value = "effectflag")
    private Boolean effectflag;

    /**
     * //还款对象id（支付宝微信 支付信息、提成明细id、报销单（冲账）id、项目预领借款主表id（项目预领借款））
     */
    @NotBlank(message = "还款对象id（支付宝微信 支付信息、提成明细id、报销单（冲账）id、项目预领借款主表id（项目预领借款））不能为空")
    @ApiModelProperty(value = "还款对象id", hidden = false, required = false)
    @TableField(value = "repaytypeid")
    private String repaytypeid;

    /**
     * 手机支付方式：1：微信，2：支付宝
     */
    @ApiModelProperty(value = "手机支付方式：1：微信，2：支付宝", hidden = false, required = false)
    @TableField(value = "phonepaytype")
    private Integer phonepaytype;

    /**
     * 手机支付状态，1：生成（等待支付），2：支付成功
     */
    @ApiModelProperty(value = "手机支付状态，1：生成（等待支付），2：支付成功", hidden = false, required = false)
    @TableField(value = "phonepaystatus")
    private Integer phonepaystatus;

    /**
     * 支付支付成功时间
     */
    @ApiModelProperty(value = "支付支付成功时间", hidden = false, required = false)
    @TableField(value = "phonepaytime")
    private Date phonepaytime;

    /**
     * 还款日期
     */
    @NotNull(message = "还款日期不能为空")
    @ApiModelProperty(value = "还款日期", hidden = false, required = false)
    @TableField(value = "repaydate")
    private Date repaydate;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "创建时间", hidden = false, required = false)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 目前还款金额
     */
    @ApiModelProperty(value = "目前还款金额", hidden = false, required = false)
    @TableField(value = "nowrepaymoney")
    private BigDecimal nowrepaymoney;

}
