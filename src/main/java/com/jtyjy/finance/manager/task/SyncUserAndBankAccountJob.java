package com.jtyjy.finance.manager.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iamxiongx.util.message.DateUtil;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.service.*;
import com.jtyjy.finance.manager.utils.SysUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SyncUserAndBankAccountJob {

	private final static String nomalStatus = "1,2,3,4";

	@Autowired
	private HrService hrService;

	@Autowired
	private WbUserService userService;

	@Autowired
	private WbPersonService personService;

	@Autowired
	private WbDeptService deptService;

	@Autowired
	private BudgetBankAccountService bankAccountService;

	@Autowired
	private WbBanksService bankService;

	@XxlJob("syncUserAndBankAccountJob")
	public ReturnT<String> syncUserAndBankAccountJob(String param) throws Exception {
		log.info("同步用户和银行账号定时器==================START==============================");
		List<WbDept> budgetDeptList = deptService.list(new LambdaQueryWrapper<WbDept>().isNotNull(WbDept::getOutKey));
		List<Map<String, Object>> hrDeptList = hrService.getSyncDeptList();
		List<Map<String,Object>> hrUserList = hrService.getHrUserList();
		userService.syncUser1(hrDeptList,budgetDeptList,hrUserList);
		//syncUser(); //同步用户
		try{
		//同步银行账户
			syncBankAccount();
		}catch(Exception e) {e.printStackTrace();}
		log.info("同步用户和银行账号定时器==================END==============================");
		return ReturnT.SUCCESS;
	}

	/**
	 * 同步HR用户。只做新增，不做修改。
	 * 只同步对内账户
	 */
	private void syncBankAccount() {
		List<Map<String, Object>> hrBankAccountList = hrService.getSyncBankAccountList();
		if (CollectionUtils.isEmpty(hrBankAccountList)) return;
		List<BudgetBankAccount> budgetBankAccountList = bankAccountService.list(new LambdaQueryWrapper<BudgetBankAccount>().eq(BudgetBankAccount::getStopflag,0));
		Map<String, List<BudgetBankAccount>> empMap = budgetBankAccountList.stream().collect(Collectors.groupingBy(BudgetBankAccount::getCode));
		Map<String, List<BudgetBankAccount>> outKeyMap = budgetBankAccountList.stream().filter(e -> StringUtils.isNotBlank(e.getOutkey())).collect(Collectors.groupingBy(BudgetBankAccount::getOutkey));
		Map<String, List<BudgetBankAccount>> bankAccountMap = budgetBankAccountList.stream().collect(Collectors.groupingBy(BudgetBankAccount::getBankaccount));
		Map<String, WbBanks> bankMap = bankService.list(null).stream().collect(Collectors.toMap(WbBanks::getSubBranchCode, e -> e, (e1, e2) -> e1));
		List<WbBanks> provinces = bankService.list(null);
		List<WbBanks> citys = bankService.list(null);
		List<BudgetBankAccount> newAccounts = new ArrayList<>();
		List<WbBanks> newBanks = new ArrayList<>();
		for (Map<String, Object> map : hrBankAccountList) {

			if (map.get("empNo") == null) continue;
			if (map.get("unionPayNo") == null) continue;
			String accountType = map.get("accountType") == null ? "1" : map.get("accountType").toString();
			if (!"0".equals(accountType)) continue;
			String isWage = map.get("isWage") == null ? "0" : map.get("isWage").toString();
			//只同步工资卡
			if("0".equals(isWage)) continue;
			String outkey = map.get("id").toString();
			String bankaccount = map.get("account").toString();
			String empno = map.get("empNo").toString();
			String unionpayno = map.get("unionPayNo").toString();
			String empname = map.get("empName").toString();
			String payeename = map.get("payee_name").toString();
			String openbank = map.get("bankName").toString();
			String province = map.get("province").toString();
			String city = map.get("city").toString();
			String banktype = map.get("dicName").toString();
			String usestatus = map.get("useStatus") == null ? "0" : map.get("useStatus").toString();
			WbBanks bank = bankMap.get(unionpayno);
			List<WbBanks> provinceList = provinces.stream().filter(e -> province.equals(e.getProvince())).collect(Collectors.toList());
			List<WbBanks> cityList = citys.stream().filter(e -> city.equals(e.getCity())).collect(Collectors.toList());
			if (Objects.isNull(bank)) {
				//开户行没有
				bank = new WbBanks();
				bank.setSubBranchName(openbank);
				bank.setSubBranchCode(unionpayno);
				bank.setProvinceCode(provinceList.isEmpty() ? "" : provinceList.get(0).getProvinceCode());
				bank.setProvince(province);
				bank.setCityCode(cityList.isEmpty() ? "" : cityList.get(0).getCityCode());
				bank.setCity(city);
				bank.setBankName(banktype);
				bankMap.put(unionpayno, bank);
				newBanks.add(bank);
			}
			List<BudgetBankAccount> outKeyBudgetBankAccounts = outKeyMap.get(outkey);
			if(CollectionUtils.isEmpty(outKeyBudgetBankAccounts)){
				if (bankAccountMap.get(bankaccount) == null || bankAccountMap.get(bankaccount).isEmpty()) {
					//新增
					BudgetBankAccount account = new BudgetBankAccount();
					account.setCode(empno);
					account.setPname(empname);
					account.setAccountname(payeename);
					account.setAccounttype(1);
					account.setBankaccount(bankaccount);
					account.setWagesflag(true);
					account.setBranchcode(unionpayno);
					account.setStopflag(!"0".equals(usestatus));
					account.setOutkey(outkey);
					account.setOrderno(0);
					account.setUpdateTime(new Date());
					account.setUpdateBy("SYSTEM");

					List<BudgetBankAccount> l = new ArrayList<>();
					l.add(account);
					bankAccountMap.put(bankaccount, l);
					newAccounts.add(account);
				}else{
					List<BudgetBankAccount> budgetBankAccounts = bankAccountMap.get(bankaccount);
					BudgetBankAccount account = budgetBankAccounts.get(0);
					account.setCode(empno);
					account.setPname(empname);
					account.setAccountname(payeename);
					account.setAccounttype(1);
					account.setWagesflag(true);
					account.setBranchcode(unionpayno);
					account.setStopflag(!"0".equals(usestatus));
					account.setOutkey(outkey);
					bankAccountService.updateById(account);
					//重复取一条有效的数据
					for (int i = 1; i < budgetBankAccounts.size(); i++) {
						BudgetBankAccount account1 = budgetBankAccounts.get(i);
						account1.setStopflag(true);
						bankAccountService.updateById(account);
					}
				}

			}else{
				BudgetBankAccount account = outKeyBudgetBankAccounts.get(0);
				account.setCode(empno);
				account.setPname(empname);
				account.setAccountname(payeename);
				account.setAccounttype(1);
				account.setBankaccount(bankaccount);
				account.setWagesflag(true);
				account.setBranchcode(unionpayno);
				account.setStopflag(!"0".equals(usestatus));
				account.setOutkey(outkey);
				bankAccountService.updateById(account);

				//重复取一条有效的数据
				for (int i = 1; i < outKeyBudgetBankAccounts.size(); i++) {
					BudgetBankAccount account1 = outKeyBudgetBankAccounts.get(i);
					account1.setStopflag(true);
					bankAccountService.updateById(account);
				}
			}
		}
		if (!newBanks.isEmpty()) bankService.saveBatch(newBanks);
		if (!newAccounts.isEmpty()) bankAccountService.saveBatch(newAccounts);
	}


	private void syncUser() {
		//获取人资系统的用户信息
		List<Map<String, Object>> hrUserList = hrService.getSyncUserList();
		//工号为key
		Map<String, WbUser> oldUserMap = userService.list().stream().collect(Collectors.toMap(WbUser::getUserName, Function.identity()));
		//wbuser.userid为key
		Map<String, WbPerson> oldPersonMap = personService.list().stream().collect(Collectors.toMap(WbPerson::getUserId, Function.identity()));

		Map<String, WbDept> oldDeptmap = new HashMap<>();
		Map<String, WbDept> olddeptMapByOutkey = new HashMap<>();
		List<WbDept> oldDeptList = deptService.list();
		for (WbDept wbDept : oldDeptList) {
			oldDeptmap.put(wbDept.getDeptFullname(), wbDept);
			if (StringUtils.isNotEmpty(wbDept.getOutKey())) {
				olddeptMapByOutkey.put(wbDept.getOutKey(), wbDept);
			}
		}
		List<WbUser> updateUsers = new ArrayList<>();
		List<WbPerson> updatePersons = new ArrayList<>();
		List<WbDept> updateDepts = new ArrayList<>();
		for (Map<String, Object> hrUserMap : hrUserList) {
			String empno = hrUserMap.get("loginid").toString();
			WbUser wbuser = oldUserMap.get(empno);
			wbuser = operateUser(wbuser, hrUserMap, oldUserMap, empno, updateUsers);
			if (null == wbuser) continue;
			String userid = wbuser.getUserId();
			WbDept wbdept = operateDept(hrUserMap, oldDeptmap, olddeptMapByOutkey, updateDepts);
			if (null == wbdept) continue;
			operatePerson(oldPersonMap, hrUserMap, wbuser, wbdept, userid, updatePersons);
		}
		if (!updateUsers.isEmpty()) userService.updateBatchById(updateUsers);
		if (!updatePersons.isEmpty()) personService.updateBatchById(updatePersons);
		if (!updateDepts.isEmpty()) deptService.updateBatchById(updateDepts);
		//更新部门的上下
		updateDeptpids(oldDeptList, "", "", "0");
	}

	private void updateDeptpids(List<WbDept> depts, String pids, String names, String pid) {
		for (WbDept dept : depts) {
			if (dept.getParentDept().equals(pid)) {
				dept.setParentIds(pids + dept.getDeptId() + "-");
				if (StringUtils.isNotEmpty(names)) {
					dept.setDeptFullname(names + "-" + dept.getDeptName());
				} else {
					dept.setDeptFullname(dept.getDeptName());
				}
				deptService.updateById(dept);
				updateDeptpids(depts, dept.getParentIds(), dept.getDeptFullname(), dept.getDeptId());
			}
		}
	}

	private void operatePerson(Map<String, WbPerson> oldPersonMap, Map<String, Object> hrUserMap, WbUser wbuser,
	                           WbDept wbdept, String userid, List<WbPerson> updatePersons) {
		WbPerson wbPerson = oldPersonMap.get(userid);
		if (null == wbPerson) {
			wbPerson = addPerson(hrUserMap, wbuser, wbdept);
			oldPersonMap.put(userid, wbPerson);
		} else {
			boolean updateflag = false;
			if (!wbdept.getDeptId().equals(wbPerson.getDeptId())) {
				wbPerson.setDeptId(wbdept.getDeptId());
				updateflag = true;
			}
			//姓名变化
			String displaynamekey = "lastname";
			if (hrUserMap.containsKey(displaynamekey) && null != hrUserMap.get(displaynamekey)) {
				String displayname = (String) hrUserMap.get(displaynamekey);
				if (!displayname.equals(wbPerson.getPersonName())) {
					wbPerson.setPersonName(displayname);
					updateflag = true;
				}
			}
			//手机号
			String mobilephonekey = "mobile";
			if (hrUserMap.containsKey(mobilephonekey) && null != hrUserMap.get(mobilephonekey)) {
				String mobilephone = hrUserMap.get(mobilephonekey).toString();
				if (!mobilephone.equals(wbPerson.getMobilePhone())) {
					wbPerson.setMobilePhone(mobilephone);
					updateflag = true;
				}
			}
			//性别
			String sexkey = "sex";
			String malestatus = "1";
			if (hrUserMap.containsKey(sexkey) && null != hrUserMap.get(sexkey)) {
				String sex = hrUserMap.get(sexkey).toString();
				if (!sex.equals(wbPerson.getSex())) {
					updateflag = true;
					if (sex.equals(malestatus)) {//男
						wbPerson.setSex("1");
					} else {//女
						wbPerson.setSex("2");
					}
				}
			}
			//出生日期
			String birthdaykey = "birthday";
			if (hrUserMap.containsKey(birthdaykey) && null != hrUserMap.get(birthdaykey)) {
				Object obj = hrUserMap.get(birthdaykey);
				String birthdaystr = "";
				if (obj instanceof Date) {
					birthdaystr = DateUtil.getStrYMDByDate((Date) obj);
				} else {
					birthdaystr = obj.toString();
					birthdaystr = birthdaystr.substring(0, 10);
				}
				Date birthdaydate = DateUtil.getDateYMDByStr(birthdaystr);
				if (null == wbPerson.getBirthdate() || wbPerson.getBirthdate().getTime() != birthdaydate.getTime()) {
					updateflag = true;
					wbPerson.setBirthdate(birthdaydate);
				}
			}
			if (updateflag) {
				updatePersons.add(wbPerson);
			}
		}
	}

	private WbPerson addPerson(Map<String, Object> hrUserMap, WbUser wbuser, WbDept wbdept) {
		WbPerson person = new WbPerson();
		person.setUserId(wbuser.getUserId());
		person.setPersonName(wbuser.getDisplayName());
		person.setDeptId(wbdept.getDeptId());
		person.setPersonCode(wbuser.getUserName());
		person.setPersonId(SysUtil.getId());
		//出生日期
		String birthdaykey = "birthday";
		if (hrUserMap.containsKey(birthdaykey) && null != hrUserMap.get(birthdaykey)) {
			Object obj = hrUserMap.get(birthdaykey);
			String birthdaystr = "";
			if (obj instanceof Date) {
				birthdaystr = DateUtil.getStrYMDByDate((Date) obj);
			} else {
				birthdaystr = obj.toString();
				birthdaystr = birthdaystr.substring(0, 10);
			}
			Date birthdaydate = DateUtil.getDateYMDByStr(birthdaystr);
			person.setBirthdate(birthdaydate);
		}
		//性别
		String sexkey = "sex";
		if (hrUserMap.containsKey(sexkey) && null != hrUserMap.get(sexkey)) {
			String sex = hrUserMap.get(sexkey).toString();
			if (sex.equals("1")) {//男
				person.setSex("1");
			} else {//女
				person.setSex("2");
			}
		}
		//手机号
		String mobilephonekey = "mobile";
		if (hrUserMap.containsKey(mobilephonekey) && null != hrUserMap.get(mobilephonekey)) {
			String mobilephone = hrUserMap.get(mobilephonekey).toString();
			person.setMobilePhone(mobilephone);
		}
		personService.save(person);
		return person;
	}

	private WbDept operateDept(Map<String, Object> hrUserMap, Map<String, WbDept> oldDeptmap, Map<String, WbDept> olddeptMapByOutkey, List<WbDept> updateDepts) {
		String deptname = hrUserMap.containsKey("fullDeptName") ? hrUserMap.get("fullDeptName").toString() : "";
		String deptid = hrUserMap.containsKey("departmentid") ? hrUserMap.get("departmentid").toString() : "";
		WbDept wbdept = olddeptMapByOutkey.get(deptid);
		if (null == wbdept) {
			wbdept = addDept(hrUserMap, oldDeptmap);
			if (null != wbdept) {
				olddeptMapByOutkey.put(deptid, wbdept);
			}
		} else if (StringUtils.isNotEmpty(deptid) && deptid.equals(wbdept.getOutKey())) {
			wbdept.setOutKey(deptid);
			wbdept.setDeptFullname(deptname);
			updateDepts.add(wbdept);
		}
		return wbdept;
	}

	private WbDept addDept(Map<String, Object> hrUserMap, Map<String, WbDept> oldDeptmap) {

		String deptidkey = "departmentid";
		if (!hrUserMap.containsKey(deptidkey) || null == hrUserMap.get(deptidkey)) {
			return null;
		}
		String deptid = hrUserMap.get(deptidkey).toString();
		String deptnamekey = "fullDeptName";
		if (!hrUserMap.containsKey(deptnamekey) || null == hrUserMap.get(deptnamekey)) {
			return null;
		}
		String deptname = hrUserMap.get(deptnamekey).toString();
		WbDept wbDept = new WbDept();
		wbDept.setOutKey(deptid);
		wbDept.setDeptId(SysUtil.getId());
		wbDept.setStatus(new BigDecimal("1"));
		wbDept.setDeptFullname(deptname);
		wbDept.setOrderIndex(new BigDecimal("0"));

		if (deptname.replace("金太阳教育-", "").lastIndexOf("-") < 0) {
			wbDept.setParentDept("0");
			wbDept.setDeptName(deptname);
		} else {
			WbDept pwbDept = oldDeptmap.get(deptname.substring(0, deptname.lastIndexOf("-")));
			if (null == pwbDept) {
				return null;
			}
			wbDept.setDeptName(deptname.substring(deptname.lastIndexOf("-") + 1, deptname.length()));
			wbDept.setParentDept(pwbDept.getDeptId());
			deptService.save(wbDept);
			wbDept.setParentIds(pwbDept.getParentIds().concat(wbDept.getDeptId()).concat("-"));
			deptService.updateById(wbDept);
			oldDeptmap.put(wbDept.getDeptFullname(), wbDept);
			return wbDept;
		}
		deptService.save(wbDept);
		wbDept.setParentIds(wbDept.getDeptId().concat("-"));
		deptService.updateById(wbDept);
		oldDeptmap.put(wbDept.getDeptFullname(), wbDept);
		return wbDept;
	}

	private WbUser operateUser(WbUser wbuser, Map<String, Object> hrUserMap, Map<String, WbUser> oldUserMap, String empno, List<WbUser> updateUsers) {
		if (Objects.isNull(wbuser)) {
			//插入
			wbuser = addUser(hrUserMap);
			if (Objects.nonNull(wbuser)) oldUserMap.put(empno, wbuser);
		} else {
			//更新
			boolean updateflag = false;
			boolean addflag = false;
			String username = wbuser.getUserName();
			String displayname = wbuser.getDisplayName();
			String idnumber = wbuser.getIdNumber();
			if (username.equals(hrUserMap.get("loginid")) && StringUtils.isEmpty(wbuser.getOutkey())) {
				addflag = true;
				wbuser.setUserName(null);
				updateflag = true;
			}
			if (null != getPassword(hrUserMap) && !wbuser.getPassword().equals(getPassword(hrUserMap))) {
				updateflag = true;
				wbuser.setPassword(getPassword(hrUserMap));
			}
			if (!displayname.equals((String) hrUserMap.get("lastname"))) {
				updateflag = true;
				wbuser.setDisplayName((String) hrUserMap.get("lastname"));
			}
			/**
			 * add by minzhq
			 */
			if (hrUserMap.get("certificatenum") != null && !idnumber.equals(hrUserMap.get("certificatenum").toString())) {
				updateflag = true;
				wbuser.setIdNumber(hrUserMap.get("certificatenum").toString());
			}

			if (wbuser.getStatus() != getStatus(hrUserMap)) {
				updateflag = true;
				wbuser.setStatus(getStatus(hrUserMap));
			}
			if (updateflag) {
				updateUsers.add(wbuser);
			}
			if (addflag) {
				wbuser = addUser(hrUserMap);
			}
		}
		return wbuser;
	}

	private WbUser addUser(Map<String, Object> hrUserMap) {
		WbUser wbuser = new WbUser();
		wbuser.setCreateDate(new Date());
		wbuser.setLoginTimes(BigDecimal.ZERO);
		wbuser.setStatus(getStatus(hrUserMap));
		String idval = "";
		if (null != hrUserMap.get("loginid")) {
			idval = hrUserMap.get("loginid").toString();
		}
		//关联hr用户id
		wbuser.setOutkey(idval);
		wbuser.setUserId(SysUtil.getId());
		String loginid = "";
		if (null == hrUserMap.get("loginid")) {
			return null;
		} else {
			loginid = hrUserMap.get("loginid").toString();
		}
		if (null != hrUserMap.get("certificatenum")) {
			wbuser.setIdNumber(hrUserMap.get("certificatenum").toString());
		} else {
			wbuser.setIdNumber("");
		}
		wbuser.setUserName(loginid);
		String password = getPassword(hrUserMap);
		if (StringUtils.isEmpty(password)) {
			password = "123456";
		}
		wbuser.setPassword(password);
		String displayname = "";
		if (null == hrUserMap.get("lastname")) {
			return null;
		} else {
			displayname = hrUserMap.get("lastname").toString();
		}
		wbuser.setDisplayName(displayname);
		userService.save(wbuser);
		return wbuser;
	}

	private String getPassword(Map<String, Object> hrUserMap) {
		String password = "";
		if (null == hrUserMap.get("password")) {
			return null;
		} else {
			password = hrUserMap.get("password").toString();
			password = password.toUpperCase();
		}
		return password;
	}

	private BigDecimal getStatus(Map<String, Object> hrUserMap) {
		String status = "";
		if (null == hrUserMap.get("status")) {
			status = "1";
		} else {
			String tmpstatus = hrUserMap.get("status").toString();
			if ("0".equals(tmpstatus)) {
				status = "1";
			} else {
				status = "0";
			}

			/*if((","+nomalStatus+",").contains(","+tmpstatus+",")) {
				status = "1";
			}else {
				status = "0";
			}*/
		}
		return new BigDecimal(status);
	}
}
