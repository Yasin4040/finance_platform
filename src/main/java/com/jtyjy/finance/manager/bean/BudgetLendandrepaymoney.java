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
@TableName(value = "budget_lendandrepaymoney_new")
@Data
public class BudgetLendandrepaymoney implements Serializable {

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
    @TableField(value = "empid")
    private String empid;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "empno")
    private String empno;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "empname")
    private String empname;

    /**
     * 借款id
     */
    @ApiParam(hidden = true)
    @TableField(value = "lendmoneyid")
    private Long lendmoneyid;

    /**
     * 还款id
     */
    @ApiParam(hidden = true)
    @TableField(value = "repaymoneyid")
    private Long repaymoneyid;

    /**
     * 当时的金额
     */
    @NotNull(message = "当时的金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "curmoney")
    private BigDecimal curmoney;

    /**
     * 借、还金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "money")
    private BigDecimal money;

    /**
     * 1:借款；-1：还款
     */
    @ApiParam(hidden = true)
    @TableField(value = "moneytype")
    private Integer moneytype;

    /**
     * 现在的金额 = 当时的金额 + moneytype * money;
     */
    @ApiParam(hidden = true)
    @TableField(value = "nowmoney")
    private BigDecimal nowmoney;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

}
