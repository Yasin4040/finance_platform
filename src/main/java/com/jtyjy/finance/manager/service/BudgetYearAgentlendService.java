package com.jtyjy.finance.manager.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.ecology.webservice.workflow.WorkflowInfo;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.dto.YearAgentLendDTO;
import com.jtyjy.finance.manager.easyexcel.YearAgentLendExcelData;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.mapper.response.MonthAgentMoneyInfo;
import com.jtyjy.finance.manager.mapper.response.ReimbursementValidateMoney;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.utils.HttpUtil;
import com.jtyjy.finance.manager.vo.BudgetYearAgentLendVO;
import com.jtyjy.finance.manager.ws.BudgetYearAgentLending;
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

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetYearAgentlendService extends DefaultBaseService<BudgetYearAgentlendMapper, BudgetYearAgentlend> {

    private final TabChangeLogMapper loggerMapper;
    private final BudgetUnitMapper budgetUnitMapper;
    private final BudgetYearPeriodMapper budgetYearPeriodMapper;
    private final BudgetSubjectMapper budgetSubjectMapper;
    private final BudgetYearAgentMapper budgetYearAgentMapper;
    private final BudgetYearAgentlendMapper budgetYearAgentlendMapper;

    private final BudgetMonthAgentMapper monthAgentMapper;

    private final OaService oaService;
    private final DistributedNumber distributedNumber;
    private final BudgetSysService budgetSysService;
    private final CuratorFramework curatorFramework;

    @Value("${yearlend.workflowid}")
    private String flowid;
    @Value("${newjf.endYearLend.url}")
    private String newJfUrl;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_year_agentlend"));
    }

    /**
     * 查询预算拆借列表（分页）
     */
    public PageResult<BudgetYearAgentLendVO> listYearAgentLendPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {
        Page<BudgetYearAgentLendVO> pageBean = new Page<>(page, rows);

        List<BudgetYearAgentLendVO> resultList = this.budgetYearAgentlendMapper.listYearAgentLendPage(pageBean, paramMap);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * 获取可拆借预算科目
     */
    public List<BudgetSubject> listLendSubject(Long budgetUnitId) {
        return this.budgetSubjectMapper.listLendSubjects(budgetUnitId);
    }

    /**
     * 获取可拆借预算动因
     */
    public List<BudgetYearAgent> listLendAgent(Long budgetUnitId, Long budgetSubjectId) {
        List<BudgetYearAgent> budgetYearAgents = this.budgetYearAgentMapper.selectList(new QueryWrapper<BudgetYearAgent>()
//                .eq("agenttype", 0)
                .eq("unitid", budgetUnitId)
                .eq("subjectid", budgetSubjectId));
        budgetYearAgents.forEach(v -> v.setBalance(v.getTotal()
                .add(v.getAddmoney())
                .add(v.getLendinmoney())
                .subtract(v.getLendoutmoney())
                .subtract(v.getExecutemoney())));
        return budgetYearAgents;
    }

    /**
     * 新增预算拆借
     */
    public void saveYearAgentLend(YearAgentLendDTO bean,List<Map<String,Object>> list) throws Exception {

        String key = UserThreadLocal.getEmpNo();
        if (bean.getId() != null) {
            BudgetYearAgentlend agentLend = this.budgetYearAgentlendMapper.selectById(bean.getId());
            if (agentLend == null) {
                throw new RuntimeException("不存在该条预算拆借记录");
            } else if (agentLend.getRequeststatus() > 0) {
                throw new RuntimeException("【" + agentLend.getOrdernumber() + "】该单据已提交审核，无法修改");
            }
            key = bean.getId().toString();
        }

        ZookeeperShareLock lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/saveYearAgentLend/" + key, o -> {
            throw new RuntimeException("正在执行年度动因预算拆借,请勿重复点击");
        });
        try {
            lock.tryLock();

            // 检测是否可以修改预算拆借
            if (bean.getOutAgentId().equals(bean.getInAgentId())) {
                throw new RuntimeException("拆进和拆出年度动因不能相同");
            }

            // 检测拆进动因是否存在，若不存在，新增年度动因
            BudgetYearAgent inAgent;
            if (bean.getInAgentId() == null) {
                if (StringUtils.isBlank(bean.getInAgentName())) {
                    throw new RuntimeException("拆进年度动因名称不能为空");
                }
                Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
                        .eq("unitId", bean.getInUnitId())
                        .eq("subjectId", bean.getInSubjectId())
                        .eq("name", bean.getInAgentName()));
                if (duplicationCount > 0) {
                    throw new RuntimeException("拆进动因名称【" + bean.getInAgentName() + "】已经存在。");
                }
                inAgent = new BudgetYearAgent();
                inAgent.setUnitid(bean.getInUnitId());
                inAgent.setSubjectid(bean.getInSubjectId());
                inAgent.setName(bean.getInAgentName());
                inAgent.setTotal(BigDecimal.ZERO);
                inAgent.setAddmoney(BigDecimal.ZERO);
                inAgent.setLendinmoney(BigDecimal.ZERO);
                inAgent.setLendoutmoney(BigDecimal.ZERO);
                inAgent.setExecutemoney(BigDecimal.ZERO);
            } else {
                inAgent = this.budgetYearAgentMapper.selectById(bean.getInAgentId());
            }

            BudgetYearAgent outAgent = this.budgetYearAgentMapper.selectById(bean.getOutAgentId());
            if (inAgent == null || outAgent == null) {
                throw new RuntimeException("拆进或拆出年度动因不存在");
            }

            // 校验拆出金额是否超出
            BigDecimal balance = outAgent.getTotal()
                    .add(outAgent.getAddmoney())
                    .add(outAgent.getLendinmoney())
                    .subtract(outAgent.getLendoutmoney())
                    .subtract(outAgent.getExecutemoney());
            if (balance.compareTo(bean.getTotal()) < 0) {
                throw new RuntimeException("年度动因【" + outAgent.getName() + "】可拆出金额不足");
            }

            // 跨部门预算拆借
            if (bean.getIsAcross()) {
                if (inAgent.getUnitid().equals(outAgent.getUnitid())) {
                    throw new RuntimeException("拆进和拆出预算单位不能相同");
                }
            }

            // 验证年度预算是否已经通过审核，审核未通过的不允许拆借
            BudgetUnit inUnit = this.budgetUnitMapper.selectById(inAgent.getUnitid());
            BudgetUnit outUnit = this.budgetUnitMapper.selectById(outAgent.getUnitid());
            if (inUnit.getRequeststatus() != 2) {
                throw new RuntimeException("【" + inUnit.getName() + "】预算单位未审核通过，不能拆借");
            } else if (outUnit.getRequeststatus() != 2) {
                throw new RuntimeException("【" + outUnit.getName() + "】预算单位未审核通过，不能拆借");
            }

            // 验证“拆借金额”是否大于拆出动因的“年度可用拆借金额”
            yearAgentAvailableBalance(outAgent, bean.getTotal());

            // 新增或修改预算拆借记录
            saveOrUpdateAgentLend(inAgent, outAgent, bean,list);
        } finally {
            lock.unLock();
        }
    }

    /**
     * 新增或修改预算拆借记录
     */
    private void saveOrUpdateAgentLend(BudgetYearAgent inAgent, BudgetYearAgent outAgent, YearAgentLendDTO bean,List<Map<String,Object>> list) throws Exception {
        WbUser user = UserThreadLocal.get();
        Date currentDate = new Date();

        // 验证成功则插入数据
        BudgetYearAgentlend yearAgentLend = new BudgetYearAgentlend();
        yearAgentLend.setYearid(outAgent.getYearid());
        yearAgentLend.setInunitid(inAgent.getUnitid());
        yearAgentLend.setOutunitid(outAgent.getUnitid());
        yearAgentLend.setInsubjectid(inAgent.getSubjectid());
        yearAgentLend.setOutsubjectid(outAgent.getSubjectid());
        yearAgentLend.setInsubjectname(this.budgetSubjectMapper.selectById(inAgent.getSubjectid()).getName());
        yearAgentLend.setOutsubjectname(this.budgetSubjectMapper.selectById(outAgent.getSubjectid()).getName());
        yearAgentLend.setTotal(bean.getTotal());

        yearAgentLend.setRemark(bean.getRemark());
        yearAgentLend.setRequeststatus(0);
        yearAgentLend.setUpdatetime(currentDate);
        yearAgentLend.setHandleflag(false);
        yearAgentLend.setDeleteflag(false);

        yearAgentLend.setFileurl(bean.getFileUrl());
        yearAgentLend.setFileoriginname(bean.getFileOriginName());
        yearAgentLend.setOapassword(bean.getOaPassword());

        // 拆出信息
        yearAgentLend.setOutyearagentid(outAgent.getId());
        yearAgentLend.setOutname(outAgent.getName());
        yearAgentLend.setOutagentmoney(outAgent.getTotal());
        yearAgentLend.setOutagentaddmoney(outAgent.getAddmoney());
        yearAgentLend.setOutagentlendoutmoney(outAgent.getLendoutmoney());
        yearAgentLend.setOutagentlendinmoney(outAgent.getLendinmoney());
        yearAgentLend.setOutagentexcutemoney(outAgent.getExecutemoney());

        // 拆进信息
        yearAgentLend.setInyearagentid(inAgent.getId());
        yearAgentLend.setInname(inAgent.getName());
        yearAgentLend.setInagentmoney(inAgent.getTotal());
        yearAgentLend.setInagentaddmoney(inAgent.getAddmoney());
        yearAgentLend.setInagentlendoutmoney(inAgent.getLendoutmoney());
        yearAgentLend.setInagentlendinmoney(inAgent.getLendinmoney());
        yearAgentLend.setInagentexcutemoney(inAgent.getExecutemoney());

        yearAgentLend.setIsExemptFine(bean.getIsExemptFine()==null?false:bean.getIsExemptFine());
        yearAgentLend.setExemptFineReason(bean.getExemptFineReason());

        if (bean.getId() == null) {
            // 申请人id, 名字
            yearAgentLend.setCreatorid(user.getUserName());
            yearAgentLend.setCreatorname(user.getDisplayName());
            yearAgentLend.setCreatetime(currentDate);

            // 新增 （单据号）
            yearAgentLend.setOrdernumber(this.distributedNumber.getYearAgentLendNum());
            this.budgetYearAgentlendMapper.insert(yearAgentLend);
        } else {
            // 修改
            yearAgentLend.setId(bean.getId());
            this.budgetYearAgentlendMapper.update(yearAgentLend, Wrappers.<BudgetYearAgentlend>lambdaUpdate()
                    .set(BudgetYearAgentlend::getInyearagentid, inAgent.getId())
                    .eq(BudgetYearAgentlend::getId, bean.getId()));
        }

        // 提交至OA系统
        if (bean.getIsSubmit()) {
            commitYearAgentLend(yearAgentLend.getId(), true,list);
        }
    }

    /**
     * 拆借提交至OA系统
     */
    public void commitYearAgentLend(Long lendId, Boolean existLock,List<Map<String,Object>> list) throws Exception {
        ZookeeperShareLock lock = null;
        try {
            if (!existLock) {
                lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/saveYearAgentLend/" + lendId, o -> {
                    throw new RuntimeException("正在提交年度动因预算拆借,请勿重复点击");
                });
                lock.tryLock();
            }
            BudgetYearAgentlend yearAgentLend = this.budgetYearAgentlendMapper.selectById(lendId);
            if (yearAgentLend.getRequeststatus() != 0 && yearAgentLend.getRequeststatus() != -1) {
                throw new RuntimeException("提交失败，该流程已经提交或者审核");
            }

            BudgetYearAgent outAgent = this.budgetYearAgentMapper.selectById(yearAgentLend.getOutyearagentid());
            // 校验拆出金额是否超出
