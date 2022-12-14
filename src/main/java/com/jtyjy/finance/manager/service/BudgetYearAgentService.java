package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.dto.UpdateBudgetYearAgentDTO;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.utils.TreeUtil;
import com.jtyjy.finance.manager.vo.BudgetSubjectVO;
import com.jtyjy.finance.manager.vo.BudgetYearAgentVO;
import com.jtyjy.finance.manager.vo.ExcelBean;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetYearAgentService extends DefaultBaseService<BudgetYearAgentMapper, BudgetYearAgent> {

    private final TabChangeLogMapper loggerMapper;
    private final BudgetYearAgentMapper budgetYearAgentMapper;
    private final BudgetMonthAgentMapper budgetMonthAgentMapper;
    private final BudgetSubjectMapper budgetSubjectMapper;
    private final BudgetUnitMapper budgetUnitMapper;
    private final BudgetYearStartupMapper budgetYearStartupMapper;
    private final BudgetProductMapper budgetProductMapper;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_year_agent"));
    }

    /**
     * ??????????????????
     */
    public List<BudgetSubjectVO> listSubject(Long yearId, Long baseUnitId, Integer type) {
        // ????????????
        BudgetUnit budgetUnit = budgetUnitMapper.selectOne(new QueryWrapper<BudgetUnit>()
                .eq("yearId", yearId)
                .eq("baseunitid", baseUnitId));

        // ????????????pids
        List<String> subjectIds = budgetSubjectMapper.listSubjectIds(yearId, baseUnitId, type);
        if (budgetUnit == null || subjectIds.isEmpty()) {
            return new ArrayList<>();
        }

        // ???????????????ids
        HashSet<String> ids = new HashSet<>();
        subjectIds.forEach(v -> {
            String[] split = v.split("-");
            ids.addAll(Arrays.asList(split.clone()));
        });

        List<BudgetSubject> budgetSubjects = budgetSubjectMapper.selectList(new QueryWrapper<BudgetSubject>()
                .in("id", ids)
                .orderByAsc("orderno"));

        List<BudgetSubjectVO> resultList = new ArrayList<>();
        budgetSubjects.forEach(v -> {
            BudgetSubjectVO subjectVO = new BudgetSubjectVO();
            subjectVO.setId(v.getId());
            subjectVO.setParentId(v.getParentid());
            subjectVO.setName(v.getName());
            subjectVO.setLeaf(v.getLeafflag());
            subjectVO.setBudgetUnitId(budgetUnit.getId());
            resultList.add(subjectVO);
        });

        List<BudgetSubjectVO> build = TreeUtil.build(resultList);

        // ??????????????????????????????
        TreeUtil.excludeParentNode(build);
        return build;
    }

    /**
     * ??????????????????????????????
     */
    public PageResult<BudgetYearAgentVO> yearAgentPage(Long budgetUnitId, Long budgetSubjectId, String name, Integer page, Integer rows ,String category) {
        Page<BudgetYearAgentVO> pageBean = new Page<>(page, rows);

        List<BudgetYearAgentVO> budgetYearAgents = budgetYearAgentMapper.yearAgentPage(pageBean, budgetUnitId, budgetSubjectId, name,category);

        return PageResult.apply(pageBean.getTotal(), budgetYearAgents);
    }

    /**
     * ??????????????????
     */
    public void addYearAgent(UpdateBudgetYearAgentDTO bean) {
        // ????????????Id
        Long budgetSubjectId = bean.getBudgetSubjectId();
        // ????????????Id
        Long budgetUnitId = bean.getBudgetUnitId();

        // ??????????????????????????????
        BudgetSubject budgetSubject = this.budgetSubjectMapper.selectById(budgetSubjectId);
        if (budgetSubject == null) {
            throw new RuntimeException("?????????????????????Id??????");
        }
        if (budgetSubject.getFormulaflag() || budgetSubject.getJointproductflag() || budgetSubject.getCostsplitflag()) {
            throw new RuntimeException("??????????????????????????????????????????????????????????????????");
        }

        // ????????????????????????????????????
        Integer childCount = this.budgetSubjectMapper.selectCount(new QueryWrapper<BudgetSubject>().eq("parentid", budgetSubjectId));
        if (childCount != 0) {
            throw new RuntimeException("??????????????????????????????????????????");
        }

        // ??????????????????????????????
        BudgetYearStartup yearStartup = this.budgetYearStartupMapper.selectOne(new QueryWrapper<BudgetYearStartup>().eq("yearId", budgetSubject.getYearid()));
        if (yearStartup == null || !yearStartup.getStartbudgetflag()) {
            throw new RuntimeException("?????????????????????????????????????????????????????????");
        }

        // ????????????
        BudgetUnit budgetUnit = budgetUnitMapper.selectById(budgetUnitId);
        if (budgetUnit.getRequeststatus() >= 1) {
            throw new RuntimeException("???????????????????????????????????????????????????????????????");
        }

        // ?????????????????????????????????
        Integer count = this.budgetUnitMapper.selectCount(new QueryWrapper<BudgetUnit>()
                .eq("parentid", budgetUnit.getId()));
        if (count > 0) {
            throw new RuntimeException("??????????????????????????????????????????????????????????????????");
        }

        // ??????????????????????????????
        if (StringUtils.isBlank(bean.getName())) {
            throw new RuntimeException("???????????????????????????????????????");
        }
        Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
                .eq("unitId", budgetUnitId)
                .eq("subjectId", budgetSubjectId)
                .eq("name", bean.getName()));
        if (duplicationCount > 0) {
            throw new RuntimeException("??????????????????????????????" + bean.getName() + "??????????????????");
        }

        BudgetYearAgent insertYearAgent = new BudgetYearAgent();
        insertYearAgent.setYearid(budgetSubject.getYearid());
        insertYearAgent.setUnitid(budgetUnit.getId());
        insertYearAgent.setSubjectid(budgetSubject.getId());
        insertYearAgent.setRemark(bean.getRemark() != null ? bean.getRemark() : "");
        setBudgetYearAgent(insertYearAgent, bean);
        this.budgetYearAgentMapper.insert(insertYearAgent);

        // ??????????????????????????????
        modifyUpdateFlagOfUnit(insertYearAgent.getUnitid(), true);
    }

    private void setBudgetYearAgent(BudgetYearAgent yearAgent, UpdateBudgetYearAgentDTO bean) {
        // ?????????????????????????????????????????????
        BigDecimal total = bean.getM1()
                .add(bean.getM2())
                .add(bean.getM3())
                .add(bean.getM4())
                .add(bean.getM5())
                .add(bean.getM6())
                .add(bean.getM7())
                .add(bean.getM8())
                .add(bean.getM9())
                .add(bean.getM10())
                .add(bean.getM11())
                .add(bean.getM12());

        yearAgent.setName(bean.getName());
        yearAgent.setHappencount(bean.getHappenCount());
        yearAgent.setPreestimate(bean.getPreEstimate());
        yearAgent.setTotal(total);
        yearAgent.setComputingprocess(bean.getComputingProcess());

        yearAgent.setM1(bean.getM1());
        yearAgent.setM2(bean.getM2());
        yearAgent.setM3(bean.getM3());
        yearAgent.setM4(bean.getM4());
        yearAgent.setM5(bean.getM5());
        yearAgent.setM6(bean.getM6());
        yearAgent.setM7(bean.getM7());
        yearAgent.setM8(bean.getM8());
        yearAgent.setM9(bean.getM9());
        yearAgent.setM10(bean.getM10());
        yearAgent.setM11(bean.getM11());
        yearAgent.setM12(bean.getM12());
    }

    /**
     * ??????????????????
     */
    public void updateYearAgent(UpdateBudgetYearAgentDTO bean) {
        if (bean.getYearAgentId() == null) {
            throw new RuntimeException("?????????????????????????????????");
        }
        BudgetYearAgent budgetYearAgent = this.budgetYearAgentMapper.selectById(bean.getYearAgentId());
        if (budgetYearAgent == null) {
            throw new RuntimeException("????????????????????????");
        }

        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetYearAgent.getUnitid());
        if (budgetUnit.getRequeststatus() > 0) {
            throw new RuntimeException("???????????????" + budgetUnit.getName() + "???????????????????????????");
        }

        // ??????????????????????????????
        if (StringUtils.isNotBlank(bean.getName())) {
            Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
                    .eq("unitId", budgetYearAgent.getUnitid())
                    .eq("subjectId", budgetYearAgent.getSubjectid())
                    .eq("name", bean.getName())
                    .ne("id", budgetYearAgent.getId()));
            if (duplicationCount > 0) {
                throw new RuntimeException("??????????????????????????????" + budgetYearAgent.getName() + "???????????????");
            }
        }

        BudgetYearAgent updateYearAgent = new BudgetYearAgent();
        updateYearAgent.setId(budgetYearAgent.getId());
        updateYearAgent.setPretotal(bean.getPretotal());
        updateYearAgent.setRemark(bean.getRemark());
        setBudgetYearAgent(updateYearAgent, bean);
        this.budgetYearAgentMapper.updateById(updateYearAgent);

        // ??????????????????
        BigDecimal oldTotal = budgetYearAgent.getTotal();
        BigDecimal newTotal = updateYearAgent.getTotal();

        // ??????????????????
        BigDecimal oldPreEstimate = budgetYearAgent.getPreestimate();
        BigDecimal newPreEstimate = bean.getPreEstimate() != null ? bean.getPreEstimate() : BigDecimal.ZERO;

        if (oldTotal.compareTo(newTotal) != 0 || oldPreEstimate.compareTo(newPreEstimate) != 0) {
            // ??????????????????
            modifyUpdateFlagOfUnit(budgetYearAgent.getUnitid(), true);
        }

        BudgetMonthAgent monthAgent = new BudgetMonthAgent();
        monthAgent.setName(budgetYearAgent.getName());
        monthAgent.setRemark(budgetYearAgent.getRemark());
        this.budgetMonthAgentMapper.update(monthAgent, new QueryWrapper<BudgetMonthAgent>().eq("yearagentid", budgetYearAgent.getId()));
    }

    /**
     * ??????????????????
     */
    public void setElastic(Long yearAgentId, Boolean elasticFlag, BigDecimal elasticMax, BigDecimal elasticRatio, Long subjectId) {
        BudgetYearAgent budgetYearAgent = this.budgetYearAgentMapper.selectById(yearAgentId);
        if (budgetYearAgent == null) {
            throw new RuntimeException("????????????????????????");
        }

        // ????????????????????????????????????
        BudgetSubject budgetSubject = this.budgetSubjectMapper.selectById(budgetYearAgent.getSubjectid());
        if (budgetSubject.getJointproductflag() || budgetSubject.getCostsplitflag()) {
            throw new RuntimeException("????????????????????????????????????");
        }

        BudgetMonthAgent updateMonthAgent = null;
        Integer count = this.budgetMonthAgentMapper.selectCount(new QueryWrapper<BudgetMonthAgent>().eq("yearagentid", budgetYearAgent.getId()));
        if (count > 0) {
            updateMonthAgent = new BudgetMonthAgent();
        }

        elasticMax = elasticFlag ? elasticMax : BigDecimal.ZERO;
        elasticRatio = elasticFlag ? elasticRatio : BigDecimal.ZERO;
        subjectId = elasticFlag ? subjectId : null;

        BudgetYearAgent updateYearAgent = new BudgetYearAgent();
        updateYearAgent.setElasticflag(elasticFlag);
        updateYearAgent.setElasticmax(elasticMax);
        updateYearAgent.setElasticratio(elasticRatio);

        if (updateMonthAgent != null) {
            updateMonthAgent.setElasticflag(elasticFlag);
            updateMonthAgent.setElasticmax(elasticMax);
            updateMonthAgent.setElasticratio(elasticRatio);
        }

        this.budgetYearAgentMapper.update(updateYearAgent, Wrappers.<BudgetYearAgent>lambdaUpdate()
                .set(BudgetYearAgent::getBudgetsubjectid, subjectId)
                .eq(BudgetYearAgent::getId, budgetYearAgent.getId()));
        if (updateMonthAgent != null) {
            this.budgetMonthAgentMapper.update(updateMonthAgent, Wrappers.<BudgetMonthAgent>lambdaUpdate()
                    .set(BudgetMonthAgent::getBudgetsubjectid, subjectId)
                    .eq(BudgetMonthAgent::getYearagentid, budgetYearAgent.getId()));
        }
    }

    /**
     * ??????????????????
     */
    public void deleteYearAgent(List<Long> yearAgentIds) {
        if (yearAgentIds == null || yearAgentIds.isEmpty()) {
            return;
        }
        // ?????????????????????????????????
        Integer notDeleteCount = this.budgetYearAgentMapper.countNotDeleteByIds(yearAgentIds);
        if (notDeleteCount > 0) {
            throw new RuntimeException("?????????????????????????????????????????????");
        }

        List<BudgetYearAgent> budgetYearAgents = this.budgetYearAgentMapper.selectList(new QueryWrapper<BudgetYearAgent>()
                .in("id", yearAgentIds));
        if (budgetYearAgents.isEmpty()) {
            throw new RuntimeException("???????????????????????????");
        }

        // ??????????????????????????????????????????
        Set<Long> unitIds = budgetYearAgents.stream().map(BudgetYearAgent::getUnitid).collect(Collectors.toSet());
        Integer count = this.budgetUnitMapper.selectCount(new QueryWrapper<BudgetUnit>()
                .in("id", unitIds)
                .gt("requeststatus", 0));
        if (count > 0) {
            throw new RuntimeException("??????????????????????????????????????????");
        }

        // ?????????????????????????????????
        Integer count1 = this.budgetMonthAgentMapper.selectCount(new QueryWrapper<BudgetMonthAgent>()
                .in("yearagentid", yearAgentIds));
        if (count1 > 0) {
            throw new RuntimeException("?????????????????????????????????,????????????");
        }

        this.budgetYearAgentMapper.deleteBatchIds(yearAgentIds);

        for (Long unitId : unitIds) {
            // ??????????????????????????????
            modifyUpdateFlagOfUnit(unitId, true);
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param unitId     ????????????Id
     * @param updateFlag ???????????????
     */
    public void modifyUpdateFlagOfUnit(Long unitId, boolean updateFlag) {
        BudgetUnit updateBudgetUnit = new BudgetUnit();
        updateBudgetUnit.setId(unitId);
        updateBudgetUnit.setUpdatetime(new Date());
        // ????????????
        if (updateFlag) {
            updateBudgetUnit.setUpdateagentflag(true);
            updateBudgetUnit.setCalculatesubjectflag(false);
        } else {
            updateBudgetUnit.setUpdateagentflag(false);
            updateBudgetUnit.setCalculatesubjectflag(true);
        }
        this.budgetUnitMapper.updateById(updateBudgetUnit);
    }

    // ????????????????????????????????????????????????????????? ----------------------------------------------------------------------------------------------------

    /**
     * ????????????????????????
     */
    public Map<String, List<BudgetYearAgent>> exportYearAgent(Long budgetUnitId, Integer type) {
        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
        if (budgetUnit == null) {
            throw new RuntimeException("????????????Id??????");
        }

        // ?????????????????????????????????(?????? or ?????? or ??????)??????
        List<BudgetSubject> productSubjectList = this.budgetSubjectMapper.listSubjectByType(budgetUnit.getYearid(), budgetUnit.getId(), type);
        if (productSubjectList.isEmpty()) {
//            throw new RuntimeException("??????????????????" + getYearAgentTypeName(type) + "??????");
            return new HashMap<>(1);
        }
        Map<Long, String> subjectMap = productSubjectList.stream().collect(Collectors.toMap(BudgetSubject::getId, BudgetSubject::getName));

        // ???????????????????????????????????????
        List<BudgetYearAgent> budgetYearAgents;
        switch (type) {
            case 1:
            case 2:
                budgetYearAgents = this.budgetYearAgentMapper.selectList(new QueryWrapper<BudgetYearAgent>()
                        .eq("yearId", budgetUnit.getYearid())
                        .eq("unitId", budgetUnit.getId()));
                break;
            case 3:
                budgetYearAgents = this.budgetYearAgentMapper.selectList(new QueryWrapper<BudgetYearAgent>()
                        .eq("yearId", budgetUnit.getYearid()));
                HashMap<Long, String> unitNameMap = new HashMap<>(2);
                budgetYearAgents.forEach(v -> {
                    if (!unitNameMap.containsKey(v.getUnitid())) {
                        BudgetUnit unit = this.budgetUnitMapper.selectById(v.getUnitid());
                        if (unit != null) {
                            unitNameMap.put(v.getUnitid(), unit.getName());
                        } else {
                            unitNameMap.put(v.getUnitid(), null);
                        }
                    }
                    v.setUnitName(unitNameMap.get(v.getUnitid()));
                });
                break;
            default:
                budgetYearAgents = new ArrayList<>();
        }

        // ???????????????????????????????????????
        Map<String, List<BudgetYearAgent>> resultMap = new HashMap<>(2);
        budgetYearAgents.forEach(v -> {
            String subjectName = subjectMap.get(v.getSubjectid());
            if (subjectName != null) {
                if (resultMap.containsKey(subjectName)) {
                    resultMap.get(subjectName).add(v);
                } else {
                    List<BudgetYearAgent> list = new ArrayList<>();
                    list.add(v);
                    resultMap.put(subjectName, list);
                }
            }
        });
        return resultMap;
    }

    /**
     * ??????????????????
     *
     * @param budgetUnitId ????????????Id
     * @param type         ???????????????1?????? 2?????? 3?????????
     * @param excelDataMap excel????????????
     * @param errorDataMap ????????????
     */
    public void importYearAgentExcel(Long budgetUnitId, Integer type, Map<String, List<List<String>>> excelDataMap, Map<String, List<List<String>>> errorDataMap) {
        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
        if (budgetUnit == null) {
            throw new RuntimeException("????????????Id??????");
        } else if (budgetUnit.getRequeststatus() == 1) {
            throw new RuntimeException("????????????????????????");
        } else if (budgetUnit.getRequeststatus() == 2) {
            throw new RuntimeException("????????????????????????");
        }

        // ??????????????????????????????
        BudgetYearStartup yearStartup = this.budgetYearStartupMapper.selectOne(new QueryWrapper<BudgetYearStartup>().eq("yearId", budgetUnit.getYearid()));
        if (yearStartup == null || !yearStartup.getStartbudgetflag()) {
            throw new RuntimeException("?????????????????????");
        }

        Set<String> names = null;
        Map<String, Long> hashMap = null;
        if (type == 2) {
            // ????????????????????????????????????
            List<BudgetProduct> productList = this.budgetProductMapper.listProduct(budgetUnit.getId());
            hashMap = productList.stream().collect(Collectors.toMap(v -> v.getName().trim(), BudgetProduct::getId));
            names = hashMap.keySet();
        } else if (type == 3) {
            // ?????????????????????????????????
            List<BudgetUnit> unitList = this.budgetUnitMapper.selectList(new QueryWrapper<BudgetUnit>().eq("yearId", budgetUnit.getYearid()));
            hashMap = unitList.stream().collect(Collectors.toMap(v -> v.getName().trim(), BudgetUnit::getId));
            names = hashMap.keySet();
        }

        // ??????Excel????????????
        Map<Long, List<BudgetYearAgent>> successMap = new HashMap<>(2);
        importValidate(type, budgetUnit, names, excelDataMap, successMap, errorDataMap);

        if (errorDataMap.isEmpty() && !successMap.isEmpty()) {
            Date currentDate = new Date();
            for (Map.Entry<Long, List<BudgetYearAgent>> entry : successMap.entrySet()) {
                // ????????????
                Long budgetSubjectId = entry.getKey();
                BudgetSubject budgetSubject = this.budgetSubjectMapper.selectById(budgetSubjectId);

                for (BudgetYearAgent yearAgent : entry.getValue()) {
                    if (type == 2) {
                        // ??????Id
                        yearAgent.setProductid(hashMap.get(yearAgent.getName()));
                    } else if (type == 3) {
                        // ????????????Id
                        budgetUnitId = hashMap.get(yearAgent.getName());

                        // ????????????????????????
                        yearAgent.setName(budgetSubject.getName());
                    }

                    yearAgent.setYearid(budgetUnit.getYearid());
                    yearAgent.setUnitid(budgetUnitId);
                    yearAgent.setSubjectid(budgetSubjectId);
                    yearAgent.setUpdatetime(currentDate);
                    BigDecimal total = BigDecimal.ZERO;
                    yearAgent.setTotal(total.add(yearAgent.getM1())
                            .add(yearAgent.getM2())
                            .add(yearAgent.getM3())
                            .add(yearAgent.getM4())
                            .add(yearAgent.getM5())
                            .add(yearAgent.getM6())
                            .add(yearAgent.getM7())
                            .add(yearAgent.getM8())
                            .add(yearAgent.getM9())
                            .add(yearAgent.getM10())
                            .add(yearAgent.getM11())
                            .add(yearAgent.getM12())
                    );

                    // ??????????????????????????????????????????, ???????????????, ???????????????
                    BudgetYearAgent existYearAgent = this.budgetYearAgentMapper.selectOne(new QueryWrapper<BudgetYearAgent>()
                            .eq("unitId", budgetUnitId)
                            .eq("subjectId", budgetSubjectId)
                            .eq("name", yearAgent.getName()));
                    if (existYearAgent != null) {
                        yearAgent.setId(existYearAgent.getId());
                        this.budgetYearAgentMapper.updateById(yearAgent);
                    } else {
                        yearAgent.setCreatetime(currentDate);
                        this.budgetYearAgentMapper.insert(yearAgent);
                    }
                }
            }
            // ??????????????????????????????
            modifyUpdateFlagOfUnit(budgetUnit.getId(), true);
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param type         ???????????????1?????? 2?????? 3?????????
     * @param budgetUnit   ??????????????????
     * @param names        ????????????
     * @param excelDataMap excel????????????
     * @param successMap   excel????????????
     * @param errorDataMap excel????????????
     */
    private void importValidate(Integer type, BudgetUnit budgetUnit, Set<String> names, Map<String, List<List<String>>> excelDataMap, Map<Long, List<BudgetYearAgent>> successMap, Map<String, List<List<String>>> errorDataMap) {
        String name = getYearAgentTypeName(type);

        for (Map.Entry<String, List<List<String>>> entry : excelDataMap.entrySet()) {
            // ??????????????????
            String subjectName = entry.getKey();

            // ??????????????????id?????????????????????????????????
            BudgetSubject subject = this.budgetSubjectMapper.getSubjectByUnitIdAndSubjectName(budgetUnit.getId(), subjectName, type);
            if (subject == null) {
                throw new RuntimeException("???????????????" + budgetUnit.getName() + "???????????????" + name + "?????????" + subjectName + "???");
            }

            List<BudgetYearAgent> successList = new ArrayList<>();
            List<ExcelBean> errorList = new ArrayList<>();

            List<List<String>> sheetContent = entry.getValue();
            int size = sheetContent.size();
            for (int i = 0; i < size; i++) {
                // ??????????????????????????????
                if (i < 1) {
                    continue;
                }
                yearAgentValidate(type, sheetContent.get(i), names, successList, errorList);
            }
            if (!successList.isEmpty()) {
                successMap.put(subject.getId(), successList);
            }
            if (!errorList.isEmpty()) {
                errorDataMap.put(subjectName, ExcelBean.transformList(errorList));
            }
        }
    }

    /**
     * ????????????????????????????????????????????????????????????
     */
    private void yearAgentValidate(Integer type, List<String> row, Set<String> names, List<BudgetYearAgent> successList, List<ExcelBean> errorList) {
        int totalColumn;
        int columnSize = row.size();
        if (type == 1 || type == 2) {
            totalColumn = 5;
        } else {
            totalColumn = 1;
        }
        try {
            if (columnSize < totalColumn) {
                throw new RuntimeException("?????????????????????");
            }
            BudgetYearAgent budgetYearAgent = new BudgetYearAgent();

            // ???????????? ??? ???????????? ????????????
            if (type == 1 || type == 2) {
                for (int i = 1; i <= columnSize; i++) {
                    String data = row.get(i - 1);
                    switch (i) {
                        case 1:
                            if (type == 1) {
                                isNotBlank(data, "????????????");
                            } else {
                                isNotBlank(data, "????????????");
                                // ????????????????????????
                                if (names != null && !names.contains(data)) {
                                    throw new RuntimeException("?????????" + data + "????????????!");
                                }
                            }
                            budgetYearAgent.setName(data);
                            break;
                        case 2:
                            isNotBlank(data, "??????????????????");
                            budgetYearAgent.setRemark(data);
                            break;
                        case 3:
                            isNotBlank(data, "??????");
                            budgetYearAgent.setHappencount(data);
                            break;
                        case 4:
                            isNotBlank(data, "????????????");
                            budgetYearAgent.setComputingprocess(data);
                            break;
                        case 5:
                            isNotBlank(data, "????????????");
                            budgetYearAgent.setPreestimate(getBigDecimal(data, "????????????"));
                            break;
                        default:
                    }
                }
                costSplitValidate(row, 6, budgetYearAgent);
            } else {
                // ???????????? ??????
                String data = row.get(0);
                isNotBlank(data, "??????????????????");
                if (names != null && !names.contains(data)) {
                    throw new RuntimeException("???????????????" + data + "????????????!");
                }
                budgetYearAgent.setName(data);
                budgetYearAgent.setRemark("");
                costSplitValidate(row, 2, budgetYearAgent);
            }

            // ??????????????????
            successList.add(budgetYearAgent);
        } catch (Exception e) {
            errorList.add(ExcelBean.transformBean(row, totalColumn + 12, e.getMessage()));
        }
    }

    /**
     * ????????????????????????????????????
     */
    private void costSplitValidate(List<String> row, int startColumn, BudgetYearAgent budgetYearAgent) {
        boolean amountIsEmpty = true;

        // ??????????????????
        BigDecimal bigDecimal = BigDecimal.ZERO;
        int columnSize = row.size();
        int size = columnSize + 12;
        for (int i = 1; i < size; i++) {
            String data = i <= columnSize ? row.get(i - 1) : null;
            if (i == startColumn) {
                bigDecimal = getBigDecimal(data, "6?????????");
                budgetYearAgent.setM6(bigDecimal);
            } else if (i == (startColumn + 1)) {
                bigDecimal = getBigDecimal(data, "7?????????");
                budgetYearAgent.setM7(bigDecimal);
            } else if (i == (startColumn + 2)) {
                bigDecimal = getBigDecimal(data, "8?????????");
                budgetYearAgent.setM8(bigDecimal);
            } else if (i == (startColumn + 3)) {
                bigDecimal = getBigDecimal(data, "9?????????");
                budgetYearAgent.setM9(bigDecimal);
            } else if (i == (startColumn + 4)) {
                bigDecimal = getBigDecimal(data, "10?????????");
                budgetYearAgent.setM10(bigDecimal);
            } else if (i == (startColumn + 5)) {
                bigDecimal = getBigDecimal(data, "11?????????");
                budgetYearAgent.setM11(bigDecimal);
            } else if (i == (startColumn + 6)) {
                bigDecimal = getBigDecimal(data, "12?????????");
                budgetYearAgent.setM12(bigDecimal);
            } else if (i == (startColumn + 7)) {
                bigDecimal = getBigDecimal(data, "1?????????");
                budgetYearAgent.setM1(bigDecimal);
            } else if (i == (startColumn + 8)) {
                bigDecimal = getBigDecimal(data, "2?????????");
                budgetYearAgent.setM2(bigDecimal);
            } else if (i == (startColumn + 9)) {
                bigDecimal = getBigDecimal(data, "3?????????");
                budgetYearAgent.setM3(bigDecimal);
            } else if (i == (startColumn + 10)) {
                bigDecimal = getBigDecimal(data, "4?????????");
                budgetYearAgent.setM4(bigDecimal);
            } else if (i == (startColumn + 11)) {
                bigDecimal = getBigDecimal(data, "5?????????");
                budgetYearAgent.setM5(bigDecimal);
            }

            // ??????????????????????????????0
            if (amountIsEmpty && !BigDecimal.ZERO.equals(bigDecimal)) {
                amountIsEmpty = false;
            }
        }
//        if (amountIsEmpty) {
//            throw new RuntimeException("???????????????????????????????????????");
//        }
    }

    private void isNotBlank(String data, String message) {
        if (StringUtils.isBlank(data)) {
            throw new RuntimeException(message + "????????????");
        }
    }

    private BigDecimal getBigDecimal(String data, String message) {
        try {
            if (StringUtils.isNotBlank(data)) {
                return new BigDecimal(data);
//                if (BigDecimal.ZERO.compareTo(bigDecimal) > 0) {
//                    throw new RuntimeException(message + "???????????????");
//                }
//                return bigDecimal;
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(message + "????????????");
        }
    }

    public String getYearAgentTypeName(Integer type) {
        String name = "??????";
        switch (type) {
            case 2:
                name = "??????";
                break;
            case 3:
                name = "??????";
                break;
            default:
        }
        return name;
    }

    /**
     * ??????????????????
     */
    public void appendYearAgent(UpdateBudgetYearAgentDTO bean) {
        // ????????????Id
        Long budgetSubjectId = bean.getBudgetSubjectId();
        // ????????????Id
        Long budgetUnitId = bean.getBudgetUnitId();

        // ??????????????????????????????
        BudgetSubject budgetSubject = this.budgetSubjectMapper.selectById(budgetSubjectId);
        if (budgetSubject == null) {
            throw new RuntimeException("?????????????????????Id??????");
        }
        // ????????????
        BudgetUnit budgetUnit = budgetUnitMapper.selectById(budgetUnitId);

        // ??????????????????????????????
        if (StringUtils.isBlank(bean.getName())) {
            throw new RuntimeException("???????????????????????????????????????");
        }
        Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
                .eq("unitId", budgetUnitId)
                .eq("subjectId", budgetSubjectId)
                .eq("name", bean.getName()));
        if (duplicationCount > 0) {
            throw new RuntimeException("??????????????????????????????" + bean.getName() + "??????????????????");
        }

        BudgetYearAgent insertYearAgent = new BudgetYearAgent();
        insertYearAgent.setYearid(budgetSubject.getYearid());
        insertYearAgent.setUnitid(budgetUnit.getId());
        insertYearAgent.setSubjectid(budgetSubject.getId());
        insertYearAgent.setRemark(bean.getRemark() != null ? bean.getRemark() : "");
        setBudgetYearAgent(insertYearAgent, bean);
        this.budgetYearAgentMapper.insert(insertYearAgent);

        // ??????????????????????????????
        modifyUpdateFlagOfUnit(insertYearAgent.getUnitid(), true);
    }

}
