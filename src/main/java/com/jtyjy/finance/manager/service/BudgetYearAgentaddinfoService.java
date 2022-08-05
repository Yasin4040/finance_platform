package com.jtyjy.finance.manager.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.ecology.EcologyClient;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.ecology.EcologyWorkFlowValue;
import com.jtyjy.ecology.webservice.workflow.WorkflowInfo;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.dto.YearAgentAddInfoDTO;
import com.jtyjy.finance.manager.easyexcel.YearAgentAddInfoExcelData;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.vo.BudgetYearAddInfoVO;
import com.jtyjy.finance.manager.ws.WSBudgetYearAgentAdd;
import com.jtyjy.finance.manager.ws.WSBudgetYearAgentAddDetail;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetYearAgentaddinfoService extends DefaultBaseService<BudgetYearAgentaddinfoMapper, BudgetYearAgentaddinfo> {

    private final TabChangeLogMapper loggerMapper;
    private final BudgetYearAgentaddMapper budgetYearAgentaddMapper;
    private final BudgetYearAgentaddinfoMapper budgetYearAgentaddinfoMapper;
    private final BudgetYearSubjectMapper budgetYearSubjectMapper;
    private final BudgetUnitMapper budgetUnitMapper;
    private final BudgetYearAgentMapper budgetYearAgentMapper;
    private final BudgetYearPeriodMapper budgetYearPeriodMapper;
    private final BudgetSubjectMapper budgetSubjectMapper;
    private final BudgetMonthPeriodMapper budgetMonthPeriodMapper;
    private final BudgetMonthEndUnitMapper budgetMonthEndUnitMapper;
    private final BudgetMonthAgentMapper budgetMonthAgentMapper;

    private final OaService oaService;
    private final DistributedNumber distributedNumber;
    private final BudgetYearAgentaddService budgetYearAgentaddService;
    private final CuratorFramework curatorFramework;
    private final BudgetSysService budgetSysService;

    @Value("${yearadd.workflowid}")
    private String flowid;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_year_agentaddinfo"));
    }

    // 年度追加 ----------------------------------------------------------------------------------------------------

    /**
     * 查询年度追加（分页）
     */
    public PageResult<BudgetYearAddInfoVO> yearAgentAddInfoPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {
        Page<BudgetYearAddInfoVO> pageBean = new Page<>(page, rows);
        List<BudgetYearAddInfoVO> resultList = this.budgetYearAgentaddinfoMapper.listYearAgentAddInfoPage(pageBean, paramMap);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * 获取年度可追加科目
     */
    public List<BudgetSubject> listCanAddSubjects(Long budgetUnitId) {
        BudgetUnit budgetUnit = this.budgetUnitMapper.selectById(budgetUnitId);
        if (budgetUnit == null) {
            throw new RuntimeException("该预算单位不存在");
        } else if (budgetUnit.getRequeststatus() != 2) {
            throw new RuntimeException("预算单位【" + budgetUnit.getName() + "】年度预算审核未通过");
        }
        return this.budgetSubjectMapper.listCanAddSubjects(budgetUnitId);
    }

    /**
     * 获取年度可追加动因
     */
    public List<BudgetYearAgent> listCanAddAgents(Long budgetUnitId, Long budgetSubjectId) {
        return this.budgetYearAgentaddMapper.listCanAddAgents(budgetUnitId, budgetSubjectId);
    }

    /**
     * 获取年度动因可追加信息
     */
    public BudgetYearAgentadd getYearAgentInfo(Long yearAgentId) {
        BudgetYearAgent yearAgent = this.budgetYearAgentMapper.selectById(yearAgentId);
        if (yearAgent != null) {
            yearAgent.setBalance(yearAgent.getTotal()
                    .add(yearAgent.getAddmoney())
                    .add(yearAgent.getLendinmoney())
                    .subtract(yearAgent.getLendoutmoney())
                    .subtract(yearAgent.getExecutemoney()));

            BudgetYearAgentadd yearAgentAdd = new BudgetYearAgentadd();
            yearAgentAdd.setYearagentid(yearAgent.getId());
            yearAgentAdd.setSubjectid(yearAgent.getSubjectid());
            yearAgentAdd.setName(yearAgent.getName());
            yearAgentAdd.setAgentmoney(yearAgent.getTotal());
            yearAgentAdd.setPreYearBalance(yearAgent.getBalance());
            yearAgentAdd.setYearBalance(yearAgent.getBalance());
            return yearAgentAdd;
        }
        return null;
    }

    /**
     * 查询单个年度追加动因列表
     */
    public List<BudgetYearAgentadd> listAddAgentByInfoId(Long infoId) {
        List<BudgetYearAgentadd> agentAdds = this.budgetYearAgentaddMapper.selectList(new QueryWrapper<BudgetYearAgentadd>().eq("infoid", infoId));
        agentAdds.forEach(v -> {
            v.setPreYearBalance(v.getAgentmoney()
                    .add(v.getAgentaddmoney())
                    .add(v.getAgentlendinmoney())
                    .subtract(v.getAgentlendoutmoney())
                    .subtract(v.getAgentexcutemoney()));
//            v.setYearBalance(v.getPreYearBalance().add(v.getTotal()));
            v.setYearBalance(v.getPreYearBalance());
        });
        return agentAdds;
    }

    /**
     * 年度预算追加
     */
    public void yearAgentAddMoney(YearAgentAddInfoDTO bean, List<Map<String, Object>> list) throws Exception {

        String key = UserThreadLocal.getEmpNo();
        if (bean.getId() != null) {
            key = bean.getId().toString();
        }

        ZookeeperShareLock lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/yearAgentAddMoney/" + key, o -> {
            throw new RuntimeException("正在执行年度动因预算追加,请勿重复点击");
        });
        try {
            lock.tryLock();

            if (bean.getId() != null) {
                BudgetYearAgentaddinfo oldAgentAddInfo = this.budgetYearAgentaddinfoMapper.selectById(bean.getId());
                if (oldAgentAddInfo == null) {
                    throw new RuntimeException("参数Id错误");
                } else if (oldAgentAddInfo.getRequeststatus() > 0) {
                    throw new RuntimeException("所选记录不允许修改");
                }
            }

            List<BudgetSubject> subjectList = this.listCanAddSubjects(bean.getBudgetUnitId());
            List<Long> subjectIds = subjectList.stream().map(BudgetSubject::getId).collect(Collectors.toList());

            // 创建年度预算追加信息
            BudgetYearAgentaddinfo info = createAddInfo(bean);

            // 当前月份
            int month = LocalDateTime.now().getMonthValue();
            BudgetMonthPeriod monthPeriod = this.budgetMonthPeriodMapper.selectOne(new QueryWrapper<BudgetMonthPeriod>().eq("code", month));

            // 新增年度动因预算追加
            saveOrUpdateYearAgentAdd(bean.getAddList(), bean.getUpdateList(), info, subjectIds, monthPeriod);
            // 删除年度动因预算追加
            if (bean.getDeleteList() != null && !bean.getDeleteList().isEmpty()) {
                this.budgetYearAgentaddMapper.deleteBatchIds(bean.getDeleteList());
            }

            // 统计追加金额总数
            BigDecimal total = BigDecimal.ZERO;
            List<BudgetYearAgentadd> yearAgentAddList = this.budgetYearAgentaddMapper.selectList(new QueryWrapper<BudgetYearAgentadd>()
                    .eq("infoid", info.getId()));
            for (BudgetYearAgentadd agentAdd : yearAgentAddList) {
                total = total.add(agentAdd.getTotal());
            }
            info.setTotal(total);
            this.budgetYearAgentaddinfoMapper.updateById(info);

            // 是否提交至OA系统
            if (bean.getIsSubmit()) {
                try {
                    submitVerify(info.getId(), true, list);
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
     * 新增或修改年度动因预算追加
     */
    private void saveOrUpdateYearAgentAdd(List<BudgetYearAgentadd> addList, List<BudgetYearAgentadd> updateList, BudgetYearAgentaddinfo info, List<Long> subjectIds, BudgetMonthPeriod monthPeriod) {
        List<BudgetYearAgentadd> list = new ArrayList<>();
        if (addList != null && !addList.isEmpty()) {
            list.addAll(addList);
        }
        if (updateList != null && !updateList.isEmpty()) {
            list.addAll(updateList);
        }

        Long month = Long.valueOf(monthPeriod.getCode());
        for (BudgetYearAgentadd yearAgentAdd : list) {
            yearAgentAdd.setMonthagentid(null);

            if (!subjectIds.contains(yearAgentAdd.getSubjectid())) {
                throw new RuntimeException("追加的预算单位与预算科目不匹配, 请重新填写数据");
            }

            if (yearAgentAdd.getType() == 1) {
                yearAgentAdd.setYearagentid(null);
                // 是否是产品科目
                BudgetSubject budgetSubject = this.budgetSubjectMapper.selectById(yearAgentAdd.getSubjectid());
                if (budgetSubject == null) {
                    throw new RuntimeException("不存在该预算科目");
                } else if (budgetSubject.getJointproductflag() != null && budgetSubject.getJointproductflag()) {
                    throw new RuntimeException("产品科目【" + budgetSubject.getName() + "】只允许追加动因金额，不允许追加动因");
                }
            }

            // 追加动因金额：查询动因追加前信息（年初预算、累计追加金额、拆出金额、拆进金额、累计执行金额）
            if (yearAgentAdd.getType() == 0) {
                // 追加
                BudgetYearAgent yearAgent = this.budgetYearAgentMapper.selectById(yearAgentAdd.getYearagentid());
                if (yearAgent == null) {
                    throw new RuntimeException("追加的年度动因【" + yearAgentAdd.getName() + "】不存在");
                } else if (!yearAgent.getSubjectid().equals(yearAgentAdd.getSubjectid())) {
                    throw new RuntimeException("追加的年度动因【" + yearAgentAdd.getName() + "】与追加的预算科目不匹配");
                }
                yearAgentAdd.setAgentmoney(yearAgent.getTotal());
                yearAgentAdd.setAgentaddmoney(yearAgent.getAddmoney());
                yearAgentAdd.setAgentlendoutmoney(yearAgent.getLendoutmoney());
                yearAgentAdd.setAgentlendinmoney(yearAgent.getLendinmoney());
                yearAgentAdd.setAgentexcutemoney(yearAgent.getExecutemoney());
            } else {
                Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
                        .eq("unitId", info.getUnitid())
                        .eq("subjectId", yearAgentAdd.getSubjectid())
                        .eq("name", yearAgentAdd.getName()));
                if (duplicationCount > 0) {
                    throw new RuntimeException("新增失败，追加动因名称【" + yearAgentAdd.getName() + "】已经存在。");
                }
            }

            Date currentDate = new Date();
            // 届别Id
            yearAgentAdd.setYearid(info.getYearid());
            // 预算单位Id
            yearAgentAdd.setUnitid(info.getUnitid());
            // 预算科目Id
            yearAgentAdd.setSubjectid(yearAgentAdd.getSubjectid());
            // 年度预算追加Id
            yearAgentAdd.setInfoid(info.getId());
            // 当前月
            yearAgentAdd.setCurmonthid(month);
            // 修改时间
            yearAgentAdd.setUpdatetime(currentDate);
            if (yearAgentAdd.getId() == null) {
                // 创建时间
                yearAgentAdd.setCreatetime(currentDate);
            }

            try {
                BigDecimal m = (BigDecimal) BudgetYearAgentadd.class.getMethod("getM" + month).invoke(yearAgentAdd);
                yearAgentAdd.setCurmonthmoney(m);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (yearAgentAdd.getCurmonthmoney().compareTo(BigDecimal.ZERO) > 0) {
                BudgetMonthEndUnit budgetMonthEndUnit = this.budgetMonthEndUnitMapper.selectOne(new QueryWrapper<BudgetMonthEndUnit>()
                        .eq("unitId", info.getUnitid())
                        .eq("monthId", month)
                        .eq("requeststatus", 2));
                if (budgetMonthEndUnit == null) {
                    throw new RuntimeException("【" + month + "】月还未完成月度预算审核！");
                } else if (budgetMonthEndUnit.getMonthendflag()) {
                    throw new RuntimeException("【" + month + "】月月度预算已月结!");
                }
                Integer count = this.budgetMonthAgentMapper.selectCount(new QueryWrapper<BudgetMonthAgent>()
                        .eq("yearId", info.getYearid())
                        .eq("unitId", info.getUnitid())
                        .eq("monthId", monthPeriod.getId()));
                if (count <= 0) {
                    throw new RuntimeException(month + "月还没开始做月度预算!");
                }
            }
            this.budgetYearAgentaddService.saveOrUpdate(yearAgentAdd);
        }
    }

    /**
     * 创建或更新年度追加记录
     */
    private BudgetYearAgentaddinfo createAddInfo(YearAgentAddInfoDTO bean) {
//        BudgetYearSubject budgetYearSubject = this.budgetYearSubjectMapper.selectOne(new QueryWrapper<BudgetYearSubject>()
//                .eq("yearId", bean.getYearId())
//                .eq("unitId", bean.getBudgetUnitId())
//                .eq("subjectId", bean.getBudgetSubjectId()));
//        if (budgetYearSubject == null) {
//            throw new RuntimeException("请先在年度汇总中同步一下数据!");
//        }

        BudgetYearAgentaddinfo updateAgentAddInfo = new BudgetYearAgentaddinfo();
        // 保存状态
        updateAgentAddInfo.setRequeststatus(0);
        // 届别
        updateAgentAddInfo.setYearid(bean.getYearId());
        // 预算单位
        updateAgentAddInfo.setUnitid(bean.getBudgetUnitId());
//        // 预算科目
//        updateAgentAddInfo.setSubjectid(bean.getBudgetSubjectId());
//        // 追加前动因累计追加金额
//        updateAgentAddInfo.setAgentaddmoney(budgetYearSubject.getAddmoney());
//        // 追加前动因累计执行金额
//        updateAgentAddInfo.setAgentexcutemoney(budgetYearSubject.getExecutemoney());
//        // 追加前拆进金额（同科目里面的动因可以拆借）
//        updateAgentAddInfo.setAgentlendinmoney(budgetYearSubject.getLendinmoney());
//        // 追加前拆出金额（同科目里面的动因可以拆借）
//        updateAgentAddInfo.setAgentlendoutmoney(budgetYearSubject.getLendoutmoney());
//        // 追加前动因年度预算金额（年初预算）
//        updateAgentAddInfo.setAgentmoney(budgetYearSubject.getTotal());
        // 附件地址
        updateAgentAddInfo.setFileurl(bean.getFileUrl());
        // 附件名称
        updateAgentAddInfo.setFileoriginname(bean.getFileOriginName());
        // oa密码，上传附件时需使用
        updateAgentAddInfo.setOapassword(bean.getOaPwd());

        updateAgentAddInfo.setIsExemptFine(bean.getIsExemptFine() == null ? false : bean.getIsExemptFine());
        updateAgentAddInfo.setExemptFineReason(bean.getExemptFineReason());
        if (bean.getId() == null) {
            WbUser wbUser = UserThreadLocal.get();
            updateAgentAddInfo.setCreatetime(new Date());
            updateAgentAddInfo.setCreatorid(wbUser.getUserName());
            updateAgentAddInfo.setCreatorname(wbUser.getDisplayName());
            updateAgentAddInfo.setHandleflag(false);
            updateAgentAddInfo.setRequeststatus(0);
            updateAgentAddInfo.setYearaddcode(this.distributedNumber.getYearAgentAddNum());
            this.budgetYearAgentaddinfoMapper.insert(updateAgentAddInfo);
        } else {
            updateAgentAddInfo.setId(bean.getId());
            updateAgentAddInfo.setUpdatetime(new Date());
            this.budgetYearAgentaddinfoMapper.updateById(updateAgentAddInfo);
        }
        return updateAgentAddInfo;
    }

    /**
     * 提交至OA系统
     */
    public void submitVerify(Long infoId, Boolean existLock, List<Map<String, Object>> list) throws Exception {
        ZookeeperShareLock lock = null;
        try {
            if (!existLock) {
                lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/yearAgentAddMoney/" + infoId, o -> {
                    throw new RuntimeException("正在提交年度动因预算追加,请勿重复点击");
                });
                lock.tryLock();
            }

            // 提交OA系统中
            List<Map<String, Object>> budAddInfo = this.budgetYearAgentaddinfoMapper.listYearAgentAddByInfoId(infoId);
            if (budAddInfo == null || budAddInfo.isEmpty()) {
                throw new RuntimeException("没有可追加的年度动因记录");
            }

            // 动因重复合并校验
            List<Long> yearAgentIds = budAddInfo.stream().map(v -> (Long) v.get("yearagentid")).filter(Objects::nonNull).collect(Collectors.toList());
            HashSet<Long> hashSet = new HashSet<>(yearAgentIds);
            if (yearAgentIds.size() != hashSet.size()) {
                throw new RuntimeException("请勿对相同的年度动因追加两次动因金额");
            }

            Map<String, Object> addInfoMap = budAddInfo.get(0);
            String empNo = addInfoMap.get("creatorid").toString();

            // 追加状态
            Integer requestStatus = (Integer) addInfoMap.get("requeststatus");
            if (requestStatus > 0) {
                throw new RuntimeException("提交失败，该流程已经提交或者审核");
            }

            String oAUserId;
            String oAdeptId;
            String userIdDeptId = this.oaService.getOaUserId(empNo, list);
            if ("0".equals(userIdDeptId)) {
                oAUserId = "0";
                oAdeptId = "0";
            } else {
                oAUserId = userIdDeptId.split(",")[0];
                oAdeptId = userIdDeptId.split(",")[1];
            }
            String userName = UserThreadLocal.get().getDisplayName();

            WorkflowInfo wi = new WorkflowInfo();
            wi.setCreatorId(oAUserId);
            wi.setRequestLevel("0");
            wi.setRequestName("年度预算追加流程--" + userName);

            WSBudgetYearAgentAdd wbAdd = new WSBudgetYearAgentAdd();
            wbAdd.setSqr(Integer.valueOf(oAUserId));
            wbAdd.setSsbm(oAdeptId);
            wbAdd.setWfid(flowid);

            List<WSBudgetYearAgentAddDetail> details = getWorkflowAgentAdd(budAddInfo, wbAdd);
            try {
                Object objRequestId = addInfoMap.get("requestid");
                Object oaCreatorId = addInfoMap.get("oacreatorid");
                if (objRequestId != null && oaCreatorId != null) {
                    this.oaService.deleteRequest(objRequestId.toString(), oaCreatorId.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 创建流程
            String requestId = createBudgetAgentAdd(wi, wbAdd, details);
            if (null == requestId || Integer.parseInt(requestId) < 0) {
                throw new RuntimeException("提交失败，oa系统未找到你的上级人员，请联系oa管理员。");
            }

            // 更新状态
            BudgetYearAgentaddinfo updateAddInfo = new BudgetYearAgentaddinfo();
            updateAddInfo.setId(infoId);
            updateAddInfo.setRequeststatus(1);
            updateAddInfo.setRequestid(requestId);
            updateAddInfo.setUpdatetime(new Date());
            updateAddInfo.setOacreatorid(oAUserId);
            this.budgetYearAgentaddinfoMapper.updateById(updateAddInfo);
        } finally {
            if (lock != null) {
                lock.unLock();
            }
        }
    }

    private List<WSBudgetYearAgentAddDetail> getWorkflowAgentAdd(List<Map<String, Object>> budAddInfo, WSBudgetYearAgentAdd wbAdd) throws Exception {
        Map<String, Object> addInfoMap = budAddInfo.get(0);

        // 创建时间
        Date createTime = (Date) addInfoMap.get("createtime");
        wbAdd.setSqrq(Constants.FORMAT_10.format(createTime));
        // 届别名称
        Long yearId = (Long) addInfoMap.get("yearid");
        wbAdd.setYsjb(this.budgetYearPeriodMapper.selectById(yearId).getPeriod());
        // 预算单位名称
        Long unitId = (Long) addInfoMap.get("unitid");
        String unitName = this.budgetUnitMapper.selectById(unitId).getName();
        wbAdd.setYsdw(unitName);
//        // 预算科目名称
//        Long subjectId = (Long) addInfoMap.get("subjectid");
//        wbAdd.setZjkm(this.budgetSubjectMapper.selectById(subjectId).getName());
        wbAdd.setZjkm("");
        // 本届别第几次追加
        Integer count = this.budgetYearAgentaddinfoMapper.selectCount(new QueryWrapper<BudgetYearAgentaddinfo>()
                .eq("yearid", yearId)
                .eq("unitid", unitId)
                .eq("requeststatus", 2));
        wbAdd.setZjcs((count + 1) + "");
//        // 科目年初预算
//        String kmncys = addInfoMap.get("kmncys").toString();
//        wbAdd.setNcys(kmncys);
//        // 科目累计追加
//        String kmljzj = addInfoMap.get("kmljzj").toString();
//        wbAdd.setLjzj(kmljzj);
//        // 科目累计执行
//        String kmljzx = addInfoMap.get("kmljzx").toString();
//        wbAdd.setLjzx(kmljzx);
//        // 科目累计拆进
//        String kmljcj = addInfoMap.get("kmljcj").toString();
//        // 科目累计拆出
//        String kmljcc = addInfoMap.get("kmljcc").toString();
//        // 本次追加后年度预算总额(科目) = 年初预算+追加前累计追加金额+本次追加+追加前拆进金额
//        String totals = addInfoMap.get("bctotal").toString();
//        wbAdd.setBczjhndysze(new BigDecimal(kmncys)
//                .add(new BigDecimal(kmljzj))
//                .add(new BigDecimal(totals))
//                .add(new BigDecimal(kmljcj)).toString());
//        // 本次追加后年度预算余额  = 年度预算总额  - 拆出  - 累计执行
//        wbAdd.setBczjhndysye(new BigDecimal(wbAdd.getBczjhndysze()).
//                subtract(new BigDecimal(kmljcc)).
//                subtract(new BigDecimal(kmljzx)).toString());
        // 是否省区预算单位
        if (this.oaService.isProvinceUnit(unitId)) {
            wbAdd.setIsswsp(1);
        } else {
            wbAdd.setIsswsp(0);
        }
        // 附件
        int code = -1;
        if (addInfoMap.get("fileurl") != null && StringUtils.isNotBlank(addInfoMap.get("fileurl").toString())) {
            String fileUrl = addInfoMap.get("fileurl").toString();
            // 附件名称
            String fileOriginName = addInfoMap.get("fileoriginname").toString();
            // oa密码
            String oaPassword = addInfoMap.get("oapassword").toString();

            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();

            // 创建文档
            String empNo = addInfoMap.get("creatorid").toString();
            code = this.oaService.createDoc(empNo, oaPassword, is, fileOriginName, fileUrl, "年度追加流程附件");
            if (code == 0) {
                throw new RuntimeException("系统错误!创建文档失败!");
            }
            is.close();
        }
        wbAdd.setFj(code + "");

        List<WSBudgetYearAgentAddDetail> details = new ArrayList<>();
        budAddInfo.forEach(yearAdd -> {
            WSBudgetYearAgentAddDetail detail = new WSBudgetYearAgentAddDetail();
            // 追加类型
            int type = Integer.parseInt(yearAdd.get("type").toString());
            detail.setSjid(yearAdd.get("detailId").toString());
            detail.setZjlx(type == 0 ? "追加金额" : "追加动因");
            // 动因名称
            detail.setDymc(yearAdd.get("name").toString());
            // 追加科目
            Long subjectId = (Long) yearAdd.get("subjectid");
            String subjectName = this.budgetSubjectMapper.selectById(subjectId).getName();
            detail.setZjkm(subjectName);
            // 追加金额
            BigDecimal total = new BigDecimal(yearAdd.get("total").toString());
            detail.setZjje(total);
            // 追加理由
            Object remark = yearAdd.get("remark");
            detail.setZjly(remark != null ? remark.toString() : "");
            //是否免罚
            detail.setSfsqmf((Boolean) yearAdd.get("is_exempt_fine"));
            //免罚理由
            Object exempt_fine_reason = yearAdd.get("exempt_fine_reason");
            detail.setMfly(exempt_fine_reason == null ? "" : exempt_fine_reason.toString());
            BudgetYearSubject budgetYearSubject = this.budgetYearSubjectMapper.selectOne(new QueryWrapper<BudgetYearSubject>()
                    .eq("yearId", yearId)
                    .eq("unitId", unitId)
                    .eq("subjectId", subjectId));
            if (budgetYearSubject == null) {
                throw new RuntimeException("请先在年度汇总中同步一下预算单位【" + unitName + "】数据!");
            }

            // 追加科目年度预算
            detail.setNcys(budgetYearSubject.getTotal().setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            // 追加科目累计执行
            detail.setLjzx(budgetYearSubject.getExecutemoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            // 本次追加后年度预算余额  = 年度预算总额  - 拆出  - 累计执行
            detail.setBczjhndysye(budgetYearSubject.getTotal()
                    .add(budgetYearSubject.getAddmoney())
                    .add(budgetYearSubject.getLendinmoney())
                    .add(total)
                    .subtract(budgetYearSubject.getLendoutmoney())
                    .subtract(budgetYearSubject.getExecutemoney())
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toString());

//                // 是否同时追加月度预算
//                // 获取当前月的金额
//                String curmonthmoney = yearAdd.get("curmonthmoney").toString();
//                if (Double.parseDouble(curmonthmoney) > 0) {
//                    // 说明追加了月度预算
//                    detail.setSfzjydys("是");
//                    detail.setZjyf(yearAdd.get("curmonthid").toString() + "月");
//                    detail.setZjydje(curmonthmoney);
//                } else {
//                    // 没有追加月度预算
//                    detail.setSfzjydys("否");
//                    detail.setZjyf("无");
//                    detail.setZjydje("无");
//                }
            detail.setWfid(flowid);
            details.add(detail);
        });
        return details;
    }

    /**
     * 创建年度追加流程
     */
    public String createBudgetAgentAdd(WorkflowInfo wi, WSBudgetYearAgentAdd wbYearAdd, List<WSBudgetYearAgentAddDetail> details) {
        Map<String, Object> main = (Map<String, Object>) JSON.toJSON(wbYearAdd);
        List<Map<String, Object>> detailList = (List<Map<String, Object>>) JSON.toJSON(details);
        return this.oaService.createWorkflow(wi, wbYearAdd.getWfid(), main, detailList);
    }

    /**
     * 删除年度预算追加
     */
    public void deleteYearAgentAdd(List<Long> ids) {
        for (Long id : ids) {
            BudgetYearAgentaddinfo agentAddInfo = this.budgetYearAgentaddinfoMapper.selectById(id);
            if (agentAddInfo != null) {
                if (agentAddInfo.getRequeststatus() != 0) {
                    throw new RuntimeException("此记录暂无法删除!");
                }
                this.budgetYearAgentaddMapper.delete(new QueryWrapper<BudgetYearAgentadd>().eq("infoid", id));
                this.budgetYearAgentaddinfoMapper.deleteById(id);
            }
        }
    }

    /**
     * 年度预算追加导出
     */
    public List<YearAgentAddInfoExcelData> exportAgentYearAdd(HashMap<String, Object> paramMap) {
        List<YearAgentAddInfoExcelData> resultList = new ArrayList<>();

        List<BudgetYearAgentaddinfo> addInfoList = this.budgetYearAgentaddinfoMapper.listYearAgentAddInfoByMap(paramMap);

        HashMap<Long, String> subjectNames = new HashMap<>(5);
        for (BudgetYearAgentaddinfo addInfo : addInfoList) {
            // 年度追加明细
            List<BudgetYearAgentadd> addList = this.budgetYearAgentaddMapper.selectList(new QueryWrapper<BudgetYearAgentadd>()
                    .eq("infoid", addInfo.getId()));
            if (!addList.isEmpty()) {
                for (BudgetYearAgentadd agentAdd : addList) {
                    YearAgentAddInfoExcelData rowData = new YearAgentAddInfoExcelData();
                    rowData.setNum(resultList.size() + 1);
                    rowData.setRequestStatus(Constants.getRequestStatus(addInfo.getRequeststatus()));
                    rowData.setYearAddCode(addInfo.getYearaddcode());
                    rowData.setPeriod(addInfo.getPeriod());
                    rowData.setUnitName(addInfo.getUnitName());
                    rowData.setIsExemptFine(agentAdd.getIsExemptFine() == null ? "否" : (agentAdd.getIsExemptFine() ? "是" : "否"));
                    rowData.setExemptFineReason(agentAdd.getExemptFineReason() == null ? "" : agentAdd.getExemptFineReason());
                    rowData.setExemptResult(agentAdd.getExemptResult() == null ? "" : (0 == agentAdd.getExemptResult() ? "免罚" : "罚款"));
                    rowData.setFineRemark(agentAdd.getFineRemark() == null ? "" : agentAdd.getFineRemark());
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
                    rowData.setType(agentAdd.getType() == 0 ? "追加金额" : "追加动因");
                    rowData.setTotal(agentAdd.getTotal());
                    rowData.setRemark(agentAdd.getRemark());
                    rowData.setAgentMoney(agentAdd.getAgentmoney().stripTrailingZeros().toPlainString());
                    BigDecimal addBefore = agentAdd.getAgentmoney()
                            .add(agentAdd.getAgentaddmoney())
                            .add(agentAdd.getAgentlendinmoney())
                            .subtract(agentAdd.getAgentlendoutmoney())
                            .subtract(agentAdd.getAgentexcutemoney());
                    rowData.setAddBefore(addBefore.stripTrailingZeros().toPlainString());
                    rowData.setAddAfter(addBefore.add(agentAdd.getTotal()).stripTrailingZeros().toPlainString());

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
     * 年度动因追加审核通过
     */
    public void endYearAgentAdd(EcologyParams params) throws Exception {
        // 流程id
        String requestId = params.getRequestid();

        ZookeeperShareLock lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/endYearAgentAdd/" + requestId, o -> {
            throw new RuntimeException("正在执行年度动因预算追加审核,请勿重复点击");
        });
        try {
            lock.tryLock();

            Date currentDate = new Date();
            // 通过流程id获取 年度动因追加
            BudgetYearAgentaddinfo agentAddInfo = this.budgetYearAgentaddinfoMapper.selectOne(new QueryWrapper<BudgetYearAgentaddinfo>()
                    .eq("requestid", requestId));
            if (agentAddInfo == null) {
                throw new RuntimeException("不存在该年度动因追加流程");
            } else if (agentAddInfo.getRequeststatus() == 2) {
                throw new RuntimeException("该年度动因追加流程已审核通过");
            }

            HashMap<Long, BigDecimal> subjectIds = new HashMap<>();
            EcologyWorkFlowValue value = EcologyClient.getWorkflowValue(params);
            Map<String, List<Map<String, String>>> detailTableValues = value.getDetailtablevalues();
            if (detailTableValues != null && !detailTableValues.isEmpty()) {
                // 获取明细表数据(一个发放单位对应一个收款单位，有多条记录)
                detailTableValues.forEach((str, values) -> {
                    values.forEach(detail -> {
                        String id = detail.get("sjid");
                        BudgetYearAgentadd yearAdd = this.budgetYearAgentaddMapper.selectById(id);
                        Long subjectId = yearAdd.getSubjectid();
                        if (!subjectIds.containsKey(subjectId)) {
                            subjectIds.put(subjectId, yearAdd.getTotal());
                        } else {
                            subjectIds.put(subjectId, subjectIds.get(subjectId).add(yearAdd.getTotal()));
                        }

                        // 获取追加类型
                        int type = yearAdd.getType();
                        // 年度动因
                        BudgetYearAgent budgetYearAgent;
                        // 月度动因
                        BudgetMonthAgent budgetMonthAgent;
                        // 当前月id
                        Long currentMonthId = yearAdd.getCurmonthid();
                        // 当前月金额
                        BigDecimal currentMonthMoney = yearAdd.getCurmonthmoney();
                        // 追加金额
                        if (type == 0) {
                            // 年度动因id
                            Long yearAgentId = yearAdd.getYearagentid();
                            budgetYearAgent = this.budgetYearAgentMapper.selectById(yearAgentId);
                            if (budgetYearAgent == null) {
                                throw new RuntimeException("追加的年度动因" + yearAdd.getName() + "不存在");
                            }
                            BudgetYearAgent updateYearAgent = new BudgetYearAgent();
                            updateYearAgent.setId(yearAgentId);
                            updateYearAgent.setUpdatetime(new Date());
                            updateYearAgent.setAddmoney(budgetYearAgent.getAddmoney().add(yearAdd.getTotal()));
                            this.budgetYearAgentMapper.updateById(updateYearAgent);

                            budgetMonthAgent = saveOrUpdateMonthAgent(budgetYearAgent, currentMonthId, currentMonthMoney, true);
                        } else {
                            Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
                                    .eq("unitId", yearAdd.getUnitid())
                                    .eq("subjectId", subjectId)
                                    .eq("name", yearAdd.getName()));
                            if (duplicationCount > 0) {
                                throw new RuntimeException("追加的年度动因名称【" + yearAdd.getName() + "】已经存在。");
                            }
                            // 追加动因
                            budgetYearAgent = new BudgetYearAgent();
                            budgetYearAgent.setYearid(yearAdd.getYearid());
                            budgetYearAgent.setUnitid(yearAdd.getUnitid());
                            budgetYearAgent.setSubjectid(subjectId);
                            budgetYearAgent.setTotal(BigDecimal.ZERO);
                            budgetYearAgent.setPretotal(BigDecimal.ZERO);
                            budgetYearAgent.setPreestimate(BigDecimal.ZERO);
                            budgetYearAgent.setCreatetime(currentDate);
                            budgetYearAgent.setUpdatetime(currentDate);
                            budgetYearAgent.setName(yearAdd.getName());
                            budgetYearAgent.setHappencount("");
                            budgetYearAgent.setRemark(yearAdd.getRemark());
                            budgetYearAgent.setAddmoney(yearAdd.getTotal());
                            budgetYearAgent.setLendoutmoney(BigDecimal.ZERO);
                            budgetYearAgent.setLendinmoney(BigDecimal.ZERO);
                            budgetYearAgent.setExecutemoney(BigDecimal.ZERO);
                            budgetYearAgent.setElasticflag(false);
                            budgetYearAgent.setElasticratio(BigDecimal.ZERO);
                            budgetYearAgent.setElasticmax(BigDecimal.ZERO);
                            budgetYearAgent.setAgenttype(1);
                            this.budgetYearAgentMapper.insert(budgetYearAgent);

                            budgetMonthAgent = saveOrUpdateMonthAgent(budgetYearAgent, currentMonthId, currentMonthMoney, false);
                        }
                        // 月度追加
                        if (budgetMonthAgent != null) {
                            // 同步月度预算科目执行数
                            this.budgetSysService.doSyncBudgetSubjectMonthAddMoney(budgetMonthAgent.getYearid(), budgetMonthAgent.getUnitid(), budgetMonthAgent.getSubjectid(), currentMonthId, currentMonthMoney, 1);
                        }
                        yearAdd.setYearagentid(budgetYearAgent.getId());
                        yearAdd.setMonthagentid(budgetMonthAgent != null ? budgetMonthAgent.getId() : null);
                        String mfjg = detail.get("mfjg");
                        yearAdd.setExemptResult(Integer.valueOf(mfjg));
                        yearAdd.setFineRemark(detail.get("fkyy"));
                        this.budgetYearAgentaddMapper.updateById(yearAdd);
                    });
                });
            }

            // 更新年度动因追加记录
            BudgetYearAgentaddinfo updateAddInfo = new BudgetYearAgentaddinfo();
            updateAddInfo.setId(agentAddInfo.getId());
            updateAddInfo.setRequeststatus(2);
            updateAddInfo.setHandleflag(true);
            updateAddInfo.setAudittime(new Date());
            this.budgetYearAgentaddinfoMapper.updateById(updateAddInfo);

            // 同步年度预算科目执行数
            subjectIds.forEach((subjectId, addTotal) -> this.budgetSysService.doSyncBudgetSubjectYearAddMoney(agentAddInfo.getYearid(), agentAddInfo.getUnitid(), subjectId, addTotal, 1));
        } finally {
            lock.unLock();
        }
    }

    private BudgetMonthAgent saveOrUpdateMonthAgent(BudgetYearAgent budgetYearAgent, Long monthId, BigDecimal monthMoney, boolean query) {
        if (monthMoney.compareTo(BigDecimal.ZERO) <= 0 || monthId == null) {
            return null;
        }
        BudgetMonthAgent budgetMonthAgent = null;
        if (query) {
            // 判断追加月度动因是否存在
            budgetMonthAgent = this.budgetMonthAgentMapper.selectOne(new QueryWrapper<BudgetMonthAgent>()
                    .eq("yearagentid", budgetYearAgent.getId())
                    .eq("monthId", monthId));
        }
        if (budgetMonthAgent == null) {
            // 追加一条月度动因
            budgetMonthAgent = new BudgetMonthAgent();
            budgetMonthAgent.setYearid(budgetYearAgent.getYearid());
            budgetMonthAgent.setUnitid(budgetYearAgent.getUnitid());
            budgetMonthAgent.setMonthid(monthId);
            budgetMonthAgent.setYearagentmoney(budgetYearAgent.getTotal());
            budgetMonthAgent.setYearaddmoney(budgetYearAgent.getAddmoney());
            budgetMonthAgent.setYearexecutemoney(budgetYearAgent.getExecutemoney());
            budgetMonthAgent.setYearlendinmoney(budgetYearAgent.getLendinmoney());
            budgetMonthAgent.setYearlendoutmoney(budgetYearAgent.getLendoutmoney());
            budgetMonthAgent.setSubjectid(budgetYearAgent.getSubjectid());
            budgetMonthAgent.setName(budgetYearAgent.getName());
            budgetMonthAgent.setElasticflag(budgetYearAgent.getElasticflag());
            budgetMonthAgent.setElasticratio(budgetYearAgent.getElasticratio());
            budgetMonthAgent.setBudgetsubjectid(budgetYearAgent.getBudgetsubjectid());
            budgetMonthAgent.setRemark(budgetYearAgent.getRemark());
            budgetMonthAgent.setProductid(budgetYearAgent.getProductid());
            budgetMonthAgent.setYearagentid(budgetYearAgent.getId());
            budgetMonthAgent.setM(BigDecimal.ZERO);
            budgetMonthAgent.setTotal(BigDecimal.ZERO);
            budgetMonthAgent.setAddmoney(monthMoney);
            budgetMonthAgent.setLendinmoney(BigDecimal.ZERO);
            budgetMonthAgent.setLendoutmoney(BigDecimal.ZERO);
            budgetMonthAgent.setExecutemoney(BigDecimal.ZERO);
            budgetMonthAgent.setCreatetime(new Date());
            budgetMonthAgent.setUpdatetime(new Date());
            budgetMonthAgent.setAgenttype(1);
            this.budgetMonthAgentMapper.insert(budgetMonthAgent);
        } else {
            budgetMonthAgent.setAddmoney(budgetMonthAgent.getAddmoney().add(monthMoney));
            budgetMonthAgent.setUpdatetime(new Date());
            this.budgetMonthAgentMapper.updateById(budgetMonthAgent);
        }
        return budgetMonthAgent;
    }

    /**
     * 年度动因追加退回
     */
    public void rejectYearAgentAdd(EcologyParams params) {
        String requestId = params.getRequestid();
        // 通过流程id获取 年度动因追加
        BudgetYearAgentaddinfo agentAddInfo = this.budgetYearAgentaddinfoMapper.selectOne(new QueryWrapper<BudgetYearAgentaddinfo>()
                .eq("requestid", requestId));
        if (agentAddInfo == null) {
            throw new RuntimeException("不存在该年度动因追加流程");
        } else if (agentAddInfo.getRequeststatus() == 2) {
            throw new RuntimeException("该年度动因追加流程已审核通过。退回失败");
        }

        BudgetYearAgentaddinfo updateAddInfo = new BudgetYearAgentaddinfo();
        updateAddInfo.setId(agentAddInfo.getId());
        updateAddInfo.setRequeststatus(-1);
        updateAddInfo.setHandleflag(true);
        updateAddInfo.setUpdatetime(new Date());
        updateAddInfo.setAudittime(new Date());
        this.budgetYearAgentaddinfoMapper.updateById(updateAddInfo);
    }

}
