package com.jtyjy.finance.manager.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.easyexcel.MonthAgentCollectExcelData;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.utils.HttpUtil;
import com.jtyjy.finance.manager.utils.TreeUtil;
import com.jtyjy.finance.manager.vo.BudgetMonthSubjectVO;
import com.jtyjy.finance.manager.vo.BudgetSubjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetMonthSubjectService extends DefaultBaseService<BudgetMonthSubjectMapper, BudgetMonthSubject> {

	private final TabChangeLogMapper loggerMapper;
	private final BudgetUnitMapper budgetUnitMapper;
	private final BudgetMonthStartupMapper budgetMonthStartupMapper;
	private final BudgetMonthSubjectMapper budgetMonthSubjectMapper;
	private final BudgetMonthSubjectHisService monthSubjectHisService;
	private final BudgetMonthEndUnitMapper budgetMonthEndUnitMapper;
	private final BudgetMonthAgentMapper budgetMonthAgentMapper;
	@Autowired
	private BudgetSysService budgetSysService;
	@Value("${newjf.submitMonthBudget.url}")
	private String newJfUrl;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}

	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_month_subject"));
	}

	/**
	 * ??????????????????
	 */
	public List<BudgetMonthSubjectVO> monthAgentSubject(Long budgetUnitId, Long monthId) {
		List<BudgetMonthSubjectVO> monthSubjectList = this.budgetMonthSubjectMapper.listMonthSubjectByUnitId(budgetUnitId, monthId);
		//String monthIdStrs = CommonUtil.getMonthids(monthId, false);
		//List<Long> monthIds = Arrays.stream(monthIdStrs.split(",")).map(Long::valueOf).collect(Collectors.toList());
		//List<BudgetMonthSubjectVO> monthSubjectList = this.budgetMonthSubjectMapper.listMonthSubjectByUnitIdAndMonth(budgetUnitId, monthIds, monthId);
		//update by minzhq ?????????????????????

		monthSubjectList.forEach(v -> {
			v.setYearAddMoney(v.getYearAddMoney().add(v.getYearLendInMoney().subtract(v.getYearLendOutMoney())));
		});
		// ??????????????????
		monthSubjectDeal(monthSubjectList);

		return monthSubjectList;
	}


	private void monthSubjectDeal(List<BudgetMonthSubjectVO> monthSubjectList) {
		monthSubjectList.forEach(v -> {
			// ??????????????????
			v.setYearTotalMoney(v.getYearMoney()
					.add(v.getYearAddMoney()));

			// ??????????????????
			v.setYearSurplusMoney(v.getYearTotalMoney().subtract(v.getYearExecuteMoney()));

			// ?????????
			BigDecimal zxl = BigDecimal.ZERO;
			if (BigDecimal.ZERO.compareTo(v.getYearTotalMoney()) != 0) {
				zxl = v.getYearExecuteMoney().divide(v.getYearTotalMoney(), 4, BigDecimal.ROUND_HALF_UP);
			}
			v.setZxl(zxl);

			// ???????????????
			v.setSyZxl(BigDecimal.ZERO.compareTo(v.getYearSurplusMoney()) == 0 ? BigDecimal.ZERO : BigDecimal.ONE.subtract(v.getZxl()));
		});

	}

	/**
	 * ??????????????????
	 */
	public void syncMonthAgentData(Long budgetUnitId, Long monthId) throws Exception {
		BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
		if (budgetUnit == null) {
			throw new RuntimeException("????????????Id??????");
		}

		// ??????????????????????????????
		BudgetMonthStartup monthStartup = this.budgetMonthStartupMapper.selectOne(new QueryWrapper<BudgetMonthStartup>()
				.eq("yearid", budgetUnit.getYearid())
				.eq("monthid", monthId));
		if (monthStartup == null || !monthStartup.getStartbudgetflag()) {
			throw new RuntimeException("???????????????????????????????????????");
		}

		// ??????????????????
		this.budgetSysService.restartUnitMonthBudget(budgetUnit, String.valueOf(monthId));
		if (budgetUnit.getParentid() != 0) {
			budgetUnit = this.budgetUnitMapper.selectById(budgetUnit.getParentid());
			if (budgetUnit != null) {
				this.budgetSysService.restartUnitMonthBudget(budgetUnit, String.valueOf(monthId));
			}
		}
	}

	/**
	 * ??????????????????
	 */
	public void submitMonthBudget(Long budgetUnitId, Long monthId, String userId, String displayName) {
		BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
		if (budgetUnit == null) {
			throw new RuntimeException("????????????Id??????");
		}

		BudgetMonthEndUnit monthEndUnit = this.budgetMonthEndUnitMapper.selectOne(new QueryWrapper<BudgetMonthEndUnit>()
				.eq("unitid", budgetUnit.getId())
				.eq("monthid", monthId));
		boolean isMatchIntegralCondition = false;
		if (monthEndUnit == null) {
			throw new RuntimeException("???????????????????????????????????????");
		}
		if(monthEndUnit.getRequeststatus() == 0){
			isMatchIntegralCondition = true;
		}
		if (monthEndUnit.getRequeststatus() > 0) {
			throw new RuntimeException("???????????????????????????????????????");
		}
		if (!monthEndUnit.getCalculatesubjectflag()) {
			throw new RuntimeException("??????????????????????????????????????????");
		}

		Integer count = this.budgetMonthAgentMapper.selectCount(new QueryWrapper<BudgetMonthAgent>()
				.eq("unitid", budgetUnit.getId())
				.eq("monthid", monthId));
		if (count <= 0) {
			throw new RuntimeException("????????????????????????????????????????????????");
		}

		// ??????????????????
		BudgetMonthEndUnit updateMonthEndUnit = new BudgetMonthEndUnit();
		updateMonthEndUnit.setSubmitflag(true);
		updateMonthEndUnit.setRequeststatus(1);
		updateMonthEndUnit.setUpdatetime(new Date());
		updateMonthEndUnit.setSubmittime(new Date());
		updateMonthEndUnit.setSubmitorid(userId);
		updateMonthEndUnit.setSubmitorname(displayName);
		this.budgetMonthEndUnitMapper.update(updateMonthEndUnit, new QueryWrapper<BudgetMonthEndUnit>()
				.eq("unitid", budgetUnit.getId())
				.eq("monthid", monthId));


		/*
		 * ??????????????????????????????????????????????????????????????????
		 */
		if(isMatchIntegralCondition){
			try{
				HttpUtil.doGet(newJfUrl+"?empNo="+ UserThreadLocal.get().getUserName()+"&month="+monthId);
			}catch (Exception ignored){ }
		}
	}

	/**
	 * ??????????????????????????????
	 */
	public List<MonthAgentCollectExcelData> exportMonthAgentCollect(Long budgetUnitId, Long monthId) {
		List<BudgetMonthSubjectVO> monthSubjectList = this.budgetMonthSubjectMapper.listMonthSubjectByUnitId(budgetUnitId, monthId);
		if (monthSubjectList.isEmpty()) {
			return new ArrayList<>();
		} else if ("???????????????".equals(monthSubjectList.get(0).getUnitName())) {
			exportHrMonthAgentCollect(monthSubjectList);
		} else {
			//????????????  ?????????????????????   update by minzhq 2022-1-7  ?????????????????????
			monthSubjectList.forEach(v -> {
				v.setYearAddMoney(v.getYearAddMoney().add(v.getYearLendInMoney().subtract(v.getYearLendOutMoney())));
			});
		}

		// ??????????????????
		monthSubjectDeal(monthSubjectList);

		// ??????????????????
		String budgetTime;
		BudgetMonthSubjectVO subjectVO = monthSubjectList.get(0);
		if (subjectVO.getMonthId() > 5 && subjectVO.getMonthId() <= 12) {
			budgetTime = (Integer.parseInt(subjectVO.getYearCode()) - 1) + "." + subjectVO.getMonthId();
		} else {
			budgetTime = subjectVO.getYearCode() + "." + subjectVO.getMonthId();
		}
		return putMonthAgentCollect(budgetTime, monthSubjectList);
	}

	private void exportHrMonthAgentCollect(List<BudgetMonthSubjectVO> monthSubjectList) {
		monthSubjectList.forEach(v -> {
			Long subjectId = v.getId();
			Long monthId = v.getMonthId();
			String subjectName = v.getSubjectName();
			if ("??????".equals(subjectName) || "?????????".equals(subjectName) || "?????????".equals(subjectName)
					|| "???????????????".equals(subjectName) || "?????????".equals(subjectName) || "????????????".equals(subjectName)
					|| "???????????????".equals(subjectName) || "????????????".equals(subjectName) || "????????????".equals(subjectName)
					|| "???????????????".equals(subjectName) || "????????????".equals(subjectName) || "?????????".equals(subjectName)
					|| "?????????".equals(subjectName)) {
				List<BudgetMonthSubjectVO> monthSubjects = this.budgetMonthSubjectMapper.listMonthSubjectBySubjectId(subjectId, monthId);

				v.setMonthMoney(monthSubjects.stream().map(BudgetMonthSubjectVO::getMonthMoney).reduce(BigDecimal.ZERO, BigDecimal::add));
				v.setYearMoney(monthSubjects.stream().map(BudgetMonthSubjectVO::getYearMoney).reduce(BigDecimal.ZERO, BigDecimal::add));

				//????????????  ?????????????????????   update by minzhq 2022-1-7  ?????????????????????
				v.setYearAddMoney(monthSubjects.stream().map(e -> {
					return e.getYearAddMoney().add(e.getYearLendInMoney().subtract(e.getYearLendOutMoney()));
				}).reduce(BigDecimal.ZERO, BigDecimal::add));
				//v.setYearAddMoney(monthSubjects.stream().map(BudgetMonthSubjectVO::getYearAddMoney).reduce(BigDecimal.ZERO, BigDecimal::add));
				v.setYearLendInMoney(monthSubjects.stream().map(BudgetMonthSubjectVO::getYearLendInMoney).reduce(BigDecimal.ZERO, BigDecimal::add));
				v.setYearLendOutMoney(monthSubjects.stream().map(BudgetMonthSubjectVO::getYearLendOutMoney).reduce(BigDecimal.ZERO, BigDecimal::add));
				v.setYearExecuteMoney(monthSubjects.stream().map(BudgetMonthSubjectVO::getYearExecuteMoney).reduce(BigDecimal.ZERO, BigDecimal::add));
				v.setYearRevenueFormula(monthSubjects.stream().map(BudgetMonthSubjectVO::getYearRevenueFormula).reduce(BigDecimal.ZERO, BigDecimal::add));
				v.setMonthBusiness("????????????");
			}
		});
	}

	/**
	 * ??????????????????????????????
	 */
	public List<MonthAgentCollectExcelData> exportCompanyMonthAgentCollect(Long yearId, Long monthId) {
		List<BudgetMonthSubjectVO> monthSubjectList = this.budgetMonthSubjectMapper.exportCompanyMonthAgentCollect(yearId, monthId);
		if (monthSubjectList.isEmpty()) {
			return new ArrayList<>();
		}

		// ??????????????????
		monthSubjectDeal(monthSubjectList);

		return putMonthAgentCollect(null, monthSubjectList);
	}

	private List<MonthAgentCollectExcelData> putMonthAgentCollect(String budgetTime, List<BudgetMonthSubjectVO> monthSubjectList) {
		Map<Long, BudgetMonthSubjectVO> collect = monthSubjectList.stream().collect(Collectors.toMap(BudgetMonthSubjectVO::getId, Function.identity()));

		// ???????????????
		List<BudgetSubjectVO> subjectList = new ArrayList<>();
		monthSubjectList.forEach(v -> {
			BudgetSubjectVO subjectVO = new BudgetSubjectVO();
			subjectVO.setId(v.getId());
			subjectVO.setParentId(v.getParentId());
			subjectVO.setName(v.getSubjectName());
			subjectVO.setOrderNo(v.getOrderNo());
			subjectList.add(subjectVO);
		});
		List<BudgetSubjectVO> treeList = TreeUtil.build(subjectList);

		List<MonthAgentCollectExcelData> resultList = new ArrayList<>();
		putExcelData(budgetTime, treeList, collect, resultList, "");
		return resultList;
	}

	private void putExcelData(String budgetTime, List<BudgetSubjectVO> treeList, Map<Long, BudgetMonthSubjectVO> hashMap, List<MonthAgentCollectExcelData> resultList, String space) {
		BigDecimal decimal = new BigDecimal("100");
		for (BudgetSubjectVO node : treeList.stream().sorted(Comparator.comparing(BudgetSubjectVO::getOrderNo)).collect(Collectors.toList())) {
			if (!hashMap.containsKey(node.getId())) {
				continue;
			}
			BudgetMonthSubjectVO v = hashMap.get(node.getId());

			MonthAgentCollectExcelData excelData = new MonthAgentCollectExcelData();
			excelData.setBudgetTime(budgetTime);
			excelData.setUnitName(v.getUnitName());
			excelData.setSubjectName(space + v.getSubjectName());
			excelData.setYearMoney(v.getYearMoney());
			excelData.setYearAddMoney(v.getYearAddMoney());
			excelData.setYearTotalMoney(v.getYearTotalMoney());
			excelData.setYearRevenueFormula(v.getYearRevenueFormula().multiply(decimal).setScale(2, BigDecimal.ROUND_HALF_UP) + "%");
			excelData.setYearExecuteMoney(v.getYearExecuteMoney());
			excelData.setZxl(v.getZxl().multiply(decimal).setScale(2, BigDecimal.ROUND_HALF_UP) + "%");
			excelData.setYearSurplusMoney(v.getYearSurplusMoney());
			excelData.setSyZxl(v.getSyZxl().multiply(decimal).setScale(2, BigDecimal.ROUND_HALF_UP) + "%");
			excelData.setMonthMoney(v.getMonthMoney());
			excelData.setMonthBusiness(v.getMonthBusiness());
			excelData.setOrderNo(node.getOrderNo());
			resultList.add(excelData);

			// ??????????????????
			if (node.getChildren() != null) {
				putExcelData(budgetTime, node.getChildren(), hashMap, resultList, space + "  ");
			}
		}
	}

	/**
	 * ???????????????????????????
	 *
	 * @param realSubjectIds ??????????????????????????????
	 * @param yearId         ????????????
	 * @param unitId         ??????????????????
	 * @param monthId        ????????????
	 * @param opt            ?????? 1????????? 2????????? 3????????? 4?????????
	 * @param money          ????????????
	 */
	public void doSyncBudgetSubjectExecuteMoney(List<Long> realSubjectIds, long yearId, long unitId, long monthId, int opt, BigDecimal money) {
		QueryWrapper<BudgetMonthSubject> wrapper = new QueryWrapper<>();
		wrapper.eq("yearid", yearId);
		wrapper.eq("monthid", monthId);
		wrapper.eq("unitid", unitId);
		wrapper.in("subjectid", realSubjectIds);
		List<BudgetMonthSubject> list = this.list(wrapper);
		if (list != null && list.size() > 0) {
			List<BudgetMonthSubjectHis> hisList = new ArrayList<>();
			BudgetMonthSubjectHis budgetMonthSubjectHis;
			for (BudgetMonthSubject bean : list) {
				budgetMonthSubjectHis = JSON.parseObject(JSON.toJSONString(bean), BudgetMonthSubjectHis.class);
				budgetMonthSubjectHis.setType(opt);
				budgetMonthSubjectHis.setCreatetime(new Date());
				budgetMonthSubjectHis.setUpdatetime(new Date());
				budgetMonthSubjectHis.setTotal(bean.getTotal());
				// before
				budgetMonthSubjectHis.setBeforeaddmoney(bean.getAddmoney());
				budgetMonthSubjectHis.setBeforeexecutemoney(bean.getExecutemoney());
				budgetMonthSubjectHis.setBeforelendinmoney(bean.getLendinmoney());
				budgetMonthSubjectHis.setBeforelendoutmoney(bean.getLendoutmoney());
				//???????????? 1????????? 2????????? 3????????? 4?????????
				if (1 == opt) {
					bean.setAddmoney(bean.getAddmoney().add(money));
				} else if (2 == opt) {
					bean.setExecutemoney(bean.getExecutemoney().add(money));
				} else if (3 == opt) {
					bean.setLendinmoney(bean.getLendinmoney().add(money));
				} else if (4 == opt) {
					bean.setLendoutmoney(bean.getLendoutmoney().add(money));
				}
				// after
				budgetMonthSubjectHis.setAfteraddmoney(bean.getAddmoney());
				budgetMonthSubjectHis.setAfterexecutemoney(bean.getExecutemoney());
				budgetMonthSubjectHis.setAfterlendinmoney(bean.getLendinmoney());
				budgetMonthSubjectHis.setAfterlendoutmoney(bean.getLendoutmoney());
				hisList.add(budgetMonthSubjectHis);
			}
			//???????????????
			this.updateBatchById(list);
			//??????????????????
			this.monthSubjectHisService.saveBatch(hisList);
		}
	}
}
