package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.*;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 欠票表
 *
 * @author User
 */
@TableName(value = "budget_reimbursementorder_lack_bill")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetReimbursementorderLackBill implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 报销单id
     */

    @NotNull(message = "报销单主键不能为空")
    @ApiModelProperty(value = "报销单主键", required = true)
    @TableField(value = "reimbursementid")
    private Long reimbursementid;

    /**
     * 开票单位id
     */
    @NotNull(message = "开票单位id不能为空")
    @ApiModelProperty(value = "开票单位id", required = true)
    @TableField(value = "bunitid")
    private Long bunitid;

    /**
     * 开票单位名称
     */
    @NotBlank(message = "开票单位名称不能为空")
    @ApiModelProperty(value = "开票单位名称", required = true)
    @TableField(value = "bunitname")
    private String bunitname;

    /**
     * 开票项目
     */
    @NotBlank(message = "开票项目不能为空")
    @ApiModelProperty(value = "开票项目", required = true)
    @TableField(value = "project")
    private String project;

    /**
     * 金额
     */
    @NotNull(message = "金额不能为空")
    @ApiModelProperty(value = "金额", required = true)
    @TableField(value = "money")
    private BigDecimal money;

    /**
     * 预计还票时间
     */
    @NotNull(message = "预计还票时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "预计还票时间", required = true)
    @TableField(value = "estimated_return_time")
    private Date estimatedReturnTime;
    /**
     * 届别id
     */
    @NotNull(message = "届别id不能为空")
    @ApiModelProperty(value = "届别主键", required = true)
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 月份id
     */
    @NotNull(message = "月份id不能为空")
    @ApiModelProperty(value = "月份主键", required = true)
    @TableField(value = "monthid")
    private Long monthid;

    @ApiModelProperty(value = "状态", hidden = true)
    @TableField(value = "bill_status")
    private Integer billStatus;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 修改人
     */
    @ApiModelProperty(value = "修改人")
    private String updateBy;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    /**
     * 非空校验
     *
     * @param list
     * @return
     */
    public static final String validate(List<BudgetReimbursementorderLackBill> list, List<BudgetReimbursementorderDetail> orderDetail) {
        if (list == null || list.size() == 0) {
            return null;
        }
        //校验重复
        String result = BaseController.validateList(list);
        if (StringUtils.isEmpty(result)) {
            Set<Long> bunitSet = orderDetail.stream().map(BudgetReimbursementorderDetail::getBunitid).collect(Collectors.toSet());
            for (BudgetReimbursementorderLackBill lackBill : list) {
                if (!bunitSet.contains(lackBill.getBunitid())) {
                    result += lackBill.getBunitname() + "开票公司非报销明细中的开票单位！";
                }
            }
        }
        return result;
    }

    public static void setBase(List<BudgetReimbursementorderLackBill> list, BudgetReimbursementorder order) {
        list.forEach(ele -> {
            setBase(ele, order);
        });
    }

    public static void setBase(BudgetReimbursementorderLackBill bean, BudgetReimbursementorder order) {
        bean.setId(null);
        bean.setReimbursementid(order.getId());
        bean.setBillStatus(0);
        bean.setMonthid(order.getMonthid());
        bean.setCreateTime(new Date());
        bean.setCreateBy(UserThreadLocal.get().getDisplayName());
    }
}
