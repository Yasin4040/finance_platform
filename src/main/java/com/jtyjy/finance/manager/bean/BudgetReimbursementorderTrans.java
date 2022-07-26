package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.bxExcel.TransferDetailDto;
import com.jtyjy.finance.manager.utils.BeanFieldTool;
import com.jtyjy.finance.manager.vo.BankAccountVO;
import com.jtyjy.finance.manager.vo.BillingUnitAccountVO;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 转账
 *
 * @author User
 */
@TableName(value = "budget_reimbursementorder_trans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetReimbursementorderTrans implements Serializable {

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
    @ApiModelProperty(value = "收款人姓名（户名）不能为空", hidden = false, required = true)
    @TableField(value = "payeename")
    private String payeename;

    /**
     * 收款人账户
     */
    @NotBlank(message = "收款人账户不能为空")
    @ApiModelProperty(value = "收款人账户", hidden = false, required = true)
    @TableField(value = "payeebankaccount")
    private String payeebankaccount;

    /**
     * 收款人开户行
     */
    @NotBlank(message = "收款人开户行不能为空")
    @ApiModelProperty(value = "收款人开户行", hidden = false, required = true)
    @TableField(value = "payeebankname")
    private String payeebankname;

    /**
     * 转账金额
     */
    @NotNull(message = "转账金额不能为空")
    @ApiModelProperty(value = "转账金额", hidden = false, required = true)
    @TableField(value = "transmoney")
    private BigDecimal transmoney;

    /**
     * 税
     */
    @ApiModelProperty(value = "税", hidden = false, required = false)
    @TableField(value = "tax")
    private BigDecimal tax;

    /**
     * 修改之前的付款单位id
     */
    @ApiModelProperty(value = "修改之前的付款单位主键", hidden = false, required = false)
    @TableField(value = "olddraweeunitaccountid")
    private Long olddraweeunitaccountid;

    /**
     * 付款单位账户id
     */
    @NotNull(message = "付款单位账户id不能为空")
    @ApiModelProperty(value = "付款单位账户主键", hidden = false, required = true)
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
    @NotBlank(message = "付款单位账户不能为空")
    @ApiModelProperty(value = "付款单位账户", hidden = false, required = true)
    @TableField(value = "draweebankaccount")
    private String draweebankaccount;

    /**
     * 付款单位账户开户行
     */
    @NotBlank(message = "付款单位账户开户行不能为空")
    @ApiModelProperty(value = "付款单位账户开户行", hidden = false, required = true)
    @TableField(value = "draweebankname")
    private String draweebankname;

    /**
     * 付款单id
     */
    @ApiModelProperty(value = "付款单主键", hidden = false, required = false)
    @TableField(value = "paymoneyid")
    private Long paymoneyid;

    /**
     * 是否欠款（0：未欠款；1：欠款）
     */
    @ApiModelProperty(value = "是否欠款", hidden = false, required = false)
    @TableField(value = "arrearsflag")
    private Boolean arrearsflag;


    /**
     * 校验
     *
     * @param list
     * @param detailPayAccountIds 明细单开票单位付款账户主键
     * @param transMoney          报销单转账金额
     * @return
     * @throws Exception
     */
    public static final String validate(List<BudgetReimbursementorderTrans> list, Set<Long> detailPayAccountIds, BigDecimal transMoney,BigDecimal money) throws Exception {
        if (list == null || list.size() == 0) {
            return null;
        }
        //校验非空
        String result = BaseController.validateList(list);
        if (StringUtils.isEmpty(result)) {
            //校验重复 暂时无需此校验--modify by shubo 20210714
//            boolean flag = BeanFieldTool.simpleDuplicateField(list, "payeebankaccount");
//            if (flag) {
//                return "收款账号重复！";
//            }
            //校验付款账号是否存在详情中
            if (null == detailPayAccountIds || detailPayAccountIds.isEmpty()) {
                return "转账单没有可用的付款账户！";
            }
            int index = 1;
            for (BudgetReimbursementorderTrans bean : list) {
                if (!detailPayAccountIds.contains(bean.getDraweeunitaccountid())) {
                    return "第" + index + "条转账申请，付款账户不在明细开票单位名下！";
                }
                index++;
            }
            //校验冲账金额是否等于冲账总和
            //BigDecimal totoalMoney = list.stream().map(BudgetReimbursementorderTrans::getTransmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (money.compareTo(transMoney) != 0) {
                return "转账金额总和不等于报销单转账金额！";
            }
        }
        return result;
    }

    public BudgetReimbursementorderTrans(TransferDetailDto excelDto) {
        BankAccountVO bankAccountInfo = excelDto.getBankAccountInfo();
        BillingUnitAccountVO unitAccountInfo = excelDto.getUnitAccountInfo();
        this.payeecode = bankAccountInfo.getCode();
        this.payeename = bankAccountInfo.getPname();
        this.payeebankaccount = bankAccountInfo.getBankAccount();
        this.payeebankname = bankAccountInfo.getBranchName();
        this.transmoney = new BigDecimal(String.valueOf(excelDto.getMoney()));
        this.draweeunitaccountid = unitAccountInfo.getId();
        this.draweeunitname = unitAccountInfo.getBillingUnitName();
        this.draweebankaccount = unitAccountInfo.getBankAccount();
        this.draweebankname = unitAccountInfo.getBranchName();
    }
    
    public static void setBase(List<BudgetReimbursementorderTrans> list, BudgetReimbursementorder order) {
        list.forEach(ele -> {
            setBase(ele, order.getId());
        });
    }

    public static void setBase(BudgetReimbursementorderTrans bean, Long orderId) {
        bean.setId(null);
        bean.setReimbursementid(orderId);
        bean.setOlddraweeunitaccountid(bean.getDraweeunitaccountid());
        bean.setArrearsflag(Boolean.FALSE);
    }

    public static final List<BudgetReimbursementorderTrans> getTestBean() {
        List<BudgetReimbursementorderTrans> list = new ArrayList<BudgetReimbursementorderTrans>();
        BudgetReimbursementorderTrans bean = new BudgetReimbursementorderTrans();
        bean.setPayeecode("11841");
        bean.setPayeename("王文辉");
        bean.setPayeebankaccount("6236682020000737426");
        bean.setPayeebankname("中国建设银行股份有限公司南昌住房城市建设支行");
        bean.setTransmoney(new BigDecimal("1.0000"));
        bean.setDraweeunitaccountid(3L);
        bean.setDraweeunitname("金太阳");
        bean.setDraweebankaccount("234234234");
        bean.setDraweebankname("工商银行福建省连江县支行");

        BudgetReimbursementorderTrans _bean = new BudgetReimbursementorderTrans();
        _bean.setPayeecode("12966");
        _bean.setPayeename("夏芳");
        _bean.setPayeebankaccount("6236682020000281722");
        _bean.setPayeebankname("中国建设银行股份有限公司南昌住房城市建设支行");
        _bean.setTransmoney(new BigDecimal("1.0000"));
        _bean.setDraweeunitaccountid(42L);
        _bean.setDraweeunitname("阿里巴巴");
        _bean.setDraweebankaccount("65522552215522542");
        _bean.setDraweebankname("工商银行上海市殷高西路支行");

        list.add(_bean);
        list.add(bean);
        return list;
    }

}
