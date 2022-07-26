package com.jtyjy.finance.manager.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * @author Admin
 */
@TableName(value = "budget_extract_importdetail")
@Data
public class BudgetExtractImportdetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提成主表id
     */
    @ApiParam(hidden = true)
    @TableField(value = "extractsumid")
    private Long extractsumid;

    /**
     * 提成明细id
     */
    @ApiParam(hidden = true)
    @TableField(value = "extractdetailid")
    private Long extractdetailid;

    /**
     * 届别
     */
    @NotNull(message = "提成届别不能为空")
    @ApiModelProperty(hidden = false, value = "提成届别")
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 是否是公司员工
     */
    @NotNull(message = "是否公司员工不能为空")
    @ApiModelProperty(hidden = false, value = "是否公司员工")
    @TableField(value = "iscompanyemp")
    private Boolean iscompanyemp;

    /**
     * 是否是坏账
     */
    @NotNull(message = "是否坏账不能为空")
    @ApiModelProperty(hidden = false, value = "是否坏账")
    @TableField(value = "isbaddebt")
    private Boolean isbaddebt;

    /**
     * 员工id
     */
    @NotBlank(message = "提成人员不能为空")
    @ApiModelProperty(hidden = false, value = "提成人员")
    @TableField(value = "empid")
    private String empid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "idnumber")
    private String idnumber;

    /**
     * 工号
     */
    @ApiParam(hidden = true)
    @TableField(value = "empno")
    private String empno;

    /**
     * 名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "empname")
    private String empname;

    /**
     * 实发提成
     */
    @NotNull(message = "应发提成不能为空")
    @ApiModelProperty(hidden = false, value = "应发提成")
    @TableField(value = "copeextract")
    private BigDecimal copeextract;

    /**
     * 综合税
     */
    @NotNull(message = "综合税不能为空")
    @ApiModelProperty(hidden = false, value = "综合税")
    @TableField(value = "consotax")
    private BigDecimal consotax;


    @ApiModelProperty(hidden = false, value = "提成类型(2021-12月新增)")
    @TableField(value = "extract_type")
    private String extractType;

    @ApiModelProperty(hidden = false, value = "应发提成(2021-12月新增)")
    @TableField(value = "should_send_extract")
    private BigDecimal shouldSendExtract = BigDecimal.ZERO;

    @ApiModelProperty(hidden = false, value = "个税(2021-12月新增)")
    @TableField(value = "tax")
    private BigDecimal tax = BigDecimal.ZERO;

    @ApiModelProperty(hidden = false, value = "个税减免(2021-12月新增)")
    @TableField(value = "tax_reduction")
    private BigDecimal taxReduction= BigDecimal.ZERO;

    @ApiModelProperty(hidden = false, value = "发票超额税金(2021-12月新增)")
    @TableField(value = "invoice_excess_tax")
    private BigDecimal invoiceExcessTax= BigDecimal.ZERO;

    @ApiModelProperty(hidden = false, value = "发票超额税金减免(2021-12月新增)")
    @TableField(value = "invoice_excess_tax_reduction")
    private BigDecimal invoiceExcessTaxReduction= BigDecimal.ZERO;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

}
