package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.bxExcel.StrickDetailDto;
import com.jtyjy.finance.manager.utils.BeanFieldTool;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 冲账表
 *
 * @author User
 */
@TableName(value = "budget_reimbursementorder_payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetReimbursementorderPayment implements Serializable {

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
    @ApiModelProperty(value = "报销单主键", hidden = false, required = false)
    @TableField(value = "reimbursementid")
    private Long reimbursementid;

    /**
     * 借款id
     */
    @NotNull(message = "借款id不能为空")
    @ApiModelProperty(value = "借款主键", hidden = false, required = true)
    @TableField(value = "lendmoneyid")
    private Long lendmoneyid;

    /**
     * 借款人编号
     */
    @ApiModelProperty(value = "借款人编号", hidden = false, required = false)
    @TableField(value = "lendmoneycode")
    private String lendmoneycode;

    /**
     * 借款人名字
     */
    @ApiModelProperty(value = "借款人名字", hidden = false, required = true)
    @TableField(value = "lendmoneyname")
    @NotEmpty(message = "借款人名字不能为空")
    private String lendmoneyname;

    /**
     * 借款编号
     */
    @ApiModelProperty(value = "借款编号", hidden = false, required = true)
    @TableField(value = "lendcode")
    @NotEmpty(message = "借款编号不能为空")
    private String lendcode;

    /**
     * 借款金额
     */
    @ApiModelProperty(value = "借款金额", hidden = false, required = true)
    @TableField(value = "lendmoney")
    @NotNull(message = "借款金额不能为空")
    private BigDecimal lendmoney;

    /**
     * 未还金额
     */
    @ApiModelProperty(value = "未还金额", hidden = false, required = true)
    @TableField(value = "unrepaidmoney")
    @NotNull(message = "未还金额不能为空")
    private BigDecimal unrepaidmoney;

    /**
     * 借款说明
     */
    @ApiModelProperty(value = "借款说明", hidden = false, required = false)
    @TableField(value = "lendmoneyremark")
    private String lendmoneyremark;

    /**
     * 冲账金额
     */
    @ApiModelProperty(value = "冲账金额", hidden = false, required = true)
    @TableField(value = "paymentmoney")
    @NotNull(message = "冲账金额不能为空")
    private BigDecimal paymentmoney;

    /**
     * 还款单id
     */
    @ApiModelProperty(value = "还款单主键", hidden = false, required = true)
    @TableField(value = "repaymoneyid")
    private Long repaymoneyid;

    @TableField(exist = false)
    private String lendType;

    /**
     * 校验
     *
     * @param list         冲账记录
     * @param paymentMoney 报销单冲账总额
     * @return
     * @throws Exception
     */
    public static final String validate(List<BudgetReimbursementorderPayment> list, BigDecimal paymentMoney) throws Exception {
        if (list == null || list.size() == 0) {
            return null;
        }
        String result = BaseController.validateList(list);
        if (StringUtils.isEmpty(result)) {
            //校验借款单号重复
            boolean flag = BeanFieldTool.simpleDuplicateField(list, "lendmoneyid");
            if (flag) {
                return "存在重复借款单！";
            }
            //冲账金额小于等于未还金额
            int index = 1;
            for (BudgetReimbursementorderPayment bean : list) {
                if (bean.getPaymentmoney().compareTo(bean.getUnrepaidmoney()) > 0) {
                    return "第" + index + "条冲账单，冲账金额大于未还金额！";
                }
                index++;
            }
            //校验冲账金额是否等于冲账总和
            BigDecimal totalMoney = list.stream().map(BudgetReimbursementorderPayment::getPaymentmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalMoney.compareTo(paymentMoney) != 0) {
                return "冲账金额总和不等于报销单冲账金额！";
            }
        }
        return result;
    }
    
    public BudgetReimbursementorderPayment(StrickDetailDto excelDto) {
        BudgetLendmoney lendMoneyInfo = excelDto.getLendMoneyInfo();
        this.lendmoneyid = lendMoneyInfo.getId();
        this.lendmoneyname = lendMoneyInfo.getEmpname() + "(" + lendMoneyInfo.getEmpno() + ")";
        this.lendcode = lendMoneyInfo.getLendmoneycode();
        this.unrepaidmoney = lendMoneyInfo.getLendmoney().subtract(lendMoneyInfo.getRepaidmoney()).add(lendMoneyInfo.getInterestmoney()).subtract(lendMoneyInfo.getRepaidinterestmoney());
        this.lendmoneyremark = lendMoneyInfo.getRemark();
        this.paymentmoney = new BigDecimal(String.valueOf(excelDto.getCzMoney()));
    }

    public static void setBase(List<BudgetReimbursementorderPayment> list, BudgetReimbursementorder order) {
        list.forEach(ele -> {
            setBase(ele, order.getId());
        });
    }

    public static void setBase(BudgetReimbursementorderPayment bean, Long orderId) {
        bean.setId(null);
        bean.setReimbursementid(orderId);
    }

    public static final List<BudgetReimbursementorderPayment> getBean() {
        List<BudgetReimbursementorderPayment> list = new ArrayList<BudgetReimbursementorderPayment>();
        BudgetReimbursementorderPayment bean = new BudgetReimbursementorderPayment();
        bean.setLendmoneyid(7582L);
        bean.setLendmoneyname("孔令程(19283)");
        bean.setLendcode("JK20201000001");
        bean.setLendmoney(new BigDecimal("1.0000"));
        bean.setUnrepaidmoney(new BigDecimal("1.0000"));
        bean.setLendmoneyremark("测试日常借款");
        bean.setPaymentmoney(new BigDecimal("1.0000"));

        BudgetReimbursementorderPayment _bean = new BudgetReimbursementorderPayment();
        _bean.setLendmoneyid(7583L);
        _bean.setLendmoneyname("孔令程(19283)");
        _bean.setLendcode("JK20201000002");
        _bean.setLendmoney(new BigDecimal("1.0000"));
        _bean.setUnrepaidmoney(new BigDecimal("0.9000"));
        _bean.setLendmoneyremark("测试");
        _bean.setPaymentmoney(new BigDecimal("0.9000"));

        list.add(bean);
        list.add(_bean);
        return list;
    }

}
