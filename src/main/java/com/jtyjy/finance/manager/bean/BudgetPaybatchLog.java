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
@TableName(value = "budget_paybatch_log_new")
@Data
public class BudgetPaybatchLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 批次号
     */
    @NotBlank(message = "批次号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "paybatchcode")
    private String paybatchcode;

    /**
     * 多个付款单id
     */
    @NotBlank(message = "多个付款单id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "paymoneyids")
    private String paymoneyids;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 创建人的名称
     */
    @NotBlank(message = "创建人的名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "creatorname")
    private String creatorname;

    /**
     * //创建人工号
     */
    @NotBlank(message = "//创建人工号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "creator")
    private String creator;

    /**
     * 付款总金额
     */
    @NotNull(message = "付款总金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "paytotalje")
    private BigDecimal paytotalje;

    /**
     * 付款记录数
     */
    @NotNull(message = "付款记录数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "paytotalnum")
    private Integer paytotalnum;

    /**
     * 付款失败记录数
     */
    @NotNull(message = "付款失败记录数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "payfailnum")
    private Integer payfailnum;

    /**
     * 付款成功记录数
     */
    @NotNull(message = "付款成功记录数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "paysuccessnum")
    private Integer paysuccessnum;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 批次id
     */
    @NotNull(message = "批次id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "batchid")
    private Long batchid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "creater")
    private String creater;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "creatername")
    private String creatername;

}
