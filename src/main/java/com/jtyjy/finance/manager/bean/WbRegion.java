package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "wb_region")
@Data
public class WbRegion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 区域代码6位;0000结尾的为省级；00为市级
     */
    @NotBlank(message = "区域代码6位;0000结尾的为省级；00为市级不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "region_code")
    private String regionCode;

    /**
     * 区域名称
     */
    @NotBlank(message = "区域名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "region_name")
    private String regionName;

    /**
     * 区域层级
     */
    @ApiParam(hidden = true)
    @TableField(value = "region_leve")
    private Integer regionLeve;

    /**
     * 区域父级（最上级为0）
     */
    @ApiParam(hidden = true)
    @TableField(value = "region_pcode")
    private String regionPcode;

}
