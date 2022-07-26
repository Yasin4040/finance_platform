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
     * 查询月度追加（分页）
     */
    public PageResult<BudgetMonthAddInfoVO> monthAgentAddInfoPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {
        Page<BudgetMonthAddInfoVO> pageBean = new Page<>(page, rows);
        List<BudgetMonthAddInfoVO> resultList = this.budgetMonthAgentaddinfoMapper.listMonthAgentAddInfoPage(pageBean, paramMap);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * 获取月度可追加科目
     */
    public List<BudgetSubject> listCanAddSubjects(Long budgetUnitId, Long monthId) {
        BudgetMonthEndUnit budgetMonthEndUnit = this.budgetMonthEndUnitMapper.selectOne(new QueryWrapper<BudgetMonthEndUnit>()
                .eq("unitid", budgetUnitId)
                .eq("monthid", monthId)
                .eq("requeststatus", 2));
        if (budgetMonthEndUnit == null) {
            throw new RuntimeException("【" + monthId + "】月还未完成月度预算审核");
        } else if (budgetMonthEndUnit.getMonthendflag()) {
            throw new RuntimeException("【" + monthId + "】月月度预算已月结");
        }
        return this.budgetSubjectMapper.listCanAddSubjects(budgetUnitId);
    }

    /**
     * 获取月度可追加动因
     */
    public List<BudgetMonthAgent> listCanAddAgents(Long budgetUnitId, Long budgetSubjectId, Long monthId) {
        return this.budgetMonthAgentaddMapper.listCanAddAgents(budgetUnitId, budgetSubjectId, monthId);
    }

    /**
     * 查询单个月度追加动因列表
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
     * 获取月度动因可追加信息
     */
    public BudgetMonthAgentAddVO getMonthAgentInfo(Long monthAgentId) {
        return this.budgetMonthAgentMapper.getMonthAgentInfo(monthAgentId);
    }

    /**
     * 月度预算追加
     */
    public void monthAgentAddMoney(MonthAgentAddInfoDTO bean,List<Map<String,Object>> list) throws Exception {

        String key = UserThreadLocal.getEmpNo();
        if (bean.getId() != null) {
            key = bean.getId().toString();
        }

        ZookeeperShareLock lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/monthAgentAddMoney/" + key, o -> {
            throw new RuntimeException("正在执行月度动因预算追加,请勿重复点击");
        });
        try {
            lock.tryLock();
            if (bean.getId() != null) {
                BudgetMonthAgentaddinfo oldAgentAddInfo = this.budgetMonthAgentaddinfoMapper.selectById(bean.getId());
                if (oldAgentAddInfo == null) {
                    throw new RuntimeException("参数Id错误");
                } else if (oldAgentAddInfo.getRequeststatus() > 0) {
                    throw new RuntimeException("所选记录不允许修改");
                }
            }

            List<BudgetSubject> subjectList = this.listCanAddSubjects(bean.getBudgetUnitId(), bean.getMonthId());
            List<Long> subjectIds = subjectList.stream().map(BudgetSubject::getId).collect(Collectors.toList());

            // 创建月度预算追加信息
            BudgetMonthAgentaddinfo info = createAddInfo(bean);

            // 新增月度动因预算追加
            saveOrUpdateMonthAgentAdd(bean.getAddList(), bean.getUpdateList(), subjectIds, info);
            // 删除月度动因预算追加
            if (bean.getDeleteList() != null && !bean.getDeleteList().isEmpty()) {
                this.budgetMonthAgentaddMapper.deleteBatchIds(bean.getDeleteList());
            }

            // 统计追加金额总数
            BigDecimal total = BigDecimal.ZERO;
            List<BudgetMonthAgentadd> yearAgentAddList = this.budgetMonthAgentaddMapper.selectList(new QueryWrapper<BudgetMonthAgentadd>()
                    .eq("infoid", info.getId()));
            for (BudgetMonthAgentadd agentAdd : yearAgentAddList) {
                total = total.add(agentAdd.getTotal());
            }
            info.setTotal(total);
            this.budgetMonthAgentaddinfoMapper.updateById(info);

            // 是否提交至OA系统
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
     * 提交至OA系统
     */
    public void submitVerify(Long infoId, Boolean existLock,List<Map<String,Object>> list) throws Exception {
        ZookeeperShareLock lock = null;
        try {
            if (!existLock) {
                lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/monthAgentAddMoney/" + infoId, o -> {
                    throw new RuntimeException("正在提交月度动因预算追加,请勿重复点击");
                });
                lock.tryLock();
            }

            // 校验
            BudgetMonthAgentaddinfo agentAddInfo = this.budgetMonthAgentaddinfoMapper.selectById(infoId);
            if (agentAddInfo.getRequeststatus() > 0) {
                throw new RuntimeException("提交失败，该流程已经提交或者审核");
            }


            String userIdDeptId = this.oaService.getOaUserId(agentAddInfo.getCreatorid(),list);
            String oaUserId = userIdDeptId.split(",")[0];
            String oaDeptId = userIdDeptId.split(",")[1];
            WbUser user = UserThreadLocal.get();

            String userName = user.getDisplayName();
            WorkflowInfo wi = new WorkflowInfo();
            wi.setCreatorId(oaUserId);
            wi.setRequestLevel("0");
            wi.setRequestName("月度预算追加流程--" + userName);
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
            wsAdd.setZjyf(agentAddInfo.getMonthid() + "月");
//        wsAdd.setZjkm(this.budgetSubjectMapper.selectById(agentAddInfo.getSubjectid()).getName());
            wsAdd.setZjkm("");
            wsAdd.setWfid(flowid);
            // 本月第几次追加
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
                code = this.oaService.createDoc(agentAddInfo.getCreatorid(), oaPassword, is, fileOriginName, fileUrl, "月度追加流程附件");
                if (code == 0) {
                    throw new RuntimeException("系统错误!创建文档失败!");
                }
                is.close();
            }
            wsAdd.setFj(code + "");

            List<WSBudgetMonthAgentAddDetail> details = new ArrayList<>();
            List<BudgetMonthAgentadd> addList = this.budgetMonthAgentaddMapper.selectList(new QueryWrapper<BudgetMonthAgentadd>()
                    .eq("infoid", agentAddInfo.getId()));
            if (addList == null || addList.size() == 0) {
                throw new RuntimeException("没有可追加的月度动因记录");
            }

            // 动因重复合并校验
            List<Long> monthAgentIds = addList.stream().map(BudgetMonthAgentadd::getMonthagentid).filter(Objects::nonNull).collect(Collectors.toList());
            HashSet<Long> hashSet = new HashSet<>(monthAgentIds);
            if (monthAgentIds.size() != hashSet.size()) {
                throw new RuntimeException("请勿对相同的月度动因追加两次动因金额");
            }

            addList.forEach(monthAdd -> {
                // 获取年度动因
                BudgetYearAgent yearAgent = this.budgetYearAgentMapper.getYearAgentByMonthAgentId(monthAdd.getMonthagentid());

                // 年度余额
                BigDecimal ndysjy = yearAgent.getTotal()
                        .add(yearAgent.getAddmoney())
                        .add(yearAgent.getLendinmoney())
                        .subtract(yearAgent.getLendoutmoney())
                        .subtract(yearAgent.getExecutemoney());

                // 计入执行的金额
                List<BigDecimal> jrzxList = this.budgetMonthAgentMapper.listReimMoneyByYearAgentId(yearAgent.getId());

                // 划拨的金额
                List<BigDecimal> hbList = this.budgetMonthAgentMapper.listAllocatedMoneyByYearAgentId(yearAgent.getId());

                // 已提交审核的追加金额（锁定状态）
                List<BigDecimal> lockList = this.budgetMonthAgentaddMapper.listLockMoneyByMonthAgentId(monthAdd.getMonthagentid());

                // 锁定金额
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

                // 判定是否可用
                String lockMsg = "";
                if (usedmoney.compareTo(BigDecimal.ZERO) > 0) {
                    lockMsg = "\n锁定金额【" + NumberUtil.subZeroAndDot(usedmoney) + "】";
                }
                if (usedmoney.add(monthAdd.getTotal()).compareTo(ndysjy) > 0) {
                    throw new RuntimeException("动因【"
                            + monthAdd.getName()
                            + "】追加金额【"
                            + NumberUtil.subZeroAndDot(monthAdd.getTotal())
                            + "】大于年度动因可用【"
                            + NumberUtil.subZeroAndDot(ndysjy)
                            + "】"
                            + lockMsg);
                }

                WSBudgetMonthAgentAddDetail detail = new WSBudgetMonthAgentAddDetail();
                detail.setZjdy(monthAdd.getName());
                // 追加科目
                detail.setZjkm(this.budgetSubjectMapper.selectById(monthAdd.getSubjectid()).getName());
                // 追加金额
                detail.setZjje(NumberUtil.subZeroAndDot(monthAdd.getTotal()));
                detail.setZjyy(monthAdd.getRemark());
                // 年度预算结余 = 追加前动因月度预算金额（年初预算）+ 追加前动因累计追加金额 + 追加前拆进金额 - 追加前拆出金额 - 追加前动因累计执行金额
                detail.setNdysjy(NumberUtil.subZeroAndDot(monthAdd.getYearagentmoney().add(monthAdd.getYearagentaddmoney())
                        .add(monthAdd.getYearagentlendinmoney().subtract(monthAdd.getYearagentlendoutmoney()))
                        .subtract(monthAdd.getYearagentexcutemoney())));
                // 月初预算总额
                detail.setYcysje(NumberUtil.subZeroAndDot(monthAdd.getAgentmoney()));
                // 月度已追加
                detail.setYzj(NumberUtil.subZeroAndDot(monthAdd.getAgentaddmoney()));
                // 月度已使用
                detail.setYsy(NumberUtil.subZeroAndDot(monthAdd.getAgentexcutemoney()));
                // 月度预算结余
                detail.setYdysjy(NumberUtil.subZeroAndDot(detail.getYcysje().add(detail.getYzj().add(monthAdd.getAgentlendinmoney()
                        .subtract(monthAdd.getAgentlendoutmoney().subtract(detail.getYsy()))))));
                // 本次追加后月度预算结余
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
                throw new RuntimeException("提交失败，oa系统未找到你的上级人员，请联系oa管理员。");
            }

            // 更新状态
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
     * 创建月度追加流程
     */
    public String createBudgetAgentAdd(WorkflowInfo wi, WSBudgetMonthAgentAdd wsMonthAdd, List<WSBudgetMonthAgentAddDetail> details) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) JSON.toJSON(details);
        Map<String, Object> main = (Map<String, Object>) JSON.toJSON(wsMonthAdd);
        return this.oaService.createWorkflow(wi, wsMonthAdd.getWfid(), main, list);
    }

    /**
     * 新增或修改月度动因预算追加
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
                throw new RuntimeException("追加的预算单位与预算科目不匹配, 请重新填写数据");
            }

            // 届别
            monthAgentAdd.setYearid(info.getYearid());
            // 预算单位
            monthAgentAdd.setUnitid(info.getUnitid());
            // 预算科目
            monthAgentAdd.setSubjectid(monthAgentAdd.getSubjectid());

            BudgetMonthAgent monthAgent = this.budgetMonthAgentMapper.selectById(monthAgentAdd.getMonthagentid());
            if (monthAgent == null) {
                throw new RuntimeException("追加的月度动因不存在");
            }
            monthAgentAdd.setAgentmoney(monthAgent.getTotal());
            monthAgentAdd.setAgentaddmoney(monthAgent.getAddmoney());
            monthAgentAdd.setAgentlendinmoney(monthAgent.getLendinmoney());
            monthAgentAdd.setAgentlendoutmoney(monthAgent.getLendoutmoney());
            monthAgentAdd.setAgentexcutemoney(monthAgent.getExecutemoney());

            // 年度预算
            BudgetYearAgent yearAgent = this.budgetYearAgentMapper.selectById(monthAgent.getYearagentid());
            if (yearAgent == null) {
                throw new RuntimeException("追加的月度动因关联的年度动因不存在");
            } else if (!yearAgent.getUnitid().equals(monthAgentAdd.getUnitid())) {
                throw new RuntimeException("追加的月度动因【" + monthAgent.getName() + "】与追加的预算单位不匹配");
            } else if (!yearAgent.getSubjectid().equals(monthAgentAdd.getSubjectid())) {
                throw new RuntimeException("追加的月度动因【" + monthAgent.getName() + "】与追加的预算科目不匹配");
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
     * 创建或更新月度追加记录
     */
    private BudgetMonthAgentaddinfo createAddInfo(MonthAgentAddInfoDTO bean) {
        BudgetMonthAgentaddinfo updateAgentAddInfo = new BudgetMonthAgentaddinfo();

        // 保存状态
        updateAgentAddInfo.setRequeststatus(0);
        // 届别
        updateAgentAddInfo.setYearid(bean.getYearId());
        // 预算单位
        updateAgentAddInfo.setUnitid(bean.getBudgetUnitId());
//        // 预算科目Id
//        updateAgentAddInfo.setSubjectid(bean.getBudgetSubjectId());
        // 月份Id
        updateAgentAddInfo.setMonthid(bean.getMonthId());
        // 附件地址
        updateAgentAddInfo.setFileurl(bean.getFileUrl());
        // 附件名称
        updateAgentAddInfo.setFileoriginname(bean.getFileOriginName());
        // oa密码，上传附件时需使用
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
     * 删除月度预算追加
     */
    public void deleteMonthAgentAdd(List<Long> ids) {
        for (Long id : ids) {
            BudgetMonthAgentaddinfo agentAddInfo = this.budgetMonthAgentaddinfoMapper.selectById(id);
            if (agentAddInfo.getRequeststatus() != 0) {
                throw new RuntimeException("此记录暂无法删除!");
            }
            this.budgetMonthAgentaddMapper.delete(new QueryWrapper<BudgetMonthAgentadd>().eq("infoid", id));
            this.budgetMonthAgentaddinfoMapper.deleteById(id);
        }
    }

    /**
     * 月度预算追加导出
     */
    public List<MonthAgentAddInfoExcelData> exportAgentMonthAdd(HashMap<String, Object> paramMap) {
        List<MonthAgentAddInfoExcelData> resultList = new ArrayList<>();

        List<BudgetMonthAgentaddinfo> addInfoList = this.budgetMonthAgentaddinfoMapper.listMonthAgentAddInfoByMap(paramMap);

        HashMap<Long, String> subjectNames = new HashMap<>(5);
        for (BudgetMonthAgentaddinfo addInfo : addInfoList) {
            // 月度追加明细
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

                    // 预算科目名称
                    if (!subjectNames.containsKey(agentAdd.getSubjectid())) {
                        BudgetSubject subject = this.budgetSubjectMapper.selectById(agentAdd.getSubjectid());
                        if (subject == null) {
                            throw new RuntimeException("预算科目不存在或已删除");
                        }
                        subjectNames.put(subject.getId(), subject.getName());
                    }
                    rowData.setSubjectName(subjectNames.get(agentAdd.getSubjectid()));

                    rowData.setAgentName(agentAdd.getName());
                    rowData.setMonthId(addInfo.getMonthid());
                    rowData.setTotal(agentAdd.getTotal());
                    rowData.setRemark(agentAdd.getRemark());
                    // 年初预算
                    rowData.setYearMoney(agentAdd.getYearagentmoney());
                    // 年初余额
                    BigDecimal yearBalance = agentAdd.getYearagentmoney()
                            .add(agentAdd.getYearagentaddmoney())
                            .add(agentAdd.getYearagentlendinmoney())
                            .subtract(agentAdd.getYearagentlendoutmoney())
                            .subtract(agentAdd.getYearagentexcutemoney());
                    rowData.setYearBalance(yearBalance);
                    // 月初预算
                    rowData.setMonthMoney(agentAdd.getAgentmoney());
                    // 月度余额
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
     * 月度动因追加审核通过
     */
    public void endMonthAgentAdd(EcologyParams params) throws Exception {
        // 流程id
        String requestId = params.getRequestid();

        ZookeeperShareLock lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/endMonthAgentAdd/" + requestId, o -> {
            throw new RuntimeException("正在执行月度动因预算追加审核,请勿重复点击");
        });
        try {
            lock.tryLock();

            Date currentDate = new Date();
            // 通过流程id获取月度动因追加
            BudgetMonthAgentaddinfo agentAddInfo = this.budgetMonthAgentaddinfoMapper.selectOne(new QueryWrapper<BudgetMonthAgentaddinfo>()
                    .eq("requestid", requestId));
            if (agentAddInfo == null) {
                throw new RuntimeException("不存在该月度动因追加流程");
            } else if (agentAddInfo.getRequeststatus() == 2) {
                throw new RuntimeException("该月度动因追加流程已审核通过");
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
                    throw new RuntimeException("追加的月度动因不存在");
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

            // 同步月度预算科目执行数
            subjectIds.forEach((subjectId, addTotal) -> this.budgetSysService.doSyncBudgetSubjectMonthAddMoney(agentAddInfo.getYearid(), agentAddInfo.getUnitid(), subjectId, agentAddInfo.getMonthid(), addTotal, 1));
        } finally {
            lock.unLock();
        }
    }

    /**
     * 月度动因追加退回
     */
    public void rejectMonthAgentAdd(EcologyParams params) {
        String requestId = params.getRequestid();

        BudgetMonthAgentaddinfo agentAddInfo = this.budgetMonthAgentaddinfoMapper.selectOne(new QueryWrapper<BudgetMonthAgentaddinfo>()
                .eq("requestid", requestId));
        if (agentAddInfo == null) {
            throw new RuntimeException("不存在该月度动因追加流程");
        } else if (agentAddInfo.getRequeststatus() == 2) {
            throw new RuntimeException("该月度动因追加流程已审核通过。退回失败");
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
