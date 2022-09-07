package com.jtyjy.finance.manager.vo.application;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/07.
 * Time: 11:11
 */

@Data
public class BudgetCommonAttachmentVO {
    /**
     *
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 关联id
     */
    @ApiModelProperty(value = "关联id 申请单id")
    private Long contactId;

    /**
     *
     */
    @ApiModelProperty(value = "文件类型  申请单 默认1", required = false, hidden = true)
    private Integer fileType;

    /**
     *
     */
    @ApiModelProperty(value = "文件地址")
    private String fileUrl;

    /**
     *
     */
    @ApiModelProperty(value = "文件扩展名", hidden = true)
    private String fileExtName;

    /**
     *
     */
    @ApiModelProperty(value = "文件名称")
    private String fileName;

    /**
     *
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     *
     */
    @ApiModelProperty(value = "创建人")
    private String creator;
}