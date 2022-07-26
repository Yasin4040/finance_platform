package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "budget_report_base_unit")
@Data
public class BudgetReportBaseUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 届别名称
     */
    @NotBlank(message = "届别名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearname")
    private String yearname;

    /**
     * 预算单位id
     */
    @NotNull(message = "预算单位id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 预算单位名称
     */
    @NotBlank(message = "预算单位名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unitname")
    private String unitname;

    /**
     * 预算单位树
     */
    @NotBlank(message = "预算单位树不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unittreename")
    private String unittreename;

    /**
     * 层级
     */
    @ApiParam(hidden = true)
    @TableField(value = "level")
    private Integer level;

    /**
     * 叶子节点（默认1）
     */
    @ApiParam(hidden = true)
    @TableField(value = "leafflag")
    private Boolean leafflag;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "pid")
    private Long pid;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "pids")
    private String pids;

    /**
     * 排序号
     */
    @NotNull(message = "排序号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "orderno")
    private Integer orderno;

    /**
     * 基础单位id
     */
    @ApiParam(hidden = true)
    @TableField(value = "baseunitid")
    private Long baseunitid;

}
