package com.jtyjy.finance.manager.bean;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * @author Admin
 */
@TableName(value = "wb_person")
@Data
public class WbPerson implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(value = "PERSON_ID",type=IdType.ASSIGN_ID)
    private String personId;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "USER_ID")
    private String userId;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "DEPT_ID")
    private String deptId;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "PERSON_CODE")
    private String personCode;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "PERSON_NAME")
    private String personName;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "SEX")
    private String sex;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "BIRTHDATE")
    private Date birthdate;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "NATION")
    private String nation;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "EDU")
    private String edu;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "SCHOOL")
    private String school;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "MAJOR")
    private String major;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "MOBILE_PHONE")
    private String mobilePhone;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "ADDR")
    private String addr;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "REMARK")
    private String remark;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "PIC")
    private String pic;

}
