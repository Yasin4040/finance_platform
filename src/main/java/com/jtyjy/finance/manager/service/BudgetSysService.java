package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.StatusConstants;
import com.jtyjy.finance.manager.controller.authorfee.excel.ContributionFeeExcelDetail;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.utils.CommonUtil;
import com.klcwqy.easy.lock.impl.ZookeeperShareLock;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
public class BudgetSysService {

    @Autowired
    private BudgetYearPeriodMapper yearPeriodMapper;

    @Autowired
    private BudgetYearPeriodService yearPeriodService;

    @Autowired
    private BudgetYearAgentService yearAgentService;

    @Autowired
    private BudgetSysMapper sysMapper;

    @Autowired
    private BudgetUnitMapper unitMapper;

    @Autowired
    private BudgetYearAgentMapper yearAgentMapper;

    @Autowired
    private BudgetProductCategoryMapper categoryMapper;

    @Autowired
    private BudgetYearSubjectMapper yearSubjectMapper;

    @Autowired
    private BudgetMonthSubjectMapper monthSubjectMapper;

    @Autowired
    private BudgetMonthSubjectHisMapper monthSubjecthisMapper;

    @Autowired
    private BudgetYearSubjectHisMapper hisMapper;

    @Autowired
    private BudgetSubjectMapper subjectMapper;

    @Autowired
    private BudgetYearStartupMapper yearStartupMapper;

    @Autowired
    private BudgetMonthPeriodMapper monthPeriodMapper;

    @Autowired
    private BudgetMonthPeriodService monthPeriodService;

    @Autowired
    private BudgetMonthStartupMapper monthStartupMapper;

    @Autowired
    private BudgetMonthEndUnitMapper monthEndUnitMapper;

    @Autowired
    private BudgetMonthAgentMapper monthAgentMapper;

    @Autowired
    private BudgetYearSubjectService budgetYearSubjectService;

    @Autowired
    private BudgetMonthSubjectService budgetMonthSubjectService;

    @Autowired
    private BudgetArrearsMapper budgetArrearsMapper;

    @Autowired
    private BudgetLendandrepaymoneyMapper budgetLendandrepaymoneyMapper;

    @Autowired
    private BudgetLendmoneyMapper budgetLendmoneyMapper;

    @Autowired
    private CuratorFramework curatorFramework;

    public final ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");

    public final static int threadHandleRecords = 5;

    /**
     * 启动预算
     *
     * @param query (届别名称-月id)
     */
    public void startBudget(String query) throws Exception {
        String[] ids = query.split("-");
        if (ids.length > 2) throw new RuntimeException("参数错误");
        BudgetYearPeriod yearPeriod = yearPeriodMapper.selectOne(new QueryWrapper<BudgetYearPeriod>().eq("period", ids[0]));
        if (Objects.isNull(yearPeriod)) throw new RuntimeException("系统异常");
        if (query.indexOf("-") >= 0) {
            //启动月度预算
            startupMonthBudget(yearPeriod.getId(), query.split("-")[1]);
        } else {
            //启动年度预算
            startUpYearBudget(yearPeriod.getId());
        }

    }

    /**
     * 启动月度预算
     *
     * @param yearid
     * @param monthid
     */
    private void startupMonthBudget(Long yearid, String monthid) throws Exception {
        //TODO 加锁
        BudgetYearStartup yearStartup = getYearStartup(yearid);
        if (!yearStartup.getStartbudgetflag()) {
            throw new RuntimeException("启动失败，年度预算还未启动。");
        }
        //重置启动月份标识,并设置当前启动月为当前月
        sysMapper.resetMonthStartFlag();
        BudgetMonthPeriod monthPeriod = monthPeriodMapper.selectById(Long.valueOf(monthid));
        monthPeriod.setCurrentflag(true);
        this.monthPeriodMapper.updateById(monthPeriod);

        //获取上个月的信息
        BudgetYearMonthPeriod tt = new BudgetYearMonthPeriod();
        tt.setBudgetMonthPeriod(monthPeriod);
        tt.setBudgetYearPeriod(yearPeriodMapper.selectById(yearid));
        tt = preBudgetYearMonthPeriod(tt);

        //上个月
        BudgetMonthStartup prebudgetMonthStartup = null;
        //本月是否已经全部月结
        if (!"6".equals(monthid)) {
            prebudgetMonthStartup = getBudgetMonthStartup(tt.getBudgetYearPeriod().getId(), tt.getBudgetMonthPeriod().getId());
            if ((null == prebudgetMonthStartup || !prebudgetMonthStartup.getStartbudgetflag())) {
                throw new RuntimeException("启动失败，" + prebudgetMonthStartup.getMonthid() + "月预算还未启动。");
            }
        } else {
            Integer count = this.unitMapper.selectCount(new QueryWrapper<BudgetUnit>().eq("budgetflag", 1).eq("requeststatus", 2).eq("yearid", yearid));
            ;
            if (count <= 0) {
                throw new RuntimeException("启动失败，还没有预算单位提交年度审核。");
            }
        }
        BudgetMonthStartup budgetMonthStartup = getBudgetMonthStartup(Long.valueOf(yearid), Long.valueOf(monthid));
        yearStartup.setEndbudgeteditflag(true);
        budgetMonthStartup.setUpdateTime(new Date());
        budgetMonthStartup.setStartbudgetflag(true);
        this.yearStartupMapper.updateById(yearStartup);
        this.monthStartupMapper.updateById(budgetMonthStartup);

        monthBudgetRestart(yearid, monthid);
        List<BudgetMonthPeriod> periodList = this.monthPeriodMapper.selectList(null);
        periodList.forEach(e->{
            e.setCurrentflag(false);
            if(e.getId().toString().equals(monthid)) e.setCurrentflag(true);
        });
        this.monthPeriodService.updateBatchById(periodList);
        List<BudgetYearPeriod> yearPeriods = this.yearPeriodMapper.selectList(null);
        yearPeriods.forEach(e->{
            e.setCurrentflag(false);
            if(e.getId().equals(yearid)) e.setCurrentflag(true);
        });
        this.yearPeriodService.updateBatchById(yearPeriods);
    }

