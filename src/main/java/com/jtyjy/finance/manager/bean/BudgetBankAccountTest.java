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

/**
 * @author Admin
 */
@TableName(value = "budget_bank_account_test")
@Data
public class BudgetBankAccountTest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "code")
    private String code;

    /**
     * 人员（单位）姓名
     */
    @ApiParam(hidden = true)
    @TableField(value = "pname")
    private String pname;

    /**
     * 户名
     */
    @ApiParam(hidden = true)
    @TableField(value = "accountname")
    private String accountname;

    /**
     * 账户类型,数据字典，1：对内，2：对外
     */
    @ApiParam(hidden = true)
    @TableField(value = "accounttype")
    private Integer accounttype;

    /**
     * 银行账户
     */
    @NotBlank(message = "银行账户不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "bankaccount")
    private String bankaccount;

    /**
     * 是否为工资账户 true表示是工资账户
     */
    @NotNull(message = "是否为工资账户 true表示是工资账户不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "wagesflag")
    private Boolean wagesflag;

    /**
     * 银行网点编号
     */
    @NotBlank(message = "银行网点编号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "branchcode")
    private String branchcode;

    /**
     * 停用标识（0：启用【默认】 1：停用）
     */
    @ApiParam(hidden = true)
    @TableField(value = "stopflag")
    private Boolean stopflag;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 第三方 系统 id
     */
    @ApiParam(hidden = true)
    @TableField(value = "outkey")
    private String outkey;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "test")
    private String test;

}
