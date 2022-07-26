package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_year_agentlend")
@Data
public class BudgetYearAgentlend implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @NotNull(message = "主键Id不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 拆借单号
     */
    @ApiParam(hidden = true)
    @TableField(value = "ordernumber")
    private String ordernumber;

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
    @TableField(value = "inunitid")
    private Long inunitid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "outunitid")
    private Long outunitid;

    /**
     * 拆出科目名
     */
    @ApiParam(hidden = true)
    @TableField(value = "outsubjectname")
    private String outsubjectname;

    /**
     * 拆出科目id
     */
    @ApiParam(hidden = true)
    @TableField(value = "outsubjectid")
    private Long outsubjectid;

    /**
     * 拆进科目名
     */
    @ApiParam(hidden = true)
    @TableField(value = "insubjectname")
    private String insubjectname;

    /**
     * 拆进科目id
     */
    @NotNull(message = "拆进科目id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "insubjectid")
    private Long insubjectid;

    /**
     * 拆出动因id
     */
    @ApiParam(hidden = true)
    @TableField(value = "outyearagentid")
    private Long outyearagentid;

    /**
     * 拆出动因名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "outname")
    private String outname;

    /**
     * 拆进动因id
     */
    @ApiParam(hidden = true)
    @TableField(value = "inyearagentid")
    private Long inyearagentid;

    /**
     * 拆进动因名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "inname")
    private String inname;

    /**
     * 拆进金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "total")
    private BigDecimal total;

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
     * 拆借原因
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 拆借状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过
     */
    @ApiParam(hidden = true)
    @TableField(value = "requeststatus")
    private Integer requeststatus;

    /**
     * 申请人id
     */
    @NotBlank(message = "申请人id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "creatorid")
    private String creatorid;

    /**
     * 申请人名字
     */
    @ApiParam(hidden = true)
    @TableField(value = "creatorname")
    private String creatorname;

    /**
     * 更新时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 创建时间
     */
    @NotNull(message = "创建时间不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 是否免罚 0 否 1是
     */
    @TableField(value = "is_exempt_fine")
    private Boolean isExemptFine;

    /**
     * 免罚原因
     */
    @TableField(value = "exempt_fine_reason")
    private String exemptFineReason;


    /**
     * 拆借前动因年度预算金额（年初预算）
     */
    @NotNull(message = "拆借前动因年度预算金额（年初预算）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "outagentmoney")
    private BigDecimal outagentmoney;

    /**
     * 拆借前动因累计追加金额
     */
    @NotNull(message = "拆借前动因累计追加金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "outagentaddmoney")
    private BigDecimal outagentaddmoney;

    /**
     * 拆借前拆出金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "拆借前拆出金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "outagentlendoutmoney")
    private BigDecimal outagentlendoutmoney;

    /**
     * 拆借前拆进金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "拆借前拆进金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "outagentlendinmoney")
    private BigDecimal outagentlendinmoney;

    /**
     * 拆借前动因累计执行金额
     */
    @NotNull(message = "拆借前动因累计执行金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "outagentexcutemoney")
    private BigDecimal outagentexcutemoney;

    /**
     * 拆借前动因年度预算金额（年初预算
     */
    @NotNull(message = "拆借前动因年度预算金额（年初预算不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "inagentmoney")
    private BigDecimal inagentmoney;

    /**
     * 拆借前动因累计追加金额
     */
    @NotNull(message = "拆借前动因累计追加金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "inagentaddmoney")
    private BigDecimal inagentaddmoney;

    /**
     * 拆借前拆出金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "拆借前拆出金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "inagentlendoutmoney")
    private BigDecimal inagentlendoutmoney;

    /**
     * 拆借前拆进金额（同科目里面的动因可以拆借）
     */
    @NotNull(message = "拆借前拆进金额（同科目里面的动因可以拆借）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "inagentlendinmoney")
    private BigDecimal inagentlendinmoney;

    /**
     * 拆借前动因累计执行金额
     */
    @NotNull(message = "拆借前动因累计执行金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "inagentexcutemoney")
    private BigDecimal inagentexcutemoney;

    /**
     * 审核时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "audittime")
    private Date audittime;

    /**
     * 处理状态  true :已处理，false：未处理（默认）
     */
    @NotNull(message = "处理状态  true :已处理，false：未处理（默认）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "handleflag")
    private Boolean handleflag;

    /**
     * 是否删除 ：false true
     */
    @NotNull(message = "是否删除 ：false true不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "deleteflag")
    private Boolean deleteflag;

    /**
     * 附件地址
     */
    @ApiParam(hidden = true)
    @TableField(value = "fileurl")
    private String fileurl;

    /**
     * 附件原名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "fileoriginname")
    private String fileoriginname;

    /**
     * oa密码。上传附件时需使用
     */
    @ApiParam(hidden = true)
    @TableField(value = "oapassword")
    private String oapassword;

}
