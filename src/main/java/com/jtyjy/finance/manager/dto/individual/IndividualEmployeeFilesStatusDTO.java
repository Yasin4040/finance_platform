package com.jtyjy.finance.manager.dto.individual;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Description: 新增实体类
 * Created by ZiYao Lee on 2022/08/29.
 * Time: 09:23
 */
@Data
@ApiModel(value = "新增实体类")
public class IndividualEmployeeFilesStatusDTO {

    /**
     * id
     */
    @ApiModelProperty(value = "ids")
    private List<Long> ids;
    /**
     * 状态 1 正常  2停用
     */
    @ApiModelProperty(value = "状态 1 正常  2停用")
    private Integer status;

}
