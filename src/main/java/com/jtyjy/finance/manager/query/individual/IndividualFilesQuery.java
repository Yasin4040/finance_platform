package com.jtyjy.finance.manager.query.individual;
import com.jtyjy.finance.manager.query.PageQuery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.NumberFormat;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Pattern;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 11:57
 */
@Data
public class IndividualFilesQuery extends PageQuery {
    @ApiModelProperty(value = "批次")
    private String batchNo;
    @ApiModelProperty(value = "部门名称")
    private String departmentName;
    @ApiModelProperty(value = "省区/大区")
    private String provinceOrRegion;
    @ApiModelProperty(value = "员工工号")
//    @Pattern(regexp = "^[0-9]*$",message = "工号只能为数字")
    @Valid
    private Integer employeeJobNum;
    @ApiModelProperty(value = "姓名")
    private String employeeName;
    @ApiModelProperty(value = "账户类型 1个人 2公户")
    private Integer accountType;
    @ApiModelProperty(value = "发放单位")
    private String issuedUnit;
    @ApiModelProperty(value = "账号")
    private String account;
    @ApiModelProperty(value = "户名")
    private String accountName;
    @ApiModelProperty(value = "开户行")
    private String depositBank;
    @ApiModelProperty(value = "状态 1 正常  2停用")
    private Integer status;
    @ApiModelProperty(value = "发放意见")
    private String releaseOpinions;
}
