package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_extractgrantlog")
@Data
public class BudgetExtractgrantlog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiModelProperty(hidden = false, value = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提成批次
     */
    @ApiModelProperty(hidden = false, value = "提成批次")
    @TableField(value = "extractmonth")
    private String extractmonth;

    /**
     * 是否公司员工 1 是 0 否
     */
    @ApiModelProperty(hidden = false, value = "是否公司员工")
    @TableField(value = "iscompanyemp")
    private Boolean iscompanyemp;

    /**
     * 身份证号
     */
    @ApiModelProperty(hidden = false, value = "身份证号")
    @TableField(value = "idnumber")
    private String idnumber;

    /**
     * 内部员工为工号，外部为编号
     */
    @ApiModelProperty(hidden = false, value = "编号")
    @TableField(value = "empno")
    private String empno;

    /**
     * 未知参数
     */
    @ApiModelProperty(hidden = false, value = "姓名")
    @TableField(value = "empname")
    private String empname;

    /**
     * 工资单位id
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "billingunitid")
    private Long billingunitid;

    /**
     * 工资单位
     */
    @ApiModelProperty(hidden = false, value = "工资单位")
    @TableField(value = "billingunitname")
    private String billingunitname;

    /**
     * 限额
     */
    @ApiModelProperty(hidden = false, value = "限额")
    @TableField(value = "excessmoney")
    private BigDecimal excessmoney;

    /**
     * 法人公司累计已发
     */
    @ApiModelProperty(hidden = false, value = "法人公司累计已发")
    @TableField(value = "alreadygrantmoney")
    private BigDecimal alreadygrantmoney;

    /**
     * 本次应发提成
     */
    @ApiModelProperty(hidden = false, value = "本次应发提成")
    @TableField(value = "shouldgrantextract")
    private BigDecimal shouldgrantextract;

    /**
     * 本次实发提成
     */
    @ApiModelProperty(hidden = false, value = "本次实发提成")
    @TableField(value = "couldgrantextract")
    private BigDecimal couldgrantextract;

    /**
     * 排序号
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "orderno")
    private Integer orderno;

    /**
     * 未知参数
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "cratetime")
    private Date cratetime;

    /**
     * 超额发放标记 1 设置超额发放时的发放日志
     */
    @ApiModelProperty(hidden = false, value = "是否超额发放")
    @TableField(value = "excessgrantflag")
    private Boolean excessgrantflag;

}
