package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @description:
 * @author: gll
 * @create: 2021-12-10 15:40
 */
@Data
@TableName("invoke_record")
public class DbInvokeRecord implements Serializable {

    /**
     * 主键Id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *银行账户编号
     */
    private Long bankId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建人工号
     */
    private String createOid;

    /**
     * 修改前数据
     */
    private String preBody;

    /**
     * 修改后数据
     */
    private String postBody;
}
