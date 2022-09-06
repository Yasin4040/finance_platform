package com.jtyjy.finance.manager.dto.individual;

import com.jtyjy.finance.manager.vo.individual.IndividualEmployeeFilesVO;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 13:59
 */
@Data
public class IndividualTicketInfoDTO {

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
    private Long ticketId;
    private String ticketCode;
    private IndividualEmployeeFilesVO filesVO;
    List<IndividualTicketDetailsDTO> detailsDTOList;
}
