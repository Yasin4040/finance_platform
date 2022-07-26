package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 员工借款表
 */
@TableName(value = "budget_lendmoney_new")
@Data
public class BudgetLendmoney implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @NotNull(message = "主键Id不能为空")
    @ApiModelProperty(value = "主键Id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * （借款人）员工id 外部人员为空
     */
    @ApiModelProperty(value = "（借款人）员工id 外部人员为空")
    @TableField(value = "empid")
    private String empid;

    /**
     * （借款人）员工工号 外部人为银行账户编号
     */
    @NotBlank(message = "（借款人）员工工号 外部人为银行账户编号不能为空")
    @ApiModelProperty(value = "（借款人）员工工号 外部人为银行账户编号")
    @TableField(value = "empno")
    private String empno;

    /**
     * （借款人）员工姓名 外部人为银行账户户名
     */
    @NotBlank(message = "（借款人）员工姓名 外部人为银行账户户名不能为空")
    @ApiModelProperty(value = "（借款人）员工姓名 外部人为银行账户户名")
    @TableField(value = "empname")
    private String empname;

    /**
     * （借款人）部门id 外部人员为空
     */
    @ApiModelProperty(value = "（借款人）部门id 外部人员为空")
    @TableField(value = "deptid")
    private String deptid;

    /**
     * （借款人）部门名称 外部人员为空
     */
    @ApiModelProperty(value = "（借款人）部门名称 外部人员为空")
    @TableField(value = "deptname")
    private String deptname;

    /**
     * （经办人）员工id
     */
    @ApiModelProperty(value = "（经办人）员工id")
    @TableField(value = "_empid")
    private String operatorEmpId;

    /**
     * （经办人）工号
     */
    @ApiModelProperty(value = "（经办人）工号   ")
    @TableField(value = "_empno")
    private String operatorEmpNo;

    /**
     * （经办人）姓名
     */
    @ApiModelProperty(value = "（经办人）姓名 ")
    @TableField(value = "_empname")
    private String operatorEmpName;

    /**
     * 届别Id
     */
    @ApiModelProperty(value = "届别Id")
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 借款单号
     */
    @NotBlank(message = "借款单号不能为空")
    @ApiModelProperty(value = "借款单号")
    @TableField(value = "lendmoneycode")
    private String lendmoneycode;

    /**
     * 是否备用金
     */
    @ApiModelProperty(value = "是否备用金")
    @TableField(value = "isbyj")
    private Boolean isbyj;

    /**
     * 11：个人借款 12：费用借款 13：销售政策支持借款申请 14：备用金借款   15: 合同借款    16：非合同借款
     */
    @NotNull(message = "11：个人借款 12：费用借款 13：销售政策支持借款申请 14：备用金借款   15: 合同借款    16：非合同借款")
    @ApiModelProperty(value = "11：个人借款 12：费用借款 13：销售政策支持借款申请 14：备用金借款   15: 合同借款    16：非合同借款")
    @TableField(value = "lendtype")
    private Integer lendtype;

    /**
     * 个人借款的细分类型（1 非公借款、2 项目实施成本、3 投标保证金、4 投标服务费）
     */
    @ApiModelProperty(value = "个人借款的细分类型（1 非公借款、2 项目实施成本、3 投标保证金、4 投标服务费）")
    @TableField(value = "personaltype")
    private Integer personaltype;

    /**
     * 借款金额（2位有效数字）(本金)
     */
    @NotNull(message = "借款金额（2位有效数字）(本金)不能为空")
    @ApiModelProperty(value = "借款金额（2位有效数字）(本金)")
    @TableField(value = "lendmoney")
    private BigDecimal lendmoney;

    /**
     * 已还金额（利息）
     */
    @ApiModelProperty(value = "已还金额（利息）")
    @TableField(value = "repaidinterestmoney")
    private BigDecimal repaidinterestmoney;

    /**
     * 已还金额（2位有效数字）
     */
    @NotNull(message = "已还金额（2位有效数字）不能为空")
    @ApiModelProperty(value = "已还金额（2位有效数字）")
    @TableField(value = "repaidmoney")
    private BigDecimal repaidmoney;

    /**
     * 借款时间(到天)
     */
    @NotNull(message = "借款时间(到天)不能为空")
    @ApiModelProperty(value = "借款时间(到天)")
    @TableField(value = "lenddate")
    private Date lenddate;

    /**
     * 计划还款日期
     */
    @ApiModelProperty(value = "计划还款日期")
    @TableField(value = "planpaydate")
    private Date planpaydate;

//    /**
//     * 付款单id
//     */
//    @ApiModelProperty(value = "付款单id")
//    @TableField(value = "paymoneyid")
//    private Long paymoneyid;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 借款事由
     */
    @ApiModelProperty(value = "借款事由")
    @TableField(value = "remark")
    private String remark;

    /**
     * oa流程编号
     */
    @ApiModelProperty(value = "oa流程编号")
    @TableField(value = "requestcode")
    private String requestcode;

    /**
     * 流程id
     */
    @ApiModelProperty(value = "流程id")
    @TableField(value = "requestid")
    private String requestid;

    /**
     * oa流程信息（表单）
     */
    @ApiModelProperty(value = "oa流程信息（表单）")
    @TableField(value = "requestinfo")
    private String requestinfo;

    /**
     * 项目借款确认状态 true:表示确认 项目借款确认状态 true:表示确认  项目借款员工确认后才能生效
     */
    @ApiModelProperty(value = "项目借款确认状态 true:表示确认 项目借款确认状态 true:表示确认  项目借款员工确认后才能生效")
    @TableField(value = "confirmflag")
    private Boolean confirmflag;

    /**
     * 项目借款确认时间
     */
    @ApiModelProperty(value = "项目借款确认时间")
    @TableField(value = "confirmtime")
    private Date confirmtime;

    /**
     * 删除状态（true：表示已经删除）
     */
    @ApiModelProperty(value = "删除状态（true：表示已经删除）")
    @TableField(value = "deleteflag")
    private Boolean deleteflag;

    /**
     * 删除时间
     */
    @ApiModelProperty(value = "删除时间")
    @TableField(value = "deletetime")
    private Date deletetime;

    /**
     * 做账标志(适用于临时、其它、日常借款。TRUE表示已做账)
     */
    @ApiModelProperty(value = "做账标志(适用于临时、其它、日常借款。TRUE表示已做账)")
    @TableField(value = "makeaccountflag")
    private Boolean makeaccountflag;

    /**
     * 合同id
     */
    @ApiModelProperty(value = "合同id")
    @TableField(value = "contractid")
    private Long contractid;

    /**
     * 当时借款公司的利率（千分之多少）记得要除1000
     */
    @ApiModelProperty(value = "当时借款公司的利率（千分之多少）记得要除1000")
    @TableField(value = "interestrate")
    private Integer interestrate;

    /**
     * 产生的利息 （每天计算利息，不是利滚利）
     */
    @NotNull(message = "产生的利息 （每天计算利息，不是利滚利）不能为空")
    @ApiModelProperty(value = "产生的利息 （每天计算利息，不是利滚利）")
    @TableField(value = "interestmoney")
    private BigDecimal interestmoney;

    /**
     * 是否可以还款(自发还款、工资还款、提成还款 ====不还利息)
     */
    @ApiModelProperty(value = "是否可以还款(自发还款、工资还款、提成还款 ====不还利息) ")
    @TableField(value = "chargebillflag")
    private Boolean chargebillflag;

    /**
     * 自发还款设置人工号
     */
    @ApiModelProperty(value = "自发还款设置人工号")
    @TableField(value = "chargebillor")
    private String chargebillor;

    /**
     * 自发还款设置人姓名
     */
    @ApiModelProperty(value = "自发还款设置人姓名")
    @TableField(value = "chargebillorname")
    private String chargebillorname;

    /**
     * 自发还款设置时间
     */
    @ApiModelProperty(value = "自发还款设置时间")
    @TableField(value = "chargebilltime")
    private Date chargebilltime;

    /**
     * 达标状态,true为达标
     */
    @ApiModelProperty(value = "达标状态,true为达标")
    @TableField(value = "flushingflag")
    private Boolean flushingflag;

    /**
     * 达标设置人工号
     */
    @ApiModelProperty(value = "达标设置人工号")
    @TableField(value = "flushingor")
    private String flushingor;

    /**
     * 达标设置人姓名
     */
    @ApiModelProperty(value = "达标设置人姓名")
    @TableField(value = "flushingorname")
    private String flushingorname;

    /**
     * 达标设置时间
     */
    @ApiModelProperty(value = "达标设置时间")
    @TableField(value = "flushtime")
    private Date flushtime;

    /**
     * 生效标识 true:为生效
     */
    @ApiModelProperty(value = "生效标识 true:为生效")
    @TableField(value = "effectflag")
    private Boolean effectflag;

    /**
     * 项目借款主表id
     */
    @ApiModelProperty(value = "项目借款主表id")
    @TableField(value = "projectlendsumid")
    private Long projectlendsumid;

    /**
     * 日常借款单
     */
    @ApiModelProperty(value = "日常借款单")
    @TableField(value = "dailylendid")
    private Long dailylendid;

    /**
     * 项目借款类型 1：现金（关联日常借款）;2：转账；3：礼品（关联项目借款）
     */
    @ApiModelProperty(value = "项目借款类型 1：现金（关联日常借款）;2：转账；3：礼品（关联项目借款）")
    @TableField(value = "projectlendtype")
    private String projectlendtype;

    /**
     * 合同借款单id
     */
    @ApiModelProperty(value = "合同借款单id")
    @TableField(value = "contractlendid")
    private Long contractlendid;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "未知参数")
    @TableField(value = "otherlendsumid")
    private Long otherlendsumid;

    /**
     * 临时的付款信息，new一个实体不要保存到数据库，然后转换成json字符串，等到接收完确认借款有效后 再反向转成实体类 保存到数据库
     */
    @ApiModelProperty(value = "临时的付款信息")
    @TableField(value = "tmppaymoneyinfo")
    private String tmppaymoneyinfo;

    /**
     * 借款线下操作-是否已经接收（0：未接收，1：已经接收）
     */
    @ApiModelProperty(value = "借款线下操作-是否已经接收（0：未接收，1：已经接收）")
    @TableField(value = "receivestatus")
    private Boolean receivestatus;

    /**
     * 借款线下操作-接收时间
     */
    @ApiModelProperty(value = "借款线下操作-接收时间")
    @TableField(value = "receivetime")
    private Date receivetime;

    /**
     * 借款线下操作-接收人（姓名、显示名）
     */
    @ApiModelProperty(value = "借款线下操作-接收人（姓名、显示名）")
    @TableField(value = "receiver")
    private String receiver;

    /**
     * 借款线下操作-是否已经确认过（-1：确认退回，0：未确认，1：确认通过）  确认通过后  effectflag字段制为true,同时生成对应的付款单
     */
    @ApiModelProperty(value = "借款线下操作-是否已经确认过（-1：确认退回，0：未确认，1：确认通过）  确认通过后  effectflag字段制为true,同时生成对应的付款单")
    @TableField(value = "receiveverifystatus")
    private Integer receiveverifystatus;

    /**
     * 借款线下操作-退回原因
     */
    @ApiModelProperty(value = "借款线下操作-退回原因")
    @TableField(value = "receivebackremark")
    private String receivebackremark;

    /**
     * 借款线下操作-确认时间
     */
    @ApiModelProperty(value = "借款线下操作-确认时间")
    @TableField(value = "receiveverifytime")
    private Date receiveverifytime;

    /**
     * 借款线下操作-确认人（姓名、显示名）
     */
    @ApiModelProperty(value = "借款线下操作-确认人（姓名、显示名）")
    @TableField(value = "receiveverifyer")
    private String receiveverifyer;

    /**
     * 利息(临时)
     */
    @TableField(exist = false)
    private BigDecimal tempInterestMoney;

}
