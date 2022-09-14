package com.jtyjy.finance.manager.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.commons.CommonsMultipartFile;


/**
 * Description:
 * Created by ZiYao Lee on 2021/11/16.
 * Time: 17:48
 */
@Data
@ApiModel(value = "核算入账查询实体")
public class AccountEntryQuery extends PageQuery {
    @ApiModelProperty("提成单号")
    private String extractCode;
    @ApiModelProperty("提成批次")
    private String extractMonth;
    @ApiModelProperty("部门名称")
    private String deptName;
    @ApiModelProperty("是否是历史数据 0 待做  1历史完成")
    private Integer status;
//    @ApiModelProperty(value = "做账单位")
//    private String unitName;
}
