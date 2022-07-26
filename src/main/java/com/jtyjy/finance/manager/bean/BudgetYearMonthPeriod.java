package com.jtyjy.finance.manager.bean;

import lombok.Data;

@Data
public class BudgetYearMonthPeriod {
	//年度期间
	private BudgetYearPeriod budgetYearPeriod;
	//月度期间
	private BudgetMonthPeriod budgetMonthPeriod;
}
