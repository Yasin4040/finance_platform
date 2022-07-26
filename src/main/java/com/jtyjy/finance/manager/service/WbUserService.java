package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.utils.SysUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@JdbcSelector(value = "defaultJdbcTemplateService")
public class WbUserService extends DefaultBaseService<WbUserMapper, WbUser> {

    @Autowired
    private  TabChangeLogMapper loggerMapper;

    @Autowired
    private WbUserMapper wuMapper;

    @Autowired
    private  WbDeptMapper dpMapper;
    @Autowired
    private  BudgetUnitMapper unitMapper;
    @Autowired
    private  WbUserService userService;
    @Autowired
    private  WbPersonService personService;
    @Autowired
    private WbPersonMapper personMapper;
    @Autowired
    private  WbDeptService deptService;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        //DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("wb_user"));
    }

    /**
     * 分页查询用户信息
     *
     * @param displayName 用户名称（模糊查询）
     * @param userName    工号（模糊查询）
     * @param page
     * @param rows
     * @return
     */
    public Page<WbUser> getUserPageInfo(String displayName, Long unitId, Integer page, Integer rows) {
        Page<WbUser> pageCond = new Page<>(page, rows);
        if (null == unitId) {
            List<WbUser> resultList = this.wuMapper.getUserPageInfo(pageCond, displayName, JdbcSqlThreadLocal.get());
            pageCond.setRecords(resultList);
        } else {
            BudgetUnit unitInfo = this.unitMapper.selectById(unitId);
            if (null != unitInfo) {
                String budgetdepts = unitInfo.getBudgetdepts();
                String budgetusers = unitInfo.getBudgetusers();
                if (StringUtils.isNotBlank(budgetdepts)) {
                    StringJoiner sj = new StringJoiner("','", "'", "'");
                    for (String deptId : budgetdepts.split(",")) {
                        //获取自身和所有子级部门
                        List<WbDept> deptList = this.dpMapper.selectList(new QueryWrapper<WbDept>().like("PARENT_IDS", deptId));
                        if (null == deptList || deptList.size() == 0) continue;
                        for (WbDept dept : deptList) {
                            sj.add(dept.getDeptId());
                        }
                    }
                    budgetdepts = sj.toString();
                } else {
                    budgetdepts = "null";
                }
                if (StringUtils.isNotBlank(budgetusers)) {
                    StringJoiner sj = new StringJoiner("','", "'", "'");
                    for (String deptId : budgetusers.split(",")) {
                        sj.add(deptId);
                    }
                    budgetusers = sj.toString();
                } else {
                    budgetusers = "null";
                }
                List<WbUser> resultList = this.wuMapper.getUserPageInfoByUnit(pageCond, displayName, budgetdepts, budgetusers, JdbcSqlThreadLocal.get());
                pageCond.setRecords(resultList);
            }
        }

        return pageCond;
    }

    /**
     * 按照主键查询
     *
     * @param idList
     * @return
     */
    public List<WbUser> selectByIds(List<String> idList) {
        QueryWrapper<WbUser> wrapper = new QueryWrapper<WbUser>();
        wrapper.in("user_id", idList);
        wrapper.eq("status", 1);
        return this.list(wrapper);
    }

    public WbUser getByEmpNo(String empNo) {
        QueryWrapper<WbUser> wrapper = new QueryWrapper<WbUser>();
        wrapper.eq("USER_NAME", empNo);
        return this.getOne(wrapper);
    }

    /**
     * 验证用户工号和姓名是否正确
     */
    public WbUser validateUser(String empNo, String empName) {
        WbUser user = this.wuMapper.selectOne(new QueryWrapper<WbUser>().eq("user_name", empNo));
        if (user == null) {
            throw new RuntimeException("工号【" + empNo + "】不存在");
        } else if (!user.getDisplayName().equals(empName)) {
            throw new RuntimeException("工号【" + empNo + "】与名称【" + empName + "】不匹配, 正确名称为【" + user.getDisplayName() + "】");
        } else if (BigDecimal.ZERO.compareTo(user.getStatus()) == 0) {
            throw new RuntimeException("员工【" + empName + "(" + empNo + ")】已离职");
        }
        return user;
    }

    public void syncUser1(List<Map<String, Object>> hrDeptList,List<WbDept> budgetDeptList,List<Map<String,Object>> hrUserList) {
        Map<String, WbDept> budgetDeptMap = budgetDeptList.stream().collect(Collectors.toMap(WbDept::getOutKey, e -> e, (e1, e2) -> e1));
        syncDept(hrDeptList,"3",budgetDeptMap);
        try{
            userSync(budgetDeptMap,hrUserList);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw e;
        }
    }

    private void userSync(Map<String, WbDept> budgetDeptMap,List<Map<String,Object>> hrUserList){
        Map<String, WbUser> wbUserMap = userService.list(null).stream().collect(Collectors.toMap(WbUser::getUserName, e -> e, (e1, e2) -> e1));
        Map<String, WbPerson> wbPersonMap = personService.list(null).stream().collect(Collectors.toMap(WbPerson::getPersonCode, e -> e, (e1, e2) -> e1));
        List<WbUser> updateUserList = new ArrayList<>();
        List<WbPerson> undatePersonList = new ArrayList<>();
        hrUserList.forEach(e->{
            String empNo = e.get("empNo").toString();
            String deptId = e.get("deptId").toString();
            WbUser wbuser;
            if(wbUserMap.get(empNo) == null){
                wbuser = new WbUser();
                wbuser.setCreateDate(new Date());
                wbuser.setLoginTimes(BigDecimal.ZERO);
                String useStatus = e.get("useStatus").toString();
                wbuser.setStatus("0".equals(useStatus)?new BigDecimal("1"):BigDecimal.ZERO);
                wbuser.setOutkey(empNo);
                wbuser.setUserId(SysUtil.getId());
                if (null != e.get("certifiCateNo")) {
                    wbuser.setIdNumber(e.get("certifiCateNo").toString());
                } else {
                    wbuser.setIdNumber("");
                }
                wbuser.setUserName(empNo);
                String password = getPassword(e);
                if (org.apache.commons.lang3.StringUtils.isEmpty(password)) {
                    password = "123456";
                }
                wbuser.setPassword(password);
                String displayname = "";
                if (null != e.get("empName")) {
                    displayname = e.get("empName").toString();
                }
                wbuser.setDisplayName(displayname);
                userService.save(wbuser);
                wbUserMap.put(empNo,wbuser);
            }else{
                wbuser = wbUserMap.get(empNo);
                String useStatus = e.get("useStatus").toString();
                wbuser.setStatus("0".equals(useStatus)?new BigDecimal("1"):BigDecimal.ZERO);
                if (null != e.get("certifiCateNo")) {
                    wbuser.setIdNumber(e.get("certifiCateNo").toString());
                } else {
                    wbuser.setIdNumber("");
                }
                String displayname = "";
                if (null != e.get("empName")) {
                    displayname = e.get("empName").toString();
                }
                String password = getPassword(e);
                if (org.apache.commons.lang3.StringUtils.isEmpty(password)) {
                    password = "123456";
                }
                wbuser.setPassword(password);
                wbuser.setDisplayName(displayname);
                updateUserList.add(wbuser);
            }

            WbPerson person;
            WbDept wbDept = budgetDeptMap.get(deptId);
            if(wbDept == null) return;
            if(wbPersonMap.get(empNo)==null){
                person = new WbPerson();
                person.setUserId(wbuser.getUserId());
                person.setPersonName(wbuser.getDisplayName());
                person.setDeptId(wbDept.getDeptId());
                person.setPersonCode(wbuser.getUserName());
                person.setPersonId(SysUtil.getId());
                //性别
                String sex = e.get("sex").toString();
                person.setSex(sex);
                //手机号
                if(e.get("mobileTel")!=null){
                    String mobilephone = e.get("mobileTel").toString();
                    person.setMobilePhone(mobilephone);
                }
                personService.save(person);
                wbPersonMap.put(empNo,person);
            }else{
                person = wbPersonMap.get(empNo);
                person.setDeptId(wbDept.getDeptId());
                person.setPersonCode(wbuser.getUserName());
                //性别
                String sex = e.get("sex").toString();
                person.setSex(sex);
                //手机号
                if(e.get("mobileTel")!=null){
                    String mobilephone = e.get("mobileTel").toString();
                    person.setMobilePhone(mobilephone);
                }
                undatePersonList.add(person);
            }
        });
        if(!CollectionUtils.isEmpty(updateUserList)){
            userService.updateBatchById(updateUserList);
        }
        if(!CollectionUtils.isEmpty(undatePersonList)){
            personService.updateBatchById(undatePersonList);
        }
    }

    private void syncDept(List<Map<String, Object>> hrDeptList, String deptId, Map<String, WbDept> budgetDeptMap) {
        hrDeptList.stream().filter(e -> e.get("parentId").toString().equals(deptId)).forEach(cDept -> {
            addDept1(cDept, budgetDeptMap, hrDeptList);
        });
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
    private void addDept1(Map<String, Object> hrDept, Map<String, WbDept> budgetDeptMap, List<Map<String, Object>> hrDeptList) {
        String deptId = hrDept.get("deptId").toString();
        WbDept wbDept;
        boolean isAdd = false;
        if (budgetDeptMap.get(deptId) == null) {
            wbDept = new WbDept();
            wbDept.setOutKey(deptId);
            wbDept.setDeptId(SysUtil.getId());
            wbDept.setOrderIndex(new BigDecimal("0"));
            isAdd = true;
        } else {
            wbDept = budgetDeptMap.get(deptId);
        }
        String useStatus = hrDept.get("useStatus").toString();
        wbDept.setDeptFullname(hrDept.get("fullDeptName").toString());
        wbDept.setStatus("0".equals(useStatus) ? new BigDecimal("1") : BigDecimal.ZERO);
        if("3".equals(hrDept.get("parentId").toString())){
            wbDept.setParentDept("0");
            wbDept.setParentIds(wbDept.getDeptId().concat("-"));
        }else{
            WbDept parentDept = budgetDeptMap.get(hrDept.get("parentId").toString());
            wbDept.setParentDept(parentDept.getDeptId());
            wbDept.setParentIds(parentDept.getParentIds() + wbDept.getDeptId().concat("-"));
        }
        String deptName = hrDept.get("deptName").toString();
        wbDept.setDeptName(deptName);
        if (isAdd) {
            deptService.save(wbDept);
            budgetDeptMap.put(deptId, wbDept);
        } else {
            deptService.updateById(wbDept);
        }
        syncDept(hrDeptList, deptId, budgetDeptMap);
    }

}
