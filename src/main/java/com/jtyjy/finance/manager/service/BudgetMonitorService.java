package com.jtyjy.finance.manager.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.finance.manager.bean.BudgetMonthAgent;
import com.jtyjy.finance.manager.bean.BudgetMonthSubject;
import com.jtyjy.finance.manager.bean.BudgetSubject;
import com.jtyjy.finance.manager.bean.BudgetYearAgent;
import com.jtyjy.finance.manager.bean.BudgetYearSubject;
import com.jtyjy.finance.manager.constants.StatusConstants;
import com.jtyjy.finance.manager.mapper.BudgetMonthAgentMapper;
import com.jtyjy.finance.manager.mapper.BudgetMonthSubjectMapper;
import com.jtyjy.finance.manager.mapper.BudgetSubjectMapper;
import com.jtyjy.finance.manager.mapper.BudgetSysMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearAgentMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearSubjectMapper;
import com.jtyjy.finance.manager.vo.MonthAddDataVO;
import com.jtyjy.finance.manager.vo.MonthExecuteDataVO;
import com.jtyjy.finance.manager.vo.YearAddDataVO;
import com.jtyjy.finance.manager.vo.YearExecuteDataVO;
import com.jtyjy.finance.manager.vo.YearLendDataVO;


@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
public class BudgetMonitorService {
	
	@Autowired
	private BudgetSysMapper sysMapper;
	
	@Autowired
	private BudgetYearSubjectMapper yearSubjectMapper;
	
	@Autowired
	private BudgetMonthSubjectMapper monthSubjectMapper;
	
	@Autowired
	private BudgetYearAgentMapper yearAgentMapper;
	
	@Autowired
	private BudgetSubjectMapper subjectMapper;
	
	@Autowired
	private BudgetMonthAgentMapper monthAgentMapper;
	
