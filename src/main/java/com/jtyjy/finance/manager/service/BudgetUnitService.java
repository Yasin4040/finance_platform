package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.result.KVBean;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.core.tools.AuthUtils;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.cache.UserCache;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.vo.BudgetUnitSubjectVO;
import com.jtyjy.finance.manager.vo.BudgetUnitVO;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JdbcSelector(value = "defaultJdbcTemplateService")
public class BudgetUnitService extends DefaultBaseService<BudgetUnitMapper, BudgetUnit> {

    public static Executor executor = Executors.newFixedThreadPool(10);

    private final TabChangeLogMapper loggerMapper;

    private final BudgetUnitMapper mapper;

    private final BudgetBaseUnitMapper bbuMapper;

    private final BudgetYearPeriodService bypService;

    private final BudgetYearAgentMapper byaMapper;

    private final BudgetYearSubjectMapper bysMapper;

    private final BudgetYearSubjectHisMapper byshMapper;

    private final BudgetMonthStartupMapper bmsMapper;

    private final BudgetMonthEndUnitMapper bmeuMapper;

    private final BudgetUnitProductMapper bupMapper;

    private final BudgetUnitSubjectMapper busMapper;

    private final BudgetUnitSubjectService busService;
    
    private final BudgetSubjectMapper bsMapper;

    private final TabDmMapper tdMapper;

    private final WbUserMapper wbUserMapper;

    private final BudgetMonthSubjectService budgetMonthSubjectService;

    private final MessageSender messageSender;

    private final BudgetYearPeriodMapper budgetYearPeriodMapper;

    private final BudgetMonthAgentMapper monthAgentMapper;

    private final BudgetProductCategoryService budgetProductCategoryService;


