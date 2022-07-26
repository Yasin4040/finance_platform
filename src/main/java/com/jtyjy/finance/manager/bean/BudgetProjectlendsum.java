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
@TableName(value = "budget_projectlendsum_new")
@Data
public class BudgetProjectlendsum implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目id
     */
    @ApiParam(hidden = true)
    @TableField(value = "projectid")
    private Long projectid;

    /**
     * 项目编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "projectno")
    private String projectno;

    /**
     * 项目名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "projectname")
    private String projectname;

    /**
     * 1项目预领 2项目借支
     */
    @ApiParam(hidden = true)
    @TableField(value = "type")
    private Integer type;

    /**
     * 审核状态 0未审核 1已审核
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifyflag")
    private Integer verifyflag;

    /**
     * 创建人
     */
    @ApiParam(hidden = true)
    @TableField(value = "creator")
    private String creator;

    /**
     * 创建人的名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "creatorname")
    private String creatorname;

    /**
     * 转账付款单位Id
     */
    @ApiParam(hidden = true)
    @TableField(value = "paymoneyunitid")
    private Long paymoneyunitid;

    /**
     * 导入时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 审核人
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifyorid")
    private String verifyorid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifyname")
    private String verifyname;

    /**
     * 审核时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifytime")
    private Date verifytime;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 提交报销状态(0:草稿,1为已提交)
     */
    @ApiParam(hidden = true)
    @TableField(value = "submitbxstatus")
    private Integer submitbxstatus;

    /**
     * 报销人(id)
     */
    @ApiParam(hidden = true)
    @TableField(value = "bxuserid")
    private String bxuserid;

    /**
     * 报销人姓名
     */
    @ApiParam(hidden = true)
    @TableField(value = "bxusername")
    private String bxusername;

    /**
     * 报销单id
     */
    @ApiParam(hidden = true)
    @TableField(value = "bxorderid")
    private Long bxorderid;

    /**
     * 报销日期
     */
    @ApiParam(hidden = true)
    @TableField(value = "bxdate")
    private Date bxdate;

    /**
     * 报销月份
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 提交报销人(id)
     */
    @ApiParam(hidden = true)
    @TableField(value = "submitorid")
    private String submitorid;

    /**
     * 提交报销人姓名
     */
    @ApiParam(hidden = true)
    @TableField(value = "submitorname")
    private String submitorname;

    /**
     * 现金
     */
    @ApiParam(hidden = true)
    @TableField(value = "cashmoney")
    private BigDecimal cashmoney;

    /**
     * 转账
     */
    @ApiParam(hidden = true)
    @TableField(value = "transfermoney")
    private BigDecimal transfermoney;

    /**
     * 转账
     */
    @ApiParam(hidden = true)
    @TableField(value = "giftmoney")
    private BigDecimal giftmoney;

    /**
     * 总金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "total")
    private BigDecimal total;

}
