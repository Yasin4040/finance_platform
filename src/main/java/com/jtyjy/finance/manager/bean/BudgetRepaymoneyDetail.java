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
@TableName(value = "budget_repaymoney_detail_new")
@Data
public class BudgetRepaymoneyDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 还款id
     */
    @NotNull(message = "还款id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "repaymoneyid")
    private Long repaymoneyid;

    /**
     * 借款id
     */
    @NotNull(message = "借款id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "lendmoneyid")
    private Long lendmoneyid;

    /**
     * 当时借款金额
     */
    @NotNull(message = "当时借款金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "curlendmoney")
    private BigDecimal curlendmoney;

    /**
     * 本次还款金额（本金）
     */
    @NotNull(message = "本次还款金额（本金）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "repaymoney")
    private BigDecimal repaymoney;

    /**
     * 还的利息
     */
    @NotNull(message = "还的利息不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "interestmoney")
    private BigDecimal interestmoney;

    /**
     * 现在还剩借款金额
     */
    @NotNull(message = "现在还剩借款金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "nowlendmoney")
    private BigDecimal nowlendmoney;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

}
