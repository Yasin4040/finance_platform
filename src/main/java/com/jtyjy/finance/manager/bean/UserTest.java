package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "user_test")
@Data
public class UserTest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "_empname")
    private String Empname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "_empno")
    private String Empno;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "_deptname")
    private String Deptname;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "_status")
    private String Status;

}
