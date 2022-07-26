package com.jtyjy.finance.manager.hrbean;

import java.io.Serializable;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * @author Admin
 */
@TableName(value = "hr_salary_year_tax_user")
@Data
public class HrSalaryYearTaxUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type=IdType.AUTO)
	private Long id;

    /**
     * 名称
     */
    @TableField(value = "username")
	private String username;

    /**
     * 工号
     */
    @TableField(value = "empno")
	private String empno;

    /**
     * 公司id
     */
    @TableField(value = "compid")
	private String compid;
    
    /**
     * 身份证号
     */
    @TableField(value = "certno")
	private String certno;
   
}
