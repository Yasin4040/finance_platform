package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_extractquota_rule")
@Data
public class BudgetExtractquotaRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(hidden = false, value = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 限额规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    @ApiModelProperty(hidden = false, value = "规则名称", required = true)
    @TableField(value = "name")
    private String name;

    /**
     * 工资发放单位ids
     */
    @NotBlank(message = "工资发放单位不能为空")
    @ApiModelProperty(hidden = false, value = "工资发放单位id(以,分隔)", required = true)
    @TableField(value = "billunitids")
    private String billunitids;

    /**
     * 工资发放单位名称
     */
    @ApiModelProperty(hidden = true, value = "工资发放单位名称(以,分隔)")
    @TableField(value = "billunitnames")
    private String billunitnames;

    /**
     * 是否终止
     */
    @ApiModelProperty(hidden = false, value = "是否终止 true终止 false不终止", required = true)
    @TableField(value = "endflag")
    private Boolean endflag;

    /**
     * 生效时间
     */
    @ApiModelProperty(hidden = false, value = "生效时间(年-月-日)", required = true)
    @TableField(value = "effectdate")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date effectdate;

    /**
     * 创建时间
     */
    @ApiModelProperty(hidden = true, value = "创建时间(年-月-日-时-分-秒)", required = false)
    @TableField(value = "createtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createtime;

    /**
     * 更新时间
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "updatetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatetime;

    /**
     * 备注
     */
    @ApiModelProperty(hidden = false, value = "备注", required = false)
    @TableField(value = "remark")
    private String remark;

}
