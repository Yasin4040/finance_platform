package com.jtyjy.finance.manager.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.vo.BudgetSubjectAgentVO;
import com.jtyjy.finance.manager.vo.ExcelBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.jdbc.JdbcTemplateService;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.mapper.response.MonthAgentMoneyInfo;
import com.jtyjy.finance.manager.mapper.response.ReimbursementValidateMoney;
import com.jtyjy.finance.manager.utils.NumberUtil;
import com.jtyjy.finance.manager.vo.BudgetMonthAgentVO;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetMonthAgentService extends DefaultBaseService<BudgetMonthAgentMapper, BudgetMonthAgent> {

	private final TabChangeLogMapper loggerMapper;
	private final BudgetMonthAgentMapper budgetMonthAgentMapper;
	private final BudgetYearAgentMapper budgetYearAgentMapper;
	private final BudgetUnitMapper budgetUnitMapper;
	private final BudgetProductMapper budgetProductMapper;
	private final BudgetSubjectMapper budgetSubjectMapper;
	private final BudgetUnitSubjectMapper budgetUnitSubjectMapper;
	private final BudgetMonthStartupMapper budgetMonthStartupMapper;
	private final BudgetAgentExecuteViewMapper budgetAgentExecuteViewMapper;
	private final BudgetMonthPeriodMapper budgetMonthPeriodMapper;
	private final BudgetYearSubjectMapper budgetYearSubjectMapper;
	private final BudgetMonthEndUnitMapper budgetMonthEndUnitMapper;
	private final BudgetYearAgentaddMapper budgetYearAgentaddMapper;
	private final BudgetMonthAgentaddMapper budgetMonthAgentaddMapper;
	private final BudgetReimbursementorderDetailMapper budgetReimbursementorderDetailMapper;
	private final BudgetReimbursementorderAllocatedMapper budgetReimbursementorderAllocatedMapper;
	private final BudgetReimbursementorderMapper reimbursementorderMapper;
	private final BudgetYearAgentService budgetYearAgentService;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}

	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_month_agent"));
	}

	/**
	 * 按照主键查询
	 */
	public List<BudgetMonthAgent> getByIds(List<Long> ids) {
		QueryWrapper<BudgetMonthAgent> wrapper = new QueryWrapper<>();
		wrapper.in("id", ids);
		return this.list(wrapper);
	}

	/**
	 * 获取受指定条件限制的月度动因主键
	 */
	public List<Long> getControlAgentId(List<Long> ids, String columnName) throws Exception {
		String theIds = JdbcTemplateService.getInSql(ids, null);
		return this.budgetMonthAgentMapper.getControlAgentId(theIds, columnName);
	}

	/**
	 * 查询月度动因（分页）
	 */
	public PageResult<BudgetMonthAgentVO> monthAgentPage(Long budgetUnitId, Long budgetSubjectId, Integer monthId, Integer type, String name, Integer page, Integer rows, String category) {
		Page<BudgetMonthAgentVO> pageBean = new Page<>(page, rows);

		List<BudgetMonthAgentVO> budgetMonthAgents = null;
		if (type == 1) {
			// 月度动因（普通）
			budgetMonthAgents = budgetMonthAgentMapper.monthAgentPage1(pageBean, budgetUnitId, budgetSubjectId, monthId, name);
		} else if (type == 2) {
			// 月度动因（产品）
			budgetMonthAgents = budgetMonthAgentMapper.monthAgentPage2(pageBean, budgetUnitId, budgetSubjectId, monthId, name,category);
		} else if (type == 3) {
			// 月度动因（分解）
			budgetMonthAgents = budgetMonthAgentMapper.monthAgentPage3(pageBean, budgetSubjectId, monthId, name);
		}
		return PageResult.apply(pageBean.getTotal(), budgetMonthAgents);
	}

	/**
	 * 查询月度动因明细
	 */
	public List<Map<String, Object>> monthAgentDetail(Long monthAgentId) throws Exception {
		BudgetMonthAgent monthAgent = this.budgetMonthAgentMapper.selectById(monthAgentId);
		if (monthAgent == null) {
			throw new Exception("月度动因Id错误");
		}

		MonthAgentMoneyInfo info = new MonthAgentMoneyInfo();
		info.setMonthAgentId(monthAgentId);
		info.setMonthId(monthAgent.getMonthid());
		info.setYearId(monthAgent.getYearid());
		info.setUnitId(monthAgent.getUnitid());
		ReimbursementValidateMoney moneyResult = this.budgetMonthAgentMapper.getUnitYearAgentInfo(info);
		if (moneyResult == null) moneyResult = new ReimbursementValidateMoney();
		Map<String, Object> row = new LinkedHashMap<>();
		getYearAgentMsg(row, moneyResult);
		moneyResult = this.budgetMonthAgentMapper.getUnitMonthSubjectInfo(info);
		getMonthSubjectMsg(monthAgent.getUnitid(), monthAgent.getSubjectid(), monthAgent.getMonthid(), row, moneyResult);


		ReimbursementValidateMoney query = new ReimbursementValidateMoney(-1L, monthAgent.getYearid(), monthAgent.getMonthid(), monthAgent.getUnitid(), null, null);
		List<ReimbursementValidateMoney> resultList = reimbursementorderMapper.getYearCourseValidateMoney(query, monthAgentId.toString());
		getYearSubjectMsg(monthAgent.getUnitid(), monthAgent.getSubjectid(), row, resultList.get(0));
		row.put("月度动因追加", NumberUtil.subZeroAndDot(monthAgent.getAddmoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
		row.put("月度动因执行", NumberUtil.subZeroAndDot(monthAgent.getExecutemoney()).setScale(2, BigDecimal.ROUND_HALF_UP));

		List<Map<String, Object>> result = new ArrayList<>();
		row.forEach((key, value) -> {
			Map<String, Object> map = new HashMap<>(2);
			map.put("key", key);
			map.put("value", value);
			result.add(map);
		});
		return result;
	}

	private void getYearAgentMsg(Map<String, Object> row, ReimbursementValidateMoney result) {
		// 年度动因
		//BudgetYearAgent yearAgent = this.budgetYearAgentMapper.selectById(yearAgentId);
		//if (yearAgent == null) {
		//    return;
		//}
		// 本身

		row.put("年度动因预算", NumberUtil.subZeroAndDot(result.getTotal()).setScale(2, BigDecimal.ROUND_HALF_UP));
		row.put("年度动因追加", NumberUtil.subZeroAndDot(result.getAddmoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
		row.put("年度动因拆进", NumberUtil.subZeroAndDot(result.getLendinmoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
		row.put("年度动因拆出", NumberUtil.subZeroAndDot(result.getLendoutmoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
		row.put("年度动因执行", NumberUtil.subZeroAndDot(result.getHbmoney().add(result.getBxmoney())).setScale(2, BigDecimal.ROUND_HALF_UP));

		// 除了本身其它的报销明细
		//List<BudgetReimbursementorderDetail> details = this.budgetReimbursementorderDetailMapper.listDetailByYearAgentId(yearAgent.getId());
		//BigDecimal bxUsedMoney = details.stream().map(BudgetReimbursementorderDetail::getReimmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
		row.put("年度动因报销占用", NumberUtil.subZeroAndDot(result.getSdmoney()).setScale(2, BigDecimal.ROUND_HALF_UP));

		// 除了本身其它的划拨明细
		//List<BudgetReimbursementorderAllocated> allocatedList = this.budgetReimbursementorderAllocatedMapper.listDetailByYearAgentId(yearAgent.getId());
		//BigDecimal hbUsedMoney = allocatedList.stream().map(BudgetReimbursementorderAllocated::getAllocatedmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
		row.put("年度动因划拨占用", NumberUtil.subZeroAndDot(result.getHbsdmoney()).setScale(2, BigDecimal.ROUND_HALF_UP));

		//total = total.subtract(bxUsedMoney).subtract(hbUsedMoney);
		row.put("<font style='color:#F56C6C;font-weight:bold'>年度动因可用</font>", "<font style='color:#F56C6C;font-weight:bold'>" + NumberUtil.subZeroAndDot(result.execMoney()).setScale(2, BigDecimal.ROUND_HALF_UP) + "</font>");
	}

	private void getMonthSubjectMsg(Long unitId, Long subjectId, Long monthId, Map<String, Object> row, ReimbursementValidateMoney result) {
		//BudgetMonthSubject monthSubject = this.budgetMonthSubjectMapper.selectOne(new QueryWrapper<BudgetMonthSubject>()
		//        .eq("unitId", unitId)
		//        .eq("subjectid", subjectId)
		//        .eq("monthId", monthId));
		//if (monthSubject == null) {
		//    return;
		// }
		// 是否超过科目的月度预算
		//BigDecimal total = monthSubject.getTotal();


		//total = total.add(monthSubject.getAddmoney())
		//        .add(monthSubject.getLendinmoney())
		//        .subtract(monthSubject.getLendoutmoney())
		//        .subtract(monthSubject.getExecutemoney());
		row.put("月度科目预算", NumberUtil.subZeroAndDot(result.getTotal()).setScale(2, BigDecimal.ROUND_HALF_UP));
		row.put("月度科目追加", NumberUtil.subZeroAndDot(result.getAddmoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
		row.put("月度科目执行", NumberUtil.subZeroAndDot(result.getBxmoney().add(result.getHbmoney())).setScale(2, BigDecimal.ROUND_HALF_UP));

		// 获取月度科目报销占用
		// List<BudgetReimbursementorderDetail> details = this.budgetReimbursementorderDetailMapper.listDetailByMonthId(unitId, subjectId, monthId);
		//BigDecimal bxUsedMoney = details.stream().map(BudgetReimbursementorderDetail::getReimmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
		row.put("月度科目报销占用", NumberUtil.subZeroAndDot(result.getSdmoney()).setScale(2, BigDecimal.ROUND_HALF_UP));

		// 获取月度科目划拨占用
		//List<BudgetReimbursementorderAllocated> allocatedList = this.budgetReimbursementorderAllocatedMapper.listDetailByMonthId(unitId, subjectId, monthId);
		//BigDecimal hbUsedMoney = allocatedList.stream().map(BudgetReimbursementorderAllocated::getAllocatedmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
		row.put("月度科目划拨占用", NumberUtil.subZeroAndDot(result.getHbsdmoney()).setScale(2, BigDecimal.ROUND_HALF_UP));

		//total = total.subtract(bxUsedMoney).subtract(hbUsedMoney);
		row.put("<font style='color:#F56C6C;font-weight:bold'>月度科目可用</font>", "<font style='color:#F56C6C;font-weight:bold'>" + NumberUtil.subZeroAndDot(result.execMoney()).setScale(2, BigDecimal.ROUND_HALF_UP) + "</font>");
	}

	private void getYearSubjectMsg(Long unitId, Long subjectId, Map<String, Object> row, ReimbursementValidateMoney result) {
        /*BudgetYearSubject yearSubject = this.budgetYearSubjectMapper.selectOne(new QueryWrapper<BudgetYearSubject>()
                .eq("unitId", unitId)
                .eq("subjectid", subjectId));

        BigDecimal total = yearSubject.getTotal();
        total = total.add(yearSubject.getAddmoney())
                .add(yearSubject.getLendinmoney())
                .subtract(yearSubject.getLendoutmoney())
                .subtract(yearSubject.getExecutemoney());*/


		row.put("年度科目预算", NumberUtil.subZeroAndDot(result.getTotal()).setScale(2, BigDecimal.ROUND_HALF_UP));
		row.put("年度科目追加", NumberUtil.subZeroAndDot(result.getAddmoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
		row.put("年度科目拆进", NumberUtil.subZeroAndDot(result.getLendinmoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
		row.put("年度科目拆出", NumberUtil.subZeroAndDot(result.getLendoutmoney().setScale(2, BigDecimal.ROUND_HALF_UP)));
		row.put("年度科目执行", NumberUtil.subZeroAndDot(result.getBxmoney().add(result.getHbmoney())).setScale(2, BigDecimal.ROUND_HALF_UP));

		// 获取年度科目报销占用
		//List<BudgetReimbursementorderDetail> details = this.budgetReimbursementorderDetailMapper.listDetailByMonthId(unitId, subjectId, null);
		//BigDecimal bxUsedMoney = details.stream().map(BudgetReimbursementorderDetail::getReimmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
		row.put("年度科目报销占用", NumberUtil.subZeroAndDot(result.getSdmoney()));

		// 获取年度科目划拨占用
		//List<BudgetReimbursementorderAllocated> allocatedList = this.budgetReimbursementorderAllocatedMapper.listDetailByMonthId(unitId, subjectId, null);
		//BigDecimal hbUsedMoney = allocatedList.stream().map(BudgetReimbursementorderAllocated::getAllocatedmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
		row.put("年度科目划拨占用", NumberUtil.subZeroAndDot(result.getHbsdmoney()).setScale(2, BigDecimal.ROUND_HALF_UP));

		//total = total.subtract(bxUsedMoney).subtract(hbUsedMoney);
		row.put("<font style='color:#F56C6C;font-weight:bold'>年度科目可用</font>", "<font style='color:#F56C6C;font-weight:bold'>" + NumberUtil.subZeroAndDot(result.execMoney()).setScale(2, BigDecimal.ROUND_HALF_UP) + "</font>");
	}

	/**
	 * 修改月度动因
	 */
	public void updateMonthAgent(Long monthAgentId, BigDecimal newTotal, String monthBusiness) throws Exception {
		// 月度动因
		BudgetMonthAgent monthAgent = this.budgetMonthAgentMapper.selectById(monthAgentId);
		if (monthAgent == null) {
			throw new Exception("月度动因Id错误");
		}

		// 预算单位月结
		BudgetMonthEndUnit budgetMonthEndUnit = this.budgetMonthEndUnitMapper.selectOne(new QueryWrapper<BudgetMonthEndUnit>()
				.eq("yearId", monthAgent.getYearid())
				.eq("unitid", monthAgent.getUnitid())
				.eq("monthId", monthAgent.getMonthid())
				.eq("submitflag", 1));
		if (budgetMonthEndUnit != null && budgetMonthEndUnit.getRequeststatus() > 0) {
			BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(monthAgent.getUnitid());
			BudgetMonthPeriod monthPeriod = this.budgetMonthPeriodMapper.selectById(monthAgent.getMonthid());
			throw new RuntimeException("预算单位【" + budgetUnit.getName() + "】" + monthPeriod.getPeriod() + "的预算已经提交。");
		}

		// 金额变小
		BigDecimal total = newTotal.subtract(monthAgent.getTotal());
		if (total.compareTo(BigDecimal.ZERO) < 0) {
			// 追加（加上追加金额）
			BigDecimal tmpTotal = newTotal.add(monthAgent.getAddmoney());

			// 执行金额
			BigDecimal totalExecuteMoney = BigDecimal.ZERO;
			List<BudgetAgentExecuteView> executeViews = this.budgetAgentExecuteViewMapper.selectList(new QueryWrapper<BudgetAgentExecuteView>()
					.eq("monthagentid", monthAgent.getId())
					.gt("reuqeststatus", 0)
					.eq("reimflag", 1));
			for (BudgetAgentExecuteView executeInfo : executeViews) {
				BigDecimal executeMoney = executeInfo.getExecutemoney();
				tmpTotal = tmpTotal.subtract(executeMoney);
				totalExecuteMoney = totalExecuteMoney.add(executeMoney);
			}
			// 判断是否可修改
			if (tmpTotal.compareTo(BigDecimal.ZERO) < 0 && totalExecuteMoney.compareTo(BigDecimal.ZERO) != 0) {
				throw new RuntimeException("修改失败，月度动因【" + monthAgent.getName() + "】预算金额【" + newTotal + "】小于已报销金额【" + totalExecuteMoney + "】");
			}
		}

		// 检测年度预算余额
		checkYearBudgetBalance(monthAgent, newTotal);

		BudgetMonthAgent updateMonthAgent = new BudgetMonthAgent();
		updateMonthAgent.setId(monthAgentId);
		// 月度预算活动说明
		updateMonthAgent.setMonthbusiness(monthBusiness);
		// 月度预算金额(可编辑)
		updateMonthAgent.setTotal(newTotal);
		// 更新时间
		updateMonthAgent.setUpdatetime(new Date());

		this.budgetMonthAgentMapper.updateById(updateMonthAgent);

		// 判断是否有更新
		if (newTotal.compareTo(monthAgent.getTotal()) != 0) {
			modifyUpdateFlagOfUnitMonth(monthAgent.getUnitid(), monthAgent.getMonthid(), true);
		}
	}

	/**
	 * 检测年度预算余额
	 */
	private void checkYearBudgetBalance(BudgetMonthAgent monthAgent, BigDecimal newTotal) {
		// 年度控制
		Boolean yearControlFlag = true;
		BudgetUnitSubject budgetUnitSubject = this.budgetUnitSubjectMapper.selectOne(new QueryWrapper<BudgetUnitSubject>()
				.eq("unitId", monthAgent.getUnitid())
				.eq("subjectid", monthAgent.getSubjectid()));
		if (budgetUnitSubject != null) {
			yearControlFlag = budgetUnitSubject.getYearcontrolflag();
		}

		// 年度预算
		if (yearControlFlag) {
			// 年度动因
			BudgetYearAgent yearAgent = this.budgetYearAgentMapper.selectById(monthAgent.getYearagentid());
			if (yearAgent == null) {
				throw new RuntimeException("修改失败，年度动因记录未找到");
			}

			// 年初年度预算 + 累计追加金额 + 累计拆进金额 - 累计拆出金额
			BigDecimal tmp = yearAgent.getTotal()
					.add(yearAgent.getAddmoney())
					.add(yearAgent.getLendinmoney())
					.subtract(yearAgent.getLendoutmoney());

			// 查询年度动因下月份报销金额与划拨金额（已提交状态和已审核状态，且排除当前月份）
			List<Map<String, BigDecimal>> list = this.budgetMonthAgentMapper.listExecuteAndAllocateByExcludeMonthId(monthAgent.getYearagentid(), monthAgent.getMonthid());
			BigDecimal bxUsedMoney = list.stream().map(v -> v.get("bxMoney")).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal hbUsedMoney = list.stream().map(v -> v.get("hbMoney")).reduce(BigDecimal.ZERO, BigDecimal::add);

			// 控制年度预算
			tmp = tmp.subtract(bxUsedMoney).subtract(hbUsedMoney);
			if (tmp.compareTo(newTotal) < 0) {
				throw new RuntimeException("【" + monthAgent.getName() + "】年度预算不足。可用年度预算【" + NumberUtil.subZeroAndDot(tmp) + "】");
			}
		}
	}

	/**
	 * 月度数据修改更新标识
	 */
	public void modifyUpdateFlagOfUnitMonth(Long unitId, Long monthId, boolean updateFlag) {
		BudgetMonthEndUnit updateMonthEndUnit = new BudgetMonthEndUnit();
		updateMonthEndUnit.setUnitid(unitId);
		updateMonthEndUnit.setMonthid(monthId);
		updateMonthEndUnit.setUpdatetime(new Date());
		// 有更新了
		if (updateFlag) {
			updateMonthEndUnit.setUpdateagentflag(true);
			updateMonthEndUnit.setCalculatesubjectflag(false);
		} else {
			updateMonthEndUnit.setUpdateagentflag(false);
			updateMonthEndUnit.setCalculatesubjectflag(true);
		}
		this.budgetMonthEndUnitMapper.update(updateMonthEndUnit, new QueryWrapper<BudgetMonthEndUnit>().eq("unitId", unitId).eq("monthId", monthId));
	}

	/**
	 * 删除月度动因
	 */
	public void deleteMonthAgent(List<Long> monthAgentIds) {
		if (monthAgentIds == null || monthAgentIds.isEmpty()) {
			return;
		}

		// 月度动因
		List<BudgetMonthAgent> monthAgents = this.budgetMonthAgentMapper.selectList(new QueryWrapper<BudgetMonthAgent>().in("id", monthAgentIds));
		List<Long> yearAgentIds = monthAgents.stream().map(BudgetMonthAgent::getYearagentid).collect(Collectors.toList());

		// 产品、分解动因不能删除
		Integer notDeleteCount = this.budgetYearAgentMapper.countNotDeleteByIds(yearAgentIds);
		if (notDeleteCount > 0) {
			throw new RuntimeException("所选动因（产品、分解）不能删除");
		}

		// 已提交的预算单位不能删除动因
		Set<Long> unitIds = monthAgents.stream().map(BudgetMonthAgent::getUnitid).collect(Collectors.toSet());
		Integer count = this.budgetUnitMapper.selectCount(new QueryWrapper<BudgetUnit>().in("id", unitIds).gt("requeststatus", 0));
		if (count > 0) {
			throw new RuntimeException("所选动因预算单位年度预算已经提交");
		}

		Integer count1 = this.budgetYearAgentaddMapper.selectCount(new QueryWrapper<BudgetYearAgentadd>().in("monthagentid", monthAgentIds));
		if (count1 > 0) {
			throw new RuntimeException("所选动因存在年度追加记录");
		}
		Integer count2 = this.budgetMonthAgentaddMapper.selectCount(new QueryWrapper<BudgetMonthAgentadd>().in("monthagentid", monthAgentIds));
		if (count2 > 0) {
			throw new RuntimeException("所选动因存在月度追加记录");
		}
		Integer count3 = this.budgetReimbursementorderDetailMapper.selectCount(new QueryWrapper<BudgetReimbursementorderDetail>().in("monthagentid", monthAgentIds));
		if (count3 > 0) {
			throw new RuntimeException("所选动因已报销");
		}
		Integer count4 = this.budgetReimbursementorderAllocatedMapper.selectCount(new QueryWrapper<BudgetReimbursementorderAllocated>().in("monthagentid", monthAgentIds));
		if (count4 > 0) {
			throw new RuntimeException("所选动因已划拨");
		}
		this.budgetMonthAgentMapper.deleteBatchIds(monthAgentIds);
	}

	// 月度动因（普通、产品、分解）导入、导出 ----------------------------------------------------------------------------------------------------

	/**
	 * 下载月度动因模板
	 */
	public Map<String, List<BudgetMonthAgentVO>> exportMonthAgent(Long budgetUnitId, Long monthId, Integer type) {
		BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
		if (budgetUnit == null) {
			throw new RuntimeException("预算单位Id错误");
		}

		// 获取当前预算单位下所有(普通 or 产品 or 分解)月度动因
		List<BudgetMonthAgentVO> monthAgentList;
		switch (type) {
			case 1:
			case 2:
				monthAgentList = this.budgetMonthAgentMapper.listMonthAgentByUnitId(budgetUnitId, monthId, type);
				break;
			case 3:
				monthAgentList = this.budgetMonthAgentMapper.listCostSplitMonthAgent(budgetUnitId, monthId);
				break;
			default:
				monthAgentList = new ArrayList<>();
		}

		// 按预算科目分组
		return monthAgentList.stream().collect(Collectors.groupingBy(BudgetMonthAgentVO::getSubjectName));
	}

	/**
	 * 月度动因导入
	 *
	 * @param budgetUnitId 预算单位Id
	 * @param type         导入类型（1普通 2产品 3分解）
	 * @param excelDataMap excel文件内容
	 * @param errorDataMap 异常数据
	 */
	public void importMonthAgentExcel(Long budgetUnitId, Integer type, Map<String, List<List<String>>> excelDataMap, Map<String, List<List<String>>> errorDataMap) {
		BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
		if (budgetUnit == null) {
			throw new RuntimeException("预算单位Id错误");
		}

		// 检测预算科目 以及 获取月份Id
		HashMap<String, Long> subjectMap = new HashMap<>(2);
		Long monthId = getImportMonthId(type, budgetUnit, excelDataMap, subjectMap);

		// 检查月度预算是否启动
		BudgetMonthStartup monthStartup = this.budgetMonthStartupMapper.selectOne(new QueryWrapper<BudgetMonthStartup>()
				.eq("yearId", budgetUnit.getYearid())
				.eq("monthid", monthId));
		if (monthStartup == null || !monthStartup.getStartbudgetflag()) {
			throw new RuntimeException("月度预算未启动");
		}

		// 检查月度预算是否提交
		BudgetMonthEndUnit monthEndUnit = this.budgetMonthEndUnitMapper.selectOne(new QueryWrapper<BudgetMonthEndUnit>()
				.eq("unitid", budgetUnit.getId())
				.eq("monthid", monthId));
		if (monthEndUnit == null || monthEndUnit.getRequeststatus() > 0) {
			throw new RuntimeException("月度预算已提交");
		}

		Map<String, Long> hashMap = null;
		if (type == 2) {
			// 获取预算单位下的所有产品
			List<BudgetProduct> productList = this.budgetProductMapper.listProduct(budgetUnit.getId());
			hashMap = productList.stream().collect(Collectors.toMap(BudgetProduct::getName, BudgetProduct::getId));
		} else if (type == 3) {
			// 获取届别下所有预算单位
			List<BudgetUnit> unitList = this.budgetUnitMapper.selectList(new QueryWrapper<BudgetUnit>().eq("yearId", budgetUnit.getYearid()));
			hashMap = unitList.stream().collect(Collectors.toMap(BudgetUnit::getName, BudgetUnit::getId));
		}

		// 检测Excel导入数据
		Map<Long, List<BudgetMonthAgent>> successMap = new HashMap<>(2);
		importValidate(type, monthId, budgetUnit, subjectMap, hashMap, excelDataMap, successMap, errorDataMap);

		if (errorDataMap.isEmpty() && !successMap.isEmpty()) {
			HashSet<Long> unitIdSet = new HashSet<>();
			Date currentDate = new Date();
			for (Map.Entry<Long, List<BudgetMonthAgent>> entry : successMap.entrySet()) {

				for (BudgetMonthAgent monthAgent : entry.getValue()) {
					monthAgent.setUpdatetime(currentDate);

					unitIdSet.add(monthAgent.getUnitid());
				}
				// 批量更新
				this.updateBatchById(entry.getValue());
			}

			if (!unitIdSet.isEmpty()) {
				for (Long unitId : unitIdSet) {
					// 有更新
					modifyUpdateFlagOfUnitMonth(unitId, monthId, true);
				}
			}
		}
	}

	/**
	 * 获取导入的月份Id
	 */
	private Long getImportMonthId(Integer type, BudgetUnit budgetUnit, Map<String, List<List<String>>> excelDataMap, HashMap<String, Long> subjectMap) {
		String name = this.budgetYearAgentService.getYearAgentTypeName(type);

		HashSet<String> monthSet = new HashSet<>();

		for (Map.Entry<String, List<List<String>>> entry : excelDataMap.entrySet()) {
			// 预算科目名称
			String subjectName = entry.getKey();

			// 根据预算单位id和科目名称查询预算科目
			BudgetSubject subject = this.budgetSubjectMapper.getSubjectByUnitIdAndSubjectName(budgetUnit.getId(), subjectName, type);
			if (subject == null) {
				throw new RuntimeException("预算单位【" + budgetUnit.getName() + "】下不存在" + name + "科目【" + subjectName + "】");
			}
			subjectMap.put(subjectName, subject.getId());

			List<List<String>> sheetContent = entry.getValue();
			int size = sheetContent.size();
			for (int i = 0; i < size; i++) {
				// 表格正文从第二行开始
				if (i < 1) {
					continue;
				}
				monthSet.add(sheetContent.get(i).get(1));
			}
		}
		if (monthSet.size() != 1) {
			throw new RuntimeException("请确认所有sheet中的月份一致且不为空");
		}

		String month = (String) monthSet.toArray()[0];
		month = month.trim();
		if (month.endsWith("月")) {
			month = month.replace("月", "");
		}
		if (month.startsWith("0")) {
			month = month.replace("0", "");
		}
		if (!NumberUtil.isNumeric(month)) {
			throw new RuntimeException("请确认月份填写都有数字");
		} else if (!(Integer.parseInt(month) > 0 && Integer.parseInt(month) < 13)) {
			throw new RuntimeException("月份数字为1-12，请勿越界");
		}
		return Long.parseLong(month);
	}

	/**
	 * 月度动因导入数据校验
	 *
	 * @param type         导入类型（1普通 2产品 3分解）
	 * @param monthId      月份Id
	 * @param budgetUnit   预算单位对象
	 * @param subjectMap   预算科目Map
	 * @param hashMap      产品 或 预算单位 Map
	 * @param excelDataMap excel文件内容
	 * @param successMap   excel成功记录
	 * @param errorDataMap excel错误记录
	 */
	private void importValidate(Integer type, Long monthId, BudgetUnit budgetUnit, HashMap<String, Long> subjectMap, Map<String, Long> hashMap, Map<String, List<List<String>>> excelDataMap, Map<Long, List<BudgetMonthAgent>> successMap, Map<String, List<List<String>>> errorDataMap) {
		for (Map.Entry<String, List<List<String>>> entry : excelDataMap.entrySet()) {
			// 预算科目名称
			String subjectName = entry.getKey();

			Long subjectId = subjectMap.get(subjectName);

			List<BudgetMonthAgent> successList = new ArrayList<>();
			List<ExcelBean> errorList = new ArrayList<>();

			List<List<String>> sheetContent = entry.getValue();
			int size = sheetContent.size();
			for (int i = 0; i < size; i++) {
				// 表格正文从第二行开始
				if (i < 1) {
					continue;
				}
				monthAgentValidate(type, budgetUnit.getId(), subjectId, subjectName, monthId, sheetContent.get(i), hashMap, successList, errorList);
			}
			if (!successList.isEmpty()) {
				successMap.put(subjectId, successList);
			}
			if (!errorList.isEmpty()) {
				errorDataMap.put(subjectName, ExcelBean.transformList(errorList));
			}
		}
	}

	/**
	 * 普通动因、产品动因、分解动因导入数据校验
	 */
	private void monthAgentValidate(Integer type, Long unitId, Long subjectId, String subjectName, Long monthId, List<String> row, Map<String, Long> hashMap, List<BudgetMonthAgent> successList, List<ExcelBean> errorList) {
		int totalColumn = 5;
		try {
			if (type != 1) {
				// 产品动因、分解动因 月度活动说明可为空
				totalColumn -= 1;
			}
			BudgetMonthAgent budgetMonthAgent = new BudgetMonthAgent();

			int columnSize = row.size();
			if (columnSize < totalColumn) {
				throw new RuntimeException("内容填写不完整");
			}
			for (int i = 1; i <= columnSize; i++) {
				String data = row.get(i - 1);
				switch (i) {
					case 1:
						if (type == 1) {
							isNotBlank(data, "动因名称");
						} else if (type == 2) {
							isNotBlank(data, "产品名称");
							// 判断产品是否存在
							if (hashMap != null && !hashMap.containsKey(data)) {
								throw new RuntimeException("产品【" + data + "】不存在");
							}
						} else if (type == 3) {
							isNotBlank(data, "预算单位");
							if (hashMap != null && !hashMap.containsKey(data)) {
								throw new RuntimeException("预算单位【" + data + "】不存在");
							} else if (hashMap != null && hashMap.containsKey(data)) {
								unitId = hashMap.get(data);
							}
							// 分解动因名称为预算科目名称
							data = subjectName;
						}
						budgetMonthAgent.setName(data);
						break;
					case 2:
						isNotBlank(data, "月份");
						budgetMonthAgent.setMonthid(monthId);
						break;
					case 4:
						isNotBlank(data, "月度预算");
						if (!NumberUtil.isNumeric(data)) {
							throw new RuntimeException("金额格式填写有误！");
						}
						budgetMonthAgent.setTotal(new BigDecimal(data));
						break;
					case 5:
						if (type == 1) {
							isNotBlank(data, "月度预算活动说明");
						}
						budgetMonthAgent.setMonthbusiness(data);
						break;
					default:
				}
			}

			// 查询是否存在该月度动因
			BudgetMonthAgent existMonthAgent = this.budgetMonthAgentMapper.selectOne(new QueryWrapper<BudgetMonthAgent>()
					.eq("unitid", unitId)
					.eq("subjectid", subjectId)
					.eq("monthid", budgetMonthAgent.getMonthid())
					.eq("name", budgetMonthAgent.getName()));
			if (existMonthAgent == null) {
				throw new RuntimeException("【" + subjectName + "】预算科目中月度动因【" + budgetMonthAgent.getName() + "】不存在");
			}
			budgetMonthAgent.setId(existMonthAgent.getId());

			// 更新后的月度预算金额是否可行
			checkYearBudgetBalance(existMonthAgent, budgetMonthAgent.getTotal());

			// 添加成功记录
			successList.add(budgetMonthAgent);
		} catch (Exception e) {
			// 解决异常: Transaction rolled back because it has been marked as rollback-only
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			errorList.add(ExcelBean.transformBean(row, totalColumn, e.getMessage()));
		}
	}

	private void isNotBlank(String data, String message) {
		if (StringUtils.isBlank(data)) {
			throw new RuntimeException(message + "不能为空");
		}
	}

	/**
	 * 获取单位月度动因信息
	 */
	public MonthAgentMoneyInfo getUnitMonthAgentInfo(MonthAgentMoneyInfo bean) {
		ReimbursementValidateMoney result = this.budgetMonthAgentMapper.getUnitYearAgentInfo(bean);
		if (result != null) {
			bean.setAgentYearMoney(result.execMoney());
			bean.setAgentYearStartMoney(result.getTotal());
		}
		result = this.budgetMonthAgentMapper.getUnitMonthSubjectInfo(bean);
		if (result != null) {
			bean.setSubjectMonthStartMoney(result.getTotal());
			bean.setSubjectMonthMoney(result.execMoney());
			bean.setSubjectId(result.getSubjectId().toString());
		}
		return bean;
	}

	/**
	 * 根据预算单位及月份查询动因
	 */
	public PageResult<BudgetSubjectAgentVO> listSubjectMonthAgent(HashMap<String, Object> paramMap, Integer page, Integer rows) {
		Page<BudgetSubjectAgentVO> pageBean = new Page<>(page, rows);
		List<BudgetSubjectAgentVO> resultList = this.budgetMonthAgentMapper.listSubjectMonthAgentByMap(pageBean, paramMap);
		return PageResult.apply(pageBean.getTotal(), resultList);
	}
}
