package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.bxExcel.HbDetailDto;
import com.jtyjy.finance.manager.mapper.response.MonthAgentMoneyInfo;
import com.jtyjy.finance.manager.utils.BeanFieldTool;
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

/**
 * 划拨表
 *
 * @author User
 */
@TableName(value = "budget_reimbursementorder_allocated")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetReimbursementorderAllocated implements Serializable {

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
     * 月度动因id
     */
    @NotNull(message = "月度动因id不能为空")
    @ApiModelProperty(value = "月度动因主键", hidden = false, required = true)
    @TableField(value = "monthagentid")
    private Long monthagentid;

    /**
     * 动因名称
     */
    @NotBlank(message = "动因名称不能为空")
    @ApiModelProperty(value = "动因名称", hidden = false, required = true)
    @TableField(value = "monthagentname")
    private String monthagentname;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空")
    @ApiModelProperty(value = "备注", hidden = false, required = true)
    @TableField(value = "remark")
    private String remark;

    /**
     * 科目名称
     */
    @NotBlank(message = "科目名称不能为空")
    @ApiModelProperty(value = "科目名称", hidden = false, required = true)
    @TableField(value = "subjectname")
    private String subjectname;


    @ApiModelProperty(value = "科目主键", hidden = false, required = true)
    @NotNull(message = "科目主键不能为空")
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 预算单位名称
     */
    @NotBlank(message = "预算单位名称不能为空")
    @ApiModelProperty(value = "预算单位名称", hidden = false, required = true)
    @TableField(value = "unitname")
    private String unitname;

    /**
     * 预算单位主键
     */
    @NotNull(message = "预算单位主键不能为空")
    @ApiModelProperty(value = "预算单位主键", hidden = false, required = true)
    @TableField(value = "unitid")
    private Long unitid;

    /**
     * 月份名称
     */
    @ApiModelProperty(value = "月份名称", hidden = false, required = true)
    @TableField(value = "monthname")
    private String monthname;

    /**
     * 划拨金额
     */
    @NotNull(message = "划拨金额不能为空")
    @ApiModelProperty(value = "划拨金额", hidden = false, required = true)
    @TableField(value = "allocatedmoney")
    private BigDecimal allocatedmoney;

    /**
     * 计入执行标识
     */
    @ApiModelProperty(value = "计入执行标识", hidden = true, required = false)
    @TableField(value = "reimflag")
    private Boolean reimflag;

    /**
     * 月度科目预算
     */
    @ApiModelProperty(value = "月度科目预算", hidden = false, required = true)
    @TableField(value = "monthagentmoney")
    private BigDecimal monthagentmoney;

    /**
     * 月度科目未执行金额
     */
    @ApiModelProperty(value = " 月度科目未执行金额", hidden = false, required = true)
    @TableField(value = "monthagentunmoney")
    private BigDecimal monthagentunmoney;

    /**
     * 年度动因预算
     */
    @ApiModelProperty(value = "年度动因预算", hidden = false, required = true)
    @TableField(value = "yearagentmoney")
    private BigDecimal yearagentmoney;

    /**
     * 年度动因未执行金额
     */
    @ApiModelProperty(value = "年度动因未执行金额", hidden = false, required = true)
    @TableField(value = "yearagentunmoney")
    private BigDecimal yearagentunmoney;

    
    @TableField(exist = false)
    private Long yearagentid;
    
    @TableField(exist = false)
    private String monthSubjectKey;//报销校验月度科目预算可用时使用
    
    @TableField(exist = false)
    private String yearSubjectKey;//报销校验年度科目预算可用时使用

    @TableField(exist = false)
    private Integer row; //行号
    
    /**
     * 校验
     *
     * @param list
     * @param noJoinCalcMoney 不计入金额总和
     * @param allocatedmoney  报销单划拨金额
     * @return
     * @throws Exception
     */
    public static final String validate(List<BudgetReimbursementorderAllocated> list, BigDecimal noJoinCalcMoney, BigDecimal allocatedmoney) throws Exception {
        if (list == null || list.size() == 0) {
            return "划拨记录为空！";
        }
        //校验非空
        String result = BaseController.validateList(list);
        if (StringUtils.isEmpty(result)) {
            //校验动因重复
            boolean flag = BeanFieldTool.simpleDuplicateField(list, "monthagentid");
            if (flag) {
                //return "划拨单动因重复！";
            }
            //校验冲账金额是否等于总和
            BigDecimal totalMoney = list.stream().map(BudgetReimbursementorderAllocated::getAllocatedmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalMoney.compareTo(noJoinCalcMoney) != 0) {
                return "划拨金额总和不等于报销单明细不计入执行金额总和！";
            }
            //划拨总和等于报销单划拨金额
            if (totalMoney.compareTo(allocatedmoney) != 0) {
                return "划拨金额总和不等于报销单划拨金额！";
            }
        }
        return result;
    }

    public static void setBase(List<BudgetReimbursementorderAllocated> list, BudgetReimbursementorder order) {
        list.forEach(ele -> {
            setBase(ele, order);
        });
    }

    public static void setBase(BudgetReimbursementorderAllocated bean, BudgetReimbursementorder order) {

        bean.setMonthname("4444");
        bean.setReimbursementid(order.getId());
        bean.setReimflag(Boolean.TRUE);
    }

    public BudgetReimbursementorderAllocated(HbDetailDto excelDto) {
        MonthAgentMoneyInfo agentMoneyInfo = excelDto.getAgentMoneyInfo();
        this.monthagentid = agentMoneyInfo.getMonthAgentId();
        this.monthagentname = excelDto.getAgentName();
        this.remark = excelDto.getRemark();
        this.subjectid = Long.valueOf(agentMoneyInfo.getSubjectId());
        this.subjectname = excelDto.getSubName();
        this.unitid = agentMoneyInfo.getUnitId();
        this.unitname = excelDto.getUnitName();
        this.allocatedmoney = new BigDecimal(String.valueOf(excelDto.getHbMoney()));
        this.monthagentmoney = agentMoneyInfo.getSubjectMonthStartMoney();
        this.monthagentunmoney = agentMoneyInfo.getSubjectMonthMoney();
        this.yearagentmoney = agentMoneyInfo.getAgentYearStartMoney();
        this.monthagentunmoney = agentMoneyInfo.getAgentYearMoney();
        
    }
    
    public static final List<BudgetReimbursementorderAllocated> getTestBean() {
        List<BudgetReimbursementorderAllocated> list = new ArrayList<BudgetReimbursementorderAllocated>();
        BudgetReimbursementorderAllocated bean = new BudgetReimbursementorderAllocated();
        bean.setMonthagentid(5470L);
        bean.setMonthagentname("笔");
        bean.setRemark("测试");
        bean.setSubjectname("文具");
        bean.setSubjectid(502L);
        bean.setUnitname("0416测试单位");
        bean.setUnitid(206L);
        bean.setMonthname("4444");
        bean.setAllocatedmoney(new BigDecimal("3.0000"));
        bean.setMonthagentmoney(new BigDecimal("20.0000"));
        bean.setMonthagentunmoney(new BigDecimal("20.0000"));
        bean.setYearagentmoney(new BigDecimal("800.0000"));
        bean.setYearagentunmoney(new BigDecimal("800.0000"));

        BudgetReimbursementorderAllocated _bean = new BudgetReimbursementorderAllocated();
        _bean.setMonthagentid(5471L);
        _bean.setMonthagentname("夜宵");
        _bean.setRemark("测试");
        _bean.setSubjectname("夜宵");
        _bean.setSubjectid(503L);
        _bean.setUnitname("0416测试单位");
        _bean.setUnitid(206L);
        _bean.setMonthname("4444");
        _bean.setAllocatedmoney(new BigDecimal("27.0000"));
        _bean.setMonthagentmoney(new BigDecimal("100.0000"));
        _bean.setMonthagentunmoney(new BigDecimal("100.0000"));
        _bean.setYearagentmoney(new BigDecimal("1200.0000"));
        _bean.setYearagentunmoney(new BigDecimal("1200.0000"));

        list.add(bean);
        list.add(_bean);
        return list;

    }

}
