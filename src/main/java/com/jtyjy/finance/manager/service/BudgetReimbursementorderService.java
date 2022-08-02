package com.jtyjy.finance.manager.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.jdbc.JdbcTemplateService;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.easy.excel.ExportHelper;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.cache.UserCache;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.constants.ReimbursementStepHelper;
import com.jtyjy.finance.manager.constants.StatusConstants;
import com.jtyjy.finance.manager.dto.CheckPassRequest;
import com.jtyjy.finance.manager.dto.ReimBursementDTO;
import com.jtyjy.finance.manager.dto.ReimbursementRequest;
import com.jtyjy.finance.manager.dto.bxExcel.*;
import com.jtyjy.finance.manager.easyexcel.PayUnitBankSumExcelData;
import com.jtyjy.finance.manager.easyexcel.PayeeDetailExcelData;
import com.jtyjy.finance.manager.enmus.LendTypeEnum;
import com.jtyjy.finance.manager.enmus.PaymoneyTypeEnum;
import com.jtyjy.finance.manager.enmus.ReimbursementTypeEnmu;
import com.jtyjy.finance.manager.event.bx.BxCodeRequest;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.mapper.response.MonthAgentMoneyInfo;
import com.jtyjy.finance.manager.mapper.response.ReimbursementValidateMoney;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.QRCodeUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.*;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import com.klcwqy.easy.lock.impl.ZookeeperShareLock;
import com.xxl.job.core.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.csource.common.MyException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@JdbcSelector(value = "defaultJdbcTemplateService")
public class BudgetReimbursementorderService extends DefaultBaseService<BudgetReimbursementorderMapper, BudgetReimbursementorder> {
    @Autowired
    private BudgetReimbursementorderMapper mapper;
    @Autowired
    private TabChangeLogMapper loggerMapper;
    @Autowired
    private DistributedNumber distributedNumber;
    @Autowired
    private BudgetReimbursementorderDetailService detailService;
    @Autowired
    private BudgetReimbursementorderAllocatedService allocatedService;
    @Autowired
    private BudgetReimbursementorderCashService cashService;
    @Autowired
    private BudgetReimbursementorderTransService transService;
    @Autowired
    private BudgetReimbursementorderTravelService travelService;
    @Autowired
    private BudgetReimbursementorderEntertainService entertainService;
    @Autowired
    private BudgetReimbursementorderPaymentService paymentService;
    @Autowired
    private BudgetPaymoneyService paymoneyService;
    @Autowired
    private BudgetBillingUnitAccountService billingUnitAccountService;
    @Autowired
    private BudgetLendmoneyUselogService lendmoneyUselogService;
    @Autowired
    private BudgetBillingUnitService billingUnitService;
    @Autowired
    private BudgetReimbursementorderScanlogService scanLogService;
    @Autowired
    private BudgetReimbursementorderVerifylogService verifylogService;
    @Autowired
    private BudgetReimbursementorderFdtaskService fdTaskService;
    @Autowired
    private BudgetSubjectService subjectService;
    @Autowired
    private BudgetUnitService unitService;
    @Autowired
    private BudgetYearAgentService yearAgentService;
    @Autowired
    private BudgetMonthAgentService monthAgentService;
    @Autowired
    private BudgetMonthAgentMapper monthAgentMapper;
    @Autowired
    private TabProcedureService procedureService;
    @Autowired
    private TabLinkLimitService linkLimitService;
    @Autowired
    private BudgetRepaymoneyService repaymoneyService;
    @Autowired
    private BudgetLendmoneyService lendmoneyService;
    @Autowired
    private BudgetReimbursementorderCashCopyService cashCopyService;
    @Autowired
    private BudgetReimbursementorderTransCopyService transCopyService;
    @Autowired
    private BudgetYearPeriodService yearService;
    @Autowired
    private BudgetReimbursementorderScanlogMapper scanlogMapper;
    @Autowired
    private BudgetPaymoneyMapper paymoneyMapper;
    @Autowired
    private BudgetUnitMapper unitMapper;
    @Autowired
    private BudgetReimbursementorderFdtaskDetailMapper fdTaskDetailMapper;
    @Autowired
    private WbBanksMapper wbMapper;
    @Autowired
    private TabDmService dmService;
    @Autowired
    private WbUserService userService;
    @Autowired
    private BudgetReimburmentTimedetailService timeDetailService;
    @Autowired
    private CuratorFramework client;

    @Autowired
    private BudgetLendmoneyMapper lendmoneyMapper;

    @Autowired
    private BudgetMonthPeriodMapper monthPeriodMapper;

    @Autowired
    private MessageSender sender;

    @Autowired
    private BudgetSpecialTravelNameListService specialTravelNameListService;

    //二维码格式
    private static final String QRCODE_FORMAT = ".png";

    @Value("${file.temp.path}")
    private String file_temp_path;

    @Value("${file.share.template}")
    private String file_share_template;

