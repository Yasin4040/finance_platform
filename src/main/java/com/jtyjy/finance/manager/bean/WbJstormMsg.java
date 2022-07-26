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
@TableName(value = "wb_jstorm_msg")
@Data
public class WbJstormMsg implements Serializable {

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
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "topic")
    private String topic;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "status")
    private Integer status;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "msgcontents")
    private String msgcontents;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "sendnum")
    private Integer sendnum;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "firstsendtime")
    private Date firstsendtime;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "lastsendtime")
    private Date lastsendtime;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "allowerrornum")
    private Integer allowerrornum;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "errornum")
    private Integer errornum;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "lasterrortime")
    private Date lasterrortime;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "errormsg")
    private String errormsg;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    private Date createtime;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "groupname")
    private String groupname;

}
