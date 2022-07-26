package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_project_new")
@Data
public class BudgetProject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目id
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "projectno")
    private String projectno;

    /**
     * 项目名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "name")
    private String name;

    /**
     * 1 项目预领 2项目借支 3个人借支
     */
    @ApiParam(hidden = true)
    @TableField(value = "type")
    private Integer type;

    /**
     * 0 任务认领，1 特区政策，2 产品政策，3 直营信用借款、4年度功勋
     */
    @ApiParam(hidden = true)
    @TableField(value = "lendtype")
    private Integer lendtype;

    /**
     * 停用标识
     */
    @ApiParam(hidden = true)
    @TableField(value = "stopflag")
    private Boolean stopflag;

    /**
     * 界别id
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 部门id
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitids")
    private String unitids;

    /**
     * 单位名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitnames")
    private String unitnames;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 排序号
     */
    @ApiParam(hidden = true)
    @TableField(value = "orderno")
    private Integer orderno;

    /**
     * 确认状态1：是 0:否   （确认后信息不可修改，同时生成项目详情）
     */
    @ApiParam(hidden = true)
    @TableField(value = "confirmflag")
    private Integer confirmflag;

    /**
     * 确认时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "confirmtime")
    private Date confirmtime;

    /**
     * 确认人 工号
     */
    @ApiParam(hidden = true)
    @TableField(value = "confirmer")
    private String confirmer;

    /**
     * 确认人 姓名
     */
    @ApiParam(hidden = true)
    @TableField(value = "confirmername")
    private String confirmername;

    /**
     * 创建人工号
     */
    @ApiParam(hidden = true)
    @TableField(value = "creater")
    private String creater;

    /**
     * 创建人姓名
     */
    @ApiParam(hidden = true)
    @TableField(value = "creatername")
    private String creatername;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

}
