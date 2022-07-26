package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.utils.BeanFieldTool;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 现金表
 *
 * @author User
 */
@TableName(value = "budget_reimbursementorder_cash")
@Data
public class BudgetReimbursementorderCash implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键", hidden = false, required = false)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报销单id
     */
    @ApiModelProperty(value = "报销单", hidden = false, required = false)
    @TableField(value = "reimbursementid")
    private Long reimbursementid;

    /**
     * 收款人编号
     */
    @NotBlank(message = "收款人编号不能为空")
    @ApiModelProperty(value = "收款人编号", hidden = false, required = true)
    @TableField(value = "payeecode")
    private String payeecode;

    /**
     * 收款人姓名（户名）
     */
    @NotBlank(message = "收款人姓名（户名）不能为空")
    @ApiModelProperty(value = "收款人姓名（户名）", hidden = false, required = true)
    @TableField(value = "payeename")
    private String payeename;

    /**
     * 现金金额
     */
    @ApiModelProperty(value = "现金金额", hidden = false, required = true)
    @NotNull(message = "现金金额不能为空")
    @TableField(value = "cashmoney")
    private BigDecimal cashmoney;

    /**
     * 修改之前的付款单位id
     */
    @ApiModelProperty(value = "修改之前的付款单位主键", hidden = false, required = false)
    @TableField(value = "olddraweeunitaccountid")
    private Long olddraweeunitaccountid;

    /**
     * 付款单位id
     */
    @ApiModelProperty(value = "付款单位主键", hidden = false, required = false)
    @TableField(value = "draweeunitaccountid")
    private Long draweeunitaccountid;

    /**
     * 付款单位名字
     */
    @NotBlank(message = "付款单位名字不能为空")
    @ApiModelProperty(value = "付款单位名字", hidden = false, required = true)
    @TableField(value = "draweeunitname")
    private String draweeunitname;

    /**
     * 付款单位账户
     */
    @ApiModelProperty(value = "付款单位账户", hidden = false, required = false)
    @TableField(value = "draweebankaccount")
    private String draweebankaccount;

    /**
     * 付款单位账户开户行
     */
    @ApiModelProperty(value = "付款单位账户开户行", hidden = false, required = false)
    @TableField(value = "draweebankname")
    private String draweebankname;

    /**
     * 付款单id
     */
    @ApiModelProperty(value = "付款单主键", hidden = false, required = false)
    @TableField(value = "paymoneyid")
    private Long paymoneyid;

    /**
     * @param list
     * @param cashMoney 保险单现金金额
     * @return
     * @throws Exception
     */
    public static final String validate(List<BudgetReimbursementorderCash> list, BigDecimal cashMoney) throws Exception {
        if (list == null || list.size() == 0) {
            return null;
        }
        //校验非空
        String result = BaseController.validateList(list);
        if (StringUtils.isEmpty(result)) {
            //校验重复
            boolean flag = BeanFieldTool.simpleDuplicateField(list, "payeecode");
            if (flag) {
                return "收款人重复！";
            }
            //校验冲账金额是否等于总和
            BigDecimal totalMoney = list.stream().map(BudgetReimbursementorderCash::getCashmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalMoney.compareTo(cashMoney) != 0) {
                return "现金金额总和不等于报销单现金金额！";
            }
        }
        return result;
    }

    public static void setBase(List<BudgetReimbursementorderCash> list, BudgetReimbursementorder order) {
        list.forEach(ele -> {
            setBase(ele, order.getId());
        });
    }

    public static void setBase(BudgetReimbursementorderCash bean, Long orderId) {
        bean.setId(null);
        bean.setReimbursementid(orderId);
    }

    public static final List<BudgetReimbursementorderCash> getTestBean() {
        List<BudgetReimbursementorderCash> list = new ArrayList<BudgetReimbursementorderCash>();
        BudgetReimbursementorderCash bean = new BudgetReimbursementorderCash();
        bean.setPayeecode("10003");
        bean.setPayeename("刘春华");
        bean.setCashmoney(new BigDecimal("2.0000"));
        bean.setDraweeunitname("现金2");
        BudgetReimbursementorderCash _bean = new BudgetReimbursementorderCash();
        _bean.setPayeecode("10005");
        _bean.setPayeename("周冬莲");
        _bean.setCashmoney(new BigDecimal("1.0000"));
        _bean.setDraweeunitname("现金1");
        list.add(_bean);
        list.add(bean);
        return list;
    }

}