	public PageResult<YearExecuteDataVO> getYearExecuteDataList(Long yearId, Long unitId, Integer page, Integer rows) {
		List<BudgetSubject> subjectList = subjectMapper.selectList(new QueryWrapper<BudgetSubject>().eq("yearid", yearId));
		Map<Long, BudgetSubject> subjectMap = subjectList.stream().collect(Collectors.toMap(e->e.getId(), e->e));
		List<BudgetYearAgent> yearAgentList = yearAgentMapper.selectList(new QueryWrapper<BudgetYearAgent>().eq("unitid", unitId));
		Map<Long, List<BudgetYearAgent>> yearAgentMap = yearAgentList.stream().collect(Collectors.groupingBy(e->e.getSubjectid()));
		List<BudgetYearSubject> yearSubjectList = yearSubjectMapper.selectList(new QueryWrapper<BudgetYearSubject>().eq("unitid", unitId));
		List<Map<String,Object>> executeDataList = sysMapper.getUnitExecuteData(unitId,StatusConstants.BX_PASS);
		
		Map<String,YearExecuteDataVO> voMap = new HashMap<>();
		yearSubjectList.forEach(bys->{
			BudgetSubject subject = subjectMap.get(bys.getSubjectid());
			if(subject==null) return;
			if(subject.getLeafflag()) {
				//如果是叶子节点
				BigDecimal subjectExecutemoney = bys.getExecutemoney();
				List<Map<String, Object>> subjectReimMap = executeDataList.stream().filter(e->yearId.toString().equals(e.get("yearid").toString()) 
						&& unitId.toString().equals(e.get("unitid").toString())
						&& e.get("subjectpids").toString().startsWith(subject.getPids()))
						.collect(Collectors.toList());
				BigDecimal reimSubjectExecuteMoneys = subjectReimMap.stream().map(e->new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);


//				if(subjectExecutemoney.compareTo(reimSubjectExecuteMoneys) !=0) {
//						YearExecuteDataVO vo = new YearExecuteDataVO();
//						vo.setSubjectName(subject.getName());
//						vo.setYearSubjectExecuteMoney(subjectExecutemoney);
//						vo.setBxYearSubjectExecuteMoney(reimSubjectExecuteMoneys);
//						vo.setIsShowHandleButton(true);
//						voMap.put(subject.getId()+"-0", vo);
//					return;
//				}


				List<BudgetYearAgent> agentList = yearAgentMap.get(subject.getId());
				if(agentList !=null && !agentList.isEmpty()) {
					agentList.forEach(agent->{
						BigDecimal agentExecutemoney = agent.getExecutemoney();
						List<Map<String, Object>> agentMap = executeDataList.stream().filter(e->agent.getYearid().toString().equals(e.get("yearid").toString()) 
								&& agent.getUnitid().toString().equals(e.get("unitid").toString())
								&& agent.getId().toString().equals(e.get("yearagentid").toString())).collect(Collectors.toList());
						BigDecimal agentExecuteMoneys = agentMap.stream().map(e->new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
						if(agentExecutemoney.compareTo(agentExecuteMoneys) !=0) {
							YearExecuteDataVO vo = new YearExecuteDataVO();
							vo.setSubjectName(subject.getName());
							vo.setYearSubjectExecuteMoney(subjectExecutemoney);
							vo.setBxYearSubjectExecuteMoney(reimSubjectExecuteMoneys);
							vo.setAgentExecuteMoney(agentExecutemoney);
							vo.setBxAgentExecuteMoney(agentExecuteMoneys);
							vo.setYearAgentId(agent.getId());
							vo.setAgentName(agent.getName());
							vo.setIsShowHandleButton(true);
							voMap.put(subject.getId()+"-"+agent.getId(), vo);
						}
					});
				}else {
					if(subjectExecutemoney.compareTo(reimSubjectExecuteMoneys) !=0) {
						if(voMap.get(subject.getId()+"-0")!=null) {
							YearExecuteDataVO vo = new YearExecuteDataVO();
							vo.setSubjectName(subject.getName());
							vo.setYearSubjectExecuteMoney(subjectExecutemoney);
							vo.setBxYearSubjectExecuteMoney(reimSubjectExecuteMoneys);
							voMap.put(subject.getId()+"-0", vo);
						}
					}
				}
			}else {
				//如果不是叶子节点
				BigDecimal executemoney = bys.getExecutemoney();
				List<Map<String, Object>> subjectReimMap = executeDataList.stream().filter(e->yearId.toString().equals(e.get("yearid").toString()) 
						&& unitId.toString().equals(e.get("unitid").toString())
						&& e.get("subjectpids").toString().startsWith(subject.getPids()))
						.collect(Collectors.toList());
				BigDecimal reimSubjectExecuteMoneys = subjectReimMap.stream().map(e->new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
				if(executemoney.compareTo(reimSubjectExecuteMoneys) !=0) {
					if(voMap.get(subject.getId()+"-0")!=null) {
						YearExecuteDataVO vo = new YearExecuteDataVO();
						vo.setSubjectName(subject.getName());
						vo.setYearSubjectExecuteMoney(executemoney);
						vo.setBxYearSubjectExecuteMoney(reimSubjectExecuteMoneys);
						voMap.put(subject.getId()+"-0", vo);
					}
				}
			}
		});
		int total = voMap.values().size();
		List<YearExecuteDataVO> records = voMap.values().stream().skip((page-1)*rows).limit(rows).collect(Collectors.toList());
		return PageResult.apply(total, records);
	}
	
	/**
	 * 处理年度执行异常数据
	 * @param yearAgentId
	 */
	public void handleYearExecuteDataList(Long yearAgentId) {
		BudgetYearAgent yearAgent = yearAgentMapper.selectById(yearAgentId);
		List<Map<String,Object>> reimExecuteList = sysMapper.getUnitExecuteData(yearAgent.getUnitid(), StatusConstants.BX_PASS);
		handleYearExecute(yearAgent.getUnitid(),yearAgent.getSubjectid(),reimExecuteList);
		BigDecimal executemoney = reimExecuteList.stream().filter(e->e.get("yearagentid").toString().equals(yearAgentId.toString())).map(e->new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
		yearAgent.setExecutemoney(executemoney);
		yearAgentMapper.updateById(yearAgent);
	}

	private void handleYearExecute(Long unitid, Long subjectid, List<Map<String, Object>> reimExecuteList) {
		if(subjectid.toString().equals("0")) return;
		BudgetYearSubject bys = this.yearSubjectMapper.selectOne(new QueryWrapper<BudgetYearSubject>().eq("unitid", unitid).eq("subjectid", subjectid));
		BudgetSubject subject = this.subjectMapper.selectById(subjectid);
		BigDecimal subjectExecutemoney = reimExecuteList.stream().filter(e->("-"+e.get("subjectpids").toString()+"-").contains("-"+subjectid.toString()+"-")).map(e->new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
		bys.setExecutemoney(subjectExecutemoney);
		this.yearSubjectMapper.updateById(bys);
		handleYearExecute(bys.getUnitid(),subject.getParentid(),reimExecuteList);
	}

	public PageResult<YearAddDataVO> getYearAddExceptionDataList(Long yearId, Long unitId, Integer page, Integer rows) {
		List<BudgetSubject> subjectList = subjectMapper.selectList(new QueryWrapper<BudgetSubject>().eq("yearid", yearId));
		Map<Long, BudgetSubject> subjectMap = subjectList.stream().collect(Collectors.toMap(e->e.getId(), e->e));
		List<BudgetYearAgent> yearAgentList = yearAgentMapper.selectList(new QueryWrapper<BudgetYearAgent>().eq("unitid", unitId));
		Map<Long, List<BudgetYearAgent>> yearAgentMap = yearAgentList.stream().collect(Collectors.groupingBy(e->e.getSubjectid()));
		List<BudgetYearSubject> yearSubjectList = yearSubjectMapper.selectList(new QueryWrapper<BudgetYearSubject>().eq("unitid", unitId));
		List<Map<String,Object>> addDataList = sysMapper.getUnitYearAddData(unitId);
		
		Map<String,YearAddDataVO> voMap = new HashMap<>();
		yearSubjectList.forEach(bys->{
			BudgetSubject subject = subjectMap.get(bys.getSubjectid());
			if(subject==null) return;
			if(subject.getLeafflag()) {
				//如果是叶子节点
				BigDecimal subjectAddmoney = bys.getAddmoney();
				List<Map<String, Object>> subjectProcessMap = addDataList.stream().filter(e->yearId.toString().equals(e.get("yearid").toString()) 
						&& unitId.toString().equals(e.get("unitid").toString())
						&& e.get("subjectpids").toString().startsWith(subject.getPids()))
						.collect(Collectors.toList());
				BigDecimal processSubjectAddMoneys = subjectProcessMap.stream().map(e->new BigDecimal(e.get("addmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
				
				List<BudgetYearAgent> agentList = yearAgentMap.get(subject.getId());
				if(agentList !=null && !agentList.isEmpty()) {
					agentList.forEach(agent->{
						BigDecimal agentAddmoney = agent.getAddmoney();
						List<Map<String, Object>> agentMap = addDataList.stream().filter(e->agent.getYearid().toString().equals(e.get("yearid").toString()) 
								&& agent.getUnitid().toString().equals(e.get("unitid").toString())
								&& agent.getId().toString().equals(e.get("yearagentid").toString())).collect(Collectors.toList());
						BigDecimal agentAddMoneys = agentMap.stream().map(e->new BigDecimal(e.get("addmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
						if(agentAddmoney.compareTo(agentAddMoneys) !=0) {
							YearAddDataVO vo = new YearAddDataVO();
							vo.setSubjectName(subject.getName());
							vo.setYearSubjectAddMoney(subjectAddmoney);
							vo.setProcessYearSubjectAddMoney(processSubjectAddMoneys);
							vo.setAgentAddMoney(agentAddmoney);
							vo.setProcessAgentAddMoney(agentAddMoneys);
							vo.setYearAgentId(agent.getId());
							vo.setAgentName(agent.getName());
							vo.setIsShowHandleButton(true);
							voMap.put(subject.getId()+"-"+agent.getId(), vo);
						}
					});
				}else {
					if(subjectAddmoney.compareTo(processSubjectAddMoneys) !=0) {
						if(voMap.get(subject.getId()+"-0")!=null) {
							YearAddDataVO vo = new YearAddDataVO();
							vo.setSubjectName(subject.getName());
							vo.setYearSubjectAddMoney(subjectAddmoney);
							vo.setProcessYearSubjectAddMoney(processSubjectAddMoneys);
							voMap.put(subject.getId()+"-0", vo);
						}
					}
				}
			}else {
				//如果不是叶子节点
				BigDecimal addMoney = bys.getAddmoney();
				List<Map<String, Object>> subjectAddMap = addDataList.stream().filter(e->yearId.toString().equals(e.get("yearid").toString()) 
						&& unitId.toString().equals(e.get("unitid").toString())
						&& e.get("subjectpids").toString().startsWith(subject.getPids()))
						.collect(Collectors.toList());
				BigDecimal processSubjectAddMoneys = subjectAddMap.stream().map(e->new BigDecimal(e.get("addmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
				if(addMoney.compareTo(processSubjectAddMoneys) !=0) {
					if(voMap.get(subject.getId()+"-0")!=null) {
						YearAddDataVO vo = new YearAddDataVO();
						vo.setSubjectName(subject.getName());
						vo.setYearSubjectAddMoney(addMoney);
						vo.setProcessYearSubjectAddMoney(processSubjectAddMoneys);
						voMap.put(subject.getId()+"-0", vo);
					}
				}
			}
		});
		int total = voMap.values().size();
		List<YearAddDataVO> records = voMap.values().stream().skip((page-1)*rows).limit(rows).collect(Collectors.toList());
		return PageResult.apply(total, records);
	}

	
	/**
	 * 处理年度追加异常数据
	 * @param yearAgentId
	 */
	public void handleYearAddExceptionData(Long yearAgentId) {
		BudgetYearAgent yearAgent = yearAgentMapper.selectById(yearAgentId);
		List<Map<String,Object>> processAddList = sysMapper.getUnitYearAddData(yearAgent.getUnitid());
		handleYearAdd(yearAgent.getUnitid(),yearAgent.getSubjectid(),processAddList);
		BigDecimal addmoney = processAddList.stream().filter(e->e.get("yearagentid").toString().equals(yearAgentId.toString())).map(e->new BigDecimal(e.get("addmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
		yearAgent.setAddmoney(addmoney);
		yearAgentMapper.updateById(yearAgent);
	}

	private void handleYearAdd(Long unitid, Long subjectid, List<Map<String, Object>> processAddList) {
		if(subjectid.toString().equals("0")) return;
		BudgetYearSubject bys = this.yearSubjectMapper.selectOne(new QueryWrapper<BudgetYearSubject>().eq("unitid", unitid).eq("subjectid", subjectid));
		BudgetSubject subject = this.subjectMapper.selectById(subjectid);
		BigDecimal subjectAddmoney = processAddList.stream().filter(e->("-"+e.get("subjectpids").toString()+"-").contains("-"+subjectid.toString()+"-")).map(e->new BigDecimal(e.get("addmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
		bys.setAddmoney(subjectAddmoney);
		this.yearSubjectMapper.updateById(bys);
		handleYearAdd(bys.getUnitid(),subject.getParentid(),processAddList);
	}
	
	/**
	 * 获取月度执行异常数据
	 * @param yearId
	 * @param unitId
	 * @param monthId
	 * @param page
	 * @param rows
	 * @return
	 */
	public PageResult<MonthExecuteDataVO> getMonthExecuteExceptionDataList(Long yearId, Long unitId, Long monthId,
			Integer page, Integer rows) {
		List<BudgetSubject> subjectList = subjectMapper.selectList(new QueryWrapper<BudgetSubject>().eq("yearid", yearId));
		Map<Long, BudgetSubject> subjectMap = subjectList.stream().collect(Collectors.toMap(e->e.getId(), e->e));
		List<BudgetMonthAgent> monthAgentList = monthAgentMapper.selectList(new QueryWrapper<BudgetMonthAgent>().eq("unitid", unitId).eq("monthid", monthId));
		Map<Long, List<BudgetMonthAgent>> monthAgentMap = monthAgentList.stream().collect(Collectors.groupingBy(e->e.getSubjectid()));
		List<BudgetMonthSubject> monthSubjectList = monthSubjectMapper.selectList(new QueryWrapper<BudgetMonthSubject>().eq("unitid", unitId).eq("monthid", monthId));
		List<Map<String,Object>> executeDataList = sysMapper.getUnitExecuteData(unitId,StatusConstants.BX_PASS);
		
		Map<String,MonthExecuteDataVO> voMap = new HashMap<>();
		monthSubjectList.forEach(bys->{
			BudgetSubject subject = subjectMap.get(bys.getSubjectid());
			if(subject==null) return;
			if(subject.getLeafflag()) {
				//如果是叶子节点
				BigDecimal subjectExecutemoney = bys.getExecutemoney();
				List<Map<String, Object>> subjectReimMap = executeDataList.stream().filter(e->yearId.toString().equals(e.get("yearid").toString()) 
						&& monthId.toString().equals(e.get("monthid").toString())
						&& unitId.toString().equals(e.get("unitid").toString())
						&& e.get("subjectpids").toString().startsWith(subject.getPids()))
						.collect(Collectors.toList());
				BigDecimal reimSubjectExecuteMoneys = subjectReimMap.stream().map(e->new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
				
				List<BudgetMonthAgent> agentList = monthAgentMap.get(subject.getId());
				if(agentList !=null && !agentList.isEmpty()) {
					agentList.forEach(agent->{
						BigDecimal agentExecutemoney = agent.getExecutemoney();
						List<Map<String, Object>> agentMap = executeDataList.stream().filter(e->agent.getYearid().toString().equals(e.get("yearid").toString()) 
								&& monthId.toString().equals(e.get("monthid").toString())
								&& agent.getUnitid().toString().equals(e.get("unitid").toString())
								&& agent.getId().toString().equals(e.get("monthagentid").toString())).collect(Collectors.toList());
						BigDecimal agentExecuteMoneys = agentMap.stream().map(e->new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
						if(agentExecutemoney.compareTo(agentExecuteMoneys) !=0) {
							MonthExecuteDataVO vo = new MonthExecuteDataVO();
							vo.setSubjectName(subject.getName());
							vo.setMonthSubjectExecuteMoney(subjectExecutemoney);
							vo.setBxMonthSubjectExecuteMoney(reimSubjectExecuteMoneys);
							vo.setAgentExecuteMoney(agentExecutemoney);
							vo.setBxAgentExecuteMoney(agentExecuteMoneys);
							vo.setMonthAgentId(agent.getId());
							vo.setAgentName(agent.getName());
							vo.setIsShowHandleButton(true);
							voMap.put(subject.getId()+"-"+agent.getId(), vo);
						}
					});
				}else {
					if(subjectExecutemoney.compareTo(reimSubjectExecuteMoneys) !=0) {
						if(voMap.get(subject.getId()+"-0")!=null) {
							MonthExecuteDataVO vo = new MonthExecuteDataVO();
							vo.setSubjectName(subject.getName());
							vo.setMonthSubjectExecuteMoney(subjectExecutemoney);
							vo.setBxMonthSubjectExecuteMoney(reimSubjectExecuteMoneys);
							voMap.put(subject.getId()+"-0", vo);
						}
					}
				}
			}else {
				//如果不是叶子节点
				BigDecimal executemoney = bys.getExecutemoney();
				List<Map<String, Object>> subjectReimMap = executeDataList.stream().filter(e->yearId.toString().equals(e.get("yearid").toString()) 
						&& monthId.toString().equals(e.get("monthid").toString())
						&& unitId.toString().equals(e.get("unitid").toString())
						&& e.get("subjectpids").toString().startsWith(subject.getPids()))
						.collect(Collectors.toList());
				BigDecimal reimSubjectExecuteMoneys = subjectReimMap.stream().map(e->new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
				if(executemoney.compareTo(reimSubjectExecuteMoneys) !=0) {
					if(voMap.get(subject.getId()+"-0")!=null) {
						MonthExecuteDataVO vo = new MonthExecuteDataVO();
						vo.setSubjectName(subject.getName());
						vo.setMonthSubjectExecuteMoney(executemoney);
						vo.setBxMonthSubjectExecuteMoney(reimSubjectExecuteMoneys);
						voMap.put(subject.getId()+"-0", vo);
					}
				}
			}
		});
		int total = voMap.values().size();
		List<MonthExecuteDataVO> records = voMap.values().stream().skip((page-1)*rows).limit(rows).collect(Collectors.toList());
		return PageResult.apply(total, records);
	}

	/**
	 * 处理月度执行异常数据
	 * @param monthAgentId
	 */
	public void handleMonthExecuteExceptionData(Long monthAgentId) {
		BudgetMonthAgent monthAgent = this.monthAgentMapper.selectById(monthAgentId);
		List<Map<String,Object>> reimExecuteList = sysMapper.getUnitExecuteData(monthAgent.getUnitid(), StatusConstants.BX_PASS);
		handleMonthExecute(monthAgent.getUnitid(),monthAgent.getSubjectid(),monthAgent.getMonthid(),reimExecuteList);
		
		BigDecimal executemoney = reimExecuteList.stream().filter(e->e.get("monthagentid").toString().equals(monthAgentId.toString())).map(e->new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
		monthAgent.setExecutemoney(executemoney);
		monthAgentMapper.updateById(monthAgent);
		
	}
	
	private void handleMonthExecute(Long unitid, Long subjectid,Long monthid, List<Map<String, Object>> reimExecuteList) {
		if(subjectid.toString().equals("0")) return;
		BudgetMonthSubject bys = this.monthSubjectMapper.selectOne(new QueryWrapper<BudgetMonthSubject>().eq("unitid", unitid).eq("subjectid", subjectid).eq("monthid", monthid));
		BudgetSubject subject = this.subjectMapper.selectById(subjectid);
		BigDecimal subjectExecutemoney = reimExecuteList.stream().filter(e->("-"+e.get("subjectpids").toString()+"-").contains("-"+subjectid.toString()+"-") && monthid.toString().equals(e.get("monthid").toString())).map(e->new BigDecimal(e.get("executemoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
		bys.setExecutemoney(subjectExecutemoney);
		this.monthSubjectMapper.updateById(bys);
		handleMonthExecute(bys.getUnitid(),subject.getParentid(),monthid,reimExecuteList);
	}

	public PageResult<MonthAddDataVO> getMonthAddExceptionDataList(Long yearId, Long unitId,Long monthId, Integer page,
			Integer rows) {
		List<BudgetSubject> subjectList = subjectMapper.selectList(new QueryWrapper<BudgetSubject>().eq("yearid", yearId));
		Map<Long, BudgetSubject> subjectMap = subjectList.stream().collect(Collectors.toMap(e->e.getId(), e->e));
		List<BudgetMonthAgent> monthAgentList = monthAgentMapper.selectList(new QueryWrapper<BudgetMonthAgent>().eq("unitid", unitId).eq("monthid", monthId));
		Map<Long, List<BudgetMonthAgent>> monthAgentMap = monthAgentList.stream().collect(Collectors.groupingBy(e->e.getSubjectid()));
		List<BudgetMonthSubject> monthSubjectList = monthSubjectMapper.selectList(new QueryWrapper<BudgetMonthSubject>().eq("unitid", unitId).eq("monthid", monthId));
		List<Map<String,Object>> monthAddDataList = sysMapper.getUnitMonthAddData(unitId,monthId);
		List<Map<String,Object>> yearMonthAddDataList = sysMapper.getUnitYearMonthAddData(unitId,monthId);
		monthAddDataList.addAll(yearMonthAddDataList);
		
		Map<String,MonthAddDataVO> voMap = new HashMap<>();
		monthSubjectList.forEach(bys->{
			BudgetSubject subject = subjectMap.get(bys.getSubjectid());
			if(subject==null) return;
			if(subject.getLeafflag()) {
				//如果是叶子节点
				BigDecimal subjectAddmoney = bys.getAddmoney();
				List<Map<String, Object>> subjectProcessMap = monthAddDataList.stream().filter(e->yearId.toString().equals(e.get("yearid").toString()) 
						&& unitId.toString().equals(e.get("unitid").toString())
						&& e.get("subjectpids").toString().startsWith(subject.getPids()))
						.collect(Collectors.toList());
				BigDecimal processSubjectAddMoneys = subjectProcessMap.stream().map(e->new BigDecimal(e.get("addmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
				
				List<BudgetMonthAgent> agentList = monthAgentMap.get(subject.getId());
				if(agentList !=null && !agentList.isEmpty()) {
					agentList.forEach(agent->{
						BigDecimal agentAddmoney = agent.getAddmoney();
						List<Map<String, Object>> agentMap = monthAddDataList.stream().filter(e->agent.getYearid().toString().equals(e.get("yearid").toString()) 
								&& agent.getUnitid().toString().equals(e.get("unitid").toString())
								&& agent.getId().toString().equals(e.get("monthagentid").toString())).collect(Collectors.toList());
						BigDecimal agentAddMoneys = agentMap.stream().map(e->new BigDecimal(e.get("addmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
						if(agentAddmoney.compareTo(agentAddMoneys) !=0) {
							MonthAddDataVO vo = new MonthAddDataVO();
							vo.setSubjectName(subject.getName());
							vo.setMonthSubjectAddMoney(subjectAddmoney);
							vo.setProcessMonthSubjectAddMoney(processSubjectAddMoneys);
							vo.setAgentAddMoney(agentAddmoney);
							vo.setProcessAgentAddMoney(agentAddMoneys);
							vo.setMonthAgentId(agent.getId());
							vo.setAgentName(agent.getName());
							vo.setIsShowHandleButton(true);
							voMap.put(subject.getId()+"-"+agent.getId(), vo);
						}
					});
				}else {
					if(subjectAddmoney.compareTo(processSubjectAddMoneys) !=0) {
						if(voMap.get(subject.getId()+"-0")!=null) {
							MonthAddDataVO vo = new MonthAddDataVO();
							vo.setSubjectName(subject.getName());
							vo.setMonthSubjectAddMoney(subjectAddmoney);
							vo.setProcessMonthSubjectAddMoney(processSubjectAddMoneys);
							voMap.put(subject.getId()+"-0", vo);
						}
					}
				}
			}else {
				//如果不是叶子节点
				BigDecimal addMoney = bys.getAddmoney();
				List<Map<String, Object>> subjectAddMap = monthAddDataList.stream().filter(e->yearId.toString().equals(e.get("yearid").toString()) 
						&& unitId.toString().equals(e.get("unitid").toString())
						&& e.get("subjectpids").toString().startsWith(subject.getPids()))
						.collect(Collectors.toList());
				BigDecimal processSubjectAddMoneys = subjectAddMap.stream().map(e->new BigDecimal(e.get("addmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
				if(addMoney.compareTo(processSubjectAddMoneys) !=0) {
					if(voMap.get(subject.getId()+"-0")!=null) {
						MonthAddDataVO vo = new MonthAddDataVO();
						vo.setSubjectName(subject.getName());
						vo.setMonthSubjectAddMoney(addMoney);
						vo.setProcessMonthSubjectAddMoney(processSubjectAddMoneys);
						voMap.put(subject.getId()+"-0", vo);
					}
				}
			}
		});
		int total = voMap.values().size();
		List<MonthAddDataVO> records = voMap.values().stream().skip((page-1)*rows).limit(rows).collect(Collectors.toList());
		return PageResult.apply(total, records);
	}

	public void handleMonthAddExceptionData(Long monthAgentId) {
		
		BudgetMonthAgent monthAgent = monthAgentMapper.selectById(monthAgentId);
		List<Map<String,Object>> monthAddDataList = sysMapper.getUnitMonthAddData(monthAgent.getUnitid(),monthAgent.getMonthid());
		List<Map<String,Object>> yearMonthAddDataList = sysMapper.getUnitYearMonthAddData(monthAgent.getUnitid(),monthAgent.getMonthid());
		monthAddDataList.addAll(yearMonthAddDataList);
		handleMonthAdd(monthAgent.getUnitid(),monthAgent.getSubjectid(),monthAgent.getMonthid(),monthAddDataList);
		BigDecimal addmoney = monthAddDataList.stream().filter(e->e.get("monthagentid").toString().equals(monthAgentId.toString())).map(e->new BigDecimal(e.get("addmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
		monthAgent.setAddmoney(addmoney);
		monthAgentMapper.updateById(monthAgent);
	}

	private void handleMonthAdd(Long unitid, Long subjectid, Long monthid, List<Map<String, Object>> monthAddDataList) {
		if(subjectid.toString().equals("0")) return;
		BudgetMonthSubject bys = this.monthSubjectMapper.selectOne(new QueryWrapper<BudgetMonthSubject>().eq("unitid", unitid).eq("subjectid", subjectid).eq("monthid", monthid));
		BudgetSubject subject = this.subjectMapper.selectById(subjectid);
		BigDecimal subjectAddmoney = monthAddDataList.stream().filter(e->("-"+e.get("subjectpids").toString()+"-").contains("-"+subjectid.toString()+"-")).map(e->new BigDecimal(e.get("addmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
		bys.setAddmoney(subjectAddmoney);
		this.monthSubjectMapper.updateById(bys);
		handleMonthAdd(bys.getUnitid(),subject.getParentid(),monthid,monthAddDataList);
	}

	public PageResult<YearLendDataVO> getYearLendExceptionDataList(Long yearId, Long unitId, Integer page,
			Integer rows) {
		List<BudgetSubject> subjectList = subjectMapper.selectList(new QueryWrapper<BudgetSubject>().eq("yearid", yearId));
		Map<Long, BudgetSubject> subjectMap = subjectList.stream().collect(Collectors.toMap(e->e.getId(), e->e));
		List<BudgetYearAgent> yearAgentList = yearAgentMapper.selectList(new QueryWrapper<BudgetYearAgent>().eq("unitid", unitId));
		Map<Long, List<BudgetYearAgent>> yearAgentMap = yearAgentList.stream().collect(Collectors.groupingBy(e->e.getSubjectid()));
		List<BudgetYearSubject> yearSubjectList = yearSubjectMapper.selectList(new QueryWrapper<BudgetYearSubject>().eq("unitid", unitId));
		List<Map<String,Object>> lendDataList = sysMapper.getUnitYearLendData(unitId);
		
		Map<String,YearLendDataVO> voMap = new HashMap<>();
		yearSubjectList.forEach(bys->{
			BudgetSubject subject = subjectMap.get(bys.getSubjectid());
			if(subject==null) return;
			if(subject.getLeafflag()!=null && subject.getLeafflag()) {
				//如果是叶子节点
				BigDecimal subjectLendinmoney = bys.getLendinmoney();
				BigDecimal subjectLendoutmoney = bys.getLendoutmoney();
				List<Map<String, Object>> subjectProcessLendinList = lendDataList.stream().filter(e->yearId.toString().equals(e.get("yearid").toString()) 
						&& unitId.toString().equals(e.get("inunitid").toString())
						&& e.get("insubjectpids").toString().startsWith(subject.getPids()))
						.collect(Collectors.toList());
				BigDecimal processSubjectlendinMoneys = subjectProcessLendinList.stream().map(e->new BigDecimal(e.get("lendmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
				
				List<Map<String, Object>> subjectProcessLendoutList = lendDataList.stream().filter(e->yearId.toString().equals(e.get("yearid").toString()) 
						&& unitId.toString().equals(e.get("outunitid").toString())
						&& e.get("outsubjectpids").toString().startsWith(subject.getPids()))
						.collect(Collectors.toList());
				BigDecimal processSubjectlendoutMoneys = subjectProcessLendoutList.stream().map(e->new BigDecimal(e.get("lendmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
				
				List<BudgetYearAgent> agentList = yearAgentMap.get(subject.getId());
				if(agentList !=null && !agentList.isEmpty()) {
					agentList.forEach(agent->{
						BigDecimal agentLendinmoney = agent.getLendinmoney();
						BigDecimal agentLendoutmoney = agent.getLendoutmoney();
						List<Map<String, Object>> agentLendinMap = lendDataList.stream().filter(e->agent.getYearid().toString().equals(e.get("yearid").toString()) 
								&& agent.getUnitid().toString().equals(e.get("inunitid").toString())
								&& agent.getId().toString().equals(e.get("inyearagentid").toString())).collect(Collectors.toList());
						BigDecimal processagentLendinMoneys = agentLendinMap.stream().map(e->new BigDecimal(e.get("lendmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
						
						List<Map<String, Object>> agentLendoutMap = lendDataList.stream().filter(e->agent.getYearid().toString().equals(e.get("yearid").toString()) 
								&& agent.getUnitid().toString().equals(e.get("outunitid").toString())
								&& agent.getId().toString().equals(e.get("outyearagentid").toString())).collect(Collectors.toList());
						BigDecimal processagentLendoutMoneys = agentLendoutMap.stream().map(e->new BigDecimal(e.get("lendmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
						
						if(agentLendinmoney.compareTo(processagentLendinMoneys) !=0 || agentLendoutmoney.compareTo(processagentLendoutMoneys) !=0) {
							YearLendDataVO vo = new YearLendDataVO();
							vo.setSubjectName(subject.getName());
							vo.setYearAgentId(agent.getId());
							vo.setAgentName(agent.getName());
							vo.setYearSubjectLendinMoney(subjectLendinmoney);
							vo.setProcessYearSubjectLendinMoney(processSubjectlendinMoneys);
							vo.setAgentLendinMoney(agentLendinmoney);
							vo.setProcessAgentLendinMoney(processagentLendinMoneys);
							vo.setYearSubjectLendoutMoney(subjectLendoutmoney);
							vo.setProcessYearSubjectLendoutMoney(processSubjectlendoutMoneys);
							vo.setAgentLendoutMoney(agentLendoutmoney);
							vo.setProcessAgentLendoutMoney(processagentLendoutMoneys);
							vo.setIsShowHandleButton(true);
							voMap.put(subject.getId()+"-"+agent.getId(), vo);
						}
					});
				}else {
					if(subjectLendinmoney.compareTo(processSubjectlendinMoneys) !=0
							|| subjectLendoutmoney.compareTo(processSubjectlendoutMoneys) !=0) {
						if(voMap.get(subject.getId()+"-0")!=null) {
							YearLendDataVO vo = new YearLendDataVO();
							vo.setSubjectName(subject.getName());
							vo.setYearSubjectLendinMoney(subjectLendinmoney);
							vo.setProcessYearSubjectLendinMoney(processSubjectlendinMoneys);
							vo.setYearSubjectLendoutMoney(subjectLendoutmoney);
							vo.setProcessYearSubjectLendoutMoney(processSubjectlendoutMoneys);
							vo.setIsShowHandleButton(true);
							voMap.put(subject.getId()+"-0", vo);
						}
					}
				}
			}else {
				//如果不是叶子节点
				BigDecimal subjectLendinmoney = bys.getLendinmoney();
				BigDecimal subjectLendoutmoney = bys.getLendoutmoney();
				List<Map<String, Object>> subjectProcessLendinList = lendDataList.stream().filter(e->yearId.toString().equals(e.get("yearid").toString()) 
						&& unitId.toString().equals(e.get("inunitid").toString())
						&& e.get("insubjectpids").toString().startsWith(subject.getPids()))
						.collect(Collectors.toList());
				BigDecimal processSubjectlendinMoneys = subjectProcessLendinList.stream().map(e->new BigDecimal(e.get("lendmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
				
				List<Map<String, Object>> subjectProcessLendoutList = lendDataList.stream().filter(e->yearId.toString().equals(e.get("yearid").toString()) 
						&& unitId.toString().equals(e.get("outunitid").toString())
						&& e.get("outsubjectpids").toString().startsWith(subject.getPids()))
						.collect(Collectors.toList());
				BigDecimal processSubjectlendoutMoneys = subjectProcessLendoutList.stream().map(e->new BigDecimal(e.get("lendmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
				if(subjectLendinmoney.compareTo(processSubjectlendinMoneys) !=0
						|| subjectLendoutmoney.compareTo(processSubjectlendoutMoneys) !=0) {
					if(voMap.get(subject.getId()+"-0")!=null) {
						YearLendDataVO vo = new YearLendDataVO();
						vo.setSubjectName(subject.getName());
						vo.setYearSubjectLendinMoney(subjectLendinmoney);
						vo.setProcessYearSubjectLendinMoney(processSubjectlendinMoneys);
						vo.setYearSubjectLendoutMoney(subjectLendoutmoney);
						vo.setProcessYearSubjectLendoutMoney(processSubjectlendoutMoneys);
						vo.setIsShowHandleButton(true);
						voMap.put(subject.getId()+"-0", vo);
					}
				}
			}
		});
		int total = voMap.values().size();
		List<YearLendDataVO> records = voMap.values().stream().skip((page-1)*rows).limit(rows).collect(Collectors.toList());
		return PageResult.apply(total, records);
	}

	public void handleYearLendExceptionData(Long yearAgentId) {
		BudgetYearAgent yearAgent = yearAgentMapper.selectById(yearAgentId);
		List<Map<String,Object>> processLendList = sysMapper.getUnitYearLendData(yearAgent.getUnitid());
		handleYearLend(yearAgent.getUnitid(),yearAgent.getSubjectid(),processLendList);
		BigDecimal lendinmoney = processLendList.stream().filter(e->e.get("inyearagentid").toString().equals(yearAgentId.toString())).map(e->new BigDecimal(e.get("lendmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
		yearAgent.setLendinmoney(lendinmoney);
		BigDecimal lendoutmoney = processLendList.stream().filter(e->e.get("outyearagentid").toString().equals(yearAgentId.toString())).map(e->new BigDecimal(e.get("lendmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
		yearAgent.setLendoutmoney(lendoutmoney);
		yearAgentMapper.updateById(yearAgent);
	}

	private void handleYearLend(Long unitid, Long subjectid, List<Map<String, Object>> processLendList) {
		if(subjectid.toString().equals("0")) return;
		BudgetYearSubject bys = this.yearSubjectMapper.selectOne(new QueryWrapper<BudgetYearSubject>().eq("unitid", unitid).eq("subjectid", subjectid));
		BudgetSubject subject = this.subjectMapper.selectById(subjectid);
		BigDecimal subjectLendinmoney = processLendList.stream().filter(e-> e.get("inunitid").toString().equals(unitid.toString())  && ("-"+e.get("insubjectpids").toString()+"-").contains("-"+subjectid.toString()+"-")).map(e->new BigDecimal(e.get("lendmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
		BigDecimal subjectLendoutmoney = processLendList.stream().filter(e-> e.get("outunitid").toString().equals(unitid.toString())  && ("-"+e.get("outsubjectpids").toString()+"-").contains("-"+subjectid.toString()+"-")).map(e->new BigDecimal(e.get("lendmoney").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
		bys.setLendinmoney(subjectLendinmoney);
		bys.setLendoutmoney(subjectLendoutmoney);
		this.yearSubjectMapper.updateById(bys);
		handleYearLend(bys.getUnitid(),subject.getParentid(),processLendList);
	}
	
}
