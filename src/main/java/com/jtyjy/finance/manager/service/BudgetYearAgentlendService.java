package com.jtyjy.finance.manager.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.ecology.EcologyClient;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.ecology.EcologyWorkFlowValue;
import com.jtyjy.ecology.webservice.workflow.WorkflowInfo;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.dto.YearAgentLendDTO;
import com.jtyjy.finance.manager.dto.YearAgentLendDetailDTO;
import com.jtyjy.finance.manager.easyexcel.YearAgentLendExcelData;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.mapper.response.MonthAgentMoneyInfo;
import com.jtyjy.finance.manager.mapper.response.ReimbursementValidateMoney;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.utils.HttpUtil;
import com.jtyjy.finance.manager.vo.BudgetYearAgentLendVO;
import com.jtyjy.finance.manager.vo.YearAgentLendDetailVO;
import com.jtyjy.finance.manager.vo.YearAgentLendVO;
import com.jtyjy.finance.manager.ws.BudgetYearAgentLending;
import com.jtyjy.finance.manager.ws.BudgetYearAgentLendingDetail;
import com.klcwqy.easy.lock.impl.ZookeeperShareLock;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetYearAgentlendService extends DefaultBaseService<BudgetYearAgentlendMapper, BudgetYearAgentlend> {

	private final TabChangeLogMapper loggerMapper;
	private final BudgetUnitMapper budgetUnitMapper;
	private final BudgetYearPeriodMapper budgetYearPeriodMapper;
	private final BudgetSubjectMapper budgetSubjectMapper;
	private final BudgetYearAgentMapper budgetYearAgentMapper;
	private final BudgetYearAgentlendMapper budgetYearAgentlendMapper;
	private final CommonService commonService;

	private final BudgetMonthAgentMapper monthAgentMapper;

	private final OaService oaService;
	private final DistributedNumber distributedNumber;
	private final BudgetSysService budgetSysService;
	private final CuratorFramework curatorFramework;
	private final BudgetYearAgentlendDetailMapper yearAgentlendDetailMapper;

	@Value("${yearlend.workflowid}")
	private String flowid;
	@Value("${newjf.endYearLend.url}")
	private String newJfUrl;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}

	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_year_agentlend"));
	}

	/**
	 * 查询预算拆借列表（分页）
	 */
	public PageResult<BudgetYearAgentLendVO> listYearAgentLendPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {

		if ((Boolean) paramMap.get("isAcross")) {
			List<Long> lendMainIdList = budgetYearAgentlendMapper.listOldYearAgentLendPageAcrossDept(paramMap);
			Page<BudgetYearAgentlend> pageBean = new Page<>(page, rows);

			Page<BudgetYearAgentlend> budgetYearAgentlendPage = budgetYearAgentlendMapper.selectPage(pageBean, new LambdaQueryWrapper<BudgetYearAgentlend>().in(BudgetYearAgentlend::getId, lendMainIdList).orderByDesc(BudgetYearAgentlend::getCreatetime));
			return PageResult.apply(pageBean.getTotal(), budgetYearAgentlendPage.getRecords().stream().map(e -> {
				BudgetYearAgentLendVO vo = new BudgetYearAgentLendVO();
				vo.setId(e.getId());
				vo.setRequestStatus(e.getRequeststatus());
				vo.setOrderNumber(e.getOrdernumber());
				vo.setYearId(e.getYearid());
				vo.setYearPeriod(getPeriodInfo(e.getYearid()).getPeriod());
				vo.setCreatorName(e.getCreatorname());
				vo.setCreateTime(e.getCreatetime());
				vo.setTotal(e.getTotal());
				return vo;
			}).collect(Collectors.toList()));
		} else {
			Page<BudgetYearAgentLendVO> pageBean = new Page<>(page, rows);
			List<BudgetYearAgentLendVO> resultList = budgetYearAgentlendMapper.listOldYearAgentLendPageNotAcrossDept(pageBean, paramMap);
			return PageResult.apply(pageBean.getTotal(), resultList);
		}

	}

	/**
	 * 获取可拆借预算科目
	 */
	public List<BudgetSubject> listLendSubject(Long budgetUnitId) {
		return this.budgetSubjectMapper.listLendSubjects(budgetUnitId);
	}

	/**
	 * 获取可拆借预算动因
	 */
	public List<BudgetYearAgent> listLendAgent(Long budgetUnitId, Long budgetSubjectId) {
		List<BudgetYearAgent> budgetYearAgents = this.budgetYearAgentMapper.selectList(new QueryWrapper<BudgetYearAgent>()
//                .eq("agenttype", 0)
				.eq("unitid", budgetUnitId)
				.eq("subjectid", budgetSubjectId));
		budgetYearAgents.forEach(v -> v.setBalance(v.getTotal()
				.add(v.getAddmoney())
				.add(v.getLendinmoney())
				.subtract(v.getLendoutmoney())
				.subtract(v.getExecutemoney())));
		return budgetYearAgents;
	}

	/**
	 * 新增预算拆借
	 */
	public void saveYearAgentLend(YearAgentLendDTO bean, List<Map<String, Object>> list) throws Exception {

		String key = UserThreadLocal.getEmpNo();
		if (bean.getId() != null) {
			BudgetYearAgentlend agentLend = this.budgetYearAgentlendMapper.selectById(bean.getId());
			if (agentLend == null) {
				throw new RuntimeException("不存在该条预算拆借记录");
			} else if (agentLend.getRequeststatus() > 0) {
				throw new RuntimeException("【" + agentLend.getOrdernumber() + "】该单据已提交审核，无法修改");
			}
			key = bean.getId().toString();
		}

		ZookeeperShareLock lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/saveYearAgentLend/" + key, o -> {
			throw new RuntimeException("正在执行年度动因预算拆借,请勿重复点击");
		});
		try {
			lock.tryLock();

			validateBaseInfo(bean);

			Long lendId = saveOrUpdateAgentLend(bean);

			// 提交至OA系统
			if (bean.getIsSubmit()) {
				commitYearAgentLend(lendId, true, list);
			}

		} finally {
			lock.unLock();
		}
	}

	private void validateBaseInfo(YearAgentLendDTO bean) {

		if (!bean.getIsAcross()) {
			if (bean.getUnitId() == null) {
				throw new RuntimeException("预算单位不能为空。");
			}
			BudgetUnit inUnit = this.budgetUnitMapper.selectById(bean.getUnitId());
			if (inUnit.getRequeststatus() != 2) {
				throw new RuntimeException("预算单位【" + inUnit.getName() + "】年度预算未审核通过，不能拆借");
			}
		}

		bean.getDetails().forEach(detail -> {
			if (detail.getOutAgentId().equals(detail.getInAgentId())) {
				throw new RuntimeException("拆进和拆出年度动因不能相同");
			}
			BudgetYearAgent inAgent = this.budgetYearAgentMapper.selectById(detail.getInAgentId());
			if (inAgent == null) {
				throw new RuntimeException("拆进年度动因不存在。错误标识：【" + detail.getInAgentId() + "】");
			}
			BudgetYearAgent outAgent = this.budgetYearAgentMapper.selectById(detail.getOutAgentId());
			if (outAgent == null) {
				throw new RuntimeException("拆进或拆出年度动因不存在");
			}
			if (bean.getIsAcross()) {
				// 验证年度预算是否已经通过审核，审核未通过的不允许拆借
				BudgetUnit inUnit = this.budgetUnitMapper.selectById(inAgent.getUnitid());
				BudgetUnit outUnit = this.budgetUnitMapper.selectById(outAgent.getUnitid());
				if (inUnit.getRequeststatus() != 2) {
					throw new RuntimeException("预算单位【" + inUnit.getName() + "】年度预算未审核通过，不能拆借");
				}
				if (outUnit.getRequeststatus() != 2) {
					throw new RuntimeException("预算单位【" + outUnit.getName() + "】年度预算未审核通过，不能拆借");
				}
			}
		});
	}

	/**
	 * 新增或修改预算拆借记录
	 */
	private Long saveOrUpdateAgentLend(YearAgentLendDTO bean) {
		WbUser user = UserThreadLocal.get();
		Date currentDate = new Date();

		// 验证成功则插入数据
		BudgetYearAgentlend yearAgentLend;
		if (bean.getId() == null) {
			// 申请人id, 名字
			yearAgentLend = new BudgetYearAgentlend();
			yearAgentLend.setCreatorid(user.getUserName());
			yearAgentLend.setCreatorname(user.getDisplayName());
			yearAgentLend.setCreatetime(currentDate);
			// 新增 （单据号）
			yearAgentLend.setOrdernumber(this.distributedNumber.getYearAgentLendNum());
		} else {
			// 修改
			yearAgentLend = budgetYearAgentlendMapper.selectById(bean.getId());
		}

		if (!bean.getIsAcross()) {
			yearAgentLend.setInunitid(bean.getUnitId());
			yearAgentLend.setOutunitid(bean.getUnitId());
		}

		yearAgentLend.setIsCrossDept(bean.getIsAcross());
		yearAgentLend.setYearid(bean.getYearId());
		yearAgentLend.setTotal(bean.getDetails().stream().map(YearAgentLendDetailDTO::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
		yearAgentLend.setRequeststatus(0);
		yearAgentLend.setUpdatetime(currentDate);
		yearAgentLend.setHandleflag(false);
		yearAgentLend.setDeleteflag(false);
		yearAgentLend.setFileurl(bean.getFileUrl());
		yearAgentLend.setFileoriginname(bean.getFileOriginName());
		yearAgentLend.setOapassword(bean.getOaPassword());


		if (bean.getId() == null) {
			this.budgetYearAgentlendMapper.insert(yearAgentLend);
		} else {
			this.budgetYearAgentlendMapper.updateById(yearAgentLend);
		}

		List<BudgetYearAgentlendDetail> budgetYearAgentlendDetails = yearAgentlendDetailMapper.selectList(new LambdaQueryWrapper<BudgetYearAgentlendDetail>().eq(BudgetYearAgentlendDetail::getYearAgentLendId, yearAgentLend.getId()));
		List<Long> detailIds = bean.getDetails().stream().filter(e -> e.getId() != null).map(YearAgentLendDetailDTO::getId).collect(Collectors.toList());

		bean.getDetails().forEach(e -> {

			BudgetYearAgentlendDetail detail;
			if (e.getId() == null) {
				detail = new BudgetYearAgentlendDetail();
			} else {
				detail = yearAgentlendDetailMapper.selectById(e.getId());
			}

			detail.setYearAgentLendId(yearAgentLend.getId());
			detail.setInunitid(e.getInUnitId());
			detail.setOutunitid(e.getOutUnitId());
			detail.setInsubjectid(e.getInSubjectId());
			detail.setOutsubjectid(e.getOutSubjectId());
			detail.setInsubjectname(this.budgetSubjectMapper.selectById(e.getInSubjectId()).getName());
			detail.setOutsubjectname(this.budgetSubjectMapper.selectById(e.getOutSubjectId()).getName());
			detail.setRemark(e.getRemark());
			detail.setIsExemptFine(e.getIsExemptFine());
			detail.setExemptFineReason(e.getExemptFineReason());
			detail.setRequeststatus(0);
			detail.setTotal(e.getTotal());

			BudgetYearAgent outAgent = budgetYearAgentMapper.selectById(e.getOutAgentId());
			// 拆出信息
			detail.setOutyearagentid(outAgent.getId());
			detail.setOutname(outAgent.getName());
			detail.setOutagentmoney(outAgent.getTotal());
			detail.setOutagentaddmoney(outAgent.getAddmoney());
			detail.setOutagentlendoutmoney(outAgent.getLendoutmoney());
			detail.setOutagentlendinmoney(outAgent.getLendinmoney());
			detail.setOutagentexcutemoney(outAgent.getExecutemoney());

			BudgetYearAgent inAgent = budgetYearAgentMapper.selectById(e.getInAgentId());
			// 拆进信息
			detail.setInyearagentid(inAgent.getId());
			detail.setInname(inAgent.getName());
			detail.setInagentmoney(inAgent.getTotal());
			detail.setInagentaddmoney(inAgent.getAddmoney());
			detail.setInagentlendoutmoney(inAgent.getLendoutmoney());
			detail.setInagentlendinmoney(inAgent.getLendinmoney());
			detail.setInagentexcutemoney(inAgent.getExecutemoney());

			if (e.getId() == null) {
				yearAgentlendDetailMapper.insert(detail);
			} else {
				yearAgentlendDetailMapper.updateById(detail);
			}
		});

		List<BudgetYearAgentlendDetail> deletedDetailIds = budgetYearAgentlendDetails.stream().filter(e -> !detailIds.contains(e.getId())).collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(deletedDetailIds)) {
			yearAgentlendDetailMapper.deleteBatchIds(deletedDetailIds.stream().map(BudgetYearAgentlendDetail::getId).collect(Collectors.toList()));
		}

		BigDecimal total = bean.getDetails().stream().map(YearAgentLendDetailDTO::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		yearAgentLend.setTotal(total);
		this.budgetYearAgentlendMapper.updateById(yearAgentLend);
		return yearAgentLend.getId();
	}

	/**
	 * 拆借提交至OA系统
	 */
	public void commitYearAgentLend(Long lendId, Boolean existLock, List<Map<String, Object>> list) throws Exception {
		ZookeeperShareLock lock = null;
		try {
			if (!existLock) {
				lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/saveYearAgentLend/" + lendId, o -> {
					throw new RuntimeException("正在提交年度动因预算拆借,请勿重复点击");
				});
				lock.tryLock();
			}
			BudgetYearAgentlend yearAgentLend = this.budgetYearAgentlendMapper.selectById(lendId);
			if (yearAgentLend.getRequeststatus() != 0 && yearAgentLend.getRequeststatus() != -1) {
				throw new RuntimeException("提交失败，该流程已经提交或者审核");
			}

			List<BudgetYearAgentlendDetail> budgetYearAgentlendDetails = yearAgentlendDetailMapper.selectList(new LambdaQueryWrapper<BudgetYearAgentlendDetail>().eq(BudgetYearAgentlendDetail::getYearAgentLendId, yearAgentLend.getId()));

			validateMoneyBalance(budgetYearAgentlendDetails, lendId);

			String requestId = yearAgentLendOa(budgetYearAgentlendDetails, yearAgentLend, list);
			if (null == requestId || Integer.parseInt(requestId) < 0) {
				throw new RuntimeException("提交失败,OA系统未找到你的上级人员，请联系OA管理员");
			}
			yearAgentLend.setRequeststatus(1);
			yearAgentLend.setRequestid(requestId);
			this.budgetYearAgentlendMapper.updateById(yearAgentLend);

			budgetYearAgentlendDetails.forEach(e -> {
				e.setRequeststatus(1);
				yearAgentlendDetailMapper.updateById(e);
			});

		} finally {
			if (lock != null) {
				lock.unLock();
			}
		}
	}

	private void validateMoneyBalance(List<BudgetYearAgentlendDetail> budgetYearAgentlendDetails, Long lendId) {
		budgetYearAgentlendDetails.stream().collect(Collectors.groupingBy(BudgetYearAgentlendDetail::getOutyearagentid)).forEach((outAgentId, lendinList) -> {
			BudgetYearAgent outAgent = this.budgetYearAgentMapper.selectById(outAgentId);

			//老数据（审核中的）
			List<BudgetYearAgentlend> yearAgentLends = this.budgetYearAgentlendMapper.selectList(new LambdaQueryWrapper<BudgetYearAgentlend>()
					.eq(BudgetYearAgentlend::getRequeststatus, 1)
					.eq(BudgetYearAgentlend::getOutyearagentid, outAgent.getId())
					.ne(BudgetYearAgentlend::getId, lendId));

			//新数据(审核中的)
			List<BudgetYearAgentlendDetail> verifyingBudgetYearAgentlendDetails = this.yearAgentlendDetailMapper.selectList(new LambdaQueryWrapper<BudgetYearAgentlendDetail>().eq(BudgetYearAgentlendDetail::getRequeststatus, 1)
					.eq(BudgetYearAgentlendDetail::getOutyearagentid, outAgent.getId()).ne(BudgetYearAgentlendDetail::getId, lendId));

			MonthAgentMoneyInfo info = new MonthAgentMoneyInfo();
			info.setYearAgentId(outAgentId);
			info.setYearId(outAgent.getYearid());
			info.setUnitId(outAgent.getUnitid());
			ReimbursementValidateMoney moneyResult = this.monthAgentMapper.getUnitYearAgentInfoByYearAgentId(info);

			BigDecimal verifyingMoney = yearAgentLends.stream().map(BudgetYearAgentlend::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal verifyingMoney1 = verifyingBudgetYearAgentlendDetails.stream().map(BudgetYearAgentlendDetail::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal curMoney = lendinList.stream().map(BudgetYearAgentlendDetail::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

			if (moneyResult.execMoney().subtract(verifyingMoney).subtract(verifyingMoney1).compareTo(curMoney) < 0) {
				System.out.println(moneyResult.execMoney());
				System.out.println(verifyingMoney);
				System.out.println(verifyingMoney1);
				throw new RuntimeException("年度动因【" + outAgent.getName() + "】可拆出金额不足");
			}
		});
	}

	/**
	 * 年度追加OA流程
	 */
	public String yearAgentLendOa(List<BudgetYearAgentlendDetail> budgetYearAgentlendDetails, BudgetYearAgentlend yearAgentLend, List<Map<String, Object>> list) {


		String period = getPeriodInfo(yearAgentLend.getYearid()).getPeriod();
		// 第几次拆借
		Integer count = getYearLendTimes(yearAgentLend.getYearid()) + 1;


		String userIdDeptId = this.oaService.getOaUserId(yearAgentLend.getCreatorid(), list);
		String oaUserId = userIdDeptId.split(",")[0];
		String oaDeptId = userIdDeptId.split(",")[1];
		yearAgentLend.setOacreatorid(oaUserId);
		int code = -1;
		String fileUrl = yearAgentLend.getFileurl();
		if (StringUtils.isNotBlank(fileUrl)) {
			try {
				String oaPassword = yearAgentLend.getOapassword();
				URLConnection connection = new URL(fileUrl).openConnection();
				InputStream is = connection.getInputStream();
				String fileOriginName = yearAgentLend.getFileoriginname();
				code = this.oaService.createDoc(yearAgentLend.getCreatorid(), oaPassword, is, fileOriginName, fileUrl, "年度拆借流程附件");
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}
		if (code == 0) {
			throw new RuntimeException("系统错误!创建文档失败!");
		}
		BudgetYearAgentLending yearLending = new BudgetYearAgentLending();
		yearLending.setFj(code + "");
		yearLending.setSsbm(oaDeptId);
		yearLending.setSqr(oaUserId);
		yearLending.setYsjb(period);
		// 申请日期
		yearLending.setSqrq(Constants.FORMAT_10.format(yearAgentLend.getCreatetime()));
		yearLending.setCjcs(count);

		BigDecimal total = budgetYearAgentlendDetails.stream().map(BudgetYearAgentlendDetail::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		yearLending.setCjje(total);
		yearLending.setWfid(flowid);
		String username = yearAgentLend.getCreatorname();
		WorkflowInfo wi = new WorkflowInfo();
		wi.setCreatorId(oaUserId);
		wi.setRequestLevel("0");
		wi.setRequestName("年度预算拆借--" + username);

		List<BudgetYearAgentLendingDetail> workflowDetails = budgetYearAgentlendDetails.stream().map(e -> {
			BudgetYearAgentLendingDetail workflowDetail = new BudgetYearAgentLendingDetail();
			workflowDetail.setSjid(e.getId());
			workflowDetail.setCjysdw(getUnitInfo(e.getInunitid()).getName());
			workflowDetail.setCjkm(e.getInsubjectname());
			workflowDetail.setCjdy(e.getInname());

			BigDecimal inBalance = e.getInagentmoney().add(e.getInagentaddmoney()).subtract(e.getInagentlendoutmoney())
					.add(e.getInagentlendinmoney()).subtract(e.getInagentexcutemoney()).add(e.getTotal());

			BigDecimal outBalance = e.getOutagentmoney().add(e.getOutagentaddmoney()).subtract(e.getOutagentlendoutmoney())
					.add(e.getOutagentlendinmoney()).subtract(e.getOutagentexcutemoney()).subtract(e.getTotal());
			workflowDetail.setCjhndys(inBalance);
			workflowDetail.setCcyysdw(getUnitInfo(e.getOutunitid()).getName());
			workflowDetail.setCckm(e.getOutsubjectname());
			workflowDetail.setCcdy(e.getOutname());
			workflowDetail.setCjhndysu(outBalance);
			workflowDetail.setCjje(e.getTotal());
			workflowDetail.setCjyy(e.getRemark());
			workflowDetail.setSfsqmf(e.getIsExemptFine() ? "0" : "1");
			workflowDetail.setMflysm(e.getExemptFineReason());
			//workflowDetail.setWfid(flowid);
			return workflowDetail;
		}).collect(Collectors.toList());


//		if (data.get("inunitname").equals(data.get("outunitname"))) {
//			wi.setRequestName("年度预算拆借--" + username);
//		} else {
//			wi.setRequestName("年度预算拆借(跨部门)--" + username);
//		}
		return createBudgetYearAgentLending(wi, yearLending, yearAgentLend, workflowDetails);
	}

	public String createBudgetYearAgentLending(WorkflowInfo wi, BudgetYearAgentLending yearAgentLending, BudgetYearAgentlend yearAgentLend, List<BudgetYearAgentLendingDetail> workflowDetails) {
		Map<String, Object> main = (Map<String, Object>) JSON.toJSON(yearAgentLending);
		if (null != yearAgentLend.getRequestid()) {
			this.oaService.deleteRequest(yearAgentLend.getRequestid(), yearAgentLend.getOacreatorid());
		}
		List<Map<String, Object>> list = (List<Map<String, Object>>) JSON.toJSON(workflowDetails);
		return this.oaService.createWorkflow(wi, yearAgentLending.getWfid(), main, list);
	}

	/**
	 * 拆借金额不能大于“年度可用拆借余额”
	 * 控制逻辑中的 ---“年度可用拆借余额” = 动因年度预算 - 已申请拆借但未审批通过的-动因已执行数
	 */
	public void yearAgentAvailableBalance(BudgetYearAgent yearAgent, BigDecimal lendTotal) {
		BigDecimal availableBalance = yearAgent.getTotal()
				.add(yearAgent.getAddmoney())
				.add(yearAgent.getLendinmoney())
				.subtract(yearAgent.getLendoutmoney())
				.subtract(yearAgent.getExecutemoney());

		List<BudgetYearAgentlend> yearAgentLends = this.budgetYearAgentlendMapper.selectList(new QueryWrapper<BudgetYearAgentlend>()
				.eq("requeststatus", 1)
				.eq("outyearagentid", yearAgent.getId()));
		BigDecimal notPassBal = BigDecimal.ZERO;
		// 已申请拆借但未审批通过的
		for (BudgetYearAgentlend agentLend : yearAgentLends) {
			notPassBal = notPassBal.add(agentLend.getTotal());
		}

		BigDecimal balance = availableBalance.subtract(notPassBal);
		if (lendTotal.compareTo(balance) > 0) {
			throw new RuntimeException("拆借金额【" + lendTotal + "】大于拆出动因的年度可用拆借金额【" + balance.stripTrailingZeros().toPlainString() + "】,不允许拆借");
		}
	}

	/**
	 * 本届第几次拆借
	 */
	public Integer getYearLendTimes(Long yearId) {
		return this.budgetYearAgentlendMapper.selectCount(new QueryWrapper<BudgetYearAgentlend>()
				.eq("yearid", yearId)
				.gt("requeststatus", 0));
	}

	/**
	 * 届别信息
	 */
	public BudgetYearPeriod getPeriodInfo(Long yearId) {
		return this.budgetYearPeriodMapper.selectById(yearId);
	}

	/**
	 * 单位信息
	 */
	public BudgetUnit getUnitInfo(Long unitId) {
		return this.budgetUnitMapper.selectById(unitId);
	}

	/**
	 * 科目信息
	 */
	public BudgetSubject getSubjectInfo(Long subjectId) {
		return this.budgetSubjectMapper.selectById(subjectId);
	}

	/**
	 * 删除预算拆借
	 */
	public void deleteYearAgentLend(List<Long> ids) {
		for (Long id : ids) {
			BudgetYearAgentlend yearAgentLend = this.budgetYearAgentlendMapper.selectById(id);
			if (yearAgentLend != null) {
				if (yearAgentLend.getRequeststatus() > 0) {
					throw new RuntimeException("【" + yearAgentLend.getOrdernumber() + "】该单据已提交审核，不允许删除");
				}
				this.budgetYearAgentlendMapper.deleteById(yearAgentLend.getId());
				this.yearAgentlendDetailMapper.delete(new LambdaQueryWrapper<BudgetYearAgentlendDetail>().eq(BudgetYearAgentlendDetail::getYearAgentLendId, yearAgentLend.getId()));
			}
		}
	}

	/**
	 * 年度预算拆借导出
	 */
	public List<YearAgentLendExcelData> exportAgentYearLend(HashMap<String, Object> paramMap) {
		List<YearAgentLendExcelData> resultList = new ArrayList<>();


		if ((Boolean) paramMap.get("isAcross")) {
			List<Long> lendMainIdList = budgetYearAgentlendMapper.listOldYearAgentLendPageAcrossDept(paramMap);

			List<BudgetYearAgentlend> budgetYearAgentLends = budgetYearAgentlendMapper.selectList(new LambdaQueryWrapper<BudgetYearAgentlend>().in(BudgetYearAgentlend::getId, lendMainIdList).orderByDesc(BudgetYearAgentlend::getCreatetime));

			budgetYearAgentLends.forEach(v -> {

				List<BudgetYearAgentlendDetail> budgetYearAgentlendDetails = yearAgentlendDetailMapper.selectList(new LambdaQueryWrapper<BudgetYearAgentlendDetail>().eq(BudgetYearAgentlendDetail::getYearAgentLendId, v.getId()));
				if (CollectionUtils.isEmpty(budgetYearAgentlendDetails)) {
					YearAgentLendExcelData excelData = new YearAgentLendExcelData();
					excelData.setNum(resultList.size() + 1);
					excelData.setRequestStatus(Constants.getRequestStatus(v.getRequeststatus()));
					excelData.setOrderNumber(v.getOrdernumber());
					excelData.setYearPeriod(getPeriodInfo(v.getYearid()).getPeriod());
					excelData.setInBudgetUnitName(getUnitInfo(v.getInunitid()).getName());
					excelData.setOutBudgetUnitName(getUnitInfo(v.getOutunitid()).getName());
					excelData.setTotal(v.getTotal().setScale(2, BigDecimal.ROUND_HALF_UP));
					excelData.setInSubjectName(v.getInsubjectname());
					excelData.setInAgentName(v.getInname());
					excelData.setInYearTotal(v.getInagentmoney().setScale(2, BigDecimal.ROUND_HALF_UP));
					//excelData.setInYearBalance(v.getInagentmoney().add(v.getInagentaddmoney()).add(v.getInagentlendinmoney()).subtract(v.getInagentlendoutmoney()).subtract(v.getInagentexcutemoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
					excelData.setInYearBalance(v.getInagentexcutemoney().setScale(2, BigDecimal.ROUND_HALF_UP));
					excelData.setOutSubjectName(v.getOutsubjectname());
					excelData.setOutAgentName(v.getOutname());
					excelData.setOutYearTotal(v.getOutagentmoney());
					excelData.setOutYearBalance(v.getOutagentmoney().add(v.getOutagentaddmoney()).add(v.getOutagentlendinmoney()).subtract(v.getOutagentlendoutmoney()).subtract(v.getOutagentexcutemoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
					excelData.setRemark(v.getRemark());
					excelData.setCreatorName(v.getCreatorname());
					excelData.setCreateTime(Constants.FORMAT_10.format(v.getCreatetime()));
					excelData.setAuditTime(v.getAudittime() != null ? Constants.FORMAT_10.format(v.getAudittime()) : "");
					resultList.add(excelData);
				} else {

					budgetYearAgentlendDetails.forEach(detail -> {
						YearAgentLendExcelData excelData = new YearAgentLendExcelData();
						excelData.setNum(resultList.size() + 1);
						excelData.setRequestStatus(Constants.getRequestStatus(v.getRequeststatus()));
						excelData.setOrderNumber(v.getOrdernumber());
						excelData.setYearPeriod(getPeriodInfo(v.getYearid()).getPeriod());
						excelData.setInBudgetUnitName(getUnitInfo(detail.getInunitid()).getName());
						excelData.setOutBudgetUnitName(getUnitInfo(detail.getOutunitid()).getName());
						excelData.setTotal(detail.getTotal().setScale(2, BigDecimal.ROUND_HALF_UP));
						excelData.setInSubjectName(detail.getInsubjectname());
						excelData.setInAgentName(detail.getInname());
						excelData.setInYearTotal(detail.getInagentmoney().setScale(2, BigDecimal.ROUND_HALF_UP));
						//excelData.setInYearBalance(detail.getInagentmoney().add(detail.getInagentaddmoney()).add(detail.getInagentlendinmoney()).subtract(detail.getInagentlendoutmoney()).subtract(detail.getInagentexcutemoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
						excelData.setInYearBalance(detail.getInagentexcutemoney().setScale(2, BigDecimal.ROUND_HALF_UP));
						excelData.setOutSubjectName(detail.getOutsubjectname());
						excelData.setOutAgentName(detail.getOutname());
						excelData.setOutYearTotal(detail.getOutagentmoney());
						excelData.setOutYearBalance(detail.getOutagentmoney().add(detail.getOutagentaddmoney()).add(detail.getOutagentlendinmoney()).subtract(detail.getOutagentlendoutmoney()).subtract(detail.getOutagentexcutemoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
						excelData.setRemark(detail.getRemark());
						excelData.setCreatorName(v.getCreatorname());
						excelData.setCreateTime(Constants.FORMAT_10.format(v.getCreatetime()));
						excelData.setAuditTime(v.getAudittime() != null ? Constants.FORMAT_10.format(v.getAudittime()) : "");
						excelData.setIsExemptFine(detail.getIsExemptFine() ? "是" : "否");
						excelData.setExemptFineReason(detail.getExemptFineReason());
						excelData.setExemptFineResult(detail.getExemptFineResult());
						excelData.setFineReasonRemark(detail.getFineReasonRemark());
						resultList.add(excelData);
					});
				}
			});

		} else {
			List<BudgetYearAgentLendVO> lendVOList = budgetYearAgentlendMapper.listOldYearAgentLendPageNotAcrossDept(null, paramMap);
			List<BudgetYearAgentlend> budgetYearAgentLends = budgetYearAgentlendMapper.selectList(new LambdaQueryWrapper<BudgetYearAgentlend>().in(BudgetYearAgentlend::getId, lendVOList.stream().map(BudgetYearAgentLendVO::getId).collect(Collectors.toList())).orderByDesc(BudgetYearAgentlend::getCreatetime));
			budgetYearAgentLends.forEach(v -> {
				List<BudgetYearAgentlendDetail> budgetYearAgentlendDetails = yearAgentlendDetailMapper.selectList(new LambdaQueryWrapper<BudgetYearAgentlendDetail>().eq(BudgetYearAgentlendDetail::getYearAgentLendId, v.getId()));
				if (CollectionUtils.isEmpty(budgetYearAgentlendDetails)) {
					YearAgentLendExcelData excelData = new YearAgentLendExcelData();
					excelData.setNum(resultList.size() + 1);
					excelData.setRequestStatus(Constants.getRequestStatus(v.getRequeststatus()));
					excelData.setOrderNumber(v.getOrdernumber());
					excelData.setYearPeriod(getPeriodInfo(v.getYearid()).getPeriod());
					excelData.setInBudgetUnitName(getUnitInfo(v.getInunitid()).getName());
					excelData.setOutBudgetUnitName(getUnitInfo(v.getOutunitid()).getName());
					excelData.setTotal(v.getTotal().setScale(2, BigDecimal.ROUND_HALF_UP));
					excelData.setInSubjectName(v.getInsubjectname());
					excelData.setInAgentName(v.getInname());
					excelData.setInYearTotal(v.getInagentmoney().setScale(2, BigDecimal.ROUND_HALF_UP));
					excelData.setInYearBalance(v.getInagentmoney().add(v.getInagentaddmoney()).add(v.getInagentlendinmoney()).subtract(v.getInagentlendoutmoney()).subtract(v.getInagentexcutemoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
					excelData.setOutSubjectName(v.getOutsubjectname());
					excelData.setOutAgentName(v.getOutname());
					excelData.setOutYearTotal(v.getOutagentmoney());
					excelData.setOutYearBalance(v.getOutagentmoney().add(v.getOutagentaddmoney()).add(v.getOutagentlendinmoney()).subtract(v.getOutagentlendoutmoney()).subtract(v.getOutagentexcutemoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
					excelData.setRemark(v.getRemark());
					excelData.setCreatorName(v.getCreatorname());
					excelData.setCreateTime(Constants.FORMAT_10.format(v.getCreatetime()));
					excelData.setAuditTime(v.getAudittime() != null ? Constants.FORMAT_10.format(v.getAudittime()) : "");
					resultList.add(excelData);
				} else {

					budgetYearAgentlendDetails.forEach(detail -> {
						YearAgentLendExcelData excelData = new YearAgentLendExcelData();
						excelData.setNum(resultList.size() + 1);
						excelData.setRequestStatus(Constants.getRequestStatus(v.getRequeststatus()));
						excelData.setOrderNumber(v.getOrdernumber());
						excelData.setYearPeriod(getPeriodInfo(v.getYearid()).getPeriod());
						excelData.setInBudgetUnitName(getUnitInfo(detail.getInunitid()).getName());
						excelData.setOutBudgetUnitName(getUnitInfo(detail.getOutunitid()).getName());
						excelData.setTotal(detail.getTotal().setScale(2, BigDecimal.ROUND_HALF_UP));
						excelData.setInSubjectName(detail.getInsubjectname());
						excelData.setInAgentName(detail.getInname());
						excelData.setInYearTotal(detail.getInagentmoney().setScale(2, BigDecimal.ROUND_HALF_UP));
						excelData.setInYearBalance(detail.getInagentmoney().add(detail.getInagentaddmoney()).add(detail.getInagentlendinmoney()).subtract(detail.getInagentlendoutmoney()).subtract(detail.getInagentexcutemoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
						excelData.setOutSubjectName(detail.getOutsubjectname());
						excelData.setOutAgentName(detail.getOutname());
						excelData.setOutYearTotal(detail.getOutagentmoney());
						excelData.setOutYearBalance(detail.getOutagentmoney().add(detail.getOutagentaddmoney()).add(detail.getOutagentlendinmoney()).subtract(detail.getOutagentlendoutmoney()).subtract(detail.getOutagentexcutemoney()).setScale(2, BigDecimal.ROUND_HALF_UP));
						excelData.setRemark(detail.getRemark());
						excelData.setCreatorName(v.getCreatorname());
						excelData.setCreateTime(Constants.FORMAT_10.format(v.getCreatetime()));
						excelData.setAuditTime(v.getAudittime() != null ? Constants.FORMAT_10.format(v.getAudittime()) : "");
						excelData.setIsExemptFine(detail.getIsExemptFine() ? "是" : "否");
						excelData.setExemptFineReason(detail.getExemptFineReason());
						excelData.setExemptFineResult(detail.getExemptFineResult());
						excelData.setFineReasonRemark(detail.getFineReasonRemark());
						resultList.add(excelData);
					});
				}
			});
		}

		return resultList;
	}

	// ----------------------------------------------------------------------------------------------------

	/**
	 * 年度动因拆借审核通过
	 */
	public void endYearAgentLend(EcologyParams params) throws Exception {
		String requestId = params.getRequestid();
		ZookeeperShareLock lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/endYearAgentLend/" + requestId, o -> {
			throw new RuntimeException("正在执行年度动因预算拆借审核,请勿重复点击");
		});
		try {
			lock.tryLock();
			BudgetYearAgentlend yearAgentLend = this.budgetYearAgentlendMapper.selectOne(new QueryWrapper<BudgetYearAgentlend>()
					.eq("requestid", requestId));
			if (yearAgentLend == null) {
				throw new RuntimeException("不存在该年度动因拆借流程");
			} else if (yearAgentLend.getRequeststatus() == 2) {
				throw new RuntimeException("该年度动因拆借流程已审核通过");
			}

			List<BudgetYearAgentlendDetail> budgetYearAgentlendDetails = yearAgentlendDetailMapper.selectList(new LambdaQueryWrapper<BudgetYearAgentlendDetail>().eq(BudgetYearAgentlendDetail::getYearAgentLendId, yearAgentLend.getId()));
			if (CollectionUtils.isEmpty(budgetYearAgentlendDetails)) {
				//老的数据归档
				// 更新拆出动因记录
				BudgetYearAgent outAgent = this.budgetYearAgentMapper.selectById(yearAgentLend.getOutyearagentid());
				if (outAgent == null) {
					throw new RuntimeException("拆出动因不存在或已删除");
				}
				BudgetYearAgent updateOutAgent = new BudgetYearAgent();
				updateOutAgent.setId(outAgent.getId());
				updateOutAgent.setLendoutmoney(outAgent.getLendoutmoney().add(yearAgentLend.getTotal()));
				this.budgetYearAgentMapper.updateById(updateOutAgent);

				// 更新拆进动因记录
				BudgetYearAgentlend updateAgentLend = new BudgetYearAgentlend();
				if (yearAgentLend.getInyearagentid() == null) {
					Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
							.eq("unitId", yearAgentLend.getInunitid())
							.eq("subjectId", yearAgentLend.getInsubjectid())
							.eq("name", yearAgentLend.getInname()));
					if (duplicationCount > 0) {
						throw new RuntimeException("拆进动因名称【" + yearAgentLend.getInname() + "】已经存在。");
					}

					BudgetYearAgent inAgent = new BudgetYearAgent();
					inAgent.setYearid(yearAgentLend.getYearid());
					inAgent.setUnitid(yearAgentLend.getInunitid());
					inAgent.setTotal(BigDecimal.ZERO);
					inAgent.setPretotal(BigDecimal.ZERO);
					inAgent.setSubjectid(yearAgentLend.getInsubjectid());
					inAgent.setAgenttype(1);
					inAgent.setName(yearAgentLend.getInname());
					inAgent.setComputingprocess("");
					inAgent.setRemark("年度拆借追加的动因");
					inAgent.setElasticflag(false);
					inAgent.setAddmoney(BigDecimal.ZERO);
					inAgent.setLendoutmoney(BigDecimal.ZERO);
					inAgent.setLendinmoney(yearAgentLend.getTotal());
					inAgent.setExecutemoney(BigDecimal.ZERO);
					inAgent.setCreatetime(new Date());
					this.budgetYearAgentMapper.insert(inAgent);

					// 修改拆进动因Id
					updateAgentLend.setInyearagentid(inAgent.getId());
				} else if (yearAgentLend.getInyearagentid() > 0) {
					BudgetYearAgent inAgent = this.budgetYearAgentMapper.selectById(yearAgentLend.getInyearagentid());
					if (inAgent == null) {
						throw new RuntimeException("拆进动因不存在或已删除");
					}

					BudgetYearAgent updateInAgent = new BudgetYearAgent();
					updateInAgent.setId(inAgent.getId());
					updateInAgent.setLendinmoney(inAgent.getLendinmoney().add(yearAgentLend.getTotal()));
					this.budgetYearAgentMapper.updateById(updateInAgent);
				}

				// 更新拆借记录
				updateAgentLend.setId(yearAgentLend.getId());
				updateAgentLend.setRequeststatus(2);
				updateAgentLend.setHandleflag(true);
				updateAgentLend.setAudittime(new Date());
				this.budgetYearAgentlendMapper.updateById(updateAgentLend);

				yearLend(yearAgentLend);
			} else {

				yearAgentLend.setRequeststatus(2);
				yearAgentLend.setHandleflag(true);
				yearAgentLend.setAudittime(new Date());
				this.budgetYearAgentlendMapper.updateById(yearAgentLend);

				Map<String, String> map = new HashMap<>();
				//新数据，多科目多动因拆借。
				EcologyWorkFlowValue workflowValue = EcologyClient.getWorkflowValue(params);
				Map<String, List<Map<String, String>>> detailtablevalues = workflowValue.getDetailtablevalues();
				detailtablevalues.values().forEach(list -> {
					list.forEach(e -> {
						String id = e.get("sjid");
						String mfjg = e.get("mfjg1");
						String mflysmi = e.get("mflysmi");
						map.put(id + "-1", mfjg);
						map.put(id + "-2", mflysmi);
					});
				});

				budgetYearAgentlendDetails.forEach(detail -> {
					detail.setRequeststatus(2);
					detail.setAudittime(new Date());
					String mfjg = map.get(detail.getId() + "-1");

					detail.setExemptFineResult("0".equals(mfjg) ? "罚款" : "免罚");
					detail.setFineReasonRemark(map.get(detail.getId() + "-2"));
					this.yearAgentlendDetailMapper.updateById(detail);


					BudgetYearAgent outAgent = this.budgetYearAgentMapper.selectById(detail.getOutyearagentid());
					if (outAgent == null) {
						throw new RuntimeException("拆出动因不存在或已删除");
					}
					outAgent.setLendoutmoney(outAgent.getLendoutmoney().add(detail.getTotal()));
					this.budgetYearAgentMapper.updateById(outAgent);

					BudgetYearAgent inAgent = this.budgetYearAgentMapper.selectById(detail.getInyearagentid());
					if (inAgent == null) {
						throw new RuntimeException("拆进动因不存在或已删除");
					}
					inAgent.setLendinmoney(inAgent.getLendinmoney().add(detail.getTotal()));
					this.budgetYearAgentMapper.updateById(inAgent);


					this.budgetSysService.doSyncBudgetSubjectYearAddMoney(yearAgentLend.getYearid(), detail.getInunitid(), detail.getInsubjectid(), detail.getTotal(), 3);

					// 拆出  同步年度预算科目执行数
					this.budgetSysService.doSyncBudgetSubjectYearAddMoney(yearAgentLend.getYearid(), detail.getOutunitid(), detail.getOutsubjectid(), detail.getTotal(), 4);

				});


				//罚款
				budgetYearAgentlendDetails.stream().filter(e -> StringUtils.isNotBlank(e.getExemptFineResult()) && "罚款".equals(e.getExemptFineResult()))
						.collect(Collectors.groupingBy(BudgetYearAgentlendDetail::getInunitid)).forEach((inUnitId, list) -> {
					BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(inUnitId);
					String budgetResponsibilities = budgetUnit.getBudgetResponsibilities();
					if (StringUtils.isNotBlank(budgetResponsibilities) && list.size() > 0) {
						commonService.createBudgetFine(2, list.size(), budgetResponsibilities.split(",")[0],yearAgentLend.getCreatorid());
					}
				});

			}

            /*
             增加积分
             */
			try {
				HttpUtil.doGet(newJfUrl + "?empNo=" + yearAgentLend.getCreatorid() + "&processNo=" + yearAgentLend.getOrdernumber());
			} catch (Exception ignored) {
			}
		} finally {
			lock.unLock();
		}
	}

	public void yearLend(BudgetYearAgentlend yearAgentLend) {
		if (!yearAgentLend.getInsubjectid().equals(yearAgentLend.getOutsubjectid())) {
			// 拆进  同步年度预算科目执行数
			this.budgetSysService.doSyncBudgetSubjectYearAddMoney(yearAgentLend.getYearid(), yearAgentLend.getInunitid(), yearAgentLend.getInsubjectid(), yearAgentLend.getTotal(), 3);

			// 拆出  同步年度预算科目执行数
			this.budgetSysService.doSyncBudgetSubjectYearAddMoney(yearAgentLend.getYearid(), yearAgentLend.getOutunitid(), yearAgentLend.getOutsubjectid(), yearAgentLend.getTotal(), 4);
		}
	}

	/**
	 * 年度动因拆借退回
	 */
	public void rejectYearAgentLend(EcologyParams params) {
		String requestId = params.getRequestid();

		BudgetYearAgentlend yearAgentLend = this.budgetYearAgentlendMapper.selectOne(new QueryWrapper<BudgetYearAgentlend>()
				.eq("requestid", requestId));
		if (yearAgentLend == null) {
			throw new RuntimeException("不存在该年度动因拆借流程");
		} else if (yearAgentLend.getRequeststatus() == 2) {
			throw new RuntimeException("该年度动因拆借流程已审核通过。退回失败");
		}

		yearAgentLend.setRequeststatus(-1);
		yearAgentLend.setHandleflag(true);
		yearAgentLend.setUpdatetime(new Date());
		this.budgetYearAgentlendMapper.updateById(yearAgentLend);


		EcologyWorkFlowValue workflowValue = EcologyClient.getWorkflowValue(params);
		Map<String, List<Map<String, String>>> detailtablevalues = workflowValue.getDetailtablevalues();
		detailtablevalues.values().forEach(list -> {
			list.forEach(e -> {
				String id = e.get("sjid");
				String mfjg = e.get("mfjg1");
				String mflysmi = e.get("mflysmi");

				BudgetYearAgentlendDetail budgetYearAgentlendDetail = yearAgentlendDetailMapper.selectById(id);
				budgetYearAgentlendDetail.setRequeststatus(-1);
				budgetYearAgentlendDetail.setExemptFineResult("0".equals(mfjg) ? "罚款" : "免罚");
				budgetYearAgentlendDetail.setFineReasonRemark(mflysmi);
				yearAgentlendDetailMapper.updateById(budgetYearAgentlendDetail);
			});
		});
	}

	public YearAgentLendVO getYearLendDetail(Long id) {
		YearAgentLendVO vo = new YearAgentLendVO();
		BudgetYearAgentlend budgetYearAgentlend = budgetYearAgentlendMapper.selectById(id);
		vo.setId(budgetYearAgentlend.getId());
		vo.setYearId(budgetYearAgentlend.getYearid());
		vo.setYearName(getPeriodInfo(budgetYearAgentlend.getYearid()).getPeriod());
		vo.setFileUrl(budgetYearAgentlend.getFileurl());
		vo.setFileOriginName(budgetYearAgentlend.getFileoriginname());
		vo.setOaPassword(budgetYearAgentlend.getOapassword());
		List<BudgetYearAgentlendDetail> budgetYearAgentlendDetails = yearAgentlendDetailMapper.selectList(new LambdaQueryWrapper<BudgetYearAgentlendDetail>().eq(BudgetYearAgentlendDetail::getYearAgentLendId, id));

		List<YearAgentLendDetailVO> details = new ArrayList<>();
		if (CollectionUtils.isEmpty(budgetYearAgentlendDetails)) {
			//说明是老数据
			YearAgentLendDetailVO detail = new YearAgentLendDetailVO();
			if (!budgetYearAgentlend.getInunitid().equals(budgetYearAgentlend.getOutunitid())) {
				//跨部门
				detail.setInUnitId(budgetYearAgentlend.getInunitid());
				detail.setOutUnitId(budgetYearAgentlend.getOutunitid());
				detail.setInUnitName(getUnitInfo(budgetYearAgentlend.getInunitid()).getName());
				detail.setOutUnitName(getUnitInfo(budgetYearAgentlend.getOutunitid()).getName());
			} else {
				vo.setUnitId(budgetYearAgentlend.getInunitid());
				vo.setUnitName(getUnitInfo(budgetYearAgentlend.getInunitid()).getName());
				detail.setInUnitId(budgetYearAgentlend.getInunitid());
				detail.setOutUnitId(detail.getInUnitId());
				detail.setInUnitName(getUnitInfo(budgetYearAgentlend.getInunitid()).getName());
				detail.setOutUnitName(detail.getInUnitName());
			}
			detail.setInAgentMoney(budgetYearAgentlend.getInagentmoney());
			detail.setInAgentExecuteMoney(budgetYearAgentlend.getInagentexcutemoney());
			detail.setOutAgentMoney(budgetYearAgentlend.getOutagentmoney());
			detail.setOutAgentBalance(budgetYearAgentlend.getOutagentmoney().subtract(budgetYearAgentlend.getOutagentexcutemoney()).subtract(budgetYearAgentlend.getOutagentlendoutmoney()).add(budgetYearAgentlend.getOutagentaddmoney()).add(budgetYearAgentlend.getOutagentlendinmoney()));
			detail.setInSubjectId(budgetYearAgentlend.getInsubjectid());
			detail.setInSubjectName(budgetYearAgentlend.getInsubjectname());
			detail.setOutSubjectId(budgetYearAgentlend.getOutsubjectid());
			detail.setOutSubjectName(budgetYearAgentlend.getOutsubjectname());
			detail.setInAgentId(budgetYearAgentlend.getInyearagentid());
			detail.setInAgentName(budgetYearAgentlend.getInname());
			detail.setRemark(budgetYearAgentlend.getRemark());
			detail.setOutAgentId(budgetYearAgentlend.getOutyearagentid());
			detail.setOutAgentName(budgetYearAgentlend.getOutname());
			detail.setTotal(budgetYearAgentlend.getTotal());
			details.add(detail);
		} else {

			long count = budgetYearAgentlendDetails.stream().filter(e -> !e.getInunitid().equals(e.getOutunitid())).count();
			int size = budgetYearAgentlendDetails.stream().collect(Collectors.groupingBy(BudgetYearAgentlendDetail::getInunitid)).size();
			int size1 = budgetYearAgentlendDetails.stream().collect(Collectors.groupingBy(BudgetYearAgentlendDetail::getOutunitid)).size();

			if (count == 0 && size == 1 && size1 == 1) {
				//非跨部门
				vo.setUnitId(budgetYearAgentlendDetails.get(0).getInunitid());
				vo.setUnitName(getUnitInfo(vo.getUnitId()).getName());
			}

			budgetYearAgentlendDetails.forEach(lendDetail -> {
				YearAgentLendDetailVO detail = new YearAgentLendDetailVO();
				detail.setId(lendDetail.getId());
				detail.setInUnitId(lendDetail.getInunitid());
				detail.setInUnitName(getUnitInfo(detail.getInUnitId()).getName());
				detail.setOutUnitId(lendDetail.getOutunitid());
				detail.setOutUnitName(getUnitInfo(detail.getOutUnitId()).getName());
				detail.setInSubjectId(lendDetail.getInsubjectid());
				detail.setInSubjectName(lendDetail.getInsubjectname());
				detail.setOutSubjectName(lendDetail.getOutsubjectname());
				detail.setOutSubjectId(lendDetail.getOutsubjectid());
				detail.setInAgentId(lendDetail.getInyearagentid());
				detail.setInAgentName(lendDetail.getInname());
				detail.setRemark(lendDetail.getRemark());
				detail.setOutAgentId(lendDetail.getOutyearagentid());
				detail.setOutAgentName(lendDetail.getOutname());
				detail.setTotal(lendDetail.getTotal());
				detail.setInAgentMoney(lendDetail.getInagentmoney());
				detail.setInAgentExecuteMoney(lendDetail.getInagentexcutemoney());
				detail.setOutAgentMoney(lendDetail.getOutagentmoney());
				detail.setOutAgentBalance(lendDetail.getOutagentmoney().subtract(lendDetail.getOutagentexcutemoney()).subtract(lendDetail.getOutagentlendoutmoney()).add(lendDetail.getOutagentaddmoney()).add(lendDetail.getOutagentlendinmoney()));
				detail.setIsExemptFine(lendDetail.getIsExemptFine());
				detail.setExemptFineReason(lendDetail.getExemptFineReason());
				detail.setExemptFineResult(lendDetail.getExemptFineResult());
				detail.setFineReasonRemark(lendDetail.getFineReasonRemark());
				details.add(detail);
			});
		}
		vo.setDetails(details);
		return vo;
	}
}
