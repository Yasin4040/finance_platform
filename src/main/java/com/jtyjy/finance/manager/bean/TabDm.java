package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jtyjy.core.anno.OrderBy;
import com.jtyjy.core.anno.ResultSql;
import com.jtyjy.core.anno.Select;
import com.jtyjy.core.anno.SelectLike;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author konglingcheng
 */
@TableName(value = "tab_dm")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TabDm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 代码类型
     */
    @NotEmpty(message = "代码类型不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "dm_type")
    @Select(column = "dm_type")
    @OrderBy(column = "t.dm_type", way = OrderBy.DESC, order = 1)
    private String dmType;

    /**
     * 状态，1：启用   0：禁用
     */
    @NotEmpty(message = "状态不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "dm_status")
    @Select
    private String dmStatus;

    /**
     * 排序号
     */
    @ApiParam(hidden = true)
    @TableField(value = "dm_order")
    @OrderBy(column = "t.dm_order", way = OrderBy.ASC, order = 2)
    private Integer dmOrder;

    /**
     * 代码
     */
    @ApiParam(hidden = true)
    @TableField(value = "dm")
    private String dm;

    /**
     * 代码名称
     */
    @NotEmpty(message = "代码名称不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "dm_name")
    @SelectLike(location = SelectLike.FULL, prefix = "'", column = "dm_name")
    private String dmName;

    @TableField(exist = false)
    @ApiParam(hidden = true)
    @ResultSql(sql = "(SELECT a.dm_name FROM tab_dm a WHERE a.dm_type = 'PARENT' AND a.dm = t.dm_type) AS typeName")
    private String typeName;

    /**
     * 代码值
     */
    @ApiParam(hidden = true)
    @TableField(value = "dm_value")
    private String dmValue;

}
