package com.jtyjy.finance.manager.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Admin
 */
@TableName(value = "budget_reimbursementorder_scanlog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BudgetReimbursementorderScanlog implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 报销单号
     */
    @ApiModelProperty(value = "报销单编号", hidden = false, required = false)
    @TableField(value = "reimcode")
    private String reimcode;

    /**
     * 扫描时间
     */
    @ApiModelProperty(value = "扫描时间", hidden = false, required = false)
    @TableField(value = "scantime")
    private Date scantime;

    /**
     * 扫描人姓名
     */
    @ApiModelProperty(value = "扫描人姓名", hidden = false, required = false)
    @TableField(value = "scanername")
    private String scanername;

    /**
     * 扫描人
     */
    @ApiModelProperty(value = "扫描人工号", hidden = false, required = false)
    @TableField(value = "scaner")
    private String scaner;

    /**
     * 是否成功扫描
     */
    @ApiModelProperty(value = "扫描结果", hidden = false, required = false)
    @TableField(value = "scanflag")
    private Boolean scanflag;

    /**
     * 当前扫描状态0：等待扫描；1:票面审核,2:预算审核,3:扫描分单,4:会计做账，5:出纳付款
     */
    @ApiModelProperty(value = "当前扫描状态0：等待扫描；1:票面审核,2:预算审核,3:扫描分单,4:会计做账，5:出纳付款", hidden = false, required = false)
    @TableField(value = "scantype")
    private Integer scantype;

    /**
     * 是否操作 0：不操作（只是扫描）1：操作（弹出页面）
     */
    @ApiModelProperty(value = "是否操作 0：不操作（只是扫描）1：操作（弹出页面）", hidden = false, required = false)
    @TableField(value = "operateflag")
    private Boolean operateflag;

    @ApiModelProperty(value = "扫描信息", hidden = false, required = false)
    @TableField(value = "scaninfo")
    private String scaninfo;

    @ApiModelProperty(value = "扫描结果", hidden = false, required = false)
    @TableField(value = "scanresult")
    private String scanresult;

    /**
     * -4:限制错误 -3：全部退回；-2:退回纸质；-1:失败（版本不一致） ；0：待接收；1：票面审核（接收）；2：预算审核（接收）；3:扫描分单（接收）；4：会计做账（接收）；5：出纳付款
     */
    @ApiModelProperty(value = "接收状态：-4:限制错误 -3：全部退回；-2:退回纸质；-1:失败（版本不一致） ；0：待接收；1：票面审核（接收）；2：预算审核（接收）；3:扫描分单（接收）；4：会计做账（接收）；5：出纳付款", hidden = false, required = false)
    @TableField(value = "receivestatus")
    private Integer receivestatus;

}