    //扫描报销二维码页面
    @Value("${bx.qrcode.url}")
    private String bx_qrcode_url;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_reimbursementorder"));
    }

    /**
     * 保存或修改、详情、划拨、转账、现金、差旅、招待、冲账单
     *
     * @param request
     * @return
     * @throws Exception
     */
    public int saveOrUpdate(ReimbursementRequest request) throws Exception {
        //保存订单
        BudgetReimbursementorder order = request.getOrder();
        //判断是否存在
        boolean exist = order.getId() != null;
        order.setVersion(null);
        BudgetReimbursementorder _order = null;
        if (exist) {
            _order = this.getById(order.getId());
        }
        String version = (!exist) ? "0" : String.valueOf(Integer.parseInt(_order.getVersion()) + 1);
        if (!exist) {
            //保存
            String bxdNum = distributedNumber.getBxdNum();
            WbUser user = null;
            if(request.getIsFixAsset()!=null && request.getIsFixAsset()){
                user = this.userService.getByEmpNo(order.getReimperonsid());
            }
            //设置基本信息
            order.setBase(bxdNum, request.getIsProjectBx(),request.getIsFixAsset(),user);
        }
        order.setVersion(version);
        order.setUpdatetime(new Date());
        this.saveOrUpdate(order);
        this.setQrcode(order);
        this.saveOrUpdate(order);
        //保存附表信息
        this.saveFbInfo(request, order);
        return 8;
    }

    /**
     * 设置二维码
     *
     * @param order
     * @throws Exception
     */
    public void setQrcode(BudgetReimbursementorder order) throws Exception {
        //生成二维码Base64
        String qrcodebase64str = QRCodeUtil.createBase64Qrcode(this.bx_qrcode_url + order.getId() + "-" + order.getVersion(), this.file_temp_path + File.separator + order.getId() + QRCODE_FORMAT);
        order.setQrcodebase64str(qrcodebase64str);
        try {
            File pngFile = new File(this.file_temp_path + File.separator + order.getId() + QRCODE_FORMAT);
            if (pngFile.exists()) {
                pngFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存附表信息
     *
     * @param request
     * @param order
     * @throws Exception
     */
    private int saveFbInfo(ReimbursementRequest request, BudgetReimbursementorder order) throws Exception {
        //保存详情
        this.detailService.saveByOrder(request.getOrderDetail(), order);
        //保存冲账
        this.paymentService.saveByOrder(request.getOrderPayment(), order);
        //保存转账
        this.transService.saveByOrder(request.getOrderTrans(), order);
        //保存现金
        this.cashService.saveByOrder(request.getOrderCash(), order);
        //保存划拨
        this.allocatedService.saveByOrder(request.getOrderAllocated(), order);
        //保存差旅
        this.travelService.saveByOrder(request.getOrderTravel(), order);
        //保存招待
        this.entertainService.saveByOrder(request.getOrderEntertain(), order);
        return 7;
    }

    /**
     * 保存或修改后提交报销单、详情、划拨、转账、现金、差旅、招待、冲账单
     *
     * @param request
     * @param isCommit
     * @return
     * @throws Exception
     */
    public int saveOrUpdateAndSubmit(ReimbursementRequest request, boolean isCommit) throws Exception {
        //保存或修改
        this.saveOrUpdate(request);
        //提交
        if (isCommit) {
            this.submit(request);
        }
        return 8;
    }

    /**
     * 提交
     *
     * @param request
     * @return
     * @throws Exception
     */
    public int submit(ReimbursementRequest request) throws Exception {
        String orderId = request.getOrder().getId().toString();
        ZookeeperShareLock lock = new ZookeeperShareLock(this.client, "/finance-platform/bxSubmit/" + orderId, o -> {
            throw new RuntimeException("报销单正在提交,请勿重复操作");
        });
        try {
            lock.tryLock();

            // 设置报销单为已提交状态
            BudgetReimbursementorder order = request.getOrder();
            order.setReuqeststatus(StatusConstants.BX_SUBMIT);
            order.setSubmittime(new Date());
            order.setCurscantime(null);
            order.setCurscanstatus(0);
            order.setReceivestatus(0);
            order.setCurscanstatusname(null);
            order.setCurscanername(null);
            order.setCurscaner(null);

            //确定审核环节
            String workFlowStep = this.ensureWorkFlowStep(request);
            order.setWorkFlowStep(workFlowStep);
            //获取版本
            TabProcedure tabProcedure = this.procedureService.getCurrentProcedure(request.getOrder().getYearid(), "1");
//		String sql = "select max(the_version) from tab_flow_condition where flow_type = '1'";
//		Integer conditionVersion = this.jdbcTemplateService.queryForObject(sql, Integer.class);
            Integer conditionVersion = tabProcedure.getId().intValue();
            order.setWorkFlowVersion(conditionVersion);
            this.saveOrUpdate(order);
            //锁定借款单
            this.lockBorrow(request);
        } finally {
            lock.unLock();
        }
        return 1;
    }

    /**
     * 确认审核环节
     *
     * @param request
     * @return
     * @throws Exception
     */
    public String ensureWorkFlowStep(ReimbursementRequest request) throws Exception {
        Long yearid = request.getOrder().getYearid();
        TabProcedure procedure = this.procedureService.getCurrentProcedure(yearid, "1");
        String steps = procedure.getProcedureLinkOrder();
        if (StringUtils.isBlank(steps)) {
            throw new Exception("环节为空！");
        }
        //此处已经获取到了顺序
        String[] orders = steps.split(",");
        List<String> stepList = Arrays.asList(orders);

        /**
         * 按照计入执行的科目来做环节限制(如果科目相同以划拨为准)
         * 根据科目汇总
         */
        Map<Long, BigDecimal> subjectMoneyMap = new HashMap<>();
        request.getOrderDetail().stream().filter(e -> e.getReimflag()).collect(Collectors.groupingBy(e -> e.getSubjectid())).forEach((subjectid, details) -> {
            subjectMoneyMap.put(subjectid, details.stream().map(e -> e.getReimmoney()).reduce(BigDecimal.ZERO, BigDecimal::add));
        });
        request.getOrderAllocated().stream().collect(Collectors.groupingBy(e -> e.getSubjectid())).forEach((subjectid, details) -> {
            subjectMoneyMap.put(subjectid, details.stream().map(e -> e.getAllocatedmoney()).reduce(BigDecimal.ZERO, BigDecimal::add));
        });
        //查询科目条件
        List<TabLinkLimit> limits = this.linkLimitService.getByPidAndSubjectIds(procedure.getId(), subjectMoneyMap.keySet());
        if (limits == null || limits.size() == 0) {
            //无条件限制，全部都走
            return steps;
        }
        //取出所有预算科目的报销金额
        String sper = "@$&%@";
        Map<String, Double> dmSubjectLimitMap = new HashMap<>();
        for (TabLinkLimit tempLimit : limits) {
            String linkDms = tempLimit.getLinkDm();
            for (String linkDm : linkDms.split(",")) {//环节代码可多选
                dmSubjectLimitMap.put(linkDm + sper + tempLimit.getSubjectId(), tempLimit.getMaxLimit());
            }

        }
        StringJoiner sj = new StringJoiner(",");
        for (String stepDm : stepList) {
            /**
             * update by minzhq
             * 判断该报销单所需要走的环节
             */
            //此环节没有环节限制
            long count = limits.stream().filter(e -> stepDm.equals(e.getLinkDm())).count();
            if (count == 0) {
                sj.add(stepDm);
                continue;
            }
            Set<Map.Entry<Long, BigDecimal>> entrySet = subjectMoneyMap.entrySet();
            boolean flag = false;
            inner:
            for (Map.Entry<Long, BigDecimal> es : entrySet) {
                Long subjectId = es.getKey();
                BigDecimal reimMoney = es.getValue();
                Double limit = dmSubjectLimitMap.get(stepDm + sper + subjectId);
                //环节限制中存在此科目
                if (limit != null) {
                    if (reimMoney.compareTo(new BigDecimal(limit.toString())) >= 0) {
                        //满足金额条件
                        flag = true;
                        break inner;
                    }
                }
            }
            if (flag) sj.add(stepDm);
        }
        return sj.toString();
    }

    public void exportBxInfo(Long id, HttpServletResponse response) throws Exception {
        long time = new Date().getTime();
        String tmpFilePath = this.file_temp_path + File.separator + "bxinfo_" + time + ".xls";
        try {
            ReimbursementRequest result = this.detail(id);
            if (null == result) {
                return;
            }
            List<BudgetReimbursementorderTrans> trans = this.transService.list(new QueryWrapper<BudgetReimbursementorderTrans>().eq("reimbursementid",id));
            result.setOrderTrans(trans);
            //现金
            List<BudgetReimbursementorderCash> cash = this.cashService.list(new QueryWrapper<BudgetReimbursementorderCash>().eq("reimbursementid",id));
            result.setOrderCash(cash);
            //划拨
            List<BudgetReimbursementorderAllocated> allocated = this.allocatedService.list(new QueryWrapper<BudgetReimbursementorderAllocated>().eq("reimbursementid",id));
            result.setOrderAllocated(allocated);
            Map<Class<?>, Map<String, Object>> export_map = new HashMap<Class<?>, Map<String, Object>>(3);
            ExportHelper exportHelp = new ExportHelper(file_share_template + File.separator + "bxApplyImportTemplate.xls");
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> cz_map = new HashMap<>();
            Map<String, Object> zz_map = new HashMap<>();
            Map<String, Object> cl_map = new HashMap<>();
            Map<String, Object> zd_map = new HashMap<>();
            Map<String, Object> hb_map = new HashMap<>();
            BudgetReimbursementorder orderInfo = result.getOrder();
            if (null == orderInfo) {
                return;
            }
            map.put("b2", orderInfo.getYearName());
            map.put("d2", orderInfo.getMonthid() + "月");
            map.put("f2", orderInfo.getUnitName());
            map.put("b3", orderInfo.getReimperonsNo());
            map.put("d3", orderInfo.getReimperonsname());
            map.put("f3", Constants.FORMAT2_10.format(orderInfo.getReimdate()));
            map.put("h3", orderInfo.getOthermoney().setScale(2, BigDecimal.ROUND_HALF_UP));
            map.put("b4", orderInfo.getAttachcount());
            map.put("d4", ReimbursementTypeEnmu.getValue(orderInfo.getBxtype()));
            map.put("f4", orderInfo.getReimcode());
            List<BxDetailDto> bxDetailList = new ArrayList<>();
            for (BudgetReimbursementorderDetail detail : result.getOrderDetail()) {
                BxDetailDto dto = new BxDetailDto();
                dto.setUnitName(detail.getBunitname());
                dto.setSubjectName(detail.getSubjectname());
                dto.setAgentName(detail.getMonthagentname());
                dto.setBxAmount(detail.getReimmoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                dto.setRemark(detail.getRemark());
                dto.setInclude(detail.getReimflag() ? "是" : "否");
                bxDetailList.add(dto);
            }
            map.put("detail", bxDetailList);
            List<StrickDetailDto> czDetailList = new ArrayList<>();
            for (BudgetReimbursementorderPayment detail : result.getOrderPayment()) {
                StrickDetailDto dto = new StrickDetailDto();
                dto.setJkCode(detail.getLendcode());
                dto.setJkName(detail.getLendmoneyname());
                dto.setCzMoney(detail.getPaymentmoney().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                czDetailList.add(dto);
            }
            if (!czDetailList.isEmpty()) {
                cz_map.put("detail", czDetailList);
                export_map.put(StrickSheetDto.class, cz_map);
            }
            List<TransferDetailDto> zzDetailList = new ArrayList<>();
            for (BudgetReimbursementorderTrans detail : result.getOrderTrans()) {
                TransferDetailDto dto = new TransferDetailDto();
                dto.setName(detail.getPayeename());
                dto.setAccount(detail.getPayeebankaccount());
                dto.setMoney(detail.getTransmoney().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setUnit(detail.getDraweeunitname());
                zzDetailList.add(dto);
            }
            if (!zzDetailList.isEmpty()) {
                zz_map.put("detail", zzDetailList);
                export_map.put(TransferSheetDto.class, zz_map);
            }
            BigDecimal travelerNum = BigDecimal.ZERO;
            if(StringUtils.isNotBlank(orderInfo.getTraveler())){
                travelerNum = new BigDecimal(orderInfo.getTraveler().split(",").length);
            }
            if (StringUtils.isNotBlank(orderInfo.getTraveler())) {
                StringJoiner sj = new StringJoiner(",");
                for (String empNo : orderInfo.getTraveler().split(",")) {
                    WbUser user = this.userService.getByEmpNo(empNo);
                    if (null != user) {
                        sj.add(user.getDisplayName());
                    }else {
                        sj.add(empNo);
                    }
                }
                cl_map.put("name", sj.toString());
            }
            if (StringUtils.isNotBlank(orderInfo.getTravelreason())) {
                cl_map.put("reason", orderInfo.getTravelreason());
            }
            List<TravelDetailDto> clDetailList = new ArrayList<>();
            for (BudgetReimbursementorderTravel detail : result.getOrderTravel()) {
                TravelDetailDto dto = new TravelDetailDto();
                TabDm vehicleDm = this.dmService.getOne(new QueryWrapper<TabDm>().eq("dm_value", detail.getTravelvehicle().toString()).eq("dm_type", "vehicle"));
                dto.setGj(vehicleDm.getDmName());
                dto.setStart(detail.getTravelstart());
                dto.setEnd(detail.getTravelend());
                dto.setLocation(detail.getTravelorigin());
                dto.setMdd(detail.getTraveldest());
                dto.setCtf(detail.getLongtravelexp().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                BigDecimal travelday = detail.getTravelday();
                BigDecimal dailysubsidy = detail.getDailysubsidy();
                BigDecimal xj = travelday.multiply(dailysubsidy).multiply(travelerNum);
                dto.setSnf(detail.getCitytravelexp().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setZsf(detail.getHotelexpense().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setTs(travelday.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setBz(dailysubsidy.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setJe(xj.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setQt(detail.getOther().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setXj(detail.getTotal().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                clDetailList.add(dto);
            }
            if (!clDetailList.isEmpty()) {
                cl_map.put("detail", clDetailList);
                export_map.put(TravelSheetDto.class, cl_map);
            }
            List<EntertainDetailDto> zdDetailList = new ArrayList<>();
            for (BudgetReimbursementorderEntertain detail : result.getOrderEntertain()) {
                EntertainDetailDto dto = new EntertainDetailDto();
                dto.setDate(Constants.FORMAT2_10.format(detail.getDate()));
                dto.setCfrs(detail.getMealsrs());
                dto.setCfbz(detail.getMealsbz().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setCfje(detail.getMealstotal().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setZsrs(detail.getHotalrs());
                dto.setZsjs(detail.getHotaljs());
                dto.setZsbz(detail.getHotalbz().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setZsje(detail.getHotaltotal().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setOther(detail.getOther().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setXcfje(detail.getPublicityexp().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setCount(detail.getTotal().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                zdDetailList.add(dto);
            }
            if (!zdDetailList.isEmpty()) {
                zd_map.put("detail", zdDetailList);
                export_map.put(EntertainSheetDto.class, zd_map);
            }
            List<HbDetailDto> hbDetailList = new ArrayList<>();
            for (BudgetReimbursementorderAllocated detail : result.getOrderAllocated()) {
                HbDetailDto dto = new HbDetailDto();
                dto.setUnitName(detail.getUnitname());
                dto.setSubName(detail.getSubjectname());
                dto.setAgentName(detail.getMonthagentname());
                dto.setHbMoney(detail.getAllocatedmoney().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                dto.setRemark(detail.getRemark());
                hbDetailList.add(dto);
            }
            if (!hbDetailList.isEmpty()) {
                hb_map.put("detail", hbDetailList);
                export_map.put(HbSheetDto.class, hb_map);
            }

            export_map.put(BxInfoSheetDto.class, map);
            exportHelp.export(export_map, tmpFilePath);
            exportHelp.end();
            ResponseUtil.exportExcel(response, tmpFilePath, orderInfo.getReimcode() + "_报销信息.xls");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            File tmpFile = new File(tmpFilePath);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        }
    }

    public void exportStrick(Long id, HttpServletResponse response) throws Exception {
        BudgetReimbursementorder order = this.getById(id);
        if (order == null) {
            throw new Exception("无效的id");
        }
        //冲账信息
        List<BudgetReimbursementorderPayment> payment = this.paymentService.getByOrderId(id);
        //转账信息
        List<BudgetReimbursementorderTrans> trans = this.transService.getByOrderId(id);
        //现金信息
        List<BudgetReimbursementorderCash> cash = this.cashService.getByOrderId(id);
        //冲账表头
        List<List<String>> czHead = new ArrayList<>();
        List<String> czTitle1 = Lists.newArrayList("冲账信息", "姓名");
        List<String> czTitle2 = Lists.newArrayList("冲账信息", "借款金额");
        List<String> czTitle3 = Lists.newArrayList("冲账信息", "未还金额");
        List<String> czTitle4 = Lists.newArrayList("冲账信息", "借款说明");
        List<String> czTitle5 = Lists.newArrayList("冲账信息", "冲账金额");
        czHead.add(czTitle1);
        czHead.add(czTitle2);
        czHead.add(czTitle3);
        czHead.add(czTitle4);
        czHead.add(czTitle5);
        //转账表头
        List<List<String>> zzHead = new ArrayList<>();
        List<String> zzTitle1 = Lists.newArrayList("转账信息", "户名");
        List<String> zzTitle2 = Lists.newArrayList("转账信息", "账号");
        List<String> zzTitle3 = Lists.newArrayList("转账信息", "");
        List<String> zzTitle4 = Lists.newArrayList("转账信息", "开户行");
        List<String> zzTitle5 = Lists.newArrayList("转账信息", "转账金额");
        zzHead.add(zzTitle1);
        zzHead.add(zzTitle2);
        zzHead.add(zzTitle3);
        zzHead.add(zzTitle4);
        zzHead.add(zzTitle5);
        //现金表头
        List<List<String>> xjHead = new ArrayList<>();
        List<String> xjTitle1 = Lists.newArrayList("现金信息", "编号");
        List<String> xjTitle2 = Lists.newArrayList("现金信息", "收款人");
        List<String> xjTitle3 = Lists.newArrayList("现金信息", "金额");
        xjHead.add(xjTitle1);
        xjHead.add(xjTitle2);
        xjHead.add(xjTitle3);
        //创建table（一个sheet页可有多个table）
        WriteTable czTable = EasyExcel.writerTable(0).head(czHead).needHead(true).build();
        WriteTable zzTable = EasyExcel.writerTable(1).head(zzHead).needHead(true).build();
        WriteTable xjTable = EasyExcel.writerTable(2).head(xjHead).needHead(true).build();
        //设置列宽
        Map<Integer, Integer> columnWidthMap = new HashMap<>();
        columnWidthMap.put(0, 5000);
        columnWidthMap.put(1, 5000);
        columnWidthMap.put(2, 5000);
        columnWidthMap.put(3, 10000);
        columnWidthMap.put(4, 5000);
        WriteSheet sheet = EasyExcel.writerSheet("冲账支付信息").build();
        sheet.setAutoTrim(true);
        sheet.setAutomaticMergeHead(true);
        sheet.setColumnWidthMap(columnWidthMap);
        ExcelWriter excelWriter = EasyExcel.write(EasyExcelUtil.getOutputStream(order.getReimcode() + "_冲账支付表", response)).build();
        //冲账列表
        List<List<String>> czList = new ArrayList<>();
        for (BudgetReimbursementorderPayment czInfo : ListUtils.emptyIfNull(payment)) {
            List<String> czData = new ArrayList<String>();
            czData.add(czInfo.getLendmoneyname());
            czData.add(czInfo.getLendmoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            czData.add(czInfo.getUnrepaidmoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            czData.add(czInfo.getLendmoneyremark());
            czData.add(czInfo.getPaymentmoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            czList.add(czData);
        }
        czList.add(Lists.newArrayList("", "", "", "", ""));
        //转账列表
        List<List<String>> zzList = new ArrayList<>();
        for (BudgetReimbursementorderTrans zzInfo : ListUtils.emptyIfNull(trans)) {
            List<String> zzData = new ArrayList<String>();
            zzData.add(zzInfo.getPayeename());
            zzData.add(zzInfo.getPayeebankaccount());
            zzData.add("");
            zzData.add(zzInfo.getPayeebankname());
            zzData.add(zzInfo.getTransmoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            zzList.add(zzData);
        }
        zzList.add(Lists.newArrayList("", "", "", "", ""));
        //现金列表
        List<List<String>> xjList = new ArrayList<>();
        for (BudgetReimbursementorderCash xjInfo : ListUtils.emptyIfNull(cash)) {
            List<String> xjData = new ArrayList<String>();
            xjData.add(xjInfo.getPayeecode());
            xjData.add(xjInfo.getPayeename());
            xjData.add(xjInfo.getCashmoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            xjList.add(xjData);
        }
        xjList.add(Lists.newArrayList("", "", ""));

        excelWriter.write(czList, sheet, czTable);
        excelWriter.write(zzList, sheet, zzTable);
        excelWriter.write(xjList, sheet, xjTable);
        excelWriter.finish();
    }

    /**
     * 按照报销单主键删除
     * 删除budget_reimbursementorder
     * 删除budget_reimbursementorder_cash
     * 删除budget_reimbursementorder_detail
     * 删除budget_reimbursementorder_trans
     * 删除budget_reimbursementorder_payment
     * 删除budget_reimbursementorder_travel
     * 删除budget_reimbursementorder_entertain
     * 删除budget_reimbursementorder_allocated
     * 删除budget_reimbursementorder_scanlog
     * 删除budget_reimbursementorder_verifylog
     * 删除budget_reimburment_timedetail
     * 删除budget_lendmoney_uselog_new
     * 删除budget_reimcode
     * 删除budget_reimbursementorder_payment
     *
     * @param id
     * @return
     */
    public String delete(Long id) {
        String sql = "delete from budget_reimbursementorder_cash where reimbursementid = " + id;
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_reimbursementorder_detail where reimbursementid = " + id;
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_reimbursementorder_trans where reimbursementid = " + id;
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_reimbursementorder_payment where reimbursementid = " + id;
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_reimbursementorder_travel where reimbursementid = " + id;
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_reimbursementorder_entertain where reimbursementid = " + id;
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_reimbursementorder_allocated where reimbursementid = " + id;
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_reimbursementorder_scanlog where reimbursementid = " + id;
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_reimbursementorder_verifylog where reimbursementid = " + id;
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_reimburment_timedetail where reimcode in(select reimcode from budget_reimbursementorder where id = " + id + ")";
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_reimbursementorder_payment where reimbursementid = " + id;
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_reimcode where reimbursementid = " + id;
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_lendmoney_uselog_new where usetype = 5 and useobjectid = " + id;
        this.jdbcTemplateService.update(sql);
        sql = "delete from budget_reimbursementorder where id = " + id;
        this.jdbcTemplateService.update(sql);
        return null;
    }

    /**
     * 锁定借款单
     *
     * @param request
     */
    private int lockBorrow(ReimbursementRequest request) {
        String orderId = request.getOrder().getId().toString();
        try {
            List<BudgetReimbursementorderPayment> list = request.getOrderPayment();
            if (list != null && list.size() > 0) {
                List<BudgetLendmoneyUselog> puts = new ArrayList<BudgetLendmoneyUselog>();
                BudgetLendmoneyUselog _log = null;
                for (BudgetReimbursementorderPayment ele : list) {
                    _log = new BudgetLendmoneyUselog();
                    _log.setCreatetime(new Date());
                    _log.setLendmoneyid(ele.getLendmoneyid());
                    _log.setLockedmoney(ele.getPaymentmoney());
                    _log.setUseflag(true);
                    _log.setUsetype(5);
                    _log.setUsemark("报销转账");
                    _log.setUseobjectid(orderId);
                    puts.add(_log);
                }
                this.lendmoneyUselogService.saveBatch(puts);
                return puts.size();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 解锁借款单
     *
     * @param request
     */
    private boolean unLockBorrow(Long orderId) {
        QueryWrapper<BudgetLendmoneyUselog> wrapper = new QueryWrapper<BudgetLendmoneyUselog>();
        wrapper.eq("useobjectid", orderId);
        wrapper.eq("usetype", 5);
        return this.lendmoneyUselogService.remove(wrapper);
    }

    /**
     * 报销单是否存在
     *
     * @param order
     * @return
     */
    public boolean existById(BudgetReimbursementorder order) {
        QueryWrapper<BudgetReimbursementorder> wrapper = new QueryWrapper<BudgetReimbursementorder>();
        wrapper.eq("id", order.getId());
        return order.getId() == null ? false : (this.count(wrapper) > 0);
    }

    /**
     * 相似校验（届别+部门+报销金额+报销人+报销类型+报销日期）
     *
     * @param order
     * @return
     */
    public boolean duplicate(BudgetReimbursementorder order) {
        QueryWrapper<BudgetReimbursementorder> wrapper = new QueryWrapper<BudgetReimbursementorder>();
        wrapper.eq("yearid", order.getYearid());
        wrapper.eq("unitid", order.getUnitid());
        wrapper.eq("reimmoney", order.getReimmoney());
        wrapper.eq("applicantid", order.getApplicantid());
        wrapper.eq("bxtype", order.getBxtype());
        wrapper.eq("reimdate", order.getReimdate());
        return this.count(wrapper) > 0;
    }

    @Autowired
    private BudgetMonthEndUnitMapper monthEndUnitMapper;

    /**
     * 是否月结
     *
     * @param order
     * @param orderAllocated
     * @return
     */
    public void checkIsMonthEnd(BudgetReimbursementorder order, List<BudgetReimbursementorderAllocated> orderAllocated) {
        String sql = "SELECT ttt.unitid,ttt.monthid FROM budget_month_end_unit ttt WHERE ttt.unitid=? AND ttt.monthid=?";
        List<BudgetMonthEndUnit> list = this.jdbcTemplateService.query(sql, BudgetMonthEndUnit.class, new Object[]{order.getUnitid(), order.getMonthid()});
        if (!list.isEmpty()) {
            Optional.ofNullable(list.get(0).getMonthendflag()).ifPresent(e->{
                if(e)throw new RuntimeException("操作失败！报销单位【" + (StringUtils.isBlank(order.getUnitName()) ? this.unitMapper.selectById(order.getUnitid()).getName() : order.getUnitName()) + "】已经月结");

            });           
        }
        Optional.ofNullable(orderAllocated).ifPresent(e -> {
            if (!e.isEmpty()) {
                List<Long> allocatedUnitidList = e.stream().map(m -> m.getUnitid()).collect(Collectors.toList());
                String monthEndUnitNames = monthEndUnitMapper.selectList(new QueryWrapper<BudgetMonthEndUnit>().eq("monthid", order.getMonthid()).eq("monthendflag", 1).in("unitid", allocatedUnitidList)).stream().map(m -> {
                    return this.unitMapper.selectById(m.getUnitid()).getName();
                }).collect(Collectors.joining(","));
                if (StringUtils.isNotBlank(monthEndUnitNames))
                    throw new RuntimeException("操作失败！划拨单位【" + monthEndUnitNames + "】已经月结");
            }
        });
    }


    /**
     * 获取校验标准信息
     *
     * @param request
     * @param agentIds
     * @return type 1:年度动因控制 2：月度科目控制 3：年度科目控制
     * @throws Exception
     */
    public Map<String, Map<String, BigDecimal>> getValidateInfo(Long orderId, Long yearId, Long monthId, Long unitId, List<Long> agentIds, Integer type,Boolean isFixAsset,List<ReimbursementRequest.AssetSubjectMsg> assetLockedSubjectList) throws Exception {
        String ids = JdbcTemplateService.getInSql(agentIds, null);
        //获取动因可执行金额
        ReimbursementValidateMoney query = new ReimbursementValidateMoney(orderId, yearId, monthId, unitId, null, null);
        List<ReimbursementValidateMoney> list = null;
        if (type == 1) {
            list = this.mapper.getYearAgentValidateMoney(query, ids);
            Map<String, Map<String, BigDecimal>> result = new HashMap<>(list.size());

            Map<Long, BigDecimal> yearAgentTempMap = new HashMap<>();
            if(isFixAsset!=null && isFixAsset && !CollectionUtils.isEmpty(assetLockedSubjectList)){
                //增加固定资产报销校验
                yearAgentTempMap = assetLockedSubjectList.stream().collect(Collectors.groupingBy(e -> e.getYearAgentId(), Collectors.reducing(BigDecimal.ZERO, e -> e.getLockedMoney(), (e1, e2) -> e1.add(e2))));
            }
            Map<Long, BigDecimal> yearAgentMap = yearAgentTempMap;
            list.forEach(ele -> {
                Map<String, BigDecimal> map = new HashMap<>();
                map.put("execMoney", ele.execMoney());
                map.put("lockedMoney", ele.getSdmoney().add(ele.getHbsdmoney()));
                map.put("assetLockedMoney",yearAgentMap.get(ele.getAgentId()));
                result.put(ele.getAgentId().toString(), map);
            });
            return result;
        }
        if (type == 2) {
            list = this.mapper.getMonthCourseValidateMoney(query, ids);
            Map<String, Map<String, BigDecimal>> result = new HashMap<String, Map<String, BigDecimal>>(list.size());

            Map<String, BigDecimal> monthSubjectTempMap = new HashMap<>();
            if(isFixAsset!=null && isFixAsset && !CollectionUtils.isEmpty(assetLockedSubjectList)){
                //增加固定资产报销校验
                monthSubjectTempMap = assetLockedSubjectList.stream().filter(e->e.getMonthId().equals(monthId))
                        .collect(Collectors.groupingBy(e->e.getUnitId()+"-"+e.getSubjectId(),
                                  Collectors.reducing(BigDecimal.ZERO, ReimbursementRequest.AssetSubjectMsg::getLockedMoney, BigDecimal::add)));
            }
            Map<String, BigDecimal> monthSubjectMap = monthSubjectTempMap;

            list.forEach(ele -> {
                Map<String, BigDecimal> map = new HashMap<>();
                map.put("execMoney", ele.execMoney());
                map.put("lockedMoney", ele.getSdmoney().add(ele.getHbsdmoney()));
                map.put("assetLockedMoney",monthSubjectMap.get(ele.getUnitId() + "-" + ele.getSubjectId()));
                result.put(ele.getUnitId() + "-" + ele.getSubjectId(), map);
            });
            return result;
        }
        if (type == 3) {
            list = this.mapper.getYearCourseValidateMoney(query, ids);
            Map<String, Map<String, BigDecimal>> result = new HashMap<>(list.size());

            Map<String, BigDecimal> yearSubjectTempMap = new HashMap<>();
            if(isFixAsset!=null && isFixAsset && !CollectionUtils.isEmpty(assetLockedSubjectList)){
                //增加固定资产报销校验
                 yearSubjectTempMap = assetLockedSubjectList.stream()
                        .collect(Collectors.groupingBy(e->e.getUnitId()+"-"+e.getSubjectId(),
                                Collectors.reducing(BigDecimal.ZERO, ReimbursementRequest.AssetSubjectMsg::getLockedMoney, BigDecimal::add)));
            }
            Map<String, BigDecimal> yearSubjectMap =  yearSubjectTempMap;

            list.forEach(ele -> {
                Map<String, BigDecimal> map = new HashMap<>();
                map.put("execMoney", ele.execMoney());
                map.put("lockedMoney", ele.getSdmoney().add(ele.getHbsdmoney()));
                map.put("assetLockedMoney",yearSubjectMap.get(ele.getUnitId() + "-" + ele.getSubjectId()));
                result.put(ele.getUnitId() + "-" + ele.getSubjectId(), map);
            });
            return result;
        }
        throw new Exception("获取动因校验标准失败，数据错乱......");
    }

    /**
     * 查询详情开票单位下的付款账户主键
     *
     * @param orderDetail
     * @return
     */
    public Set<Long> getPayAccountFromDetail(List<BudgetReimbursementorderDetail> orderDetail) {
        Set<Long> kpdw = orderDetail.stream().map(BudgetReimbursementorderDetail::getBunitid).collect(Collectors.toSet());
        Set<BudgetBillingUnitAccount> info = this.billingUnitAccountService.getByUnitIds(kpdw);
        if (info.isEmpty()) {
            return null;
        }
        return info.stream().map(BudgetBillingUnitAccount::getId).collect(Collectors.toSet());
    }


    /**
     * 获取受指定条件限制的月度动因主键
     *
     * @param monthAgentIds
     * @param columnName
     * @return
     * @throws Exception
     */
    public List<Long> getControlAgentId(List<Long> monthAgentIds, String columnName) throws Exception {
        return this.monthAgentService.getControlAgentId(monthAgentIds, columnName);
    }

    /**
     * 报销单开票单位
     *
     * @param order
     * @return
     */
    public List<BudgetBillingUnit> orderBillingUnit(BudgetReimbursementorder order) {
        Long orderId = order.getId();
        List<BudgetReimbursementorderDetail> details = this.detailService.getByOrderId(orderId);
        List<Long> ticketUnitIds = details.stream().map(BudgetReimbursementorderDetail::getBunitid).collect(Collectors.toList());
        QueryWrapper<BudgetBillingUnit> wrapper = new QueryWrapper<BudgetBillingUnit>();
        wrapper.in("id", ticketUnitIds);
        wrapper.eq("billingunittype", "1");
        wrapper.eq("stopflag", 0);
        List<BudgetBillingUnit> list = this.billingUnitService.list(wrapper);
        return list;
    }

    /**
     * 报销单开票单位是否有票
     *
     * @param order
     * @return
     */
    public boolean hasTicket(BudgetReimbursementorder order) {
        List<BudgetBillingUnit> list = this.orderBillingUnit(order);
        return (list == null || list.size() == 0) ? false : true;
    }

    /**
     * 更新报销单，创建扫描模式的日志
     *
     * @param codeRequest
     * @param order
     * @param step
     * @param opt
     */
    public void updateAndSaveScanLog(BxCodeRequest codeRequest, BudgetReimbursementorder order, String step, String opt) {
        this.saveOrUpdate(order);
        this.scanLogService.saveByScanOrder(codeRequest, order, step, opt);
    }

    /**
     * 审核通过报销单（预算审核、报销审核中使用）
     *
     * @param orderId
     * @param transList
     * @param billingUnitIdList
     * @param string
     * @throws Exception
     */
    public String checkPass(Long orderId, String step, List<BudgetReimbursementorderDetail> detailList, List<BudgetReimbursementorderTrans> transList,List<Long> deletedTranIdList,boolean isBxVerify) throws Exception {
        BudgetReimbursementorder order = this.getById(orderId);
        //当前环节是否已经审核
        boolean hasChecked = ReimbursementStepHelper.getStepFieldValue(order, step, false);
        if (hasChecked) {
            return "当前扫描报销单【" + order.getReimcode() + "】，已经完成【" + ReimbursementStepHelper.getName(step) + "】！";
        }
        String errMsg = BudgetReimbursementorderDetail.validate(detailList);
        if (StringUtils.isNotEmpty(errMsg)) {
            return errMsg;
        }

        /**
         * add by minzhq
         * 增加报销审核界面的判断
         */
        if(isBxVerify){
            if((order.getFinancialmanagereceivestatus()!=null && order.getFinancialmanagereceivestatus()) ||
                    (order.getGeneralmanagereceivestatus()!=null && order.getGeneralmanagereceivestatus()) ||
                    (order.getFdstatus()!=null && order.getFdstatus()) || order.getReuqeststatus() == 2) return "该报销单已经经过了后续环节！";
            if (ReimbursementStepHelper.BILL_RECEIVE.equals(step)) {
                if(order.getParverifyreceivestatus()!=null && order.getParverifyreceivestatus()) return "该报销单已经完成单据接收！";
                order.setCurscanstatus(1);
                order.setCurscanstatusname("单据接收");
                order.setReceivestatus(2);
            }else if (ReimbursementStepHelper.BUDGET_CHECK.equals(step)) {
                if(order.getParverifyreceivestatus()==null || !order.getParverifyreceivestatus() || order.getParverifystatus()==null || !order.getParverifystatus()) return "该报销单还未完成单据接收！";
                if(order.getBudgetverifystatus() !=null && order.getBudgetverifystatus()) return "该报销单已经完成预算审核！";
                order.setCurscanstatus(2);
                order.setCurscanstatusname("预算审核");
                order.setReceivestatus(2);
            }
        }

        /**
         * 获取新增的转账单
         */
        List<BudgetReimbursementorderTrans> addTranList = transList.stream().filter(e -> e.getId() == null).collect(Collectors.toList());
        BigDecimal addTranMoney = addTranList.stream().map(BudgetReimbursementorderTrans::getTransmoney).reduce(BigDecimal.ZERO, BigDecimal::add);

        /**
         * 获取修改的转账单
         */
        List<BudgetReimbursementorderTrans> updateTranList = transList.stream().filter(e -> e.getId() != null).collect(Collectors.toList());
        BigDecimal updateTransMoney = updateTranList.stream().map(BudgetReimbursementorderTrans::getTransmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Long> updateTransIdList = updateTranList.stream().map(e -> e.getId()).collect(Collectors.toList());

        List<BudgetReimbursementorderTrans> budgetReimbursementorderTrans = this.transService.list(new QueryWrapper<BudgetReimbursementorderTrans>().eq("reimbursementid", order.getId()));
        //获取除修改和删除的其它转账金额（稿费报销单会存在这种情况（多数据量导致））
        BigDecimal dbMoney = budgetReimbursementorderTrans.stream().filter(e -> !updateTransIdList.contains(e.getId()) && !deletedTranIdList.contains(e.getId())).map(BudgetReimbursementorderTrans::getTransmoney).reduce(BigDecimal.ZERO, BigDecimal::add);

        //查询所有开票单位下的所有付款账户主键
        Set<Long> tranPayAccountIds = this.getPayAccountFromDetail(detailList);
        errMsg = BudgetReimbursementorderTrans.validate(transList, tranPayAccountIds, order.getTransmoney(),addTranMoney.add(dbMoney).add(updateTransMoney));
        if (StringUtils.isNotEmpty(errMsg)) {
            return errMsg;
        }

        Date nowDate = new Date();
        //创建审核日志
        BudgetReimbursementorderVerifylog log = new BudgetReimbursementorderVerifylog();
        log.setBstatus(order.getReuqeststatus());
        log.setVerifytime(nowDate);
        log.setReimbursementid(order.getId());
        log.setReimcode(order.getReimcode());
        log.setVerifyflag(true);
        log.setVerifyername(UserThreadLocal.get().getDisplayName());
        log.setVerifyer(UserThreadLocal.get().getUserName());
        log.setVerifyinfo(ReimbursementStepHelper.getName(step));
        log.setVerifytype(Integer.parseInt(step));

        if (!StatusConstants.BX_SUBMIT.equals(order.getReuqeststatus())) {
            order.setReuqeststatus(StatusConstants.BX_SUBMIT);
        }
        ReimbursementStepHelper.setReceivedStatusTrue(order, step);
        ReimbursementStepHelper.setCheckedStatusTrue(order, step);
        log.setAstatus(order.getReuqeststatus());
        if (Integer.valueOf(step) < 5) {//出纳付款前
            //修改报销明细（开票单位可变）
            this.detailService.saveByOrder(detailList, order);
        }
        if (ReimbursementStepHelper.BILL_RECEIVE.equals(step)) {
            this.timeDetailService.createBudgetReimbursentTimeDetail(order.getSubmittime(), nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), 1);//单据提交新增
            this.timeDetailService.createBudgetReimbursentTimeDetail(nowDate, nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), 2);//票面接收新增
            this.timeDetailService.createBudgetReimbursentTimeDetail(nowDate, null, order.getReimcode(), UserThreadLocal.get().getUserName(), 3);//票面审核接收新增
        }
        if (ReimbursementStepHelper.BUDGET_CHECK.equals(step)) {
            if (!order.getParverifystatus()) {
                //未完成票面审核
                ReimbursementStepHelper.setReceivedStatusTrue(order, ReimbursementStepHelper.BILL_RECEIVE);
                ReimbursementStepHelper.setCheckedStatusTrue(order, ReimbursementStepHelper.BILL_RECEIVE);
            }
            BudgetReimburmentTimedetail firstDetail = this.timeDetailService.getOne(new QueryWrapper<BudgetReimburmentTimedetail>().eq("reimcode", order.getReimcode()).eq("type", 1).eq("iseffective", 1));
            if (null == firstDetail) {
                //报销审核中不扫码直接预算审核
                this.timeDetailService.createBudgetReimbursentTimeDetail(order.getSubmittime(), nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), 1);//单据提交新增
                this.timeDetailService.createBudgetReimbursentTimeDetail(nowDate, nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), 2);//票面接收新增
                this.timeDetailService.createBudgetReimbursentTimeDetail(nowDate, nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), 3);//票面审核接收新增
                this.timeDetailService.createBudgetReimbursentTimeDetail(nowDate, null, order.getReimcode(), UserThreadLocal.get().getUserName(), 4);//预算审核新增
            } else {
                //预算审核
                this.timeDetailService.createBudgetReimbursentTimeDetail(null, nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), 3);//预算审核修改
                this.timeDetailService.createBudgetReimbursentTimeDetail(nowDate, null, order.getReimcode(), UserThreadLocal.get().getUserName(), 4);//预算审核新增
            }
        }
        //修改转账
        if(!updateTranList.isEmpty()) {
            updateTranList.forEach(e->e.setReimbursementid(order.getId()));
            this.transService.updateBatchById(updateTranList);
        }
        if(!addTranList.isEmpty()) {
            addTranList.forEach(e->e.setReimbursementid(order.getId()));
            this.transService.saveBatch(addTranList);
        }
        if(!deletedTranIdList.isEmpty()) this.transService.removeByIds(deletedTranIdList);

        this.saveOrUpdate(order);
        this.verifylogService.saveOrUpdate(log);
        return null;
    }

    /**
     * 退回:
     * 1.报销单已经审核通过，不能退回
     * 2.报销单已经退回，不用退回
     * 3.非普通保险单不能退回
     * 4.已经退回纸质校验
     * 5.已经全部退回校验
     * 6.全部退回更新报销单审核状态为退回：流程重头开始，即初始化流程
     * 7.全部退回释放冲账
     *
     * @param orderId
     * @param step
     * @param backType
     * @param remark
     * @return
     * @throws Exception
     */
    public String back(Long orderId, String step, String backType, String remark,String type) throws Exception {
        BudgetReimbursementorder order = this.getById(orderId);
        if (StatusConstants.BX_PASS == order.getReuqeststatus()) {
            return "报销单已经审核通过，不能退回";
        }
        if (StatusConstants.BX_BACK == order.getReuqeststatus()) {
            return "报销单已经退回，不用退回";
        }
        if (0 != order.getOrderscrtype()) {
            if("2".equals(backType)) return "该报销单不能全部退回";
        }
        Integer bsStatus = order.getReuqeststatus();
        String reason = "";
        if ("1".equals(backType)) {
            if (StatusConstants.BX_BACK_PAPER == order.getReceivestatus()) {
                return "报销单已经退回纸质";
            }
            order.setReceivestatus(StatusConstants.BX_BACK_PAPER);
            reason = "退回纸质";
        } else if ("2".equals(backType)) {
            if (StatusConstants.BX_BACK_ALL == order.getReceivestatus()) {
                return "报销单已经全部退回";
            }
            order.setReceivestatus(StatusConstants.BX_BACK_ALL);
            //设置报销单退回状态
            order.setReuqeststatus(StatusConstants.BX_BACK);
            reason = "全部退回";
        }
        //创建审核日志
        String info = "1".equals(backType) ? "退回纸质：" : "全部退回：";
        BudgetReimbursementorderVerifylog log = new BudgetReimbursementorderVerifylog();
        log.setBstatus(bsStatus);
        log.setAstatus(order.getReuqeststatus());
        log.setVerifytime(new Date());
        log.setReimbursementid(order.getId());
        log.setReimcode(order.getReimcode());
        log.setVerifyflag(false);
        log.setVerifyername(UserThreadLocal.get().getDisplayName());
        log.setVerifyer(UserThreadLocal.get().getUserName());
        log.setVerifyinfo(info + remark);
        if (StringUtils.isBlank(step)) {
            log.setVerifytype(order.getCurscanstatus());
            step = order.getCurscanstatus().toString();
        } else {
            log.setVerifytype(Integer.parseInt(step));
            order.setCurscanstatusname(ReimbursementStepHelper.getName(step));
        }

        log.setBacktype(Integer.parseInt(backType));
        order.setCurscanstatus(order.getReceivestatus());
        order.setCurscanstatusname("1".equals(backType)? "退回纸质" : "全部退回");
        order.setCurscanername(UserThreadLocal.get().getDisplayName());
        order.setCurscaner(UserThreadLocal.get().getUserName());
        this.verifylogService.save(log);
        //全部退回
        if ("2".equals(backType)) {
            //冲账单解锁
            this.unLockBorrow(order.getId());
            //初始化流程
            ReimbursementStepHelper.init(order, ReimbursementStepHelper.afterSteps(order, ReimbursementStepHelper.get(step), true));
        }
        order.setUpdatetime(new Date());
        order.setBackType(type);
        this.updateById(order);
        try {
            //通过预算单位获取预算人
            if("其他".equals(type)){
                type = StringUtils.isNotBlank(remark)?remark:type;
            }
            sendMessageByManId(orderId, type, order);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return null;
    }

    private void sendMessageByManId(Long orderId, String type, BudgetReimbursementorder order) throws MyException {
        BudgetUnit unit = this.unitService.getById(order.getUnitid());
        String manager =  unit.getManagers();
        if(StringUtils.isNotBlank(manager)){
            String userId = manager.split(",")[0];
            WbUser applyUser = userService.getById(userId);
            if(Objects.isNull(applyUser)){
                throw new MyException("通过编号"+userId+"获取用户失败");
            }
            StringBuilder builder = new StringBuilder();
            String date = DateUtil.format(order.getReimdate(),"yyyy年MM月dd日");
            List<BudgetReimbursementorderDetail> details = this.detailService.getByOrderId(orderId);
            String subjects = details.stream().map(BudgetReimbursementorderDetail::getSubjectname).collect(Collectors.joining(","));
            BigDecimal bxMoney = order.getReimmoney()==null?new BigDecimal(0):order.getReimmoney().setScale(2, BigDecimal.ROUND_HALF_UP);
            builder.append("您于").append(date).append("申请的报销被退回\n").append("流水单号：").append(order.getReimcode()).append("\n")
                    .append("报销科目：").append(subjects).append("\n报销金额：").append(bxMoney).append("\n备注：").append(type);
            sender.sendQywxMsgSyn(new QywxTextMsg(applyUser.getUserName(), null, null, 0, builder.toString(), null));

        }
    }

    /**
     * 报销分单
     *
     * @param orderId
     * @param bunitid
     * @return
     * @throws Exception
     */
    public String splitOrder(Long orderId, long bunitid, String step) throws Exception {
        BudgetReimbursementorder order = this.getById(orderId);
        //是否已经完成分单
        boolean flag = ReimbursementStepHelper.getStepFieldValue(order, step, false);
        if (flag) {
            return "报销单【" + order.getReimcode() + "】已经完成分单扫描；";
        }
        Date nowDate = new Date();
        //创建审核日志
        BudgetReimbursementorderVerifylog log = new BudgetReimbursementorderVerifylog();
        log.setBstatus(order.getReuqeststatus());
        log.setVerifytime(nowDate);
        log.setReimbursementid(order.getId());
        log.setReimcode(order.getReimcode());
        log.setVerifyflag(true);
        log.setVerifyername(UserThreadLocal.get().getDisplayName());
        log.setVerifyer(UserThreadLocal.get().getUserName());
        log.setVerifyinfo(ReimbursementStepHelper.getName(step));
        log.setVerifytype(Integer.parseInt(step));
        log.setAstatus(StatusConstants.BX_PASS);

        ReimbursementStepHelper.setCheckedStatusTrue(order, step);
        order.setReuqeststatus(StatusConstants.BX_PASS);
        order.setUpdatetime(nowDate);
        order.setVerifytime(nowDate);
        this.verifylogService.save(log);


        if (!this.hasTicket(order)) {
            //全部为无票单位:生成付款单，修改状态为已做账（无需分单确认）
            this.paymoneyService.savePayMoneyByReimbursementOrder(order);
            order.setAccountstatus(Boolean.TRUE);
            order.setAccounter(UserThreadLocal.get().getUserName());
            order.setAccounttime(Constants.FULL_FORMAT.format(new Date()));
        } else {
            //需要做账的报销明细
            List<BudgetReimbursementorderDetail> details = this.detailService.getDoBillDetail(order.getId());
            //创建分单任务
            this.fdTaskService.createSplitOrderTaskByOrder(order, details, bunitid);
        }
        this.timeDetailService.createBudgetReimbursentTimeDetail(null, nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), 4);//预算审核修改
        this.timeDetailService.createBudgetReimbursentTimeDetail(nowDate, null, order.getReimcode(), UserThreadLocal.get().getUserName(), 5);//分单扫描新增
        //同步执行数
        this.syncagnentexecute(order);
        //更新报销单
        this.updateById(order);
        return null;
    }

    /**
     * 同步报销明细计入执行的执行数
     * 1.获取计入执行的报销明细
     * 2.设置报销单处理状态handleflag为true
     * 3.更新月度动因执行数
     * 4.更新借款
     * 5.复制现金和转账 ？
     *
     * @param order
     * @throws Exception
     */
    private void syncagnentexecute(BudgetReimbursementorder order) throws Exception {
        ZookeeperShareLock lock = new ZookeeperShareLock(this.client, "/finance-platform/syncagnentexecute/" + order.getId(), null);
        try {
            this.updateMonthAgentExecMoney(order);
            this.updateLendMoney(order);
            this.copyCashAndPayment(order);
        } catch (Exception e) {
            // 解决异常: Transaction rolled back because it has been marked as rollback-only
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//		    e.printStackTrace();
            throw e;
        } finally {
            lock.unLock();
        }
    }

    /**
     * 复制现金和转账
     *
     * @param order
     */
    private void copyCashAndPayment(BudgetReimbursementorder order) {
        List<BudgetReimbursementorderCash> cashs = this.cashService.getByOrderId(order.getId());
        if (cashs != null && cashs.size() > 0) {
            BudgetReimbursementorderCashCopy cashcopy = null;
            List<BudgetReimbursementorderCashCopy> list = new ArrayList<BudgetReimbursementorderCashCopy>();
            for (BudgetReimbursementorderCash bean : cashs) {
                cashcopy = JSON.parseObject(JSON.toJSONString(bean), BudgetReimbursementorderCashCopy.class);
                cashcopy.setCashid(bean.getId());
                cashcopy.setCreatetime(new Date());
                list.add(cashcopy);
            }
            this.cashCopyService.saveBatch(list);
        }
        List<BudgetReimbursementorderTrans> trans = this.transService.getByOrderId(order.getId());
        if (trans != null && trans.size() > 0) {
            BudgetReimbursementorderTransCopy copy = null;
            List<BudgetReimbursementorderTransCopy> list = new ArrayList<BudgetReimbursementorderTransCopy>();
            for (BudgetReimbursementorderTrans bean : trans) {
                copy = JSON.parseObject(JSON.toJSONString(bean), BudgetReimbursementorderTransCopy.class);
                copy.setTransid(bean.getId());
                copy.setCreatetime(new Date());
                list.add(copy);
            }
            this.transCopyService.saveBatch(list);
        }
    }

    /**
     * 更新借款
     * 0.校验借款单（借款总额-还款总额）> 冲账金额
     * 1.为每一笔冲账记录生成一笔还款单和还款详情
     *
     * @param order
     * @throws Exception
     */
    private void updateLendMoney(BudgetReimbursementorder order) throws Exception {
        List<BudgetReimbursementorderPayment> payments = this.paymentService.getByOrderId(order.getId());
        if (payments == null || payments.size() == 0) {
            return;
        }
        Map<String, BudgetLendmoney> _map = new HashMap<String, BudgetLendmoney>();
        Set<String> codes = new HashSet<String>();
        payments.forEach(ele -> {
            codes.add(ele.getLendcode());
        });
        //查询借款单
        List<BudgetLendmoney> lendList = this.lendmoneyService.getByCodes(codes);
        if (lendList == null || lendList.size() == 0) {
            throw new Exception("借款单不存在");
        }
        lendList.forEach(ele -> {
            _map.put(ele.getLendmoneycode(), ele);
        });
        BudgetLendmoney lendmoney = null;
        for (BudgetReimbursementorderPayment payment : payments) {
            lendmoney = _map.get(payment.getLendcode());
            if (lendmoney == null) {
                throw new Exception("借款单【" + payment.getLendcode() + "】不存在");
            }
            this.repaymoneyService.payBack(lendmoney, payment.getId().toString(), payment.getPaymentmoney(), payment.getUnrepaidmoney(), order.getReimperonsid());
        }
    }

    /**
     * 更新月度动因执行数（包含划拨）
     * 1.根据报销单计入执行的详情月度动因主键查询月度动因，更新其已执行金额为原来的金额+本次报销的金额
     * 2.根据上述月度动因查询年度动因，更新其已执行金额为原来的金额+本次报销的金额
     * 3.更新年度和月度预算科目（需要递归更新上级：不想上汇总，递归结束）
     * 3.1 根据界别 + 预算单位 + 月（年）度 + 预算科目查询，月（年）度科目
     * 3.2 持久化BudgetMonthSubjectHis（BudgetYearSubjectHis）
     * 3.3 更新本身
     *
     * @param order
     */
    @SuppressWarnings("null")
    private void updateMonthAgentExecMoney(BudgetReimbursementorder order) {
        //同步数据
        try {
            this.jdbcTemplateService.queryForObject("SELECT COUNT(0) FROM budget_reimbursementorder WHERE handleflag != 1 AND id = " + order.getId(), Long.class);
        } catch (Exception e) {
            return;
        }
        order.setHandleflag(true);
        //获取报销单预算单位、界别和月份
        Long unitid = order.getUnitid();
        Long yearid = order.getYearid();
        Long monthid = order.getMonthid();
        //报销详情
        List<BudgetReimbursementorderDetail> details = this.detailService.getByOrderId(order.getId());
        BudgetUnit unit = this.unitService.getById(unitid);
        String[] parenIds = null;
        if (0 != unit.getParentid()) {
            parenIds = unit.getPids().split("-");
        }
        BudgetMonthAgent monthAgent = null;
        BudgetYearAgent yearAgent = null;
        List<BudgetMonthAgent> monthAgents = new ArrayList<BudgetMonthAgent>();
        List<BudgetYearAgent> yearAgents = new ArrayList<BudgetYearAgent>();
        Map<Long, BudgetMonthAgent> monthAgentMap = new HashMap<Long, BudgetMonthAgent>();
        Map<Long, BudgetYearAgent> yearAgentMap = new HashMap<Long, BudgetYearAgent>();
        for (BudgetReimbursementorderDetail detail : details.stream().filter(e -> e.getReimflag() != null && e.getReimflag()).collect(Collectors.toList())) {
            monthAgent = monthAgentMap.get(detail.getMonthagentid());
            if (monthAgent == null) {
                monthAgent = this.monthAgentService.getById(detail.getMonthagentid());
                monthAgentMap.put(monthAgent.getId(), monthAgent);
                monthAgents.add(monthAgent);
            }
            monthAgent.setExecutemoney(monthAgent.getExecutemoney().add(detail.getReimmoney()));

            yearAgent = yearAgentMap.get(monthAgent.getYearagentid());
            if (yearAgent == null) {
                yearAgent = this.yearAgentService.getById(monthAgent.getYearagentid());
                yearAgentMap.put(yearAgent.getId(), yearAgent);
                yearAgents.add(yearAgent);
            }
            yearAgent.setExecutemoney(yearAgent.getExecutemoney().add(detail.getReimmoney()));

            //同步本级预算单位预算科目
            this.subjectService.syncBudgetSubjectExecuteMoney(yearid, unitid, monthid, detail.getSubjectid(), 2, detail.getReimmoney());
            //同步上级预算单位预算科目
            if (parenIds != null && parenIds.length > 0) {
                for (String parentId : parenIds) {
                    if (Long.parseLong(parentId) != unitid) {
                        this.subjectService.syncBudgetSubjectExecuteMoney(yearid, Long.parseLong(parentId), monthid, detail.getSubjectid(), 2, detail.getReimmoney());
                    }
                }
            }
        }
        //划拨
        List<BudgetReimbursementorderAllocated> allcatedList = this.allocatedService.getByOrderId(order.getId());
        if (allcatedList != null && allcatedList.size() > 0) {
            Long allocatedUnitId = null;
            BudgetUnit allocatedUnit = null;
            String[] _parenIds = null;
            for (BudgetReimbursementorderAllocated bean : allcatedList) {
                monthAgent = monthAgentMap.get(bean.getMonthagentid());
                if (monthAgent == null) {
                    monthAgent = this.monthAgentService.getById(bean.getMonthagentid());
                    monthAgentMap.put(monthAgent.getId(), monthAgent);
                    monthAgents.add(monthAgent);
                }
                monthAgent.setExecutemoney(monthAgent.getExecutemoney().add(bean.getAllocatedmoney()));

                yearAgent = yearAgentMap.get(monthAgent.getYearagentid());
                if (yearAgent == null) {
                    yearAgent = this.yearAgentService.getById(monthAgent.getYearagentid());
                    yearAgentMap.put(yearAgent.getId(), yearAgent);
                    yearAgents.add(yearAgent);
                }
                yearAgent.setExecutemoney(yearAgent.getExecutemoney().add(bean.getAllocatedmoney()));

                allocatedUnitId = bean.getUnitid();
                //同步本级预算单位预算科目
                this.subjectService.syncBudgetSubjectExecuteMoney(yearid, allocatedUnitId, monthid, bean.getSubjectid(), 2, bean.getAllocatedmoney());
                allocatedUnit = this.unitService.getById(allocatedUnitId);
                if (0 != allocatedUnit.getParentid()) {
                    _parenIds = allocatedUnit.getPids().split("-");
                    //同步上级预算单位预算科目
                    if (_parenIds != null && _parenIds.length > 0) {
                        for (String parentId : _parenIds) {
                            if (Long.parseLong(parentId) != allocatedUnitId) {
                                this.subjectService.syncBudgetSubjectExecuteMoney(yearid, Long.parseLong(parentId), monthid, bean.getSubjectid(), 2, bean.getAllocatedmoney());
                            }
                        }
                    }
                }
            }
        }
        //同步动因
        this.monthAgentService.updateBatchById(monthAgents);
        this.yearAgentService.updateBatchById(yearAgents);
    }

    /**
     * 完成做账（分单确认后调用）
     *
     * @param checkPassRequest
     * @return
     * @throws Exception
     */
    public String doAccount(CheckPassRequest checkPassRequest) throws Exception {
        Long orderId = checkPassRequest.getOrderId();
        BudgetReimbursementorder order = this.getById(orderId);
        if (order == null) {
            return "报销单不存在！";
        }
        if (StatusConstants.BX_PASS != order.getReuqeststatus()) {
            return "完成做账失败，该报销单【" + order.getReimcode() + "】还未审核，不能完成做账！";
        }
        if ((order.getTransmoney().compareTo(BigDecimal.ZERO) == 1 || order.getCashmoney().compareTo(BigDecimal.ZERO) == 1) && true != order.getFdstatus()) {
            return "完成做账失败，该报销单【" + order.getReimcode() + "】还未分单，不能完成做账！";
        }
        if (true == order.getAccountstatus()) {
            return "完成做账失败，该报销单【" + order.getReimcode() + "】已完成做账！";
        }
        //获取当前报销单，当前会计的做账任务
        List<BudgetReimbursementorderFdtaskDetail> taskDetails = this.fdTaskService.getTaskDetailByOrderIdAndUserEmpno(orderId, UserThreadLocal.get().getUserName());
        if (taskDetails == null || taskDetails.size() == 0) {
            return "没有获取到您的做账任务！";
        }
        String errMsg = BudgetReimbursementorderDetail.validate(checkPassRequest.getOrderDetail());
        if (StringUtils.isNotEmpty(errMsg)) {
            return errMsg;
        }
        //查询所有开票单位下的所有付款账户主键
        Set<Long> tranPayAccountIds = this.getPayAccountFromDetail(checkPassRequest.getOrderDetail());
        List<BudgetReimbursementorderTrans> transList = checkPassRequest.getOrderTrans();
        List<Long> deletedTranIdList = checkPassRequest.getDeletedTranList();
        /**
         * 获取新增的转账单
         */
        List<BudgetReimbursementorderTrans> addTranList = transList.stream().filter(e -> e.getId() == null).collect(Collectors.toList());
        BigDecimal addTranMoney = addTranList.stream().map(BudgetReimbursementorderTrans::getTransmoney).reduce(BigDecimal.ZERO, BigDecimal::add);

        /**
         * 获取修改的转账单
         */
        List<BudgetReimbursementorderTrans> updateTranList = transList.stream().filter(e -> e.getId() != null).collect(Collectors.toList());
        BigDecimal updateTransMoney = updateTranList.stream().map(BudgetReimbursementorderTrans::getTransmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Long> updateTransIdList = updateTranList.stream().map(e -> e.getId()).collect(Collectors.toList());

        List<BudgetReimbursementorderTrans> budgetReimbursementorderTrans = this.transService.list(new QueryWrapper<BudgetReimbursementorderTrans>().eq("reimbursementid", order.getId()));
        //获取除修改和删除的其它转账金额（稿费报销单会存在这种情况（多数据量导致））
        BigDecimal dbMoney = budgetReimbursementorderTrans.stream().filter(e -> !updateTransIdList.contains(e.getId()) && !deletedTranIdList.contains(e.getId())).map(BudgetReimbursementorderTrans::getTransmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        errMsg = BudgetReimbursementorderTrans.validate(checkPassRequest.getOrderTrans(), tranPayAccountIds, order.getTransmoney(),addTranMoney.add(dbMoney).add(updateTransMoney));
        if (StringUtils.isNotEmpty(errMsg)) {
            return errMsg;
        }
//        BigDecimal totalTransMoney = new BigDecimal(0);
//        for (BudgetReimbursementorderTrans trans : budgetReimbursementorderTrans) {
//            totalTransMoney = totalTransMoney.add(trans.getTransmoney());
//        }
//        if (order.getTransmoney().compareTo(totalTransMoney) != 0) {
//            return "转账明细总金额不等于原转账总金额";
//        }
        Date nowDate = new Date();
        for (BudgetReimbursementorderFdtaskDetail taskdetail : taskDetails) {
            taskdetail.setReceiver(StringUtils.isBlank(taskdetail.getReceiver()) ? UserThreadLocal.get().getUserName() : taskdetail.getReceiver());
            taskdetail.setReceivername(StringUtils.isBlank(taskdetail.getReceivername()) ? UserThreadLocal.get().getDisplayName() : taskdetail.getReceivername());
            taskdetail.setReceivetime(taskdetail.getReceivetime() == null ? nowDate : taskdetail.getReceivetime());
            taskdetail.setAccountstatus(true);
            taskdetail.setAccounttime(nowDate);
            taskdetail.setAccounter(UserThreadLocal.get().getUserName());
            taskdetail.setAccountername(UserThreadLocal.get().getDisplayName());
        }
        this.fdTaskService.updateTaskDetails(taskDetails);
        //修改报销明细（开票单位可变）
        //this.detailService.saveByOrder(checkPassRequest.getOrderDetail(), order);
        //修改转账
        if(!updateTranList.isEmpty()) {
            updateTranList.forEach(e->e.setReimbursementid(order.getId()));
            this.transService.updateBatchById(updateTranList);
        }
        if(!addTranList.isEmpty()) {
            addTranList.forEach(e->e.setReimbursementid(order.getId()));
            this.transService.saveBatch(addTranList);
        }
        if(!deletedTranIdList.isEmpty()) this.transService.removeByIds(deletedTranIdList);
        //this.transService.saveByOrder(checkPassRequest.getOrderTrans(), order);
        //是否完成所有分单任务
        boolean flag = this.fdTaskService.isFinishAllTask(orderId);
        if (flag) {
            //所有分单任务完成后更新分单确认状态
            ReimbursementStepHelper.setCheckedStatusTrue(order, ReimbursementStepHelper.SPLIT_BILL_CONFIRM);
            //同步付款单
            this.paymoneyService.savePayMoneyByReimbursementOrder(order);
            this.updateById(order);
            this.timeDetailService.createBudgetReimbursentTimeDetail(null, nowDate, order.getReimcode(), UserThreadLocal.get().getUserName(), 5);//扫描分单修改
            this.timeDetailService.createBudgetReimbursentTimeDetail(nowDate, null, order.getReimcode(), UserThreadLocal.get().getUserName(), 6);//分单确认新增

        }
        //审核日志
        BudgetReimbursementorderVerifylog log = new BudgetReimbursementorderVerifylog();
        log.setBstatus(order.getReuqeststatus());
        log.setVerifytime(nowDate);
        log.setReimbursementid(order.getId());
        log.setReimcode(order.getReimcode());
        log.setAstatus(order.getReuqeststatus());
        log.setVerifytype(Integer.parseInt(ReimbursementStepHelper.SPLIT_BILL_CONFIRM));
        log.setVerifyername(UserThreadLocal.get().getDisplayName());
        log.setVerifyer(UserThreadLocal.get().getUserName());
        log.setVerifyflag(true);
        this.verifylogService.save(log);

        return null;
    }

    /**
     * 按照单号查询
     *
     * @param paymoneyobjectcode
     * @return
     */
    public BudgetReimbursementorder getByCode(String paymoneyobjectcode) {
        QueryWrapper<BudgetReimbursementorder> wrapper = new QueryWrapper<BudgetReimbursementorder>();
        wrapper.eq("reimcode", paymoneyobjectcode);
        return this.getOne(wrapper);
    }

    /**
     * 一键审核
     *
     * @param ids
     * @throws Exception
     */
    public void oneKeyCheck(String ids) throws Exception {
        //业务需求，现在需要临时审核通过6、7月份的数据
        //String sql = "SELECT * FROM budget_reimbursementorder WHERE yearid = 2 AND (monthid = 6 OR monthid = 7) AND reuqeststatus = 1";
        /**
         * 开放给财务使用 =======> 选中的报销单提供一键审核
         */
        List<Long> idList = new ArrayList<Long>();
        for (String id : ids.split(",")) {
            idList.add(Long.valueOf(id));
        }
        List<BudgetReimbursementorder> orderList = this.mapper.selectList(new QueryWrapper<BudgetReimbursementorder>().in("id", idList));
        for (BudgetReimbursementorder order : orderList) {
            if (order.getReuqeststatus().intValue() == 0)
                throw new RuntimeException("报销单【" + order.getReimcode() + "】状态为草稿状态。");
            if (order.getReuqeststatus().intValue() == -1)
                throw new RuntimeException("报销单【" + order.getReimcode() + "】状态为退回状态。");
            if (order.getReuqeststatus().intValue() == 2)
                throw new RuntimeException("报销单【" + order.getReimcode() + "】已审核。");
            order.setReuqeststatus(StatusConstants.BX_PASS);
            //修改更新时间
            //order.setUpdatetime(new Date());
            //审核通过时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            order.setVerifytime(calendar.getTime());
            this.syncagnentexecute(order);
            this.updateById(order);
        }
    }

    /**
     * 分页查询
     *
     * @param page
     * @param rows
     * @param authSql
     * @param authSql
     * @param order
     * @return
     * @throws Exception
     */
    public Page<ReimbursementInfoVO> pageLike(Integer page, Integer rows, Map<String, Object> conditionMap, String authSql) throws Exception {
        Page<ReimbursementInfoVO> pageCond = new Page<>(page, rows);
        List<ReimbursementInfoVO> retList = this.mapper.getReimbursementPageInfo(pageCond, conditionMap, authSql);
        for (ReimbursementInfoVO vo : retList) {
            if (StatusConstants.BX_BACK.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("退回");
            } else if (StatusConstants.BX_SAVE.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("草稿");
            } else if (StatusConstants.BX_SUBMIT.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("审核中");
            } else if (StatusConstants.BX_PASS.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("审核通过");
            }
            vo.setBxtype_dictname(ReimbursementTypeEnmu.getValue(vo.getBxtype()));
        }
        pageCond.setRecords(retList);
        return pageCond;

    }

    /**
     * 报销单撤回
     *
     * @param id
     * @return
     */
    public String withDraw(Long id) {
        BudgetReimbursementorder order = this.getById(id);
        if (null != order) {
            if (1 == order.getOrderscrtype()) {
                return "稿费报销单不能撤回";
            }
            if (order.getReuqeststatus() < StatusConstants.BX_PASS && order.getReceivestatus() <= 0) {
                if (UserThreadLocal.get().getUserId().equals(order.getApplicantid())) {
                    UpdateWrapper<BudgetReimbursementorder> wrapper = new UpdateWrapper<>();
                    wrapper.set("reuqeststatus", StatusConstants.BX_SAVE);
                    wrapper.set("updatetime", new Date());
                    wrapper.eq("id", id);
                    this.update(wrapper);
                } else {
                    return "非申请人不能操作";
                }
            } else {
                return "当前状态无法撤回";
            }
        } else {
            return "未找到此报销单";
        }
        return "";
    }

    /**
     * 报销单明细
     *
     * @param id
     * @return
     */
    public ReimbursementRequest detail(long id) {
        BudgetReimbursementorder order = this.getById(id);
        if (order == null) {
            return null;
        }
        order.setReimperonsNo(UserCache.getUserByUserId(order.getReimperonsid()).getUserName());
        if (StringUtils.isNotBlank(order.getTraveler())) {
            StringJoiner sj = new StringJoiner(",");
            for (String empNo : order.getTraveler().split(",")) {
                WbUser user = this.userService.getByEmpNo(empNo);
                if (null != user) {
                    sj.add(user.getDisplayName());
                }else {
                    sj.add(empNo);
                }
            }
            order.setTravelerName(sj.toString());
        }
        if(StringUtils.isNotBlank(order.getSpecialTravelerids())){
            String empNames = specialTravelNameListService.listByIds(Arrays.asList(order.getSpecialTravelerids().split(","))).stream().map(BudgetSpecialTravelNameList::getEmpName).collect(Collectors.joining(","));
            order.setSpecialTravelerNames(empNames);
        }
        //查询界别和预算单位
        BudgetUnit unit = this.unitService.getById(order.getUnitid());
        order.setUnitName(unit.getName());
        BudgetYearPeriod year = this.yearService.getById(order.getYearid());
        order.setYearName(year.getPeriod());
        BudgetMonthPeriod monthPeriod = this.monthPeriodMapper.selectById(order.getMonthid());
        order.setMonthName(monthPeriod.getPeriod());
        order.setBxTypeName(ReimbursementTypeEnmu.getValue(order.getBxtype()));
        ReimbursementRequest request = new ReimbursementRequest();
        request.setOrder(order);
        //报销明细
        List<BudgetReimbursementorderDetail> details = this.detailService.getByOrderId(id);

        request.setOrderDetail(details);
        //冲账
        List<BudgetReimbursementorderPayment> payment = this.paymentService.getByOrderId(id);
        payment.forEach(lendmoney->{
            BudgetLendmoney budgetLendmoney = lendmoneyMapper.selectById(lendmoney.getLendmoneyid());
            lendmoney.setLendType(LendTypeEnum.getValue(budgetLendmoney.getLendtype()));
        });
        request.setOrderPayment(payment);
        //转账
        List<BudgetReimbursementorderTrans> trans = this.transService.getByOrderId(id);
        request.setOrderTrans(trans);
        //现金
        List<BudgetReimbursementorderCash> cash = this.cashService.getByOrderId(id);
        request.setOrderCash(cash);
        //划拨
        List<BudgetReimbursementorderAllocated> allocated = this.allocatedService.getByOrderId(id);
        request.setOrderAllocated(allocated);

        int length = 0;
        if(StringUtils.isNotBlank(order.getTraveler())){
            length = length +  order.getTraveler().replace("，",",").split(",").length;
        }
        if(StringUtils.isNotBlank(order.getSpecialTravelerids())){
            length = length + order.getSpecialTravelerids().split(",").length;
        }
        //差旅
        List<BudgetReimbursementorderTravel> travel = this.travelService.getByOrderId(id);
        List<TabDm> dmList = this.dmService.list(new QueryWrapper<TabDm>().eq("dm_type", "vehicle"));
        int lengthTemp = length;
        travel.forEach(t -> {
            TabDm dm = dmList.stream().filter(d -> t.getTravelvehicle().toString().equals(d.getDmValue())).findFirst().orElse(null);
            t.setTravelvehicleName(dm == null ? "" : dm.getDmName());
            t.setSubsidyMoney(new BigDecimal(lengthTemp).multiply(t.getDailysubsidy()).multiply(t.getTravelday()));
        });
        request.setOrderTravel(travel);
        //招待
        List<BudgetReimbursementorderEntertain> entertain = this.entertainService.getByOrderId(id);
        request.setOrderEntertain(entertain);
        return request;
    }

    public void redFlush(Long id) throws Exception {
        ReimbursementRequest request = this.detail(id);
        if(request.getOrderTrans()!=null)request.getOrderTrans().clear();
        if(request.getOrderPayment()!=null)request.getOrderPayment().clear();
        if(request.getOrderCash()!=null) request.getOrderCash().clear();
        BudgetReimbursementorder order = request.getOrder();
        if (!StatusConstants.BX_PASS.equals(order.getReuqeststatus())) {
            throw new Exception("未审核通过不能冲红！");
        }
        order.setId(null);
        String bxdNum = distributedNumber.getBxdNum();
        order.setReimcode(bxdNum);
        order.setReuqeststatus(StatusConstants.BX_SAVE);
        order.setReimmoney(order.getReimmoney().negate());
        order.setPaymentmoney(BigDecimal.ZERO);
        order.setTransmoney(BigDecimal.ZERO);
        order.setAllocatedmoney(order.getAllocatedmoney().negate());
        order.setCashmoney(BigDecimal.ZERO);
        order.setOthermoney(order.getOthermoney().negate());
        request.getOrderDetail().forEach(e -> e.setReimmoney(e.getReimmoney().negate()));
        request.getOrderAllocated().forEach(e -> e.setAllocatedmoney(e.getAllocatedmoney().negate()));
        request.getOrderTravel().forEach(e -> {
            e.setLongtravelexp(e.getLongtravelexp().negate());
            e.setCitytravelexp(e.getCitytravelexp().negate());
            e.setHotelexpense(e.getHotelexpense().negate());
            e.setOther(e.getOther().negate());
            e.setDailysubsidy(e.getDailysubsidy().negate());
            e.setTotal(e.getTotal().negate());
        });
        request.getOrderEntertain().forEach(e -> {
            e.setMealsbz(e.getMealsbz().negate());
            e.setMealstotal(e.getMealstotal().negate());
            e.setHotalbz(e.getHotalbz().negate());
            e.setHotaltotal(e.getHotaltotal().negate());
            e.setTotal(e.getTotal().negate());
            e.setPublicityexp(e.getPublicityexp().negate());
            e.setOther(e.getOther().negate());
        });
        
        this.saveOrUpdate(order);
        this.setQrcode(order);
        this.saveOrUpdate(order);
        //保存附表信息
        this.saveFbInfo(request, order);
    }


    
    private List<PrintReimbursementDetail> getPrintReimbursementDetail(Long reimbursementId) {
        String sql = "SELECT detail_.subjectname,detail_.monthagentname,detail_.bunitid,detail_.reimmoney,detail_.remark,subject_.jindiecode subjectcode,subject_.pids ";
        sql = sql+" FROM budget_reimbursementorder_detail AS detail_ INNER JOIN budget_month_agent AS agent_ ON detail_.monthagentid = agent_.id ";
        sql = sql+" INNER JOIN budget_subject subject_ ON subject_.id = agent_.subjectid WHERE  detail_.reimbursementid=?  order by detail_.id ";

        List<PrintReimbursementDetail> details = this.jdbcTemplateService.query(sql, PrintReimbursementDetail.class,new Object[]{reimbursementId});
        List<PrintReimbursementDetail> newdetaillist = new java.util.ArrayList<>();
        for(PrintReimbursementDetail detail : details){
            String subjectpids = detail.getPids();
            String subjectids = "";
            String[] subjectidsArr = subjectpids.split("-");
            for(int i=0;i<subjectidsArr.length;i++){
                subjectids = subjectids+subjectidsArr[i];
                if(i<subjectidsArr.length-1) subjectids = subjectids+",";
            }

            //所有的父科目
            List<BudgetSubject> subjects = this.jdbcTemplateService.query("select id,name,code from budget_subject where id in ("+subjectids+")",BudgetSubject.class);
            for(BudgetSubject subject : subjects){
                String sname = subject.getName();
                if("省区经费".equals(sname) || "名校成本".equals(sname) || "福利费".equals(sname) || "社会保险金".equals(sname)){
                    String code = subject.getCode();
                    detail.setSubjectname(sname);
                    detail.setSubjectcode(code);
                    break;
                }
            }
            newdetaillist.add(detail);
        }
        int size = details.stream().collect(Collectors.groupingBy(e -> e.getBunitId())).size();

        //合并操作
        Map<String,PrintReimbursementDetail> groupBysubjectMap = new java.util.HashMap<>();
        for(PrintReimbursementDetail newdetail : newdetaillist){
            String subjectname = newdetail.getSubjectname();
            if(groupBysubjectMap.containsKey(subjectname)){
                PrintReimbursementDetail subjectmsg = groupBysubjectMap.get(subjectname);
                java.math.BigDecimal reimmoney = subjectmsg.getReimmoney().add(newdetail.getReimmoney());
                String remark1 = subjectmsg.getRemark()==null?"":subjectmsg.getRemark();
                String remark2 = newdetail.getRemark()==null?"":newdetail.getRemark().toString();
                subjectmsg.setReimmoney(reimmoney);
                subjectmsg.setRemark(remark1+"  "+remark2);
                groupBysubjectMap.put(subjectname,subjectmsg);
            }else{
                groupBysubjectMap.put(subjectname,newdetail);
            }
        }
        newdetaillist = new java.util.ArrayList<>(groupBysubjectMap.values());
        /*
        取消多家公司发票
         */
//        for (int i = 0; i < newdetaillist.size(); i++) {
//            if(i==0 && size>1) {
//                PrintReimbursementDetail reimbursementDetail = newdetaillist.get(0);
//                reimbursementDetail.setRemark("(多家公司发票)<br>"+reimbursementDetail.getRemark());
//            }
//        }
        if(newdetaillist.size()>=4){
            newdetaillist = newdetaillist.subList(0, 4);
        }else{
            int j = 4-newdetaillist.size();
            for(int i=0;i<j;i++){
                newdetaillist.add(new PrintReimbursementDetail(" "," ",null,null," "," ",""));
            }
        }
        return newdetaillist;
    }

    /**
     * 报销明细
     *
     * @param id
     * @return
     * @throws Exception
     */
    public Page<BxDetailVO> agentDetail(Integer page, Integer rows, Map<String, Object> conditionMap) throws Exception {
        //报销明细
        Page<BxDetailVO> pageCond = this.detailService.pageLike(page, rows, conditionMap);
        return pageCond;
    }

    /**
     * 报销明细不分页（导出）
     *
     * @param id
     * @return
     * @throws Exception
     */
    public List<BxDetailVO> agentDetailNoPage(Map<String, Object> conditionMap) throws Exception {
        //报销明细
        List<BxDetailVO> list = this.detailService.noPage(conditionMap);
        return list;
    }

    /**
     * 流转记录分页查询
     *
     * @param page
     * @param rows
     * @param reimcode
     * @return
     */
    public Page<BxLiuZhuanVO> liuzhuanPage(Integer page, Integer rows, String reimcode) {
        Page<BxLiuZhuanVO> pageCond = new Page<>(page, rows);
        List<BxLiuZhuanVO> retList = this.scanlogMapper.getLzPageInfo(pageCond, reimcode, JdbcSqlThreadLocal.get());
        pageCond.setRecords(retList);
        return pageCond;
    }

    /**
     * 异常付款分页查询
     *
     * @param page
     * @param rows
     * @param conditionMap
     * @return
     */
    public Page<BudgetPaymoney> getErrorPaymoney(Integer page, Integer rows, Map<String, Object> conditionMap) {
        Page<BudgetPaymoney> pageCond = new Page<>(page, rows);

        String userid = (null == UserThreadLocal.get()) ? "" : UserThreadLocal.getEmpNo();
        //获取当前用户所有的预算单位
        List<BudgetUnit> unitList = new ArrayList<BudgetUnit>();
        List<BudgetUnit> allUnitList = this.unitMapper.getUnitByUserId(null);

        if (!"".equals(userid)) {
            unitList = allUnitList.stream().filter(e -> ("," + e.getManagers() + ",").contains("," + UserThreadLocal.get().getUserId() + ",")).collect(Collectors.toList());
            List<BudgetUnit> tempunitList = new ArrayList<>();
            tempunitList.addAll(unitList);
            unitList.stream().forEach(e -> {
                tempunitList.addAll(allUnitList.stream().filter(a -> a.getBaseunitid().equals(e.getBaseunitid())).collect(Collectors.toList()));
            });
            ;
            unitList = tempunitList;
        } else {
            unitList = this.unitMapper.selectList(null);
        }
        List<BudgetPaymoney> retList = Lists.newArrayList();
        //所有的付款单Id
        List<String> resultpmids = new ArrayList<>();
        if (!unitList.isEmpty()) {
            String unitids = "";
            if (!"".equals(userid)) {
                List<String> authUnitidList = this.unitService.getBaseUnitIdListByAuthCenter(JdbcSqlThreadLocal.get());
                List<String> unitidList = unitList.stream().map(e -> e.getId().toString()).collect(Collectors.toList());
                unitidList.addAll(authUnitidList);
                unitids = unitidList.stream().distinct().collect(Collectors.joining(","));
            } else {
                unitids = unitList.stream().map(e -> e.getId().toString()).collect(Collectors.joining(","));
            }
            if (StringUtils.isBlank(unitids)) {
                pageCond.setTotal(0);
                pageCond.setRecords(retList);
                return pageCond;
            }
            //报销付款
            String getPmIdsSql = "SELECT bp.id from budget_reimbursementorder bro,budget_paymoney bp WHERE bro.reimcode = bp.paymoneyobjectcode AND bp.verifystatus = -1 and bro.unitid in (" + unitids + ")";
            List<Long> pmList = this.jdbcTemplateService.queryForList(getPmIdsSql, Long.class);
            if (!pmList.isEmpty()) {
                for (Long pmId : pmList) {
                    resultpmids.add(pmId.toString());
                }
            }
            List<String> budgetdeptList = new ArrayList<>();
            List<String> budgetuserList = new ArrayList<>();
            for (BudgetUnit unit : unitList) {
                String budgetdepts = unit.getBudgetdepts();
                if (StringUtils.isNotEmpty(budgetdepts)) {
                    String[] budgetdeptsArr = budgetdepts.split(",");
                    if (budgetdeptsArr.length > 0) {
                        for (String budgetdeptid : budgetdeptsArr) {
                            budgetdeptList.add(budgetdeptid);
                        }
                    }
                }
                String budgetusers = unit.getBudgetusers();
                if (StringUtils.isNotEmpty(budgetusers)) {
                    String[] budgetusersArr = budgetusers.split(",");
                    if (budgetusersArr.length > 0) {
                        for (String budgetuserid : budgetusersArr) {
                            budgetuserList.add(budgetuserid);
                        }
                    }
                }
            }
            //获取付款失败的借款付款,提成付款
            List<Map<String, Object>> lendExtractInfoList = this.mapper.getErrorLendAndExtractList();
            if (!lendExtractInfoList.isEmpty()) {
                for (Map<String, Object> msg : lendExtractInfoList) {
                    //判断是否在预算单位下
                    String empid = msg.get("empid").toString();
                    String deptids = msg.get("pids").toString();
                    String[] deptidArr = deptids.split("-");
                    boolean isinUnit = false;

                    for (String deptid : deptidArr) {
                        if (budgetdeptList.contains(deptid)) {
                            isinUnit = true;
                            break;
                        }
                    }
                    if (budgetuserList.contains(empid) || isinUnit) {
                        resultpmids.add(msg.get("paymoneyid").toString());
                    }
                }

            }
        }
        if (!resultpmids.isEmpty()) {
            String paymoneyids = resultpmids.stream().map(e -> e).collect(Collectors.joining(","));
            conditionMap.put("ids", paymoneyids);
            retList = this.paymoneyMapper.getErrorPaymoneyPageInfo(pageCond, conditionMap, null);
        }
        pageCond.setTotal(retList.size());
        List<BudgetPaymoney> resultList = retList.stream().skip((page - 1) * rows).limit(rows).collect(Collectors.toList());
        pageCond.setRecords(resultList);
        return pageCond;
    }

    /**
     * 报销执行列表分页查询
     *
     * @param page
     * @param rows
     * @param linkCode 环节代码
     * @param isHis    是否历史数据
     * @param reimcode 报销单号模糊查询
     * @return
     */
    public Page<ReimbursementInfoVO> progressPage(Integer page, Integer rows, String linkCode, Boolean isHis, String reimcode, Double reimmoney) {
        Page<ReimbursementInfoVO> pageCond = new Page<>(page, rows);
        String whereSql = "";
        String curscaner = (StringUtils.isBlank(UserThreadLocal.getEmpNo()) ? "" : UserThreadLocal.getEmpNo());
        switch (linkCode) {
            case ReimbursementStepHelper.BILL_RECEIVE:
                if (isHis) {
                    whereSql = " AND ttt.parverifystatus = 1";
                } else {
                    //update by minzhq
                    // 显示单据接收后的单子，不然扫完之后都没数据。
                    whereSql = " AND ttt.parverifyreceivestatus = 1 AND ttt.parverifystatus = 1 and ttt.fdstatus != 1 and ttt.accountstatus != 1";
                }
                break;
            case ReimbursementStepHelper.BUDGET_CHECK:
                if (isHis) {
                    whereSql = " AND ttt.budgetverifystatus = 1";
                } else {
                    whereSql = " AND ttt.budgetverifyreceivestatus = 1 AND ttt.budgetverifystatus = 0";
                }
                break;
            case ReimbursementStepHelper.SPLIT_BILL_SCAN:
                if (isHis) {
                    whereSql = " AND ttt.fdstatus = 1";
                } else {
                    whereSql = " AND ttt.fdreceivestatus = 1 AND ttt.fdstatus = 0";
                }
                break;
            case ReimbursementStepHelper.SPLIT_BILL_CONFIRM:
                if (isHis) {
                    whereSql = " AND ttt.accountstatus = 1";
                } else {
                    whereSql = " AND ttt.accountreceivestatus = 1 AND ttt.accountstatus = 0";
                }
                break;

            case ReimbursementStepHelper.FINANCIAL_MANAGE_CHECK:
                if (isHis) {
                    whereSql = " AND ttt.financialmanagestatus = 1";
                } else {
                    whereSql = " AND ttt.financialmanagereceivestatus = 1 AND ttt.financialmanagestatus = 0";
                }
                break;
            case ReimbursementStepHelper.GENERAL_MANAGER_CHECK:
                if (isHis) {
                    whereSql = " AND ttt.generalmanagestatus = 1";
                } else {
                    whereSql = " AND ttt.generalmanagereceivestatus = 1 AND ttt.generalmanagestatus = 0";
                }
                break;
            case ReimbursementStepHelper.ACCOUNTING_DO_BILL:
                whereSql = " AND ttt.account1receivestatus = 1";
                break;
            case ReimbursementStepHelper.VOUCHER_CHECK:
                whereSql = " AND ttt.voucherauditreceivestatus = 1";
                break;
            case ReimbursementStepHelper.CORPORATION_DRAW_BILL:
                whereSql = " AND ttt.drawbillreceivestatus = 1";
                break;
            case ReimbursementStepHelper.BXSH:
                curscaner = "";//报销审核不需登录人为接收人,排除掉审核通过的单据
                /**
                 * update by minzhq
                 * 去除这个限制   冯致娜2021-10-12要求的
                 * AND ttt.reuqeststatus != 2 AND (ttt.parverifystatus = 0 OR ttt.budgetverifystatus = 0)
                 */
                //whereSql = "";
                break;
            default:

                break;
        }
        List<ReimbursementInfoVO> retList = this.mapper.queryBxProgressPageInfo(pageCond, curscaner, reimcode, reimmoney, whereSql, JdbcSqlThreadLocal.get());
        for (ReimbursementInfoVO vo : retList) {
            if (StatusConstants.BX_BACK.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("退回");
            } else if (StatusConstants.BX_SAVE.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("草稿");
            } else if (StatusConstants.BX_SUBMIT.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("审核中");
            } else if (StatusConstants.BX_PASS.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("审核通过");
            }
            String financial_manage_check = ReimbursementStepHelper.get(ReimbursementStepHelper.FINANCIAL_MANAGE_CHECK);
            String general_manager_check = ReimbursementStepHelper.get(ReimbursementStepHelper.GENERAL_MANAGER_CHECK);
            String workFlowStep = vo.getWorkFlowStep();
            if (StringUtils.isNotBlank(workFlowStep) && workFlowStep.contains(financial_manage_check)) {
                vo.setFinancialManagerVerifyType(vo.getFinancialmanagereceivestatus() == null ? -1 : vo.getFinancialmanagereceivestatus() ? 0 : 1);
            } else {
                vo.setFinancialManagerVerifyType(-1);
            }
            if (StringUtils.isNotBlank(workFlowStep) && workFlowStep.contains(general_manager_check)) {
                vo.setGeneralManagerVerifyType(vo.getGeneralmanagereceivestatus() == null ? -1 : vo.getGeneralmanagereceivestatus() ? 0 : 1);
            } else {
                vo.setGeneralManagerVerifyType(-1);
            }
        }
        pageCond.setRecords(retList);
        return pageCond;
    }


    /**
     * 做账任务
     *
     * @param page
     * @param rows
     * @return
     */
    public Page<MakeAccountTaskVO> getFdDetailPage(Integer page, Integer rows, String reimcode) {
        Page<MakeAccountTaskVO> pageCond = new Page<>(page, rows);
        List<MakeAccountTaskVO> retList = this.fdTaskDetailMapper.getFdDetailPageInfo(pageCond, reimcode, JdbcSqlThreadLocal.get());
        pageCond.setRecords(retList);
        return pageCond;
    }

    /**
     * 更新报销优先级
     *
     * @param id
     * @param type
     * @return
     */
    public String updateRequestLevel(Long id, String type) {
        String getMaxLevSql = "SELECT max(ttt.requestlevel) + 1 maxlevel from budget_reimbursementorder ttt ";
        Integer maxLev = this.jdbcTemplateService.queryForObject(getMaxLevSql, Integer.class);
        BudgetReimbursementorder bean = this.mapper.selectById(id);
        if (null == bean) {
            return "报销单不存在";
        } else {
            if ("up".equals(type)) {
                bean.setRequestlevel(maxLev);
            } else {
                bean.setRequestlevel(0);
            }
            this.mapper.updateById(bean);
            return "成功";
        }
    }

    /**
     * 做账任务
     *
     * @param page
     * @param rows
     * @return
     */
    public Page<MakeAccountTaskVO> accountTaskPage(Integer page, Integer rows, String reimcode) {
        Page<MakeAccountTaskVO> pageCond = new Page<>(page, rows);
        WbUser user = UserThreadLocal.get();
        List<MakeAccountTaskVO> retList = this.mapper.getAccountTaskPageInfo(pageCond, user.getUserName(), user.getUserId(), reimcode, JdbcSqlThreadLocal.get());
        pageCond.setRecords(retList);
        return pageCond;
    }

    public void exportPreparePay(String payids, Long paybatchid, HttpServletResponse response) throws Exception {
        try {

            QueryWrapper<BudgetPaymoney> wrapper = new QueryWrapper<BudgetPaymoney>();
            if (StringUtils.isNotBlank(payids)) {
                List<String> payIds = Arrays.asList(payids.split(","));
                wrapper.in("id", payIds);
            }
            if (null != paybatchid) {
                wrapper.eq("paybatchid", paybatchid);
            }
            List<BudgetPaymoney> list = this.paymoneyService.list(wrapper);


            Map<Integer, Long> paymoneyTypeCountMap = list.stream().collect(Collectors.groupingBy(e -> {
                if (e.getPaymoneytype() == PaymoneyTypeEnum.EXTRACT_PAY.type) {
                    return PaymoneyTypeEnum.REIMBURSEMENT_PAY.type;
                }
                return e.getPaymoneytype();
            }, Collectors.counting()));
            String paymoneySystem = "";
            if(paymoneyTypeCountMap.size()>1){
                paymoneySystem = "-OA-预算";
            }else{
                if(paymoneyTypeCountMap.containsKey(PaymoneyTypeEnum.REIMBURSEMENT_PAY.type)){
                    paymoneySystem = "-预算";
                }
                if(paymoneyTypeCountMap.containsKey(PaymoneyTypeEnum.LEND_PAY.type)){
                    paymoneySystem = "-OA";
                }
            }

            Set<String> unitNameSet = new LinkedHashSet<>();//付款单位set
            Map<String, List<PayeeDetailExcelData>> unitNameListMap = new HashMap<>();
            Set<PayUnitBankSumExcelData> sumDataSet = new LinkedHashSet<>();
            Map<String, WbBanks> banksMap = this.wbMapper.queryAllBanks();//获取所有银行信息
            for (int i = 0; i < list.size(); i++) {
                BudgetPaymoney pm = list.get(i);
                if (Constants.PAY_TYPE.CASH.equals(pm.getPaytype())) {
                    //现金不统计
                    continue;
                }
                if (unitNameSet.add(pm.getBunitname())) {
                    List<PayeeDetailExcelData> detailList = new ArrayList<>();
                    WbBanks bankInfo = banksMap.get(pm.getBankaccountbranchcode());
                    if (null != bankInfo) {
                        PayeeDetailExcelData detailInfo = new PayeeDetailExcelData(pm.getBankaccount(), pm.getBankaccountname(), bankInfo.getSubBranchName(), bankInfo.getProvince(), bankInfo.getCity(), pm.getPaymoney(), bankInfo.getSubBranchCode(), pm.getBankaccountbranchname());
                        detailList.add(detailInfo);
                        unitNameListMap.put(pm.getBunitname(), detailList);
                    }
                } else {
                    WbBanks bankInfo = banksMap.get(pm.getBankaccountbranchcode());
                    if (null != bankInfo) {
                        List<PayeeDetailExcelData> detailList = unitNameListMap.get(pm.getBunitname());
                        PayeeDetailExcelData detailInfo = new PayeeDetailExcelData(pm.getBankaccount(), pm.getBankaccountname(), bankInfo.getSubBranchName(), bankInfo.getProvince(), bankInfo.getCity(), pm.getPaymoney(), bankInfo.getSubBranchCode(), pm.getBankaccountbranchname());
                        detailList.add(detailInfo);
                    }
                }
                PayUnitBankSumExcelData excelData = new PayUnitBankSumExcelData(pm.getBunitname(), pm.getBankaccountbranchname(), pm.getPaymoney());
                if (sumDataSet.add(excelData)) {
                    excelData.getIndexList().add(i);
                } else {
                    for (PayUnitBankSumExcelData tempData : sumDataSet) {
                        if (tempData.equals(excelData)) {
                            BigDecimal totolMoney = tempData.getPayMoney().add(excelData.getPayMoney());
                            tempData.setPayMoney(totolMoney);
                            tempData.getIndexList().add(i);
                            break;
                        }
                    }
                }
            }
            Map<String, List<PayeeDetailExcelData>> unitAndBankListMap = new HashMap<>();
            BigDecimal sumAllMoney = new BigDecimal(0);
            for (PayUnitBankSumExcelData excelData : sumDataSet) {
                List<PayeeDetailExcelData> detailList = new ArrayList<>();
                BigDecimal totalMoney = new BigDecimal(0);
                for (Integer index : excelData.getIndexList()) {
                    BudgetPaymoney pm = list.get(index);
                    WbBanks bankInfo = banksMap.get(pm.getBankaccountbranchcode());
                    PayeeDetailExcelData detailInfo = new PayeeDetailExcelData(pm.getBankaccount(), pm.getBankaccountname(), bankInfo.getSubBranchName(), bankInfo.getProvince(), bankInfo.getCity(), pm.getPaymoney(), bankInfo.getSubBranchCode(), pm.getBankaccountbranchname());
                    detailList.add(detailInfo);
                    totalMoney = totalMoney.add(pm.getPaymoney());
                }
                PayeeDetailExcelData sumData = new PayeeDetailExcelData("", "", "", "", "总计：", totalMoney, "", "");
                detailList.add(sumData);//总计为单独一行
                unitAndBankListMap.put(excelData.getUnitName() + "-" + excelData.getBankName(), detailList);
                sumAllMoney = sumAllMoney.add(excelData.getPayMoney());//所有金额总计
            }
            ExcelWriter excelWriter = EasyExcel.write(EasyExcelUtil.getOutputStream("付款明细表"+paymoneySystem, response)).build();

            WriteSheet sumSheet = EasyExcel.writerSheet("付款明细汇总").build();
            WriteTable sumTable = EasyExcel.writerTable(0).head(PayUnitBankSumExcelData.class).needHead(true).build();
            List<PayUnitBankSumExcelData> sumList = new ArrayList<>(sumDataSet);
            PayUnitBankSumExcelData allSumData = new PayUnitBankSumExcelData("", "总计：", sumAllMoney);
            sumList.add(allSumData);
            excelWriter.write(sumList, sumSheet, sumTable);
            WriteTable detailTable = EasyExcel.writerTable(1).head(PayeeDetailExcelData.class).needHead(true).build();
            detailTable.setUseDefaultStyle(true);
            for (Map.Entry<String, List<PayeeDetailExcelData>> entry : unitNameListMap.entrySet()) {
                List<PayeeDetailExcelData> tempList = entry.getValue();
                BigDecimal totalMoney = new BigDecimal(0);
                for (PayeeDetailExcelData tempData : tempList) {
                    totalMoney = totalMoney.add(tempData.getPayMoney());
                }
                PayeeDetailExcelData sumData = new PayeeDetailExcelData("", "", "", "", "总计：", totalMoney, "", "");
                tempList.add(sumData);//总计为单独一行
                WriteSheet detailSheet = EasyExcel.writerSheet(entry.getKey()).build();
                excelWriter.write(tempList, detailSheet, detailTable);
            }
            for (Map.Entry<String, List<PayeeDetailExcelData>> entry : unitAndBankListMap.entrySet()) {
                WriteSheet detailSheet = EasyExcel.writerSheet(entry.getKey()).build();
                excelWriter.write(entry.getValue(), detailSheet, detailTable);
            }
            excelWriter.finish();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 根据报销单号获取二维码
     *
     * @param reimcode
     * @return
     */
    public List<Map<String, Object>> getQrCodeByReimcode(String reimcode) {
        if (StringUtils.isBlank(reimcode) || reimcode.length() < 4) {
            return null;
        }
        List<Map<String, Object>> qrCodeList = this.mapper.getQrCodeByReimcode(reimcode);
        return qrCodeList;
    }

    /**
     * 导出退回原因汇总表
     *
     * @param yearId
     * @param monthId
     * @param response
     * @throws Exception
     */
    public void exportReturnReason(Long yearId, Long monthId, HttpServletResponse response) throws Exception {
        List<Map<String, Object>> retList = this.mapper.getBxReturnReason(yearId, monthId);
        List<List<String>> dataList = new ArrayList<>();
        for (Map<String, Object> vo : retList) {
            List<String> colList = new ArrayList<>();
            colList.add((String) vo.get("period"));
            colList.add((String) vo.get("monthName"));
            colList.add((String) vo.get("unitName"));
            colList.add((String) vo.get("reimcode"));
            colList.add((String) vo.get("subjectNames"));
            BigDecimal reimmoney = (BigDecimal) vo.get("reimmoney");
            colList.add(reimmoney.toString());
            colList.add((String) vo.get("reimperonsname"));
            colList.add((String) vo.get("verifyinfo"));
            dataList.add(colList);
        }
        ResponseUtil.exportBxReturn(dataList, EasyExcelUtil.getOutputStream("导出退回原因汇总", response));
    }

	public void initReimcode() {
        List<BudgetReimbursementorder> budgetReimbursementorders = this.mapper.selectList(null);

        budgetReimbursementorders.forEach(e->{
            String qrcodebase64str = null;
            try {
                qrcodebase64str = QRCodeUtil.createBase64Qrcode(this.bx_qrcode_url + e.getId() + "-" + e.getVersion(), this.file_temp_path + File.separator + e.getId() + QRCODE_FORMAT);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            e.setQrcodebase64str(qrcodebase64str);
        });
        this.updateBatchById(budgetReimbursementorders);
    }



    public ReimbursementRequest printDetail(Long id) {
        BudgetReimbursementorder order = this.getById(id);
        if (order == null) {
           throw new RuntimeException("报销单id【"+id+"】已不存在");
        }
        if (StringUtils.isNotBlank(order.getTraveler()) || StringUtils.isNotBlank(order.getSpecialTravelerids())) {
            StringJoiner sj = new StringJoiner(",");
            for (String empNo : order.getTraveler().split(",")) {
                if(StringUtils.isBlank(empNo)) continue;
                WbUser user = this.userService.getByEmpNo(empNo);
                if (null != user) {
                    sj.add(user.getDisplayName());
                }else {
                    sj.add(empNo);
                }
            }
            if(StringUtils.isNotBlank(order.getSpecialTravelerids())){
                specialTravelNameListService.listByIds(Arrays.asList(order.getSpecialTravelerids().split(","))).forEach(e->sj.add(e.getEmpName()));
            }
            order.setTravelerName(sj.toString());
        }
        //查询界别和预算单位
        BudgetUnit unit = this.unitService.getById(order.getUnitid());
        order.setUnitName(unit.getName());
        BudgetYearPeriod year = this.yearService.getById(order.getYearid());
        order.setYearName(year.getPeriod());
        BudgetMonthPeriod monthPeriod = this.monthPeriodMapper.selectById(order.getMonthid());
        order.setMonthName(monthPeriod.getPeriod());
        order.setBxTypeName(ReimbursementTypeEnmu.getValue(order.getBxtype()));
        ReimbursementRequest request = new ReimbursementRequest();
        request.setOrder(order);
        //报销明细
        //List<BudgetReimbursementorderDetail> details = this.detailService.getByOrderId(id);

        /**
         * update by minzhq
         * 1。根据科目合并。
         */
        List<PrintReimbursementDetail> printReimbursementDetails = getPrintReimbursementDetail(order.getId());
        List<BudgetReimbursementorderDetail> resultDetails = printReimbursementDetails.stream().map(e -> {
            BudgetReimbursementorderDetail detail = new BudgetReimbursementorderDetail();
            detail.setSubjectCode(e.getSubjectcode());
            detail.setSubjectname(e.getSubjectname());
            detail.setReimmoney(e.getReimmoney());
            detail.setRemark(e.getRemark());
            return detail;
        }).collect(Collectors.toList());
        request.setOrderDetail(resultDetails);
        //冲账
        List<BudgetReimbursementorderPayment> payment = this.paymentService.getByOrderId(id);
        request.setOrderPayment(payment);
        //转账
        List<BudgetReimbursementorderTrans> trans = this.transService.list(new QueryWrapper<BudgetReimbursementorderTrans>().eq("reimbursementid",id));
        //List<BudgetReimbursementorderTrans> trans = this.transService.getByOrderId(id);
        request.setOrderTrans(trans);
        //现金
        List<BudgetReimbursementorderCash> cash = this.cashService.getByOrderId(id);
        request.setOrderCash(cash);
        //划拨
        List<BudgetReimbursementorderAllocated> allocated = this.allocatedService.getByOrderId(id);
        request.setOrderAllocated(allocated);
        //差旅
        List<BudgetReimbursementorderTravel> travel = this.travelService.getByOrderId(id);
        int length = 0;
        if(StringUtils.isNotBlank(order.getTraveler())){
            length = order.getTraveler().replace("，",",").split(",").length;
        }
        int lengthTemp = length;
        List<TabDm> dmList = this.dmService.list(new QueryWrapper<TabDm>().eq("dm_type", "vehicle"));
        travel.forEach(t -> {
            TabDm dm = dmList.stream().filter(d -> t.getTravelvehicle().toString().equals(d.getDmValue())).findFirst().orElse(null);
            t.setTravelvehicleName(dm == null ? "" : dm.getDmName());
            t.setSubsidyMoney(new BigDecimal(lengthTemp).multiply(t.getDailysubsidy()).multiply(t.getTravelday()));
        });
        request.setOrderTravel(travel);
        //招待
        List<BudgetReimbursementorderEntertain> entertain = this.entertainService.getByOrderId(id);
        request.setOrderEntertain(entertain);
        return request;
    }

    public static void main(String[] args) {
//        String qrcodebase64str = null;
//        try {
//            qrcodebase64str = QRCodeUtil.createBase64Qrcode("http://oauth.jtyjy.com/ys/api/reimbursement/code?c=" + 26743 + "-" + 6, "/home/data/tmp" + File.separator + 26743 + QRCODE_FORMAT);
//        } catch (Exception exception) {
//            exception.printStackTrace();
//        }
//        System.out.println(qrcodebase64str);
        System.out.println(new BigDecimal("123").negate());
    }

    public List<String> listBackType() {
        return this.mapper.listBackType();
    }

    public void exportExpense2(ReimBursementDTO dto, HttpServletResponse response) {
        Map<String,Object> map = dto.toMap();
        String authSql = "";
        if (dto.getBudgeterflag()) {
            //增加权限控制
            authSql = JdbcSqlThreadLocal.get();
            map.put("managers", UserThreadLocal.get().getUserId());
        }else {
            map.put("applicantid", UserThreadLocal.get().getUserId());
        }
        List<ExpenseInfoVO> vos = new ArrayList<>();
        List<ReimbursementInfoVO> retList = this.mapper.getReimbursementPageInfo(null, map, authSql);
        for (ReimbursementInfoVO vo : retList) {
            if (StatusConstants.BX_BACK.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("退回");
            } else if (StatusConstants.BX_SAVE.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("草稿");
            } else if (StatusConstants.BX_SUBMIT.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("审核中");
            } else if (StatusConstants.BX_PASS.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("审核通过");
            }
            vo.setBxtype_dictname(ReimbursementTypeEnmu.getValue(vo.getBxtype()));
            ExpenseInfoVO infoVO = new ExpenseInfoVO();
            BeanUtils.copyProperties(vo,infoVO);
            vos.add(infoVO);
        }
        InputStream is =null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream("template/expenseInfoTemplate.xlsx");
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("22", response), ExpenseInfoVO.class).withTemplate(is).build();
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            sheet.setSheetName("银行账户");
            workBook.fill(vos, sheet);
            workBook.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }/*finally {
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/


    }

    public List<ExpenseInfoVO> getExpenseInfo(Map<String, Object> conditionMap, String authSql){
        List<ReimbursementInfoVO> retList = this.mapper.getReimbursementPageInfo(null, conditionMap, authSql);
        List<ExpenseInfoVO> vos = new ArrayList<>();
        for (ReimbursementInfoVO vo : retList) {
            if (StatusConstants.BX_BACK.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("退回");
            } else if (StatusConstants.BX_SAVE.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("草稿");
            } else if (StatusConstants.BX_SUBMIT.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("审核中");
            } else if (StatusConstants.BX_PASS.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("审核通过");
            }
            vo.setBxtype_dictname(ReimbursementTypeEnmu.getValue(vo.getBxtype()));
            ExpenseInfoVO infoVO = new ExpenseInfoVO();
            BeanUtils.copyProperties(vo,infoVO);
            vos.add(infoVO);
        }
        return vos;
    }

    public void exportExpense(Map<String, Object> conditionMap, String authSql, HttpServletResponse response) throws IOException {
        List<ReimbursementInfoVO> retList = this.mapper.getReimbursementPageInfo(null, conditionMap, authSql);
        List<ExpenseInfoVO> vos = new ArrayList<>();
        for (ReimbursementInfoVO vo : retList) {
            if (StatusConstants.BX_BACK.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("退回");
            } else if (StatusConstants.BX_SAVE.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("草稿");
            } else if (StatusConstants.BX_SUBMIT.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("审核中");
            } else if (StatusConstants.BX_PASS.equals(vo.getReuqeststatus())) {
                vo.setReuqeststatus_dictname("审核通过");
            }
            vo.setBxtype_dictname(ReimbursementTypeEnmu.getValue(vo.getBxtype()));
            ExpenseInfoVO infoVO = new ExpenseInfoVO();
            BeanUtils.copyProperties(vo,infoVO);
            vos.add(infoVO);
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode("预算员报销", "UTF-8").replaceAll("\\+", "%20");;
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(),ExpenseInfoVO.class)
                .autoCloseStream(false).sheet("预算员报销").doWrite(vos);
    }

    /**
     * 发送消息
     * @param msg 消息内容
     * @param empno 工号
     */
	public void sendQYWXTextMsg(String msg, String empno) {
        sender.sendQywxMsgSyn(new QywxTextMsg(empno+"|17474", null, null, 0, msg, null));
    }

    /**
     * 发送报销进度消息提醒报销人
     * @param stepFlag
     * @param opt
     * @param setStep
     * @param order
     * @throws MyException
     */
    public void noticeScheduleToBxr(String stepFlag, String opt, String setStep, BudgetReimbursementorder order) throws MyException {
	    if (ReimbursementStepHelper.RECEIVED.equals(opt)) {
	        //票面接收
            if (!ReimbursementStepHelper.BILL_RECEIVE.equals(stepFlag) && !ReimbursementStepHelper.FINANCIAL_MANAGE_CHECK.equals(stepFlag)
                    && !ReimbursementStepHelper.GENERAL_MANAGER_CHECK.equals(stepFlag) && !ReimbursementStepHelper.CASHIER_PAY.equals(stepFlag)) {
                return;
            }
        } else {
	        //审核
            if (!ReimbursementStepHelper.BUDGET_CHECK.equals(stepFlag) && !ReimbursementStepHelper.SPLIT_BILL_SCAN.equals(stepFlag)
                    && !ReimbursementStepHelper.SPLIT_BILL_CONFIRM.equals(stepFlag)) {
                return;
            }
        }
	    //出纳付款前面环节需发送消息
        List<TabDm> dmList = dmService.list(Wrappers.<TabDm>lambdaQuery().eq(TabDm::getDmType, "verifyNotice").eq(TabDm::getDmStatus, 1));
        if(!CollectionUtils.isEmpty(dmList)) {
            //不包含指定科目无需发消息
            List<String> dms = dmList.stream().map(TabDm::getDmName).collect(Collectors.toList());
            List<BudgetReimbursementorderDetail> detailList = detailService.getByOrderId(order.getId());
            List<BudgetReimbursementorderDetail> matchSubjectDetailList = detailList.stream().filter(e -> dms.contains(e.getSubjectname())).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(matchSubjectDetailList)){
                //需通知报销人
                WbUser user = UserCache.getUserByUserId(order.getReimperonsid());
                String msg = "您报销的["+ matchSubjectDetailList.get(0).getSubjectname()+"]（￥" + order.getReimmoney() + "）已流转至[" + setStep + "]环节";
                sender.sendQywxMsg(new QywxTextMsg(user.getUserName(), null, null, 0, msg, null));
            }
        }



    }
}
@Data
@AllArgsConstructor
@NoArgsConstructor
class PrintReimbursementDetail{
    private String subjectname;
    private String monthagentname;
    private Long bunitId;
    private BigDecimal reimmoney;
    private String remark;
    private String subjectcode;
    private String pids;
}


@Data
@NoArgsConstructor
@AllArgsConstructor
class A{
    private String a;
    private BigDecimal m;
}
