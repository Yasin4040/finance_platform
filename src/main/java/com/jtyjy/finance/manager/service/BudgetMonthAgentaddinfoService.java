package com.jtyjy.finance.manager.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.ecology.webservice.workflow.WorkflowInfo;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.dto.MonthAgentAddInfoDTO;
import com.jtyjy.finance.manager.easyexcel.MonthAgentAddInfoExcelData;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.utils.NumberUtil;
import com.jtyjy.finance.manager.vo.BudgetMonthAddInfoVO;
import com.jtyjy.finance.manager.vo.BudgetMonthAgentAddVO;
import com.jtyjy.finance.manager.ws.WSBudgetMonthAgentAdd;
import com.jtyjy.finance.manager.ws.WSBudgetMonthAgentAddDetail;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetMonthAgentaddinfoService extends DefaultBaseService<BudgetMonthAgentaddinfoMapper, BudgetMonthAgentaddinfo> {

    private final TabChangeLogMapper loggerMapper;
    private final BudgetMonthSubjectMapper budgetMonthSubjectMapper;
    private final BudgetYearSubjectMapper budgetYearSubjectMapper;
    private final BudgetMonthEndUnitMapper budgetMonthEndUnitMapper;
    private final BudgetUnitMapper budgetUnitMapper;
    private final BudgetSubjectMapper budgetSubjectMapper;
    private final BudgetMonthAgentMapper budgetMonthAgentMapper;
    private final BudgetYearAgentMapper budgetYearAgentMapper;
    private final BudgetYearPeriodMapper budgetYearPeriodMapper;
    private final BudgetMonthAgentaddMapper budgetMonthAgentaddMapper;
    private final BudgetMonthAgentaddinfoMapper budgetMonthAgentaddinfoMapper;

    private final CuratorFramework curatorFramework;
    private final OaService oaService;
    private final DistributedNumber distributedNumber;
    private final BudgetSysService budgetSysService;

    @Value("${monthadd.workflowid}")
    private String flowid;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_month_agentaddinfo"));
    }

    /**
     * ??????????????????????????????
     */
    public PageResult<BudgetMonthAddInfoVO> monthAgentAddInfoPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {
        Page<BudgetMonthAddInfoVO> pageBean = new Page<>(page, rows);
        List<BudgetMonthAddInfoVO> resultList = this.budgetMonthAgentaddinfoMapper.listMonthAgentAddInfoPage(pageBean, paramMap);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * ???????????????????????????
     */
    public List<BudgetSubject> listCanAddSubjects(Long budgetUnitId, Long monthId) {
        BudgetMonthEndUnit budgetMonthEndUnit = this.budgetMonthEndUnitMapper.selectOne(new QueryWrapper<BudgetMonthEndUnit>()
                .eq("unitid", budgetUnitId)
                .eq("monthid", monthId)
                .eq("requeststatus", 2));
        if (budgetMonthEndUnit == null) {
            throw new RuntimeException("???" + monthId + "????????????????????????????????????");
        } else if (budgetMonthEndUnit.getMonthendflag()) {
            throw new RuntimeException("???" + monthId + "???????????????????????????");
        }
        return this.budgetSubjectMapper.listCanAddSubjects(budgetUnitId);
    }

    /**
     * ???????????????????????????
     */
    public List<BudgetMonthAgent> listCanAddAgents(Long budgetUnitId, Long budgetSubjectId, Long monthId) {
        return this.budgetMonthAgentaddMapper.listCanAddAgents(budgetUnitId, budgetSubjectId, monthId);
    }

    /**
     * ????????????????????????????????????
     */
    public List<BudgetMonthAgentAddVO> listAddAgentByInfoId(Long infoId) {
        List<BudgetMonthAgentAddVO> agentAddList = this.budgetMonthAgentaddMapper.listAddAgentByInfoId(infoId);
        agentAddList.forEach(v -> {
            v.setMonthMoney(v.getPreMonthMoney());
            v.setMonthBalance(v.getPreMonthBalance());
        });
        return agentAddList;
    }

    /**
     * ?????????????????????????????????
     */
    public BudgetMonthAgentAddVO getMonthAgentInfo(Long monthAgentId) {
        return this.budgetMonthAgentMapper.getMonthAgentInfo(monthAgentId);
    }

    /**
     * ??????????????????
     */
    public void monthAgentAddMoney(MonthAgentAddInfoDTO bean,List<Map<String,Object>> list) throws Exception {

        String key = UserThreadLocal.getEmpNo();
        if (bean.getId() != null) {
            key = bean.getId().toString();
        }

        ZookeeperShareLock lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/monthAgentAddMoney/" + key, o -> {
            throw new RuntimeException("????????????????????????????????????,??????????????????");
        });
        try {
            lock.tryLock();
            if (bean.getId() != null) {
                BudgetMonthAgentaddinfo oldAgentAddInfo = this.budgetMonthAgentaddinfoMapper.selectById(bean.getId());
                if (oldAgentAddInfo == null) {
                    throw new RuntimeException("??????Id??????");
                } else if (oldAgentAddInfo.getRequeststatus() > 0) {
                    throw new RuntimeException("???????????????????????????");
                }
            }

            List<BudgetSubject> subjectList = this.listCanAddSubjects(bean.getBudgetUnitId(), bean.getMonthId());
            List<Long> subjectIds = subjectList.stream().map(BudgetSubject::getId).collect(Collectors.toList());

            // ??????????????????????????????
            BudgetMonthAgentaddinfo info = createAddInfo(bean);

            // ??????????????????????????????
            saveOrUpdateMonthAgentAdd(bean.getAddList(), bean.getUpdateList(), subjectIds, info);
            // ??????????????????????????????
            if (bean.getDeleteList() != null && !bean.getDeleteList().isEmpty()) {
                this.budgetMonthAgentaddMapper.deleteBatchIds(bean.getDeleteList());
            }

            // ????????????????????????
            BigDecimal total = BigDecimal.ZERO;
            List<BudgetMonthAgentadd> yearAgentAddList = this.budgetMonthAgentaddMapper.selectList(new QueryWrapper<BudgetMonthAgentadd>()
                    .eq("infoid", info.getId()));
            for (BudgetMonthAgentadd agentAdd : yearAgentAddList) {
                total = total.add(agentAdd.getTotal());
            }
            info.setTotal(total);
            this.budgetMonthAgentaddinfoMapper.updateById(info);

            // ???????????????OA??????
            if (bean.getIsSubmit()) {
                try {
                    submitVerify(info.getId(), true,list);
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
     * ?????????OA??????
     */
    public void submitVerify(Long infoId, Boolean existLock,List<Map<String,Object>> list) throws Exception {
        ZookeeperShareLock lock = null;
        try {
            if (!existLock) {
                lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/monthAgentAddMoney/" + infoId, o -> {
                    throw new RuntimeException("????????????????????????????????????,??????????????????");
                });
                lock.tryLock();
            }

            // ??????
            BudgetMonthAgentaddinfo agentAddInfo = this.budgetMonthAgentaddinfoMapper.selectById(infoId);
            if (agentAddInfo.getRequeststatus() > 0) {
                throw new RuntimeException("????????????????????????????????????????????????");
            }


            String userIdDeptId = this.oaService.getOaUserId(agentAddInfo.getCreatorid(),list);
            String oaUserId = userIdDeptId.split(",")[0];
            String oaDeptId = userIdDeptId.split(",")[1];
            WbUser user = UserThreadLocal.get();

            String userName = user.getDisplayName();
            WorkflowInfo wi = new WorkflowInfo();
            wi.setCreatorId(oaUserId);
            wi.setRequestLevel("0");
            wi.setRequestName("????????????????????????--" + userName);
            WSBudgetMonthAgentAdd wsAdd = new WSBudgetMonthAgentAdd();
            wsAdd.setSqr(Integer.valueOf(oaUserId));
            wsAdd.setSsbm(oaDeptId);
            wsAdd.setYsdw(this.budgetUnitMapper.selectById(agentAddInfo.getUnitid()).getName());
            wsAdd.setYsjb(this.budgetYearPeriodMapper.selectById(agentAddInfo.getYearid()).getPeriod());
            wsAdd.setSqrq(Constants.FORMAT_10.format(agentAddInfo.getCreatetime()));
            wsAdd.setIsswsp(0);
            if (this.oaService.isProvinceUnit(agentAddInfo.getUnitid())) {
                wsAdd.setIsswsp(1);
            }
            wsAdd.setZjyf(agentAddInfo.getMonthid() + "???");
//        wsAdd.setZjkm(this.budgetSubjectMapper.selectById(agentAddInfo.getSubjectid()).getName());
            wsAdd.setZjkm("");
            wsAdd.setWfid(flowid);
            // ?????????????????????
            Integer count = this.budgetMonthAgentaddinfoMapper.selectCount(new QueryWrapper<BudgetMonthAgentaddinfo>()
                    .eq("yearid", agentAddInfo.getYearid())
                    .eq("unitid", agentAddInfo.getUnitid())
                    .eq("monthid", agentAddInfo.getMonthid())
                    .eq("requeststatus", 2)
            );
            wsAdd.setByzjcs((count + 1) + "");

            String fileUrl = agentAddInfo.getFileurl();
            int code = -1;
            if (StringUtils.isNotBlank(fileUrl)) {
                String oaPassword = agentAddInfo.getOapassword();
                URL url = new URL(fileUrl);
                URLConnection connection = url.openConnection();
                InputStream is = connection.getInputStream();
                String fileOriginName = agentAddInfo.getFileoriginname();
                code = this.oaService.createDoc(agentAddInfo.getCreatorid(), oaPassword, is, fileOriginName, fileUrl, "????????????????????????");
                if (code == 0) {
                    throw new RuntimeException("????????????!??????????????????!");
                }
                is.close();
            }
            wsAdd.setFj(code + "");

            List<WSBudgetMonthAgentAddDetail> details = new ArrayList<>();
            List<BudgetMonthAgentadd> addList = this.budgetMonthAgentaddMapper.selectList(new QueryWrapper<BudgetMonthAgentadd>()
                    .eq("infoid", agentAddInfo.getId()));
            if (addList == null || addList.size() == 0) {
                throw new RuntimeException("????????????????????????????????????");
            }

            // ????????????????????????
            List<Long> monthAgentIds = addList.stream().map(BudgetMonthAgentadd::getMonthagentid).filter(Objects::nonNull).collect(Collectors.toList());
            HashSet<Long> hashSet = new HashSet<>(monthAgentIds);
            if (monthAgentIds.size() != hashSet.size()) {
                throw new RuntimeException("??????????????????????????????????????????????????????");
            }

            addList.forEach(monthAdd -> {
                // ??????????????????
                BudgetYearAgent yearAgent = this.budgetYearAgentMapper.getYearAgentByMonthAgentId(monthAdd.getMonthagentid());

                // ????????????
                BigDecimal ndysjy = yearAgent.getTotal()
                        .add(yearAgent.getAddmoney())
                        .add(yearAgent.getLendinmoney())
                        .subtract(yearAgent.getLendoutmoney())
                        .subtract(yearAgent.getExecutemoney());

                // ?????????????????????
                List<BigDecimal> jrzxList = this.budgetMonthAgentMapper.listReimMoneyByYearAgentId(yearAgent.getId());

                // ???????????????
                List<BigDecimal> hbList = this.budgetMonthAgentMapper.listAllocatedMoneyByYearAgentId(yearAgent.getId());

                // ????????????????????????????????????????????????
                List<BigDecimal> lockList = this.budgetMonthAgentaddMapper.listLockMoneyByMonthAgentId(monthAdd.getMonthagentid());

                // ????????????
                BigDecimal usedmoney = BigDecimal.ZERO;
                if (!jrzxList.isEmpty()) {
                    BigDecimal jrzxtotalmoney = jrzxList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    usedmoney = usedmoney.add(jrzxtotalmoney);
                }
                if (!hbList.isEmpty()) {
                    BigDecimal jrzxtotalmoney = hbList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    usedmoney = usedmoney.add(jrzxtotalmoney);
                }
                if (!lockList.isEmpty()) {
                    BigDecimal lockmoney = lockList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    usedmoney = usedmoney.add(lockmoney);
                }

                // ??????????????????
                String lockMsg = "";
                if (usedmoney.compareTo(BigDecimal.ZERO) > 0) {
                    lockMsg = "\n???????????????" + NumberUtil.subZeroAndDot(usedmoney) + "???";
                }
                if (usedmoney.add(monthAdd.getTotal()).compareTo(ndysjy) > 0) {
                    throw new RuntimeException("?????????"
                            + monthAdd.getName()
                            + "??????????????????"
                            + NumberUtil.subZeroAndDot(monthAdd.getTotal())
                            + "??????????????????????????????"
                            + NumberUtil.subZeroAndDot(ndysjy)
                            + "???"
                            + lockMsg);
                }

                WSBudgetMonthAgentAddDetail detail = new WSBudgetMonthAgentAddDetail();
                detail.setZjdy(monthAdd.getName());
                // ????????????
                detail.setZjkm(this.budgetSubjectMapper.selectById(monthAdd.getSubjectid()).getName());
                // ????????????
                detail.setZjje(NumberUtil.subZeroAndDot(monthAdd.getTotal()));
                detail.setZjyy(monthAdd.getRemark());
                // ?????????????????? = ???????????????????????????????????????????????????+ ????????????????????????????????? + ????????????????????? - ????????????????????? - ?????????????????????????????????
                detail.setNdysjy(NumberUtil.subZeroAndDot(monthAdd.getYearagentmoney().add(monthAdd.getYearagentaddmoney())
                        .add(monthAdd.getYearagentlendinmoney().subtract(monthAdd.getYearagentlendoutmoney()))
                        .subtract(monthAdd.getYearagentexcutemoney())));
                // ??????????????????
                detail.setYcysje(NumberUtil.subZeroAndDot(monthAdd.getAgentmoney()));
                // ???????????????
                detail.setYzj(NumberUtil.subZeroAndDot(monthAdd.getAgentaddmoney()));
                // ???????????????
                detail.setYsy(NumberUtil.subZeroAndDot(monthAdd.getAgentexcutemoney()));
                // ??????????????????
                detail.setYdysjy(NumberUtil.subZeroAndDot(detail.getYcysje().add(detail.getYzj().add(monthAdd.getAgentlendinmoney()
                        .subtract(monthAdd.getAgentlendoutmoney().subtract(detail.getYsy()))))));
                // ?????????????????????????????????
                detail.setZjhjy(NumberUtil.subZeroAndDot(detail.getYdysjy().add(detail.getZjje())));
                details.add(detail);
            });
            String oldRequestId = agentAddInfo.getRequestid();
            if (oldRequestId != null) {
                String oaCreatorId = agentAddInfo.getOacreatorid();
                this.oaService.deleteRequest(oldRequestId, oaCreatorId);
            }

            String requestId = createBudgetAgentAdd(wi, wsAdd, details);
            if (requestId == null || Integer.parseInt(requestId) < 0) {
                throw new RuntimeException("???????????????oa?????????????????????????????????????????????oa????????????");
            }

            // ????????????
            BudgetMonthAgentaddinfo updateAddInfo = new BudgetMonthAgentaddinfo();
            updateAddInfo.setId(infoId);
            updateAddInfo.setRequeststatus(1);
            updateAddInfo.setRequestid(requestId);
            updateAddInfo.setUpdatetime(new Date());
            updateAddInfo.setOacreatorid(oaUserId);
            this.budgetMonthAgentaddinfoMapper.updateById(updateAddInfo);
        } finally {
            if (lock != null) {
                lock.unLock();
            }
        }
    }

    /**
     * ????????????????????????
     */
    public String createBudgetAgentAdd(WorkflowInfo wi, WSBudgetMonthAgentAdd wsMonthAdd, List<WSBudgetMonthAgentAddDetail> details) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) JSON.toJSON(details);
        Map<String, Object> main = (Map<String, Object>) JSON.toJSON(wsMonthAdd);
        return this.oaService.createWorkflow(wi, wsMonthAdd.getWfid(), main, list);
    }

    /**
     * ???????????????????????????????????????
     */
    private void saveOrUpdateMonthAgentAdd(List<BudgetMonthAgentadd> addList, List<BudgetMonthAgentadd> updateList, List<Long> subjectIds, BudgetMonthAgentaddinfo info) {
        List<BudgetMonthAgentadd> list = new ArrayList<>();
        if (addList != null && !addList.isEmpty()) {
            list.addAll(addList);
        }
        if (updateList != null && !updateList.isEmpty()) {
            list.addAll(updateList);
        }

        for (BudgetMonthAgentadd monthAgentAdd : list) {

            if (!subjectIds.contains(monthAgentAdd.getSubjectid())) {
                throw new RuntimeException("?????????????????????????????????????????????, ?????????????????????");
            }

            // ??????
            monthAgentAdd.setYearid(info.getYearid());
            // ????????????
            monthAgentAdd.setUnitid(info.getUnitid());
            // ????????????
            monthAgentAdd.setSubjectid(monthAgentAdd.getSubjectid());

            BudgetMonthAgent monthAgent = this.budgetMonthAgentMapper.selectById(monthAgentAdd.getMonthagentid());
            if (monthAgent == null) {
                throw new RuntimeException("??????????????????????????????");
            }
            monthAgentAdd.setAgentmoney(monthAgent.getTotal());
            monthAgentAdd.setAgentaddmoney(monthAgent.getAddmoney());
            monthAgentAdd.setAgentlendinmoney(monthAgent.getLendinmoney());
            monthAgentAdd.setAgentlendoutmoney(monthAgent.getLendoutmoney());
            monthAgentAdd.setAgentexcutemoney(monthAgent.getExecutemoney());

            // ????????????
            BudgetYearAgent yearAgent = this.budgetYearAgentMapper.selectById(monthAgent.getYearagentid());
            if (yearAgent == null) {
                throw new RuntimeException("???????????????????????????????????????????????????");
            } else if (!yearAgent.getUnitid().equals(monthAgentAdd.getUnitid())) {
                throw new RuntimeException("????????????????????????" + monthAgent.getName() + "????????????????????????????????????");
            } else if (!yearAgent.getSubjectid().equals(monthAgentAdd.getSubjectid())) {
                throw new RuntimeException("????????????????????????" + monthAgent.getName() + "????????????????????????????????????");
            }
            monthAgentAdd.setYearagentmoney(yearAgent.getTotal());
            monthAgentAdd.setYearagentaddmoney(yearAgent.getAddmoney());
            monthAgentAdd.setYearagentlendinmoney(yearAgent.getLendinmoney());
            monthAgentAdd.setYearagentlendoutmoney(yearAgent.getLendoutmoney());
            monthAgentAdd.setYearagentexcutemoney(yearAgent.getExecutemoney());

            monthAgentAdd.setInfoid(info.getId());
            if (monthAgentAdd.getId() == null) {
                this.budgetMonthAgentaddMapper.insert(monthAgentAdd);
            } else {
                this.budgetMonthAgentaddMapper.updateById(monthAgentAdd);
            }
        }
    }

    /**
     * ?????????????????????????????????
     */
    private BudgetMonthAgentaddinfo createAddInfo(MonthAgentAddInfoDTO bean) {
        BudgetMonthAgentaddinfo updateAgentAddInfo = new BudgetMonthAgentaddinfo();

        // ????????????
        updateAgentAddInfo.setRequeststatus(0);
        // ??????
        updateAgentAddInfo.setYearid(bean.getYearId());
        // ????????????
        updateAgentAddInfo.setUnitid(bean.getBudgetUnitId());
//        // ????????????Id
//        updateAgentAddInfo.setSubjectid(bean.getBudgetSubjectId());
        // ??????Id
        updateAgentAddInfo.setMonthid(bean.getMonthId());
        // ????????????
        updateAgentAddInfo.setFileurl(bean.getFileUrl());
        // ????????????
        updateAgentAddInfo.setFileoriginname(bean.getFileOriginName());
        // oa?????????????????????????????????
        updateAgentAddInfo.setOapassword(bean.getOaPwd());

//        BudgetMonthSubject budgetMonthSubject = this.budgetMonthSubjectMapper.selectOne(new QueryWrapper<BudgetMonthSubject>()
//                .eq("yearid", bean.getYearId())
//                .eq("unitid", bean.getBudgetUnitId())
//                .eq("subjectid", bean.getBudgetSubjectId())
//                .eq("monthid", bean.getMonthId()));
//        updateAgentAddInfo.setAgentmoney(budgetMonthSubject.getTotal());
//        updateAgentAddInfo.setAgentaddmoney(budgetMonthSubject.getAddmoney());
//        updateAgentAddInfo.setAgentexcutemoney(budgetMonthSubject.getExecutemoney());
//        updateAgentAddInfo.setAgentlendinmoney(budgetMonthSubject.getLendinmoney());
//        updateAgentAddInfo.setAgentlendoutmoney(budgetMonthSubject.getLendoutmoney());

//        BudgetYearSubject budgetYearSubject = this.budgetYearSubjectMapper.selectOne(new QueryWrapper<BudgetYearSubject>()
//                .eq("yearid", bean.getYearId())
//                .eq("unitid", bean.getBudgetUnitId())
//                .eq("subjectid", bean.getBudgetSubjectId()));
//        updateAgentAddInfo.setYearagentaddmoney(budgetYearSubject.getAddmoney());
//        updateAgentAddInfo.setYearagentexcutemoney(budgetYearSubject.getExecutemoney());
//        updateAgentAddInfo.setYearagentlendinmoney(budgetYearSubject.getLendinmoney());
//        updateAgentAddInfo.setYearagentlendoutmoney(budgetYearSubject.getLendoutmoney());
//        updateAgentAddInfo.setYearagentmoney(budgetYearSubject.getTotal());

        if (bean.getId() == null) {
            WbUser wbUser = UserThreadLocal.get();
            updateAgentAddInfo.setCreatorid(wbUser.getUserName());
            updateAgentAddInfo.setCreatetime(new Date());
            updateAgentAddInfo.setCreatorname(wbUser.getDisplayName());
            updateAgentAddInfo.setHandleflag(false);
            updateAgentAddInfo.setRequeststatus(0);
            updateAgentAddInfo.setTotal(BigDecimal.ZERO);
            updateAgentAddInfo.setMonthaddcode(this.distributedNumber.getMonthAgentAddNum());
            this.budgetMonthAgentaddinfoMapper.insert(updateAgentAddInfo);
        } else {
            updateAgentAddInfo.setId(bean.getId());
            updateAgentAddInfo.setUpdatetime(new Date());
            this.budgetMonthAgentaddinfoMapper.updateById(updateAgentAddInfo);
        }
        return updateAgentAddInfo;
    }

    /**
     * ????????????????????????
     */
    public void deleteMonthAgentAdd(List<Long> ids) {
        for (Long id : ids) {
            BudgetMonthAgentaddinfo agentAddInfo = this.budgetMonthAgentaddinfoMapper.selectById(id);
            if (agentAddInfo.getRequeststatus() != 0) {
                throw new RuntimeException("????????????????????????!");
            }
            this.budgetMonthAgentaddMapper.delete(new QueryWrapper<BudgetMonthAgentadd>().eq("infoid", id));
            this.budgetMonthAgentaddinfoMapper.deleteById(id);
        }
    }

    /**
     * ????????????????????????
     */
    public List<MonthAgentAddInfoExcelData> exportAgentMonthAdd(HashMap<String, Object> paramMap) {
        List<MonthAgentAddInfoExcelData> resultList = new ArrayList<>();

        List<BudgetMonthAgentaddinfo> addInfoList = this.budgetMonthAgentaddinfoMapper.listMonthAgentAddInfoByMap(paramMap);

        HashMap<Long, String> subjectNames = new HashMap<>(5);
        for (BudgetMonthAgentaddinfo addInfo : addInfoList) {
            // ??????????????????
            List<BudgetMonthAgentadd> addList = this.budgetMonthAgentaddMapper.selectList(new QueryWrapper<BudgetMonthAgentadd>()
                    .eq("infoid", addInfo.getId()));
            if (!addList.isEmpty()) {
                for (BudgetMonthAgentadd agentAdd : addList) {
                    MonthAgentAddInfoExcelData rowData = new MonthAgentAddInfoExcelData();
                    rowData.setNum(resultList.size() + 1);
                    rowData.setRequestStatus(Constants.getRequestStatus(addInfo.getRequeststatus()));
                    rowData.setMonthAddCode(addInfo.getMonthaddcode());
                    rowData.setUnitName(addInfo.getUnitName());
                    rowData.setPeriod(addInfo.getPeriod());

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
                    rowData.setMonthId(addInfo.getMonthid());
                    rowData.setTotal(agentAdd.getTotal());
                    rowData.setRemark(agentAdd.getRemark());
                    // ????????????
                    rowData.setYearMoney(agentAdd.getYearagentmoney());
                    // ????????????
                    BigDecimal yearBalance = agentAdd.getYearagentmoney()
                            .add(agentAdd.getYearagentaddmoney())
                            .add(agentAdd.getYearagentlendinmoney())
                            .subtract(agentAdd.getYearagentlendoutmoney())
                            .subtract(agentAdd.getYearagentexcutemoney());
                    rowData.setYearBalance(yearBalance);
                    // ????????????
                    rowData.setMonthMoney(agentAdd.getAgentmoney());
                    // ????????????
                    BigDecimal monthBalance = agentAdd.getAgentmoney()
                            .add(agentAdd.getAgentaddmoney())
                            .add(agentAdd.getAgentlendinmoney())
                            .subtract(agentAdd.getAgentlendoutmoney())
                            .subtract(agentAdd.getAgentexcutemoney());
                    rowData.setAddBefore(monthBalance);
                    rowData.setAddAfter(monthBalance.add(agentAdd.getTotal()));
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
    public void endMonthAgentAdd(EcologyParams params) throws Exception {
        // ??????id
        String requestId = params.getRequestid();

        ZookeeperShareLock lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/endMonthAgentAdd/" + requestId, o -> {
            throw new RuntimeException("??????????????????????????????????????????,??????????????????");
        });
        try {
            lock.tryLock();

            Date currentDate = new Date();
            // ????????????id????????????????????????
            BudgetMonthAgentaddinfo agentAddInfo = this.budgetMonthAgentaddinfoMapper.selectOne(new QueryWrapper<BudgetMonthAgentaddinfo>()
                    .eq("requestid", requestId));
            if (agentAddInfo == null) {
                throw new RuntimeException("????????????????????????????????????");
            } else if (agentAddInfo.getRequeststatus() == 2) {
                throw new RuntimeException("??????????????????????????????????????????");
            }

            HashMap<Long, BigDecimal> subjectIds = new HashMap<>();
            List<BudgetMonthAgentadd> addList = this.budgetMonthAgentaddMapper.selectList(new QueryWrapper<BudgetMonthAgentadd>()
                    .eq("infoid", agentAddInfo.getId()));
            addList.forEach(monthAdd -> {
                Long subjectId = monthAdd.getSubjectid();
                if (!subjectIds.containsKey(subjectId)) {
                    subjectIds.put(subjectId, monthAdd.getTotal());
                } else {
                    subjectIds.put(subjectId, subjectIds.get(subjectId).add(monthAdd.getTotal()));
                }

                BudgetMonthAgent budgetMonthAgent = this.budgetMonthAgentMapper.selectById(monthAdd.getMonthagentid());
                if (budgetMonthAgent == null) {
                    throw new RuntimeException("??????????????????????????????");
                }
                BudgetMonthAgent updateMonthAgent = new BudgetMonthAgent();
                updateMonthAgent.setId(budgetMonthAgent.getId());
                updateMonthAgent.setAddmoney(budgetMonthAgent.getAddmoney().add(monthAdd.getTotal()));
                updateMonthAgent.setUpdatetime(currentDate);
                this.budgetMonthAgentMapper.updateById(updateMonthAgent);
            });

            BudgetMonthAgentaddinfo updateAddInfo = new BudgetMonthAgentaddinfo();
            updateAddInfo.setId(agentAddInfo.getId());
            updateAddInfo.setRequeststatus(2);
            updateAddInfo.setHandleflag(true);
            updateAddInfo.setAudittime(currentDate);
            this.budgetMonthAgentaddinfoMapper.updateById(updateAddInfo);

            // ?????????????????????????????????
            subjectIds.forEach((subjectId, addTotal) -> this.budgetSysService.doSyncBudgetSubjectMonthAddMoney(agentAddInfo.getYearid(), agentAddInfo.getUnitid(), subjectId, agentAddInfo.getMonthid(), addTotal, 1));
        } finally {
            lock.unLock();
        }
    }

    /**
     * ????????????????????????
     */
    public void rejectMonthAgentAdd(EcologyParams params) {
        String requestId = params.getRequestid();

        BudgetMonthAgentaddinfo agentAddInfo = this.budgetMonthAgentaddinfoMapper.selectOne(new QueryWrapper<BudgetMonthAgentaddinfo>()
                .eq("requestid", requestId));
        if (agentAddInfo == null) {
            throw new RuntimeException("????????????????????????????????????");
        } else if (agentAddInfo.getRequeststatus() == 2) {
            throw new RuntimeException("?????????????????????????????????????????????????????????");
        }

        BudgetMonthAgentaddinfo updateAddInfo = new BudgetMonthAgentaddinfo();
        updateAddInfo.setId(agentAddInfo.getId());
        updateAddInfo.setRequeststatus(-1);
        updateAddInfo.setHandleflag(true);
        updateAddInfo.setUpdatetime(new Date());
        updateAddInfo.setAudittime(new Date());
        this.budgetMonthAgentaddinfoMapper.updateById(updateAddInfo);
    }
}
