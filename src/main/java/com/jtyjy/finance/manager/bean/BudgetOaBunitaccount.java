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
@TableName(value = "budget_oa_bunitaccount")
@Data
public class BudgetOaBunitaccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @NotNull(message = "主键Id不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitname")
    private String bunitname;

    /**
     * 银行账户
     */
    @NotBlank(message = "银行账户不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "bunitaccount")
    private String bunitaccount;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "branchname")
    private String branchname;

    /**
     * 是否法人单位
     */
    @ApiParam(hidden = true)
    @TableField(value = "corporation")
    private Boolean corporation;

}
