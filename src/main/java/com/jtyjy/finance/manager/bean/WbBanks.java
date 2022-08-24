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
import java.io.Serializable;

/**
 * @author Admin
 */
@ApiModel(description = "开户行信息表")
@TableName(value = "wb_banks")
@Data
public class WbBanks implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 银行名称
     */
    @NotBlank(message = "支行名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "sub_branch_name")
    @ApiModelProperty(value = "支行名称")
    private String subBranchName;

    /**
     * 银行代码
     */
    @NotBlank(message = "联行号不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "sub_branch_code")
    @ApiModelProperty(value = "联行号")
    private String subBranchCode;

    /**
     * 省代码
     */
    @NotBlank(message = "省代码不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "province_code")
    @ApiModelProperty(value = "省代码")
    private String provinceCode;

    /**
     * 省名称
     */
    @NotBlank(message = "省名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "province")
    @ApiModelProperty(value = "省名称")
    private String province;

    /**
     * 城市代码
     */
    @NotBlank(message = "城市代码不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "city_code")
    @ApiModelProperty(value = "城市代码")
    private String cityCode;

    /**
     * 城市名称
     */
    @NotBlank(message = "城市名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "city")
    @ApiModelProperty(value = "城市名称")
    private String city;

    /**
     * 银行类型
     */
    @NotBlank(message = "银行类型不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "bank_name")
    @ApiModelProperty(value = "银行类型")
    private String bankName;

    // --------------------------------------------------

    /**
     * 开票单位Id
     */
    @TableField(exist = false)
    private Long billingUnitId;

    /**
     * 开票单位名称
     */
    @TableField(exist = false)
    private String billingUnitName;

    /**
     * 账户名称
     */
    @TableField(exist = false)
    private String accountName;

    /**
     * 银行账户
     */
    @TableField(exist = false)
    private String bankAccount;

    /**
     * 编号
     */
    @TableField(exist = false)
    private String bankCode;

}
