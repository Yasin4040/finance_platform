package com.jtyjy.finance.manager.vo;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import com.jtyjy.finance.manager.dto.ReimbursementRequest;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

/**
 * 报销单打印
 * @author User
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReimbursementMainPrintVo {
	@ApiModelProperty(value = "届别",hidden = false)
	private String year;
	@ApiModelProperty(value = "月份",hidden = false)
	private String month;
	@ApiModelProperty(value = "二维码",hidden = false)
	private String qrCode;
	@ApiModelProperty(value = "预算单位名称",hidden = false)
	private String unitName;
	@ApiModelProperty(value = "报销时间",hidden = false)
	private String theTime;
	@ApiModelProperty(value = "附件个数",hidden = false)
	private Integer fileCount;
	@ApiModelProperty(value = "流水号",hidden = false)
	private String serialNumber;
	@ApiModelProperty(value = "备注",hidden = false)
	private String remark;
	@ApiModelProperty(value = "报销总计",hidden = false)
	private BigDecimal total;
	@ApiModelProperty(value = "冲账金额",hidden = false)
	private BigDecimal paymenyMoney;
	@ApiModelProperty(value = "借款人",hidden = false)
	private String lendmoneyName;
	@ApiModelProperty(value = "借款金额",hidden = false)
	private BigDecimal lendMoney;
	@ApiModelProperty(value = "转账金额",hidden = false)
	private BigDecimal transMoney;
	@ApiModelProperty(value = "转账户名",hidden = false)
	private String transAccountName;
	@ApiModelProperty(value = "转账账号",hidden = false)
	private String transAccount;
	@ApiModelProperty(value = "转账开户行",hidden = false)
	private String transOpenBank;
	@ApiModelProperty(value = "现金金额",hidden = false)
	private BigDecimal cashMoney;
	@ApiModelProperty(value = "其他金额",hidden = false)
	private BigDecimal other;
	@ApiModelProperty(value = "报销人",hidden = false)
	private String name;
	@ApiModelProperty(value = "科目信息",hidden = false)
	private List<Subject> subjectInfo;
	@ApiModelProperty(value = "出差人员",hidden = false)
	private String traveler;
	@ApiModelProperty(value = "出差事由",hidden = false)
	private String travelreason;
	private Integer bxType;
	private String bxTypeName;

	public static ReimbursementMainPrintVo apply(ReimbursementRequest request) {
		FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd");
		BudgetReimbursementorder order = request.getOrder();
		List<BudgetReimbursementorderDetail> details = request.getOrderDetail();
		ReimbursementMainPrintVo vo = new ReimbursementMainPrintVo();
		vo.setYear(order.getYearName());
		vo.setMonth(order.getMonthid().toString());
		vo.setQrCode(order.getQrcodebase64str());
		vo.setUnitName(order.getUnitName());
		vo.setTheTime(format.format(order.getReimdate()));
		vo.setFileCount(order.getAttachcount());
		vo.setSerialNumber(order.getReimcode());
		vo.setBxType(order.getBxtype());
		vo.setBxTypeName(order.getBxTypeName());
		String remarks = details.stream().filter(e->StringUtils.isNotBlank(e.getRemark())).map(e -> e.getRemark()).collect(Collectors.joining(","));

		//设置备注和科目信息
		//StringJoiner sj = new StringJoiner("；");
		List<Subject> subjectList = new ArrayList<Subject>();
		if(order.getBxtype() == 1){
			for (int i = 0; i < details.size(); i++) {
				BudgetReimbursementorderDetail detail = details.get(i);
				String remark = "";

				remark = detail.getRemark();
				if(i==0 && request.getOrderAllocated().size()>0) remark = "(划拨单)<br>"+remark;
				subjectList.add(new Subject(detail.getSubjectid(), detail.getSubjectCode(), detail.getSubjectname(), detail.getReimmoney(),remark,null,null));
			}
			vo.setRemark(subjectList.stream().filter(e->StringUtils.isNotBlank(e.getRemark())).map(e->e.getRemark()).distinct().collect(Collectors.joining("<br>")));
		}else if(order.getBxtype() ==2 || order.getBxtype() ==4){
			int length = 0;
			String travels = "";
			if(StringUtils.isNotBlank(order.getTraveler())){
				length = order.getTraveler().replace("，",",").split(",").length;
			}
			int lengthTemp = length;
			for (int i = 0; i < request.getOrderTravel().size(); i++) {
				BudgetReimbursementorderTravel travel = request.getOrderTravel().get(i);
				TravelPrintInfo info = new TravelPrintInfo();
				//BeanUtils.copyProperties(travel,info);
				removeZeroData(lengthTemp, travel, info);
				info.setStartMonth(travel.getTravelstart().substring(5,7));
				info.setStartDay(travel.getTravelstart().substring(8,10));
				info.setEndMonth(travel.getTravelend().substring(5,7));
				info.setEndDay(travel.getTravelend().substring(8,10));

				String remark = "";
				if(i==0){
					remark = remarks;
					if(i==0 && request.getOrderAllocated().size()>0) remark = "(划拨单)<br>"+remark;
				}
				info.setRemark(remark);
				subjectList.add(new Subject(null,null,null,null,null,info,null));
			}
			int j = subjectList.size();
			if(j<4){
				for(int i=0;i<4-j;i++){
					subjectList.add(new Subject(null,null,null,null,null,null,null));
				}
			}
			vo.setTraveler(order.getTravelerName());
			vo.setTravelreason(order.getTravelreason());
			vo.setRemark(subjectList.stream().filter(e->e.getTravelPrintInfo()!=null &&  StringUtils.isNotBlank(e.getTravelPrintInfo().getRemark())).map(e->e.getTravelPrintInfo().getRemark()).distinct().collect(Collectors.joining("<br>")));
		}else if(order.getBxtype() == 3 || order.getBxtype() == 5){
			for (int i = 0; i < request.getOrderEntertain().size(); i++) {
				BudgetReimbursementorderEntertain entertain = request.getOrderEntertain().get(i);
				EntertainPrintInfo info = new EntertainPrintInfo();
				//BeanUtils.copyProperties(entertain,info);
				removeFeteZeroData(entertain, info);
				info.setDate(entertain.getDate());
				String remark = "";

				remark = remarks;
				if(i==0 && request.getOrderAllocated().size()>0) {
					remark = "(划拨单)<br>"+remark;
				}
				info.setRemark(remark);
				subjectList.add(new Subject(null,null,null,null,null,null,info));
			}
			int j = subjectList.size();
			if(j<4){
				for(int i=0;i<4-j;i++){
					subjectList.add(new Subject(null,null,null,null,null,null,null));
				}
			}
			vo.setRemark(subjectList.stream().filter(e-> e.getEntertainPrintInfo() !=null && StringUtils.isNotBlank(e.getEntertainPrintInfo().getRemark())).map(e->e.getEntertainPrintInfo().getRemark()).distinct().collect(Collectors.joining("<br>")));
		}

		vo.setSubjectInfo(subjectList);
		vo.setTotal(order.getReimmoney());
		String defaultName = "见详请";
		List<BudgetReimbursementorderPayment> pList = request.getOrderPayment();

		Optional.ofNullable(pList).ifPresent(e->{
			BigDecimal paymentMoney = BigDecimal.ZERO;
			BigDecimal lendMoney = BigDecimal.ZERO;
			for (BudgetReimbursementorderPayment bean : e) {
				paymentMoney = paymentMoney.add(bean.getPaymentmoney());
				lendMoney = lendMoney.add(bean.getLendmoney());
			}
			if(e.size() == 1){
				vo.setPaymenyMoney(paymentMoney);
				vo.setLendMoney(lendMoney);
				vo.setLendmoneyName(e.get(0).getLendmoneyname());
			}else if(e.size()>1){
				vo.setPaymenyMoney(paymentMoney);
				vo.setLendMoney(null);
				vo.setLendmoneyName(defaultName);
			}
		});
		//设置转账信息
		List<BudgetReimbursementorderTrans> tList = request.getOrderTrans();
		Optional.ofNullable(tList).ifPresent(e->{
			BigDecimal money = BigDecimal.ZERO;
			for (BudgetReimbursementorderTrans bean : e) {
				money = money.add(bean.getTransmoney());
			}
			if(e.size() == 1){
				vo.setTransMoney(money);
				vo.setTransAccount(e.get(0).getPayeebankaccount());
				vo.setTransAccountName(e.get(0).getPayeename());
				vo.setTransOpenBank(e.get(0).getPayeebankname());
			}else if(e.size() > 1){
				vo.setTransMoney(money);
				vo.setTransAccount(null);
				vo.setTransAccountName(defaultName);
				vo.setTransOpenBank(null);
			}
		});
		//设置现金信息
		vo.setCashMoney(order.getCashmoney().compareTo(BigDecimal.ZERO) == 0 ? null : order.getCashmoney());
		vo.setOther(order.getOthermoney());
		vo.setName(order.getReimperonsname());

		return vo;
	}

	/**
	 * 处理招待费用为0的情况
	 * @param entertain
	 * @param info
	 */
	private static void removeFeteZeroData(BudgetReimbursementorderEntertain entertain, EntertainPrintInfo info) {
		BigDecimal standard = new BigDecimal(0);
		if(standard.compareTo(entertain.getMealstotal())==-1){
			info.setMealsbz(entertain.getMealsbz());
			info.setMealsrs(entertain.getMealsrs());
			info.setMealstotal(entertain.getMealstotal());
		}
		if(standard.compareTo(entertain.getHotaltotal())==-1){
			info.setHotalbz(entertain.getHotalbz());
			info.setHotaljs(entertain.getHotaljs());
			info.setHotalrs(entertain.getHotalrs());
			info.setHotaltotal(entertain.getHotaltotal());
		}
		if(standard.compareTo(entertain.getPublicityexp())==-1){
			info.setPublicityexp(entertain.getPublicityexp());
		}
		if(standard.compareTo(entertain.getOther())==-1){
			info.setOther(entertain.getOther());
		}
		if(standard.compareTo(entertain.getTotal())==-1){
			info.setTotal(entertain.getTotal());
		}
	}

	private static void removeZeroData(int lengthTemp, BudgetReimbursementorderTravel travel, TravelPrintInfo info) {
		info.setTravelvehicleName(travel.getTravelvehicleName());
		info.setTravelorigin(travel.getTravelorigin());
		info.setTraveldest(travel.getTraveldest());
		info.setOther(travel.getOther());
		info.setTotal(travel.getTotal());
		BigDecimal standard = new BigDecimal(0);
		if(standard.compareTo(travel.getCitytravelexp())==-1){
			info.setCitytravelexp(travel.getCitytravelexp());
		}
		if(standard.compareTo(travel.getLongtravelexp())==-1){
			info.setLongtravelexp(travel.getLongtravelexp());
		}
		if(standard.compareTo(travel.getHotelexpense())==-1){
			info.setHotelexpense(travel.getHotelexpense());
		}
		if(standard.compareTo(travel.getDailysubsidy())==-1){
			info.setDailysubsidy(travel.getDailysubsidy());
			if(standard.compareTo(travel.getTravelday())==-1){
				info.setTravelday(travel.getTravelday());
				if(0!= lengthTemp){
					info.setSubsidyMoney(new BigDecimal(lengthTemp).multiply(travel.getDailysubsidy()).multiply(travel.getTravelday()));
				}
			}
		}
	}
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
class Subject{
    private Long id;
	private String subjectDm;
	private String subjectName;
	private BigDecimal money;
	private String remark;

