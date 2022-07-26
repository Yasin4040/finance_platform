package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.bxExcel.BxDetailDto;
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
 * 报销明细表
 *
 * @author User
 */
@TableName(value = "budget_reimbursementorder_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetReimbursementorderDetail implements Serializable {

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
     * 报销金额
     */
    @NotNull(message = "报销金额不能为空")
    @ApiModelProperty(value = "报销金额", hidden = false, required = false, example = "保存时必传，提交时不填")
    @TableField(value = "reimmoney")
    private BigDecimal reimmoney;

    /**
     * 月度动因id
     */
    @NotNull(message = "月度动因id不能为空")
    @ApiModelProperty(value = "月度动因主键", hidden = false, required = false)
    @TableField(value = "monthagentid")
    private Long monthagentid;

    /**
     * 动因名称
     */
    @NotBlank(message = "动因名称不能为空")
    @ApiModelProperty(value = "动因名称", hidden = false, required = false)
    @TableField(value = "monthagentname")
    private String monthagentname;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注", hidden = false, required = false)
    @TableField(value = "remark")
    private String remark;

    /**
     * 科目名称
     */
    @NotBlank(message = "科目名称不能为空")
    @ApiModelProperty(value = "科目名称", hidden = false, required = false)
    @TableField(value = "subjectname")
    private String subjectname;

    @ApiModelProperty(value = "科目主键", hidden = false, required = false)
    @NotNull(message = "科目主键不能为空")
    @TableField(value = "subjectid")
    private Long subjectid;


    /**
     * 开票单位id
     */
    @NotNull(message = "开票单位不能为空")
    @ApiModelProperty(value = "开票单位主键", hidden = false, required = false)
    @TableField(value = "bunitid")
    private Long bunitid;

    /**
     * 开票单位名称
     */
    @NotBlank(message = "开票单位名称不能为空")
    @ApiModelProperty(value = "开票单位名称", hidden = false, required = false)
    @TableField(value = "bunitname")
    private String bunitname;

    /**
     * 月份名称
     */
    @ApiModelProperty(value = "月份名称", hidden = false, required = false)
    @TableField(value = "monthname")
    private String monthname;

    /**
     * 计入执行标识
     */
    @NotNull(message = "计入执行标识不能为空")
    @ApiModelProperty(value = "计入执行标识", hidden = false, required = false)
    @TableField(value = "reimflag")
    private Boolean reimflag;

    /**
     * 月度科目预算
     */
    @ApiModelProperty(value = "月度科目预算", hidden = false, required = false)
    @TableField(value = "monthagentmoney")
    private BigDecimal monthagentmoney;

    /**
     * 月度科目未执行金额
     */
    @ApiModelProperty(value = "月度科目未执行金额", hidden = false, required = false)
    @TableField(value = "monthagentunmoney")
    private BigDecimal monthagentunmoney;

    /**
     * 年度动因预算
     */
    @ApiModelProperty(value = "年度动因预算", hidden = false, required = false)
    @TableField(value = "yearagentmoney")
    private BigDecimal yearagentmoney;

    /**
     * 年度动因未执行金额
     */
    @ApiModelProperty(value = "年度动因未执行金额", hidden = false, required = false)
    @TableField(value = "yearagentunmoney")
    private BigDecimal yearagentunmoney;
    
    @TableField(exist = false)
    private Long yearagentid; //报销校验年度动因预算可用时使用
    
    @TableField(exist = false)
    private String monthSubjectKey;//报销校验月度科目预算可用时使用
    
    @TableField(exist = false)
    private String yearSubjectKey;//报销校验年度科目预算可用时使用
    
    @TableField(exist = false)
    private String subjectCode; //报销校验年度动因预算可用时使用

    @TableField(exist = false)
    private Integer row; //行号
    /**
     * 非空校验
     *
     * @return
     * @throws Exception
     */
    public static final String validate(List<BudgetReimbursementorderDetail> list) throws Exception {
        if (list.isEmpty() || list.size() == 0) {
            return "明细为空！";
        }
        String result = BaseController.validateList(list);
        if (StringUtils.isEmpty(result)) {
            //校验重复
            boolean flag = BeanFieldTool.simpleDuplicateField(list, "monthagentid");
            if (flag) {
                //return "存在重复动因！";
            }
        }
        return result;
    }

    public static void setBase(BudgetReimbursementorderDetail detail, Long orderId) {
        detail.setId(null);
        detail.setReimbursementid(orderId);
    }

    public static void setBase(List<BudgetReimbursementorderDetail> list, BudgetReimbursementorder order) {
        list.forEach(ele -> {
            setBase(ele, order.getId());
        });
    }
    
    public BudgetReimbursementorderDetail(BxDetailDto excelDto) {
        MonthAgentMoneyInfo agentMoneyInfo = excelDto.getAgentMoneyInfo();
        this.reimmoney = new BigDecimal(String.valueOf(excelDto.getBxAmount()));
        this.monthagentid = agentMoneyInfo.getMonthAgentId();
        this.monthagentname = excelDto.getAgentName();
        this.remark = excelDto.getRemark();
        this.subjectname = excelDto.getSubjectName();
        this.subjectid = Long.valueOf(agentMoneyInfo.getSubjectId());
        this.bunitid = excelDto.getUnitId();
        this.bunitname = excelDto.getUnitName();
        this.reimflag = "是".equals(excelDto.getInclude());
        this.monthagentmoney = agentMoneyInfo.getSubjectMonthStartMoney();
        this.monthagentunmoney = agentMoneyInfo.getSubjectMonthMoney();
        this.yearagentmoney = agentMoneyInfo.getAgentYearStartMoney();
        this.yearagentunmoney = agentMoneyInfo.getAgentYearMoney();
    }

    public static final List<BudgetReimbursementorderDetail> getTestBean() {
        BudgetReimbursementorderDetail detail = new BudgetReimbursementorderDetail();
        detail.setReimmoney(new BigDecimal("100.0000"));
        detail.setMonthagentid(5457L);
        detail.setMonthagentname("办公费");
        detail.setRemark("测试测试");
        detail.setSubjectname("办公费0413");
        detail.setSubjectid(487L);
        detail.setBunitid(65L);
        detail.setBunitname("阿里巴巴");
        detail.setReimflag(true);
        detail.setMonthagentmoney(new BigDecimal("250.0000"));
        detail.setMonthagentunmoney(new BigDecimal("238.0000"));
        detail.setYearagentmoney(new BigDecimal("212.0000"));
        detail.setYearagentunmoney(new BigDecimal("200"));

        BudgetReimbursementorderDetail _detail = new BudgetReimbursementorderDetail();
        _detail.setReimmoney(new BigDecimal("30.0000"));
        _detail.setMonthagentid(5453L);
        _detail.setMonthagentname("当代中学生报高一");
        _detail.setRemark("测试详情");
        _detail.setSubjectname("报纸产品印制费");
        _detail.setSubjectid(490L);
        _detail.setBunitid(64L);
        _detail.setBunitname("金太阳");
        _detail.setReimflag(false);
        _detail.setMonthagentmoney(new BigDecimal("100.0000"));
        _detail.setMonthagentunmoney(new BigDecimal("50.0000"));
        _detail.setYearagentmoney(new BigDecimal("100.0000"));
        _detail.setYearagentunmoney(new BigDecimal("50.0000"));

        List<BudgetReimbursementorderDetail> list = new ArrayList<BudgetReimbursementorderDetail>(2);
        list.add(detail);
        list.add(_detail);
        return list;
    }

}
