package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jtyjy.finance.manager.easyexcel.AuthorExcelData;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "budget_author")
@Data
public class BudgetAuthor implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 编号
     */
    @ApiParam(hidden = true)
    @TableField(value = "code")
    private String code;

    /**
     * 作者
     */
    @ApiParam(hidden = true)
    @TableField(value = "author")
    private String author;

    /**
     * 是否公司员工(1：是   0 ：否）
     */
    @ApiParam(hidden = true)
    @TableField(value = "authortype")
    private Boolean authortype;

    /**
     * 身份证号(个人作者)
     */
    @ApiParam(hidden = true)
    @TableField(value = "idnumber", updateStrategy = FieldStrategy.IGNORED)
    private String idnumber;

    /**
     * 纳税人识别号(单位作者）
     */
    @ApiParam(hidden = true)
    @TableField(value = "taxpayernumber", updateStrategy = FieldStrategy.IGNORED)
    private String taxpayernumber;

    /**
     * 所在单位
     */
    @ApiParam(hidden = true)
    @TableField(value = "company")
    private String company;

    /**
     * 电子联行号
     */
    @ApiParam(hidden = true)
    @TableField(value = "branchcode")
    private String branchcode;

    /**
     * 银行账号
     */
    @ApiParam(hidden = true)
    @TableField(value = "bankaccount")
    private String bankaccount;

    /**
     * 备注
     */
    @ApiParam(hidden = true)
    @TableField(value = "remark")
    private String remark;

    public BudgetAuthor() {
        
    }
    
    public BudgetAuthor(AuthorExcelData excelData) {
        this.author = excelData.getAuthor();
        this.idnumber = excelData.getIdnumber();
        this.taxpayernumber = excelData.getTaxpayernumber();
        this.authortype = "是".equals(excelData.getAuthortype());
        this.company = excelData.getCompany();
        this.bankaccount = excelData.getBankaccount();
        this.branchcode = excelData.getBranchcode();
        this.remark = excelData.getRemark();
    }
}
