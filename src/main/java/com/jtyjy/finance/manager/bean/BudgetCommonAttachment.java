package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName budget_common_attachment
 */
@TableName(value ="budget_common_attachment")
@Data
public class BudgetCommonAttachment implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联id
     */
    @TableField(value = "contact_id")
    private Long contactId;

    /**
     * 
     */
    @TableField(value = "file_type")
    private Integer fileType;

    /**
     * 
     */
    @TableField(value = "file_url")
    private String fileUrl;

    /**
     * 
     */
    @TableField(value = "file_ext_name")
    private String fileExtName;

    /**
     * 
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 
     */
    @TableField(value = "creator")
    private String creator;

    @TableField(value = "oa_password")
    private String oaPassword;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}