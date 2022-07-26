package com.jtyjy.finance.manager.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jtyjy.core.interceptor.LoginThreadLocal;
import com.jtyjy.finance.manager.constants.StatusConstants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.enmus.ReimbursementFromEnmu;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Admin
 */
@TableName(value = "budget_reimbursementorder")
@Data
public class BudgetReimbursementorder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(value = "报销单主键【修改时必填】", hidden = false, required = false)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报销单编号(13位 ，BX + 年月 + 5位流水)
     */
    @ApiModelProperty(value = "报销单编号", hidden = false, required = false)
    @TableField(value = "reimcode")
    private String reimcode;

    /**
     * 期间批次  如果是稿费就是就是稿费批次号    工资 该值就是 201908
     */
    @ApiModelProperty(value = "期间批次", hidden = true, required = false)
    @TableField(value = "interimbatch")
    private String interimbatch;

    /**
     * //报销单来源 0：普通报销单（预算员手动填写的）1：稿费 2：提成 3：工资 4:项目预领
     */
    @ApiModelProperty(value = "报销单来源 0：普通报销单（预算员手动填写的）1：稿费 2：提成 3：工资 4:项目预领", hidden = true, required = false)
    @TableField(value = "orderscrtype")
    private Integer orderscrtype;

    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiModelProperty(value = "届别主键", hidden = false, required = true)
    @TableField(value = "yearid")
    private Long yearid;

    @ApiModelProperty(value = "返单类型", hidden = false, required = true)
    @TableField(value = "back_type")
    private String backType;

    /**
     * 预算单位id
     */
    @NotNull(message = "预算单位id不能为空")
    @ApiModelProperty(value = "预算单位主键", hidden = false, required = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 月份id
     */
    @NotNull(message = "月份id不能为空")
    @ApiModelProperty(value = "月份主键", hidden = false, required = true)
    @TableField(value = "monthid")
    private Long monthid;
    
    @TableField(exist = false)
    private String monthName;//月名

    /**
     * 报销人id
     */
    @NotBlank(message = "报销人id不能为空")
    @ApiModelProperty(value = "报销人主键", hidden = false, required = true)
    @TableField(value = "reimperonsid")
    private String reimperonsid;

    /**
     * 报销人姓名
     */
    @NotBlank(message = "报销人姓名不能为空")
    @ApiModelProperty(value = "报销人姓名", hidden = false, required = true)
    @TableField(value = "reimperonsname")
    private String reimperonsname;

    /**
     * 报销日期
     */
    @NotNull(message = "报销日期不能为空")
    @ApiModelProperty(value = "报销日期", hidden = false, required = true)
    @TableField(value = "reimdate")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date reimdate;

    /**
     * 报销金额
     */
    @NotNull(message = "报销金额不能为空")
    @ApiModelProperty(value = "报销金额", hidden = false, required = true)
    @TableField(value = "reimmoney")
    private BigDecimal reimmoney;

    /**
     * 不计入执行报销金额
     */
    @NotNull(message = "不计入执行报销金额不能为空")
    @ApiModelProperty(value = "不计入执行报销金额", hidden = false, required = true)
    @TableField(value = "nonreimmoney")
    private BigDecimal nonreimmoney;

    /**
     * 冲账金额
     */
    @NotNull(message = "冲账金额不能为空")
    @ApiModelProperty(value = "冲账金额", hidden = false, required = true)
    @TableField(value = "paymentmoney")
    private BigDecimal paymentmoney;

    /**
     * 转账金额
     */
    @NotNull(message = "转账金额不能为空")
    @ApiModelProperty(value = "转账金额", hidden = false, required = true)
    @TableField(value = "transmoney")
    private BigDecimal transmoney;

    /**
     * 现金金额
     */
    @NotNull(message = "现金金额不能为空")
    @ApiModelProperty(value = "现金金额", hidden = false, required = true)
    @TableField(value = "cashmoney")
    private BigDecimal cashmoney;

    /**
     * 划拨金额
     */
    @NotNull(message = "划拨金额不能为空")
    @ApiModelProperty(value = "划拨金额", hidden = false, required = true)
    @TableField(value = "allocatedmoney")
    private BigDecimal allocatedmoney;

    /**
     * 其它金额
     */
    @NotNull(message = "其它金额不能为空")
    @ApiModelProperty(value = "其它金额", hidden = false, required = true)
    @TableField(value = "othermoney")
    private BigDecimal othermoney;

    /**
     * 附件张数
     */
    @NotNull(message = "附件张数不能为空")
    @ApiModelProperty(value = "附件张数", hidden = false, required = true)
    @TableField(value = "attachcount")
    private Integer attachcount;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注", hidden = false, required = false)
    @TableField(value = "remark")
    private String remark;

    /**
     * 审核通过时间
     */
    @ApiModelProperty(value = "审核通过时间", hidden = false, required = false)
    @TableField(value = "verifytime")
    private Date verifytime;

    /**
     * 提交时间
     */
    @ApiModelProperty(value = "提交时间", hidden = false, required = false)
    @TableField(value = "submittime")
    private Date submittime;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", hidden = false, required = false)
    @TableField(value = "createtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createtime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", hidden = false, required = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 申请人id
     */
    @ApiModelProperty(value = "申请人主键", hidden = true, required = false)
    @TableField(value = "applicantid")
    private String applicantid;

    /**
     * 申请人姓名
     */
    @ApiModelProperty(value = "申请人姓名", hidden = true, required = false)
    @TableField(value = "applicantame")
    private String applicantame;

    /**
     * 申请时间
     */
    @ApiModelProperty(value = "申请时间", hidden = true, required = false)
    @TableField(value = "applicanttime")
    private Date applicanttime;

    /**
     * 报销类型1：通用，2：差旅，3：招待
     */
    @ApiModelProperty(value = "报销类型1：通用，2：差旅，3：招待", hidden = false, required = true)
    @TableField(value = "bxtype")
    @NotNull(message = "报销类型不能为空！")
    private Integer bxtype;
    
    @TableField(exist = false)
    private String bxTypeName;

    /**
     * 出差人员
     */
    @ApiModelProperty(value = "出差人员", hidden = false, required = false)
    @TableField(value = "traveler")
    private String traveler;

    /**
     * 出差事由
     */
    @ApiModelProperty(value = "出差事由", hidden = false, required = false)
    @TableField(value = "travelreason")
    private String travelreason;

    /**
     * 版本号
     */
    @ApiModelProperty(value = "版本号", hidden = true, required = false)
    @TableField(value = "version")
    private String version;

    /**
     * 票面审核接收状态   1:票面审核已接收 0:票面审核未接收   （票面审核-->预算审核-->扫描分单-->会计做账）
     */
    @ApiModelProperty(value = "单据接收接收状态", hidden = false, required = false)
    @TableField(value = "parverifyreceivestatus")
    private Boolean parverifyreceivestatus;

    /**
     * 票面审核时间
     */
    @ApiModelProperty(value = "单据接收时间", hidden = false, required = false)
    @TableField(value = "parverifytime")
    private String parverifytime;

    /**
     * 票面审核状态 0：未审核；1：已审核
     */
    @ApiModelProperty(value = "票面审核状态", hidden = false, required = false)
    @TableField(value = "parverifystatus")
    private Boolean parverifystatus;

    /**
     * 票面审核人（工号，登录账号）
     */
    @ApiModelProperty(value = "票面审核人", hidden = false, required = false)
    @TableField(value = "parverifyer")
    private String parverifyer;

    /**
     * 预算审核接收状态   1:预算审核已接收 0：票面审核未接收   （票面审核-->预算审核-->扫描分单-->会计做账）
     */
    @ApiModelProperty(value = "预算审核接收状态", hidden = false, required = false)
    @TableField(value = "budgetverifyreceivestatus")
    private Boolean budgetverifyreceivestatus;

    /**
     * 预算审核时间
     */
    @ApiModelProperty(value = "预算审核时间", hidden = false, required = false)
    @TableField(value = "budgetverifytime")
    private String budgetverifytime;

    /**
     * 预算审核状态0：未审核；1：已审核
     */
    @ApiModelProperty(value = "预算审核状态", hidden = false, required = false)
    @TableField(value = "budgetverifystatus")
    private Boolean budgetverifystatus;

    /**
     * 预算审核人（工号，登录账号）
     */
    @ApiModelProperty(value = "预算审核人", hidden = false, required = false)
    @TableField(value = "budgetverifyer")
    private String budgetverifyer;

    /**
     * 分单扫描接收状态          1:分单已经接收   0：分单未接收   （票面审核-->预算审核-->扫描分单-->会计做账）
     */
    @ApiModelProperty(value = "分单扫描接收状态", hidden = false, required = false)
    @TableField(value = "fdreceivestatus")
    private Boolean fdreceivestatus;

    /**
     * 分单扫描时间 （只有票面通过、预算通过 后才能分单）
     */
    @ApiModelProperty(value = "分单扫描时间", hidden = false, required = false)
    @TableField(value = "fdtime")
    private String fdtime;

    /**
     * 分单扫描状态 0:还未分单；1：已分单
     */
    @ApiModelProperty(value = "分单扫描状态", hidden = false, required = false)
    @TableField(value = "fdstatus")
    private Boolean fdstatus;

    /**
     * 分单扫描人（工号，登录账号）
     */
    @ApiModelProperty(value = "分单扫描人", hidden = false, required = false)
    @TableField(value = "fder")
    private String fder;

    /**
     * 分单确认接收状态           1:做账已经接收 0:做账未接收   （票面审核-->预算审核-->扫描分单-->会计做账）
     */
    @ApiModelProperty(value = "分单确认接收状态", hidden = false, required = false)
    @TableField(value = "accountreceivestatus")
    private Boolean accountreceivestatus;

    /**
     * 分单确认时间
     */
    @ApiModelProperty(value = "分单确认时间", hidden = false, required = false)
    @TableField(value = "accounttime")
    private String accounttime;

    /**
     * 分单确认状态 0:还未做账；1：已做账
     */
    @ApiModelProperty(value = "分单确认状态", hidden = false, required = false)
    @TableField(value = "accountstatus")
    private Boolean accountstatus;

    /**
     * 分单确认人（工号，登录账号）
     */
    @ApiModelProperty(value = "分单确认人", hidden = false, required = false)
    @TableField(value = "accounter")
    private String accounter;

    /**
     * 最后扫描时间
     */
    @ApiModelProperty(value = "最后扫描时间", hidden = false, required = false)
    @TableField(value = "curscantime", updateStrategy = FieldStrategy.IGNORED)
    private String curscantime;

    /**
     * 当前扫描状态0：等待扫描；1:票面审核,2:预算审核,3:扫描分单,4:会计做账,5:出纳付款
     */
    @ApiModelProperty(value = "当前扫描状态", hidden = false, required = false)
    @TableField(value = "curscanstatus")
    private Integer curscanstatus;

    /**
     * "接收状态:-4:限制错误 -3：全部退回；-2:退回纸质；-1:失败（版本不一致） ；0：待接收；1：票面审核（接收）；2：预算审核（接收）；3:扫描分单（接收）；4：会计做账（接收）；5：出纳付款
     */
    @ApiModelProperty(value = "接收状态", hidden = false, required = false)
    @TableField(value = "receivestatus")
    private Integer receivestatus;

    /**
     * 当前接收环节名称
     */
    @ApiModelProperty(value = "当前接收环节名称", hidden = false, required = false)
    @TableField(value = "curscanstatusname", updateStrategy = FieldStrategy.IGNORED)
    private String curscanstatusname;

    /**
     * 当前接收人id
     */
    @ApiModelProperty(value = "当前接收人主键", hidden = false, required = false)
    @TableField(value = "curscaner", updateStrategy = FieldStrategy.IGNORED)
    private String curscaner;

    /**
     * 当前接收人名称
     */
    @ApiModelProperty(value = "当前接收人名称", hidden = false, required = false)
    @TableField(value = "curscanername", updateStrategy = FieldStrategy.IGNORED)
    private String curscanername;

    /**
     * 审核状态，-1：退回，0：保存，1：已提交（待审核），2：审核通过
     */
    @ApiModelProperty(value = "审核状态", hidden = false, required = false)
    @TableField(value = "reuqeststatus")
    private Integer reuqeststatus;

    /**
     * 紧急程度  值越大越紧急（默认为0）
     */
    @ApiModelProperty(value = "紧急程度", hidden = true, required = false)
    @TableField(value = "requestlevel")
    private Integer requestlevel;

    /**
     * 处理状态  true :已处理，false：未处理（默认）
     */
    @ApiModelProperty(value = "处理状态", hidden = false, required = false)
    @TableField(value = "handleflag")
    private Boolean handleflag;

    /**
     * 二维码 base64字符
     */
    @ApiModelProperty(value = "二维码", hidden = false, required = false)
    @TableField(value = "qrcodebase64str")
    private String qrcodebase64str;

    /**
     * 出纳付款 是否接收 （0：未接收，1：已经结束）
     */
    @ApiModelProperty(value = "出纳付款 是否接收 （0：未接收，1：已经结束）", hidden = false, required = false)
    @TableField(value = "cashierpaymentreceivestatus")
    private Boolean cashierpaymentreceivestatus;

    /**
     * 会计做账 是否接收 （0：未接收，1：已经结束）
     */
    @ApiModelProperty(value = "会计做账 是否接收 （0：未接收，1：已经结束", hidden = false, required = false)
    @TableField(value = "account1receivestatus")
    private Boolean account1receivestatus;

    /**
     * 凭证审核 是否接收 （0：未接收，1：已经结束）
     */
    @ApiModelProperty(value = "凭证审核 是否接收 （0：未接收，1：已经结束）", hidden = false, required = false)
    @TableField(value = "voucherauditreceivestatus")
    private Boolean voucherauditreceivestatus;

    /**
     * 法人公司抽单 是否接收 （0：未接收，1：已经结束）
     */
    @ApiModelProperty(value = "法人公司抽单 是否接收 （0：未接收，1：已经结束）", hidden = false, required = false)
    @TableField(value = "drawbillreceivestatus")
    private Boolean drawbillreceivestatus;

    /**
     * 内部内部结束 是否接收 （0：未接收，1：已经结束）
     */
    @ApiModelProperty(value = "内部结束 是否接收 （0：未接收，1：已经结束）", hidden = false, required = false)
    @TableField(value = "endreceivestatus")
    private Boolean endreceivestatus;


    /**
     * 审核环节
     */
    @ApiModelProperty(value = "审核环节", hidden = false, required = false)
    @TableField(value = "work_flow_step")
    private String workFlowStep;

    /**
     *
     *
     *
     *
     * bill_receive,
     * budget_check,
     * financial_manage_check,
     * general_manager_check,
     * split_bill_scan,
     * split_bill_confirm,
     * cashier_pay,
     * accounting_do_bill,
     * voucher_check,
     * corporation_draw_bill
     */

    /**
     * 条件版本
     */
    @ApiModelProperty(value = "条件版本（流程模板id）", hidden = true, required = false)
    @TableField(value = "work_flow_version")
    private Integer workFlowVersion;

    /**
     * 财务总监审核接收状态   1:已接收 0：未接收
     */
    @ApiModelProperty(value = "财务总监审核接收状态 1:已接收 0：未接收", hidden = false, required = false)
    @TableField(value = "financialmanagereceivestatus")
    private Boolean financialmanagereceivestatus;

    /**
     * 财务总监审核时间
     */
    @ApiModelProperty(value = "财务总监审核时间", hidden = false, required = false)
    @TableField(value = "financialmanagetime")
    private String financialmanagetime;

    /**
     * 财务总监审核状态 0：未审核；1：已审核
     */
    @ApiModelProperty(value = "财务总监审核状态 0：未审核；1：已审核", hidden = false, required = false)
    @TableField(value = "financialmanagestatus")
    private Boolean financialmanagestatus;

    /**
     * 财务总监（工号，登录账号）
     */
    @ApiModelProperty(value = "财务总监（工号，登录账号）", hidden = false, required = false)
    @TableField(value = "financialmanager")
    private String financialmanager;

    /**
     * 总经理审核接收状态   1:已接收 0：未接收
     */
    @ApiModelProperty(value = "总经理审核接收状态 1:已接收 0：未接收", hidden = false, required = false)
    @TableField(value = "generalmanagereceivestatus")
    private Boolean generalmanagereceivestatus;

    /**
     * 总经理审核时间
     */
    @ApiModelProperty(value = "总经理审核时间", hidden = false, required = false)
    @TableField(value = "generalmanagetime")
    private String generalmanagetime;

    /**
     * 总经理审核状态 0：未审核；1：已审核
     */
    @ApiModelProperty(value = "总经理审核状态 0：未审核；1：已审核", hidden = false, required = false)
    @TableField(value = "generalmanagestatus")
    private Boolean generalmanagestatus;

    /**
     * 总经理（工号，登录账号）
     */
    @ApiModelProperty(value = "总经理（工号，登录账号）", hidden = false, required = false)
    @TableField(value = "generalmanager")
    private String generalmanager;

    @ApiModelProperty(value = "特殊出差人员ids,以逗号,分隔")
    @TableField(value = "special_travelerids")
    private String specialTravelerids;

    @ApiModelProperty(value = "特殊出差人员名称（作显示用）")
    @TableField(exist = false)
    private String specialTravelerNames;

    /**
     * 界别名称
     */
    @TableField(exist = false)
    private String yearName;

    /**
     * 预算单位名称
     */
    @TableField(exist = false)
    private String unitName;

    /**
     * 出差人员名称
     */
    @TableField(exist = false)
    private String travelerName;

    public void setBase(String reimcode, Boolean isProjectBx,Boolean isFixAsset,WbUser user) {
        //设置主键为空
        this.setId(null);
        //设置报销单编号(13位 ，BX + 年月 + 5位流水)
        this.setReimcode(reimcode);
        //设置期间批次
        this.setInterimbatch(null);
        //设置报销单来源
        this.setOrderscrtype(ReimbursementFromEnmu.COMMON.getCode());
        if(isProjectBx!=null && isProjectBx){
            this.setOrderscrtype(ReimbursementFromEnmu.PROJECT.getCode());
        }else if(isFixAsset!=null && isFixAsset){
            this.setOrderscrtype(ReimbursementFromEnmu.FIXED_ASSET.getCode());
        }
        //this.setOrderscrtype(isProjectBx==null ? ReimbursementFromEnmu.COMMON.getCode():isProjectBx ? ReimbursementFromEnmu.PROJECT.getCode() : ReimbursementFromEnmu.COMMON.getCode());
        //设置创建时间
        this.setCreatetime(new Date());
        //设置
        this.setUpdatetime(this.getCreatetime());

        if(isFixAsset!=null && isFixAsset){
            this.setReimperonsid(user.getUserId());
            this.setReimperonsname(user.getDisplayName());
            this.setApplicantid(user.getUserId());
            this.setApplicantame(user.getDisplayName());
        }else{
            //设置申请人
            this.setApplicantid(UserThreadLocal.get().getUserId());
            this.setApplicantame(UserThreadLocal.get().getDisplayName());
        }


        //设置申请时间
        this.setApplicanttime(this.getCreatetime());
        //设置版本号
        this.setVersion("0");
        //设置票面审核接收状态
        this.setParverifyreceivestatus(Boolean.FALSE);
        //设置票面审核状态
        this.setParverifystatus(Boolean.FALSE);
        //设置预算审核接收状态
        this.setBudgetverifyreceivestatus(Boolean.FALSE);
        //设置预算审核状态
        this.setBudgetverifystatus(Boolean.FALSE);
        //设置分单接收状态
        this.setFdreceivestatus(Boolean.FALSE);
        //设置分单状态
        this.setFdstatus(Boolean.FALSE);
        //设置做账接收状态
        this.setAccountreceivestatus(Boolean.FALSE);
        //设置财务总监审核状态
        this.setFinancialmanagereceivestatus(Boolean.FALSE);
        this.setFinancialmanagestatus(Boolean.FALSE);
        //设置总经理审核状态
        this.setGeneralmanagereceivestatus(Boolean.FALSE);
        this.setGeneralmanagestatus(Boolean.FALSE);
        //设置做账状态
        this.setAccountstatus(Boolean.FALSE);
        //设置审核状态
        this.setReuqeststatus(StatusConstants.BX_SAVE);
        //设置紧急程度
        this.setRequestlevel(StatusConstants.SERIOUS_ZERO);
        //设置处理状态
        this.setHandleflag(Boolean.FALSE);
        //设置接收状态
        this.setReceivestatus(StatusConstants.BX_SAVE);
    }

    /**
     * 非空校验
     *
     * @return
     */
    public String validate() {
        //非空校验
        return BaseController.validate(this);
    }


    public static final BudgetReimbursementorder getTestBean() {
        //设置报销单信息
        BudgetReimbursementorder order = new BudgetReimbursementorder();
        order.setYearid(34L);
        order.setUnitid(200L);
        order.setMonthid(6L);
        order.setReimperonsid("004C4C3DPCVNR");
        order.setReimperonsname("孔令程");
        order.setReimdate(new Date());
        order.setReimmoney(new BigDecimal("130.0000"));
        order.setNonreimmoney(new BigDecimal("30.0000"));
        order.setPaymentmoney(new BigDecimal("1.9"));
        order.setTransmoney(new BigDecimal("2"));
        order.setCashmoney(new BigDecimal("3"));
        order.setAllocatedmoney(new BigDecimal("30"));
        order.setOthermoney(new BigDecimal("123.1"));
        order.setAttachcount(10);
        order.setRemark("测试......");
        order.setBxtype(1);
        return order;
    }
}
