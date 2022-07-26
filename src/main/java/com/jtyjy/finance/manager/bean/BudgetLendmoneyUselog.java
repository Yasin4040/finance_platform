package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@TableName(value = "budget_lendmoney_uselog_new")
@Data
public class BudgetLendmoneyUselog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 锁定金额
     */
    @NotNull(message = "锁定金额不能为空")
    @ApiModelProperty(value = "锁定金额")
    @TableField(value = "lockedmoney")
    private BigDecimal lockedmoney;

    /**
     * 借款id
     */
    @NotNull(message = "借款id不能为空")
    @ApiModelProperty(value = "借款id")
    @TableField(value = "lendmoneyid")
    private Long lendmoneyid;

    /**
     * 使用类型： 1:现金还款，2：手机支付还款 3：工资还款，4：提成还款，5：报销冲账还款 6:项目借款抵消还款
     */
    @NotNull(message = "使用类型： 1:现金还款，2：手机支付还款 3：工资还款，4：提成还款，5：报销冲账还款 6:项目借款抵消还款不能为空")
    @ApiModelProperty(value = "使用类型： 1:现金还款，2：手机支付还款 3：工资还款，4：提成还款，5：报销冲账还款 6:项目借款抵消还款")
    @TableField(value = "usetype")
    private Integer usetype;

    /**
     * 使用对象id
     */
    @NotBlank(message = "使用对象id不能为空")
    @ApiModelProperty(value = "使用对象id")
    @TableField(value = "useobjectid")
    private String useobjectid;

    /**
     * 使用备注
     */
    @NotBlank(message = "使用备注不能为空")
    @ApiModelProperty(value = "使用备注")
    @TableField(value = "usemark")
    private String usemark;

    /**
     * 使用状态 true:正在被使用，false:未被使用
     */
    @NotNull(message = "使用状态 true:正在被使用，false:未被使用不能为空")
    @ApiModelProperty(value = "使用状态 true:正在被使用，false:未被使用")
    @TableField(value = "useflag")
    private Boolean useflag;

    /**
     * 创建时间
     */
    @NotNull(message = "创建时间不能为空")
    @ApiModelProperty(value = "创建时间")
    @TableField(value = "createtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createtime;

    // --------------------------------------------------

    @ApiModelProperty(value = "报销单号")
    @TableField(exist = false)
    private String reimcode;

}
