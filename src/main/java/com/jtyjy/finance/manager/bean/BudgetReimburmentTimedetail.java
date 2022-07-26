package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_reimburment_timedetail")
@Data
public class BudgetReimburmentTimedetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiParam(hidden = true)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报销单号
     */
    @ApiParam(hidden = true)
    @TableField(value = "reimcode")
    private String reimcode;

    /**
     * 操作人工号
     */
    @ApiParam(hidden = true)
    @TableField(value = "empno")
    private String empno;

    /**
     * 开始时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "starttime")
    private Date starttime;

    /**
     * 结束时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "endtime")
    private Date endtime;

    /**
     * 天数
     */
    @ApiParam(hidden = true)
    @TableField(value = "days")
    private BigDecimal days;

    /**
     * 1:票面接收 - 单据提交  2：票面审核-票面接收  3:预算审核-票面审核  4扫描分单-预算审核 5 做账 - 扫描分单 6 出纳接收-做账
     */
    @ApiParam(hidden = true)
    @TableField(value = "type")
    private Integer type;

    /**
     * 是否有效   1：有效  0 无效
     */
    @ApiParam(hidden = true)
    @TableField(value = "iseffective")
    private Integer iseffective;

    /**
     * 创建时间
     */
    @ApiParam(hidden = true)
    @TableField(value = "creattime")
    private Date creattime;

    @TableField(exist = false)
    private Long orderId;
    
    @TableField(exist = false)
    private String reimMoney;
    
    @TableField(exist = false)
    private String unitName;
    
    @TableField(exist = false)
    private Date submitTime;
    
    @TableField(exist = false)
    private String fdUnitName;
}
