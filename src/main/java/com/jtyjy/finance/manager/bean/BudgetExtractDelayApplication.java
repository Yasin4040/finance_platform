package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 提成延期支付申请单
 * @TableName budget_extract_delay_application
 */
@TableName(value ="budget_extract_delay_application")
@Data
public class BudgetExtractDelayApplication implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 延期支付申请单号
     */
    private String delayCode;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 关联提成单号
     */
    private String relationExtractCode;

    //延期发放批次
    private Integer batch;

    private String extractMonth;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}