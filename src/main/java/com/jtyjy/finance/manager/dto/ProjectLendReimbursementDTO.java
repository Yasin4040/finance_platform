package com.jtyjy.finance.manager.dto;

import com.jtyjy.finance.manager.bean.BudgetProjectlendbxdetail;
import com.jtyjy.finance.manager.bean.BudgetProjectlendbxpayment;
import com.jtyjy.finance.manager.bean.BudgetProjectlendbxtrans;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @Author 袁前兼
 * @Date 2021/7/14 16:17
 */
@Data
public class ProjectLendReimbursementDTO {

    @NotNull(message = "项目借款Id不能为空")
    @ApiModelProperty(value = "项目借款Id")
    private Long projectLendSumId;

    @NotNull(message = "月份Id不能为空")
    @ApiModelProperty(value = "月份Id")
    private Long monthId;

    @NotBlank(message = "报销人Id不能为空")
    @ApiModelProperty(value = "报销人Id")
    private String bxUserId;

    @NotBlank(message = "报销人名称不能为空")
    @ApiModelProperty(value = "报销人名称")
    private String bxUserName;

    @NotNull(message = "报销时间不能为空")
    @ApiModelProperty(value = "报销时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date bxDate;

    @ApiModelProperty(value = "修改报销动因")
    private List<BudgetProjectlendbxdetail> updateList;

    @ApiModelProperty(value = "删除报销动因")
    private List<Long> deleteList;

    @ApiModelProperty(value = "项目借款报销冲账明细")
    private List<BudgetProjectlendbxpayment> czList;

    @ApiModelProperty(value = "项目借款报销转账明细")
    private List<BudgetProjectlendbxtrans> zzList;

    @NotNull(message = "是否提交不能为空")
    @ApiModelProperty(value = "是否提交")
    private Boolean isSubmit;

}
