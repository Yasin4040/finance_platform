package com.jtyjy.finance.manager.controller.authorfee.excel;

import com.jtyjy.finance.manager.bean.*;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ContributionFeeImportCommonData {
	private List<BudgetYearPeriod> yearPeriods;
	private List<BudgetUnit> unitList;
	private List<BudgetMonthPeriod> monthPeriods;
	private List<WbUser> userList;
	private List<WbDept> deptList;
	private List<WbPerson> personList;
	private List<Map<String,Object>> unitSubjects;
	private List<BudgetProductCategory> productCategories;
	private List<BudgetProduct> products;
	private List<BudgetMonthAgent> monthAgentList;
	private List<BudgetAuthor> authors;
	private List<BudgetAuthorfeesum> authorfeesums;
}
