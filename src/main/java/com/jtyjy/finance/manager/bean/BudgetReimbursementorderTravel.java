package com.jtyjy.finance.manager.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.bxExcel.TravelDetailDto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 差旅表
 *
 * @author User
 */
@TableName(value = "budget_reimbursementorder_travel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetReimbursementorderTravel implements Serializable {

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
     * 交通工具
     */
    @ApiModelProperty(value = "交通工具", hidden = false, required = true)
    @TableField(value = "travelvehicle")
    private Integer travelvehicle;
    
    @TableField(exist = false)
    private String travelvehicleName;

    /**
     * 开始日期
     */
    @ApiModelProperty(value = "开始日期", hidden = false, required = true)
    @TableField(value = "travelstart")
    private String travelstart;

    /**
     * 未知参数
     */
    @ApiModelProperty(value = "结束日期", hidden = false, required = true)
    @TableField(value = "travelend")
    private String travelend;

    /**
     * 出发地
     */
    @NotBlank(message = "出发地不能为空")
    @ApiModelProperty(value = "出发地", hidden = false, required = true)
    @TableField(value = "travelorigin")
    private String travelorigin;

    /**
     * 目的地
     */
    @NotBlank(message = "目的地不能为空")
    @ApiModelProperty(value = "目的地", hidden = false, required = true)
    @TableField(value = "traveldest")
    private String traveldest;

    /**
     * 市内交通费
     */
    @ApiModelProperty(value = "市内交通费", hidden = false, required = true)
    @TableField(value = "citytravelexp")
    private BigDecimal citytravelexp;

    /**
     * 长途交通费
     */
    @ApiModelProperty(value = "长途交通费", hidden = false, required = true)
    @TableField(value = "longtravelexp")
    private BigDecimal longtravelexp;

    /**
     * 住宿费
     */
    @ApiModelProperty(value = "住宿费", hidden = false, required = true)
    @TableField(value = "hotelexpense")
    private BigDecimal hotelexpense;

    /**
     * 补助天数
     */
    @ApiModelProperty(value = "补助天数", hidden = false, required = true)
    @TableField(value = "travelday")
    private BigDecimal travelday;

    /**
     * 每天补助
     */
    @ApiModelProperty(value = "每天补助", hidden = false, required = true)
    @TableField(value = "dailysubsidy")
    private BigDecimal dailysubsidy;


    @TableField(exist = false)
    private BigDecimal subsidyMoney;

    /**
     * 其它
     */
    @ApiModelProperty(value = "其它", hidden = false, required = true)
    @TableField(value = "other")
    private BigDecimal other;

    /**
     * 小计
     */
    @ApiModelProperty(value = "小计", hidden = false, required = true)
    @TableField(value = "total")
    private BigDecimal total;

    /**
     * 非空校验
     *
     * @param list
     * @param reimMoney 
     * @return
     */
    public static final String validate(List<BudgetReimbursementorderTravel> list, BigDecimal reimMoney,int length) {
        if (list == null || list.size() == 0) {
            return null;
        }
        //校验非空
        String result = BaseController.validateList(list);
        BigDecimal total = BigDecimal.ZERO;
        if (StringUtils.isEmpty(result)) {
            //校验重复 TODO
            //校验小计
            BudgetReimbursementorderTravel bean = null;
            BigDecimal theTotal = BigDecimal.ZERO;
            for (int i = 0; i < list.size(); i++) {
                bean = list.get(i);
                if (null == bean.getTravelday()) {
                    bean.setTravelday(BigDecimal.ZERO);
                }
                if (null == bean.getLongtravelexp()) {
                    bean.setLongtravelexp(BigDecimal.ZERO);
                }
                if (null == bean.getCitytravelexp()) {
                    bean.setCitytravelexp(BigDecimal.ZERO);
                }
                if (null == bean.getHotelexpense()) {
                    bean.setHotelexpense(BigDecimal.ZERO);
                }
                if (null == bean.getDailysubsidy()) {
                    bean.setDailysubsidy(BigDecimal.ZERO);
                }
                if (null == bean.getOther()) {
                    bean.setOther(BigDecimal.ZERO);
                }
                theTotal = bean.getTravelday().multiply(bean.getDailysubsidy()).multiply(new BigDecimal(length)).add(bean.getLongtravelexp()).add(bean.getCitytravelexp()).add(bean.getHotelexpense()).add(bean.getOther());
                total = total.add(theTotal);
                if (theTotal.compareTo(bean.getTotal()) != 0) {
                    return "第" + i + 1 + "条差旅费【小计】不等于【长途交通费】+【市内交通费】+【住宿费】+【出差补助金额】+【其他】之和！";
                }
            }
            if(total.compareTo(reimMoney)!=0) return "差旅费总额需等于报销金额！";
        }
        return result;
    }

    public static void setBase(List<BudgetReimbursementorderTravel> list, BudgetReimbursementorder order) {
        list.forEach(ele -> {
            setBase(ele, order);
        });
    }

    public static void setBase(BudgetReimbursementorderTravel bean, BudgetReimbursementorder order) {
        bean.setReimbursementid(order.getId());
    }

    public BudgetReimbursementorderTravel(TravelDetailDto excelDto) {
        this.travelvehicle = excelDto.getVehicleType();
        this.travelstart = excelDto.getStart();
        this.travelend = excelDto.getEnd();
        this.travelorigin = excelDto.getLocation();
        this.traveldest = excelDto.getMdd();
        this.citytravelexp = new BigDecimal(String.valueOf(excelDto.getSnf()));
        this.longtravelexp = new BigDecimal(String.valueOf(excelDto.getCtf()));
        this.hotelexpense = new BigDecimal(String.valueOf(excelDto.getZsf()));
        this.travelday = new BigDecimal(String.valueOf(excelDto.getTs()));
        this.dailysubsidy = new BigDecimal(String.valueOf(excelDto.getBz()));
        this.other = new BigDecimal(String.valueOf(excelDto.getQt()));
        this.total = new BigDecimal(String.valueOf(excelDto.getXj()));
    }

    public static final List<BudgetReimbursementorderTravel> getTestBean() {
        List<BudgetReimbursementorderTravel> list = new ArrayList<BudgetReimbursementorderTravel>();
        BudgetReimbursementorderTravel bean = new BudgetReimbursementorderTravel();
        bean.setTravelvehicle(1);
        bean.setTravelstart("2021-05-22 09:00:00");
        bean.setTravelend("2021-05-31 09:00:00");
        bean.setTravelorigin("南昌");
        bean.setTraveldest("亳州");
        bean.setCitytravelexp(new BigDecimal("200"));
        bean.setLongtravelexp(new BigDecimal("500"));
        bean.setHotelexpense(new BigDecimal("600"));
        bean.setTravelday(new BigDecimal("10"));
        bean.setDailysubsidy(new BigDecimal("80"));
        bean.setOther(new BigDecimal("300"));
        bean.setTotal(new BigDecimal("1000"));

        BudgetReimbursementorderTravel _bean = new BudgetReimbursementorderTravel();
        _bean.setTravelvehicle(1);
        _bean.setTravelstart("2021-05-22 09:00:00");
        _bean.setTravelend("2021-05-31 09:00:00");
        _bean.setTravelorigin("南昌");
        _bean.setTraveldest("亳州");
        _bean.setCitytravelexp(new BigDecimal("200"));
        _bean.setLongtravelexp(new BigDecimal("500"));
        _bean.setHotelexpense(new BigDecimal("600"));
        _bean.setTravelday(new BigDecimal("10"));
        _bean.setDailysubsidy(new BigDecimal("80"));
        _bean.setOther(new BigDecimal("300"));
        _bean.setTotal(new BigDecimal("1000"));

        list.add(bean);
        list.add(_bean);
        return list;
    }

}
