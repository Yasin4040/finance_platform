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
@TableName(value = "budget_report_monthsubjectsum")
@Data
public class BudgetReportMonthsubjectsum implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 届别id
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 届别
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearname")
    private String yearname;

    /**
     * 月份id
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 月份
     */
    @ApiParam(hidden = true)
    @TableField(value = "month")
    private String month;

    /**
     * 预算单位id
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 预算单位
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitname")
    private String unitname;

    /**
     * 预算科目id
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 预算科目
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectname")
    private String subjectname;

    /**
     * 科目代码
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectcode")
    private String subjectcode;

    /**
     * 层级
     */
    @ApiParam(hidden = true)
    @TableField(value = "level")
    private Integer level;

    /**
     * 追加次数
     */
    @ApiParam(hidden = true)
    @TableField(value = "addcount")
    private Integer addcount;

    /**
     * 报销次数
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimcount")
    private Integer reimcount;

    /**
     * 追加总金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "addmoney")
    private BigDecimal addmoney;

    /**
     * 执行总金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "executemoney")
    private BigDecimal executemoney;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 更新时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

}
