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
@TableName(value = "budget_year_startup")
@Data
public class BudgetYearStartup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建人id
     */
    @ApiParam(hidden = true)
    @TableField(value = "creator_id")
    private String creatorId;

    /**
     * 创建人名称
     */
    @ApiParam(hidden = true)
    @TableField(value = "creator_name")
    private String creatorName;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 届别id
     */
    @ApiParam(hidden = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 结束预算单位修改
     */
    @ApiParam(hidden = true)
    @TableField(value = "enduniteditflag")
    private Boolean enduniteditflag;

    /**
     * 结束预算科目修改
     */
    @ApiParam(hidden = true)
    @TableField(value = "endsubjecteditflag")
    private Boolean endsubjecteditflag;

    /**
     * 结束预算编制
     */
    @ApiParam(hidden = true)
    @TableField(value = "endbudgeteditflag")
    private Boolean endbudgeteditflag;

    /**
     * 启动预算编制
     */
    @ApiParam(hidden = true)
    @TableField(value = "startbudgetflag")
    private Boolean startbudgetflag;

}