    public ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_unit"));
    }

    public List<BudgetUnitVO> getBudgetUnit(Integer yearId, String unitName, boolean haveAuth) {

        Long year;
        // 届别为空默认获取当前届别
        if (yearId == null) {
            BudgetYearPeriod nowYear = this.bypService.getNowYearPeriod();
            year = nowYear.getId();
        } else {
            year = Long.valueOf(yearId);
        }
        List<BudgetUnitVO> retList = null;
        if (haveAuth) {
            retList = this.mapper.getBudgetUnit(year, unitName, JdbcSqlThreadLocal.get(), UserThreadLocal.get().getUserId());
        } else {
            retList = this.mapper.getBudgetUnitNoAuth(year, unitName);
        }
        //获取所有人员信息（减少数据库查询频率）
        Map<String, Map<String, Object>> userNameMap = this.mapper.queryAllUserName();
        //获取所有部门信息（减少数据库查询频率）
        Map<String, Map<String, Object>> deptNameMap = this.mapper.queryAllDeptName();

        for (BudgetUnitVO vo : retList) {
            if (null != vo.getUnitType()) {
                TabDm dm = this.tdMapper.selectOne(new QueryWrapper<TabDm>().eq("dm_type", "unitType").eq("dm", vo.getUnitType().toString()));
                vo.setUnitTypeName(null == dm ? null : dm.getDmName());
            }
            vo.setAccountingCode(getSplitValue(vo.getAccounting(), userNameMap, "USER_NAME"));
            vo.setAccountingName(getSplitValue(vo.getAccounting(), userNameMap, "DISPLAY_NAME"));
            vo.setManagersCode(getSplitValue(vo.getManagers(), userNameMap, "USER_NAME"));
            vo.setBudgetUsersName(getSplitValue(vo.getBudgetUsers(), userNameMap, "DISPLAY_NAME"));
            vo.setBudgetUsersCode(getSplitValue(vo.getBudgetUsers(), userNameMap, "USER_NAME"));
            vo.setBudgetDeptsName(getSplitValue(vo.getBudgetDepts(), deptNameMap, "DEPT_NAME"));

            if(StringUtils.isNotBlank(vo.getBudgetResponsibilities())){
                String names = Arrays.asList(vo.getBudgetResponsibilities().split(",")).stream().map(e -> UserCache.getUserByEmpNo(e).getDisplayName()).collect(Collectors.joining(","));
                vo.setBudgetResponsibilitienames(names);
            }
        }
        return retList;
    }

    private String getSplitValue(String splits, Map<String, Map<String, Object>> hashMap, String key) {
        if (StringUtils.isNotBlank(splits)) {
            List<String> list = new ArrayList<>();
            for (String id : splits.split(",")) {
                Map<String, Object> objectMap = hashMap.get(id);
                if (objectMap != null) {
                    list.add(objectMap.get(key).toString());
                }
            }
            return String.join(",", list);
        }
        return "";
    }

    public void addUnit(BudgetUnit unitinfo) {
        unitinfo.setCreatetime(new Date());
        unitinfo.setParentid(0L);
        unitinfo.setSubmitflag(false);
        unitinfo.setRequeststatus(0);
        this.mapper.insert(unitinfo);
        unitinfo.setPids(unitinfo.getId() + "-");
        this.mapper.updateById(unitinfo);
    }

    public void updateUnit(BudgetUnit unitinfo) {
        unitinfo.setUpdatetime(new Date());
        this.mapper.updateById(unitinfo);
    }
    
    /**
     * 移动预算单位
     *
     * @param id  要移动的id
     * @param pid 新的父级id（最外层为0）
     * @return
     */
    public String moveUnit(Long id, Long pid) {
        if (null == id || null == pid) {
            return "id和pid不能为空";
        }
        if (id.equals(pid)) {
            return "id和pid不能为同一个";
        }
        if (0 == pid) {//最外层
            BudgetUnit originalBean = this.mapper.selectById(id);
            originalBean.setParentid(0l);
            originalBean.setPids(id + "-");
            this.mapper.updateById(originalBean);
            return "";
        } else {
            BudgetUnit originalBean = this.mapper.selectById(id);
            BudgetUnit fatherBean = this.mapper.selectById(pid);
            if (null == originalBean || null == fatherBean) {
                return "id或pid无效";
            }
            List<BudgetYearAgent> agentList = this.byaMapper.selectList(new QueryWrapper<BudgetYearAgent>().eq("unitid", pid));
            if (null != agentList && agentList.size() > 0) {
                return "预算单位【" + fatherBean.getName() + "】已做预算，不允许作为父单位";
            }
            List<BudgetUnitSubjectVO> unitSubjectList = this.queryUnitSubject(pid);
            for (BudgetUnitSubjectVO unitSubVO : unitSubjectList) {
                if (unitSubVO.getChecked()) {
                    return "预算单位【" + fatherBean.getName() + "】已设置科目，不允许作为父单位";
                }
            }
            List<BudgetUnitProduct> unitProductList = this.bupMapper.selectList(new QueryWrapper<BudgetUnitProduct>().eq("unitid", pid));
            ;
            if (null != unitProductList && unitProductList.size() > 0) {
                return "预算单位【" + fatherBean.getName() + "】已设置产品，不允许作为父单位";
            }
            originalBean.setParentid(pid);
            originalBean.setPids(fatherBean.getPids() + id + "-");
            updateSonPids(originalBean);
            this.mapper.updateById(originalBean);
            return "";
        }
    }

    /**
     * 递归更新子级
     *
     * @param pc
     */
    private void updateSonPids(BudgetUnit pc) {
        List<BudgetUnit> units = this.mapper.selectList(new QueryWrapper<BudgetUnit>().eq("parentid", pc.getId()));
        if (null != units && units.size() > 0) {
            for (BudgetUnit tmpPc : units) {
                tmpPc.setPids(pc.getPids() + tmpPc.getId() + "-");
                this.mapper.updateById(tmpPc);
                updateSonPids(tmpPc);
            }
        }
    }

    public Boolean deleteUnit(String ids, StringBuffer errMsg) {
        if (StringUtils.isBlank(ids)) {
            errMsg.append("ids不能为空！");
            return false;
        }
        List<Long> subIds = new ArrayList<>();
        for (String id : ids.split(",")) {
            BudgetUnit unit = this.getById(Long.valueOf(id));
            if (null != unit) {
                subIds.add(unit.getId());
                iteratQueryUnit(unit.getId(), subIds);
            } else {
                errMsg.append("预算单位id【" + id + "】不存在！");
                return false;
            }
        }

        for (Long id : subIds) {
            BudgetUnit unit = this.getById(id);
            List<BudgetUnitSubjectVO> unitSubjectList = this.queryUnitSubject(id);
            for (BudgetUnitSubjectVO unitSubVO : unitSubjectList) {
                if (unitSubVO.getChecked()) {
                    errMsg.append("预算单位【" + unit.getName() + "】已设置科目，不允许删除");
                    return false;
                }
            }
            List<BudgetUnitProduct> unitProductList = this.bupMapper.selectList(new QueryWrapper<BudgetUnitProduct>().eq("unitid", id));
            if (null != unitProductList && unitProductList.size() > 0) {
                errMsg.append("预算单位【" + unit.getName() + "】已设置产品，不允许删除");
                return false;
            }
            List agentList = this.byaMapper.selectList(new QueryWrapper<BudgetYearAgent>().eq("yearid", unit.getYearid()).eq("unitid", unit.getId()));
            if (null != agentList && agentList.size() > 0) {
                errMsg.append("【" + unit.getName() + "】该预算单位已做预算，不能删除！");
                return false;
            } else {
                this.bmeuMapper.delete(new QueryWrapper<BudgetMonthEndUnit>().eq("unitid", id));
                this.bupMapper.delete(new QueryWrapper<BudgetUnitProduct>().eq("unitid", unit.getId()));
                this.busMapper.delete(new QueryWrapper<BudgetUnitSubject>().eq("unitid", unit.getId()));
                this.mapper.deleteById(unit.getId());

            }
        }
        return true;
    }

    /**
     * @param unitId 预算单位id
     * @return
     */
    public List<BudgetUnitSubjectVO> queryUnitSubject(Long unitId) {

        BudgetUnit unit = this.mapper.selectById(unitId);
        List<Map<String, Object>> subjectlist = this.mapper.getUnitSubByUnit(unitId);

        List<String> subjectidlist = new ArrayList<>();
        Map<String, Map<String, Object>> subjectmap = new HashMap<>();
        for (Map<String, Object> subject : subjectlist) {
            subjectidlist.add(subject.get("subjectid").toString());
            subjectmap.put(subject.get("subjectid").toString(), subject);
        }
        List<BudgetUnitSubjectVO> unitsubjectlist = this.mapper.getSubInfoByYear(unit.getYearid());
        for (BudgetUnitSubjectVO unitsubject : unitsubjectlist) {
            String id = unitsubject.getSubid().toString();
            unitsubject.setChecked(subjectidlist.contains(id));
            Map<String, Object> tmp = subjectmap.get(id);
            if (null != tmp) {
                if (null != tmp.get("yearcontrolflag")) {
                    unitsubject.setYearcontrolflag((Boolean) tmp.get("yearcontrolflag"));
                }
                if (null != tmp.get("lendflag") || null == unitsubject.getLendflag()) {
                    unitsubject.setLendflag((Boolean) tmp.get("lendflag"));
                }
                if (null != tmp.get("revenueformula")) {
                    unitsubject.setRevenueformula((String) tmp.get("revenueformula"));
                    if(StringUtils.isNotBlank(unitsubject.getRevenueformula()))unitsubject.setShowRevenueformula(unitsubject.getRevenueformula().replace("[this]",unitsubject.getName()));
                }
                if (null != tmp.get("preccratioformula")) {
                    unitsubject.setPreccratioformula((String) tmp.get("preccratioformula"));
                }
                if (null != tmp.get("ccratioformula")) {
                    unitsubject.setCcratioformula((String) tmp.get("ccratioformula"));
                    if(StringUtils.isNotBlank(unitsubject.getCcratioformula()))unitsubject.setShowCcratioformula(unitsubject.getCcratioformula().replace("[this]",unitsubject.getName()));
                }
                if (null != tmp.get("formula") || null == unitsubject.getFormula()) {
                    unitsubject.setFormula((String) tmp.get("formula"));
                    if(StringUtils.isNotBlank(unitsubject.getFormula()))unitsubject.setShowFormula(unitsubject.getFormula().replace("[this]",unitsubject.getName()));
                }
                if (null != tmp.get("hidden")) {
                    unitsubject.setHidden((Boolean) tmp.get("hidden"));
                }
                if (null != tmp.get("monthcontrolflag")) {
                    unitsubject.setMonthcontrolflag((Boolean) tmp.get("monthcontrolflag"));
                }
                if (null != tmp.get("yearsubjectcontrolflag")) {
                    unitsubject.setYearsubjectcontrolflag((Boolean) tmp.get("yearsubjectcontrolflag"));
                }
                if (null != tmp.get("splitflag")) {
                    unitsubject.setSplitflag((Boolean) tmp.get("splitflag"));
                }
                if (null != tmp.get("addflag") || null == unitsubject.getAddflag()) {
                    unitsubject.setAddflag((Boolean) tmp.get("addflag"));
                }
            } else {
                unitsubject.setMonthcontrolflag(true);
                unitsubject.setYearcontrolflag(true);
                unitsubject.setYearsubjectcontrolflag(true);
                if(StringUtils.isNotBlank(unitsubject.getCcratioformula()))unitsubject.setShowCcratioformula(unitsubject.getCcratioformula().replace("[this]",unitsubject.getName()));
                if(StringUtils.isNotBlank(unitsubject.getRevenueformula()))unitsubject.setShowRevenueformula(unitsubject.getRevenueformula().replace("[this]",unitsubject.getName()));
                if(StringUtils.isNotBlank(unitsubject.getFormula()))unitsubject.setShowFormula(unitsubject.getFormula().replace("[this]",unitsubject.getName()));
            }
            if (!subjectidlist.contains(id)) {
                if (!unitsubject.getFormulaflag()) {
                    //非公式科目清空计算公式
                    unitsubject.setFormula("");
                }
                unitsubject.setCcratioformula("");
                unitsubject.setRevenueformula("");
                unitsubject.setPreccratioformula("");
            }
        }

        // 构建树结构
        return unitsubjectlist;
    }


    public void updateunitsubject(String unitId, List<BudgetUnitSubjectVO> datas) {
        //预算单位
        BudgetUnit unit = this.mapper.selectById(Long.valueOf(unitId));
        if (null == unit) {
            throw new RuntimeException("预算单位id不存在");
        }
        List<BudgetUnit> haveSon = this.mapper.selectList(new QueryWrapper<BudgetUnit>().eq("parentid", unit.getId()));
        if (null != haveSon && haveSon.size() > 0) {
            throw new RuntimeException("预算单位【" + unit.getName() + "】存在子单位，无法设置科目");
        }
        String subIds = "";
        List<Long> tmpIdList = new ArrayList<>();

        if (null == datas || datas.isEmpty()) {
            subIds = "0";
            tmpIdList.add(0l);
        } else {
            for (BudgetUnitSubjectVO data : datas) {
                String tmpid = data.getSubid().toString();
                subIds += data.getSubid() + ",";
                tmpIdList.add(data.getSubid());
            }
            if (StringUtils.isNotBlank(subIds)) {
                subIds = subIds.substring(0, subIds.length() - 1);
            }

        }
        String uncheckedIds = "";//取消选中的id字符串
//        List<BudgetUnitSubject> orgCheckedList = this.busMapper.selectList(new QueryWrapper<BudgetUnitSubject>().eq("unitid", unitId));
//        for (BudgetUnitSubject unitSub : orgCheckedList) {
//            String subId = unitSub.getSubjectid().toString();
//            orgCheckedMap.put(subId, unitSub);
//            if (!newCheckedMap.containsKey(subId)) {//取消选中的
//                uncheckedIds += subId + ",";
//            }
//        }
        List<Long> updateIdList = new ArrayList<>();
        List<BudgetUnitSubject> orgCheckedList = this.busMapper.selectList(new QueryWrapper<BudgetUnitSubject>().eq("unitid", unitId));
        List<Long> orgIdList = new ArrayList<>();//原选中集合
        orgCheckedList.stream().forEach(tmpSub -> orgIdList.add(tmpSub.getSubjectid()));
        List<BudgetUnitSubjectVO> unitsubjectlist = this.mapper.getSubInfoByYear(unit.getYearid());
        for (BudgetUnitSubjectVO unitsubject : unitsubjectlist) {
            Long subId = unitsubject.getSubid();
            if (orgIdList.contains(subId)) {
                if (tmpIdList.contains(subId)) {
                    //仍选中，更新
                    updateIdList.add(subId);
                }else {
                    //原选中，现取消
                    uncheckedIds += subId + ",";
                }
            }
        }
        if (StringUtils.isNotBlank(uncheckedIds)) {
            uncheckedIds = uncheckedIds.substring(0, uncheckedIds.length() - 1);


            List<String> unCheckedSubjectIds = Arrays.asList(uncheckedIds.split(","));

            //取消勾选的产品科目ids
            List<String> cancelProductSubjectIds = unCheckedSubjectIds.stream().filter(e -> {
                BudgetSubject budgetSubject = bsMapper.selectById(e);
                return budgetSubject.getJointproductflag() != null && budgetSubject.getJointproductflag();
            }).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(cancelProductSubjectIds)){

                List<BudgetYearAgent> budgetYearAgents = byaMapper.selectList(new LambdaQueryWrapper<BudgetYearAgent>().eq(BudgetYearAgent::getUnitid, unit.getId()).in(BudgetYearAgent::getSubjectid, cancelProductSubjectIds));
                List<BudgetMonthAgent> budgetMonthAgents = monthAgentMapper.selectList(new LambdaQueryWrapper<BudgetMonthAgent>().eq(BudgetMonthAgent::getUnitid, unit.getId()).in(BudgetMonthAgent::getSubjectid, cancelProductSubjectIds));

                long count = budgetYearAgents.stream().filter(e -> e.getTotal().compareTo(BigDecimal.ZERO) != 0).count();
                long count1 = budgetMonthAgents.stream().filter(e -> e.getTotal().compareTo(BigDecimal.ZERO) != 0).count();
                if(count == 0 && count1 == 0){
                    //全为0
                    if(unit.getRequeststatus() >= 1){
                        throw new RuntimeException("取消失败！年度预算已提交！");
                    }
                    if(!CollectionUtils.isEmpty(budgetMonthAgents)){
                        monthAgentMapper.deleteBatchIds(budgetMonthAgents.stream().map(BudgetMonthAgent::getId).collect(Collectors.toList()));
                    }
                    if(!CollectionUtils.isEmpty(budgetYearAgents)){
                        byaMapper.deleteBatchIds(budgetYearAgents.stream().map(BudgetYearAgent::getId).collect(Collectors.toList()));
                    }
                    UpdateWrapper<BudgetUnitSubject> wrapper = new UpdateWrapper<>();
                    wrapper.eq("unitid", Long.valueOf(unitId));
                    wrapper.in("subjectid", cancelProductSubjectIds);
                    this.busMapper.delete(wrapper);
                }else{
                    throw new RuntimeException("取消失败！该科目已编制预算！");
                }
            }

            uncheckedIds = unCheckedSubjectIds.stream().filter(e->!cancelProductSubjectIds.contains(e)).collect(Collectors.joining(","));
            if(StringUtils.isNotBlank(uncheckedIds)){
                Map<String, Object> map = this.busMapper.countYearAgent(uncheckedIds, unit.getYearid(), unit.getId());
                Long count = (Long)map.get("sum");
                if (count > 0) {
                    throw new RuntimeException("已存在【" + map.get("agentNames") + "】等年度动因，对应科目不能取消。");
                }
                UpdateWrapper<BudgetUnitSubject> wrapper = new UpdateWrapper<>();
                wrapper.eq("unitid", Long.valueOf(unitId));
                wrapper.in("subjectid", Arrays.asList(uncheckedIds.split(",")));
                this.busMapper.delete(wrapper);
            }

        }
        //获取预算科目
        List<BudgetSubject> mysubjects = this.bsMapper.querySubByIds(subIds);
        Map<String, BudgetSubject> subjectsmap = new HashMap<String, BudgetSubject>();
        for (BudgetSubject subject : mysubjects) {
            subjectsmap.put(subject.getId().toString(), subject);
        }

        for (BudgetUnitSubjectVO vo : datas) {
            if (null == vo) {
                continue;
            }
            BudgetSubject subject = subjectsmap.get(String.valueOf(vo.getSubid()));
            //月度科目控制
            Boolean monthcontrolflag = vo.getMonthcontrolflag();
            //年度动因控制
            Boolean yearcontrolflag = vo.getYearcontrolflag();
            //
            Boolean yearsubjectcontrolflag = vo.getYearsubjectcontrolflag();
            //String procategoryid = null==datamap_.get("procategoryid")?null:datamap_.get("procategoryid").toString();
            String procategoryid = subject.getProcategoryid();
            Boolean hidden = vo.getHidden();
            if (null == hidden) hidden = false;
            Boolean splitflag = vo.getSplitflag();
            if (null == splitflag) splitflag = false;
            //
            if (true == splitflag) {
                //update by minzhq
                //存在多个部门分解同一个科目的情况
                /*long count_ = countEntityManager(" SELECT COUNT(0) FROM budget_unit_subject WHERE unitid!=? AND subjectid=? AND splitflag=1 ", new Object[] {id,myid});
                if(count_ > 0) {
                    BudgetUnit unit_ = (BudgetUnit) oneEntityManager(" SELECT * FROM budget_unit _unit WHERE _unit.id in ( SELECT unitid FROM budget_unit_subject WHERE unitid!=? AND subjectid=? AND splitflag=1 ) ", new Object[] {id,myid}, BudgetUnit.class);
                    BudgetSubject subject_ = (BudgetSubject) oneEntityManagerByFk(Long.valueOf(myid), BudgetSubject.class);
                    throw new RuntimeException("预算单位【"+unit_.getName()+"】已有预算科目【"+subject_.getName()+"】的分解权限。");
                }*/
            }
            //Boolean lendflag = (Boolean) datamap_.get("lendflag");
            Boolean lendflag = subject.getCostlendflag();
            if (null == lendflag) lendflag = false;
            //Boolean addflag = (Boolean) datamap_.get("addflag");
            Boolean addflag = subject.getCostaddflag();
            if (null == addflag) addflag = false;
            String ccratioformula = vo.getCcratioformula();
            if (StringUtils.isBlank(ccratioformula)) {
                //subject.getf
                ccratioformula = unit.getCcratioformula();
            }
            String preccratioformula = ccratioformula;//(String) datamap_.get("preccratioformula");
            String revenueformula = vo.getRevenueformula();
            if (StringUtils.isBlank(revenueformula)) {
                revenueformula = unit.getRevenueformula();
            }
            String formula = vo.getFormula();
            if (StringUtils.isBlank(formula)) {
                formula = subject.getFormula();
            }

            try {
                jse.eval(formulareplace(preccratioformula));
                jse.eval(formulareplace(ccratioformula));
                jse.eval(formulareplace(revenueformula));
                jse.eval(formulareplace(formula));
            } catch (ScriptException e) {
                e.printStackTrace();
                throw new RuntimeException("计算公式有误。");
            }
            BudgetUnitSubject bus = new BudgetUnitSubject();
            bus.setUnitid(Long.valueOf(unitId));
            bus.setSubjectid(vo.getSubid());
            bus.setMonthcontrolflag(monthcontrolflag);
            bus.setProcategoryid(procategoryid);
            bus.setHidden(hidden);
            bus.setSplitflag(splitflag);
            bus.setCcratioformula(ccratioformula);
            bus.setPreccratioformula(preccratioformula);
            bus.setRevenueformula(revenueformula);
            bus.setLendflag(lendflag);
            bus.setAddflag(addflag);
            bus.setYearcontrolflag(yearcontrolflag);
            bus.setFormula(formula);
            bus.setYearsubjectcontrolflag(yearsubjectcontrolflag);
            
            if (updateIdList.contains(vo.getSubid())) {
                UpdateWrapper<BudgetUnitSubject> wrapper = new UpdateWrapper<>();
                wrapper.eq("unitid", Long.valueOf(unitId));
                wrapper.eq("subjectid", vo.getSubid());
                this.busMapper.update(bus, wrapper);
            }else {
                this.busMapper.insert(bus);
            }

        }

        //一级单位
        if (0 == unit.getParentid()) {
            List<Map<String, Object>> subjectlist = this.mapper.getUnitSonSub(unit.getId(), null, unit.getYearid());
            if (null != subjectlist && subjectlist.size() > 0) {
                for (Map<String, Object> subject_ : subjectlist) {
                    String subjectid = subject_.get("subjectid").toString();
                    if (!tmpIdList.contains(Long.valueOf(subjectid))) {
                        tmpIdList.add(Long.valueOf(subjectid));
                    } else {
                        BudgetSubject budgetSubject = subjectsmap.get(subjectid);
                        if (budgetSubject.getLeafflag()) {
                            throw new RuntimeException("所选科目【" + budgetSubject.getName() + "】已经在子预算单位存在。");
                        }
                    }
                }
            }
            QueryWrapper<BudgetYearSubject> delWrapper = new QueryWrapper<>();
            //budget_year_subject
            delWrapper.eq("unitid", unit.getId());
            delWrapper.notIn("subjectid", tmpIdList);
            QueryWrapper<BudgetYearSubjectHis> delHisWrapper = new QueryWrapper<>();
            //budget_year_subject
            delHisWrapper.eq("unitid", unit.getId());
            delHisWrapper.notIn("subjectid", tmpIdList);
            this.bysMapper.delete(delWrapper);
            this.byshMapper.delete(delHisWrapper);

        } else {
            //budget_year_subject
            QueryWrapper<BudgetYearSubject> delWrapper = new QueryWrapper<>();
            delWrapper.eq("unitid", unit.getId());
            delWrapper.notIn("subjectid", tmpIdList);
            this.bysMapper.delete(delWrapper);
            //budget_year_subject_his
            QueryWrapper<BudgetYearSubjectHis> delHisWrapper = new QueryWrapper<>();
            delHisWrapper.eq("unitid", unit.getId());
            delHisWrapper.notIn("subjectid", tmpIdList);
            this.bysMapper.delete(delWrapper);
            this.byshMapper.delete(delHisWrapper);
            //获取减少的科目
            Long punitid = unit.getParentid();
            List<Map<String, Object>> unitSonList = this.mapper.getUnitSonSub(punitid, punitid, unit.getYearid());
            tmpIdList.clear();
            if (null != unitSonList && unitSonList.size() > 0) {
                for (Map<String, Object> subject_ : unitSonList) {
                    String subjectid = subject_.get("subjectid").toString();
                    if (!tmpIdList.contains(Long.valueOf(subjectid))) {
                        tmpIdList.add(Long.valueOf(subjectid));                    }
                }
            }
            delWrapper = new QueryWrapper<>();
            delHisWrapper = new QueryWrapper<>();
            delWrapper.eq("unitid", punitid);
            delWrapper.notIn("subjectid", tmpIdList);
            delHisWrapper.eq("unitid", punitid);
            delHisWrapper.notIn("subjectid", tmpIdList);
            this.bysMapper.delete(delWrapper);
            this.byshMapper.delete(delHisWrapper);

        }

    }

    public List<Map<String, Object>> getProductByUnit(Long unitId, String name, Long cid) {
        List<Map<String, Object>> retList = new ArrayList<>();
        String ids ="";
        if(Objects.isNull(cid)){
            //通过UnitId去获取二类
            ids = budgetProductCategoryService.getPcIds(unitId);
        }else{
            ids = cid.toString();
        }
        List<BudgetProduct> proList = this.mapper.getProductByCid(ids, name);
        List<Long> productids = new ArrayList<>();
        this.bupMapper.selectList(new QueryWrapper<BudgetUnitProduct>().eq("unitid", unitId)).forEach(item -> productids.add(item.getProductid()));


        for (BudgetProduct pro : proList) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", pro.getId());
            map.put("pid", pro.getPids());
            map.put("name", pro.getName());
            map.put("categoryname", pro.getCategoryname());
            map.put("checked", productids.contains(pro.getId()));
            retList.add(map);
        }
        return retList;
    }

    public Boolean setUnitProduct(Long unitid, String protypeid, String proids) {
        BudgetUnit unit = this.getById(unitid);
        if (null == unit) {
            throw new RuntimeException("预算单位id不存在");
        }
        List<BudgetUnit> haveSon = this.mapper.selectList(new QueryWrapper<BudgetUnit>().eq("parentid", unitid));
        if (null != haveSon && haveSon.size() > 0) {
            throw new RuntimeException("预算单位【" + unit.getName() + "】存在子单位，无法设置产品");
        }
        String getProIdSql = "SELECT productid FROM budget_unit_product WHERE unitid=" + unitid + " AND productid IN (SELECT pro.id FROM budget_product AS pro INNER JOIN budget_product_category AS cate ON pro.procategoryid = cate.id WHERE cate.id = " + protypeid + ")";
        List<Long> proIdList = this.jdbcTemplateService.queryForList(getProIdSql, Long.class);
        for (Long productid : proIdList) {
            String productidstr = productid.toString();
            if (StringUtils.isEmpty(proids) || !("," + proids + ",").contains("," + productidstr + ",")) {//取消选择
                String countSql = "select count(0) FROM  budget_year_agent _agent INNER JOIN budget_unit _unit ON _agent.yearid = _unit.yearid AND _agent.unitid = _unit.id WHERE _agent.unitid=" + unitid + "  AND _agent.productid=" + productidstr;
                Integer count = this.jdbcTemplateService.queryForObject(countSql, Integer.class);
                if (count > 0) {
                    throw new RuntimeException("所选产品已经被使用，不能修改。");
                }
                try {
                    UpdateWrapper<BudgetYearAgent> deleteWrapper = new UpdateWrapper<>();
                    deleteWrapper.eq("unitid", unitid);
                    deleteWrapper.eq("productid", productid);
                    this.byaMapper.delete(deleteWrapper);
                } catch (Exception e) {
                    throw new RuntimeException("所选产品已经被使用，不能修改。");
                }
            }
            Map<String, Object> delMap = new HashMap<>();
            delMap.put("unitid", unitid);
            delMap.put("productid", productid);
            this.bupMapper.deleteByMap(delMap);//删除曾选中的
        }
        if (StringUtils.isNotEmpty(proids)) {
            for (String pid : proids.split(",")) {
                BudgetUnitProduct bean = new BudgetUnitProduct();
                bean.setUnitid(unitid);
                bean.setProductid(Long.valueOf(pid));
                this.bupMapper.insert(bean);
            }
        }
        return true;
    }

    /**
     * 迭代预算单位查看是否还有子节点
     */
    private void iteratQueryUnit(Long unitid, List<Long> subIds) {
        List<BudgetUnit> units = this.mapper.selectList(new QueryWrapper<BudgetUnit>().eq("parentid", unitid));
        if (null != units && units.size() > 0) {
            for (BudgetUnit tmpPc : units) {
                subIds.add(tmpPc.getId());
                iteratQueryUnit(tmpPc.getId(), subIds);
            }
        }
    }

    /**
     * 校验数据
     *
     * @param bean
     * @param errMsg 错误信息
     * @return 校验结果
     */
    public Boolean checkData(BudgetUnit bean, StringBuffer errMsg) {
        if (null == bean) {
            errMsg.append("数据不能为空");
            return false;
        }
        if (StringUtils.isBlank(bean.getName())) {
            BudgetBaseUnit baseunit = this.bbuMapper.selectById(bean.getBaseunitid());
            bean.setName(baseunit.getName());
        }
        BudgetUnit sameBaseUnit = this.mapper.selectOne(new QueryWrapper<BudgetUnit>().eq("yearid", bean.getYearid()).eq("baseunitid", bean.getBaseunitid()));
        BudgetUnit sameName = this.mapper.selectOne(new QueryWrapper<BudgetUnit>().eq("yearid", bean.getYearid()).eq("name", bean.getName()));
        
        if (null == bean.getId() || 0 == bean.getId().intValue()) {
            if (null != sameBaseUnit) {
                errMsg.append("此届别已存在相同的基础单位！");
                return false;
            }
            if (null != sameName) {
                errMsg.append("同一届别下预算单位名称不能重复！");
                return false;
            }
        } else {
            if (null != sameBaseUnit && !sameBaseUnit.getId().equals(bean.getId())) {
                errMsg.append("此届别已存在相同的基础单位！");
                return false;
            }
            if (null != sameName && !sameName.getId().equals(bean.getId())) {
                errMsg.append("同一届别下预算单位名称不能重复！");
                return false;
            }
        }
        if (StringUtils.isNotBlank(bean.getCcratioformula())) {
            try {
                jse.eval(formulareplace(bean.getCcratioformula()));
            } catch (ScriptException e) {
                errMsg.append("本届码洋占比公式有误。");
                return false;
            }
        }
        bean.setPreccratioformula(bean.getCcratioformula());
        if (StringUtils.isNotBlank(bean.getRevenueformula())) {
            try {
                jse.eval(formulareplace(bean.getRevenueformula()));
            } catch (ScriptException e) {
                errMsg.append("本届收入占比公式有误。");
                return false;
            }
        }
        return true;
    }

    public final String formulareplace(String formula) {
        String numberstr = "0123456789";
        if (StringUtils.isBlank(formula)) {
            return "";
        } else {
            if (formula.startsWith(".") || formula.endsWith(".")) {
                throw new RuntimeException("计算公式错误");
            }
            for (int i = 0; i < formula.length(); i++) {
                if (".".equals(formula.charAt(i) + "") && (!numberstr.contains((formula.charAt(i + 1) + ""))
                        || !numberstr.contains((formula.charAt(i - 1) + "")))) {
                    throw new RuntimeException("计算公式错误。");
                }
            }
            int index_ = formula.indexOf("[");
            int _index = formula.indexOf("]");
            while (index_ >= 0 && _index >= 0) {
                if (index_ > 1) {
                    String tmp = formula.substring(index_ - 1, index_);
                    if ("]".equals(tmp) || ".".equals(tmp) || numberstr.contains(tmp)) {
                        throw new RuntimeException("计算公式错误。");
                    }
                }
                if (_index < formula.length()) {
                    String tmp = formula.substring(_index, _index + 1);
                    if ("[".equals(tmp) || ".".equals(tmp) || numberstr.contains(tmp)) {
                        throw new RuntimeException("计算公式错误。");
                    }
                }
                String repalcestr = formula.substring(index_, _index + 1);
                formula = formula.replace(repalcestr, "1");
                index_ = formula.indexOf("[");
                _index = formula.indexOf("]");
            }
        }
        return formula;
    }

    /**
     * 查询年度审核列表（分页）
     */
    public PageResult<BudgetUnit> listYearAuditPage(Integer page, Integer rows, Long yearId, String name) {
        Page<BudgetUnit> pageBean = new Page<>(page, rows);
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("yearId", yearId);
        paramMap.put("name", name);
        List<BudgetUnit> resultList = this.mapper.listYearAuditPage(pageBean, paramMap);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * 年度预算审核
     *
     * @param budgetUnitId 预算单位Id
     * @param remark       审核说明
     * @param type         1通过 2退回 3强制退回
     */
    public void yearBudgetAudit(Long budgetUnitId, String remark, int type,WbUser user) throws Exception {
        BudgetUnit budgetUnit = this.mapper.selectById(budgetUnitId);
        if (budgetUnit == null) {
            throw new RuntimeException("预算单位Id错误");
        }
        if (type == 1 && budgetUnit.getRequeststatus() < 1) {
            throw new RuntimeException("审核失败，预算单位【" + budgetUnit.getName() + "】年度预算还未提交");
        } else if (type == 1 && budgetUnit.getRequeststatus() == 2) {
            throw new RuntimeException("审核失败，预算单位【" + budgetUnit.getName() + "】年度预算已审核");
        } else if (type == 2 && budgetUnit.getRequeststatus() < 1) {
            throw new RuntimeException("退回失败，预算单位【" + budgetUnit.getName() + "】年度预算还未提交");
        } else if (type == 2 && budgetUnit.getRequeststatus() == 2) {
            throw new RuntimeException("退回失败，预算单位【" + budgetUnit.getName() + "】年度预算已审核");
        } else if (type == 3 && budgetUnit.getRequeststatus() != 2) {
            throw new RuntimeException("强制退回失败，预算单位【" + budgetUnit.getName() + "】年度预算还未审核通过");
        } else if ((type == 2 || type == 3) && budgetUnit.getRequeststatus() == -1) {
            throw new RuntimeException(type == 2 ? "" : "强制" + "退回失败，预算单位【" + budgetUnit.getName() + "】年度预算已退回");
        }


        // 更新预算单位审核信息
        BudgetUnit updateBudgetUnit = new BudgetUnit();
        updateBudgetUnit.setId(budgetUnitId);
        updateBudgetUnit.setVerifyorid(user.getUserId());
        updateBudgetUnit.setVerifyorname(user.getDisplayName());
        updateBudgetUnit.setVerifytime(new Date());
        if (type == 1) {
            // 通过
            updateBudgetUnit.setRequeststatus(2);
            updateBudgetUnit.setVerifystr("");
        } else {
            // 退回 or 强制退回
            updateBudgetUnit.setRequeststatus(-1);
            updateBudgetUnit.setVerifystr(remark);
        }
        this.mapper.updateById(updateBudgetUnit);

        if (type == 1) {
            // 启动月度预算
            BudgetMonthStartup monthStartUp = this.bmsMapper.getCurrentMonthStartUp(budgetUnit.getYearid());
            if (monthStartUp != null) {
                this.budgetMonthSubjectService.syncMonthAgentData(budgetUnitId, 6L);
            }
        }

        // 发送企业微信消息
        String managers = budgetUnit.getManagers();
        if (StringUtils.isNotBlank(managers)) {
            String userId = managers.split(",")[0];

            WbUser sendUser = this.wbUserMapper.selectById(userId);
            if (sendUser != null) {
                String period = this.budgetYearPeriodMapper.selectById(budgetUnit.getYearid()).getPeriod();
                String message = "【" + period + "】预算单位【" + budgetUnit.getName() + "】";
                switch (type) {
                    case 1:
                        this.messageSender.sendQywxMsg(new QywxTextMsg(sendUser.getUserName(), null, null, 0, message + "年度预算已审核通过", 0));
                        break;
                    case 2:
                        this.messageSender.sendQywxMsg(new QywxTextMsg(sendUser.getUserName(), null, null, 0, message + "年度预算已被退回, 退回原因：" + remark, 0));
                        break;
                    case 3:
                        this.messageSender.sendQywxMsg(new QywxTextMsg(sendUser.getUserName(), null, null, 0, message + "年度预算已被强制退回", 0));
                        break;
                    default:
                }
            }
        }
    }

    /**
     * 通过权限中心配置的权限获取能看见的预算单位id
     *
     * @param authSql
     * @return
     * @author minzhq
     */
    public List<Long> getUnitIdListByAuthCenter(String authSql) {
        List<Long> unitIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(authSql)) {
            List<KVBean> kvBeanList = AuthUtils.getSimpleAuth(authSql);
            if (!CollectionUtils.isEmpty(kvBeanList)) {
                KVBean yearPeriodKvBean = kvBeanList.stream().filter(e -> e.getK().toString().equals("t.year_id")).findFirst().orElse(null);
                if (Objects.nonNull(yearPeriodKvBean) && StringUtils.isNotBlank(yearPeriodKvBean.getV().toString())) {
                    //届别不为空
                    List<String> yearIdList = Arrays.asList(yearPeriodKvBean.getV().toString().split(","));
                    /**
                     * 获取基础单位集合
                     */
                    KVBean baseUnitKvBean = kvBeanList.stream().filter(e -> e.getK().toString().equals("t.base_unit_id")).findFirst().orElse(null);
                    if (Objects.nonNull(baseUnitKvBean) && StringUtils.isNotBlank(baseUnitKvBean.getV().toString())) {
                        List<BudgetUnit> unitList = this.mapper.selectList(null);
                        List<String> baseUnitIdList = Arrays.asList(baseUnitKvBean.getV().toString().split(","));
                        /**
                         * 届别和基础单位都选了才有数据展示
                         */
                        if (!CollectionUtils.isEmpty(yearIdList) && !CollectionUtils.isEmpty(baseUnitIdList)) {
                            List<BudgetUnit> authUnitList = unitList.stream().filter(e -> yearIdList.contains(e.getYearid().toString())
                                    && baseUnitIdList.contains(e.getBaseunitid().toString())).collect(Collectors.toList());
                            /**
                             * 如果选中的是父级的基础单位，也要显示子级的。
                             */
                            authUnitList.forEach(authUnit -> {
                                unitIdList.addAll(unitList.stream().filter(e -> e.getPids().startsWith(authUnit.getPids())).map(e -> e.getId()).collect(Collectors.toList()));
                            });
                            return unitIdList.stream().distinct().collect(Collectors.toList());
                        }
                    }
                }
            }

        }
        return unitIdList;
    }


    /**
     * 通过权限中心配置的权限获取能看见的基础单位id
     *
     * @param authSql
     * @return
     * @author minzhq
     */
    public List<String> getBaseUnitIdListByAuthCenter(String authSql) {
        List<String> baseUnitIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(authSql)) {
            List<KVBean> kvBeanList = AuthUtils.getSimpleAuth(authSql);
            if (!CollectionUtils.isEmpty(kvBeanList)) {
                KVBean baseUnitKvBean = kvBeanList.stream().filter(e -> e.getK().toString().equals("t.base_unit_id")).findFirst().orElse(null);
                if (Objects.nonNull(baseUnitKvBean) && StringUtils.isNotBlank(baseUnitKvBean.getV().toString())) {
                    /**
                     * 获取基础单位集合
                     */
                    return Arrays.asList(baseUnitKvBean.getV().toString().split(","));
                }
            }
        }
        return Lists.newArrayList("0");
    }
    
    
    public void initUnit(Long sourceYearId, Long targetYearId) throws Exception {
        List<BudgetUnitVO> sourceList = this.mapper.queryAllUnitByYearId(sourceYearId);
        List<BudgetUnitVO> targetList = this.mapper.queryAllUnitByYearId(targetYearId);
        List<Long> baseUnitList = new ArrayList<>();
        if (null != targetList && !targetList.isEmpty()) {
            targetList.stream().forEach(target -> baseUnitList.add(target.getBaseUnitId()));
            //throw new Exception("目标届别已存在预算单位，请先删除后再进行初始化！");
        }
        Map<String, BudgetUnitVO> targetUnitMap = new HashMap<>();
        Map<Long, BudgetUnitVO> sourceUnitMap = new LinkedHashMap<>();
        sourceUnitMap = sourceList.stream().collect(Collectors.toMap(BudgetUnitVO::getId, vo -> vo));
        //this.mapper.delete(new UpdateWrapper<BudgetUnit>().eq("yearid", targetYearId));//清空目标届别的预算单位
        List<BudgetUnit> newUnitList = new ArrayList<>();
        Date nowDate = new Date();
        for (BudgetUnitVO vo : sourceList) {
            BudgetUnit bean = null;
            if (baseUnitList.contains(vo.getBaseUnitId())) {
                //目标届别已有此基础单位
                BudgetUnitVO targetVo = targetList.stream().filter(target -> target.getBaseUnitId().equals(vo.getBaseUnitId())).findFirst().get();
                bean = new BudgetUnit(targetVo);
                bean.setParentid(targetVo.getParentId());
                bean.setPids(targetVo.getPids());
            }else {
                bean = new BudgetUnit(vo);
                bean.setId(null);
                bean.setYearid(targetYearId);
                bean.setCreatetime(nowDate);
                bean.setParentid(vo.getParentId());
                bean.setSubmitflag(false);
                bean.setRequeststatus(0);
                bean.setPids(vo.getPids());
            }
            
            newUnitList.add(bean);
        }
        this.saveOrUpdateBatch(newUnitList);
        for (BudgetUnit unit : newUnitList) {
            BudgetUnitVO vo = new BudgetUnitVO();
            vo.setId(unit.getId());
            vo.setParentId(unit.getParentid());
            vo.setName(unit.getName());
            vo.setPids(unit.getPids());
            targetUnitMap.put(vo.getName(), vo);
        }
        List<BudgetUnit> updateUnitList = new ArrayList<>();
        for (BudgetUnit unit : newUnitList) {
            Long id = unit.getId();
            if (0 == unit.getParentid().intValue()) {
                unit.setPids(id + "-");
                targetUnitMap.get(unit.getName()).setPids(id + "-");
                updateUnitList.add(unit);
            }else {
                //获取原届别预算单位的父单位
                BudgetUnitVO sourceParentUnit = sourceUnitMap.get(unit.getParentid());
                if (null != sourceParentUnit) {
                    //获取父单位名称匹配目前届别的父单位
                    String parentName = sourceParentUnit.getName();
                    BudgetUnitVO targetParentUnit = targetUnitMap.get(parentName);
                    
                    //父单位未更新parentid
                    if (0 != targetParentUnit.getParentId().intValue() && targetParentUnit.getParentId().equals(sourceParentUnit.getParentId())) {
                        updateParentUnit(sourceUnitMap, targetUnitMap, unit.getPids());
                    }else {
                        unit.setParentid(targetParentUnit.getId());
                        unit.setPids(targetParentUnit.getPids() + id + "-");
                        targetUnitMap.get(unit.getName()).setParentId(targetParentUnit.getId());
                        targetUnitMap.get(unit.getName()).setPids(targetParentUnit.getPids() + id + "-");
                        updateUnitList.add(unit);
                        
                    }
                }
            }
        }
        this.updateBatchById(updateUnitList);
        
    }
    
    /**
     * 父单位未更新的情况先更新所有父单位
     * @param sourceUnitMap
     * @param targetUnitMap
     * @param oldPid
     */
    public void updateParentUnit(Map<Long, BudgetUnitVO> sourceUnitMap, Map<String, BudgetUnitVO> targetUnitMap, String oldPid){
        
        String[] oldPids = oldPid.split("-");
        List<BudgetUnitVO> newPidList = new ArrayList<>();
        for (int i = 0; i < oldPids.length; i++) {
            Long pid = Long.valueOf(oldPids[i]);
            BudgetUnitVO tmpUnit = sourceUnitMap.get(pid);
            if (null != tmpUnit) {
                BudgetUnitVO targetUnit = targetUnitMap.get(tmpUnit.getName());
                if (0 != targetUnit.getParentId().intValue()) {
                    //最上层已更新不用更新
                    UpdateWrapper<BudgetUnit> wrapper = new UpdateWrapper<>();
                    wrapper.set("parentid", newPidList.get(i - 1).getId());
                    wrapper.set("pids", newPidList.get(i - 1).getPids() + targetUnit.getId() + "-");
                    wrapper.eq("id", targetUnit.getId());
                    this.update(wrapper);
                    targetUnit.setParentId(newPidList.get(i - 1).getId());
                    targetUnit.setPids(newPidList.get(i - 1).getPids() + targetUnit.getId() + "-");
                    
                }
                newPidList.add(targetUnit);
            }
        }
    
    }

    /**
     *获取月度动因信息
     * @return
     */
    public List<BudgetMonthAgent> getMonthAgentInfo(HashMap<String, Object> paramMap) {
        List<BudgetMonthAgent> list= monthAgentMapper.getMonthAgentInfoAsset(paramMap);

        return list;
    }

    public List<Map<String,Object>> getBudgetUnitForAsset(Long yearId) {
        List<Map<String,Object>> list =  mapper.getBudgetUnitForAsset(yearId);
        return list;
    }

    public String batchYearBudgetAudit(String budgetUnitIds,String remark, int type) throws Exception {
        List<Long> unitIds = getBudgetUnitIds(budgetUnitIds);
        StringBuilder builder = new StringBuilder();
        List<CompletableFuture> futures = new ArrayList<>();
        WbUser user = UserThreadLocal.get();
        for(Long budgetUnitId: unitIds){
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                try {
                    this.yearBudgetAudit(budgetUnitId, remark, type,user);
                } catch (Exception e) {
                    builder.append(e.getMessage()).append("\n");
                }
                return 1;
            }, executor);
            futures.add(future);
        }
        for(CompletableFuture future:futures){
            future.get();
        }
        if(builder.length()>0){
            return "以下条目存在问题，请查看：\n"+builder.toString();
        }
        return "";
    }

    private List<Long> getBudgetUnitIds(String budgetUnitIds) throws Exception {
        if(StringUtils.isBlank(budgetUnitIds)){
            throw new Exception("预算单位不能为空");
        }
        String[] budgetUnitNums = budgetUnitIds.split(",");
        Long[] dutyIds = (Long[]) ConvertUtils.convert(budgetUnitNums,Long.class);
        List<Long> unitIds = Arrays.asList(dutyIds);
        return unitIds;
    }
}
