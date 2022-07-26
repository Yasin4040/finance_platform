package com.jtyjy.finance.manager.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.bxExcel.EntertainDetailDto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 招待表
 *
 * @author User
 */
@TableName(value = "budget_reimbursementorder_entertain")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetReimbursementorderEntertain implements Serializable {

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
     * 餐费人数
     */
    @ApiModelProperty(value = "餐费人数", hidden = false, required = true)
    @TableField(value = "mealsrs")
    private Integer mealsrs = 0;

    /**
     * 餐费标准
     */
    @ApiModelProperty(value = "餐费标准", hidden = false, required = true)
    @TableField(value = "mealsbz")
    private BigDecimal mealsbz = BigDecimal.ZERO;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "餐费总计", hidden = false, required = true)
    @TableField(value = "mealstotal")
    private BigDecimal mealstotal = BigDecimal.ZERO;

    /**
     * 住宿人数
     */
    @ApiModelProperty(value = "住宿人数", hidden = false, required = true)
    @TableField(value = "hotalrs")
    private Integer hotalrs;

    /**
     * 标准
     */
    @ApiModelProperty(value = "住宿标准", hidden = false, required = true)
    @TableField(value = "hotalbz")
    private BigDecimal hotalbz;

    /**
     * 间数
     */
    @ApiModelProperty(value = "间数", hidden = false, required = true)
    @TableField(value = "hotaljs")
    private Integer hotaljs;

    /**
     * 住宿天数
     */
    @ApiModelProperty(value = "住宿天数", hidden = false, required = true)
    @TableField(value = "hotalts")
    private Integer hotalts;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "住宿总计", hidden = false, required = true)
    @TableField(value = "hotaltotal")
    private BigDecimal hotaltotal= BigDecimal.ZERO;;

    /**
     * 小计
     */
    @ApiModelProperty(value = "小计", hidden = false, required = true)
    @TableField(value = "total")
    private BigDecimal total;

    /**
     * 日期
     */
    @ApiModelProperty(value = "日期", hidden = false, required = true)
    @TableField(value = "date")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date date;

    /**
     * 宣传品费
     */
    @ApiModelProperty(value = "宣传品费", hidden = false, required = true)
    @TableField(value = "publicityexp")
    private BigDecimal publicityexp= BigDecimal.ZERO;;

    /**
     * 其它
     */
    @ApiModelProperty(value = "其它", hidden = false, required = true)
    @TableField(value = "other")
    private BigDecimal other= BigDecimal.ZERO;;

    /**
     * 非空校验
     *
     * @param list
     * @param reimMoney 
     * @return
     */
    public static final String validate(List<BudgetReimbursementorderEntertain> list, BigDecimal reimMoney) {
        if (list == null || list.size() == 0) {
            return null;
        }
        //校验重复
        String result = BaseController.validateList(list);
        BigDecimal total = BigDecimal.ZERO;
        if (StringUtils.isEmpty(result)) {
            //校验餐费：餐费标准=餐费金额 / 人数 TODO
            //校验住宿费：住宿费金额=人数 * 标准 * 间数 TODO
            //校验小计：餐费金额 + 住宿费 + 宣传品费 + 其他
            BudgetReimbursementorderEntertain bean = null;
            BigDecimal theTotal = BigDecimal.ZERO;
            for (int i = 0; i < list.size(); i++) {
                bean = list.get(i);
                if (null == bean.getMealsrs()) {
                    bean.setMealsrs(0);
                }
                if (null == bean.getMealsbz()) {
                    bean.setMealsbz(BigDecimal.ZERO);
                }
                if (null == bean.getMealstotal()) {
                    bean.setMealstotal(BigDecimal.ZERO);
                }
                if (null == bean.getHotaljs()) {
                    bean.setHotaljs(0);
                }
                if (null == bean.getHotalrs()) {
                    bean.setHotalrs(0);
                }
                if (null == bean.getHotalbz()) {
                    bean.setHotalbz(BigDecimal.ZERO);
                }
                if (null == bean.getHotaltotal()) {
                    bean.setHotaltotal(BigDecimal.ZERO);
                }
                if (null == bean.getTotal()) {
                    bean.setTotal(BigDecimal.ZERO);
                }
                if (null == bean.getPublicityexp()) {
                    bean.setPublicityexp(BigDecimal.ZERO);
                }
                if (null == bean.getOther()) {
                    bean.setOther(BigDecimal.ZERO);
                }
                theTotal = bean.getMealstotal().add(bean.getHotaltotal().add(bean.getOther()).add(bean.getPublicityexp()));
                total = total.add(theTotal);
                if (theTotal.compareTo(bean.getTotal()) != 0) {
                    return "第" + i + 1 + "条招待费【小计】不等于【餐费】+【住宿费】+【宣传品费】+【其他费用】之和！";
                }

            }
            if(total.compareTo(reimMoney) !=0) return "招待费总额需等于报销金额！";
        }
        return result;
    }

    public static void setBase(List<BudgetReimbursementorderEntertain> list, BudgetReimbursementorder order) {
        list.forEach(ele -> {
            setBase(ele, order);
        });
    }

    public static void setBase(BudgetReimbursementorderEntertain bean, BudgetReimbursementorder order) {
        bean.setReimbursementid(order.getId());
    }

    public BudgetReimbursementorderEntertain(EntertainDetailDto excelDto) {
        this.mealsrs = excelDto.getCfrs();
        this.mealsbz = new BigDecimal(excelDto.getCfbz());
        this.mealstotal = new BigDecimal(excelDto.getCfje());
        this.hotalrs = excelDto.getZsrs();
        this.hotalbz = new BigDecimal(excelDto.getZsbz());
        this.hotaljs = excelDto.getZsjs();
        this.hotalts = 1;
        this.hotaltotal = new BigDecimal(excelDto.getZsje());
        this.other = new BigDecimal(excelDto.getOther());
        this.publicityexp = new BigDecimal(excelDto.getXcfje()); 
        this.total = new BigDecimal(excelDto.getCount());
        try {
            this.date = Constants.FORMAT_10.parse(excelDto.getDate());
        } catch (ParseException e) {
            this.date = new Date();
        }
    }
    
    public static final List<BudgetReimbursementorderEntertain> getTestBean() {
        List<BudgetReimbursementorderEntertain> list = new ArrayList<BudgetReimbursementorderEntertain>();
        BudgetReimbursementorderEntertain bean = new BudgetReimbursementorderEntertain();
        bean.setMealsrs(10);
        bean.setMealsbz(new BigDecimal("20"));
        bean.setMealstotal(new BigDecimal("200"));
        bean.setHotalrs(10);
        bean.setHotalbz(new BigDecimal("200"));
        bean.setHotaljs(8);
        bean.setHotalts(5);
        bean.setHotaltotal(new BigDecimal("1800"));
        bean.setTotal(new BigDecimal("2800"));
        bean.setDate(new Date());
        bean.setPublicityexp(new BigDecimal("300"));
        bean.setOther(new BigDecimal("70"));

        BudgetReimbursementorderEntertain _bean = new BudgetReimbursementorderEntertain();
        _bean.setMealsrs(10);
        _bean.setMealsbz(new BigDecimal("20"));
        _bean.setMealstotal(new BigDecimal("200"));
        _bean.setHotalrs(10);
        _bean.setHotalbz(new BigDecimal("200"));
        _bean.setHotaljs(8);
        _bean.setHotalts(5);
        _bean.setHotaltotal(new BigDecimal("1800"));
        _bean.setTotal(new BigDecimal("2800"));
        _bean.setDate(new Date());
        _bean.setPublicityexp(new BigDecimal("300"));
        _bean.setOther(new BigDecimal("70"));

        list.add(bean);
        list.add(_bean);
        return list;
    }

}
