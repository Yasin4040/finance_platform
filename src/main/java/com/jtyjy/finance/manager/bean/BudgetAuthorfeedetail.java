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
@TableName(value = "budget_authorfeedetail")
@Data
public class BudgetAuthorfeedetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @NotNull(message = "主键id不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 届别
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearperiod")
    private String yearperiod;

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
    @TableField(value = "feemonth")
    private String feemonth;

    /**
     * 处理状态【-1:退回 0：未审核 1：已审核 2： 已计税 3： 已发放(数据字典)】
     */
    @ApiParam(hidden = true)
    @TableField(value = "status")
    private Integer status;

    /**
     * 汇总id
     */
    @ApiParam(hidden = true)
    @TableField(value = "authorfeesumid")
    private Long authorfeesumid;

    /**
     * 合并稿费id
     */
    @ApiParam(hidden = true)
    @TableField(value = "authormergeid")
    private Long authormergeid;

    /**
     * 作者类型（1：公司内部    0：公司外部）
     */
    @ApiParam(hidden = true)
    @TableField(value = "authortype")
    private Boolean authortype;

    /**
     * 作者id
     */
    @ApiParam(hidden = true)
    @TableField(value = "authorid")
    private Long authorid;

    /**
     * 作者名字/单位账户名（银行账户名）
     */
    @ApiParam(hidden = true)
    @TableField(value = "authorname")
    private String authorname;

    /**
     * 身份证号码（个人作者）
     */
    @ApiParam(hidden = true)
    @TableField(value = "authoridnumber")
    private String authoridnumber;

    /**
     * 纳税人识别号（单位作者）
     */
    @ApiParam(hidden = true)
    @TableField(value = "taxpayeridnumber")
    private String taxpayeridnumber;

    /**
     * 作者单位
     */
    @ApiParam(hidden = true)
    @TableField(value = "authorcompany")
    private String authorcompany;

    /**
     * 收款省
     */
    @ApiParam(hidden = true)
    @TableField(value = "authorprovince")
    private String authorprovince;

    /**
     * 收款市
     */
    @ApiParam(hidden = true)
    @TableField(value = "authorcity")
    private String authorcity;

    /**
     * 银行账号
     */
    @ApiParam(hidden = true)
    @TableField(value = "bankaccount")
    private String bankaccount;

    /**
     * 银行编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "bankaccountbranchcode")
    private String bankaccountbranchcode;

    /**
     * 银行名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "bankaccountbranchname")
    private String bankaccountbranchname;

    /**
     * 稿酬标准
     */
    @ApiParam(hidden = true)
    @TableField(value = "feestandard")
    private BigDecimal feestandard;

    /**
     * 应发稿酬
     */
    @ApiParam(hidden = true)
    @TableField(value = "copefee")
    private BigDecimal copefee;

    /**
     * 扣税类型（1：是    0： 否）
     */
    @ApiParam(hidden = true)
    @TableField(value = "taxtype")
    private Boolean taxtype;

    /**
     * 个人税费
     */
    @ApiParam(hidden = true)
    @TableField(value = "tax")
    private BigDecimal tax;

    /**
     * 导入次数
     */
    @ApiParam(hidden = true)
    @TableField(value = "times")
    private Integer times;

    /**
     * 月度动因id
     */
    @ApiParam(hidden = true)
    @TableField(value = "agentid")
    private Long agentid;

    /**
     * 月度动因名称（）根据产品II类查询出来的
     */
    @ApiParam(hidden = true)
    @TableField(value = "agentname")
    private String agentname;

    /**
     * 在提报部门下的预算科目id
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 报销科目
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimbursesubject")
    private String reimbursesubject;

    /**
     * 产品形态
     */
    @ApiParam(hidden = true)
    @TableField(value = "producttype")
    private String producttype;

    /**
     * 产品预算二类
     */
    @ApiParam(hidden = true)
    @TableField(value = "productbgtcls")
    private String productbgtcls;

    /**
     * 约稿老师id
     */
    @ApiParam(hidden = true)
    @TableField(value = "empid")
    private String empid;

    /**
     * 约稿老师工号
     */
    @ApiParam(hidden = true)
    @TableField(value = "empno")
    private String empno;

    /**
     * 约稿老师姓名
     */
    @ApiParam(hidden = true)
    @TableField(value = "empname")
    private String empname;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 学科
     */
    @ApiParam(hidden = true)
    @TableField(value = "subject")
    private String subject;

    /**
     * 邀稿内容及去向
     */
    @ApiParam(hidden = true)
    @TableField(value = "context")
    private String context;

    /**
     * 稿件质量
     */
    @ApiParam(hidden = true)
    @TableField(value = "paperquality")
    private String paperquality;

    /**
     * 页码或份数
     */
    @ApiParam(hidden = true)
    @TableField(value = "pageorcopy")
    private String pageorcopy;

    /**
     * 划拨部门id
     */
    @ApiParam(hidden = true)
    @TableField(value = "feebdgdeptid")
    private Long feebdgdeptid;

    /**
     * 稿费所属部门
     */
    @ApiParam(hidden = true)
    @TableField(value = "feebdgdept")
    private String feebdgdept;

    /**
     * 归属事业群
     */
    @ApiParam(hidden = true)
    @TableField(value = "businessgroup")
    private String businessgroup;

    @ApiParam(hidden = true)
    @TableField(value = "needzz")
    private Boolean needzz;

}
