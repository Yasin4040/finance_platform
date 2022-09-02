package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 申请单 oa 审批日志记录
 * @TableName budget_extract_commission_application_log
 */
@TableName(value ="budget_extract_commission_application_log")
@Data
public class BudgetExtractCommissionApplicationLog implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申请单id
     */
    @TableField(value = "application_id")
    @ApiModelProperty(value = "申请单id")
    private Long applicationId;

    /**
     * 操作节点    枚举
     */
    @TableField(value = "node")
    @ApiModelProperty(value = "操作节点")
    private Integer node;

    /**
     * 操作状态  1 同意 2拒绝
     */
    @TableField(value = "status")
    @ApiModelProperty(value = "操作状态")
    private Integer status;

    /**
     * 备注 操作信息
     */
    @TableField(value = "remarks")
    @ApiModelProperty(value = "操作信息")
    private String remarks;

    /**
     * 操作人
     */
    @TableField(value = "create_by")
    @ApiModelProperty(value = "操作人")
    private String createBy;
    /**
     * 操作人名称
     */
    @TableField(value = "creator_name")
    @ApiModelProperty(value = "操作人名称")
    private String creatorName;

    /**
     * 操作时间
     */
    @ApiModelProperty(value = "操作时间")
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}