package com.jtyjy.finance.manager.dto.individual;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.jtyjy.finance.manager.annotation.ExcelDecimalValid;
import com.jtyjy.finance.manager.annotation.ExcelValid;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 13:59
 */
@Data
public class IndividualImportDTO {


    /**
     * 批次
     */
    @ApiModelProperty(value = "批次")
    @ExcelProperty(value = "*批次")
    @ExcelValid(message = "批次不能为空")
    private String batchNo;
//
//    @ApiModelProperty(value = "事业群")
//    @ExcelProperty(value = "*事业群",index = 2)
//    private String businessGroup;
//
//    @ApiModelProperty(value = "部门名称")
//    @ExcelProperty(value = "*部门")
//    private String departmentName;

    @ApiModelProperty(value = "省区/大区")
    @ExcelProperty(value = "*省区/大区")
    @ExcelValid(message = "省区/大区不能为空")
    private String provinceOrRegion;

    @ApiModelProperty(value = "员工工号")
    @ExcelProperty(value = "*工号")
    @ExcelValid(message = "工号不能为空")
    private Integer employeeJobNum;

    @ApiModelProperty(value = "员工名称")
    @ExcelProperty(value = "*姓名")
    @ExcelValid(message = "姓名不能为空")
    private String employeeName;

    @ApiModelProperty(value = "联系电话")
    @ExcelProperty(value = "*联系电话")
    @ExcelValid(message = "联系电话不能为空")
    private String phone;

    @ApiModelProperty(value = "*账户类型  1个卡 2 公户")
    @ExcelProperty(value = "*账户类型(个卡/公户)")
    @ExcelValid(message = "账户类型不能为空")
    private String accountType;

    @ApiModelProperty(value = "社保停发日期")
    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty(value = "社保停发日期")
    private Date socialSecurityStopDate;

    @ApiModelProperty(value = "离职日期")
    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty(value = "离职日期")
    private Date leaveDate;
    @ApiModelProperty(value = "户名")
    @ExcelProperty(value = "*户名")
    @ExcelValid(message = "户名不能为空")
    private String accountName;

    @ApiModelProperty(value = "账号")
    @ExcelProperty(value = "*账号")
    @ExcelValid(message = "账号不能为空")
    private String account;

    @ApiModelProperty(value = "银行类型")
    @ExcelProperty(value = "*银行类型")
    private String bankType;

    @ApiModelProperty(value = "开户行")
    @ExcelProperty(value = "*开户行")
    @ExcelValid(message = "开户行不能为空")
    private String depositBank;

    @ApiModelProperty(value = "省份")
    @ExcelProperty(value = "*省份")
    private String province;

    @ApiModelProperty(value = "城市")
    @ExcelProperty(value = "*城市")
    private String city;

    @ApiModelProperty(value = "电子联行号")
    @ExcelProperty(value = "*电子联行号")
    private String electronicInterBankNo;


    @ApiModelProperty(value = "发放单位")
    @ExcelProperty(value = "*发放单位")
    @ExcelValid(message = "发放单位不能为空")
    private String issuedUnit;

    @ApiModelProperty(value = "发放意见")
    @ExcelProperty(value = "*发放意见")
    @ExcelValid(message = "发放意见不能为空")
    private String releaseOpinions;

    @ApiModelProperty(value = "服务协议")
    @ExcelProperty(value = "服务协议")
    private String serviceAgreement;

    @ApiModelProperty(value = "自办还是代办  1自办 2 代办")
    @ExcelProperty(value = "自办/代办")
    private String selfOrAgency;

    @ApiModelProperty(value = "平台公司")
    @ExcelProperty(value = "平台公司")
    private String platformCompany;

    @ApiModelProperty(value = "核定/查账")
    @ExcelProperty(value = "核定/查账")
    private String verificationAudit;

    @ApiModelProperty(value = "年额度")
    @ExcelProperty(value = "*年额度")
    @ExcelValid(message = "年额度不能为空")
    private BigDecimal annualQuota;

    @ApiModelProperty(value = "备注")
    @ExcelProperty(value = "备注")
    private String remarks;

}
