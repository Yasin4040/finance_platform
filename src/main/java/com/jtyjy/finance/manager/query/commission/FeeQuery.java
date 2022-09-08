package com.jtyjy.finance.manager.query.commission;

import com.alibaba.excel.annotation.ExcelProperty;
import com.jtyjy.finance.manager.query.PageQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/08.
 * Time: 16:50
 */
@Data
public class FeeQuery extends PageQuery {
    @ApiModelProperty(value = "批次",required = true)
    private String ExtractMonth;
    @ApiModelProperty(value = "工号或者姓名")
    private String employeeName;
}
