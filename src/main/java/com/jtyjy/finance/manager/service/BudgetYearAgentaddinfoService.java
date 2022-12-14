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
import com.jtyjy.finance.manager.dto.YearAgentAddInfoDTO;
import com.jtyjy.finance.manager.easyexcel.YearAgentAddInfoExcelData;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.vo.BudgetYearAddInfoVO;
import com.jtyjy.finance.manager.ws.WSBudgetYearAgentAdd;
import com.jtyjy.finance.manager.ws.WSBudgetYearAgentAddDetail;
import com.klcwqy.easy.lock.impl.ZookeeperShareLock;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetYearAgentaddinfoService extends DefaultBaseService<BudgetYearAgentaddinfoMapper, BudgetYearAgentaddinfo> {

	private final TabChangeLogMapper loggerMapper;
	private final BudgetYearAgentaddMapper budgetYearAgentaddMapper;
	private final BudgetYearAgentaddinfoMapper budgetYearAgentaddinfoMapper;
	private final BudgetYearSubjectMapper budgetYearSubjectMapper;
	private final BudgetUnitMapper budgetUnitMapper;
	private final BudgetYearAgentMapper budgetYearAgentMapper;
	private final BudgetYearPeriodMapper budgetYearPeriodMapper;
	private final BudgetSubjectMapper budgetSubjectMapper;
	private final BudgetMonthPeriodMapper budgetMonthPeriodMapper;
	private final BudgetMonthEndUnitMapper budgetMonthEndUnitMapper;
	private final BudgetMonthAgentMapper budgetMonthAgentMapper;

	private final OaService oaService;
	private final DistributedNumber distributedNumber;
	private final BudgetYearAgentaddService budgetYearAgentaddService;
	private final CuratorFramework curatorFramework;
	private final BudgetSysService budgetSysService;
	private final CommonService commonService;

	@Value("${yearadd.workflowid}")
	private String flowid;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}

	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_year_agentaddinfo"));
	}

	// ???????????? ----------------------------------------------------------------------------------------------------

	/**
	 * ??????????????????????????????
	 */
	public PageResult<BudgetYearAddInfoVO> yearAgentAddInfoPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {
		Page<BudgetYearAddInfoVO> pageBean = new Page<>(page, rows);
		List<BudgetYearAddInfoVO> resultList = this.budgetYearAgentaddinfoMapper.listYearAgentAddInfoPage(pageBean, paramMap);
		return PageResult.apply(pageBean.getTotal(), resultList);
	}

	/**
	 * ???????????????????????????
	 */
	public List<BudgetSubject> listCanAddSubjects(Long budgetUnitId) {
		BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
		if (budgetUnit == null) {
			throw new RuntimeException("????????????????????????");
		} else if (budgetUnit.getRequeststatus() != 2) {
			throw new RuntimeException("???????????????" + budgetUnit.getName() + "??????????????????????????????");
		}
		return this.budgetSubjectMapper.listCanAddSubjects(budgetUnitId);
	}

	/**
	 * ???????????????????????????
	 */
	public List<BudgetYearAgent> listCanAddAgents(Long budgetUnitId, Long budgetSubjectId) {
		return this.budgetYearAgentaddMapper.listCanAddAgents(budgetUnitId, budgetSubjectId);
	}

	/**
	 * ?????????????????????????????????
	 */
	public BudgetYearAgentadd getYearAgentInfo(Long yearAgentId) {
		BudgetYearAgent yearAgent = this.budgetYearAgentMapper.selectById(yearAgentId);
		if (yearAgent != null) {
			yearAgent.setBalance(yearAgent.getTotal()
					.add(yearAgent.getAddmoney())
					.add(yearAgent.getLendinmoney())
					.subtract(yearAgent.getLendoutmoney())
					.subtract(yearAgent.getExecutemoney()));

			BudgetYearAgentadd yearAgentAdd = new BudgetYearAgentadd();
			yearAgentAdd.setYearagentid(yearAgent.getId());
			yearAgentAdd.setSubjectid(yearAgent.getSubjectid());
			yearAgentAdd.setName(yearAgent.getName());
			yearAgentAdd.setAgentmoney(yearAgent.getTotal());
			yearAgentAdd.setPreYearBalance(yearAgent.getBalance());
			yearAgentAdd.setYearBalance(yearAgent.getBalance());
			return yearAgentAdd;
		}
		return null;
	}

	/**
	 * ????????????????????????????????????
	 */
	public List<BudgetYearAgentadd> listAddAgentByInfoId(Long infoId) {
		List<BudgetYearAgentadd> agentAdds = this.budgetYearAgentaddMapper.selectList(new QueryWrapper<BudgetYearAgentadd>().eq("infoid", infoId));
		agentAdds.forEach(v -> {
			v.setPreYearBalance(v.getAgentmoney()
					.add(v.getAgentaddmoney())
					.add(v.getAgentlendinmoney())
					.subtract(v.getAgentlendoutmoney())
					.subtract(v.getAgentexcutemoney()));
//            v.setYearBalance(v.getPreYearBalance().add(v.getTotal()));
			v.setYearBalance(v.getPreYearBalance());
			v.setShowExemptResult(v.getExemptResult() == null ? "" : (0 == v.getExemptResult() ? "??????" : "??????"));
			v.setSubjectName(budgetSubjectMapper.selectById(v.getSubjectid()).getName());
		});
		return agentAdds;
	}

	/**
	 * ??????????????????
	 */
	public void yearAgentAddMoney(YearAgentAddInfoDTO bean, List<Map<String, Object>> list) throws Exception {

		String key = UserThreadLocal.getEmpNo();
		if (bean.getId() != null) {
			key = bean.getId().toString();
		}

		ZookeeperShareLock lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/yearAgentAddMoney/" + key, o -> {
			throw new RuntimeException("????????????????????????????????????,??????????????????");
		});
		try {
			lock.tryLock();

			if (bean.getId() != null) {
				BudgetYearAgentaddinfo oldAgentAddInfo = this.budgetYearAgentaddinfoMapper.selectById(bean.getId());
				if (oldAgentAddInfo == null) {
					throw new RuntimeException("??????Id??????");
				} else if (oldAgentAddInfo.getRequeststatus() > 0) {
					throw new RuntimeException("???????????????????????????");
				}
			}

			List<BudgetSubject> subjectList = this.listCanAddSubjects(bean.getBudgetUnitId());
			List<Long> subjectIds = subjectList.stream().map(BudgetSubject::getId).collect(Collectors.toList());

			// ??????????????????????????????
			BudgetYearAgentaddinfo info = createAddInfo(bean);

			// ????????????
			int month = LocalDateTime.now().getMonthValue();
			BudgetMonthPeriod monthPeriod = this.budgetMonthPeriodMapper.selectOne(new QueryWrapper<BudgetMonthPeriod>().eq("code", month));

			// ??????????????????????????????
			saveOrUpdateYearAgentAdd(bean.getAddList(), bean.getUpdateList(), info, subjectIds, monthPeriod);
			// ??????????????????????????????
			if (bean.getDeleteList() != null && !bean.getDeleteList().isEmpty()) {
				this.budgetYearAgentaddMapper.deleteBatchIds(bean.getDeleteList());
			}

			// ????????????????????????
			BigDecimal total = BigDecimal.ZERO;
			List<BudgetYearAgentadd> yearAgentAddList = this.budgetYearAgentaddMapper.selectList(new QueryWrapper<BudgetYearAgentadd>()
					.eq("infoid", info.getId()));
			for (BudgetYearAgentadd agentAdd : yearAgentAddList) {
				total = total.add(agentAdd.getTotal());
			}
			info.setTotal(total);
			this.budgetYearAgentaddinfoMapper.updateById(info);

			// ???????????????OA??????
			if (bean.getIsSubmit()) {
				try {
					submitVerify(info.getId(), true, list);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());
				}
			}
		} finally {
			lock.unLock();
		}

	}

	/**
	 * ???????????????????????????????????????
	 */
	private void saveOrUpdateYearAgentAdd(List<BudgetYearAgentadd> addList, List<BudgetYearAgentadd> updateList, BudgetYearAgentaddinfo info, List<Long> subjectIds, BudgetMonthPeriod monthPeriod) {
		List<BudgetYearAgentadd> list = new ArrayList<>();
		if (addList != null && !addList.isEmpty()) {
			list.addAll(addList);
		}
		if (updateList != null && !updateList.isEmpty()) {
			list.addAll(updateList);
		}

		Long month = Long.valueOf(monthPeriod.getCode());
		for (BudgetYearAgentadd yearAgentAdd : list) {
			yearAgentAdd.setMonthagentid(null);

			if (!subjectIds.contains(yearAgentAdd.getSubjectid())) {
				throw new RuntimeException("?????????????????????????????????????????????, ?????????????????????");
			}

			if (yearAgentAdd.getType() == 1) {
				yearAgentAdd.setYearagentid(null);
				// ?????????????????????
				BudgetSubject budgetSubject = this.budgetSubjectMapper.selectById(yearAgentAdd.getSubjectid());
				if (budgetSubject == null) {
					throw new RuntimeException("????????????????????????");
				} else if (budgetSubject.getJointproductflag() != null && budgetSubject.getJointproductflag()) {
					throw new RuntimeException("???????????????" + budgetSubject.getName() + "??????????????????????????????????????????????????????");
				}
			}

			// ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
			if (yearAgentAdd.getType() == 0) {
				// ??????
				BudgetYearAgent yearAgent = this.budgetYearAgentMapper.selectById(yearAgentAdd.getYearagentid());
				if (yearAgent == null) {
					throw new RuntimeException("????????????????????????" + yearAgentAdd.getName() + "????????????");
				} else if (!yearAgent.getSubjectid().equals(yearAgentAdd.getSubjectid())) {
					throw new RuntimeException("????????????????????????" + yearAgentAdd.getName() + "????????????????????????????????????");
				}
				yearAgentAdd.setAgentmoney(yearAgent.getTotal());
				yearAgentAdd.setAgentaddmoney(yearAgent.getAddmoney());
				yearAgentAdd.setAgentlendoutmoney(yearAgent.getLendoutmoney());
				yearAgentAdd.setAgentlendinmoney(yearAgent.getLendinmoney());
				yearAgentAdd.setAgentexcutemoney(yearAgent.getExecutemoney());
			} else {
				Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
						.eq("unitId", info.getUnitid())
						.eq("subjectId", yearAgentAdd.getSubjectid())
						.eq("name", yearAgentAdd.getName()));
				if (duplicationCount > 0) {
					throw new RuntimeException("????????????????????????????????????" + yearAgentAdd.getName() + "??????????????????");
				}
			}

			Date currentDate = new Date();
			// ??????Id
			yearAgentAdd.setYearid(info.getYearid());
			// ????????????Id
			yearAgentAdd.setUnitid(info.getUnitid());
			// ????????????Id
			yearAgentAdd.setSubjectid(yearAgentAdd.getSubjectid());
			// ??????????????????Id
			yearAgentAdd.setInfoid(info.getId());
			// ?????????
			yearAgentAdd.setCurmonthid(month);
			// ????????????
			yearAgentAdd.setUpdatetime(currentDate);
			if (yearAgentAdd.getId() == null) {
				// ????????????
				yearAgentAdd.setCreatetime(currentDate);
			}

			try {
				BigDecimal m = (BigDecimal) BudgetYearAgentadd.class.getMethod("getM" + month).invoke(yearAgentAdd);
				yearAgentAdd.setCurmonthmoney(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (yearAgentAdd.getCurmonthmoney().compareTo(BigDecimal.ZERO) > 0) {
				BudgetMonthEndUnit budgetMonthEndUnit = this.budgetMonthEndUnitMapper.selectOne(new QueryWrapper<BudgetMonthEndUnit>()
						.eq("unitId", info.getUnitid())
						.eq("monthId", month)
						.eq("requeststatus", 2));
				if (budgetMonthEndUnit == null) {
					throw new RuntimeException("???" + month + "???????????????????????????????????????");
				} else if (budgetMonthEndUnit.getMonthendflag()) {
					throw new RuntimeException("???" + month + "???????????????????????????!");
				}
				Integer count = this.budgetMonthAgentMapper.selectCount(new QueryWrapper<BudgetMonthAgent>()
						.eq("yearId", info.getYearid())
						.eq("unitId", info.getUnitid())
						.eq("monthId", monthPeriod.getId()));
				if (count <= 0) {
					throw new RuntimeException(month + "??????????????????????????????!");
				}
			}
			this.budgetYearAgentaddService.saveOrUpdate(yearAgentAdd);
		}
	}

	/**
	 * ?????????????????????????????????
	 */
	private BudgetYearAgentaddinfo createAddInfo(YearAgentAddInfoDTO bean) {
//        BudgetYearSubject budgetYearSubject = this.budgetYearSubjectMapper.selectOne(new QueryWrapper<BudgetYearSubject>()
//                .eq("yearId", bean.getYearId())
//                .eq("unitId", bean.getBudgetUnitId())
//                .eq("subjectId", bean.getBudgetSubjectId()));
//        if (budgetYearSubject == null) {
//            throw new RuntimeException("??????????????????????????????????????????!");
//        }

		BudgetYearAgentaddinfo updateAgentAddInfo = new BudgetYearAgentaddinfo();
		// ????????????
		updateAgentAddInfo.setRequeststatus(0);
		// ??????
		updateAgentAddInfo.setYearid(bean.getYearId());
		// ????????????
		updateAgentAddInfo.setUnitid(bean.getBudgetUnitId());
//        // ????????????
//        updateAgentAddInfo.setSubjectid(bean.getBudgetSubjectId());
//        // ?????????????????????????????????
//        updateAgentAddInfo.setAgentaddmoney(budgetYearSubject.getAddmoney());
//        // ?????????????????????????????????
//        updateAgentAddInfo.setAgentexcutemoney(budgetYearSubject.getExecutemoney());
//        // ???????????????????????????????????????????????????????????????
//        updateAgentAddInfo.setAgentlendinmoney(budgetYearSubject.getLendinmoney());
//        // ???????????????????????????????????????????????????????????????
//        updateAgentAddInfo.setAgentlendoutmoney(budgetYearSubject.getLendoutmoney());
//        // ???????????????????????????????????????????????????
//        updateAgentAddInfo.setAgentmoney(budgetYearSubject.getTotal());
		// ????????????
		updateAgentAddInfo.setFileurl(bean.getFileUrl());
		// ????????????
		updateAgentAddInfo.setFileoriginname(bean.getFileOriginName());
		// oa?????????????????????????????????
		updateAgentAddInfo.setOapassword(bean.getOaPwd());

		updateAgentAddInfo.setIsExemptFine(bean.getIsExemptFine() == null ? false : bean.getIsExemptFine());
		updateAgentAddInfo.setExemptFineReason(bean.getExemptFineReason());
		if (bean.getId() == null) {
			WbUser wbUser = UserThreadLocal.get();
			updateAgentAddInfo.setCreatetime(new Date());
			updateAgentAddInfo.setCreatorid(wbUser.getUserName());
			updateAgentAddInfo.setCreatorname(wbUser.getDisplayName());
			updateAgentAddInfo.setHandleflag(false);
			updateAgentAddInfo.setRequeststatus(0);
			updateAgentAddInfo.setYearaddcode(this.distributedNumber.getYearAgentAddNum());
			this.budgetYearAgentaddinfoMapper.insert(updateAgentAddInfo);
		} else {
			updateAgentAddInfo.setId(bean.getId());
			updateAgentAddInfo.setUpdatetime(new Date());
			this.budgetYearAgentaddinfoMapper.updateById(updateAgentAddInfo);
		}
		return updateAgentAddInfo;
	}

	/**
	 * ?????????OA??????
	 */
	public void submitVerify(Long infoId, Boolean existLock, List<Map<String, Object>> list) throws Exception {
		ZookeeperShareLock lock = null;
		try {
			if (!existLock) {
				lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/yearAgentAddMoney/" + infoId, o -> {
					throw new RuntimeException("????????????????????????????????????,??????????????????");
				});
				lock.tryLock();
			}

			// ??????OA?????????
			List<Map<String, Object>> budAddInfo = this.budgetYearAgentaddinfoMapper.listYearAgentAddByInfoId(infoId);
			if (budAddInfo == null || budAddInfo.isEmpty()) {
				throw new RuntimeException("????????????????????????????????????");
			}

			// ????????????????????????
			List<Long> yearAgentIds = budAddInfo.stream().map(v -> (Long) v.get("yearagentid")).filter(Objects::nonNull).collect(Collectors.toList());
			HashSet<Long> hashSet = new HashSet<>(yearAgentIds);
			if (yearAgentIds.size() != hashSet.size()) {
				throw new RuntimeException("??????????????????????????????????????????????????????");
			}

			Map<String, Object> addInfoMap = budAddInfo.get(0);
			String empNo = addInfoMap.get("creatorid").toString();

			// ????????????
			Integer requestStatus = (Integer) addInfoMap.get("requeststatus");
			if (requestStatus > 0) {
				throw new RuntimeException("????????????????????????????????????????????????");
			}

			String oAUserId;
			String oAdeptId;
			String userIdDeptId = this.oaService.getOaUserId(empNo, list);
			if ("0".equals(userIdDeptId)) {
				oAUserId = "0";
				oAdeptId = "0";
			} else {
				oAUserId = userIdDeptId.split(",")[0];
				oAdeptId = userIdDeptId.split(",")[1];
			}
			String userName = UserThreadLocal.get().getDisplayName();

			WorkflowInfo wi = new WorkflowInfo();
			wi.setCreatorId(oAUserId);
			wi.setRequestLevel("0");
			wi.setRequestName("????????????????????????--" + userName);

			WSBudgetYearAgentAdd wbAdd = new WSBudgetYearAgentAdd();
			wbAdd.setSqr(Integer.valueOf(oAUserId));
			wbAdd.setSsbm(oAdeptId);
			wbAdd.setWfid(flowid);

			List<WSBudgetYearAgentAddDetail> details = getWorkflowAgentAdd(budAddInfo, wbAdd);
			try {
				Object objRequestId = addInfoMap.get("requestid");
				Object oaCreatorId = addInfoMap.get("oacreatorid");
				if (objRequestId != null && oaCreatorId != null) {
					this.oaService.deleteRequest(objRequestId.toString(), oaCreatorId.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// ????????????
			String requestId = createBudgetAgentAdd(wi, wbAdd, details);
			if (null == requestId || Integer.parseInt(requestId) < 0) {
				throw new RuntimeException("???????????????oa?????????????????????????????????????????????oa????????????");
			}

			// ????????????
			BudgetYearAgentaddinfo updateAddInfo = new BudgetYearAgentaddinfo();
			updateAddInfo.setId(infoId);
			updateAddInfo.setRequeststatus(1);
			updateAddInfo.setRequestid(requestId);
			updateAddInfo.setUpdatetime(new Date());
			updateAddInfo.setOacreatorid(oAUserId);
			this.budgetYearAgentaddinfoMapper.updateById(updateAddInfo);
		} finally {
			if (lock != null) {
				lock.unLock();
			}
		}
	}

	private List<WSBudgetYearAgentAddDetail> getWorkflowAgentAdd(List<Map<String, Object>> budAddInfo, WSBudgetYearAgentAdd wbAdd) throws Exception {
		Map<String, Object> addInfoMap = budAddInfo.get(0);

		// ????????????
		Date createTime = (Date) addInfoMap.get("createtime");
		wbAdd.setSqrq(Constants.FORMAT_10.format(createTime));
		// ????????????
		Long yearId = (Long) addInfoMap.get("yearid");
		wbAdd.setYsjb(this.budgetYearPeriodMapper.selectById(yearId).getPeriod());
		// ??????????????????
		Long unitId = (Long) addInfoMap.get("unitid");
		String unitName = this.budgetUnitMapper.selectById(unitId).getName();
		wbAdd.setYsdw(unitName);
//        // ??????????????????
//        Long subjectId = (Long) addInfoMap.get("subjectid");
//        wbAdd.setZjkm(this.budgetSubjectMapper.selectById(subjectId).getName());
		wbAdd.setZjkm("");
		// ????????????????????????
		Integer count = this.budgetYearAgentaddinfoMapper.selectCount(new QueryWrapper<BudgetYearAgentaddinfo>()
				.eq("yearid", yearId)
				.eq("unitid", unitId)
				.eq("requeststatus", 2));
		wbAdd.setZjcs((count + 1) + "");
//        // ??????????????????
//        String kmncys = addInfoMap.get("kmncys").toString();
//        wbAdd.setNcys(kmncys);
//        // ??????????????????
//        String kmljzj = addInfoMap.get("kmljzj").toString();
//        wbAdd.setLjzj(kmljzj);
//        // ??????????????????
//        String kmljzx = addInfoMap.get("kmljzx").toString();
//        wbAdd.setLjzx(kmljzx);
//        // ??????????????????
//        String kmljcj = addInfoMap.get("kmljcj").toString();
//        // ??????????????????
//        String kmljcc = addInfoMap.get("kmljcc").toString();
//        // ?????????????????????????????????(??????) = ????????????+???????????????????????????+????????????+?????????????????????
//        String totals = addInfoMap.get("bctotal").toString();
//        wbAdd.setBczjhndysze(new BigDecimal(kmncys)
//                .add(new BigDecimal(kmljzj))
//                .add(new BigDecimal(totals))
//                .add(new BigDecimal(kmljcj)).toString());
//        // ?????????????????????????????????  = ??????????????????  - ??????  - ????????????
//        wbAdd.setBczjhndysye(new BigDecimal(wbAdd.getBczjhndysze()).
//                subtract(new BigDecimal(kmljcc)).
//                subtract(new BigDecimal(kmljzx)).toString());
		// ????????????????????????
		if (this.oaService.isProvinceUnit(unitId)) {
			wbAdd.setIsswsp(1);
		} else {
			wbAdd.setIsswsp(0);
		}
		// ??????
		int code = -1;
		if (addInfoMap.get("fileurl") != null && StringUtils.isNotBlank(addInfoMap.get("fileurl").toString())) {
			String fileUrl = addInfoMap.get("fileurl").toString();
			// ????????????
			String fileOriginName = addInfoMap.get("fileoriginname").toString();
			// oa??????
			String oaPassword = addInfoMap.get("oapassword").toString();

			URL url = new URL(fileUrl);
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();

			// ????????????
			String empNo = addInfoMap.get("creatorid").toString();
			code = this.oaService.createDoc(empNo, oaPassword, is, fileOriginName, fileUrl, "????????????????????????");
			if (code == 0) {
				throw new RuntimeException("????????????!??????????????????!");
			}
			is.close();
		}
		wbAdd.setFj(code + "");

		List<WSBudgetYearAgentAddDetail> details = new ArrayList<>();
		budAddInfo.forEach(yearAdd -> {
			WSBudgetYearAgentAddDetail detail = new WSBudgetYearAgentAddDetail();
			// ????????????
			int type = Integer.parseInt(yearAdd.get("type").toString());
			detail.setSjid(yearAdd.get("detailId").toString());
			detail.setZjlx(type == 0 ? "????????????" : "????????????");
			// ????????????
			detail.setDymc(yearAdd.get("name").toString());
			// ????????????
			Long subjectId = (Long) yearAdd.get("subjectid");
			String subjectName = this.budgetSubjectMapper.selectById(subjectId).getName();
			detail.setZjkm(subjectName);
			// ????????????
			BigDecimal total = new BigDecimal(yearAdd.get("total").toString());
			detail.setZjje(total);
			// ????????????
			Object remark = yearAdd.get("remark");
			detail.setZjly(remark != null ? remark.toString() : "");
			//????????????
			detail.setSfsqmf((Boolean) yearAdd.get("is_exempt_fine") ? "0" : "1");
			//????????????
			Object exempt_fine_reason = yearAdd.get("exempt_fine_reason");
			detail.setMfly(exempt_fine_reason == null ? "" : exempt_fine_reason.toString());
			BudgetYearSubject budgetYearSubject = this.budgetYearSubjectMapper.selectOne(new QueryWrapper<BudgetYearSubject>()
					.eq("yearId", yearId)
					.eq("unitId", unitId)
					.eq("subjectId", subjectId));
			if (budgetYearSubject == null) {
				throw new RuntimeException("???????????????????????????????????????????????????" + unitName + "?????????!");
			}

			// ????????????????????????
			detail.setNcys(budgetYearSubject.getTotal().setScale(2, BigDecimal.ROUND_HALF_UP).toString());

			// ????????????????????????
			detail.setLjzx(budgetYearSubject.getExecutemoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());

			// ?????????????????????????????????  = ??????????????????  - ??????  - ????????????
			detail.setBczjhndysye(budgetYearSubject.getTotal()
					.add(budgetYearSubject.getAddmoney())
					.add(budgetYearSubject.getLendinmoney())
					.add(total)
					.subtract(budgetYearSubject.getLendoutmoney())
					.subtract(budgetYearSubject.getExecutemoney())
					.setScale(2, BigDecimal.ROUND_HALF_UP)
					.toString());

//                // ??????????????????????????????
//                // ????????????????????????
//                String curmonthmoney = yearAdd.get("curmonthmoney").toString();
//                if (Double.parseDouble(curmonthmoney) > 0) {
//                    // ???????????????????????????
//                    detail.setSfzjydys("???");
//                    detail.setZjyf(yearAdd.get("curmonthid").toString() + "???");
//                    detail.setZjydje(curmonthmoney);
//                } else {
//                    // ????????????????????????
//                    detail.setSfzjydys("???");
//                    detail.setZjyf("???");
//                    detail.setZjydje("???");
//                }
			detail.setWfid(flowid);
			details.add(detail);
		});
		return details;
	}

	/**
	 * ????????????????????????
	 */
	public String createBudgetAgentAdd(WorkflowInfo wi, WSBudgetYearAgentAdd wbYearAdd, List<WSBudgetYearAgentAddDetail> details) {
		Map<String, Object> main = (Map<String, Object>) JSON.toJSON(wbYearAdd);
		List<Map<String, Object>> detailList = (List<Map<String, Object>>) JSON.toJSON(details);
		return this.oaService.createWorkflow(wi, wbYearAdd.getWfid(), main, detailList);
	}

	/**
	 * ????????????????????????
	 */
	public void deleteYearAgentAdd(List<Long> ids) {
		for (Long id : ids) {
			BudgetYearAgentaddinfo agentAddInfo = this.budgetYearAgentaddinfoMapper.selectById(id);
			if (agentAddInfo != null) {
				if (agentAddInfo.getRequeststatus() != 0) {
					throw new RuntimeException("????????????????????????!");
				}
				this.budgetYearAgentaddMapper.delete(new QueryWrapper<BudgetYearAgentadd>().eq("infoid", id));
				this.budgetYearAgentaddinfoMapper.deleteById(id);
			}
		}
	}

	/**
	 * ????????????????????????
	 */
	public List<YearAgentAddInfoExcelData> exportAgentYearAdd(HashMap<String, Object> paramMap) {
		List<YearAgentAddInfoExcelData> resultList = new ArrayList<>();

		List<BudgetYearAgentaddinfo> addInfoList = this.budgetYearAgentaddinfoMapper.listYearAgentAddInfoByMap(paramMap);

		HashMap<Long, String> subjectNames = new HashMap<>(5);
		for (BudgetYearAgentaddinfo addInfo : addInfoList) {
			// ??????????????????
			List<BudgetYearAgentadd> addList = this.budgetYearAgentaddMapper.selectList(new QueryWrapper<BudgetYearAgentadd>()
					.eq("infoid", addInfo.getId()));
			if (!addList.isEmpty()) {
				for (BudgetYearAgentadd agentAdd : addList) {
					YearAgentAddInfoExcelData rowData = new YearAgentAddInfoExcelData();
					rowData.setNum(resultList.size() + 1);
					rowData.setRequestStatus(Constants.getRequestStatus(addInfo.getRequeststatus()));
					rowData.setYearAddCode(addInfo.getYearaddcode());
					rowData.setPeriod(addInfo.getPeriod());
					rowData.setUnitName(addInfo.getUnitName());
					rowData.setIsExemptFine(agentAdd.getIsExemptFine() == null ? "???" : (agentAdd.getIsExemptFine() ? "???" : "???"));
					rowData.setExemptFineReason(agentAdd.getExemptFineReason() == null ? "" : agentAdd.getExemptFineReason());
					rowData.setExemptResult(agentAdd.getExemptResult() == null ? "" : (0 == agentAdd.getExemptResult() ? "??????" : "??????"));
					rowData.setFineRemark(agentAdd.getFineRemark() == null ? "" : agentAdd.getFineRemark());
					// ??????????????????
					if (!subjectNames.containsKey(agentAdd.getSubjectid())) {
						BudgetSubject subject = this.budgetSubjectMapper.selectById(agentAdd.getSubjectid());
						if (subject == null) {
							throw new RuntimeException("?????????????????????????????????");
						}
						subjectNames.put(subject.getId(), subject.getName());
					}
					rowData.setSubjectName(subjectNames.get(agentAdd.getSubjectid()));

					rowData.setAgentName(agentAdd.getName());
					rowData.setType(agentAdd.getType() == 0 ? "????????????" : "????????????");
					rowData.setTotal(agentAdd.getTotal());
					rowData.setRemark(agentAdd.getRemark());
					rowData.setAgentMoney(agentAdd.getAgentmoney().stripTrailingZeros().toPlainString());
					BigDecimal addBefore = agentAdd.getAgentmoney()
							.add(agentAdd.getAgentaddmoney())
							.add(agentAdd.getAgentlendinmoney())
							.subtract(agentAdd.getAgentlendoutmoney())
							.subtract(agentAdd.getAgentexcutemoney());
					rowData.setAddBefore(addBefore.stripTrailingZeros().toPlainString());
					rowData.setAddAfter(addBefore.add(agentAdd.getTotal()).stripTrailingZeros().toPlainString());

					rowData.setCreatorName(addInfo.getCreatorname());
					rowData.setCreateTime(Constants.FORMAT_10.format(addInfo.getCreatetime()));
					rowData.setAuditTime(addInfo.getAudittime() != null ? Constants.FORMAT_10.format(addInfo.getAudittime()) : "");

					resultList.add(rowData);
				}
			}
		}
		return resultList;
	}

	// ----------------------------------------------------------------------------------------------------

	/**
	 * ??????????????????????????????
	 */
	public void endYearAgentAdd(EcologyParams params) throws Exception {
		// ??????id
		String requestId = params.getRequestid();

		ZookeeperShareLock lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/endYearAgentAdd/" + requestId, o -> {
			throw new RuntimeException("??????????????????????????????????????????,??????????????????");
		});
		try {
			lock.tryLock();

			Date currentDate = new Date();
			// ????????????id?????? ??????????????????
			BudgetYearAgentaddinfo agentAddInfo = this.budgetYearAgentaddinfoMapper.selectOne(new QueryWrapper<BudgetYearAgentaddinfo>()
					.eq("requestid", requestId));
			if (agentAddInfo == null) {
				throw new RuntimeException("????????????????????????????????????");
			} else if (agentAddInfo.getRequeststatus() == 2) {
				throw new RuntimeException("??????????????????????????????????????????");
			}

			HashMap<Long, BigDecimal> subjectIds = new HashMap<>();
			EcologyWorkFlowValue value = EcologyClient.getWorkflowValue(params);
			Map<String, List<Map<String, String>>> detailTableValues = value.getDetailtablevalues();
			if (detailTableValues != null && !detailTableValues.isEmpty()) {
				// ?????????????????????(????????????????????????????????????????????????????????????)
				detailTableValues.forEach((str, values) -> {
					values.forEach(detail -> {
						String id = detail.get("sjid");
						BudgetYearAgentadd yearAdd = this.budgetYearAgentaddMapper.selectById(id);
						Long subjectId = yearAdd.getSubjectid();
						if (!subjectIds.containsKey(subjectId)) {
							subjectIds.put(subjectId, yearAdd.getTotal());
						} else {
							subjectIds.put(subjectId, subjectIds.get(subjectId).add(yearAdd.getTotal()));
						}

						// ??????????????????
						int type = yearAdd.getType();
						// ????????????
						BudgetYearAgent budgetYearAgent;
						// ????????????
						BudgetMonthAgent budgetMonthAgent;
						// ?????????id
						Long currentMonthId = yearAdd.getCurmonthid();
						// ???????????????
						BigDecimal currentMonthMoney = yearAdd.getCurmonthmoney();
						// ????????????
						if (type == 0) {
							// ????????????id
							Long yearAgentId = yearAdd.getYearagentid();
							budgetYearAgent = this.budgetYearAgentMapper.selectById(yearAgentId);
							if (budgetYearAgent == null) {
								throw new RuntimeException("?????????????????????" + yearAdd.getName() + "?????????");
							}
							BudgetYearAgent updateYearAgent = new BudgetYearAgent();
							updateYearAgent.setId(yearAgentId);
							updateYearAgent.setUpdatetime(new Date());
							updateYearAgent.setAddmoney(budgetYearAgent.getAddmoney().add(yearAdd.getTotal()));
							this.budgetYearAgentMapper.updateById(updateYearAgent);

							budgetMonthAgent = saveOrUpdateMonthAgent(budgetYearAgent, currentMonthId, currentMonthMoney, true);
						} else {
							Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
									.eq("unitId", yearAdd.getUnitid())
									.eq("subjectId", subjectId)
									.eq("name", yearAdd.getName()));
							if (duplicationCount > 0) {
								throw new RuntimeException("??????????????????????????????" + yearAdd.getName() + "??????????????????");
							}
							// ????????????
							budgetYearAgent = new BudgetYearAgent();
							budgetYearAgent.setYearid(yearAdd.getYearid());
							budgetYearAgent.setUnitid(yearAdd.getUnitid());
							budgetYearAgent.setSubjectid(subjectId);
							budgetYearAgent.setTotal(BigDecimal.ZERO);
							budgetYearAgent.setPretotal(BigDecimal.ZERO);
							budgetYearAgent.setPreestimate(BigDecimal.ZERO);
							budgetYearAgent.setCreatetime(currentDate);
							budgetYearAgent.setUpdatetime(currentDate);
							budgetYearAgent.setName(yearAdd.getName());
							budgetYearAgent.setHappencount("");
							budgetYearAgent.setRemark(yearAdd.getRemark());
							budgetYearAgent.setAddmoney(yearAdd.getTotal());
							budgetYearAgent.setLendoutmoney(BigDecimal.ZERO);
							budgetYearAgent.setLendinmoney(BigDecimal.ZERO);
							budgetYearAgent.setExecutemoney(BigDecimal.ZERO);
							budgetYearAgent.setElasticflag(false);
							budgetYearAgent.setElasticratio(BigDecimal.ZERO);
							budgetYearAgent.setElasticmax(BigDecimal.ZERO);
							budgetYearAgent.setAgenttype(1);
							this.budgetYearAgentMapper.insert(budgetYearAgent);

							budgetMonthAgent = saveOrUpdateMonthAgent(budgetYearAgent, currentMonthId, currentMonthMoney, false);
						}
						// ????????????
						if (budgetMonthAgent != null) {
							// ?????????????????????????????????
							this.budgetSysService.doSyncBudgetSubjectMonthAddMoney(budgetMonthAgent.getYearid(), budgetMonthAgent.getUnitid(), budgetMonthAgent.getSubjectid(), currentMonthId, currentMonthMoney, 1);
						}
						yearAdd.setYearagentid(budgetYearAgent.getId());
						yearAdd.setMonthagentid(budgetMonthAgent != null ? budgetMonthAgent.getId() : null);
						String mfjg = detail.get("mfjg");
						yearAdd.setExemptResult(Integer.valueOf(mfjg));
						yearAdd.setFineRemark(detail.get("fkyy"));
						this.budgetYearAgentaddMapper.updateById(yearAdd);
					});
				});
			}

			// ??????????????????????????????
			BudgetYearAgentaddinfo updateAddInfo = new BudgetYearAgentaddinfo();
			updateAddInfo.setId(agentAddInfo.getId());
			updateAddInfo.setRequeststatus(2);
			updateAddInfo.setHandleflag(true);
			updateAddInfo.setAudittime(new Date());
			this.budgetYearAgentaddinfoMapper.updateById(updateAddInfo);


			List<BudgetYearAgentadd> budgetYearAgentadds = budgetYearAgentaddMapper.selectList(new LambdaQueryWrapper<BudgetYearAgentadd>().eq(BudgetYearAgentadd::getInfoid, agentAddInfo.getId()));
			//??????
			long size = budgetYearAgentadds.stream().filter(e -> e.getExemptResult() != null && e.getExemptResult() == 0).count();

			BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(agentAddInfo.getUnitid());
			String budgetResponsibilities = budgetUnit.getBudgetResponsibilities();
			if (StringUtils.isNotBlank(budgetResponsibilities) && size > 0) {
				commonService.createBudgetFine(1, (int) size, budgetResponsibilities.split(",")[0],agentAddInfo.getCreatorid());
			}

			// ?????????????????????????????????
			subjectIds.forEach((subjectId, addTotal) -> this.budgetSysService.doSyncBudgetSubjectYearAddMoney(agentAddInfo.getYearid(), agentAddInfo.getUnitid(), subjectId, addTotal, 1));
		} finally {
			lock.unLock();
		}
	}

	private BudgetMonthAgent saveOrUpdateMonthAgent(BudgetYearAgent budgetYearAgent, Long monthId, BigDecimal monthMoney, boolean query) {
		if (monthMoney.compareTo(BigDecimal.ZERO) <= 0 || monthId == null) {
			return null;
		}
		BudgetMonthAgent budgetMonthAgent = null;
		if (query) {
			// ????????????????????????????????????
			budgetMonthAgent = this.budgetMonthAgentMapper.selectOne(new QueryWrapper<BudgetMonthAgent>()
					.eq("yearagentid", budgetYearAgent.getId())
					.eq("monthId", monthId));
		}
		if (budgetMonthAgent == null) {
			// ????????????????????????
			budgetMonthAgent = new BudgetMonthAgent();
			budgetMonthAgent.setYearid(budgetYearAgent.getYearid());
			budgetMonthAgent.setUnitid(budgetYearAgent.getUnitid());
			budgetMonthAgent.setMonthid(monthId);
			budgetMonthAgent.setYearagentmoney(budgetYearAgent.getTotal());
			budgetMonthAgent.setYearaddmoney(budgetYearAgent.getAddmoney());
			budgetMonthAgent.setYearexecutemoney(budgetYearAgent.getExecutemoney());
			budgetMonthAgent.setYearlendinmoney(budgetYearAgent.getLendinmoney());
			budgetMonthAgent.setYearlendoutmoney(budgetYearAgent.getLendoutmoney());
			budgetMonthAgent.setSubjectid(budgetYearAgent.getSubjectid());
			budgetMonthAgent.setName(budgetYearAgent.getName());
			budgetMonthAgent.setElasticflag(budgetYearAgent.getElasticflag());
			budgetMonthAgent.setElasticratio(budgetYearAgent.getElasticratio());
			budgetMonthAgent.setBudgetsubjectid(budgetYearAgent.getBudgetsubjectid());
			budgetMonthAgent.setRemark(budgetYearAgent.getRemark());
			budgetMonthAgent.setProductid(budgetYearAgent.getProductid());
			budgetMonthAgent.setYearagentid(budgetYearAgent.getId());
			budgetMonthAgent.setM(BigDecimal.ZERO);
			budgetMonthAgent.setTotal(BigDecimal.ZERO);
			budgetMonthAgent.setAddmoney(monthMoney);
			budgetMonthAgent.setLendinmoney(BigDecimal.ZERO);
			budgetMonthAgent.setLendoutmoney(BigDecimal.ZERO);
			budgetMonthAgent.setExecutemoney(BigDecimal.ZERO);
			budgetMonthAgent.setCreatetime(new Date());
			budgetMonthAgent.setUpdatetime(new Date());
			budgetMonthAgent.setAgenttype(1);
			this.budgetMonthAgentMapper.insert(budgetMonthAgent);
		} else {
			budgetMonthAgent.setAddmoney(budgetMonthAgent.getAddmoney().add(monthMoney));
			budgetMonthAgent.setUpdatetime(new Date());
			this.budgetMonthAgentMapper.updateById(budgetMonthAgent);
		}
		return budgetMonthAgent;
	}

	/**
	 * ????????????????????????
	 */
	public void rejectYearAgentAdd(EcologyParams params) {
		String requestId = params.getRequestid();
		// ????????????id?????? ??????????????????
		BudgetYearAgentaddinfo agentAddInfo = this.budgetYearAgentaddinfoMapper.selectOne(new QueryWrapper<BudgetYearAgentaddinfo>()
				.eq("requestid", requestId));
		if (agentAddInfo == null) {
			throw new RuntimeException("????????????????????????????????????");
		} else if (agentAddInfo.getRequeststatus() == 2) {
			throw new RuntimeException("?????????????????????????????????????????????????????????");
		}

		BudgetYearAgentaddinfo updateAddInfo = new BudgetYearAgentaddinfo();
		updateAddInfo.setId(agentAddInfo.getId());
		updateAddInfo.setRequeststatus(-1);
		updateAddInfo.setHandleflag(true);
		updateAddInfo.setUpdatetime(new Date());
		updateAddInfo.setAudittime(new Date());
		this.budgetYearAgentaddinfoMapper.updateById(updateAddInfo);


		EcologyWorkFlowValue workflowValue = EcologyClient.getWorkflowValue(params);
		Map<String, List<Map<String, String>>> detailtablevalues = workflowValue.getDetailtablevalues();
		detailtablevalues.values().forEach(list -> {
			list.forEach(e -> {
				String id = e.get("sjid");
				String mfjg = e.get("mfjg");
				String fkyy = e.get("fkyy");

				BudgetYearAgentadd budgetYearAgentadd = budgetYearAgentaddMapper.selectById(id);
				budgetYearAgentadd.setIsExemptFine(!"0".equals(mfjg));
				budgetYearAgentadd.setFineRemark(fkyy);
				budgetYearAgentaddMapper.updateById(budgetYearAgentadd);
			});
		});

	}

}
