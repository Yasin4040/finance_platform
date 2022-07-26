package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_fundtransfer")
@Data
public class BudgetFundtransfer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请人
     */
    @ApiParam(hidden = true)
    @TableField(value = "empname")
    private String empname;

    /**
     * 流程编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "requestnumber")
    private String requestnumber;

    /**
     * 申请部门
     */
    @ApiParam(hidden = true)
    @TableField(value = "dept")
    private String dept;

    /**
     * 资金调拨类型(集团调拨，利润调拨，单个公司调拨，多个公司调拨）
     */
    @ApiParam(hidden = true)
    @TableField(value = "fundtranstype")
    private String fundtranstype;

    /**
     * 调拨事由
     */
    @ApiParam(hidden = true)
    @TableField(value = "applyreason")
    private String applyreason;

    /**
     * 调拨金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "applymoney")
    private Float applymoney;

    /**
     * 付款单位-账户类型（0 ：私人账户，1 ：对公账户）
     */
    @ApiParam(hidden = true)
    @TableField(value = "paytype")
    private String paytype;

    /**
     * 付款单位银行账号
     */
    @ApiParam(hidden = true)
    @TableField(value = "paybankaccount")
    private String paybankaccount;

    /**
     * 流转单位-账户类型（私人账户，对公账户）
     */
    @ApiParam(hidden = true)
    @TableField(value = "transtype")
    private String transtype;

    /**
     * 流转单位银行账号
     */
    @ApiParam(hidden = true)
    @TableField(value = "transbankaccount")
    private String transbankaccount;

    /**
     * 收款单位-账户类型（私人账户，对公账户）
     */
    @ApiParam(hidden = true)
    @TableField(value = "recetype")
    private String recetype;

    /**
     * 收款单位银行账号
     */
    @ApiParam(hidden = true)
    @TableField(value = "recebankaccount")
    private String recebankaccount;

    /**
     * 申请时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "applydate")
    private Date applydate;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 流程Id
     */
    @ApiParam(hidden = true)
    @TableField(value = "requestid")
    private String requestid;

}
