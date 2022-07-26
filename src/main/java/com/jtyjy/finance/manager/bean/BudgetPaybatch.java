package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_paybatch")
@Data
public class BudgetPaybatch implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键", required = false, hidden = false)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 付款方式 0：其它1：报销 2：提成3：现金4:项目付款
     */
    @ApiModelProperty(value = "付款方式 0：其它1：报销 2：提成3：现金4:项目付款", required = false, hidden = false)
    @TableField(value = "paybatchtype")
    private Integer paybatchtype;

    /**
     * 批次号
     */
    @ApiModelProperty(value = "批次号", required = false, hidden = false)
    @TableField(value = "paybatchcode")
    private String paybatchcode;

    /**
     * 多个付款单id
     */
    @ApiModelProperty(value = "多个付款单id", required = false, hidden = false)
    @TableField(value = "paymoneyids")
    private String paymoneyids;

    @ApiModelProperty(value = "创建时间", required = false, hidden = false)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 创建人的名称
     */
    @ApiModelProperty(value = "创建人的名称", required = false, hidden = false)
    @TableField(value = "creatorname")
    private String creatorname;

    /**
     * //创建人工号
     */
    @ApiModelProperty(value = "创建人工号", required = false, hidden = false)
    @TableField(value = "creator")
    private String creator;

    /**
     * 付款总金额
     */
    @ApiModelProperty(value = "付款总金额", required = false, hidden = false)
    @TableField(value = "paytotalje")
    private BigDecimal paytotalje;

    /**
     * 付款记录数
     */
    @ApiModelProperty(value = "付款记录数", required = false, hidden = false)
    @TableField(value = "paytotalnum")
    private Integer paytotalnum;

    /**
     * 付款失败记录数
     */
    @ApiModelProperty(value = "付款失败记录数", required = false, hidden = false)
    @ApiParam(hidden = true)
    @TableField(value = "payfailnum")
    private Integer payfailnum;

    /**
     * 付款失败付款单ids
     */
    @ApiModelProperty(value = "付款失败付款单ids", required = false, hidden = false)
    @TableField(value = "payfailids")
    private String payfailids;

    /**
     * 付款成功记录数
     */
    @ApiModelProperty(value = "付款成功记录数", required = false, hidden = false)
    @ApiParam(hidden = true)
    @TableField(value = "paysuccessnum")
    private Integer paysuccessnum;

    /**
     * 付款成功付款单ids
     */
    @ApiModelProperty(value = "付款成功付款单ids", required = false, hidden = false)
    @TableField(value = "paysuccessids")
    private String paysuccessids;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注", required = false, hidden = false)
    @TableField(value = "remark")
    private String remark;

    /**
     * 导出次数
     */
    @ApiModelProperty(value = "导出次数", required = false, hidden = false)
    @TableField(value = "exportcount")
    private Integer exportcount;

}
