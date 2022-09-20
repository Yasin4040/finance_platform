package com.jtyjy.finance.manager.query.commission;

import com.jtyjy.finance.manager.query.PageQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/08.
 * Time: 16:50
 */
@Data
public class CommissionQuery extends PageQuery {
    @ApiModelProperty(value = "工号或者姓名")
    private String employeeName;
    @ApiModelProperty(value = "部门name(客户类型)")
    private String departmentName;
    @ApiModelProperty(value = "预算单位")
    private String budgetUnitName;
    @ApiModelProperty(value = "届别id")
    private String yearId;
    @ApiModelProperty(value = "月份id")
    private String monthId;
    @ApiModelProperty(value = "批次")
    private String ExtractMonth;

}
