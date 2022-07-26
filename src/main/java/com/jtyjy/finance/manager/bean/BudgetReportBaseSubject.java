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
@TableName(value = "budget_report_base_subject")
@Data
public class BudgetReportBaseSubject implements Serializable {

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
     * 预算科目id
     */
    @NotNull(message = "预算科目id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 预算科目名称
     */
    @NotBlank(message = "预算科目名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjectname")
    private String subjectname;

    /**
     * 数据科目树形名字
     */
    @NotBlank(message = "数据科目树形名字不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjecttrrname")
    private String subjecttrrname;

    /**
     * 首拼
     */
    @NotBlank(message = "首拼不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "firstspell")
    private String firstspell;

    /**
     * 全拼
     */
    @NotBlank(message = "全拼不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "fullspell")
    private String fullspell;

    /**
     * 预算科目代码
     */
    @NotBlank(message = "预算科目代码不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjectcode")
    private String subjectcode;

    /**
     * 层级 默认为1
     */
    @NotNull(message = "层级 默认为1不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "level")
    private Integer level;

    /**
     * 叶子节点标识默认为1
     */
    @NotNull(message = "叶子节点标识默认为1不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "leafflag")
    private Boolean leafflag;

    /**
     * 父节点id
     */
    @NotNull(message = "父节点id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "pid")
    private Long pid;

    /**
     * 所有上级id,用"-"隔开
     */
    @NotBlank(message = "所有上级id,用'-'隔开不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "pids")
    private String pids;

    /**
     * 辅助性指标标识 true:是，false:否
     */
    @ApiParam(hidden = true)
    @TableField(value = "assistflag")
    private Boolean assistflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "upsumflag")
    private Boolean upsumflag;

    /**
     * 费用分解 true:是，false:否
     */
    @ApiParam(hidden = true)
    @TableField(value = "costsplitflag")
    private Boolean costsplitflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "costaddflag")
    private Boolean costaddflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "costlendflag")
    private Boolean costlendflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "jointproductflag")
    private Boolean jointproductflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearplantype")
    private Integer yearplantype;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "formulaflag")
    private Boolean formulaflag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "formula")
    private String formula;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "formulaorderno")
    private Integer formulaorderno;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "procategoryid")
    private String procategoryid;

    /**
     * 排序号
     */
    @NotNull(message = "排序号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "orderno")
    private Long orderno;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "basesubjectid")
    private Long basesubjectid;

}
