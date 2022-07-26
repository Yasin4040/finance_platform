package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jtyjy.core.interceptor.BaseUser;
import com.jtyjy.core.interceptor.LoginThreadLocal;
import com.jtyjy.core.log.DefaultChangeLog;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author Admin
 */
@TableName(value = "tab_change_log")
@Data
public class TabChangeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotBlank(message = "主键不能为空")
    @ApiParam(hidden = true)
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 操作人
     */
    @NotBlank(message = "操作人不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "operator_id")
    private String operatorId;

    /**
     * 操作人名
     */
    @ApiParam(hidden = true)
    @TableField(value = "operator_name")
    private String operatorName;

    /**
     * 工号
     */
    @ApiParam(hidden = true)
    @TableField(value = "username")
    private String username;

    /**
     * 业务表
     */
    @NotBlank(message = "业务表不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "table_name")
    private String tableName;

    /**
     * 动作
     */
    @NotBlank(message = "动作不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "action")
    private String action;

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "context")
    private String context;

    /**
     * 创建时间
     */
    @NotNull(message = "创建时间不能为空")
    @ApiParam(hidden = true)
    @TableField(value = "create_time")
    private Long createTime;

    /**
     * 获取当前操作表日志实例
     *
     * @param tableName 表名
     */
    public static DefaultChangeLog getInstance(String tableName) {
        DefaultChangeLog theLog = new DefaultChangeLog();
        BaseUser user = LoginThreadLocal.get();
        // 数据库对应的表名
        theLog.setTableName(tableName);
        //当前登录人的主键
        theLog.setOperatorId(user != null ? user.getEmpid().toString() : "1");
        //当前登录人的名称
        theLog.setUsername(user != null ? user.getEmpname() : "系统管理员");
        //当前登录人的工号
        theLog.setOperatorName(user != null ? user.getEmpno() : "10001");
        // 日志创建时间
        theLog.setCreateTime(System.currentTimeMillis());
        return theLog;
    }

}
