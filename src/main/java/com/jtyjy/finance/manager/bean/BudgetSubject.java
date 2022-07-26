package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Admin
 */
@TableName(value = "budget_subject")
@Data
public class BudgetSubject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id
     */
    @NotNull(message = "主键Id不能为空")
    @ApiModelProperty(value = "主键Id")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 基础科目id
     */
    @ApiModelProperty(value = "基础科目id")
    @TableField(value = "subjectid")
    private Long subjectid;

    /**
     * 名称(别名）
     */
    @ApiModelProperty(value = "名称")
    @TableField(value = "name")
    private String name;

    /**
     * 届别id
     */
    @ApiModelProperty(value = "届别id")
    @TableField(value = "yearid")
    private Long yearid;

    /**
     * 上级id
     */
    @ApiModelProperty(value = "上级id")
    @TableField(value = "parentid")
    private Long parentid;

    /**
     * 编号
     */
    @ApiModelProperty(value = "编号")
    @TableField(value = "code")
    private String code;

    /**
     * 金蝶科目代码
     */
    @ApiModelProperty(value = "金蝶科目代码")
    @TableField(value = "jindiecode")
    private String jindiecode;

    /**
     * 金蝶科目代码
     */
    @ApiModelProperty(value = "金蝶科目名称")
    @TableField(value = "jindiename")
    private String jindiename;
    
    /**
     * 排序号
     */
    @ApiModelProperty(value = "排序号")
    @TableField(value = "orderno")
    private Integer orderno;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    @TableField(value = "remark")
    private String remark;

    /**
     * 停用标志
     */
    @ApiModelProperty(value = "停用标志")
    @TableField(value = "stopflag")
    private Boolean stopflag;

    /**
     * 所有上级id,用"-"隔开
     */
    @ApiModelProperty(value = "所有上级id,用\"-\"隔开")
    @TableField(value = "pids")
    private String pids;

    /**
     * 名称首拼
     */
    @ApiModelProperty(value = "名称首拼")
    @TableField(value = "firstspell")
    private String firstspell;

    /**
     * 名称全拼
     */
    @ApiModelProperty(value = "名称全拼")
    @TableField(value = "fullspell")
    private String fullspell;

    /**
     * 辅助性指标标识 true:是，false:否
     */
    @NotNull(message = "辅助性指标标识 true:是，false:否不能为空")
    @ApiModelProperty(value = "辅助性指标标识 true:是，false:否")
    @TableField(value = "assistflag")
    private Boolean assistflag;

    /**
     * 向上汇总 true:是，false:否
     */
    @NotNull(message = "向上汇总 true:是，false:否不能为空")
    @ApiModelProperty(value = "向上汇总 true:是，false:否")
    @TableField(value = "upsumflag")
    private Boolean upsumflag;

    /**
     * 费用分解 true:是，false:否
     */
    @NotNull(message = "费用分解 true:是，false:否不能为空")
    @ApiModelProperty(value = "费用分解 true:是，false:否")
    @TableField(value = "costsplitflag")
    private Boolean costsplitflag;

    /**
     * 费用追加 true:是，false:否
     */
    @ApiModelProperty(value = "费用追加 true:是，false:否")
    @TableField(value = "costaddflag")
    private Boolean costaddflag;

    /**
     * 费用拆借 true:是，false:否
     */
    @NotNull(message = "费用拆借 true:是，false:否不能为空")
    @ApiModelProperty(value = "费用拆借 true:是，false:否")
    @TableField(value = "costlendflag")
    private Boolean costlendflag;

    /**
     * 关联产品标识true:是，false:否
     */
    @NotNull(message = "关联产品标识true:是，false:否不能为空")
    @ApiModelProperty(value = "关联产品标识true:是，false:否")
    @TableField(value = "jointproductflag")
    private Boolean jointproductflag;

    /**
     * 年度预算计划类型  1:码洋计划、2:收入计划、3:成本动因、4:发样计划、5:配赠计划
     */
    @ApiModelProperty(value = "年度预算计划类型  1:码洋计划、2:收入计划、3:成本动因、4:发样计划、5:配赠计划")
    @TableField(value = "yearplantype")
    private Integer yearplantype;

    /**
     * 叶子节点标识默认为1
     */
    @NotNull(message = "叶子节点标识默认为1不能为空")
    @ApiModelProperty(value = "叶子节点标识默认为1")
    @TableField(value = "leafflag")
    private Boolean leafflag;

    /**
     * 公式科目（通过公式计算）
     */
    @NotNull(message = "公式科目（通过公式计算）不能为空")
    @ApiModelProperty(value = "公式科目（通过公式计算）")
    @TableField(value = "formulaflag")
    private Boolean formulaflag;

    /**
     * 计算公式
     */
    @ApiModelProperty(value = "计算公式")
    @TableField(value = "formula", updateStrategy = FieldStrategy.IGNORED)
    private String formula;

    /**
     * 计算顺序
     */
    @ApiModelProperty(value = "计算顺序")
    @TableField(value = "formulaorderno")
    private Integer formulaorderno;

    /**
     * 层级 默认为1
     */
    @ApiModelProperty(value = "层级 默认为1")
    @TableField(value = "level")
    private Integer level;

    /**
     * 多个产品一级分类id（用逗号隔开）
     */
    @ApiModelProperty(value = "多个产品一级分类id（用逗号隔开）")
    @TableField(value = "procategoryid", updateStrategy = FieldStrategy.IGNORED)
    private String procategoryid;

    // --------------------------------------------------

    /**
     * 本届码洋占比公式
     */
    @ApiModelProperty(value = "本届码洋占比公式")
    @TableField(exist = false)
    private String ccratioformula;

    /**
     * 上届码洋占比公式
     */
    @ApiModelProperty(value = "上届码洋占比公式")
    @TableField(exist = false)
    private String preccratioformula;

    /**
     * 本届收入占比公式
     */
    @ApiModelProperty(value = "本届收入占比公式")
    @TableField(exist = false)
    private String revenueformula;


}
