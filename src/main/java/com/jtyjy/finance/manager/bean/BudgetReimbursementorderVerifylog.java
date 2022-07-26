package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_reimbursementorder_verifylog")
@Data
public class BudgetReimbursementorderVerifylog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "报销单主键【修改时必填】", hidden = false, required = false)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报销单编号
     */
    @ApiModelProperty(value = "报销单编号", hidden = false, required = false)
    @TableField(value = "reimcode")
    private String reimcode;

    /**
     * 报销单id
     */
    @ApiModelProperty(value = "报销单主键】", hidden = false, required = false)
    @TableField(value = "reimbursementid")
    private Long reimbursementid;

    /**
     * 审核时间
     */
    @ApiModelProperty(value = "审核时间", hidden = false, required = false)
    @TableField(value = "verifytime")
    private Date verifytime;

    /**
     * 审核人姓名
     */
    @ApiModelProperty(value = "审核人姓名", hidden = false, required = false)
    @TableField(value = "verifyername")
    private String verifyername;

    /**
     * 审核人
     */
    @ApiModelProperty(value = "审核人", hidden = false, required = false)
    @TableField(value = "verifyer")
    private String verifyer;

    /**
     * 1：单据接收，2：票面审核，3：预算审核，4：扫描分单
     */
    @ApiModelProperty(value = " 1：单据接收，2：票面审核，3：预算审核，4：扫描分单", hidden = false, required = false)
    @TableField(value = "verifytype")
    private Integer verifytype;

    /**
     * 0:退回 1：通过
     */
    @ApiModelProperty(value = "审核状态【0:退回 1：通过】", hidden = false, required = false)
    @TableField(value = "verifyflag")
    private Boolean verifyflag;

    /**
     * 1:退回纸质，2：全部退回
     */
    @ApiModelProperty(value = "退回状态【1:退回纸质，2：全部退回】", hidden = false, required = false)
    @TableField(value = "backtype")
    private Integer backtype;

    /**
     * 审核前的状态
     */
    @ApiModelProperty(value = "审核前的状态", hidden = false, required = false)
    @TableField(value = "bstatus")
    private Integer bstatus;

    /**
     * 审核后的状态
     */
    @ApiModelProperty(value = "审核后的状态", hidden = false, required = false)
    @TableField(value = "astatus")
    private Integer astatus;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "审核信息", hidden = false, required = false)
    @TableField(value = "verifyinfo")
    private String verifyinfo;

}
