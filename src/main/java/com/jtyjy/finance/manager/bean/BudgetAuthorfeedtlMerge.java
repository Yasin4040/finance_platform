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
@TableName(value = "budget_authorfeedtl_merge")
@Data
public class BudgetAuthorfeedtlMerge implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属报表id
     */
    @ApiParam(hidden = true)
    @TableField(value = "reportid")
    private Long reportid;

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
     * 作者id
     */
    @ApiParam(hidden = true)
    @TableField(value = "authorid")
    private Long authorid;

    /**
     * 作者名字（银行账户名）
     */
    @ApiParam(hidden = true)
    @TableField(value = "authorname")
    private String authorname;

    /**
     * 作者类型（1：公司内部    0：公司外部）
     */
    @ApiParam(hidden = true)
    @TableField(value = "authortype")
    private Boolean authortype;

    /**
     * 身份证号（个人作者）
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
     * 是否发放（默认1：发放  0：不发放）
     */
    @ApiParam(hidden = true)
    @TableField(value = "payflag")
    private Boolean payflag;

    /**
     * 教育社的服务费是否已加 0 ：false 1 ：true
     */
    @ApiParam(hidden = true)
    @TableField(value = "svefeeflag")
    private Boolean svefeeflag;

    /**
     * 收款方是否为教育社  1：是   0：否
     */
    @ApiParam(hidden = true)
    @TableField(value = "edupubflag")
    private Boolean edupubflag;

    /**
     * 付款方式否为个卡（陈彩莲）1：是  0： 否
     */
    @ApiParam(hidden = true)
    @TableField(value = "prlautflag")
    private Boolean prlautflag;

    /**
     * 教育社服务费
     */
    @ApiParam(hidden = true)
    @TableField(value = "servicefee")
    private BigDecimal servicefee;

    /**
     * 合并应发稿酬
     */
    @ApiParam(hidden = true)
    @TableField(value = "copefee")
    private BigDecimal copefee;

    /**
     * 第一次的总税前稿酬（二次导入时使用）
     */
    @ApiParam(hidden = true)
    @TableField(value = "scdcopefee")
    private BigDecimal scdcopefee;

    /**
     * 合并实发稿费
     */
    @ApiParam(hidden = true)
    @TableField(value = "realfee")
    private BigDecimal realfee;

    /**
     * 报销科目
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimbursesubject")
    private String reimbursesubject;

    /**
     * 稿费所属部门
     */
    @ApiParam(hidden = true)
    @TableField(value = "feebdgdept")
    private String feebdgdept;

    /**
     * 工资发放单位（公司员工）
     */
    @ApiParam(hidden = true)
    @TableField(value = "salaryunit")
    private String salaryunit;

    /**
     * 公司留存个税
     */
    @ApiParam(hidden = true)
    @TableField(value = "companystoretax")
    private BigDecimal companystoretax;

    /**
     * 归属事业群
     */
    @ApiParam(hidden = true)
    @TableField(value = "businessgroup")
    private String businessgroup;

    /**
     * 收款方编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "gathercode")
    private String gathercode;

    /**
     * 收款单位（对应个人账户或江西教育社）
     */
    @ApiParam(hidden = true)
    @TableField(value = "gatherunit")
    private String gatherunit;

    /**
     * 收款单位开户行
     */
    @ApiParam(hidden = true)
    @TableField(value = "gatherbank")
    private String gatherbank;

    /**
     * 收款方银行类型（收款方为教育社时记录作者的银行类型）
     */
    @ApiParam(hidden = true)
    @TableField(value = "gatherbanktype")
    private String gatherbanktype;

    /**
     * 作者账户（收款方为教育社时记录作者的账户）
     */
    @ApiParam(hidden = true)
    @TableField(value = "gatherauthoraccount")
    private String gatherauthoraccount;

    /**
     * 收款单位银行账户
     */
    @ApiParam(hidden = true)
    @TableField(value = "gatherbankaccount")
    private String gatherbankaccount;

    /**
     * 发放单位账户id
     */
    @ApiParam(hidden = true)
    @TableField(value = "payunitaccountid")
    private Long payunitaccountid;

    /**
     * 发放单位id
     */
    @ApiParam(hidden = true)
    @TableField(value = "payunitid")
    private Long payunitid;

    /**
     * 发放单位
     */
    @ApiParam(hidden = true)
    @TableField(value = "payunit")
    private String payunit;

    /**
     * 发放单位开户行
     */
    @ApiParam(hidden = true)
    @TableField(value = "paybank")
    private String paybank;

    /**
     * 发放单位账户
     */
    @ApiParam(hidden = true)
    @TableField(value = "paybankaccount")
    private String paybankaccount;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;


    //老的计税方式
    @TableField(exist = false)
    private boolean isCommon = true;

    //是否是读者出版
    @TableField(exist = false)
    private boolean isDzcb = false;
    //是否是原创版
    @TableField(exist = false)
    private boolean isYcb = false;
    //是否是校园版
    @TableField(exist = false)
    private boolean isXyb = false;

}
