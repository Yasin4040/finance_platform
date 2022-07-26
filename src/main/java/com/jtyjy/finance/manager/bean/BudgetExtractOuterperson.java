package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_extract_outerperson")
@Data
public class BudgetExtractOuterperson implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiModelProperty(hidden = false,value="id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @ApiModelProperty(hidden = false, value = "身份证号")
    @TableField(value = "idnumber")
    private String idnumber;

    /**
     * 未知参数
     */
    @NotBlank(message = "人员编号不能为空")
    @ApiModelProperty(hidden = false, value = "人员编号")
    @TableField(value = "empno")
    private String empno;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    @ApiModelProperty(hidden = false, value = "姓名")
    @TableField(value = "name")
    private String name;

    /**
     * 银行账号
     */
    @NotBlank(message = "银行账号不能为空")
    @ApiModelProperty(hidden = false, value = "银行账号")
    @TableField(value = "bankaccount")
    private String bankaccount;

    /**
     * 电子银联号
     */
    @NotBlank(message = "开户行不能为空")
    @ApiModelProperty(hidden = false, value = "电子银联号")
    @TableField(value = "branchcode")
    private String branchcode;

    /**
     * 关联身份证号
     */
    @ApiModelProperty(hidden = false, value = "关联身份证号")
    @TableField(value = "referidnumber")
    private String referidnumber;

    /**
     * 排序号
     */
    @ApiModelProperty(hidden = false, value = "排序号")
    @TableField(value = "orderno")
    private Integer orderno = 0;

    /**
     * 是否停用
     */
    @ApiModelProperty(hidden = false, value = "是否停用。true停用false启用")
    @TableField(value = "stopflag")
    private Boolean stopflag;

    /**
     * 备注
     */
    @ApiModelProperty(hidden = false, value = "备注")
    @TableField(value = "remark")
    private String remark;

    /**
     * 创建时间
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "createtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createtime;

    /**
     * 更新时间
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "updatetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updatetime;

    /**
     * 发放单位id
     */
    @ApiModelProperty(hidden = false, value = "工资发放单位id")
    @TableField(value = "budgetbillingunitid")
    private Long budgetbillingunitid;
    
    @ApiModelProperty(hidden = true, value = "工资发放单位名称")
    @TableField(exist = false)
    private String billingUnitName;
    
    @ApiModelProperty(hidden = true, value = "开户行")
    @TableField(exist = false)
    private String openBank;

}
