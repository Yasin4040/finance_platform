package com.jtyjy.finance.manager.dto;

import com.jtyjy.finance.manager.bean.BudgetMonthAgentadd;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author User
 */
@Data
public class MonthAgentAddInfoDTO {

    @ApiModelProperty(value = "主键Id（新增时不传, 更新时必传）", required = true)
    private Long id;

    @NotNull(message = "届别Id不能为空")
    @ApiModelProperty(value = "届别Id", required = true)
    private Long yearId;

    @NotNull(message = "预算单位Id不能为空")
    @ApiModelProperty(value = "预算单位Id", required = true)
    private Long budgetUnitId;

//    @NotNull(message = "预算科目Id不能为空")
//    @ApiModelProperty(value = "预算科目Id", required = true)
//    private Long budgetSubjectId;

    @NotNull(message = "月份Id不能为空")
    @ApiModelProperty(value = "预算科目Id", required = true)
    private Long monthId;

    @ApiModelProperty(value = "附件地址")
    private String fileUrl;

    @ApiModelProperty(value = "文件原始名称")
    private String fileOriginName;

    @ApiModelProperty(value = "oa密码,上传附件时需使用")
    private String oaPwd;

    @ApiModelProperty(value = "新增的年度动因追加记录")
    private List<BudgetMonthAgentadd> addList;

    @ApiModelProperty(value = "修改的年度动因追加记录")
    private List<BudgetMonthAgentadd> updateList;

    @ApiModelProperty(value = "删除的年度动因追加Ids")
    private List<Long> deleteList;

    @NotNull(message = "是否提交至OA系统不能为空")
    @ApiModelProperty(value = "是否提交至OA系统")
    private Boolean isSubmit;

}
