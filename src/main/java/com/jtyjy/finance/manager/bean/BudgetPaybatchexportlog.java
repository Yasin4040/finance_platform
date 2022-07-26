package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_paybatchexportlog_new")
@Data
public class BudgetPaybatchexportlog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "paybatchid")
    private Long paybatchid;

    /**
     * 导出人工号
     */
    @ApiParam(hidden = true)
    @TableField(value = "exportor")
    private String exportor;

    /**
     * 导出人姓名
     */
    @ApiParam(hidden = true)
    @TableField(value = "exportname")
    private String exportname;

    /**
     * 导出时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "exporttime")
    private Date exporttime;

}
