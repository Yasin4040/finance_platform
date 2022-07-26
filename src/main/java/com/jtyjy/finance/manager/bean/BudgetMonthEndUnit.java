package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_month_end_unit")
@Data
public class BudgetMonthEndUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 预算单位id
     */
    @NotNull(message = "预算单位id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 月份id
     */
    @NotNull(message = "月份id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 同步月度动因标识:true
     */
    @NotNull(message = "同步月度动因标识:true不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "syncagentflag")
    private Boolean syncagentflag;

    /**
     * 提交月度标识
     */
    @NotNull(message = "提交月度标识不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "submitflag")
    private Boolean submitflag;

    /**
     * 是否更新过动因标识 ,更新过修改 calculatesubjectflag 为true
     */
    @NotNull(message = "是否更新过动因标识 ,更新过修改 calculatesubjectflag 为true不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "updateagentflag")
    private Boolean updateagentflag;

    /**
     * 是否需要动因合并到科目标识，合并后 更新updateagentflag 的为 false
     */
    @NotNull(message = "是否需要动因合并到科目标识，合并后 更新updateagentflag 的为 false不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "calculatesubjectflag")
    private Boolean calculatesubjectflag;

    /**
     * 修改时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatetime;

    /**
     * 提交时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "submittime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submittime;

    /**
     * 提交者id
     */
    @ApiParam(hidden = true)
    @TableField(value = "submitorid")
    private String submitorid;

    /**
     * 提交者名字
     */
    @ApiParam(hidden = true)
    @TableField(value = "submitorname")
    private String submitorname;

    /**
     * 审核状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过
     */
    @NotNull(message = "审核状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "requeststatus")
    private Integer requeststatus;

    /**
     * 审核时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifytime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date verifytime;

    /**
     * 审核意见
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifystr")
    private String verifystr;

    /**
     * 审核人id
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifyorid")
    private String verifyorid;

    /**
     * 审核人名字
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifyorname")
    private String verifyorname;

    /**
     * 月结标识
     */
    @NotNull(message = "月结标识不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthendflag")
    private Boolean monthendflag;

    /**
     * 月结时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthendtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date monthendtime;

    /**
     * 上次月结时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "premonthendtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date premonthendtime;

    // --------------------------------------------------

    @TableField(exist = false)
    private String id;

    @TableField(exist = false)
    @ApiModelProperty(value = "届别名称")
    private String yearPeriod;

    @TableField(exist = false)
    @ApiModelProperty(value = "月份名称")
    private String monthPeriod;

    @TableField(exist = false)
    @ApiModelProperty(value = "预算单位名称")
    private String budgetUnitName;

}