	private TravelPrintInfo travelPrintInfo;
	private EntertainPrintInfo entertainPrintInfo;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
class EntertainPrintInfo{
	@ApiModelProperty(value = "日期", hidden = false, required = true)
	@JsonFormat(timezone = "GMT+8",pattern = "MM.dd")
	private Date date;
	@ApiModelProperty(value = "餐费人数", hidden = false, required = true)
	private Integer mealsrs;
	@ApiModelProperty(value = "餐费标准", hidden = false, required = true)
	private BigDecimal mealsbz;
	@ApiModelProperty(value = "餐费总计", hidden = false, required = true)
	private BigDecimal mealstotal;
	@ApiModelProperty(value = "住宿人数", hidden = false, required = true)
	@TableField(value = "hotalrs")
	private Integer hotalrs;
	@ApiModelProperty(value = "住宿标准", hidden = false, required = true)
	private BigDecimal hotalbz;
	@ApiModelProperty(value = "间数", hidden = false, required = true)
	private Integer hotaljs;
	@ApiModelProperty(value = "住宿总计", hidden = false, required = true)
	private BigDecimal hotaltotal;
	@ApiModelProperty(value = "宣传品费", hidden = false, required = true)
	private BigDecimal publicityexp;
	@ApiModelProperty(value = "其它", hidden = false, required = true)
	private BigDecimal other;
	@ApiModelProperty(value = "小计", hidden = false, required = true)
	private BigDecimal total;
	@ApiModelProperty(value = "备注", hidden = false, required = true)
	private String remark;
}


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
class TravelPrintInfo{

