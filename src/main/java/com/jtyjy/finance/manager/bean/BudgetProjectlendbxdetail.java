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
import java.math.BigDecimal;

/**
 * @author Admin
 */
@TableName(value = "budget_projectlendbxdetail_new")
@Data
public class BudgetProjectlendbxdetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目借款主表id
     */
    @ApiParam(hidden = true)
    @TableField(value = "projectlendsumid")
    private Long projectlendsumid;

    /**
     * 报销金额
     */
    @NotNull(message = "报销金额不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "reimmoney")
    private BigDecimal reimmoney;

    /**
     * 月度动因id
     */
    @NotNull(message = "月度动因id不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthagentid")
    private Long monthagentid;

    /**
     * 动因名称
     */
    @NotBlank(message = "动因名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthagentname")
    private String monthagentname;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 科目名称
     */
    @NotBlank(message = "科目名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjectname")
    private String subjectname;

    /**
     * 开票单位id
     */
    @ApiParam(hidden = true)
    @TableField(value = "bunitid")
    private Long bunitid;

    /**
     * 开票单位名称
     */
    @NotBlank(message = "开票单位名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "bunitname")
    private String bunitname;

    /**
     * 月度动因预算
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthagentmoney")
    private BigDecimal monthagentmoney;

    /**
     * 月度动因未执行金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "monthagentunmoney")
    private BigDecimal monthagentunmoney;

    /**
     * 年度动因预算
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentmoney")
    private BigDecimal yearagentmoney;

    /**
     * 年度动因未执行金额
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentunmoney")
    private BigDecimal yearagentunmoney;

}
