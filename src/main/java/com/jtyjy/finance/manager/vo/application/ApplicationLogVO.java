package com.jtyjy.finance.manager.vo.application;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/14.
 * Time: 10:07
 */
public class ApplicationLogVO {
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
     * 操作状态  （本系统操作）0已完成   1 同意 2退回
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
     * oa流程编号
     */
    @ApiModelProperty(value = "oa流程编号")
    @TableField(value = "request_code")
    private String request_code;

    /**
     * 流程id
     */
    @ApiModelProperty(value = "流程id")
    @TableField(value = "request_id")
    private String request_id;
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
}
