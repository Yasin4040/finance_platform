package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_base_subject")
@Data
@ApiModel(description = "基础科目表")
public class BudgetBaseSubject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "基础科目id")
    private Long id;

    /**
     * 科目编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "code")
    @ApiModelProperty(value = "科目编号")
    private String code;

    /**
     * 科目名称
     */
    @NotBlank(message = "科目名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "name")
    @ApiModelProperty(value = "科目名称")
    private String name;

    /**
     * 停用标识 0：停用，1：不停用
     */
    @ApiParam(hidden = true)
    @TableField(value = "stopflag")
    @ApiModelProperty(value = "停用标识 0：停用，1：不停用")
    private Integer stopflag;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "createtime")
    @ApiModelProperty(value = "创建时间")
    private Date createtime;

    /**
     * 排序号
     */
    @ApiParam(hidden = true)
    @TableField(value = "orderno")
    @ApiModelProperty(value = "排序号")
    private Integer orderno;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark", updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 名称全拼
     */
    @ApiParam(hidden = true)
    @TableField(value = "fullspell")
    @ApiModelProperty(value = "名称全拼")
    private String fullspell;

    /**
     * 名称首拼
     */
    @ApiParam(hidden = true)
    @TableField(value = "firstspell")
    @ApiModelProperty(value = "名称首拼")
    private String firstspell;

    /**
     * 更新时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "updatetime")
    @ApiModelProperty(value = "更新时间")
    private Date updatetime;

}
