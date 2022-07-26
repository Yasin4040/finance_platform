package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_extractpay_rule")
@Data
public class BudgetExtractpayRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiModelProperty(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    @NotEmpty(message = "规则名称不能为空")
    @ApiModelProperty(hidden = false, value = "规则名称")
    @TableField(value = "name")
    private String name;

    /**
     * 多个开票公司id
     */
    @NotBlank(message = "工资发放单位id不能为空")
    @ApiModelProperty(hidden = false, value = "工资发放单位id（以,分隔）", dataType = "String")
    @TableField(value = "billunitids")
    private String billunitids;

    /**
     * 是否终止 true 终止 ， 可以提前终止
     */
    @ApiModelProperty(hidden = false, value = "是否终止。true终止 false不终止")
    @TableField(value = "endflag")
    private Boolean endflag = false;

    /**
     * 生效日期
     */
    @NotNull(message = "生效日期不能为空")
    @ApiModelProperty(hidden = false, value = "生效日期(年-月-日)")
    @TableField(value = "effectdate")
    private Date effectdate;

    /**
     * 临界金额
     */
    @NotNull(message = "临界金额不能为空")
    @ApiModelProperty(hidden = false, value = "临界金额")
    @TableField(value = "je")
    private BigDecimal je;

    /**
     * 银行账户id
     */
    @NotNull(message = "避税发放单位账户不能为空")
    @ApiModelProperty(hidden = false, value = "避税发放单位账户id")
    @TableField(value = "personunitid")
    private Long personunitid;

    /**
     * 更新时间
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 创建时间
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 备注
     */
    @ApiModelProperty(hidden = false, value = "备注")
    @TableField(value = "remark")
    private String remark;

    /**
     * 工资发放单位名称
     */
    @ApiModelProperty(hidden = true, value = "工资发放单位名称")
    @TableField(value = "salarypayunitnames")
    private String salarypayunitnames;

}
