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
import java.math.BigDecimal;

/**
 * @author Admin
 */
@TableName(value = "wb_dept")
@Data
public class WbDept implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.ASSIGN_ID)
    private String deptId;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "DEPT_NAME")
    private String deptName;

    /**
     * 未知参数
     */
    @NotBlank(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "PARENT_DEPT")
    private String parentDept;

    /**
     * 未知参数
     */
    @NotNull(message = "未知参数不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "ORDER_INDEX")
    private BigDecimal orderIndex;

    /**
     * 未知参数
     */
    @ApiParam(hidden = true)
    @TableField(value = "OUT_KEY")
    private String outKey;

    /**
     * 所有上级部门ID
     */
    @ApiParam(hidden = true)
    @TableField(value = "PARENT_IDS")
    private String parentIds;

    /**
     * 部门全称
     */
    @ApiParam(hidden = true)
    @TableField(value = "DEPT_FULLNAME")
    private String deptFullname;

    /**
     * 1
     */
    @ApiParam(hidden = true)
    @TableField(value = "STATUS")
    private BigDecimal status;

}
