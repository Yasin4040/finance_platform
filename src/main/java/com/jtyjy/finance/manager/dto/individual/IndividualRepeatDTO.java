package com.jtyjy.finance.manager.dto.individual;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Description: 新增实体类
 * Created by ZiYao Lee on 2022/08/29.
 * Time: 09:23
 */
@Data
@ApiModel(value = "repeat新增实体类")
public class IndividualRepeatDTO {



    /**
     * 户名
     */
    @ApiModelProperty(value = "档案id,修改时候需要传，新增不需要",required = false)
    private Long id;
    /**
     * 户名
     */
    @ApiModelProperty(value = "户名")
    private String accountName;


}
