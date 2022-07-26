package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "wb_user")
@Data
@ApiModel(description = "用户表")
public class WbUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.ASSIGN_ID, value = "user_id")
    @ApiModelProperty(value = "用户id")
    private String userId;

    /**
     * 工号
     */
    @NotBlank(message = "工号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "USER_NAME")
    @ApiModelProperty(value = "工号")
    private String userName;

    /**
     * 用户名称
     */
    @NotBlank(message = "用户名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "DISPLAY_NAME")
    @ApiModelProperty(value = "用户名称")
    private String displayName;

    /**
     * 身份证号码
     */
    @NotBlank(message = "身份证号码不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "ID_NUMBER")
    @ApiModelProperty(value = "身份证号码")
    private String idNumber;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "PASSWORD")
    @ApiModelProperty(value = "密码")
    private String password;

    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "STATUS")
    @ApiModelProperty(value = "状态")
    private BigDecimal status;

    /**
     * 创建时间
     */
    @NotNull(message = "创建时间不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "CREATE_DATE")
    @ApiModelProperty(value = "创建时间")
    private Date createDate;

    /**
     * 登录次数
     */
    @NotNull(message = "登录次数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "LOGIN_TIMES")
    @ApiModelProperty(value = "登录次数")
    private BigDecimal loginTimes;

    /**
     * 邮箱
     */
    @ApiParam(hidden = true)
    @TableField(value = "EMAIL")
    @ApiModelProperty(value = "邮箱")
    private String email;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "USE_LANG")
    @ApiModelProperty(value = "主键id")
    private String useLang;

    /**
     * 最后登录时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "LAST_LOGIN")
    @ApiModelProperty(value = "最后登录时间")
    private Date lastLogin;

    /**
     * 第三方id
     */
    @ApiParam(hidden = true)
    @TableField(value = "OUTKEY")
    @ApiModelProperty(value = "第三方id")
    private String outkey;

    // --------------------------------------------------

    /**
     * 部门Id
     */
    @TableField(exist = false)
    private String deptId;

    /**
     * 部门名称
     */
    @TableField(exist = false)
    private String deptName;

    /**
     * 部门完整名称
     */
    @TableField(exist = false)
    private String deptFullName;


}
