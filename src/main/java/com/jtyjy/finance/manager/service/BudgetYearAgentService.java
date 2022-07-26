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
     * 年度动因科目
     */
    public List<BudgetSubjectVO> listSubject(Long yearId, Long baseUnitId, Integer type) {
        // 预算单位
        BudgetUnit budgetUnit = budgetUnitMapper.selectOne(new QueryWrapper<BudgetUnit>()
                .eq("yearId", yearId)
                .eq("baseunitid", baseUnitId));

        // 获取科目pids
        List<String> subjectIds = budgetSubjectMapper.listSubjectIds(yearId, baseUnitId, type);
        if (budgetUnit == null || subjectIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取科目的ids
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

        // 排除无子节点的父节点
        TreeUtil.excludeParentNode(build);
        return build;
    }

    /**
     * 查询年度动因（分页）
     */
    public PageResult<BudgetYearAgentVO> yearAgentPage(Long budgetUnitId, Long budgetSubjectId, String name, Integer page, Integer rows) {
        Page<BudgetYearAgentVO> pageBean = new Page<>(page, rows);

        List<BudgetYearAgentVO> budgetYearAgents = budgetYearAgentMapper.yearAgentPage(pageBean, budgetUnitId, budgetSubjectId, name);

        return PageResult.apply(pageBean.getTotal(), budgetYearAgents);
    }

    /**
     * 新增年度动因
     */
    public void addYearAgent(UpdateBudgetYearAgentDTO bean) {
        // 预算科目Id
        Long budgetSubjectId = bean.getBudgetSubjectId();
        // 预算单位Id
        Long budgetUnitId = bean.getBudgetUnitId();

        // 检查动因科目是否正常
        BudgetSubject budgetSubject = this.budgetSubjectMapper.selectById(budgetSubjectId);
        if (budgetSubject == null) {
            throw new RuntimeException("新增失败，科目Id错误");
        }
        if (budgetSubject.getFormulaflag() || budgetSubject.getJointproductflag() || budgetSubject.getCostsplitflag()) {
            throw new RuntimeException("新增失败，产品、分解、公式科目不允许添加动因");
        }

        // 检查动因科目是否有子节点
        Integer childCount = this.budgetSubjectMapper.selectCount(new QueryWrapper<BudgetSubject>().eq("parentid", budgetSubjectId));
        if (childCount != 0) {
            throw new RuntimeException("新增失败，请选择叶子节点操作");
        }

        // 检查年度预算是否启动
        BudgetYearStartup yearStartup = this.budgetYearStartupMapper.selectOne(new QueryWrapper<BudgetYearStartup>().eq("yearId", budgetSubject.getYearid()));
        if (yearStartup == null || !yearStartup.getStartbudgetflag()) {
            throw new RuntimeException("新增失败，年度预算未启动，不可新增动因");
        }

        // 预算单位
        BudgetUnit budgetUnit = budgetUnitMapper.selectById(budgetUnitId);
        if (budgetUnit.getRequeststatus() >= 1) {
            throw new RuntimeException("新增失败，年度预算已经提交，不可新增动因。");
        }

        // 判断预算单位是否为父级
        Integer count = this.budgetUnitMapper.selectCount(new QueryWrapper<BudgetUnit>()
                .eq("parentid", budgetUnit.getId()));
        if (count > 0) {
            throw new RuntimeException("新增失败，预算单位为父级单位，不可新增动因。");
        }

        // 判断动因名称是否相同
        if (StringUtils.isBlank(bean.getName())) {
            throw new RuntimeException("新增失败，动因名称不能为空");
        }
        Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
                .eq("unitId", budgetUnitId)
                .eq("subjectId", budgetSubjectId)
                .eq("name", bean.getName()));
        if (duplicationCount > 0) {
            throw new RuntimeException("新增失败，动因名称【" + bean.getName() + "】已经存在。");
        }

        BudgetYearAgent insertYearAgent = new BudgetYearAgent();
        insertYearAgent.setYearid(budgetSubject.getYearid());
        insertYearAgent.setUnitid(budgetUnit.getId());
        insertYearAgent.setSubjectid(budgetSubject.getId());
        insertYearAgent.setRemark(bean.getRemark() != null ? bean.getRemark() : "");
        setBudgetYearAgent(insertYearAgent, bean);
        this.budgetYearAgentMapper.insert(insertYearAgent);

        // 年度数据修改更新标识
        modifyUpdateFlagOfUnit(insertYearAgent.getUnitid(), true);
    }

    private void setBudgetYearAgent(BudgetYearAgent yearAgent, UpdateBudgetYearAgentDTO bean) {
        // 判断预算金额与分解总和是否一致
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
     * 修改年度动因
     */
    public void updateYearAgent(UpdateBudgetYearAgentDTO bean) {
        if (bean.getYearAgentId() == null) {
            throw new RuntimeException("请先选择一条记录后重试");
        }
        BudgetYearAgent budgetYearAgent = this.budgetYearAgentMapper.selectById(bean.getYearAgentId());
        if (budgetYearAgent == null) {
            throw new RuntimeException("此年度动因不存在");
        }

        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetYearAgent.getUnitid());
        if (budgetUnit.getRequeststatus() > 0) {
            throw new RuntimeException("预算单位【" + budgetUnit.getName() + "】年度预算已经提交");
        }

        // 判断动因名称是否相同
        if (StringUtils.isNotBlank(bean.getName())) {
            Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
                    .eq("unitId", budgetYearAgent.getUnitid())
                    .eq("subjectId", budgetYearAgent.getSubjectid())
                    .eq("name", bean.getName())
                    .ne("id", budgetYearAgent.getId()));
            if (duplicationCount > 0) {
                throw new RuntimeException("修改失败，动因名称【" + budgetYearAgent.getName() + "】已经存在");
            }
        }

        BudgetYearAgent updateYearAgent = new BudgetYearAgent();
        updateYearAgent.setId(budgetYearAgent.getId());
        updateYearAgent.setPretotal(bean.getPretotal());
        updateYearAgent.setRemark(bean.getRemark());
        setBudgetYearAgent(updateYearAgent, bean);
        this.budgetYearAgentMapper.updateById(updateYearAgent);

        // 本届预算金额
        BigDecimal oldTotal = budgetYearAgent.getTotal();
        BigDecimal newTotal = updateYearAgent.getTotal();

        // 上届预估金额
        BigDecimal oldPreEstimate = budgetYearAgent.getPreestimate();
        BigDecimal newPreEstimate = bean.getPreEstimate() != null ? bean.getPreEstimate() : BigDecimal.ZERO;

        if (oldTotal.compareTo(newTotal) != 0 || oldPreEstimate.compareTo(newPreEstimate) != 0) {
            // 数据有了修改
            modifyUpdateFlagOfUnit(budgetYearAgent.getUnitid(), true);
        }

        BudgetMonthAgent monthAgent = new BudgetMonthAgent();
        monthAgent.setName(budgetYearAgent.getName());
        monthAgent.setRemark(budgetYearAgent.getRemark());
        this.budgetMonthAgentMapper.update(monthAgent, new QueryWrapper<BudgetMonthAgent>().eq("yearagentid", budgetYearAgent.getId()));
    }

    /**
     * 设置弹性动因
     */
    public void setElastic(Long yearAgentId, Boolean elasticFlag, BigDecimal elasticMax, BigDecimal elasticRatio, Long subjectId) {
        BudgetYearAgent budgetYearAgent = this.budgetYearAgentMapper.selectById(yearAgentId);
        if (budgetYearAgent == null) {
            throw new RuntimeException("此年度动因不存在");
        }

        // 判断是否可以设置弹性动因
        BudgetSubject budgetSubject = this.budgetSubjectMapper.selectById(budgetYearAgent.getSubjectid());
        if (budgetSubject.getJointproductflag() || budgetSubject.getCostsplitflag()) {
            throw new RuntimeException("所选动因不能设置弹性动因");
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
     * 删除年度动因
     */
    public void deleteYearAgent(List<Long> yearAgentIds) {
        if (yearAgentIds == null || yearAgentIds.isEmpty()) {
            return;
        }
        // 产品、分解动因不能删除
        Integer notDeleteCount = this.budgetYearAgentMapper.countNotDeleteByIds(yearAgentIds);
        if (notDeleteCount > 0) {
            throw new RuntimeException("所选动因（产品、分解）不能删除");
        }

        List<BudgetYearAgent> budgetYearAgents = this.budgetYearAgentMapper.selectList(new QueryWrapper<BudgetYearAgent>()
                .in("id", yearAgentIds));
        if (budgetYearAgents.isEmpty()) {
            throw new RuntimeException("所选年度动因不存在");
        }

        // 已提交的预算单位不能删除动因
        Set<Long> unitIds = budgetYearAgents.stream().map(BudgetYearAgent::getUnitid).collect(Collectors.toSet());
        Integer count = this.budgetUnitMapper.selectCount(new QueryWrapper<BudgetUnit>()
                .in("id", unitIds)
                .gt("requeststatus", 0));
        if (count > 0) {
            throw new RuntimeException("所选动因预算单位预算已经提交");
        }

        // 已存在月度动因不能删除
        Integer count1 = this.budgetMonthAgentMapper.selectCount(new QueryWrapper<BudgetMonthAgent>()
                .in("yearagentid", yearAgentIds));
        if (count1 > 0) {
            throw new RuntimeException("已存在相关联的月度动因,无法删除");
        }

        this.budgetYearAgentMapper.deleteBatchIds(yearAgentIds);

        for (Long unitId : unitIds) {
            // 年度数据修改更新标识
            modifyUpdateFlagOfUnit(unitId, true);
        }
    }

    /**
     * 年度数据修改更新标识
     *
     * @param unitId     预算单位Id
     * @param updateFlag 是否有更新
     */
    public void modifyUpdateFlagOfUnit(Long unitId, boolean updateFlag) {
        BudgetUnit updateBudgetUnit = new BudgetUnit();
        updateBudgetUnit.setId(unitId);
        updateBudgetUnit.setUpdatetime(new Date());
        // 有更新了
        if (updateFlag) {
            updateBudgetUnit.setUpdateagentflag(true);
            updateBudgetUnit.setCalculatesubjectflag(false);
        } else {
            updateBudgetUnit.setUpdateagentflag(false);
            updateBudgetUnit.setCalculatesubjectflag(true);
        }
        this.budgetUnitMapper.updateById(updateBudgetUnit);
    }

    // 年度动因（普通、产品、分解）导入、导出 ----------------------------------------------------------------------------------------------------

    /**
     * 下载年度动因模板
     */
    public Map<String, List<BudgetYearAgent>> exportYearAgent(Long budgetUnitId, Integer type) {
        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
        if (budgetUnit == null) {
            throw new RuntimeException("预算单位Id错误");
        }

        // 获取当前预算单位下所有(普通 or 产品 or 分解)科目
        List<BudgetSubject> productSubjectList = this.budgetSubjectMapper.listSubjectByType(budgetUnit.getYearid(), budgetUnit.getId(), type);
        if (productSubjectList.isEmpty()) {
//            throw new RuntimeException("预算单位下无" + getYearAgentTypeName(type) + "科目");
            return new HashMap<>(1);
        }
        Map<Long, String> subjectMap = productSubjectList.stream().collect(Collectors.toMap(BudgetSubject::getId, BudgetSubject::getName));

        // 获取预算单位下所有年度动因
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

        // 根据科目名称对年度动因分类
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
     * 年度动因导入
     *
     * @param budgetUnitId 预算单位Id
     * @param type         导入类型（1普通 2产品 3分解）
     * @param excelDataMap excel文件内容
     * @param errorDataMap 异常数据
     */
    public void importYearAgentExcel(Long budgetUnitId, Integer type, Map<String, List<List<String>>> excelDataMap, Map<String, List<List<String>>> errorDataMap) {
        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
        if (budgetUnit == null) {
            throw new RuntimeException("预算单位Id错误");
        } else if (budgetUnit.getRequeststatus() == 1) {
            throw new RuntimeException("该预算单位已提交");
        } else if (budgetUnit.getRequeststatus() == 2) {
            throw new RuntimeException("该预算单位已审核");
        }

        // 检查年度预算是否启动
        BudgetYearStartup yearStartup = this.budgetYearStartupMapper.selectOne(new QueryWrapper<BudgetYearStartup>().eq("yearId", budgetUnit.getYearid()));
        if (yearStartup == null || !yearStartup.getStartbudgetflag()) {
            throw new RuntimeException("年度预算未启动");
        }

        Set<String> names = null;
        Map<String, Long> hashMap = null;
        if (type == 2) {
            // 获取预算单位下的所有产品
            List<BudgetProduct> productList = this.budgetProductMapper.listProduct(budgetUnit.getId());
            hashMap = productList.stream().collect(Collectors.toMap(v -> v.getName().trim(), BudgetProduct::getId));
            names = hashMap.keySet();
        } else if (type == 3) {
            // 获取届别下所有预算单位
            List<BudgetUnit> unitList = this.budgetUnitMapper.selectList(new QueryWrapper<BudgetUnit>().eq("yearId", budgetUnit.getYearid()));
            hashMap = unitList.stream().collect(Collectors.toMap(v -> v.getName().trim(), BudgetUnit::getId));
            names = hashMap.keySet();
        }

        // 检测Excel导入数据
        Map<Long, List<BudgetYearAgent>> successMap = new HashMap<>(2);
        importValidate(type, budgetUnit, names, excelDataMap, successMap, errorDataMap);

        if (errorDataMap.isEmpty() && !successMap.isEmpty()) {
            Date currentDate = new Date();
            for (Map.Entry<Long, List<BudgetYearAgent>> entry : successMap.entrySet()) {
                // 预算科目
                Long budgetSubjectId = entry.getKey();
                BudgetSubject budgetSubject = this.budgetSubjectMapper.selectById(budgetSubjectId);

                for (BudgetYearAgent yearAgent : entry.getValue()) {
                    if (type == 2) {
                        // 产品Id
                        yearAgent.setProductid(hashMap.get(yearAgent.getName()));
                    } else if (type == 3) {
                        // 预算单位Id
                        budgetUnitId = hashMap.get(yearAgent.getName());

                        // 替换年度动因名称
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

                    // 判断是否存在动因名称相同记录, 若存在更新, 不存在新增
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
            // 年度数据修改更新标识
            modifyUpdateFlagOfUnit(budgetUnit.getId(), true);
        }
    }

    /**
     * 年度动因导入数据校验
     *
     * @param type         导入类型（1普通 2产品 3分解）
     * @param budgetUnit   预算单位对象
     * @param names        名称集合
     * @param excelDataMap excel文件内容
     * @param successMap   excel成功记录
     * @param errorDataMap excel错误记录
     */
    private void importValidate(Integer type, BudgetUnit budgetUnit, Set<String> names, Map<String, List<List<String>>> excelDataMap, Map<Long, List<BudgetYearAgent>> successMap, Map<String, List<List<String>>> errorDataMap) {
        String name = getYearAgentTypeName(type);

        for (Map.Entry<String, List<List<String>>> entry : excelDataMap.entrySet()) {
            // 预算科目名称
            String subjectName = entry.getKey();

            // 根据预算单位id和科目名称查询预算科目
            BudgetSubject subject = this.budgetSubjectMapper.getSubjectByUnitIdAndSubjectName(budgetUnit.getId(), subjectName, type);
            if (subject == null) {
                throw new RuntimeException("预算单位【" + budgetUnit.getName() + "】下不存在" + name + "科目【" + subjectName + "】");
            }

            List<BudgetYearAgent> successList = new ArrayList<>();
            List<ExcelBean> errorList = new ArrayList<>();

            List<List<String>> sheetContent = entry.getValue();
            int size = sheetContent.size();
            for (int i = 0; i < size; i++) {
                // 表格正文从第三行开始
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
     * 普通动因、产品动因、分解动因导入数据校验
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
                throw new RuntimeException("内容填写不完整");
            }
            BudgetYearAgent budgetYearAgent = new BudgetYearAgent();

            // 普通动因 或 产品动因 导入校验
            if (type == 1 || type == 2) {
                for (int i = 1; i <= columnSize; i++) {
                    String data = row.get(i - 1);
                    switch (i) {
                        case 1:
                            if (type == 1) {
                                isNotBlank(data, "动因名称");
                            } else {
                                isNotBlank(data, "产品名称");
                                // 判断产品是否存在
                                if (names != null && !names.contains(data)) {
                                    throw new RuntimeException("产品【" + data + "】不存在!");
                                }
                            }
                            budgetYearAgent.setName(data);
                            break;
                        case 2:
                            isNotBlank(data, "业务活动内容");
                            budgetYearAgent.setRemark(data);
                            break;
                        case 3:
                            isNotBlank(data, "次数");
                            budgetYearAgent.setHappencount(data);
                            break;
                        case 4:
                            isNotBlank(data, "计算过程");
                            budgetYearAgent.setComputingprocess(data);
                            break;
                        case 5:
                            isNotBlank(data, "上届预估");
                            budgetYearAgent.setPreestimate(getBigDecimal(data, "上届预估"));
                            break;
                        default:
                    }
                }
                costSplitValidate(row, 6, budgetYearAgent);
            } else {
                // 分解动因 导入
                String data = row.get(0);
                isNotBlank(data, "预算单位名称");
                if (names != null && !names.contains(data)) {
                    throw new RuntimeException("预算单位【" + data + "】不存在!");
                }
                budgetYearAgent.setName(data);
                budgetYearAgent.setRemark("");
                costSplitValidate(row, 2, budgetYearAgent);
            }

            // 添加成功记录
            successList.add(budgetYearAgent);
        } catch (Exception e) {
            errorList.add(ExcelBean.transformBean(row, totalColumn + 12, e.getMessage()));
        }
    }

    /**
     * 年度动因金额月度分解校验
     */
    private void costSplitValidate(List<String> row, int startColumn, BudgetYearAgent budgetYearAgent) {
        boolean amountIsEmpty = true;

        // 年度金额分解
        BigDecimal bigDecimal = BigDecimal.ZERO;
        int columnSize = row.size();
        int size = columnSize + 12;
        for (int i = 1; i < size; i++) {
            String data = i <= columnSize ? row.get(i - 1) : null;
            if (i == startColumn) {
                bigDecimal = getBigDecimal(data, "6月金额");
                budgetYearAgent.setM6(bigDecimal);
            } else if (i == (startColumn + 1)) {
                bigDecimal = getBigDecimal(data, "7月金额");
                budgetYearAgent.setM7(bigDecimal);
            } else if (i == (startColumn + 2)) {
                bigDecimal = getBigDecimal(data, "8月金额");
                budgetYearAgent.setM8(bigDecimal);
            } else if (i == (startColumn + 3)) {
                bigDecimal = getBigDecimal(data, "9月金额");
                budgetYearAgent.setM9(bigDecimal);
            } else if (i == (startColumn + 4)) {
                bigDecimal = getBigDecimal(data, "10月金额");
                budgetYearAgent.setM10(bigDecimal);
            } else if (i == (startColumn + 5)) {
                bigDecimal = getBigDecimal(data, "11月金额");
                budgetYearAgent.setM11(bigDecimal);
            } else if (i == (startColumn + 6)) {
                bigDecimal = getBigDecimal(data, "12月金额");
                budgetYearAgent.setM12(bigDecimal);
            } else if (i == (startColumn + 7)) {
                bigDecimal = getBigDecimal(data, "1月金额");
                budgetYearAgent.setM1(bigDecimal);
            } else if (i == (startColumn + 8)) {
                bigDecimal = getBigDecimal(data, "2月金额");
                budgetYearAgent.setM2(bigDecimal);
            } else if (i == (startColumn + 9)) {
                bigDecimal = getBigDecimal(data, "3月金额");
                budgetYearAgent.setM3(bigDecimal);
            } else if (i == (startColumn + 10)) {
                bigDecimal = getBigDecimal(data, "4月金额");
                budgetYearAgent.setM4(bigDecimal);
            } else if (i == (startColumn + 11)) {
                bigDecimal = getBigDecimal(data, "5月金额");
                budgetYearAgent.setM5(bigDecimal);
            }

            // 检测分解金额是否都为0
            if (amountIsEmpty && !BigDecimal.ZERO.equals(bigDecimal)) {
                amountIsEmpty = false;
            }
        }
//        if (amountIsEmpty) {
//            throw new RuntimeException("分解金额至少有一个不能为零");
//        }
    }

    private void isNotBlank(String data, String message) {
        if (StringUtils.isBlank(data)) {
            throw new RuntimeException(message + "不能为空");
        }
    }

    private BigDecimal getBigDecimal(String data, String message) {
        try {
            if (StringUtils.isNotBlank(data)) {
                return new BigDecimal(data);
//                if (BigDecimal.ZERO.compareTo(bigDecimal) > 0) {
//                    throw new RuntimeException(message + "不能为负数");
//                }
//                return bigDecimal;
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(message + "格式错误");
        }
    }

    public String getYearAgentTypeName(Integer type) {
        String name = "普通";
        switch (type) {
            case 2:
                name = "产品";
                break;
            case 3:
                name = "分解";
                break;
            default:
        }
        return name;
    }

    /**
     * 新增年度动因
     */
    public void appendYearAgent(UpdateBudgetYearAgentDTO bean) {
        // 预算科目Id
        Long budgetSubjectId = bean.getBudgetSubjectId();
        // 预算单位Id
        Long budgetUnitId = bean.getBudgetUnitId();

        // 检查动因科目是否正常
        BudgetSubject budgetSubject = this.budgetSubjectMapper.selectById(budgetSubjectId);
        if (budgetSubject == null) {
            throw new RuntimeException("新增失败，科目Id错误");
        }
        // 预算单位
        BudgetUnit budgetUnit = budgetUnitMapper.selectById(budgetUnitId);

        // 判断动因名称是否相同
        if (StringUtils.isBlank(bean.getName())) {
            throw new RuntimeException("新增失败，动因名称不能为空");
        }
        Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
                .eq("unitId", budgetUnitId)
                .eq("subjectId", budgetSubjectId)
                .eq("name", bean.getName()));
        if (duplicationCount > 0) {
            throw new RuntimeException("新增失败，动因名称【" + bean.getName() + "】已经存在。");
        }

        BudgetYearAgent insertYearAgent = new BudgetYearAgent();
        insertYearAgent.setYearid(budgetSubject.getYearid());
        insertYearAgent.setUnitid(budgetUnit.getId());
        insertYearAgent.setSubjectid(budgetSubject.getId());
        insertYearAgent.setRemark(bean.getRemark() != null ? bean.getRemark() : "");
        setBudgetYearAgent(insertYearAgent, bean);
        this.budgetYearAgentMapper.insert(insertYearAgent);

        // 年度数据修改更新标识
        modifyUpdateFlagOfUnit(insertYearAgent.getUnitid(), true);
    }

}
