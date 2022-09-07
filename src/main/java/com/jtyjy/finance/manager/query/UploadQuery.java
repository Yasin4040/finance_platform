package com.jtyjy.finance.manager.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.util.List;


/**
 * Description:
 * Created by ZiYao Lee on 2021/11/16.
 * Time: 17:48
 */
@Data
public class UploadQuery {
    @ApiModelProperty("申请单关联id")
    private Long contactId;

    @ApiModelProperty(value = "关联资源类型 默认 1  提成申请单",required = false)
    private Integer fileType = 1;
//    @ApiModelProperty(value = "是否覆盖",required = false)
//    private boolean isCover;
//    @ApiModelProperty(value = "文件list",required = false)
//    private List<MultipartFile> files;

    @ApiModelProperty(value = "文件list",required = false)
    private CommonsMultipartFile[] files;
}
