package com.jtyjy.finance.manager.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.utils.TreeUtil;
import com.jtyjy.finance.manager.vo.BudgetSubjectVO;
import com.jtyjy.finance.manager.vo.BudgetUnitTree;
import com.jtyjy.finance.manager.vo.BudgetUnitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.easyexcel.YearAgentCollectExcelData;
import com.jtyjy.finance.manager.easyexcel.YearAgentDetailExcelData;
import com.jtyjy.finance.manager.mapper.BudgetSubjectMapper;
import com.jtyjy.finance.manager.mapper.BudgetUnitMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearAgentMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearStartupMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearSubjectMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.utils.NumberUtil;
import com.jtyjy.finance.manager.vo.BudgetYearSubjectVO;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetYearSubjectService extends DefaultBaseService<BudgetYearSubjectMapper, BudgetYearSubject> {

    private final TabChangeLogMapper loggerMapper;
    private final BudgetUnitMapper budgetUnitMapper;
    private final BudgetSubjectMapper budgetSubjectMapper;
    private final BudgetYearStartupMapper budgetYearStartupMapper;
    private final BudgetYearSubjectMapper budgetYearSubjectMapper;
    private final BudgetYearAgentMapper budgetYearAgentMapper;
    private final BudgetYearPeriodMapper budgetYearPeriodMapper;

    @Autowired
    private BudgetSysService budgetSysService;
    @Autowired
    private BudgetYearSubjectHisService budgetYearSubjectHisService;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_year_subject"));
    }

    // ???????????? ----------------------------------------------------------------------------------------------------

    /**
     * ?????????????????????(????????????)
     */
    public List<BudgetUnitTree> listUnit(Long yearId) {
        List<BudgetUnitVO> unitList = this.budgetUnitMapper.getBudgetUnit(yearId, null, JdbcSqlThreadLocal.get(), UserThreadLocal.get().getUserId());

        // ??????pids
        List<String> subjectIds = unitList.stream().map(BudgetUnitVO::getPids).collect(Collectors.toList());
        if (subjectIds.isEmpty()) {
            return new ArrayList<>();
        }

        // ?????????????????????ids
        HashSet<String> ids = new HashSet<>();
        subjectIds.forEach(v -> {
            String[] split = v.split("-");
            ids.addAll(Arrays.asList(split.clone()));
        });

        List<BudgetUnit> budgetUnits = this.budgetUnitMapper.selectList(new QueryWrapper<BudgetUnit>()
                .in("id", ids)
                .orderByAsc("orderno"));

        List<BudgetUnitTree> resultList = new ArrayList<>();
        budgetUnits.forEach(v -> {
            BudgetUnitTree unitTree = new BudgetUnitTree();
            unitTree.setId(v.getId());
            unitTree.setParentId(v.getParentid());
            unitTree.setName(v.getName());
            unitTree.setBaseUnitId(v.getBaseunitid());
            resultList.add(unitTree);
        });

        // ???????????????????????????????????????????????????
        List<BudgetUnitTree> build = TreeUtil.build(resultList);
        TreeUtil.judgeLeafAfter(build);
        TreeUtil.excludeParentNode(build);
        return build;
    }

    /**
     * ??????????????????
     */
    public List<BudgetYearSubjectVO> yearAgentSubject(Long budgetUnitId) {
        return this.budgetYearSubjectMapper.listYearSubjectByUnitId(budgetUnitId);
    }

    /**
     * ??????????????????
     */
    public void syncYearAgentData(Long budgetUnitId) throws Exception {
        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
        if (budgetUnit == null) {
            throw new RuntimeException("????????????Id??????");
        }

        // ??????????????????????????????
        BudgetYearStartup yearStartup = this.budgetYearStartupMapper.selectOne(new QueryWrapper<BudgetYearStartup>().eq("yearId", budgetUnit.getYearid()));
        if (yearStartup == null || !yearStartup.getStartbudgetflag()) {
            throw new RuntimeException("???????????????????????????????????????");
        }

        // ?????????????????????????????????
        this.budgetSysService.unitRestartYearBudgetData(budgetUnit);
        if (budgetUnit.getParentid() != 0) {
            budgetUnit = this.budgetUnitMapper.selectById(budgetUnit.getParentid());
            if (budgetUnit != null) {
                this.budgetSysService.unitRestartYearBudgetData(budgetUnit);
            }
        }
    }

    /**
     * ??????????????????
     */
    public void submitYearBudget(Long budgetUnitId, String userId, String displayName) {
        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
        if (budgetUnit == null) {
            throw new RuntimeException("????????????Id??????");
        }
        // ?????????????????????????????????
        Integer count = this.budgetUnitMapper.selectCount(new QueryWrapper<BudgetUnit>()
                .eq("parentid", budgetUnit.getId()));
        if (count > 0) {
            throw new RuntimeException("??????????????????????????????????????????????????????????????????");
        }

        if (!budgetUnit.getBudgetflag()) {
            throw new RuntimeException("?????????????????????????????????, ??????????????????");
        } else if (budgetUnit.getRequeststatus() >= 1) {
            throw new RuntimeException("?????????????????????????????????????????????");
        } else if (!budgetUnit.getCalculatesubjectflag()) {
            throw new RuntimeException("??????????????????????????????????????????");
        }

        // ??????????????????????????????
        BudgetYearStartup yearStartup = this.budgetYearStartupMapper.selectOne(new QueryWrapper<BudgetYearStartup>().eq("yearId", budgetUnit.getYearid()));
        if (yearStartup == null || !yearStartup.getStartbudgetflag()) {
            throw new RuntimeException("???????????????????????????????????????");
        }

        // ??????????????????????????????
        count = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>().eq("unitid", budgetUnitId));
        if (count <= 0) {
            throw new RuntimeException("????????????????????????????????????????????????");
        }

        Date currentDate = new Date();
        budgetUnit.setSubmitflag(true);
        budgetUnit.setRequeststatus(1);
        budgetUnit.setUpdatetime(currentDate);
        budgetUnit.setSubmittime(currentDate);
        budgetUnit.setSubmitorid(userId);
        budgetUnit.setSubmitorname(displayName);
        this.budgetUnitMapper.updateById(budgetUnit);
    }

    /**
     * ??????????????????????????????
     */
    public List<YearAgentDetailExcelData> exportYearAgentDetail(Long budgetUnitId) {
        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
        if (budgetUnit == null) {
            throw new RuntimeException("????????????Id??????");
        }

        HashMap<Long, String> subjectNameMap = new HashMap<>(5);
        BudgetYearPeriod yearPeriod = this.budgetYearPeriodMapper.selectById(budgetUnit.getYearid());
        List<BudgetYearAgent> budgetYearAgents = this.budgetYearAgentMapper.selectList(new QueryWrapper<BudgetYearAgent>()
                .eq("unitid", budgetUnit.getId()));

        BigDecimal decimal = new BigDecimal("100");
        List<YearAgentDetailExcelData> resultList = new ArrayList<>();
        budgetYearAgents.forEach(v -> {
            YearAgentDetailExcelData excelData = new YearAgentDetailExcelData();
            excelData.setYearName(yearPeriod.getPeriod());
            excelData.setUnitName(budgetUnit.getName());
            excelData.setBudgetSubjectName(getSubjectNameById(v.getSubjectid(), subjectNameMap));
            excelData.setName(v.getName());
            excelData.setRemark(v.getRemark());
            excelData.setElasticFlag(v.getElasticflag() ? "???" : "???");
            excelData.setElasticRatio(v.getElasticratio() != null ? v.getElasticratio().multiply(decimal).setScale(2, BigDecimal.ROUND_HALF_UP) + "%" : "");
            excelData.setElasticMax(v.getElasticmax() != null ? NumberUtil.subZeroAndDot(v.getElasticmax()).toString() : "");
            excelData.setProportionSubjectName(getSubjectNameById(v.getBudgetsubjectid(), subjectNameMap));
            excelData.setPreEstimate(v.getPreestimate().setScale(2, BigDecimal.ROUND_HALF_UP));
            excelData.setTotal(v.getTotal().setScale(2, BigDecimal.ROUND_HALF_UP));
            excelData.setHappenCount(v.getHappencount());
            excelData.setComputingProcess(v.getComputingprocess());
            excelData.setM6(v.getM6());
            excelData.setM7(v.getM7());
            excelData.setM8(v.getM8());
            excelData.setM9(v.getM9());
            excelData.setM10(v.getM10());
            excelData.setM11(v.getM11());
            excelData.setM12(v.getM12());
            excelData.setM1(v.getM1());
            excelData.setM2(v.getM2());
            excelData.setM3(v.getM3());
            excelData.setM4(v.getM4());
            excelData.setM5(v.getM5());
            resultList.add(excelData);
        });
        return resultList;
    }

    private String getSubjectNameById(Long subjectId, HashMap<Long, String> subjectNameMap) {
        String subjectName = "";
        if (subjectId != null) {
            subjectName = subjectNameMap.get(subjectId);
            if (subjectName == null) {
                BudgetSubject budgetSubject = this.budgetSubjectMapper.selectById(subjectId);
                if (budgetSubject != null) {
                    subjectNameMap.put(subjectId, budgetSubject.getName());
                    return budgetSubject.getName();
                }
            }
        }
        return subjectName;
    }

    /**
     * ??????????????????????????????
     */
    public List<YearAgentCollectExcelData> exportYearAgentCollect(Long budgetUnitId) {
        List<BudgetYearSubjectVO> budgetYearSubjects = this.budgetYearSubjectMapper.listYearSubjectByUnitId(budgetUnitId);
        if (budgetYearSubjects.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, BudgetYearSubjectVO> collect = budgetYearSubjects.stream().collect(Collectors.toMap(BudgetYearSubjectVO::getId, Function.identity()));

        // ???????????????
        List<BudgetSubjectVO> subjectList = new ArrayList<>();
        budgetYearSubjects.forEach(v -> {
            BudgetSubjectVO subjectVO = new BudgetSubjectVO();
            subjectVO.setId(v.getId());
            subjectVO.setParentId(v.getParentId());
            subjectVO.setName(v.getBudgetSubjectName());
            subjectList.add(subjectVO);
        });
        List<BudgetSubjectVO> treeList = TreeUtil.build(subjectList);

        List<YearAgentCollectExcelData> resultList = new ArrayList<>();
        putYearAgentCollectExcelData(treeList, collect, resultList, "");
        return resultList;
    }

    private void putYearAgentCollectExcelData(List<BudgetSubjectVO> treeList, Map<Long, BudgetYearSubjectVO> hashMap, List<YearAgentCollectExcelData> resultList, String space) {
        BigDecimal decimal = new BigDecimal("100");

        for (BudgetSubjectVO node : treeList) {
            if (!hashMap.containsKey(node.getId())) {
                continue;
            }
            BudgetYearSubjectVO v = hashMap.get(node.getId());
            YearAgentCollectExcelData rowData = new YearAgentCollectExcelData();
            // ????????????
            rowData.setSubjectName(space + v.getBudgetSubjectName());
            // ??????????????????
            rowData.setSubjectCode(v.getBudgetSubjectCode());
            // ????????????
            rowData.setPreTotal(v.getPreTotal().setScale(2, BigDecimal.ROUND_HALF_UP));

            // ????????????-??????
            rowData.setPreEstimate(v.getPreEstimate().setScale(2, BigDecimal.ROUND_HALF_UP));
            // ????????????-????????????
            rowData.setPreCcRatioFormula(v.getPreCcRatioFormula().multiply(decimal).setScale(2, BigDecimal.ROUND_HALF_UP) + "%");
            // ????????????-????????????
            rowData.setPreRevenueFormula(v.getPreRevenueFormula().multiply(decimal).setScale(2, BigDecimal.ROUND_HALF_UP) + "%");

            // ????????????-??????
            rowData.setTotal(v.getTotal().setScale(2, BigDecimal.ROUND_HALF_UP));
            // ????????????-????????????
            rowData.setCcRatioFormula(v.getCcRatioFormula().multiply(decimal).setScale(2, BigDecimal.ROUND_HALF_UP) + "%");
            // ????????????-????????????
            rowData.setRevenueFormula(v.getRevenueFormula().multiply(decimal).setScale(2, BigDecimal.ROUND_HALF_UP) + "%");

            resultList.add(rowData);
            // ??????????????????
            if (node.getChildren() != null) {
                putYearAgentCollectExcelData(node.getChildren(), hashMap, resultList, space + "  ");
            }
        }
    }

    /**
     * ???????????????????????????
     *
     * @param realSubjectIds ????????????????????????????????????
     * @param yearId         ????????????
     * @param unitId         ??????????????????
     * @param opt            1????????? 2????????? 3????????? 4?????????
     * @param money          ????????????
     */
    public void doSyncBudgetSubjectExecuteMoney(List<Long> realSubjectIds, long yearId, long unitId, int opt, BigDecimal money) {
        QueryWrapper<BudgetYearSubject> wrapper = new QueryWrapper<>();
        wrapper.eq("yearid", yearId);
        wrapper.eq("unitid", unitId);
        wrapper.in("subjectid", realSubjectIds);
        List<BudgetYearSubject> list = this.list(wrapper);
        if (list != null && list.size() > 0) {
            List<BudgetYearSubjectHis> hisList = new ArrayList<>();
            BudgetYearSubjectHis budgetYearSubjectHis;
            for (BudgetYearSubject bean : list) {
                // ????????????
                budgetYearSubjectHis = JSON.parseObject(JSON.toJSONString(bean), BudgetYearSubjectHis.class);
                budgetYearSubjectHis.setType(opt);
                budgetYearSubjectHis.setCreatetime(new Date());
                budgetYearSubjectHis.setUpdatetime(new Date());
                // budgetYearSubjectHis
                budgetYearSubjectHis.setTotal(bean.getTotal());
                // before
                budgetYearSubjectHis.setBeforeaddmoney(bean.getAddmoney());
                budgetYearSubjectHis.setBeforeexecutemoney(bean.getExecutemoney());
                budgetYearSubjectHis.setBeforelendinmoney(bean.getLendinmoney());
                budgetYearSubjectHis.setBeforelendoutmoney(bean.getLendoutmoney());
                // ???????????? 1????????? 2????????? 3????????? 4?????????
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
                budgetYearSubjectHis.setAfteraddmoney(bean.getAddmoney());
                budgetYearSubjectHis.setAfterexecutemoney(bean.getExecutemoney());
                budgetYearSubjectHis.setAfterlendinmoney(bean.getLendinmoney());
                budgetYearSubjectHis.setAfterlendoutmoney(bean.getLendoutmoney());
            }
            // ???????????????
            this.updateBatchById(list);
            // ??????????????????
            this.budgetYearSubjectHisService.saveBatch(hisList);
        }
    }
}
