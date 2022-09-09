package com.jtyjy.finance.manager.dto.individual;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 13:59
 */
@Data
public class IndividualExportDTO {

//    @ApiModelProperty(value = "id")
//    @ExcelProperty(value = "序号",index = 0)
//    private Integer id;
    /**
     * 批次
     */
    @ApiModelProperty(value = "批次")
    @ExcelProperty(value = "*批次",index = 0)
    private String batchNo;
//
//    @ApiModelProperty(value = "事业群")
//    @ExcelProperty(value = "*事业群",index = 2)
//    private String businessGroup;
//

    @ApiModelProperty(value = "部门名称")
    @ExcelProperty(value = "*事业群",index = 1)
    private String departmentName;

    @ApiModelProperty(value = "省区/大区")
    @ExcelProperty(value = "*省区/大区",index = 2)
    private String provinceOrRegion;

    @ApiModelProperty(value = "员工工号")
    @ExcelProperty(value = "*工号",index = 3)
    private Integer employeeJobNum;

    @ApiModelProperty(value = "员工名称")
    @ExcelProperty(value = "*姓名",index = 4)
    private String employeeName;

    @ApiModelProperty(value = "联系电话")
    @ExcelProperty(value = "*联系电话",index = 5)
    private String phone;

    @ApiModelProperty(value = "*账户类型  1个卡 2 公户")
    @ExcelProperty(value = "*账户类型(个卡/公户)",index = 6)
    private String accountType;

    @ApiModelProperty(value = "户名")
    @ExcelProperty(value = "*户名",index = 7)
    private String accountName;

    @ApiModelProperty(value = "账号")
    @ExcelProperty(value = "*账号",index = 8)
    private String account;

    @ApiModelProperty(value = "银行类型")
    @ExcelProperty(value = "*银行类型",index = 9)
    private String bankType;

    @ApiModelProperty(value = "开户行")
    @ExcelProperty(value = "*开户行",index = 10)
    private String depositBank;

    @ApiModelProperty(value = "省份")
    @ExcelProperty(value = "*省份",index = 11)
    private String province;

    @ApiModelProperty(value = "城市")
    @ExcelProperty(value = "*城市",index = 12)
    private String city;

    @ApiModelProperty(value = "电子联行号")
    @ExcelProperty(value = "*电子联行号",index = 13)
    private String electronicInterBankNo;


    @ApiModelProperty(value = "发放单位")
    @ExcelProperty(value = "*发放单位",index = 14)
    private String issuedUnit;

    @ApiModelProperty(value = "发放意见")
    @ExcelProperty(value = "*发放意见",index = 15)
    private String releaseOpinions;

    @ApiModelProperty(value = "社保停发日期")
    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty(value = "社保停发日期",index = 16)
    private Date socialSecurityStopDate;

    @ApiModelProperty(value = "离职日期")
    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty(value = "离职日期",index = 17)
    private Date leaveDate;

    @ApiModelProperty(value = "服务协议")
    @ExcelProperty(value = "服务协议",index = 18)
    private String serviceAgreement;

    @ApiModelProperty(value = "自办还是代办  1自办 2 代办")
    @ExcelProperty(value = "自办/代办",index = 19)
    private String selfOrAgency;

    @ApiModelProperty(value = "平台公司")
    @ExcelProperty(value = "平台公司",index = 20)
    private String platformCompany;

    @ApiModelProperty(value = "核定/查账")
    @ExcelProperty(value = "核定/查账",index = 21)
    private String verificationAudit;

    @ApiModelProperty(value = "年额度")
    @ExcelProperty(value = "*年额度",index = 22)
    private BigDecimal annualQuota;

    @ApiModelProperty(value = "备注")
    @ExcelProperty(value = "备注",index = 23)
    private String remarks;


}