    private void monthBudgetRestart(Long yearid, String monthid) throws Exception {
        List<BudgetUnit> unitList = this.unitMapper.selectList(new QueryWrapper<BudgetUnit>().eq("yearid", yearid));

        int threadSize = (unitList.size()/threadHandleRecords)+1;
        final CountDownLatch latch = new CountDownLatch(threadSize);
        List<BudgetUnit> newlist = null;
        ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
        for(int i=0;i<threadSize;i++){
            if ((i + 1) == threadSize) {
                newlist = unitList.subList((i * threadHandleRecords), unitList.size());
            }else{
                newlist = unitList.subList(i * threadHandleRecords, (i+1) * threadHandleRecords);
            }
            List<BudgetUnit> processList = newlist;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    for(BudgetUnit unit : processList){
                        try {
                            restartUnitMonthBudget(unit, monthid);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

    }

    @Autowired
    private BudgetMonthAgentService monthAgentService;

    /**
     * 重启月度预算
     *
     * @param unit
     */
    public void restartUnitMonthBudget(BudgetUnit unit, String monthid) throws Exception {
        String lockKey = "/finance-platform/restartUnitMonthBudget" + unit.getId() + "-" + monthid;
        ZookeeperShareLock zookeeperShareLock = new ZookeeperShareLock(this.curatorFramework, lockKey, null);
        try {
            zookeeperShareLock.tryLock();
            //TODO 加锁
            modifyUpdateflagOfunitmonth(unit, Long.valueOf(monthid));
            sysMapper.clearMonthData(unit.getId(), monthid);
            sysMapper.clearHisMonthData(unit.getId(), monthid);
            //获取年度动因
            List<BudgetYearAgent> yearAgentList = this.yearAgentMapper.selectList(new QueryWrapper<BudgetYearAgent>().eq("unitid", unit.getId()));
            //获取月度动因
            List<BudgetMonthAgent> monthAgentList = monthAgentMapper.selectList(new QueryWrapper<BudgetMonthAgent>().eq("unitid", unit.getId()).eq("monthid", monthid));
            //key为年度动因id
            Map<Long, BudgetMonthAgent> monthAgentMap = monthAgentList.stream().collect(Collectors.toMap(BudgetMonthAgent::getYearagentid, e -> e, (e1, e2) -> e1));

            List<Map<Long, Map<String, BigDecimal>>> yearmonthDataSumList = yearmonthinfo(unit.getYearid(), unit.getId(), Long.valueOf(monthid), false);
            //动因数据
            Map<Long, Map<String, BigDecimal>> yearmonthAgentDataSum = yearmonthDataSumList.get(0);
            //科目数据
            Map<Long, Map<String, BigDecimal>> yearmonthSubjectDataSum = yearmonthDataSumList.get(1);


            List<BudgetMonthAgent> newMonthAgentList = new ArrayList<>();
            List<BudgetMonthAgent> updateMonthAgentList = new ArrayList<>();
            for (BudgetYearAgent yearAgent : yearAgentList) {
                Map<String, BigDecimal> yearmonthinfo = yearmonthAgentDataSum.get(yearAgent.getId());
                BigDecimal executemoney = BigDecimal.ZERO;
                BigDecimal addmoney = BigDecimal.ZERO;
                BigDecimal lendinmoney = BigDecimal.ZERO;
                BigDecimal lendoutmoney = BigDecimal.ZERO;
                if (null != yearmonthinfo) {
                    executemoney = yearmonthinfo.get("executemoney");
                    if (null == executemoney) executemoney = BigDecimal.ZERO;
                    addmoney = yearmonthinfo.get("addmoney");
                    if (null == addmoney) addmoney = BigDecimal.ZERO;
                    lendinmoney = yearmonthinfo.get("lendinmoney");
                    if (null == lendinmoney) lendinmoney = BigDecimal.ZERO;
                    lendoutmoney = yearmonthinfo.get("lendoutmoney");
                    if (null == lendoutmoney) lendoutmoney = BigDecimal.ZERO;
                    if (lendinmoney.compareTo(lendoutmoney) == 0) {
                        lendinmoney = BigDecimal.ZERO;
                        lendoutmoney = BigDecimal.ZERO;
                    }
                }
                BudgetMonthAgent budgetMonthAgent = monthAgentMap.get(yearAgent.getId());
                if (null == budgetMonthAgent) {
                    budgetMonthAgent = initBudgetMonthAgent(yearAgent, Long.valueOf(monthid));
                    budgetMonthAgent.setMonthid(Long.valueOf(monthid));
                    budgetMonthAgent.setLendinmoney(BigDecimal.ZERO);
                    budgetMonthAgent.setLendoutmoney(BigDecimal.ZERO);
                    budgetMonthAgent.setExecutemoney(BigDecimal.ZERO);
                    budgetMonthAgent.setAddmoney(BigDecimal.ZERO);
                    //当时的年度动因金额
                    budgetMonthAgent.setYearagentmoney(yearAgent.getTotal());
                    //当时的年度追加金额
                    budgetMonthAgent.setYearaddmoney(addmoney);
                    //当时的年度拆出金额（同科目里面的动因可以拆借）
                    budgetMonthAgent.setYearlendoutmoney(lendoutmoney);
                    //当时的年度当时的年度拆进金额（同科目里面的动因可以拆借）
                    budgetMonthAgent.setYearlendinmoney(lendinmoney);
                    //当时的年度执行数量
                    budgetMonthAgent.setYearexecutemoney(executemoney);
                    //对应月份的分解金额
                    budgetMonthAgent.setM(getM(yearAgent, Long.valueOf(monthid)));
                    //月度预算金额(可编辑)
                    //budgetMonthAgent.setTotal(budgetMonthAgent.getM());
                    budgetMonthAgent.setTotal(BigDecimal.ZERO);
                    newMonthAgentList.add(budgetMonthAgent);
                    monthAgentMap.put(budgetMonthAgent.getYearagentid(), budgetMonthAgent);
                } else {
                    budgetMonthAgent.setYearagentmoney(yearAgent.getTotal());
                    //当时的年度追加金额
                    budgetMonthAgent.setYearaddmoney(addmoney);
                    //当时的年度拆出金额（同科目里面的动因可以拆借）
                    budgetMonthAgent.setYearlendoutmoney(lendoutmoney);
                    //当时的年度当时的年度拆进金额（同科目里面的动因可以拆借）
                    budgetMonthAgent.setYearlendinmoney(lendinmoney);
                    //当时的年度执行数量
                    budgetMonthAgent.setYearexecutemoney(executemoney);
                    //对应月份的分解金额
                    budgetMonthAgent.setM(getM(yearAgent, Long.valueOf(monthid)));
                    //月度预算金额(可编辑)
                    //budgetMonthAgent.setTotal(budgetMonthAgent.getM());
                    budgetMonthAgent.setUpdatetime(new Date());
                    updateMonthAgentList.add(budgetMonthAgent);
                }
            }
            if (!newMonthAgentList.isEmpty()) monthAgentService.saveBatch(newMonthAgentList);
            if (!updateMonthAgentList.isEmpty()) monthAgentService.updateBatchById(updateMonthAgentList);

            List<BudgetYearSubject> yearSubjectList = this.yearSubjectMapper.selectList(new QueryWrapper<BudgetYearSubject>().eq("unitid", unit.getId()));
            List<BudgetMonthSubject> monthSubectList = monthSubjectMapper.selectList(new QueryWrapper<BudgetMonthSubject>().eq("unitid", unit.getId()).eq("monthid", monthid));
            Map<Long, BudgetMonthSubject> monthSubectMap = monthSubectList.stream().collect(Collectors.toMap(e -> e.getSubjectid(), e -> e, (e1, e2) -> e1));
            List<Map<String, Object>> subjectMsgList = sysMapper.getYearSubjectMap(unit.getId());

            Map<String, Map<String, Object>> basesubjectnamemap = subjectMsgList.stream().collect(Collectors.toMap(e -> e.get("bbsname").toString(), e -> e, (e1, e2) -> e1));
            List<Map<String, Object>> allSubjects = sysMapper.getAllSubjects(unit.getYearid(), unit.getId());
            Map<Long, Map<String, Object>> budgetSubjectmap = allSubjects.stream().collect(Collectors.toMap(e -> Long.valueOf(e.get("subjectid").toString()), e -> e, (e1, e2) -> e1));
            List<BudgetMonthSubject> newMonthSubjectList = new ArrayList<>();
            List<BudgetMonthSubject> updateMonthSubjectList = new ArrayList<>();
            for (BudgetYearSubject budgetYearSubject : yearSubjectList) {
                BudgetMonthSubject budgetMonthSubject = monthSubectMap.get(budgetYearSubject.getSubjectid());
                if (null == budgetMonthSubject) {
                    budgetMonthSubject = new BudgetMonthSubject();
                }
                budgetMonthSubject.setYearid(unit.getYearid());
                budgetMonthSubject.setUnitid(unit.getId());
                budgetMonthSubject.setSubjectid(budgetYearSubject.getSubjectid());
                budgetMonthSubject.setMonthid(Long.valueOf(monthid));
                try {
                    Method getM = budgetYearSubject.getClass().getMethod("getM" + monthid);
                    budgetMonthSubject.setM((BigDecimal) getM.invoke(budgetYearSubject));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                budgetMonthSubject.setYearagentmoney(budgetYearSubject.getTotal());
                //年度科目
                Map<String, BigDecimal> yearmonthinfo = yearmonthSubjectDataSum.get(budgetYearSubject.getSubjectid());
                BigDecimal executemoney = BigDecimal.ZERO;
                BigDecimal addmoney = BigDecimal.ZERO;
                BigDecimal lendinmoney = BigDecimal.ZERO;
                BigDecimal lendoutmoney = BigDecimal.ZERO;
                if (null != yearmonthinfo) {
                    executemoney = yearmonthinfo.get("executemoney");
                    if (null == executemoney) executemoney = BigDecimal.ZERO;
                    addmoney = yearmonthinfo.get("addmoney");
                    if (null == addmoney) addmoney = BigDecimal.ZERO;
                    lendinmoney = yearmonthinfo.get("lendinmoney");
                    if (null == lendinmoney) lendinmoney = BigDecimal.ZERO;
                    lendoutmoney = yearmonthinfo.get("lendoutmoney");
                    if (null == lendoutmoney) lendoutmoney = BigDecimal.ZERO;
                    if (lendinmoney.compareTo(lendoutmoney) == 0) {
                        lendinmoney = BigDecimal.ZERO;
                        lendoutmoney = BigDecimal.ZERO;
                    }
                }
                budgetMonthSubject.setYearaddmoney(addmoney);
                budgetMonthSubject.setYearexecutemoney(executemoney);
                budgetMonthSubject.setYearlendinmoney(lendinmoney);
                budgetMonthSubject.setYearlendoutmoney(lendoutmoney);
                //本届收入占比公式
                String revenueformulastr = budgetYearSubject.getRevenueformulastr();
                if (StringUtils.isEmpty(revenueformulastr)) {
                    revenueformulastr = unit.getRevenueformula();
                }
                BigDecimal thismoney = budgetYearSubject.getTotal();
                if (null == budgetMonthSubject.getId()) {
                    thismoney = thismoney.add(budgetYearSubject.getAddmoney())
                            .add(budgetYearSubject.getLendinmoney())
                            .subtract(budgetYearSubject.getExecutemoney())
                            .subtract(budgetYearSubject.getLendoutmoney());
                }
                if (StringUtils.isNotEmpty(revenueformulastr)) {
                    revenueformulastr = repalceformulasstr(revenueformulastr, basesubjectnamemap, "total", thismoney);
                    try {
                        BigDecimal result = js(revenueformulastr, null);
                        budgetMonthSubject.setRevenueformula(result);
                    } catch (Exception e) {
                    }
                }
                String formulastr = budgetYearSubject.getFormula();
                if (StringUtils.isNotEmpty(formulastr)) {
                    formulastr = repalceformulasstr(formulastr, basesubjectnamemap, "executemoney", null);
                    try {
                        BigDecimal result = js(formulastr, null);
                        budgetMonthSubject.setYearexecutemoney(result);
                    } catch (Exception e) {
                    }
                }

                if (null == budgetMonthSubject.getId()) {
                    budgetMonthSubject.setCreatetime(new Date());
                    newMonthSubjectList.add(budgetMonthSubject);
                } else {
                    budgetMonthSubject.setUpdatetime(new Date());
                    updateMonthSubjectList.add(budgetMonthSubject);
                }
            }
            if (!newMonthSubjectList.isEmpty()) monthSubjectService.saveBatch(newMonthSubjectList);
            if (!updateMonthSubjectList.isEmpty()) monthSubjectService.updateBatchById(updateMonthSubjectList);
            upsummonthsubject(unit.getYearid(), unit.getId(), Long.valueOf(monthid), budgetSubjectmap);
            updateformulamonthsubject(unit.getYearid(), Long.valueOf(monthid), unit, budgetSubjectmap);
        } catch (Exception e) {
            throw e;
        } finally {
            zookeeperShareLock.unLock();
        }
    }

    @Autowired
    private BudgetMonthSubjectService monthSubjectService;

    public void updateformulamonthsubject(Long yearid, Long monthid, BudgetUnit
            unit, Map<Long, Map<String, Object>> budgetSubjectmap) {
        Long unitid = unit.getId();
        synchronized (unitid + "_" + monthid) {
            List<BudgetMonthSubject> monthsubjects = this.monthSubjectMapper.selectList(new QueryWrapper<BudgetMonthSubject>().eq("yearid", yearid).eq("unitid", unitid).eq("monthid", monthid));
            //key 为 id
            Map<Long, BudgetMonthSubject> monthsubjectidmap = monthsubjects.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));

            List<Map<String, Object>> monthsubjectmaps = sysMapper.getMonthSubjectMaps(yearid, unitid, monthid);
            //key 为 name
            Map<String, Map<String, Object>> monthsubjectnamemap = new HashMap<String, Map<String, Object>>();
            for (Map<String, Object> monthsubjectmap : monthsubjectmaps) {
                String name = monthsubjectmap.get("name").toString();
                monthsubjectnamemap.put(name, monthsubjectmap);
            }
            //计算公式 科目
            Map<String, String> subjectformula = new HashMap<String, String>();
            for (Map<String, Object> monthsubjectmap : monthsubjectmaps) {
                String name = monthsubjectmap.get("name").toString();
                String formula = (String) monthsubjectmap.get("formula");
                BigDecimal total = new BigDecimal(monthsubjectmap.get("total").toString());
                if (StringUtils.isNotEmpty(formula)) {
                    formula = formula.replace("[this]", "(" + total + ")");
                    subjectformula.put(name, formula);
                }
            }
            //[销售收入]-[已销产品成本]-[间接制造费用]-[发运费用]-[销售费用]-[管理费用]-[库存报废成本]
            //计算过的科目
            Set<String> formulasubject = new HashSet<String>();

            //科目金额
            Map<String, BigDecimal> subjectformulaje = new HashMap<String, BigDecimal>();
            for (Map<String, Object> monthsubjectmap : monthsubjectmaps) {
                String name = monthsubjectmap.get("name").toString();
                BigDecimal total = new BigDecimal(monthsubjectmap.get("total").toString());
                subjectformulaje.put(name, total);
            }
            int i = 0;
            while (formulasubject.size() < subjectformula.size()) {
                for (String subjectname : subjectformula.keySet()) {
                    String formula = subjectformula.get(subjectname);
                    if (formulasubject.contains(subjectname)) {
                        continue;
                    }
                    List<String> list = extractMessageByRegular(formula);
                    if (null == list || list.size() <= 0) {
                        try {
                            BigDecimal ccratioformula = js(formula, null);
                            subjectformulaje.put(subjectname, ccratioformula);
                            formulasubject.add(subjectname);

                        } catch (NumberFormatException | ScriptException e) {
                            e.printStackTrace();
                        }
                    } else {
                        for (String subjectname_ : list) {
                            if (formulasubject.contains(subjectname_)
                                    || !subjectformula.containsKey(subjectname_)
                            ) {
                                formula = formula.replace("[" + subjectname_ + "]", "(" + subjectformulaje.get(subjectname_) + ")");
                            }
                        }
                        if (!formula.contains("[") && !formula.contains("]")) {
                            try {
                                BigDecimal ccratioformula = js(formula, null);
                                subjectformulaje.put(subjectname, ccratioformula);
                                formulasubject.add(subjectname);
                            } catch (NumberFormatException | ScriptException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    subjectformula.put(subjectname, formula);
                }
                if (i > 20) {
                    break;
                }
                i++;
            }

            //上级预算单位
            List<Map<String, Object>> psubjectlist = sysMapper.getPsubjectList(unit.getParentid(), monthid);
            Map<String, BigDecimal> psubjectmap = new HashMap<String, BigDecimal>();
            for (Map<String, Object> psubject : psubjectlist) {
                String name_ = psubject.get("name").toString();
                BigDecimal total_ = new BigDecimal(psubject.get("total").toString());
                psubjectmap.put(name_, total_);
            }

            //在持续一遍
            for (String subjectname : subjectformula.keySet()) {
                String formula = subjectformula.get(subjectname);
                List<String> list = extractMessageByRegular(formula);

                if (null != list && list.size() > 0) {
                    for (String subjectname_ : list) {
                        formula = formula.replace("[" + subjectname_ + "]", "(0)");
                    }
                    try {
                        BigDecimal ccratioformula = js(formula, null);
                        subjectformulaje.put(subjectname, ccratioformula);
                        formulasubject.add(subjectname);
                    } catch (Exception e) {
                    }
                }
            }

            for (Map<String, Object> monthsubjectmap : monthsubjectmaps) {
                Long id = Long.valueOf(monthsubjectmap.get("id").toString());
                Long subjectid = Long.valueOf(monthsubjectmap.get("unitsubjectid").toString());
                String name = monthsubjectmap.get("name").toString();
                BudgetMonthSubject budgetMonthSubject = monthsubjectidmap.get(id);
                //公式项
                String formula = (String) monthsubjectmap.get("formula");
                BigDecimal total = new BigDecimal(monthsubjectmap.get("total").toString());
                if (StringUtils.isNotEmpty(formula)) {
                    budgetMonthSubject.setTotal(subjectformulaje.get(name));
                    this.monthSubjectMapper.updateById(budgetMonthSubject);

                    Long tmpsubjectid = subjectid;
                    boolean flag = true;
                    while (flag && budgetMonthSubject.getTotal().compareTo(total) != 0) {
                        //获取上级科目 parentid
                        Map<String, Object> budgetSubject = budgetSubjectmap.get(tmpsubjectid);
                        //上级科目id
                        Long parentid = Long.valueOf(budgetSubject.get("parentid").toString());
                        //向上汇总
                        Boolean upsumflag = (Boolean) budgetSubject.get("upsumflag");
                        if (upsumflag && null != budgetSubjectmap.get(parentid)) {
                            tmpsubjectid = parentid;

                            budgetSubject = budgetSubjectmap.get(tmpsubjectid);
                            //科目公式
                            String formula_ = (String) budgetSubject.get("formula");
                            if (StringUtils.isNotEmpty(formula_)) {
                                flag = false;
                            }
                            if (true == flag) {
                                updatemonthsubjectbyinit(yearid, unitid, monthid, tmpsubjectid, budgetMonthSubject.getTotal().subtract(total));
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }

        }
    }

    private void upsummonthsubject(Long yearid, Long unitid, Long
            monthid, Map<Long, Map<String, Object>> budgetSubjectmap) {
        List<BudgetMonthAgent> monthagentlist = sysMapper.getAllAgents(yearid, unitid, monthid);
        //查询所有的动因
        //科目向上
        for (BudgetMonthAgent agent : monthagentlist) {
            Long subjectid = agent.getSubjectid();
            Long tmpsubjectid = subjectid;
            boolean flag = true;
            while (flag) {
                if (unitid.toString().equals("83") && subjectid.toString().equals("1339")) {
                    System.err.println("a");
                }
                //获取上级科目 parentid
                Map<String, Object> budgetSubject = budgetSubjectmap.get(tmpsubjectid);
                //科目公式
                String formula = (String) budgetSubject.get("formula");
                if (StringUtils.isNotEmpty(formula)) {
                    flag = false;
                }
                if (true == flag) {
                    updatemonthsubjectbyinit(agent.getYearid(), unitid, agent.getMonthid(), tmpsubjectid, agent.getTotal());
                }
                //上级科目id
                Long parentid = Long.valueOf(budgetSubject.get("parentid").toString());
                //向上汇总
                Boolean upsumflag = (Boolean) budgetSubject.get("upsumflag");
                if (upsumflag && null != budgetSubjectmap.get(parentid)) {
                    tmpsubjectid = parentid;
                } else {
                    flag = false;
                }
            }
        }
        sysMapper.setMonthBusiness(null, yearid, unitid, monthid, null);
        //说明汇总
        List<Map<String, Object>> monthbusineslist = sysMapper.getMonthBussinessList(yearid, unitid, monthid);
        for (Map<String, Object> monthbusines : monthbusineslist) {
            String _yearid = monthbusines.get("yearid").toString();
            String _unitid = monthbusines.get("unitid").toString();
            String _monthid = monthbusines.get("monthid").toString();
            String _subjectid = monthbusines.get("subjectid").toString();
            String _monthbusiness = (String) monthbusines.get("monthbusiness");
            //更新汇总明细
            sysMapper.setMonthBusiness(_monthbusiness, Long.valueOf(_yearid), Long.valueOf(_unitid), Long.valueOf(_monthid), Long.valueOf(_subjectid));
        }
    }

    public void updatemonthsubjectbyinit(Long yearid, Long unitid, Long monthid, Long subjectid, BigDecimal total) {
        String key = yearid + "_" + unitid + "_" + monthid + "_" + subjectid;
        synchronized (key) {

            BudgetMonthSubject budgetMonthSubject = this.monthSubjectMapper.selectOne(new QueryWrapper<BudgetMonthSubject>().eq("yearid", yearid).eq("unitid", unitid).eq("monthid", monthid).eq("subjectid", subjectid));

            BudgetSubject budgetSubject = this.subjectMapper.selectById(subjectid);
            //历史信息
            QueryWrapper<BudgetMonthSubjectHis> wrapper = new QueryWrapper<BudgetMonthSubjectHis>().eq("yearid", yearid).eq("unitid", unitid).eq("subjectid", subjectid).eq("type", 0).eq("monthid", monthid);
            BudgetMonthSubjectHis budgetmonthSubjectHis = this.monthSubjecthisMapper.selectOne(wrapper);

            budgetMonthSubject.setTotal(budgetMonthSubject.getTotal().add(total));

            this.monthSubjectMapper.updateById(budgetMonthSubject);
            if (null != budgetmonthSubjectHis) {
                budgetmonthSubjectHis.setTotal(budgetMonthSubject.getTotal());
                this.monthSubjecthisMapper.updateById(budgetmonthSubjectHis);
            } else if (budgetSubject.getLeafflag()) {
                budgetmonthSubjectHis = new BudgetMonthSubjectHis();
                budgetmonthSubjectHis.setYearid(yearid);
                budgetmonthSubjectHis.setUnitid(unitid);
                budgetmonthSubjectHis.setMonthid(monthid);
                budgetmonthSubjectHis.setSubjectid(subjectid);
                budgetmonthSubjectHis.setType(0);
                budgetmonthSubjectHis.setTotal(budgetMonthSubject.getTotal());
                this.monthSubjecthisMapper.insert(budgetmonthSubjectHis);
            }
        }
    }

    private final String repalceformulasstr(final String formulastr,
                                            final Map<String, Map<String, Object>> subjectje, String key, BigDecimal self) {
        String tmp = formulastr;
        if (null != self) {
            tmp = tmp.replace("[this]", "(" + self.toString() + ")");
        }
        for (String key_ : subjectje.keySet()) {
            Map<String, Object> vv = subjectje.get(key_);
            if (tmp.contains(key_)) {
                tmp = tmp.replace("[" + key_ + "]", "(" + vv.get(key).toString() + ")");
            }
        }
        Matcher mat = Pattern.compile("[\u4e00-\u9fa5]").matcher(tmp);
        //公式中含有本预算单位中没有的科目
        //例如：(14732769.6400)-(2787095.6800)-(170000.0000)-(1527913.0400)-(709007.5600)-(2013579.9300)-[税金及附加]-(0.0000)
        if (mat.find()) {
            /**
             * update by minzhq
             */
            tmp = tmp.replaceAll("[\u4e00-\u9fa5]", "");
            tmp = tmp.replaceAll("[\\[]", "(0");
            tmp = tmp.replaceAll("[\\]]", ")");
            tmp = tmp.replaceAll("[（）]", "");
        }
        return tmp;
    }

    private BigDecimal getM(BudgetYearAgent budgetYearAgent, Long monthid) {
        if (monthid.longValue() == 1) {
            return budgetYearAgent.getM1();
        } else if (monthid.longValue() == 1) {
            return budgetYearAgent.getM1();
        }
        if (monthid.longValue() == 2) {
            return budgetYearAgent.getM2();
        }
        if (monthid.longValue() == 3) {
            return budgetYearAgent.getM3();
        }
        if (monthid.longValue() == 4) {
            return budgetYearAgent.getM4();
        }
        if (monthid.longValue() == 5) {
            return budgetYearAgent.getM5();
        }
        if (monthid.longValue() == 6) {
            return budgetYearAgent.getM6();
        }
        if (monthid.longValue() == 7) {
            return budgetYearAgent.getM7();
        }
        if (monthid.longValue() == 8) {
            return budgetYearAgent.getM8();
        }
        if (monthid.longValue() == 9) {
            return budgetYearAgent.getM9();
        }
        if (monthid.longValue() == 10) {
            return budgetYearAgent.getM10();
        }
        if (monthid.longValue() == 11) {
            return budgetYearAgent.getM11();
        }
        if (monthid.longValue() == 12) {
            return budgetYearAgent.getM12();
        } else {
            return new BigDecimal(0);
        }
    }

    private BudgetMonthAgent initBudgetMonthAgent(BudgetYearAgent budgetYearAgent, Long monthid) {
        BudgetMonthAgent budgetMonthAgent = new BudgetMonthAgent();
        //年度期间id
        budgetMonthAgent.setYearid(budgetYearAgent.getYearid());
        //预算科目id
        budgetMonthAgent.setSubjectid(budgetYearAgent.getSubjectid());
        //预算单位id
        budgetMonthAgent.setUnitid(budgetYearAgent.getUnitid());
        //预算动因id
        budgetMonthAgent.setYearagentid(budgetYearAgent.getId());
        //动因名称
        budgetMonthAgent.setName(budgetYearAgent.getName());
        //产品id
        budgetMonthAgent.setProductid(budgetYearAgent.getProductid());
        //弹性动因标识 true表示弹性
        budgetMonthAgent.setElasticflag(budgetYearAgent.getElasticflag());
        //弹性率
        budgetMonthAgent.setElasticratio(budgetYearAgent.getElasticratio());
        //占比科目id
        budgetMonthAgent.setBudgetsubjectid(budgetYearAgent.getBudgetsubjectid());
        //弹性动因占比上限
        budgetMonthAgent.setElasticmax(budgetYearAgent.getElasticmax());
        //动因内容
        budgetMonthAgent.setRemark(budgetYearAgent.getRemark());
        return budgetMonthAgent;
    }

    /**
     * 获取当前届别当前月之前所有的执行、追加、拆借数据
     *
     * @param yearid
     * @param unitid
     * @param monthid
     * @param curflag
     * @return
     */
    private List<Map<Long, Map<String, BigDecimal>>> yearmonthinfo(Long yearid, Long unitid, Long monthid, Boolean
            curflag) {
        Map<Long, Map<String, BigDecimal>> subjectresult = new HashMap<Long, Map<String, BigDecimal>>();
        Map<Long, Map<String, BigDecimal>> agentresult = new HashMap<Long, Map<String, BigDecimal>>();

        String monthids = CommonUtil.getMonthids(monthid, curflag);
        String enddate = CommonUtil.getEnddate(yearPeriodMapper.selectById(yearid), monthid, curflag);

        populateExecuteData(subjectresult, agentresult, unitid, monthids);
        populateYearAddData(subjectresult, agentresult, unitid, enddate);
        populateYearLendData(subjectresult, agentresult, unitid, enddate);
        List<Map<Long, Map<String, BigDecimal>>> results = new ArrayList<Map<Long, Map<String, BigDecimal>>>();
        results.add(agentresult);
        results.add(subjectresult);
        return results;
    }

    /**
     * 填充拆借数据
     *
     * @param subjectresult
     * @param agentresult
     * @param unitid
     * @param enddate
     */
    private void populateYearLendData(Map<Long, Map<String, BigDecimal>> subjectresult,
                                      Map<Long, Map<String, BigDecimal>> agentresult, Long unitid, String enddate) {
        List<Map<String, Object>> yearLendingDataList = sysMapper.getYearLendingData(unitid, enddate);
        List<Map<String, Object>> yearLendoutDataList = sysMapper.getYearLendoutData(unitid, enddate);
        List<Map<String, Object>> erpLendDataList = sysMapper.getERPLendData(unitid, enddate);

        for (Map<String, Object> yearLendingData : yearLendingDataList) {

            String subjectpids = (String) yearLendingData.get("insubjectpids");
            Long yearagentid = Long.valueOf(yearLendingData.get("inyearagentid").toString());

            BigDecimal executemoney = new BigDecimal(yearLendingData.get("lendmoney").toString());
            String[] subjectpidss = subjectpids.split("-");
            for (String subjectid : subjectpidss) {
                Long _subjectid_ = Long.valueOf(subjectid);
                Map<String, BigDecimal> _result_ = subjectresult.get(_subjectid_);
                if (null == _result_) {
                    _result_ = new HashMap<String, BigDecimal>();
                }
                BigDecimal _executemoney_ = _result_.get("lendinmoney");
                if (null == _executemoney_) {
                    _executemoney_ = new BigDecimal(0);
                }
                _executemoney_ = _executemoney_.add(executemoney);
                _result_.put("lendinmoney", _executemoney_);
                subjectresult.put(_subjectid_, _result_);
            }
            Map<String, BigDecimal> _result_ = agentresult.get(yearagentid);
            if (null == _result_) {
                _result_ = new HashMap<String, BigDecimal>();
            }
            BigDecimal _executemoney_ = _result_.get("lendinmoney");
            if (null == _executemoney_) {
                _executemoney_ = new BigDecimal(0);
            }
            _executemoney_ = _executemoney_.add(executemoney);
            _result_.put("lendinmoney", _executemoney_);
            agentresult.put(yearagentid, _result_);
        }

        for (Map<String, Object> yearLendoutData : yearLendoutDataList) {
            {
                String subjectpids = (String) yearLendoutData.get("outsubjectpids");
                Long yearagentid = Long.valueOf(yearLendoutData.get("outyearagentid").toString());
                BigDecimal executemoney = new BigDecimal(yearLendoutData.get("lendmoney").toString());
                String[] subjectpidss = subjectpids.split("-");
                for (String subjectid : subjectpidss) {
                    Long _subjectid_ = Long.valueOf(subjectid);

                    Map<String, BigDecimal> _result_ = subjectresult.get(_subjectid_);
                    if (null == _result_) {
                        _result_ = new HashMap<String, BigDecimal>();
                    }
                    BigDecimal _executemoney_ = _result_.get("lendoutmoney");
                    if (null == _executemoney_) {
                        _executemoney_ = new BigDecimal(0);
                    }
                    _executemoney_ = _executemoney_.add(executemoney);
                    _result_.put("lendoutmoney", _executemoney_);
                    subjectresult.put(_subjectid_, _result_);
                }

                {
                    Map<String, BigDecimal> _result_ = agentresult.get(yearagentid);
                    if (null == _result_) {
                        _result_ = new HashMap<String, BigDecimal>();
                    }
                    BigDecimal _executemoney_ = _result_.get("lendoutmoney");
                    if (null == _executemoney_) {
                        _executemoney_ = new BigDecimal(0);
                    }
                    _executemoney_ = _executemoney_.add(executemoney);
                    _result_.put("lendoutmoney", _executemoney_);
                    agentresult.put(yearagentid, _result_);
                }
            }
        }

        for (Map<String, Object> executedata : erpLendDataList) {
            //进
            {
                String subjectpids = (String) executedata.get("insubjectpids");
                Long yearagentid = Long.valueOf(executedata.get("inyearagentid").toString());
                BigDecimal executemoney = new BigDecimal(executedata.get("lendmoney").toString());
                String[] subjectpidss = subjectpids.split("-");
                for (String subjectid : subjectpidss) {
                    Long _subjectid_ = Long.valueOf(subjectid);

                    Map<String, BigDecimal> _result_ = subjectresult.get(_subjectid_);
                    if (null == _result_) {
                        _result_ = new HashMap<String, BigDecimal>();
                    }
                    BigDecimal _executemoney_ = _result_.get("lendinmoney");
                    if (null == _executemoney_) {
                        _executemoney_ = new BigDecimal(0);
                    }
                    _executemoney_ = _executemoney_.add(executemoney);
                    _result_.put("lendinmoney", _executemoney_);
                    subjectresult.put(_subjectid_, _result_);
                }

                {
                    Map<String, BigDecimal> _result_ = agentresult.get(yearagentid);
                    if (null == _result_) {
                        _result_ = new HashMap<String, BigDecimal>();
                    }
                    BigDecimal _executemoney_ = _result_.get("lendinmoney");
                    if (null == _executemoney_) {
                        _executemoney_ = new BigDecimal(0);
                    }
                    _executemoney_ = _executemoney_.add(executemoney);
                    _result_.put("lendinmoney", _executemoney_);
                    agentresult.put(yearagentid, _result_);
                }

            }
            {
                String subjectpids = (String) executedata.get("outsubjectpids");
                Long yearagentid = Long.valueOf(executedata.get("inyearagentid").toString());
                BigDecimal executemoney = new BigDecimal(executedata.get("lendmoney").toString());
                String[] subjectpidss = subjectpids.split("-");
                for (String subjectid : subjectpidss) {
                    Long _subjectid_ = Long.valueOf(subjectid);

                    Map<String, BigDecimal> _result_ = subjectresult.get(_subjectid_);
                    if (null == _result_) {
                        _result_ = new HashMap<String, BigDecimal>();
                    }
                    BigDecimal _executemoney_ = _result_.get("lendoutmoney");
                    if (null == _executemoney_) {
                        _executemoney_ = new BigDecimal(0);
                    }
                    _executemoney_ = _executemoney_.add(executemoney);
                    _result_.put("lendoutmoney", _executemoney_);
                    subjectresult.put(_subjectid_, _result_);
                }

                {
                    Map<String, BigDecimal> _result_ = agentresult.get(yearagentid);
                    if (null == _result_) {
                        _result_ = new HashMap<String, BigDecimal>();
                    }
                    BigDecimal _executemoney_ = _result_.get("lendoutmoney");
                    if (null == _executemoney_) {
                        _executemoney_ = new BigDecimal(0);
                    }
                    _executemoney_ = _executemoney_.add(executemoney);
                    _result_.put("lendoutmoney", _executemoney_);
                    agentresult.put(yearagentid, _result_);
                }
            }
        }
    }

    /**
     * 填充年度追加数据
     *
     * @param subjectresult
     * @param agentresult
     * @param unitid
     * @param monthids
     * @param enddate
     */
    private void populateYearAddData(Map<Long, Map<String, BigDecimal>> subjectresult,
                                     Map<Long, Map<String, BigDecimal>> agentresult, Long unitid, String enddate) {
        List<Map<String, Object>> yearAddDataList = sysMapper.getYearAddData(unitid, enddate);
        for (Map<String, Object> yearAddData : yearAddDataList) {
            String subjectpids = (String) yearAddData.get("subjectpids");
            BigDecimal executemoney = new BigDecimal(yearAddData.get("addmoney").toString());
            Long yearagentid = Long.valueOf(yearAddData.get("yearagentid").toString());
            String[] subjectpidss = subjectpids.split("-");
            for (String subjectid : subjectpidss) {
                Long _subjectid_ = Long.valueOf(subjectid);
                Map<String, BigDecimal> _result_ = subjectresult.get(_subjectid_);
                if (null == _result_) {
                    _result_ = new HashMap<String, BigDecimal>();
                }
                BigDecimal _executemoney_ = _result_.get("addmoney");
                if (null == _executemoney_) {
                    _executemoney_ = new BigDecimal(0);
                }
                _executemoney_ = _executemoney_.add(executemoney);
                _result_.put("addmoney", _executemoney_);
                subjectresult.put(_subjectid_, _result_);

            }
            Map<String, BigDecimal> _result_ = agentresult.get(yearagentid);
            if (null == _result_) {
                _result_ = new HashMap<String, BigDecimal>();
            }
            BigDecimal _executemoney_ = _result_.get("addmoney");
            if (null == _executemoney_) {
                _executemoney_ = new BigDecimal(0);
            }
            _executemoney_ = _executemoney_.add(executemoney);
            _result_.put("addmoney", _executemoney_);
            agentresult.put(yearagentid, _result_);
        }
    }

    private void populateExecuteData
            (Map<Long, Map<String, BigDecimal>> subjectresult, Map<Long, Map<String, BigDecimal>> agentresult, Long
                    unitid, String monthids) {
        //获取当前届别当前月之前所有的执行数据
        List<Map<String, Object>> executedDataList = sysMapper.getExecuteData(unitid, monthids, StatusConstants.BX_PASS);
        for (Map<String, Object> executedata : executedDataList) {
            String subjectpids = (String) executedata.get("subjectpids");
            BigDecimal executemoney = new BigDecimal(executedata.get("executemoney").toString());

            Long yearagentid = Long.valueOf(executedata.get("yearagentid").toString());

            String[] subjectpidss = subjectpids.split("-");
            for (String subjectid : subjectpidss) {
                Long _subjectid_ = Long.valueOf(subjectid);
                Map<String, BigDecimal> _result_ = subjectresult.get(_subjectid_);
                if (null == _result_) {
                    _result_ = new HashMap<String, BigDecimal>();
                }
                BigDecimal _executemoney_ = _result_.get("executemoney");
                if (null == _executemoney_) {
                    _executemoney_ = new BigDecimal(0);
                }
                _executemoney_ = _executemoney_.add(executemoney);
                _result_.put("executemoney", _executemoney_);
                subjectresult.put(_subjectid_, _result_);
            }

            Map<String, BigDecimal> _result_ = agentresult.get(yearagentid);
            if (null == _result_) {
                _result_ = new HashMap<String, BigDecimal>();
            }
            BigDecimal _executemoney_ = _result_.get("executemoney");
            if (null == _executemoney_) {
                _executemoney_ = new BigDecimal(0);
            }
            _executemoney_ = _executemoney_.add(executemoney);
            _result_.put("executemoney", _executemoney_);
            agentresult.put(yearagentid, _result_);
        }
    }



    private void modifyUpdateflagOfunitmonth(BudgetUnit unit, Long monthid) {
        BudgetMonthEndUnit monthEndUnit = monthEndUnitMapper.selectOne(new QueryWrapper<BudgetMonthEndUnit>().eq("unitid", unit.getId()).eq("monthid", monthid));
        if (Objects.nonNull(monthEndUnit)) {
            sysMapper.modifyUpdateflagOfunitmonth(new Date(), monthEndUnit.getUnitid(), monthEndUnit.getMonthid());
        } else {
            monthEndUnit = new BudgetMonthEndUnit();
            monthEndUnit.setUnitid(unit.getId());
            monthEndUnit.setMonthid(monthid);
            monthEndUnit.setYearid(unit.getYearid());
            this.monthEndUnitMapper.insert(monthEndUnit);
        }

    }

    public final BudgetMonthStartup getBudgetMonthStartup(final Long yearid, final Long monthid) {
        return monthStartupMapper.selectOne(new QueryWrapper<BudgetMonthStartup>().eq("monthid", monthid).eq("yearid", yearid));
    }

    private BudgetYearMonthPeriod preBudgetYearMonthPeriod(BudgetYearMonthPeriod nowBudgetYearMonthPeriod) {
        BudgetYearPeriod preBudgetYearPeriod = null;
        String codestr = nowBudgetYearMonthPeriod.getBudgetMonthPeriod().getCode();
        if (StringUtils.isEmpty(codestr)) {
            codestr = nowBudgetYearMonthPeriod.getBudgetMonthPeriod().getId() + "";
        }
        int code = Integer.valueOf(codestr);
        if ((6 < code && code <= 12) || (1 < code && code <= 5)) {
            code--;
            preBudgetYearPeriod = nowBudgetYearMonthPeriod.getBudgetYearPeriod();
        } else if (code == 1) {
            code = 12;
            preBudgetYearPeriod = nowBudgetYearMonthPeriod.getBudgetYearPeriod();
        } else if (code == 6) {
            code = 5;
            //跨年
            preBudgetYearPeriod = preBudgetYearPeriod(nowBudgetYearMonthPeriod.getBudgetYearPeriod());
        }
        BudgetYearMonthPeriod newBudgetYearMonthPeriod = new BudgetYearMonthPeriod();
        newBudgetYearMonthPeriod.setBudgetYearPeriod(preBudgetYearPeriod);
        newBudgetYearMonthPeriod.setBudgetMonthPeriod(getBudgetMonthPeriodByCode(code));
        return newBudgetYearMonthPeriod;
    }

    private BudgetMonthPeriod getBudgetMonthPeriodByCode(int code) {
        return this.monthPeriodMapper.selectOne(new QueryWrapper<BudgetMonthPeriod>().eq("code", code));
    }

    public final BudgetYearPeriod preBudgetYearPeriod(final BudgetYearPeriod nowBudgetYearPeriod) {
        return getBudgetYearPeriodByCode(Integer.valueOf(nowBudgetYearPeriod.getCode()) - 1);
    }

    private BudgetYearPeriod getBudgetYearPeriodByCode(int code) {
        return this.yearPeriodMapper.selectOne(new QueryWrapper<BudgetYearPeriod>().eq("code", code));
    }

    private BudgetYearStartup getYearStartup(Long yearId) {
        return yearStartupMapper.selectOne(new QueryWrapper<BudgetYearStartup>().eq("yearid", yearId));
    }

    /**
     * 启动年度预算
     *
     * @param yearId 届别id
     */
    private void startUpYearBudget(Long yearId) throws Exception {
        //TODO 加锁
        restartUpYearBudget(yearId);
        List<BudgetMonthPeriod> periodList = this.monthPeriodMapper.selectList(null);
        periodList.forEach(e->{
            e.setCurrentflag(false);
            if(e.getId() == 6L) e.setCurrentflag(true);
        });
        this.monthPeriodService.updateBatchById(periodList);

        List<BudgetYearPeriod> yearPeriods = this.yearPeriodMapper.selectList(null);
        yearPeriods.forEach(e->{
            e.setCurrentflag(false);
            if(e.getId().equals(yearId)) e.setCurrentflag(true);
        });
        this.yearPeriodService.updateBatchById(yearPeriods);
    }

    /**
     * 重启年度预算
     *
     * @param yearId
     */
    private void restartUpYearBudget(Long yearId) throws Exception {
        List<BudgetUnit> unitList = unitMapper.selectList(new QueryWrapper<BudgetUnit>().eq("yearid", yearId));
        checkIsCanStartUpYearBudget(unitList);
        BudgetYearStartup yearStartup = getYearStartup(yearId);
        yearStartup.setStartbudgetflag(true);
        this.yearStartupMapper.updateById(yearStartup);
        int threadSize = (unitList.size()/threadHandleRecords)+1;
        final CountDownLatch latch = new CountDownLatch(threadSize);
        List<BudgetUnit> newlist = null;
        ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
        for(int i=0;i<threadSize;i++){
            if ((i + 1) == threadSize) {
                newlist = unitList.subList((i * threadHandleRecords), unitList.size());
            }else{
                newlist = unitList.subList(i * threadHandleRecords, (i+1) * threadHandleRecords);
            }
            List<BudgetUnit> processList = newlist;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    for(BudgetUnit unit : processList){
                        try {
                            unitRestartYearBudgetData(unit);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();
    }

    /**
     * 部门重新生成年度预算数据
     *
     * @param budgetUnit
     */
    public void unitRestartYearBudgetData(BudgetUnit budgetUnit) throws Exception {
        String lockKey = "/finance-platform/unitRestartYearBudgetData" + budgetUnit.getId();
        ZookeeperShareLock zookeeperShareLock = new ZookeeperShareLock(this.curatorFramework, lockKey, null);
        try {
            zookeeperShareLock.tryLock();

            modifyUnitIsAlreadySynchronize(budgetUnit);

            addNewYearAgent(budgetUnit);

            //key为subjectid
            Map<Long, BudgetYearSubject> yearSubjectMap = yearSubjectMapper.selectList(new QueryWrapper<BudgetYearSubject>().eq("unitid", budgetUnit.getId())).stream().collect(Collectors.toMap(e -> e.getSubjectid(), e -> e, (e1, e2) -> e1));
            //获取当前部门所有的科目信息
            List<Map<String, Object>> subjectList = sysMapper.getAllSubjects(budgetUnit.getYearid(), budgetUnit.getId());
            Map<Long, Map<String, Object>> budgetSubjectmap = new HashMap<Long, Map<String, Object>>();
            List<BudgetYearSubjectHis> hisList = new ArrayList<>();
            List<BudgetYearSubject> newYearSubjectList = new ArrayList<>();
            List<BudgetYearSubject> updateYearSubjectList = new ArrayList<>();
            for (Map<String, Object> budgetSubject : subjectList) {
                //科目id
                Long subjectid = Long.valueOf(budgetSubject.get("subjectid").toString());
                //叶子节点
                Boolean leafflag = (Boolean) budgetSubject.get("leafflag");
                //本届收入占比公式
                String revenueformulastr = (String) budgetSubject.get("revenueformula");
                revenueformulastr = StringUtils.isEmpty(revenueformulastr) ? "" : revenueformulastr;
                revenueformulastr = StringUtils.isEmpty(revenueformulastr) ? budgetUnit.getRevenueformula() : revenueformulastr;
                //本届码洋占比公式
                String ccratioformulastr = (String) budgetSubject.get("ccratioformula");
                ccratioformulastr = StringUtils.isEmpty(ccratioformulastr) ? "" : ccratioformulastr;
                ccratioformulastr = StringUtils.isEmpty(ccratioformulastr) ? budgetUnit.getCcratioformula() : ccratioformulastr;
                //上届码洋占比公式
                String preccratioformulastr = (String) budgetSubject.get("preccratioformula");
                preccratioformulastr = StringUtils.isEmpty(preccratioformulastr) ? "" : preccratioformulastr;
                preccratioformulastr = StringUtils.isEmpty(preccratioformulastr) ? budgetUnit.getPreccratioformula() : preccratioformulastr;
                //
                String formula = (String) budgetSubject.get("formula");
                formula = StringUtils.isEmpty(formula) ? "" : formula;

                BudgetYearSubject budgetYearSubject = yearSubjectMap.get(subjectid);
                if (null == budgetYearSubject) {
                    budgetYearSubject = new BudgetYearSubject();
                }
                budgetYearSubject.setYearid(budgetUnit.getYearid());
                budgetYearSubject.setUnitid(budgetUnit.getId());
                budgetYearSubject.setSubjectid(subjectid);
                budgetYearSubject.setRevenueformulastr(revenueformulastr);
                budgetYearSubject.setCcratioformulastr(ccratioformulastr);
                budgetYearSubject.setPreccratioformulastr(preccratioformulastr);
                budgetYearSubject.setFormula(formula);
                budgetYearSubject.setCreatetime(new Date());
                budgetYearSubject.setUpdatetime(new Date());
                if (null == budgetYearSubject.getId()) {
                    long count = newYearSubjectList.stream().filter(e -> e.getUnitid().equals(budgetUnit.getId()) && e.getSubjectid().equals(subjectid)).count();
                    if (count == 0) newYearSubjectList.add(budgetYearSubject);
                } else {
                    updateYearSubjectList.add(budgetYearSubject);
                }
                if (leafflag && null != yearSubjectMap.get(subjectid)) {
                    BudgetYearSubjectHis budgetYearSubjectHis = new BudgetYearSubjectHis();
                    budgetYearSubjectHis.setYearid(budgetUnit.getYearid());
                    budgetYearSubjectHis.setUnitid(budgetUnit.getId());
                    budgetYearSubjectHis.setSubjectid(subjectid);
                    budgetYearSubjectHis.setCreatetime(new Date());
                    hisList.add(budgetYearSubjectHis);
                }
                yearSubjectMap.put(subjectid, budgetYearSubject);
                budgetSubjectmap.put(subjectid, budgetSubject);
            }
            if (!hisList.isEmpty()) hisService.saveBatch(hisList);
            if (!newYearSubjectList.isEmpty()) yearSubjectService.saveBatch(newYearSubjectList);
            if (!updateYearSubjectList.isEmpty()) yearSubjectService.updateBatchById(updateYearSubjectList);
            //向上汇总
            upsumyearsubject(budgetUnit.getYearid(), budgetUnit, budgetSubjectmap);
            updateformulayearsubject(budgetUnit.getYearid(), budgetUnit.getId(), budgetSubjectmap);
        } catch (Exception e) {
            throw e;
        } finally {
            zookeeperShareLock.unLock();
        }
    }

    @Autowired
    private BudgetYearSubjectHisService hisService;

    @Autowired
    private BudgetYearSubjectService yearSubjectService;

    /**
     * 增加新的年度动因。
     *
     * @param budgetUnit
     */
    private void addNewYearAgent(BudgetUnit budgetUnit) {
        Date currentDate = new Date();
        //获取需新增的分解动因
        List<Map<String, Object>> newAddSplitYearAgent = sysMapper.getNewSplitYearAgent(budgetUnit.getId());
        List<BudgetYearAgent> newyearAgentList = new ArrayList<>();
        List<BudgetYearAgent> updateyearAgentList = new ArrayList<>();
        newAddSplitYearAgent.forEach(splitYearAgentMap -> {
            Long subjectid = Long.valueOf(splitYearAgentMap.get("subjectid").toString());
            String subjectname = splitYearAgentMap.get("subjectname").toString();
            //年度动因
            BudgetYearAgent budgetYearAgent = new BudgetYearAgent();
            budgetYearAgent.setSubjectid(subjectid);
            budgetYearAgent.setName(subjectname);
            budgetYearAgent.setYearid(budgetUnit.getYearid());
            budgetYearAgent.setUnitid(budgetUnit.getId());
            budgetYearAgent.setHappencount("");
            budgetYearAgent.setRemark("");
            budgetYearAgent.setElasticflag(false);
            budgetYearAgent.setCreatetime(currentDate);
            newyearAgentList.add(budgetYearAgent);
        });
        Map<Long, BudgetProductCategory> productCategoryMap = categoryMapper.selectList(null).stream().collect(Collectors.toMap(e -> e.getId(), e -> e, (e1, e2) -> e1));
        //获取勾选的所有产品
        List<Map<String, Object>> checkedProductList = sysMapper.getCheckedUnitProductList(budgetUnit.getId());
        //获取当前部门的产品科目
        List<Map<String, Object>> productSubjectList = sysMapper.getCurUnitProductSubjectList(budgetUnit.getId());
        //获取产品动因
        List<BudgetYearAgent> productYearAgentList = yearAgentMapper.selectList(new QueryWrapper<BudgetYearAgent>().eq("unitid", budgetUnit.getId()).isNotNull("productid"));
        Map<String, BudgetYearAgent> yearAgentMap = productYearAgentList.stream().collect(Collectors.toMap(e -> e.getProductid() + "_" + e.getSubjectid(), e -> e, (e1, e2) -> e1));

        for (Map<String, Object> checkedProductMap : checkedProductList) {
            //选中产品的产品分类父id
            String categoryPids = checkedProductMap.get("pids").toString();
            String productid = checkedProductMap.get("productid").toString();
            String productname = checkedProductMap.get("productname").toString();
            /**
             * 勾中了父类的产品分类，产品动因也同样要生成
             */
            productSubjectList.stream().filter(e -> {
                //该科目所勾选的所有产品分类id
                String procategoryid = e.get("procategoryid").toString();
                for (String categoryId : procategoryid.split(",")) {
                    String pids = productCategoryMap.get(Long.valueOf(categoryId)).getPids();
                    if (categoryPids.startsWith(pids)) return true;
                }
                return false;
            }).map(e -> e.get("subjectid").toString()).forEach(subjectid -> {
                BudgetYearAgent budgetYearAgent = yearAgentMap.get(productid + "_" + subjectid);
                if (null == budgetYearAgent) {
                    budgetYearAgent = new BudgetYearAgent();
                    budgetYearAgent.setHappencount("");
                    budgetYearAgent.setRemark("");
                }
                budgetYearAgent.setUnitid(budgetUnit.getId());
                budgetYearAgent.setName(productname);
                budgetYearAgent.setSubjectid(Long.valueOf(subjectid));
                budgetYearAgent.setYearid(budgetUnit.getYearid());
                budgetYearAgent.setProductid(Long.valueOf(productid));
                budgetYearAgent.setElasticflag(false);
                if (null != budgetYearAgent.getId()) {
                    budgetYearAgent.setUpdatetime(currentDate);
                    updateyearAgentList.add(budgetYearAgent);
                } else {
                    budgetYearAgent.setCreatetime(currentDate);
                    newyearAgentList.add(budgetYearAgent);
                }
                //防止重复生成
                yearAgentMap.put(productid + "_" + subjectid, budgetYearAgent);
            });
        }
        if (!newyearAgentList.isEmpty()) yearAgentService.saveBatch(newyearAgentList);
        if (!updateyearAgentList.isEmpty()) yearAgentService.updateBatchById(updateyearAgentList);
    }

    private List<String> extractMessageByRegular(String msg) {
        List<String> list = new ArrayList<String>();
        Pattern p = Pattern.compile("(\\[[^\\]]*\\])");
        Matcher m = p.matcher(msg);
        while (m.find()) {
            list.add(m.group().substring(1, m.group().length() - 1));
        }
        return list;
    }

    public final BigDecimal js(String formula, Float self) throws NumberFormatException, ScriptException {
        if (null != self) {
            formula = formula.replace("[this]", "(" + self.toString() + ")");
        }
        return new BigDecimal(jse.eval("(" + formula + ").toFixed(4)").toString());
    }

    /**
     * 更新占比码洋、收入、公式科目
     *
     * @param yearid
     * @param unitid
     * @param budgetSubjectmap
     */
    public void updateformulayearsubject(Long yearid, Long
            unitid, Map<Long, Map<String, Object>> budgetSubjectmap) {
        synchronized (unitid) {
            //预算单位
            BudgetUnit unit = this.unitMapper.selectById(unitid);
            List<BudgetYearSubject> yearsubjects = yearSubjectMapper.selectList(new QueryWrapper<BudgetYearSubject>().eq("unitid", unitid).eq("yearid", yearid));
            //key 为 id
            Map<Long, BudgetYearSubject> yearsubjectidmap = yearsubjects.stream().collect(Collectors.toMap(e -> e.getId(), e -> e, (e1, e2) -> e1));

            List<Map<String, Object>> yearsubjectmaps = sysMapper.getYearSubectList(yearid, unitid);
            //key 为 name
            //Map<String,Map<String,Object>> yearsubjectnamemap = yearsubjectmaps.stream().collect(Collectors.toMap(e->e.get("name").toString(), e->e,(e1,e2)->e2));
            //计算公式 科目
            Map<String, String> subjectformula = new HashMap<String, String>();
            for (Map<String, Object> monthsubjectmap : yearsubjectmaps) {
                String name = monthsubjectmap.get("name").toString();
                String formula = (String) monthsubjectmap.get("formula");
                BigDecimal total = new BigDecimal(monthsubjectmap.get("total").toString());
                if (StringUtils.isNotEmpty(formula)) {
                    formula = formula.replace("[this]", "(" + total + ")");
                    subjectformula.put(name, formula);
                }
            }
            //[销售收入]-[已销产品成本]-[间接制造费用]-[发运费用]-[销售费用]-[管理费用]-[库存报废成本]
            //计算过的科目
            Set<String> formulasubject = new HashSet<String>();
            //科目金额
            Map<String, BigDecimal> subjectformulaje = new HashMap<String, BigDecimal>();
            for (Map<String, Object> monthsubjectmap : yearsubjectmaps) {
                String name = monthsubjectmap.get("name").toString();
                BigDecimal total = new BigDecimal(monthsubjectmap.get("total").toString());
                subjectformulaje.put(name, total);
            }
            int i = 0;
            while (formulasubject.size() < subjectformula.size()) {
                for (String subjectname : subjectformula.keySet()) {
                    String formula = subjectformula.get(subjectname);
                    if (formulasubject.contains(subjectname)) {
                        continue;
                    }
                    List<String> list = extractMessageByRegular(formula);
                    if (null == list || list.size() <= 0) {
                        try {
                            BigDecimal ccratioformula = js(formula, null);
                            subjectformulaje.put(subjectname, ccratioformula);
                            formulasubject.add(subjectname);

                        } catch (NumberFormatException | ScriptException e) {
                            e.printStackTrace();
                        }
                    } else {
                        for (String subjectname_ : list) {
                            if (formulasubject.contains(subjectname_)
                                    || !subjectformula.containsKey(subjectname_)
                            ) {
                                formula = formula.replace("[" + subjectname_ + "]", "(" + subjectformulaje.get(subjectname_) + ")");
                            }
                        }
                        if (!formula.contains("[") && !formula.contains("]")) {
                            try {
                                BigDecimal ccratioformula = js(formula, null);
                                subjectformulaje.put(subjectname, ccratioformula);
                                formulasubject.add(subjectname);

                            } catch (NumberFormatException | ScriptException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    subjectformula.put(subjectname, formula);
                }
                if (i > 20) {
                    break;
                }
                i++;
            }
            //上级预算单位
            List<Map<String, Object>> psubjectlist = sysMapper.getParentUnitSubjectDataList(unit.getParentid());
            Map<String, BigDecimal> psubjectmap = psubjectlist.stream().collect(Collectors.toMap(e -> e.get("name").toString(), e -> new BigDecimal(e.get("total").toString())));
            //用父预算的科目替代
            for (String subjectname : subjectformula.keySet()) {
                String formula = subjectformula.get(subjectname);
                List<String> list = extractMessageByRegular(formula);
                if (null != list && list.size() > 0) {
                    for (String subjectname_ : list) {
                        //formula = formula.replace("["+subjectname_+"]", "(0)");
                        if (null != psubjectmap.get(subjectname_)) {
                            formula = formula.replace("[" + subjectname_ + "]", "(" + psubjectmap.get(subjectname_).toString() + ")");
                        }
                    }
                    try {
                        BigDecimal ccratioformula = js(formula, null);
                        subjectformulaje.put(subjectname, ccratioformula);
                        formulasubject.add(subjectname);
                    } catch (Exception e) {
                    }
                }
            }
            //在持续一遍
            for (String subjectname : subjectformula.keySet()) {
                String formula = subjectformula.get(subjectname);
                List<String> list = extractMessageByRegular(formula);

                if (null != list && list.size() > 0) {
                    for (String subjectname_ : list) {
                        formula = formula.replace("[" + subjectname_ + "]", "(0)");
                    }
                    try {
                        BigDecimal ccratioformula = js(formula, null);
                        subjectformulaje.put(subjectname, ccratioformula);
                        formulasubject.add(subjectname);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            for (Map<String, Object> yearsubjectmap : yearsubjectmaps) {
                Long id = Long.valueOf(yearsubjectmap.get("id").toString());
                Long subjectid = Long.valueOf(yearsubjectmap.get("unitsubjectid").toString());
                String name = yearsubjectmap.get("name").toString();
                BudgetYearSubject budgetYearSubject = yearsubjectidmap.get(id);
                //公式项
                String formula = (String) yearsubjectmap.get("formula");
                BigDecimal total = new BigDecimal(yearsubjectmap.get("total").toString());
                BigDecimal preestimate = new BigDecimal(yearsubjectmap.get("preestimate").toString());
                //修改标识
                boolean updateflag = false;
                //公式
                if (StringUtils.isNotEmpty(formula)) {
                    subjectformulaje.get(name);
                    budgetYearSubject.setTotal(subjectformulaje.get(name));

                    Long tmpsubjectid = subjectid;
                    boolean flag = true;
                    while (flag && budgetYearSubject.getTotal().compareTo(total) != 0) {
                        //updatemonthsubjectbyinit(agent.getYearid(), agent.getUnitid(), agent.getMonthid(),tmpsubjectid, agent.getTotal());
                        //获取上级科目 parentid
                        Map<String, Object> budgetSubject = budgetSubjectmap.get(tmpsubjectid);
                        //科目公式
                        //上级科目id
                        Long parentid = Long.valueOf(budgetSubject.get("parentid").toString());
                        //向上汇总
                        Boolean upsumflag = (Boolean) budgetSubject.get("upsumflag");
                        if (upsumflag && null != budgetSubjectmap.get(parentid)) {
                            tmpsubjectid = parentid;

                            budgetSubject = budgetSubjectmap.get(tmpsubjectid);
                            //科目公式
                            String formula_ = (String) budgetSubject.get("formula");
                            if (StringUtils.isNotEmpty(formula_)) {
                                flag = false;
                            }
                            if (true == flag) {
                                updateyearsubjectbyinit(yearid, unitid, tmpsubjectid, budgetYearSubject.getTotal().subtract(total), new BigDecimal(0), new BigDecimal(0));
                                if (0 != unit.getParentid()) {
                                    updateyearsubjectbyinit(yearid, unit.getParentid(), tmpsubjectid, budgetYearSubject.getTotal().subtract(total), new BigDecimal(0), new BigDecimal(0));
                                }
                            }
                        } else {
                            flag = false;
                        }
                    }
                    //上届呆账率
                    //试卷发样码洋
                    //试卷入库码洋
		    		/*formula = repalceformulasstr(formula, yearsubjectnamemap, "total", total);
		    		try {
		    			BigDecimal ccratioformula = js(formula, null);
		    			budgetYearSubject.setTotal(ccratioformula);
		    			updateflag = true;
		    		}catch(Exception e) {}*/
                }
                //本届码洋
                String ccratioformulastr = (String) yearsubjectmap.get("ccratioformulastr");
                if (StringUtils.isNotEmpty(ccratioformulastr)) {
                    ccratioformulastr = ccratioformulastr.replace("[this]", "(" + budgetYearSubject.getTotal() + ")");
                    List<String> list = extractMessageByRegular(ccratioformulastr);
                    if (null != list && list.size() > 0) {
                        for (String s : list) {
                            if (subjectformulaje.containsKey(s)) {
                                ccratioformulastr = ccratioformulastr.replace("[" + s + "]", "(" + subjectformulaje.get(s) + ")");
                            }
                        }
                    }
                    //上级预算单位
                    list = extractMessageByRegular(ccratioformulastr);
                    if (null != list && list.size() > 0) {
                        for (String s : list) {
                            if (null != psubjectmap.get(s)) {
                                ccratioformulastr = ccratioformulastr.replace("[" + s + "]", "(" + psubjectmap.get(s) + ")");
                            }
                        }
                    }
                    try {
                        BigDecimal result = js(ccratioformulastr, null);
                        budgetYearSubject.setCcratioformula(result);
                        updateflag = true;
                    } catch (Exception e) {
                    }
                } else {
                    budgetYearSubject.setCcratioformula(new BigDecimal("0"));
                    updateflag = true;
                }
                //上届码洋
                String preccratioformulastr = (String) yearsubjectmap.get("preccratioformulastr");
                if (StringUtils.isNotEmpty(preccratioformulastr)) {
                    preccratioformulastr = preccratioformulastr.replace("[this]", "(" + budgetYearSubject.getPreestimate() + ")");
                    List<String> list = extractMessageByRegular(preccratioformulastr);
                    if (null != list && list.size() > 0) {
                        for (String s : list) {
                            if (subjectformulaje.containsKey(s)) {
                                preccratioformulastr = preccratioformulastr.replace("[" + s + "]", "(" + subjectformulaje.get(s) + ")");
                            }
                        }
                    }
                    //上级预算单位
                    list = extractMessageByRegular(preccratioformulastr);
                    if (null != list && list.size() > 0) {
                        for (String s : list) {
                            if (null != psubjectmap.get(s)) {
                                preccratioformulastr = preccratioformulastr.replace("[" + s + "]", "(" + psubjectmap.get(s) + ")");
                            }
                        }
                    }
                    try {
                        BigDecimal result = js(preccratioformulastr, null);
                        budgetYearSubject.setPreccratioformula(result);
                        updateflag = true;
                    } catch (Exception e) {
                    }
                } else {
                    budgetYearSubject.setPreccratioformula(new BigDecimal("0"));
                    updateflag = true;
                }
                //本届收入
                String revenueformulastr = (String) yearsubjectmap.get("revenueformulastr");
                if (StringUtils.isNotEmpty(revenueformulastr)) {
                    revenueformulastr = revenueformulastr.replace("[this]", "(" + budgetYearSubject.getTotal() + ")");
                    List<String> list = extractMessageByRegular(revenueformulastr);
                    if (null != list && list.size() > 0) {
                        for (String s : list) {
                            if (subjectformulaje.containsKey(s)) {
                                revenueformulastr = revenueformulastr.replace("[" + s + "]", "(" + subjectformulaje.get(s) + ")");
                            }
                        }
                    }
                    //上级预算单位
                    list = extractMessageByRegular(revenueformulastr);
                    if (null != list && list.size() > 0) {
                        for (String s : list) {
                            if (null != psubjectmap.get(s)) {
                                revenueformulastr = revenueformulastr.replace("[" + s + "]", "(" + psubjectmap.get(s) + ")");
                            }
                        }
                    }
                    try {
                        BigDecimal result = js(revenueformulastr, null);
                        budgetYearSubject.setRevenueformula(result);
                        updateflag = true;
                    } catch (Exception e) {
                    }
                } else {
                    budgetYearSubject.setRevenueformula(new BigDecimal("0"));
                    updateflag = true;
                }
                //上届收入
                revenueformulastr = (String) yearsubjectmap.get("revenueformulastr");
                if (StringUtils.isNotEmpty(revenueformulastr)) {
                    revenueformulastr = revenueformulastr.replace("[this]", "(" + budgetYearSubject.getPreestimate() + ")");
                    List<String> list = extractMessageByRegular(revenueformulastr);
                    if (null != list && list.size() > 0) {
                        for (String s : list) {
                            revenueformulastr = revenueformulastr.replace("[" + s + "]", "(" + subjectformulaje.get(s) + ")");
                        }
                    }
                    try {
                        BigDecimal result = js(revenueformulastr, null);
                        budgetYearSubject.setPrerevenueformula(result);
                        updateflag = true;
                    } catch (Exception e) {
                    }
                } else {
                    budgetYearSubject.setPrerevenueformula(new BigDecimal("0"));
                    updateflag = true;
                }

                //确定要修改了
                if (true == updateflag) {
                    this.yearSubjectMapper.updateById(budgetYearSubject);
                }
            }

        }

    }


    private void upsumyearsubject(Long yearid, BudgetUnit
            unit, Map<Long, Map<String, Object>> budgetSubjectmap) {
        sysMapper.clearYearSubject(yearid, unit.getId());
        sysMapper.clearYearSubjectHis(yearid, unit.getId());
        List<BudgetYearAgent> yearagentlist = this.sysMapper.getYearAgents(yearid, unit.getId(), unit.getId());
        //科目向上
        for (BudgetYearAgent agent : yearagentlist) {
            Long subjectid = agent.getSubjectid();
            Long tmpsubjectid = subjectid;
            boolean flag = true;
            while (flag) {
                //获取上级科目 parentid
                Map<String, Object> budgetSubject = budgetSubjectmap.get(tmpsubjectid);
                String formulaflag = budgetSubject.get("formulaflag") == null ? "0" : budgetSubject.get("formulaflag").toString();
                //科目公式
                String formula = (String) budgetSubject.get("formula");
                if (StringUtils.isNotEmpty(formula) && "1".equals(formulaflag)) {
                    flag = false;
                }
                if (flag) {
                    updateyearsubjectbyinit(agent.getYearid(), unit.getId(), tmpsubjectid, agent.getTotal(), agent.getPretotal(), agent.getPreestimate());
                }
                //上级科目id
                Long parentid = Long.valueOf(budgetSubject.get("parentid").toString());
                //向上汇总
                Boolean upsumflag = (Boolean) budgetSubject.get("upsumflag");
                if (null != upsumflag && true == upsumflag && null != budgetSubjectmap.get(parentid) && StringUtils.isEmpty(formula)) {
                    tmpsubjectid = parentid;
                } else {
                    flag = false;
                }
            }
        }
    }


    public void updateyearsubjectbyinit(Long yearid, Long unitid, Long subjectid, BigDecimal total, BigDecimal
            pretotal, BigDecimal preestimate) {
        String key = yearid + "_" + unitid + "_" + subjectid;
        synchronized (key) {
            //年度
            BudgetYearSubject budgetYearSubject = this.yearSubjectMapper.selectOne(new QueryWrapper<BudgetYearSubject>().eq("unitid", unitid).eq("yearid", yearid).eq("subjectid", subjectid));
            BudgetSubject budgetSubject = subjectMapper.selectById(subjectid);
            List<BudgetYearSubjectHis> yearSubjectHisList = hisMapper.selectList(new QueryWrapper<BudgetYearSubjectHis>().eq("yearid", yearid).eq("unitid", unitid).eq("subjectid", subjectid));
            budgetYearSubject.setTotal(budgetYearSubject.getTotal().add(total));
            budgetYearSubject.setPretotal(budgetYearSubject.getPretotal().add(pretotal));
            budgetYearSubject.setPreestimate(budgetYearSubject.getPreestimate().add(preestimate));
            this.yearSubjectMapper.updateById(budgetYearSubject);
            if (!yearSubjectHisList.isEmpty()) {
                List<Long> ids = yearSubjectHisList.stream().map(e -> e.getId()).collect(Collectors.toList());
                this.sysMapper.refreshTotal(budgetYearSubject.getTotal(), ids);
            } else if (budgetSubject.getLeafflag() && yearSubjectHisList.isEmpty()) {
                BudgetYearSubjectHis budgetYearSubjectHis = new BudgetYearSubjectHis();
                budgetYearSubjectHis.setYearid(yearid);
                budgetYearSubjectHis.setUnitid(unitid);
                budgetYearSubjectHis.setSubjectid(subjectid);
                budgetYearSubjectHis.setType(0);
                budgetYearSubjectHis.setTotal(budgetYearSubject.getTotal());
                budgetYearSubjectHis.setCreatetime(new Date());
                this.hisMapper.insert(budgetYearSubjectHis);
            }
        }
    }

    /**
     * 更新部门的状态为已同步（提交预算的时候就不需要提示）
     *
     * @param budgetUnit
     */
    private void modifyUnitIsAlreadySynchronize(BudgetUnit budgetUnit) {
        budgetUnit.setUpdateagentflag(false);
        budgetUnit.setCalculatesubjectflag(true);
        budgetUnit.setUpdatetime(new Date());
        unitMapper.updateById(budgetUnit);
    }

    /**
     * 校验是否能够启动年度预算
     *
     * @param unitList
     */
    private void checkIsCanStartUpYearBudget(List<BudgetUnit> unitList) {
        if (CollectionUtils.isEmpty(unitList)) throw new RuntimeException("请先添加预算单位后再启动年度预算。");
        /**
         * 如果存在分解科目，需先设置分解科目的部门权限
         */
        List<Map<String, Object>> splitSubjectList = sysMapper.getSplitSubjectData(unitList.get(0).getYearid());
        if (!splitSubjectList.isEmpty()) {
            //未设置分解部门的科目
            List<String> unSetSplitUnitSubjectList = new ArrayList<>();
            splitSubjectList.stream().collect(Collectors.groupingBy(e -> e.get("subjectid").toString())).forEach((subjectid, list) -> {
                long count = list.stream().filter(e -> e.get("splitflag") != null && "true".equals(e.get("splitflag").toString())).count();
                if (count == 0) unSetSplitUnitSubjectList.add(list.get(0).get("name").toString());
            });

            if (!unSetSplitUnitSubjectList.isEmpty()) {
                String subjectName = unSetSplitUnitSubjectList.stream().collect(Collectors.joining(","));
                throw new RuntimeException("启动失败！分解科目【" + subjectName + "】未设置分解预算单位");
            }
        }
    }

    /**
     * 同步年度预算科目执行数（向上汇总）
     */
    public void doSyncBudgetSubjectYearAddMoney(Long yearId, Long unitId, Long subjectId, BigDecimal money, int opt) {
        List<Long> subjectIds = getUpSumSubjectIds(subjectId);

        BudgetUnit budgetUnit = this.unitMapper.selectById(unitId);
        // 同步年度预算科目执行数
        this.budgetYearSubjectService.doSyncBudgetSubjectExecuteMoney(subjectIds, yearId, budgetUnit.getId(), opt, money);
        if (budgetUnit.getParentid() != 0) {
            this.budgetYearSubjectService.doSyncBudgetSubjectExecuteMoney(subjectIds, yearId, budgetUnit.getParentid(), opt, money);
        }
    }

    /**
     * 同步月度预算科目执行数（向上汇总）
     */
    public void doSyncBudgetSubjectMonthAddMoney(Long yearId, Long unitId, Long subjectId, Long monthId, BigDecimal money, int opt) {
        List<Long> subjectIds = getUpSumSubjectIds(subjectId);

        BudgetUnit budgetUnit = this.unitMapper.selectById(unitId);
        // 同步月度预算科目执行数
        this.budgetMonthSubjectService.doSyncBudgetSubjectExecuteMoney(subjectIds, yearId, budgetUnit.getId(), monthId, opt, money);
        if (budgetUnit.getParentid() != 0) {
            this.budgetMonthSubjectService.doSyncBudgetSubjectExecuteMoney(subjectIds, yearId, budgetUnit.getParentid(), monthId, opt, money);
        }
    }

    private List<Long> getUpSumSubjectIds(Long subjectId) {
        BudgetSubject budgetSubject = this.subjectMapper.selectById(subjectId);
        String[] split = budgetSubject.getPids().split("-");

        BudgetSubject tmpSubject;
        List<Long> subjectIds = new ArrayList<>();
        for (int i = split.length - 1; i >= 0; i--) {
            String id = split[i];
            if (id.equals(budgetSubject.getId() + "")) {
                tmpSubject = budgetSubject;
            } else {
                tmpSubject = this.subjectMapper.selectById(id);
            }
            subjectIds.add(tmpSubject.getId());

            // 判断是否向上汇总
            if (!tmpSubject.getUpsumflag()) {
                break;
            }
        }
        return subjectIds;
    }

    /**
     * 借款
     */
    public void lendMoney(BudgetLendmoney budgetLendMoney) throws Exception {
        String lockKey = "/finance-platform/lendMoney/" + budgetLendMoney.getEmpno();
        ZookeeperShareLock zookeeperShareLock = new ZookeeperShareLock(this.curatorFramework, lockKey, null);
        try {
            zookeeperShareLock.tryLock();
            // 生成付款单
            Date currentDate = new Date();

            BudgetArrears arrears = this.budgetArrearsMapper.selectOne(new QueryWrapper<BudgetArrears>()
                    .eq("empno", budgetLendMoney.getEmpno()));
            if (arrears == null) {
                // 新增
                BudgetArrears insertArrears = new BudgetArrears();
                insertArrears.setEmpid(budgetLendMoney.getEmpid());
                insertArrears.setEmpno(budgetLendMoney.getEmpno());
                insertArrears.setEmpname(budgetLendMoney.getEmpname());
                insertArrears.setLendmoney(budgetLendMoney.getLendmoney());
                insertArrears.setInterestmoney(budgetLendMoney.getInterestmoney());
                insertArrears.setRepaymoney(BigDecimal.ZERO);
                insertArrears.setArrearsmoeny(budgetLendMoney.getLendmoney().add(budgetLendMoney.getInterestmoney()));
                insertArrears.setCreatetime(currentDate);
                insertArrears.setUpdatetime(currentDate);
                this.budgetArrearsMapper.insert(insertArrears);
            } else {
                arrears.setLendmoney(arrears.getLendmoney().add(budgetLendMoney.getLendmoney()));
                arrears.setInterestmoney(arrears.getInterestmoney().add(budgetLendMoney.getInterestmoney()));
                arrears.setArrearsmoeny(arrears.getLendmoney().add(arrears.getInterestmoney()).subtract(arrears.getRepaymoney()));
                arrears.setUpdatetime(currentDate);
                this.budgetArrearsMapper.updateById(arrears);
            }

            BigDecimal curMoney = arrears == null ? BigDecimal.ZERO : arrears.getArrearsmoeny();
            BudgetLendandrepaymoney insertLendAndRepay = new BudgetLendandrepaymoney();
            insertLendAndRepay.setEmpid(budgetLendMoney.getEmpid());
            insertLendAndRepay.setEmpno(budgetLendMoney.getEmpno());
            insertLendAndRepay.setEmpname(budgetLendMoney.getEmpname());
            insertLendAndRepay.setLendmoneyid(budgetLendMoney.getId());
            insertLendAndRepay.setCurmoney(curMoney);
            insertLendAndRepay.setMoney(budgetLendMoney.getLendmoney().add(budgetLendMoney.getInterestmoney()));
            insertLendAndRepay.setMoneytype(1);
            insertLendAndRepay.setNowmoney(curMoney.add(budgetLendMoney.getLendmoney().add(budgetLendMoney.getInterestmoney())));
            insertLendAndRepay.setCreatetime(currentDate);
            insertLendAndRepay.setRemark(budgetLendMoney.getRemark());
            this.budgetLendandrepaymoneyMapper.insert(insertLendAndRepay);

            BudgetLendmoney updateLendMoney = new BudgetLendmoney();
            updateLendMoney.setId(budgetLendMoney.getId());
            updateLendMoney.setConfirmflag(true);
            updateLendMoney.setEffectflag(true);
            this.budgetLendmoneyMapper.updateById(updateLendMoney);
        } finally {
            zookeeperShareLock.unLock();
        }
    }

}
