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
@TableName(value = "budget_year_erpproject")
@Data
public class BudgetYearErpproject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 届别id
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "erporderid")
    private Long erporderid;

    /**
     * 预算单位id
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * erp订单编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "erporderno")
    private String erporderno;

    /**
     * erp订单名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "erpordername")
    private String erpordername;

    /**
     * erp订单金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "erporderje")
    private BigDecimal erporderje;

    /**
     * erp订单时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "erpordertime")
    private Date erpordertime;

    /**
     * oa流程创建人id(oa系统id)
     */
    @ApiParam(hidden = true)
    @TableField(value = "oacreatorid")
    private String oacreatorid;

    /**
     * 流程id
     */
    @ApiParam(hidden = true)
    @TableField(value = "requestid")
    private String requestid;

    /**
     * 追加状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过
     */
    @ApiParam(hidden = true)
    @TableField(value = "requeststatus")
    private Integer requeststatus;

    /**
     * 审核时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "audittime")
    private Date audittime;

    /**
     * 处理状态  true :已处理，false：未处理（默认）
     */
    @ApiParam(hidden = true)
    @TableField(value = "handleflag")
    private Boolean handleflag;

    /**
     * 删除标识 ：  0 否   1 是
     */
    @ApiParam(hidden = true)
    @TableField(value = "deleteflag")
    private Boolean deleteflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 创建者id
     */
    @ApiParam(hidden = true)
    @TableField(value = "creatorid")
    private String creatorid;

    /**
     * 创建者名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "creatorname")
    private String creatorname;

    /**
     * 项目订单负责人
     */
    @ApiParam(hidden = true)
    @TableField(value = "leaderid")
    private String leaderid;

    /**
     * 负责人姓名
     */
    @ApiParam(hidden = true)
    @TableField(value = "leadername")
    private String leadername;

    /**
     * 项目人数
     */
    @ApiParam(hidden = true)
    @TableField(value = "projectrs")
    private Integer projectrs;

}
