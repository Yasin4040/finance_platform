package com.jtyjy.finance.manager.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description: 分页基础类
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 11:57
 */
@ApiModel("分页查询业务对象")
@Data
public class PageQuery {
    @ApiModelProperty(value = "当前页码",name = "page")
    private Integer pageNum = 1;
    @ApiModelProperty(value = "每页条数",name = "rows")
    private Integer pageSize = 20;
}
