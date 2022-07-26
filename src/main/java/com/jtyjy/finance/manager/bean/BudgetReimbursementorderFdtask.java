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
@TableName(value = "budget_reimbursementorder_fdtask")
@Data
public class BudgetReimbursementorderFdtask implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(required = false, hidden = false, value = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报销单号
     */
    @ApiModelProperty(required = false, hidden = false, value = "报销单号")
    @TableField(value = "reimcode")
    private String reimcode;

    /**
     * 报销单id
     */
    @ApiModelProperty(required = false, hidden = false, value = "报销单id")
    @TableField(value = "reimbursementid")
    private Long reimbursementid;

    /**
     * 分单人工号
     */
    @ApiModelProperty(required = false, hidden = false, value = "分单人工号")
    @TableField(value = "fder")
    private String fder;

    /**
     * 分单人姓名
     */
    @ApiModelProperty(required = false, hidden = false, value = "分单人姓名")
    @TableField(value = "fdername")
    private String fdername;

    /**
     * 分单时间
     */
    @ApiModelProperty(required = false, hidden = false, value = "分单时间")
    @TableField(value = "fdtime")
    private Date fdtime;

    /**
     * 分单开票单位id
     */
    @ApiModelProperty(required = false, hidden = false, value = "分单开票单位id")
    @TableField(value = "bunitid")
    private Long bunitid;

    /**
     * 分单开票单位名称
     */
    @ApiModelProperty(required = false, hidden = false, value = "分单开票单位名称")
    @TableField(value = "bunitname")
    private String bunitname;

    /**
     * 创建时间
     */
    @ApiModelProperty(required = false, hidden = false, value = "创建时间")
    @TableField(value = "createtime")
    private Date createtime;

}
