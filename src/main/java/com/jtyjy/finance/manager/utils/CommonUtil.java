package com.jtyjy.finance.manager.utils;

import com.jtyjy.finance.manager.bean.BudgetYearPeriod;
import com.jtyjy.finance.manager.constants.Constants;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {

	//截止日期
	private final static String BY_DAY = "25";


	/**
	 * 获取当前启动月份之前所有的月份
	 *
	 * @param monthid 月id
	 * @param curflag 包括当月
	 * @return
	 */
	public static String getMonthids(Long monthid, Boolean curflag) {
		String results = "";
		if (monthid.intValue() <= 12 && monthid.intValue() >= 6) {
			for (int i = 6; i <= monthid.intValue(); i++) {
				if (!curflag && i == monthid.intValue()) {
					break;
				}
				if (StringUtils.isEmpty(results)) {
					results = "" + i;
				} else {
					results += "," + i;
				}
			}
		}
		if (monthid.intValue() <= 5 && monthid.intValue() >= 1) {
			results = "6,7,8,9,10,11,12";
			for (int i = 1; i <= monthid.intValue(); i++) {
				if (!curflag && i == monthid.intValue()) {
					break;
				}
				if (StringUtils.isEmpty(results)) {
					results = "" + i;
				} else {
					results += "," + i;
				}
			}
		}
		if (StringUtils.isEmpty(results)) {
			results = "0";
		}
		return results;
	}


	/**
	 * 获取结束日期
	 *
	 * @param yearid
	 * @param monthid
	 * @param curflag 是否是当月
	 * @return
	 */
	public static String getEnddate(BudgetYearPeriod yearPeriod, Long monthid, Boolean curflag) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
		Date startdate = yearPeriod.getStartdate();
		String startyear = formatter.format(startdate);
		Date enddate = yearPeriod.getEnddate();
		String endyear = formatter.format(enddate);
		String enddateres = "";
		if (!curflag) {
			if (monthid == 6) {
				monthid = 6L;
			} else if (monthid == 1) {
				monthid = 12L;
			} else {
				monthid = monthid - 1;
			}
		}
		if (monthid >= 6 && monthid <= 12) {
			enddateres = startyear + "-" + (monthid > 9 ? ("" + monthid) : ("0" + monthid)) + "-" + BY_DAY;
		}
		if (monthid >= 1 && monthid <= 5) {
			enddateres = endyear + "-" + ("0" + monthid) + "-" + BY_DAY;
		}
		return enddateres;
	}

	public static void main(String[] args) throws ParseException {
		System.out.println(getMonthids(7L, false));

		BudgetYearPeriod byp = new BudgetYearPeriod();
		byp.setStartdate(Constants.FORMAT_10.parse("2021-06-01"));
		byp.setEnddate(Constants.FORMAT_10.parse("2022-05-31"));
		System.out.println(getEnddate(byp, 7L, true));
	}
}
