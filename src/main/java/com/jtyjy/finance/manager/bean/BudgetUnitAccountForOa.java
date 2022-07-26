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
@TableName(value = "budget_unit_account_for_oa")
@Data
public class BudgetUnitAccountForOa implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @NotNull(message = "主键Id不能为空")
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
     * 名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "name")
    private String name;

    /**
     * 公司发票(1)、无票(0)
     */
    @ApiParam(hidden = true)
    @TableField(value = "billingunittype")
    private String billingunittype;

    /**
     * 是否法人单位
     */
    @ApiParam(hidden = true)
    @TableField(value = "corporation")
    private Boolean corporation;

    /**
     * 内部单位标志
     */
    @ApiParam(hidden = true)
    @TableField(value = "ownflag")
    private Boolean ownflag;

    /**
     * 银行关联Id
     */
    @NotBlank(message = "银行关联Id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "branchcode")
    private String branchcode;

    /**
     * 银行账户
     */
    @NotBlank(message = "银行账户不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "bankaccount")
    private String bankaccount;

}
