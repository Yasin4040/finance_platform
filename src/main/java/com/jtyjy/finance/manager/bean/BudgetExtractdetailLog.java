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
@TableName(value = "budget_extractdetail_log")
@Data
public class BudgetExtractdetailLog implements Serializable {

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
    @TableField(value = "extractdetailid")
    private Long extractdetailid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "empno")
    private String empno;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "empname")
    private String empname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "beforecopeetract")
    private BigDecimal beforecopeetract;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "beforeconsotax")
    private BigDecimal beforeconsotax;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "aftercopeetract")
    private BigDecimal aftercopeetract;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "afterconsotax")
    private BigDecimal afterconsotax;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "operator")
    private String operator;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "operatorname")
    private String operatorname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "operatime")
    private Date operatime;

}