//            BigDecimal balance = outAgent.getTotal()
//                    .add(outAgent.getAddmoney())
//                    .add(outAgent.getLendinmoney())
//                    .subtract(outAgent.getLendoutmoney())
//                    .subtract(outAgent.getExecutemoney());

            MonthAgentMoneyInfo info = new MonthAgentMoneyInfo();
            info.setYearAgentId(yearAgentLend.getOutyearagentid());
            info.setYearId(outAgent.getYearid());
            info.setUnitId(outAgent.getUnitid());
            ReimbursementValidateMoney moneyResult = this.monthAgentMapper.getUnitYearAgentInfoByYearAgentId(info);
            if (moneyResult.execMoney().compareTo(yearAgentLend.getTotal()) < 0) {
                throw new RuntimeException("年度动因【" + outAgent.getName() + "】可拆出金额不足");
            }

//            if (balance.compareTo(yearAgentLend.getTotal()) < 0) {
//                throw new RuntimeException("年度动因【" + outAgent.getName() + "】可拆出金额不足");
//            }

            Map<String, Object> data = (Map<String, Object>) JSON.toJSON(yearAgentLend);
            // 届别、单位、科目名
            data.put("period", getPeriodInfo(yearAgentLend.getYearid()).getPeriod());
            // 支持跨部门拆借
            data.put("inunitname", getUnitInfo(yearAgentLend.getInunitid()).getName());
            data.put("outunitname", getUnitInfo(yearAgentLend.getOutunitid()).getName());
            data.put("insubjectname", getSubjectInfo(yearAgentLend.getInsubjectid()).getName());
            data.put("outsubjectname", getSubjectInfo(yearAgentLend.getOutsubjectid()).getName());

            // 第几次拆借
            Integer count = getYearLendTimes(yearAgentLend.getYearid()) + 1;
            data.put("count", count);

            //拆进、拆出余额
            BigDecimal outBalance = yearAgentLend.getOutagentmoney().add(yearAgentLend.getOutagentaddmoney()).subtract(yearAgentLend.getOutagentlendoutmoney())
                    .add(yearAgentLend.getOutagentlendinmoney()).subtract(yearAgentLend.getOutagentexcutemoney()).subtract(yearAgentLend.getTotal());
            BigDecimal inBalance = yearAgentLend.getInagentmoney().add(yearAgentLend.getInagentaddmoney()).subtract(yearAgentLend.getInagentlendoutmoney())
                    .add(yearAgentLend.getInagentlendinmoney()).subtract(yearAgentLend.getInagentexcutemoney()).add(yearAgentLend.getTotal());
            data.put("outbalance", outBalance);
            data.put("inbalance", inBalance);
            data.put("displayName", yearAgentLend.getCreatorname());
            String userIdDeptId = this.oaService.getOaUserId(yearAgentLend.getCreatorid(),list);
            String oaUserId = userIdDeptId.split(",")[0];
            String oaDeptId = userIdDeptId.split(",")[1];
            data.put("oaUserId", oaUserId);
            data.put("oaDeptId", oaDeptId);
            data.put("yearAgentLend", yearAgentLend);
            String requestId = yearAgentLendOa(data);
            if (null == requestId || Integer.parseInt(requestId) < 0) {
                throw new RuntimeException("提交失败,OA系统未找到你的上级人员，请联系OA管理员");
            }

            BudgetYearAgentlend updateAgentLend = new BudgetYearAgentlend();
            updateAgentLend.setId(yearAgentLend.getId());
            updateAgentLend.setRequeststatus(1);
            updateAgentLend.setUpdatetime(new Date());
            updateAgentLend.setOacreatorid(oaUserId);
            updateAgentLend.setRequestid(requestId);
            this.budgetYearAgentlendMapper.updateById(updateAgentLend);
        } finally {
            if (lock != null) {
                lock.unLock();
            }
        }
    }

    /**
     * 年度追加OA流程
     */
    public String yearAgentLendOa(Map<String, Object> data) {
        BudgetYearAgentLending yearLending = new BudgetYearAgentLending();
        String oaUserId = data.get("oaUserId").toString();
        String oaDeptId = data.get("oaDeptId").toString();

        // 创建附件
        Object obj = data.get("fileurl");
        String fileUrl = "";
        if (obj != null) {
            fileUrl = data.get("fileurl").toString();
        }
        int code = -1;
        if (StringUtils.isNotEmpty(fileUrl)) {
            try {
                String oaPassword = data.get("oapassword").toString();
                URLConnection connection = new URL(fileUrl).openConnection();
                InputStream is = connection.getInputStream();
                String fileOriginName = data.get("fileoriginname").toString();
                code = this.oaService.createDoc(data.get("creatorid").toString(), oaPassword, is, fileOriginName, fileUrl, "年度拆借流程附件");
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
        if (code == 0) {
            throw new RuntimeException("系统错误!创建文档失败!");
        }
        yearLending.setFj(code + "");
        yearLending.setSsbm(oaDeptId);
        yearLending.setSqr(oaUserId);
        // 拆进预算单位
        yearLending.setYsdw((String) data.get("inunitname"));
        // 拆出预算单位
        yearLending.setCcysdw((String) data.get("outunitname"));
        yearLending.setYsjb((String) data.get("period"));
        // 申请日期
        yearLending.setSqrq(Constants.FORMAT_10.format((Date) data.get("createtime")));
        yearLending.setCjcs(Integer.valueOf((data.get("count").toString())));
        yearLending.setCjje(BigDecimal.valueOf(Double.parseDouble(data.get("total").toString())));
        yearLending.setCjkm((String) data.get("insubjectname"));
        yearLending.setCckm((String) data.get("outsubjectname"));
        yearLending.setCjdy((String) data.get("inname"));
        yearLending.setCcdy((String) data.get("outname"));
        yearLending.setCjkmye(BigDecimal.valueOf(Double.parseDouble(data.get("inbalance").toString())));
        yearLending.setCckmye(BigDecimal.valueOf(Double.parseDouble(data.get("outbalance").toString())));
        yearLending.setCjyy((String) data.get("remark"));
        yearLending.setWfid(flowid);
        String username = (String) data.get("displayName");
        WorkflowInfo wi = new WorkflowInfo();
        wi.setCreatorId(oaUserId);
        wi.setRequestLevel("0");
        if (data.get("inunitname").equals(data.get("outunitname"))) {
            wi.setRequestName("年度预算拆借--" + username);
        } else {
            wi.setRequestName("年度预算拆借(跨部门)--" + username);
        }
        return createBudgetYearAgentLending(wi, yearLending, (BudgetYearAgentlend) data.get("yearAgentLend"));
    }

    public String createBudgetYearAgentLending(WorkflowInfo wi, BudgetYearAgentLending yearAgentLending, BudgetYearAgentlend yearAgentLend) {
        Map<String, Object> main = (Map<String, Object>) JSON.toJSON(yearAgentLending);
        if (null != yearAgentLend.getRequestid()) {
            this.oaService.deleteRequest(yearAgentLend.getRequestid(), yearAgentLend.getOacreatorid());
        }
        return this.oaService.createWorkflow(wi, yearAgentLending.getWfid(), main, null);
    }

    /**
     * 拆借金额不能大于“年度可用拆借余额”
     * 控制逻辑中的 ---“年度可用拆借余额” = 动因年度预算 - 已申请拆借但未审批通过的-动因已执行数
     */
    public void yearAgentAvailableBalance(BudgetYearAgent yearAgent, BigDecimal lendTotal) {
        BigDecimal availableBalance = yearAgent.getTotal()
                .add(yearAgent.getAddmoney())
                .add(yearAgent.getLendinmoney())
                .subtract(yearAgent.getLendoutmoney())
                .subtract(yearAgent.getExecutemoney());

        List<BudgetYearAgentlend> yearAgentLends = this.budgetYearAgentlendMapper.selectList(new QueryWrapper<BudgetYearAgentlend>()
                .eq("requeststatus", 1)
                .eq("outyearagentid", yearAgent.getId()));
        BigDecimal notPassBal = BigDecimal.ZERO;
        // 已申请拆借但未审批通过的
        for (BudgetYearAgentlend agentLend : yearAgentLends) {
            notPassBal = notPassBal.add(agentLend.getTotal());
        }

        BigDecimal balance = availableBalance.subtract(notPassBal);
        if (lendTotal.compareTo(balance) > 0) {
            throw new RuntimeException("拆借金额【" + lendTotal + "】大于拆出动因的年度可用拆借金额【" + balance.stripTrailingZeros().toPlainString() + "】,不允许拆借");
        }
    }

    /**
     * 本届第几次拆借
     */
    public Integer getYearLendTimes(Long yearId) {
        return this.budgetYearAgentlendMapper.selectCount(new QueryWrapper<BudgetYearAgentlend>()
                .eq("yearid", yearId)
                .gt("requeststatus", 0));
    }

    /**
     * 届别信息
     */
    public BudgetYearPeriod getPeriodInfo(Long yearId) {
        return this.budgetYearPeriodMapper.selectById(yearId);
    }

    /**
     * 单位信息
     */
    public BudgetUnit getUnitInfo(Long unitId) {
        return this.budgetUnitMapper.selectById(unitId);
    }

    /**
     * 科目信息
     */
    public BudgetSubject getSubjectInfo(Long subjectId) {
        return this.budgetSubjectMapper.selectById(subjectId);
    }

    /**
     * 删除预算拆借
     */
    public void deleteYearAgentLend(List<Long> ids) {
        for (Long id : ids) {
            BudgetYearAgentlend yearAgentLend = this.budgetYearAgentlendMapper.selectById(id);
            if (yearAgentLend != null) {
                if (yearAgentLend.getRequeststatus() > 0) {
                    throw new RuntimeException("【" + yearAgentLend.getOrdernumber() + "】该单据已提交审核，不允许删除");
                }
                yearAgentLend.setDeleteflag(true);
                this.budgetYearAgentlendMapper.updateById(yearAgentLend);
            }
        }
    }

    /**
     * 年度预算拆借导出
     */
    public List<YearAgentLendExcelData> exportAgentYearLend(HashMap<String, Object> paramMap) {
        List<YearAgentLendExcelData> resultList = new ArrayList<>();

        List<BudgetYearAgentLendVO> agentLendList = this.budgetYearAgentlendMapper.listYearAgentLendPage(null, paramMap);

        agentLendList.forEach(v -> {
            YearAgentLendExcelData excelData = new YearAgentLendExcelData();
            excelData.setMf(v.getIsExemptFine()==null?"否":(v.getIsExemptFine()?"是":"否"));
            excelData.setNum(resultList.size() + 1);
            excelData.setRequestStatus(Constants.getRequestStatus(v.getRequestStatus()));
            excelData.setOrderNumber(v.getOrderNumber());
            excelData.setYearPeriod(v.getYearPeriod());
            excelData.setInBudgetUnitName(v.getInBudgetUnitName());
            excelData.setOutBudgetUnitName(v.getOutBudgetUnitName());
            excelData.setTotal(v.getTotal().setScale(2, BigDecimal.ROUND_HALF_UP));
            excelData.setInSubjectName(v.getInSubjectName());
            excelData.setInAgentName(v.getInAgentName());
            excelData.setInYearTotal(v.getInYearTotal().setScale(2, BigDecimal.ROUND_HALF_UP));
            excelData.setInYearBalance(v.getInYearBalance().setScale(2, BigDecimal.ROUND_HALF_UP));
            excelData.setOutSubjectName(v.getOutSubjectName());
            excelData.setOutAgentName(v.getOutAgentName());
            excelData.setOutYearTotal(v.getOutYearTotal().setScale(2, BigDecimal.ROUND_HALF_UP));
            excelData.setOutYearBalance(v.getOutYearBalance().setScale(2, BigDecimal.ROUND_HALF_UP));
            excelData.setRemark(v.getRemark());
            excelData.setCreatorName(v.getCreatorName());
            excelData.setCreateTime(Constants.FORMAT_10.format(v.getCreateTime()));
            excelData.setAuditTime(v.getAuditTime() != null ? Constants.FORMAT_10.format(v.getAuditTime()) : "");

            resultList.add(excelData);
        });
        return resultList;
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * 年度动因拆借审核通过
     */
    public void endYearAgentLend(EcologyParams params) throws Exception {
        String requestId = params.getRequestid();
        ZookeeperShareLock lock = new ZookeeperShareLock(this.curatorFramework, "/finance-platform/endYearAgentLend/" + requestId, o -> {
            throw new RuntimeException("正在执行年度动因预算拆借审核,请勿重复点击");
        });
        try {
            lock.tryLock();
            BudgetYearAgentlend yearAgentLend = this.budgetYearAgentlendMapper.selectOne(new QueryWrapper<BudgetYearAgentlend>()
                    .eq("requestid", requestId));
            if (yearAgentLend == null) {
                throw new RuntimeException("不存在该年度动因拆借流程");
            } else if (yearAgentLend.getRequeststatus() == 2) {
                throw new RuntimeException("该年度动因拆借流程已审核通过");
            }

            // 更新拆出动因记录
            BudgetYearAgent outAgent = this.budgetYearAgentMapper.selectById(yearAgentLend.getOutyearagentid());
            if (outAgent == null) {
                throw new RuntimeException("拆出动因不存在或已删除");
            }
            BudgetYearAgent updateOutAgent = new BudgetYearAgent();
            updateOutAgent.setId(outAgent.getId());
            updateOutAgent.setLendoutmoney(outAgent.getLendoutmoney().add(yearAgentLend.getTotal()));
            this.budgetYearAgentMapper.updateById(updateOutAgent);

            // 更新拆进动因记录
            BudgetYearAgentlend updateAgentLend = new BudgetYearAgentlend();
            if (yearAgentLend.getInyearagentid() == null) {
                Integer duplicationCount = this.budgetYearAgentMapper.selectCount(new QueryWrapper<BudgetYearAgent>()
                        .eq("unitId", yearAgentLend.getInunitid())
                        .eq("subjectId", yearAgentLend.getInsubjectid())
                        .eq("name", yearAgentLend.getInname()));
                if (duplicationCount > 0) {
                    throw new RuntimeException("拆进动因名称【" + yearAgentLend.getInname() + "】已经存在。");
                }

                BudgetYearAgent inAgent = new BudgetYearAgent();
                inAgent.setYearid(yearAgentLend.getYearid());
                inAgent.setUnitid(yearAgentLend.getInunitid());
                inAgent.setTotal(BigDecimal.ZERO);
                inAgent.setPretotal(BigDecimal.ZERO);
                inAgent.setSubjectid(yearAgentLend.getInsubjectid());
                inAgent.setAgenttype(1);
                inAgent.setName(yearAgentLend.getInname());
                inAgent.setComputingprocess("");
                inAgent.setRemark("年度拆借追加的动因");
                inAgent.setElasticflag(false);
                inAgent.setAddmoney(BigDecimal.ZERO);
                inAgent.setLendoutmoney(BigDecimal.ZERO);
                inAgent.setLendinmoney(yearAgentLend.getTotal());
                inAgent.setExecutemoney(BigDecimal.ZERO);
                inAgent.setCreatetime(new Date());
                this.budgetYearAgentMapper.insert(inAgent);

                // 修改拆进动因Id
                updateAgentLend.setInyearagentid(inAgent.getId());
            } else if (yearAgentLend.getInyearagentid() > 0) {
                BudgetYearAgent inAgent = this.budgetYearAgentMapper.selectById(yearAgentLend.getInyearagentid());
                if (inAgent == null) {
                    throw new RuntimeException("拆进动因不存在或已删除");
                }

                BudgetYearAgent updateInAgent = new BudgetYearAgent();
                updateInAgent.setId(inAgent.getId());
                updateInAgent.setLendinmoney(inAgent.getLendinmoney().add(yearAgentLend.getTotal()));
                this.budgetYearAgentMapper.updateById(updateInAgent);
            }

            // 更新拆借记录
            updateAgentLend.setId(yearAgentLend.getId());
            updateAgentLend.setRequeststatus(2);
            updateAgentLend.setHandleflag(true);
            updateAgentLend.setAudittime(new Date());
            this.budgetYearAgentlendMapper.updateById(updateAgentLend);

            yearLend(yearAgentLend);

            /*
             增加积分
             */
            try{
                HttpUtil.doGet(newJfUrl+"?empNo="+yearAgentLend.getCreatorid()+"&processNo="+yearAgentLend.getOrdernumber());
            }catch (Exception ignored){}
        } finally {
            lock.unLock();
        }
    }

    public void yearLend(BudgetYearAgentlend yearAgentLend) {
        if (!yearAgentLend.getInsubjectid().equals(yearAgentLend.getOutsubjectid())) {
            // 拆进  同步年度预算科目执行数
            this.budgetSysService.doSyncBudgetSubjectYearAddMoney(yearAgentLend.getYearid(), yearAgentLend.getInunitid(), yearAgentLend.getInsubjectid(), yearAgentLend.getTotal(), 3);

            // 拆出  同步年度预算科目执行数
            this.budgetSysService.doSyncBudgetSubjectYearAddMoney(yearAgentLend.getYearid(), yearAgentLend.getOutunitid(), yearAgentLend.getOutsubjectid(), yearAgentLend.getTotal(), 4);
        }
    }

    /**
     * 年度动因拆借退回
     */
    public void rejectYearAgentLend(EcologyParams params) {
        String requestId = params.getRequestid();

        BudgetYearAgentlend yearAgentLend = this.budgetYearAgentlendMapper.selectOne(new QueryWrapper<BudgetYearAgentlend>()
                .eq("requestid", requestId));
        if (yearAgentLend == null) {
            throw new RuntimeException("不存在该年度动因拆借流程");
        } else if (yearAgentLend.getRequeststatus() == 2) {
            throw new RuntimeException("该年度动因拆借流程已审核通过。退回失败");
        }

        BudgetYearAgentlend updateAgentLend = new BudgetYearAgentlend();
        updateAgentLend.setId(yearAgentLend.getId());
        updateAgentLend.setRequeststatus(-1);
        updateAgentLend.setHandleflag(true);
        updateAgentLend.setUpdatetime(new Date());
        this.budgetYearAgentlendMapper.updateById(updateAgentLend);
    }
}
