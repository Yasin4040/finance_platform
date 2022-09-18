package com.jtyjy.finance.manager.dto.commission;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jtyjy.finance.manager.dto.individual.ImportErrorDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商务导入 回款明细表
 * @TableName budget_business_pay_collection
 */
@TableName(value ="budget_business_pay_collection")
@Data
public class BusinessPayCollectionErrorDTO extends ImportErrorDTO {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "id")
    @ExcelIgnore
    private Long id;

    /**
     * 提成类型
     */
    @TableField(value = "extract_type")
    @ApiModelProperty(value = "提成类型")
    @ExcelProperty(value = "提成类型")
    private String extractType;

    /**
     * 届别Id
     */
    @TableField(value = "year")
    @ApiModelProperty(value = "届别 中文传参")
    @ExcelProperty(value = "届别")
    private String year;

    /**
     * 月份Id
     */
    @TableField(value = "month")
    @ApiModelProperty(value = "月份 传数字")
    @ExcelProperty(value = "月份")
    private String month;

    /**
     * 批次号
     */
    @TableField(value = "batch_no")
    @ApiModelProperty(value = "批次号")
    @ExcelProperty(value = "批次")
    private String batchNo;

    /**
     * 回款工号
     */
    @TableField(value = "collection_emp_no")
    @ApiModelProperty(value = "员工工号")
    @ExcelProperty(value = "员工工号")
    private String empNo;
    /**
     * 部门完整名称   客户类型。
     */
    @TableField(value = "dept_full_name")
    @ApiModelProperty(value = "部门完整名称   客户类型。")
    @ExcelProperty(value = "客户类型")
    private String deptFullName;
    /**
     * 业务经理
     */
    @TableField(value = "emp_name")
    @ApiModelProperty(value = "业务经理")
    @ExcelProperty(value = "业务经理")
    private String empName;


    /**
     * 回款业务经理
     */
    @TableField(value = "collection_emp_name")
    @ApiModelProperty(value = "回款业务经理")
    @ExcelProperty(value = "回款业务经理")
    private String collectionEmpName;

    /**
     * 单位名称
     */
    @TableField(value = "unit_Name")
    @ApiModelProperty(value = "单位名称")
    @ExcelProperty(value = "单位名称")
    private String unitName;

    /**
     * 联系人
     */
    @TableField(value = "contact_person")
    @ApiModelProperty(value = "联系人")
    @ExcelProperty(value = "联系人")
    private String contactPerson;

    /**
     * 记账单号
     */
    @TableField(value = "bookkeeping_no")
    @ApiModelProperty(value = "记账单号")
    @ExcelProperty(value = "记账单号")
    private String bookkeepingNo;

    /**
     * 产品名称
     */
    @TableField(value = "product_name")
    @ApiModelProperty(value = "产品名称")
    @ExcelProperty(value = "产品名称")
    private String productName;

    /**
     * 码洋
     */
    @TableField(value = "total_price")
    @ApiModelProperty(value = "码洋")
    @ExcelProperty(value = "码洋")
    private BigDecimal totalPrice;

    /**
     * 实洋
     */
    @TableField(value = "actual_price")
    @ApiModelProperty(value = "实洋")
    @ExcelProperty(value = "实洋")
    private BigDecimal actualPrice;

    /**
     * 底价
     */
    @TableField(value = "floor_price")
    @ApiModelProperty(value = "底价")
    @ExcelProperty(value = "底价")
    private BigDecimal floorPrice;

    /**
     * 本期回款
     */
    @TableField(value = "current_collection")
    @ApiModelProperty(value = "本期回款")
    @ExcelProperty(value = "本期回款")
    private BigDecimal currentCollection;

    /**
     * 本期提成
     */
    @TableField(value = "current_commission")
    @ApiModelProperty(value = "本期提成")
    @ExcelProperty(value = "本期提成")
    private BigDecimal currentCommission;

    /**
     * 预留提成
     */
    @TableField(value = "reserved_commission")
    @ApiModelProperty(value = "预留提成")
    @ExcelProperty(value = "预留提成")
    private BigDecimal reservedCommission;

    /**
     * 前期回款
     */
    @TableField(value = "early_collection")
    @ApiModelProperty(value = "前期回款")
    @ExcelProperty(value = "前期回款")
    private BigDecimal earlyCollection;

    /**
     * 前期提成
     */
    @TableField(value = "early_commission")
    @ApiModelProperty(value = "前期提成")
    @ExcelProperty(value = "前期提成")
    private BigDecimal earlyCommission;

    /**
     * 是否大区经理  0  1  2
     */
    @TableField(value = "if_big_manager")
    @ApiModelProperty(value = "是否大区经理  0  1  2")
    @ExcelIgnore
    private Integer ifBigManager;

    /**
     * 是否业务经理查看 0 1 2
     */
    @TableField(value = "if_manager")
    @ApiModelProperty(value = "是否业务经理查看 0  1  2")
    @ExcelIgnore
    private Integer ifManager;


    /**
     * 是否大区经理  0  1  2
     */
    @TableField(value = "if_big_manager")
    @ApiModelProperty(value = "是否大区经理  允许,关闭")
    @ExcelProperty("大区经理查看状态")
    private String ifBigManagerView;

    /**
     * 是否业务经理查看 0 1 2
     */
    @TableField(value = "if_manager")
    @ApiModelProperty(value = "是否业务经理查看  允许,关闭")
    @ExcelProperty("业务经理查看状态")
    private String ifManagerView;


    /**
     * 创建人
     */
    @TableField(value = "create_by")
    @ApiModelProperty(value = "创建人")
    @ExcelIgnore
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ExcelIgnore
    private Date createTime;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    @ApiModelProperty(value = "更新人")
    @ExcelIgnore
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ExcelIgnore
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}