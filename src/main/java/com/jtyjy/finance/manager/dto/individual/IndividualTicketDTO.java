package com.jtyjy.finance.manager.dto.individual;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 13:59
 */
@Data
public class IndividualTicketDTO {

//
//    @ApiModelProperty(value = "姓名")
//    @ExcelProperty(value = "*姓名")
//    private String employeeName;
//
//    @ApiModelProperty(value = "批次")
//    @ExcelProperty(value = "*批次")
//    private String batchNo;
//
//
//    @ApiModelProperty(value = "部门")
//    @ExcelProperty(value = "*事业群")
//    private String departmentName;
//
//    @ApiModelProperty(value = "省区/大区")
//    @ExcelProperty(value = "*省区/大区")
//    private String provinceOrRegion;
    @ApiModelProperty(value = "工号")
    private Integer employeeJobNum;
    @ApiModelProperty(value = "个体户档案id")
    private Long individualEmployeeInfoId;
    @ApiModelProperty(value = "个体户名称")
    private String individualName;
    @ApiModelProperty(value = "备注")
    private String remarks;
    List<IndividualTicketDetailsDTO> detailsDTOList;
}
