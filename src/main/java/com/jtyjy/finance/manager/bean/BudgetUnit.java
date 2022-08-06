package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jtyjy.finance.manager.vo.BudgetUnitVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 预算单位表
 *
 * @author shubo
 */
@TableName(value = "budget_unit")
@Data
@ApiModel(description = "预算单位表")
public class BudgetUnit implements Serializable {

    private static final long serialVersionUID = 3717076330844756369L;

    /**
     * 主键id
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键id")
    private Long id;

    /**
     * 基础单位id
     */
    @NotNull(message = "基础单位id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "baseunitid")
    @ApiModelProperty(value = "基础单位id")
    private Long baseunitid;

    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "name")
    @ApiModelProperty(value = "名称")
    private String name;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    @ApiModelProperty(value = "届别id")
    private Long yearid;

    /**
     * 上级id
     */
    @NotNull(message = "上级id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "parentid")
    @ApiModelProperty(value = "上级id")
    private Long parentid;

    /**
     * 是否为预算体系0：否，1：是
     */
    @NotNull(message = "是否为预算体系0：否，1：是不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "budgetflag")
    @ApiModelProperty(value = "是否为预算体系0：否，1：是")
    private Boolean budgetflag;

    /**
     * 所有上级id
     */
    @ApiParam(hidden = true)
    @TableField(value = "pids")
    @ApiModelProperty(value = "树id")
    private String pids;

    /**
     * 多个预算管理员
     */
    @ApiParam(hidden = true)
    @TableField(value = "managers")
    @ApiModelProperty(value = "预算员")
    private String managers;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createtime;

    /**
     * 多个预算部门，不可和其它的重复
     */
    @ApiParam(hidden = true)
    @TableField(value = "budgetdepts")
    @ApiModelProperty(value = "预算部门")
    private String budgetdepts;

    /**
     * 多个预算人员，不可重复
     */
    @ApiParam(hidden = true)
    @TableField(value = "budgetusers")
    @ApiModelProperty(value = "人员")
    private String budgetusers;

    /**
     * 排序号
     */
    @ApiParam(hidden = true)
    @TableField(value = "orderno")
    @ApiModelProperty(value = "排序号")
    private Integer orderno;

    /**
     * 单位类型
     */
    @ApiParam(hidden = true)
    @TableField(value = "unittype")
    @ApiModelProperty(value = "单位类型")
    private Integer unittype;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 本届码洋占比公式
     */
    @ApiParam(hidden = true)
    @TableField(value = "ccratioformula")
    @ApiModelProperty(value = "本届码洋占比公式")
    private String ccratioformula;

    /**
     * 上届码洋占比公式
     */
    @ApiParam(hidden = true)
    @TableField(value = "preccratioformula")
    @ApiModelProperty(value = "上届码洋占比公式")
    private String preccratioformula;

    /**
     * 本届收入占比公式
     */
    @ApiParam(hidden = true)
    @TableField(value = "revenueformula")
    @ApiModelProperty(value = "本届收入占比公式")
    private String revenueformula;

    /**
     * 是否更新过动因标识 ,更新过修改 calculatesubjectflag 为true
     */
    @ApiParam(hidden = true)
    @TableField(value = "updateagentflag")
    @ApiModelProperty(value = "是否更新过动因标识 ,更新过修改 calculatesubjectflag 为true")
    private Boolean updateagentflag;

    /**
     * 是否需要动因合并到科目标识，合并后 更新updateagentflag 的为 false
     */
    @ApiParam(hidden = true)
    @TableField(value = "calculatesubjectflag")
    @ApiModelProperty(value = "是否需要动因合并到科目标识，合并后 更新updateagentflag 的为 false")
    private Boolean calculatesubjectflag;

    /**
     * 提交年度预算标识
     */
    @ApiParam(hidden = true)
    @TableField(value = "submitflag")
    @ApiModelProperty(value = "提交年度预算标识")
    private Boolean submitflag;

    /**
     * 提交时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "submittime")
    @ApiModelProperty(value = "提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submittime;

    /**
     * 提交者id
     */
    @ApiParam(hidden = true)
    @TableField(value = "submitorid")
    @ApiModelProperty(value = "提交者id")
    private String submitorid;

    /**
     * 提交者名字
     */
    @ApiParam(hidden = true)
    @TableField(value = "submitorname")
    @ApiModelProperty(value = "提交者名字")
    private String submitorname;

    /**
     * 更新时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatetime;

    /**
     * 审核状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过
     */
    @NotNull(message = "审核状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "requeststatus")
    @ApiModelProperty(value = "审核状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过")
    private Integer requeststatus;

    /**
     * 审核时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifytime")
    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date verifytime;

    /**
     * 审核意见
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifystr")
    @ApiModelProperty(value = "审核意见")
    private String verifystr;

    /**
     * 审核人id
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifyorid")
    @ApiModelProperty(value = "审核人id")
    private String verifyorid;

    /**
     * 审核人名字
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifyorname")
    @ApiModelProperty(value = "审核人名字")
    private String verifyorname;

    @ApiModelProperty(value="传工号，预算责任人，多个以逗号分隔。")
    @TableField(value = "budget_responsibilities")
    private String budgetResponsibilities;

    // --------------------------------------------------

    /**
     * 届别名称
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "届别名称")
    private String yearPeriod;

    public BudgetUnit() {
    }

    public BudgetUnit(BudgetUnitVO vo) {
        this.id = vo.getId();
        this.name = vo.getName();
        this.baseunitid = vo.getBaseUnitId();
        this.unittype = vo.getUnitType();
        this.budgetflag = 0 != vo.getBudgetFlag();
        this.yearid = vo.getYearId();
        this.managers = vo.getManagers();
        this.budgetdepts = vo.getBudgetDepts();
        this.budgetusers = vo.getBudgetUsers();
        this.ccratioformula = vo.getCcratioFormula();
        this.revenueformula = vo.getRevenueFormula();
        this.orderno = vo.getOrderNo();
        this.remark = vo.getRemark();
        this.budgetResponsibilities = vo.getBudgetResponsibilities();
    }
}
