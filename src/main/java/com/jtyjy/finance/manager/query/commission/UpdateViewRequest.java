package com.jtyjy.finance.manager.query.commission;

import com.jtyjy.finance.manager.query.PageQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/08.
 * Time: 16:50
 */
@Data
public class UpdateViewRequest  {
    @ApiModelProperty(value = "批量 ids")
    private List<String> ids;
    @ApiModelProperty(value = "允许还是关闭  ture 允许 false关闭")
    private Boolean ifAllow;
    @ApiModelProperty(value = "是否是大区经理 true 大区经理 false 业务经理")
    private Boolean ifBig;
}
