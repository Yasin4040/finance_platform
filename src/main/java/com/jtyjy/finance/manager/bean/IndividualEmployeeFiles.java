package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 员工个体户档案
 * @TableName budget_individual_employee_files
 */
@TableName(value ="budget_individual_employee_files")
@Data
public class IndividualEmployeeFiles implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工工号
     */
    @TableField(value = "employee_job_num")
    private Integer employeeJobNum;

    /**
     * 联系电话

     */
    @TableField(value = "phone")
    private Integer phone;

    /**
     * 账户类型  1个人 2 公户
     */
    @TableField(value = "account_type")
    private Integer accountType;

    /**
     * 户名

     */
    @TableField(value = "account_name")
    private String accountName;

    /**
     * 开户行
     */
    @TableField(value = "deposit_bank")
    private String depositBank;

    /**
     * 发放单位
     */
    @TableField(value = "issued_unit")
    private String issuedUnit;

    /**
     * 发放意见
     */
    @TableField(value = "release_opinions")
    private String releaseOpinions;

    /**
     * 社保停发日期
     */
    @TableField(value = "social_security_stop_date")
    private Date socialSecurityStopDate;

    /**
     * 离职日期
     */
    @TableField(value = "leave_date")
    private Date leaveDate;

    /**
     * 服务协议
     */
    @TableField(value = "service_agreement")
    private String serviceAgreement;

    /**
     * 自办还是代办  1自办 2 代办
     */
    @TableField(value = "self_or_agency")
    private Integer selfOrAgency;

    /**
     * 平台公司
     */
    @TableField(value = "platform_company")
    private String platformCompany;

    /**
     * 核定/查账
     */
    @TableField(value = "verification_audit")
    private String verificationAudit;

    /**
     * 年额度
     */
    @TableField(value = "annual_quota")
    private Integer annualQuota;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "udpate_time")
    private Date udpateTime;

    /**
     * 状态 1 正常  2停用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 批次
     */
    @TableField(value = "batch_no")
    private String batchNo;

    /**
     * 部门
     */
    @TableField(value = "department_no")
    private String departmentNo;

    /**
     * 部门名称
     */
    @TableField(value = "department_name")
    private String departmentName;

    /**
     * 省区/大区
     */
    @TableField(value = "province_or_region")
    private String provinceOrRegion;

    /**
     * 员工名称
     */
    @TableField(value = "employee_name")
    private String employeeName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        IndividualEmployeeFiles other = (IndividualEmployeeFiles) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getEmployeeJobNum() == null ? other.getEmployeeJobNum() == null : this.getEmployeeJobNum().equals(other.getEmployeeJobNum()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getAccountType() == null ? other.getAccountType() == null : this.getAccountType().equals(other.getAccountType()))
            && (this.getAccountName() == null ? other.getAccountName() == null : this.getAccountName().equals(other.getAccountName()))
            && (this.getDepositBank() == null ? other.getDepositBank() == null : this.getDepositBank().equals(other.getDepositBank()))
            && (this.getIssuedUnit() == null ? other.getIssuedUnit() == null : this.getIssuedUnit().equals(other.getIssuedUnit()))
            && (this.getReleaseOpinions() == null ? other.getReleaseOpinions() == null : this.getReleaseOpinions().equals(other.getReleaseOpinions()))
            && (this.getSocialSecurityStopDate() == null ? other.getSocialSecurityStopDate() == null : this.getSocialSecurityStopDate().equals(other.getSocialSecurityStopDate()))
            && (this.getLeaveDate() == null ? other.getLeaveDate() == null : this.getLeaveDate().equals(other.getLeaveDate()))
            && (this.getServiceAgreement() == null ? other.getServiceAgreement() == null : this.getServiceAgreement().equals(other.getServiceAgreement()))
            && (this.getSelfOrAgency() == null ? other.getSelfOrAgency() == null : this.getSelfOrAgency().equals(other.getSelfOrAgency()))
            && (this.getPlatformCompany() == null ? other.getPlatformCompany() == null : this.getPlatformCompany().equals(other.getPlatformCompany()))
            && (this.getVerificationAudit() == null ? other.getVerificationAudit() == null : this.getVerificationAudit().equals(other.getVerificationAudit()))
            && (this.getAnnualQuota() == null ? other.getAnnualQuota() == null : this.getAnnualQuota().equals(other.getAnnualQuota()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUdpateTime() == null ? other.getUdpateTime() == null : this.getUdpateTime().equals(other.getUdpateTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getBatchNo() == null ? other.getBatchNo() == null : this.getBatchNo().equals(other.getBatchNo()))
            && (this.getDepartmentNo() == null ? other.getDepartmentNo() == null : this.getDepartmentNo().equals(other.getDepartmentNo()))
            && (this.getDepartmentName() == null ? other.getDepartmentName() == null : this.getDepartmentName().equals(other.getDepartmentName()))
            && (this.getProvinceOrRegion() == null ? other.getProvinceOrRegion() == null : this.getProvinceOrRegion().equals(other.getProvinceOrRegion()))
            && (this.getEmployeeName() == null ? other.getEmployeeName() == null : this.getEmployeeName().equals(other.getEmployeeName()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getEmployeeJobNum() == null) ? 0 : getEmployeeJobNum().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getAccountType() == null) ? 0 : getAccountType().hashCode());
        result = prime * result + ((getAccountName() == null) ? 0 : getAccountName().hashCode());
        result = prime * result + ((getDepositBank() == null) ? 0 : getDepositBank().hashCode());
        result = prime * result + ((getIssuedUnit() == null) ? 0 : getIssuedUnit().hashCode());
        result = prime * result + ((getReleaseOpinions() == null) ? 0 : getReleaseOpinions().hashCode());
        result = prime * result + ((getSocialSecurityStopDate() == null) ? 0 : getSocialSecurityStopDate().hashCode());
        result = prime * result + ((getLeaveDate() == null) ? 0 : getLeaveDate().hashCode());
        result = prime * result + ((getServiceAgreement() == null) ? 0 : getServiceAgreement().hashCode());
        result = prime * result + ((getSelfOrAgency() == null) ? 0 : getSelfOrAgency().hashCode());
        result = prime * result + ((getPlatformCompany() == null) ? 0 : getPlatformCompany().hashCode());
        result = prime * result + ((getVerificationAudit() == null) ? 0 : getVerificationAudit().hashCode());
        result = prime * result + ((getAnnualQuota() == null) ? 0 : getAnnualQuota().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUdpateTime() == null) ? 0 : getUdpateTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getBatchNo() == null) ? 0 : getBatchNo().hashCode());
        result = prime * result + ((getDepartmentNo() == null) ? 0 : getDepartmentNo().hashCode());
        result = prime * result + ((getDepartmentName() == null) ? 0 : getDepartmentName().hashCode());
        result = prime * result + ((getProvinceOrRegion() == null) ? 0 : getProvinceOrRegion().hashCode());
        result = prime * result + ((getEmployeeName() == null) ? 0 : getEmployeeName().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", employeeJobNum=").append(employeeJobNum);
        sb.append(", phone=").append(phone);
        sb.append(", accountType=").append(accountType);
        sb.append(", accountName=").append(accountName);
        sb.append(", depositBank=").append(depositBank);
        sb.append(", issuedUnit=").append(issuedUnit);
        sb.append(", releaseOpinions=").append(releaseOpinions);
        sb.append(", socialSecurityStopDate=").append(socialSecurityStopDate);
        sb.append(", leaveDate=").append(leaveDate);
        sb.append(", serviceAgreement=").append(serviceAgreement);
        sb.append(", selfOrAgency=").append(selfOrAgency);
        sb.append(", platformCompany=").append(platformCompany);
        sb.append(", verificationAudit=").append(verificationAudit);
        sb.append(", annualQuota=").append(annualQuota);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", udpateTime=").append(udpateTime);
        sb.append(", status=").append(status);
        sb.append(", batchNo=").append(batchNo);
        sb.append(", departmentNo=").append(departmentNo);
        sb.append(", departmentName=").append(departmentName);
        sb.append(", provinceOrRegion=").append(provinceOrRegion);
        sb.append(", employeeName=").append(employeeName);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}