	@ApiModelProperty(value = "交通工具",hidden = false)
	private String travelvehicleName;
	@ApiModelProperty(value = "开始月",hidden = false)
	@JsonFormat(timezone = "GMT+8",pattern = "MM")
	private String startMonth;
	@ApiModelProperty(value = "开始天",hidden = false)
	@JsonFormat(timezone = "GMT+8",pattern = "dd")
	private String startDay;
	@ApiModelProperty(value = "结束月",hidden = false)
	@JsonFormat(timezone = "GMT+8",pattern = "MM")
	private String endMonth;
	@ApiModelProperty(value = "结束日",hidden = false)
	@JsonFormat(timezone = "GMT+8",pattern = "dd")
	private String endDay;
	@ApiModelProperty(value = "出发地", hidden = false, required = true)
	private String travelorigin;
	@ApiModelProperty(value = "目的地", hidden = false, required = true)
	private String traveldest;
	@ApiModelProperty(value = "长途交通费", hidden = false, required = true)
	private BigDecimal longtravelexp;
	@ApiModelProperty(value = "市内交通费", hidden = false, required = true)
	private BigDecimal citytravelexp;
	@ApiModelProperty(value = "住宿费", hidden = false, required = true)
	private BigDecimal hotelexpense;
	@ApiModelProperty(value = "补助天数", hidden = false, required = true)
	private BigDecimal travelday;
	@ApiModelProperty(value = "每天补助", hidden = false, required = true)
	private BigDecimal dailysubsidy;
	@ApiModelProperty(value = "金额", hidden = false, required = true)
	private BigDecimal subsidyMoney;
	@ApiModelProperty(value = "其它", hidden = false, required = true)
	private BigDecimal other;
	@ApiModelProperty(value = "小计", hidden = false, required = true)
	private BigDecimal total;
	@ApiModelProperty(value = "备注", hidden = false, required = true)
	private String remark;
}
