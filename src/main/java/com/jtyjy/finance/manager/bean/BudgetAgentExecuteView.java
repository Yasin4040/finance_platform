package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName(value = "budget_agent_execute_view")
@Data
public class BudgetAgentExecuteView implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "reuqeststatus")
    private Integer reuqeststatus;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimflag")
    private Boolean reimflag;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthid")
    private Long monthid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "baseunitid")
    private Long baseunitid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "unitpids")
    private String unitpids;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unitpid")
    private Long unitpid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "basesubjectid")
    private Long basesubjectid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectpids")
    private String subjectpids;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "subjectpid")
    private Long subjectpid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearagentid")
    private Long yearagentid;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthagentid")
    private Long monthagentid;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthagentname")
    private String monthagentname;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "yearagentmoney")
    private BigDecimal yearagentmoney;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "monthagentmoney")
    private BigDecimal monthagentmoney;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "executemoney")
    private BigDecimal executemoney;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimdate")
    private String reimdate;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "verifytime")
    private String verifytime;

}
