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
@TableName(value = "budget_year_erporder")
@Data
public class BudgetYearErporder implements Serializable {

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
     * 管理费
     */
    @ApiParam(hidden = true)
    @TableField(value = "managefee")
    private BigDecimal managefee;

    /**
     * erp订单时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "erpordertime")
    private Date erpordertime;

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
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

}
