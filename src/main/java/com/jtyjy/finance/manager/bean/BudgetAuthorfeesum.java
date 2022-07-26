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
@TableName(value = "budget_authorfeesum")
@Data
public class BudgetAuthorfeesum implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @NotNull(message = "主键id不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 稿酬编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "code")
    private String code;

    /**
     * 处理状态【-1:退回 0：草稿 1:审核中  2：已审核  3： 已计税  4：已生成发放(数据字典)】
     */
    @ApiParam(hidden = true)
    @TableField(value = "status")
    private Integer status;

    /**
     * 报销人id
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimid")
    private String reimid;

    /**
     * 报销人工号
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimno")
    private String reimno;

    /**
     * 报销人姓名
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimname")
    private String reimname;

    /**
     * 提报部门id
     */
    @ApiParam(hidden = true)
    @TableField(value = "feedeptid")
    private Long feedeptid;

    /**
     * 提报部门
     */
    @ApiParam(hidden = true)
    @TableField(value = "feedeptname")
    private String feedeptname;

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
    @TableField(value = "yearperiod")
    private String yearperiod;

    /**
     * 归属月份id
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 稿酬归属月份
     */
    @ApiParam(hidden = true)
    @TableField(value = "feemonth")
    private String feemonth;

    /**
     * 报销月份（系统生成）
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimbursemonth")
    private String reimbursemonth;

    /**
     * 稿费数量
     */
    @ApiParam(hidden = true)
    @TableField(value = "authorfeenum")
    private Integer authorfeenum;

    /**
     * 【稿费】科目总额
     */
    @ApiParam(hidden = true)
    @TableField(value = "contributionfee")
    private BigDecimal contributionfee;

    /**
     * 【外审外包费】科目总额
     */
    @ApiParam(hidden = true)
    @TableField(value = "externalauditfee")
    private BigDecimal externalauditfee;

    @ApiParam(hidden = true)
    @TableField(value = "contributionfeenext")
    private BigDecimal contributionfeenext;

    /**
     * 【外审外包费】科目总额
     */
    @ApiParam(hidden = true)
    @TableField(value = "externalauditfeenext")
    private BigDecimal externalauditfeenext;

    /**
     * 计税总额
     */
    @ApiParam(hidden = true)
    @TableField(value = "needtaxtotal")
    private BigDecimal needtaxtotal;

    /**
     * 不计税总额
     */
    @ApiParam(hidden = true)
    @TableField(value = "noneedtaxtotal")
    private BigDecimal noneedtaxtotal;

    /**
     * gasga
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimburseflag")
    private Boolean reimburseflag;

    /**
     * 删除标识（1：是   0：否）
     */
    @ApiParam(hidden = true)
    @TableField(value = "deleteflag")
    private Boolean deleteflag;

    /**
     * 导入次数
     */
    @ApiParam(hidden = true)
    @TableField(value = "times")
    private Integer times;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 创建人Id
     */
    @ApiParam(hidden = true)
    @TableField(value = "creatorid")
    private String creatorid;

    /**
     * 创建者
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
     * 审批意见
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

}
