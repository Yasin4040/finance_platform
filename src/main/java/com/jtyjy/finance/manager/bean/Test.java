package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "test")
@Data
public class Test implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiParam(hidden = true)
    @TableField(value = "str")
    private String str;

}
