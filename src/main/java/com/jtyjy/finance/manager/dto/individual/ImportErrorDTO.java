package com.jtyjy.finance.manager.dto.individual;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 13:59
 */
@Data
public class ImportErrorDTO {

    /**
     * 导入验证错误原因
     */
    @ApiModelProperty(value = "验证格式错误")
    @ExcelProperty(value = "验证格式错误",index = 0)
    private String validationFormatError;

    /**
     * 导入验证错误原因
     */
    @ApiModelProperty(value = "插入数据库错误")
    @ExcelProperty(value = "插入数据库错误",index = 1)
    private String insertDatabaseError;


}
