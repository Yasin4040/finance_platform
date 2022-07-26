package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_extractsum")
@Data
public class BudgetExtractsum implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提成单号
     */
    @ApiParam(hidden = true)
    @TableField(value = "code")
    private String code;

    /**
     * 状态(-1:退回 0：未审核 1：已审核(数据字典))
     */
    @ApiParam(hidden = true)
    @TableField(value = "status")
    private Integer status;

    /**
     * 届别id
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 预算单位Id
     */
    @ApiParam(hidden = true)
    @TableField(value = "deptid")
    private String deptid;

    /**
     * 预算单位名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "deptname")
    private String deptname;

    /**
     * 对应工资月份
     */
    @ApiParam(hidden = true)
    @TableField(value = "salarymonth")
    private String salarymonth;

    /**
     * 提成月份
     */
    @ApiParam(hidden = true)
    @TableField(value = "extractmonth")
    private String extractmonth;

    /**
     * 提成次数 目前一个月两次
     */
    @ApiParam(hidden = true)
    @TableField(value = "extractseq")
    private Integer extractseq;

    /**
     * 提成人数
     */
    @ApiParam(hidden = true)
    @TableField(value = "extractnum")
    private Integer extractnum;

    /**
     * 导入时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 创建人id
     */
    @ApiParam(hidden = true)
    @TableField(value = "creator")
    private String creator;

    /**
     * 创建人姓名
     */
    @ApiParam(hidden = true)
    @TableField(value = "createorname")
    private String createorname;

    /**
     * 更改时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    private Date updatetime;

    /**
     * 审批意见
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 删除标记 1为已删除
     */
    @ApiParam(hidden = true)
    @TableField(value = "deleteflag")
    private Integer deleteflag;

    /**
     * 是否报销（0:不走报销,1:走报销）
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimbursementflag")
    private Integer reimbursementflag;

    /**
     * 报销科目
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimbursementsubjectname")
    private String reimbursementsubjectname;

    /**
     * 报销动因
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimbursementagentname")
    private String reimbursementagentname;

    /**
     * 报销科目id
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimbursementsubjectid")
    private Long reimbursementsubjectid;

    /**
     * 报销动因id
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimbursementagentid")
    private Long reimbursementagentid;

    /**
     * 预算人员id
     */
    @ApiParam(hidden = true)
    @TableField(value = "budgetuserid")
    private String budgetuserid;

    /**
     * 二维码
     */
    @ApiParam(hidden = true)
    @TableField(value = "qrcodebase64str")
    private String qrcodebase64str;

    /**
     * 审批人id
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifyorid")
    private String verifyorid;

    /**
     * 审核时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifytime")
    private Date verifytime;

    /**
     * 审核人姓名
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifyorname")
    private String verifyorname;

    /**
     * 重新提交人(工号)
     */
    @ApiParam(hidden = true)
    @TableField(value = "resubmitor")
    private String resubmitor;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "resubmitorname")
    private String resubmitorname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "resubmittime")
    private Date resubmittime;

}
