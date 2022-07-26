package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_authorfeepay_rule")
@Data
public class BudgetAuthorfeepayRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "id(修改时需传)", dataType = "body")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    @NotNull(message = "规则名称不能为空")
    @ApiModelProperty(value = "规则名称", name = "name", dataType = "body", required = true)
    @TableField(value = "name")
    private String name;

    /**
     * 是否扣税 true 计税
     */
    @NotNull(message = "是否扣税不能为空")
    @ApiModelProperty(value = "是否扣税 true 计税 false否", dataType = "body", required = true)
    @TableField(value = "taxflag")
    private Boolean taxflag;

    /**
     * 是否终止 true 终止 ， 可以提前终止
     */
    @NotNull(message = "是否终止不能为空")
    @ApiModelProperty(value = "是否终止 true是 false否", dataType = "body", required = true)
    @TableField(value = "endflag")
    private Boolean endflag;

    /**
     * 生效开始日期
     */
    @ApiModelProperty(value = "生效日期（yyyy-mm-dd）", dataType = "body", required = true)
    @TableField(value = "effectdate")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date effectdate;

    /**
     * 更新时间
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "updatetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatetime;

    /**
     * 创建时间
     */
    @ApiModelProperty(hidden = true)
    @TableField(value = "createtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createtime;

    /**
     * 备注
     */
    @ApiModelProperty(hidden = false, dataType = "body", value = "备注")
    @TableField(value = "remark")
    private String remark;


}
