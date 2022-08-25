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
@TableName(value = "budget_extractdetail")
@Data
public class BudgetExtractdetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "id(新增时不传，修改时传)")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 还款id
     */
    @ApiModelProperty(value = "还款单id（扣款明细中使用）", hidden = false)
    @TableField(value = "repaymoneyid")
    private Long repaymoneyid;

    /**
     * 汇总id
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "extractsumid")
    private Long extractsumid;

    /**
     * 提成人id
     */
    @ApiModelProperty(value = "人员id", hidden = false)
    @TableField(value = "empid")
    private String empid;

    /**
     * 提成人工号
     */
    @ApiModelProperty(value = "人员编号", hidden = false)
    @TableField(value = "empno")
    private String empno;

    /**
     * 提成人名称
     */
    @ApiModelProperty(value = "人员名称", hidden = false)
    @TableField(value = "empname")
    private String empname;

    /**
     * 身份证号
     */
    @ApiModelProperty(value = "人员身份证号", hidden = false)
    @TableField(value = "idnumber")
    private String idnumber;

    /**
     * 应付提成 （不含税）
     */
    @ApiModelProperty(value = "应发提成", hidden = false)
    @TableField(value = "copeextract")
    private BigDecimal copeextract;

    /**
     * 综合税
     */
    @ApiModelProperty(value = "综合税", hidden = false)
    @TableField(value = "consotax")
    private BigDecimal consotax;

    /**
     * 补贴税 （如何发放）
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "subsidytax")
    private BigDecimal subsidytax;

    /**
     * 补贴税状态（true为财务总监审核通过状态）
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "subsidytaxstatus")
    private Integer subsidytaxstatus;

    /**
     * 流程id
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "requestid")
    private String requestid;

    /**
     * 扣款（借款还款）
     */
    @ApiModelProperty(value = "扣款金额", hidden = false)
    @TableField(value = "withholdmoney")
    private BigDecimal withholdmoney;

    /**
     * 实付提成  （应付提成 - 扣款）
     */
    @ApiModelProperty(value = "实发提成", hidden = false)
    @TableField(value = "realextract")
    private BigDecimal realextract;

    /**
     * 创建时间
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 更新时间
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 删除标记
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "deleteflag")
    private Integer deleteflag;

    /**
     * 是否公司员工 1 是 0 否
     */
    @ApiModelProperty(value = "是否公司员工", hidden = false)
    @TableField(value = "iscompanyemp")
    private Boolean iscompanyemp;

    /**
     * 超额类型 0 未超额 1超额且发完 2 超额未发完
     */
    @ApiModelProperty(value = "超额类型", hidden = false)
    @TableField(value = "excesstype")
    private Integer excesstype;

    /**
     * 超额金额
     */
    @ApiModelProperty(value = "超额金额", hidden = false)
    @TableField(value = "excessmoney")
    private BigDecimal excessmoney;

    /**
     * 是否处理超额 1 已处理
     */
    @ApiModelProperty(value = "是否处理超额", hidden = false)
    @TableField(value = "handleflag")
    private Boolean handleflag;
    /**
     * 员工个体户id
     */
    @ApiModelProperty(value = "员工个体户id", hidden = false)
    @TableField(value = "individual_employee_id")
    private Integer individualEmployeeId;
    @ApiModelProperty(value = "提成批次", hidden = false)
    @TableField(exist = false)
    private String extractmonth;

}
