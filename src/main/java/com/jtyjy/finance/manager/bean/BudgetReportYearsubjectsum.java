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
@TableName(value = "budget_report_yearsubjectsum")
@Data
public class BudgetReportYearsubjectsum implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

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
    @TableField(value = "yearname")
    private String yearname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitname")
    private String unitname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectname")
    private String subjectname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "level")
    private Integer level;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "addcount")
    private Integer addcount;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "lendincount")
    private Integer lendincount;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "lendoutcount")
    private Integer lendoutcount;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "addmoney")
    private BigDecimal addmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "lendinmoney")
    private BigDecimal lendinmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "lendoutmoney")
    private BigDecimal lendoutmoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "executemoney")
    private BigDecimal executemoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

}
