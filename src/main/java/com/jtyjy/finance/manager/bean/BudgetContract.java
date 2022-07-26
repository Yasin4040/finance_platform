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
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_contract")
@Data
public class BudgetContract implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @NotNull(message = "主键Id不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 合同名称
     */
    @NotBlank(message = "合同名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "contractname")
    private String contractname;

    /**
     * 合同编号
     */
    @NotBlank(message = "合同编号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "contractcode")
    private String contractcode;

    /**
     * 合同金额
     */
    @NotNull(message = "合同金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "contractmoney")
    private Float contractmoney;

    /**
     * 合同已经支付金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "chargedmoney")
    private Float chargedmoney;

    /**
     * 合同已经冲账金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "repaidmoney")
    private Float repaidmoney;

    /**
     * 签订日期（到天）
     */
    @NotNull(message = "签订日期（到天）不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "signdate")
    private Date signdate;

    /**
     * 签订份数
     */
    @ApiParam(hidden = true)
    @TableField(value = "contractcopies")
    private String contractcopies;

    /**
     * 合同终止状态 ：0 进行中  ， 1 已终止
     */
    @NotNull(message = "合同终止状态 ：0 进行中  ， 1 已终止不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "terminationflag")
    private Integer terminationflag;

    /**
     * （计划）终止日期（到天）
     */
    @ApiParam(hidden = true)
    @TableField(value = "terminationdate")
    private Date terminationdate;

    /**
     * （实际）终止日期（到天
     */
    @ApiParam(hidden = true)
    @TableField(value = "realterminationdate")
    private Date realterminationdate;

    /**
     * 约定结算方式
     */
    @ApiParam(hidden = true)
    @TableField(value = "agreesumtype")
    private String agreesumtype;

    /**
     * 合同内容摘要
     */
    @ApiParam(hidden = true)
    @TableField(value = "contextdigest")
    private String contextdigest;

    /**
     * 对方单位名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "otherpartyunit")
    private String otherpartyunit;

    /**
     * 创建时间
     */
    @NotNull(message = "创建时间不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 流程id
     */
    @ApiParam(hidden = true)
    @TableField(value = "requestid")
    private String requestid;

    /**
     * 合同类型：0 外购产品、1 纸张采购、2 外业务合作费、3 其他
     */
    @NotBlank(message = "合同类型：0 外购产品、1 纸张采购、2 外业务合作费、3 其他不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "contracttype")
    private String contracttype;

    /**
     * 其他信息（拼接）
     */
    @ApiParam(hidden = true)
    @TableField(value = "otherinfo")
    private String otherinfo;

}
