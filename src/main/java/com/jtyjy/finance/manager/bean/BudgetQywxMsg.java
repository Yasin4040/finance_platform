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
@TableName(value = "budget_qywx_msg")
@Data
public class BudgetQywxMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "toparty")
    private String toparty;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "totag")
    private String totag;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "touser")
    private String touser;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "msgtype")
    private String msgtype;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "content")
    private String content;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "btntxt")
    private String btntxt;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "description")
    private String description;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "title")
    private String title;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "url")
    private String url;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "success")
    private Boolean success;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "errmsg")
    private String errmsg;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

}
