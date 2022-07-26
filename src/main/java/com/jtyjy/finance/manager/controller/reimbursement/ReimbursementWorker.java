package com.jtyjy.finance.manager.controller.reimbursement;

import com.google.common.collect.Lists;
import com.jtyjy.common.tools.ClassTools;
import com.jtyjy.core.jdbc.JdbcTemplateService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.StatusConstants;
import com.jtyjy.finance.manager.dto.ReimbursementRequest;
import com.jtyjy.finance.manager.enmus.ReimbursementTypeEnmu;
import com.jtyjy.finance.manager.mapper.BudgetMonthAgentMapper;
import com.jtyjy.finance.manager.mapper.response.LendmoneyUseBean;
import com.jtyjy.finance.manager.service.BudgetLendmoneyService;
import com.jtyjy.finance.manager.service.BudgetPaymoneyService;
import com.jtyjy.finance.manager.service.BudgetReimbursementorderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 报销工作者
 *
 * @author User
 * <p>
 * <p>
 * 校验逻辑
 * 报销单：
 * 新增：
 * ​	通用校验 -> 相似校验（届别+部门+报销金额+报销人+报销类型+报销日期）-> 保存
 * 提交：
 * ​	按照报销单主键查看数据库是否存在：
 * ​	存在： 表单状态校验(是否提交)  -> 通用校验（可能存在数据修改）-> 相似校验(非本报销单) -> 动因和科目校验 -> 保存或修改 -> 提交 ----> 锁定借款单
 * ​	不存在：通用校验 -> 相似校验 -> 动因和科目校验 -> 保存或修改 -> 提交 --> 锁定借款单
 * 修改：
 * ​	表单状态校验(是否提交)  -> 通用校验 -> 相似校验 -> 保存或修改 --------可选------>  动因和科目校验 -> 保存或修改 -> 提交 ----> 锁定借款单
 */
@Component
public class ReimbursementWorker {

    @Autowired
    private BudgetReimbursementorderService orderService;
    @Autowired
    private BudgetLendmoneyService lendmoneyService;
    @Autowired
    private BudgetPaymoneyService paymoneyService;

    @Autowired
    private BudgetMonthAgentMapper monthAgentMapper;


    /**
     * 保存:通用校验 -> 相似校验（届别+部门+报销金额+报销人+报销类型+报销日期）----提交校验-----> 保存
     *
     * @param request
     * @param isCommit 是否提交
     * @throws Exception
     */
    public String save(ReimbursementRequest request, boolean isCommit) throws Exception {
        String result = "";
        //提交校验
        if (isCommit) {
            result = this.submitValidate(request);
            if (StringUtils.isNotEmpty(result)) {
                return result;
            }
        } else {
            result = this.baseValidate(request);
            if (StringUtils.isNotEmpty(result)) {
                return result;
            }
            result = this.likeValidate(request);
            if (StringUtils.isNotEmpty(result)) {
                return result;
            }
        }

        Boolean isFixAsset = request.getIsFixAsset();
        Boolean isOnlyValidate = request.getIsOnlyValidate();
        /**
         * add by minzhq
         * 做保存操作：
         *    一、不是固定资产
         *    二、(是固定资产)并且(不做验证操作)
         */
        if((isFixAsset==null) || (isFixAsset!=null && isFixAsset && isOnlyValidate!=null && !isOnlyValidate)) this.orderService.saveOrUpdateAndSubmit(request, isCommit);
        return null;
    }

    /**
     * 修改：表单状态校验(是否提交)  -> 通用校验 -> 相似校验 -> 保存或修改 --------可选------>  提交校验 -> 保存或修改 -> 提交
     *
     * @param request
     * @param isCommit 是否提交
     * @return
     * @throws Exception
     */
    public String update(ReimbursementRequest request, boolean isCommit) throws Exception {
        String result = "";
        if (isCommit) {
            result = this.submitValidate(request);
            if (StringUtils.isNotEmpty(result)) {
                return result;
            }
        } else {
            result = this.formStatusValidate(request);
            if (StringUtils.isNotEmpty(result)) {
                return result;
            }
            result = this.baseValidate(request);
            if (StringUtils.isNotEmpty(result)) {
                return result;
            }
            result = this.likeValidate(request);
            if (StringUtils.isNotEmpty(result)) {
                return result;
            }
        }
        //保存或修改
        this.orderService.saveOrUpdateAndSubmit(request, isCommit);
        return null;
    }

    /**
     * 提交：
     * ​	按照报销单主键查看数据库是否存在：
     * ​	存在： 表单状态校验(是否提交)  -> 通用校验（可能存在数据修改）-> 相似校验(非本报销单) -> 动因和科目校验 -> 保存或修改 -> 提交
     * ​	不存在：通用校验 -> 相似校验 -> 动因和科目校验 -> 保存或修改 -> 提交
     *
     * @param request
     * @return
     * @throws Exception
     */
    public String submit(ReimbursementRequest request) throws Exception {
        String result = this.submitValidate(request);
        if (StringUtils.isNotEmpty(result)) {
            return result;
        }
        //提交
        this.orderService.submit(request);
        return null;
    }


    /**
     * 删除:报销单产生付款记录不能删除、报销单非草稿状态不能删除
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
     * 删除budget_reimbursementorder_payment
     *
     * @param id
     * @return
     * @throws Exception
     */
    public String delete(Long id) throws Exception {
        //报销单是否草稿状态
        BudgetReimbursementorder order = this.orderService.getById(id);
        if (order == null) {
            return "报销单不存在！";
        }
        if (!StatusConstants.BX_SAVE.equals(order.getReuqeststatus()) && !StatusConstants.BX_BACK.equals(order.getReuqeststatus())) {
            return "报销单非草稿状态，不能删除！";
        }
        //是否产生付款记录
        List<BudgetPaymoney> list = this.paymoneyService.getByReimbursementOrderId(order.getReimcode());
        if (list != null && list.size() > 0) {
            return "该报销单已经产生付款记录！";
        }
        //执行删除
        return this.orderService.delete(order.getId());
    }

    /**
     * 差旅和招待互斥校验
     *
     * @param request
     * @return
     */
    public final String travelEntertainValidate(ReimbursementRequest request) {
        BudgetReimbursementorder order = request.getOrder();
        List<BudgetReimbursementorderTravel> orderTravel = request.getOrderTravel();
        List<BudgetReimbursementorderEntertain> orderEntertain = request.getOrderEntertain();
        if (order.getBxtype() == ReimbursementTypeEnmu.TRAVAL.getCode() || order.getBxtype() == ReimbursementTypeEnmu.TRAVALSUBSIDIES.getCode()) {
            if (orderTravel.isEmpty()) {
                return "差旅费信息为空！";
            }
            if (!orderEntertain.isEmpty()) {
                return "差旅类报销单不能包含招待数据！";
            }
        }
        if (order.getBxtype() == ReimbursementTypeEnmu.ENTERTAIN.getCode() || order.getBxtype() == ReimbursementTypeEnmu.ENTERTAINSPREAD.getCode()) {
            if (orderEntertain.isEmpty()) {
                return "招待费信息为空！";
            }
            if (!orderTravel.isEmpty()) {
                return "招待类报销单不能包含差旅数据！";
            }
        }
        return null;
    }

    /**
     * 不计入执行金额
     *
     * @return
     */
    public final BigDecimal noJoinCalcMoney(List<BudgetReimbursementorderDetail> orderDetail) {
        BigDecimal total = new BigDecimal(-1);
        if (!orderDetail.isEmpty()) {
            total = BigDecimal.ZERO;
            for (BudgetReimbursementorderDetail detail : orderDetail) {
                if (!detail.getReimflag()) {
                    total = total.add(detail.getReimmoney());
                }
            }
        }
        return total;
    }

    /**
     * 报销基础校验：通用校验
     *
     * @param request
     * @return
     * @throws Exception
     */
    public final String baseValidate(ReimbursementRequest request) throws Exception {
        BudgetReimbursementorder order = request.getOrder();

        if(order.getBxtype().equals(ReimbursementTypeEnmu.TRAVAL.getCode()) || order.getBxtype().equals(ReimbursementTypeEnmu.TRAVALSUBSIDIES.getCode())){
            //出差校验
            if(StringUtils.isBlank(order.getTraveler()) && StringUtils.isBlank(order.getSpecialTravelerids()))
                return "请填写有效的出差人员！";
        }

        List<BudgetReimbursementorderDetail> orderDetail = request.getOrderDetail();
        List<BudgetReimbursementorderAllocated> orderAllocated = request.getOrderAllocated();

        List<Long> monthAgentIds = orderDetail.stream().map(BudgetReimbursementorderDetail::getMonthagentid).collect(Collectors.toList());
        List<Long> allocatedMonthAgentIds = orderAllocated.stream().map(BudgetReimbursementorderAllocated::getMonthagentid).collect(Collectors.toList());
        monthAgentIds.addAll(allocatedMonthAgentIds);

        List<BudgetMonthAgent> budgetMonthAgents = monthAgentMapper.selectBatchIds(monthAgentIds);

        long count = budgetMonthAgents.stream().filter(e -> !e.getMonthid().toString().equals(order.getMonthid().toString())).count();
        if(count>0){
            return "动因中存在不属于【"+order.getMonthid()+"】月下的动因。请删掉划拨及明细中的动因重试";
        }
        /*
         * add by minzhq
         * 校验报销时使用。
         */
        Map<Long, BudgetMonthAgent> monthAgentMap = budgetMonthAgents.stream().collect(Collectors.toMap(e -> e.getId(), Function.identity()));
        List<String> subjects = new ArrayList<>();
        orderDetail.forEach(e -> {
            BudgetMonthAgent bma = monthAgentMap.get(e.getMonthagentid());
            e.setYearagentid(bma.getYearagentid());
            e.setSubjectid(bma.getSubjectid());
            e.setMonthSubjectKey(bma.getUnitid() + "-" + bma.getSubjectid());
            e.setYearSubjectKey(bma.getUnitid() + "-" + bma.getSubjectid());
            subjects.add(e.getSubjectname());
        });
        /**
         * add by gll
         * 报销类型和科目校验
         */
        String msg = validBxTypeAndSubject(order.getBxtype(),subjects);
        if(StringUtils.isNotBlank(msg)){
            return msg;
        }

        List<BudgetReimbursementorderPayment> orderPayment = request.getOrderPayment();
        List<BudgetReimbursementorderTrans> orderTrans = request.getOrderTrans();
        List<BudgetReimbursementorderTravel> orderTravel = request.getOrderTravel();
        List<BudgetReimbursementorderEntertain> orderEntertain = request.getOrderEntertain();
        List<BudgetReimbursementorderCash> orderCash = request.getOrderCash();

        String result = order.validate();
        if (StringUtils.isNotEmpty(result)) {
            return result;
        }
        //普通报销单与差旅，招待互斥
        if ((request.getOrderTravel() != null && request.getOrderTravel().size() > 0) || (request.getOrderEntertain() != null && request.getOrderEntertain().size() > 0)) {
            if (order.getBxtype().equals(ReimbursementTypeEnmu.COMMON.getCode())) {
                return "通用报销单不能填报差旅或招待信息！";
            }
        }
        //查询所有开票单位下的所有付款账户主键
        Set<Long> tranPayAccountIds = this.orderService.getPayAccountFromDetail(orderDetail);
        //差旅补贴和差旅报销与招待报销和推广招待互斥
        result = travelEntertainValidate(request);
        if (StringUtils.isNotEmpty(result)) {
            return result;
        }
        result = BudgetReimbursementorderDetail.validate(orderDetail);
        if (StringUtils.isNotEmpty(result)) {
            return result;
        }
        result = BudgetReimbursementorderPayment.validate(orderPayment, order.getPaymentmoney());
        if (StringUtils.isNotEmpty(result)) {
            return result;
        }

        /**
         * 增加校验，付款账户的金额合并必须小于开票单位的金额
         */
        //Map<Long, List<BudgetBillingUnitAccount>> unitAccountMap = unitAccountMapper.selectList(null).stream().collect(Collectors.groupingBy(BudgetBillingUnitAccount::getBillingunitid));

        /*List<String> results = new ArrayList<>();
        orderDetail.stream().collect(Collectors.groupingBy(BudgetReimbursementorderDetail::getBunitid)).forEach((bunitid,list)->{
            List<BudgetBillingUnitAccount> budgetBillingUnitAccounts = unitAccountMap.get(bunitid);
            if(CollectionUtils.isEmpty(budgetBillingUnitAccounts)) return;
            List<Long> accountIds = budgetBillingUnitAccounts.stream().map(BudgetBillingUnitAccount::getId).collect(Collectors.toList());
            BigDecimal money = orderTrans.stream().filter(e -> accountIds.contains(e.getDraweeunitaccountid())).map(BudgetReimbursementorderTrans::getTransmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal reimmoney = list.stream().map(BudgetReimbursementorderDetail::getReimmoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            if(money.compareTo(reimmoney)>0) results.add("转账金额合计【"+money.stripTrailingZeros().toPlainString()+"】大于开票单位金额合计【"+reimmoney.stripTrailingZeros().toPlainString()+"】");
        });
        if(!results.isEmpty()){
            return results.stream().collect(Collectors.joining("<br>"));
        }*/

        result = BudgetReimbursementorderTrans.validate(orderTrans, tranPayAccountIds, order.getTransmoney(),order.getTransmoney());
        if (StringUtils.isNotEmpty(result)) {
            return result;
        }

        result = BudgetReimbursementorderCash.validate(orderCash, order.getCashmoney());
        if (StringUtils.isNotEmpty(result)) {
            return result;
        }
        //校验是否有不计入执行
        BigDecimal noJoinCalcMoney = noJoinCalcMoney(orderDetail);
        //不计入执行金额大于0
        if (noJoinCalcMoney.compareTo(BigDecimal.ZERO) > 0) {
            result = BudgetReimbursementorderAllocated.validate(orderAllocated, noJoinCalcMoney, order.getAllocatedmoney());
            if (StringUtils.isNotEmpty(result)) {
                return result;
            }
        }
        if(!orderAllocated.isEmpty() && noJoinCalcMoney.compareTo(BigDecimal.ZERO) == 0) return "报销明细中不存在不计入执行数据！";
        BigDecimal reimMoney = orderDetail.stream().map(BudgetReimbursementorderDetail::getReimmoney).reduce(BigDecimal.ZERO,BigDecimal::add);

        int length = 0;
        if(StringUtils.isNotBlank(order.getTraveler())){
            length = length + order.getTraveler().replace("，",",").split(",").length;
        }
        if(StringUtils.isNotBlank(order.getSpecialTravelerids())){
            length = length + order.getSpecialTravelerids().split(",").length;
        }

        result = BudgetReimbursementorderTravel.validate(orderTravel,reimMoney,length);
        if (StringUtils.isNotEmpty(result)) {
            return result;
        }
        result = BudgetReimbursementorderEntertain.validate(orderEntertain,reimMoney);
        if (StringUtils.isNotEmpty(result)) {
            return result;
        }
        //提交即做此校验
        if ("1".equals(request.getSubmit())) {
            //校验报销金额：报销金额=冲账金额 + 转账金额 + 现金金额 + 其他金额
            BigDecimal total = order.getPaymentmoney().add(order.getTransmoney()).add(order.getCashmoney()).add(order.getOthermoney());
            if (order.getReimmoney().compareTo(total) != 0) {
                return "【报销金额】不等于【冲账金额 + 转账金额 + 现金金额 + 其他金额】";
            }
        }

        return null;
    }

    private String validBxTypeAndSubject(Integer bxtype, List<String> subjects) {
        String msg = "";
        switch (bxtype){
            case 2:
                for(String str:subjects){
                    if(!"差旅费".equals(str)){
                        msg = "差旅报销对应的科目只能是差旅费";
                        break;
                    }
                }
                break;
            case 3:
                for(String str:subjects){
                    if(!"招待费".equals(str)){
                        msg = "招待报销对应的科目只能是招待费";
                        break;
                    }
                }
                break;
            case 4:
                for(String str:subjects){
                    if(!"差旅补贴".equals(str)){
                        msg = "差旅补贴对应的科目只能是差旅补贴";
                        break;
                    }
                }
                break;
            case 5:
                for(String str:subjects){
                    if(!"推广招待".equals(str)){
                        msg = "推广招待对应的科目只能是推广招待";
                        break;
                    }
                }
                break;
        }
        return msg;
    }

    /**
     * 相似校验
     * 届别+部门+报销金额+报销人+报销类型+报销日期唯一
     *
     * @param request
     * @return
     */
    public final String likeValidate(ReimbursementRequest request) {
        boolean exist = this.orderService.duplicate(request.getOrder());
        if (exist) {
            return "您有一个相似的报销单，请确认！本次操作已取消！";
        }
        return null;
    }

    /**
     * 表单状态校验:是否草稿或退回状态
     */
    public final String formStatusValidate(ReimbursementRequest request) {
        BudgetReimbursementorder order = this.orderService.getById(request.getOrder().getId());
        if (request.getIsProjectBx() == null || !request.getIsProjectBx()){
            if (order.getOrderscrtype() != null && (order.getOrderscrtype() == 4 || order.getOrderscrtype() == 1)) {
                return "稿费/项目报销单不允许修改或者提交！";
            }
        }
        if (-1 == order.getReuqeststatus() || 0 == order.getReuqeststatus()) {
            return null;
        }
        return "该报销单非草稿或退回状态，不允许修改！";
    }


    /**
     * 提交校验：
     * 按照报销单主键查看数据库是否存在：
     * ​	存在： 表单状态校验(是否提交)  -> 通用校验（可能存在数据修改）-> 相似校验(非本报销单) -> 动因和科目校验 -> 保存或修改 -> 提交
     * ​	不存在：通用校验 -> 相似校验 -> 动因和科目校验 -> 保存或修改 -> 提交
     *
     * @param request
     * @return
     * @throws Exception
     */
    public String submitValidate(ReimbursementRequest request) throws Exception {
        String result = null;
        //查看是否存在
        boolean exist = this.orderService.existById(request.getOrder());
        if (exist) {
            result = this.formStatusValidate(request);
            if (StringUtils.isNotEmpty(result)) {
                return result;
            }
        }
        result = this.baseValidate(request);
        if (StringUtils.isNotEmpty(result)) {
            return result;
        }
        result = this.likeValidate(request);
        if (StringUtils.isNotEmpty(result)) {
            return result;
        }
        //冲账校验
        result = this.paymentValidate(request);
        if (StringUtils.isNotEmpty(result)) {
            return result;
        }
        //校验非划拨动因
        BudgetReimbursementorder order = request.getOrder();
        Long orderId = order.getId() == null ? -99999L : request.getOrder().getId();

        //赋值行号
        for (int i = 0; i < request.getOrderDetail().size(); i++) {
            request.getOrderDetail().get(i).setRow(i+1);
        }

        //需要校验的报销明细（计入执行）
        List<BudgetReimbursementorderDetail> validatedDetails = request.getOrderDetail().stream().filter(BudgetReimbursementorderDetail::getReimflag).collect(Collectors.toList());
        /**
         * add by minzhq
         * 报销明细和划拨明细中不能存在重复的月度动因。
         */
        Map<Long, Long> map = validatedDetails.stream().collect(Collectors.groupingBy(BudgetReimbursementorderDetail::getMonthagentid, Collectors.counting()));
        long count = request.getOrderAllocated().stream().filter(e -> map.get(e.getMonthagentid()) != null && map.get(e.getMonthagentid()) > 0).count();
        if (count > 0) throw new RuntimeException("请保证划拨明细中与报销明细中动因不重复！");

        /**
         * 增加固定资产报销校验
         * add by minzhq
         */
        if(request.getIsFixAsset()!=null && request.getIsFixAsset() && !CollectionUtils.isEmpty(request.getAssetLockedSubjectList())){
            List<Long> monthAgentIds = request.getAssetLockedSubjectList().stream().map(ReimbursementRequest.AssetSubjectMsg::getMonthAgentId).collect(Collectors.toList());
            if(!monthAgentIds.isEmpty()){
                Map<Long, BudgetMonthAgent> monthAgentMap = this.monthAgentMapper.selectBatchIds(monthAgentIds).stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
                request.getAssetLockedSubjectList().forEach(e->{
                    BudgetMonthAgent budgetMonthAgent = monthAgentMap.get(e.getMonthAgentId());
                    e.setMonthId(budgetMonthAgent.getMonthid());
                    e.setSubjectId(budgetMonthAgent.getSubjectid());
                    e.setYearAgentId(budgetMonthAgent.getYearagentid());
                    e.setUnitId(budgetMonthAgent.getUnitid());
                });
            }
        }
        List<String> errorMsgList = this.agentAndSubjectValidate(BudgetReimbursementorderDetail.class, "monthagentid", "reimmoney",
                validatedDetails, orderId, order.getYearid(), order.getMonthid(), order.getUnitid(), "报销详情",
                request.getIsFixAsset(),
                request.getAssetLockedSubjectList());
        //校验划拨动因
        List<String> allocatedErrorMsgList = this.allocatedAgentAndSubjectValidate(request, orderId);
        if (!CollectionUtils.isEmpty(allocatedErrorMsgList)) {
            errorMsgList.addAll(allocatedErrorMsgList);
        }
        if (!CollectionUtils.isEmpty(errorMsgList)) {
            return errorMsgList.stream().distinct().collect(Collectors.joining("<br>"));
        }
        return result;
    }

    /**
     * 冲账校验
     * 借款已经被使用（锁定）不能冲账
     * 借款不存在不能冲账
     * 借款未生效不能冲账
     * 项目借款未达标不能冲账
     * 借款待还款金额小于冲账金额不能冲账
     *
     * @param request
     * @return
     * @throws Exception
     */
    private String paymentValidate(ReimbursementRequest request) throws Exception {
        List<BudgetReimbursementorderPayment> payments = request.getOrderPayment();
        if (payments == null || payments.size() == 0) {
            return null;
        }
        List<Long> lendmoneyIds = request.getOrderPayment().stream().map(BudgetReimbursementorderPayment::getLendmoneyid).collect(Collectors.toList());
        String ids = JdbcTemplateService.getInSql(lendmoneyIds, null);
        List<LendmoneyUseBean> list = this.lendmoneyService.getUseInfo(ids);
        if (list == null || list.size() == 0) {
            return "借款单不存在！";
        }
        //映射map
        Map<Long, LendmoneyUseBean> useInfoMap = new HashMap<Long, LendmoneyUseBean>(list.size());
        list.forEach(ele -> useInfoMap.put(ele.getId(), ele));
        //单个校验
        int current = 1;
        LendmoneyUseBean bean = null;
        for (BudgetReimbursementorderPayment payment : payments) {
            bean = useInfoMap.get(payment.getLendmoneyid());
            if (bean == null)
                return "冲账数据的第【" + current + "】行所用借款【" + payment.getLendcode() + "," + payment.getLendmoneyname() + "】不存在......";
            if (bean.getUsecount() > 0 && !bean.getId().equals(payment.getLendmoneyid()))
                return "冲账数据的第【" + current + "】行所用借款【" + payment.getLendcode() + "," + payment.getLendmoneyname() + "】已经被锁定......";
            if (1 != bean.getEffectflag())
                return "冲账数据的第【" + current + "】行所用借款【" + payment.getLendcode() + "," + payment.getLendmoneyname() + "】未生效......";
            //if(4== bean.getLendtype() && null != bean.getFlushingflag() && 1 != bean.getFlushingflag()) return "冲账数据的第【"+current+"】行所用借款【"+payment.getLendcode()+","+payment.getLendmoneyname()+"】未达标......";
            if (bean.getUnrepaymoney().compareTo(payment.getPaymentmoney()) < 0)
                return "冲账数据的第【" + current + "】行所用借款【" + payment.getLendcode() + "," + payment.getLendmoneyname() + "】未还金额为【" + bean.getUnrepaymoney().stripTrailingZeros().toPlainString() + "】......";
            current++;
        }
        return null;
    }

    /**
     * 动因校验
     *
     * @param <T>
     * @param clazz
     * @param agentFieldName
     * @param moneyFieldName
     * @param list
     * @param orderId
     * @param yearId
     * @param monthId
     * @param unitId
     * @return
     * @throws Exception
     */
    public <T> List<String> agentAndSubjectValidate(Class<T> clazz, String agentFieldName, String moneyFieldName, List<T> list, Long orderId, Long yearId, Long monthId, Long unitId, String mainTip,Boolean isFixAsset,List<ReimbursementRequest.AssetSubjectMsg> assetLockedSubjectList) throws Exception {
        if (list.isEmpty()) return Lists.newArrayList();
        List<String> resultErrorMsgList = new ArrayList<>();
        //取出动因
        List<Long> monthAgentIds = ClassTools.getFieldValueList(list, agentFieldName, Long.class);
        //获取所有受年度限制的动因
        List<Long> agentIds = this.orderService.getControlAgentId(monthAgentIds, "yearcontrolflag");
        //年度动因:本次报销动因金额 <= 年度动因预算 + 本动因本届拆进金额 + 本动因本届追加金额 - 本动因本届拆出金额 - 本届本动因已报销总额（审核通过计入执行 + 审核通过划拨） - 锁定的本动因之和（锁定计入执行 + 锁定划拨）  --校验通过
        List<Long> tempAgentIds = agentIds;
        if (agentIds != null && agentIds.size() > 0) {
            Map<String, Map<String, BigDecimal>> map = this.orderService.getValidateInfo(orderId, yearId, monthId, unitId, agentIds, 1,isFixAsset,assetLockedSubjectList);
            /**
             * add by minzhq
             * 需把不需要预算控制的数据去除。
             */
            List<T> validateList = list.stream().filter(e -> {
                if (e instanceof BudgetReimbursementorderDetail) {
                    BudgetReimbursementorderDetail detail = (BudgetReimbursementorderDetail) e;
                    return tempAgentIds.contains(detail.getMonthagentid());
                } else if (e instanceof BudgetReimbursementorderAllocated) {
                    BudgetReimbursementorderAllocated detail = (BudgetReimbursementorderAllocated) e;
                    return tempAgentIds.contains(detail.getMonthagentid());
                }
                return true;
            }).collect(Collectors.toList());
            List<String> errorMsgList = ReimbursementRequest.agentValidate(clazz, validateList, "yearagentid", moneyFieldName, map, mainTip + "年度动因校验不通过","row");
            if (!CollectionUtils.isEmpty(errorMsgList)) {
                resultErrorMsgList.addAll(errorMsgList);
            }
        }
        //获取所有受月度科目限制的动因
        agentIds = this.orderService.getControlAgentId(monthAgentIds, "monthcontrolflag");
        List<Long> tempAgentIds1 = agentIds;
        //月度科目:本报销单同科目下动因报销金额之和 <= 本科目本月所有动因预算之和 + 本月本科目所有动因追加之和 + 本科目本月所有动因拆进金额之和 - 本月本科目所有动因拆出金额之和 - 本科目本月已报销金额之和（审核通过计入执行 + 审核通过划拨）- 本科目本月已锁定金额（锁定计入执行 + 锁定划拨） --校验通过
        if (agentIds != null && agentIds.size() > 0) {
            Map<String, Map<String, BigDecimal>> map = this.orderService.getValidateInfo(orderId, yearId, monthId, unitId, agentIds, 2,isFixAsset,assetLockedSubjectList);
            /*
             * add by minzhq
             * 需把不需要预算控制的数据去除。
             */
            List<T> validateList = list.stream().filter(e -> {
                if (e instanceof BudgetReimbursementorderDetail) {
                    BudgetReimbursementorderDetail detail = (BudgetReimbursementorderDetail) e;
                    return tempAgentIds1.contains(detail.getMonthagentid());
                } else if (e instanceof BudgetReimbursementorderAllocated) {
                    BudgetReimbursementorderAllocated detail = (BudgetReimbursementorderAllocated) e;
                    return tempAgentIds1.contains(detail.getMonthagentid());
                }
                return true;
            }).collect(Collectors.toList());
            List<String> errorMsgList = ReimbursementRequest.agentValidate(clazz, validateList, "monthSubjectKey", moneyFieldName, map, mainTip + "月度科目校验不通过","row");
            if (!CollectionUtils.isEmpty(errorMsgList)) {
                resultErrorMsgList.addAll(errorMsgList);
            }
        }
        //获取所有受年度科目限制的动因
        agentIds = this.orderService.getControlAgentId(monthAgentIds, "yearsubjectcontrolflag");
        List<Long> tempAgentIds2 = agentIds;
        //年度科目:本报销单科目金额 <= 本科目本年所有动因预算之和 + 本年本科目所有动因追加之和 + 本科目本年所有动因拆进金额之和 - 本年本科目所有动因拆出金额之和 - 本科目本年已报销金额之和（审核通过计入执行 + 审核通过划拨）- 本科目本年已锁定金额（锁定计入执行 + 锁定划拨） --校验通过
        if (agentIds != null && agentIds.size() > 0) {
            Map<String, Map<String, BigDecimal>> map = this.orderService.getValidateInfo(orderId, yearId, monthId, unitId, agentIds, 3,isFixAsset,assetLockedSubjectList);
            /**
             * add by minzhq
             * 需把不需要预算控制的数据去除。
             */
            List<T> validateList = list.stream().filter(e -> {
                if (e instanceof BudgetReimbursementorderDetail) {
                    BudgetReimbursementorderDetail detail = (BudgetReimbursementorderDetail) e;
                    return tempAgentIds2.contains(detail.getMonthagentid());
                } else if (e instanceof BudgetReimbursementorderAllocated) {
                    BudgetReimbursementorderAllocated detail = (BudgetReimbursementorderAllocated) e;
                    return tempAgentIds2.contains(detail.getMonthagentid());
                }
                return true;
            }).collect(Collectors.toList());
            List<String> errorMsgList = ReimbursementRequest.agentValidate(clazz, validateList, "yearSubjectKey", moneyFieldName, map, mainTip + "年度科目校验不通过","row");
            if (!CollectionUtils.isEmpty(errorMsgList)) {
                resultErrorMsgList.addAll(errorMsgList);
            }
        }
        return resultErrorMsgList;
    }

    /**
     * 校验划拨动因
     *
     * @param request
     * @param orderId
     * @return
     * @throws Exception
     */
    private List<String> allocatedAgentAndSubjectValidate(ReimbursementRequest request, Long orderId) throws Exception {
        List<BudgetReimbursementorderAllocated> list = request.getOrderAllocated();
        if (list != null && list.size() > 0) {
            BudgetReimbursementorder order = request.getOrder();
            Map<Long, BudgetMonthAgent> monthAgentMap = monthAgentMapper.selectBatchIds(list.stream().map(BudgetReimbursementorderAllocated::getMonthagentid).collect(Collectors.toList())).stream().collect(Collectors.toMap(e -> e.getId(), Function.identity()));
            for (int i = 0; i < list.size(); i++) {
                BudgetReimbursementorderAllocated ele = list.get(i);
                BudgetMonthAgent bma = monthAgentMap.get(ele.getMonthagentid());
                ele.setYearagentid(bma.getYearagentid());
                ele.setSubjectid(bma.getSubjectid());
                ele.setMonthSubjectKey(bma.getUnitid() + "-" + bma.getSubjectid());
                ele.setYearSubjectKey(bma.getUnitid() + "-" + bma.getSubjectid());
                ele.setRow(i + 1);
            }

            Set<Entry<Long, List<BudgetReimbursementorderAllocated>>> entrySet = list.stream().collect(Collectors.groupingBy(BudgetReimbursementorderAllocated::getUnitid)).entrySet();
            List<String> resultErrorMsgList = new ArrayList<>();
            for (Entry<Long, List<BudgetReimbursementorderAllocated>> es : entrySet) {
                List<BudgetReimbursementorderAllocated> allocatedList = es.getValue();
                for (BudgetReimbursementorderAllocated ele : allocatedList) {
                    List<String> errorMsgList = this.agentAndSubjectValidate(BudgetReimbursementorderAllocated.class, "monthagentid", "allocatedmoney", allocatedList, orderId, order.getYearid(), order.getMonthid(), ele.getUnitid(), "划拨详情",request.getIsFixAsset(),
                            request.getAssetLockedSubjectList());
                    if (!CollectionUtils.isEmpty(errorMsgList)) {
                        resultErrorMsgList.addAll(errorMsgList);
                        /*Pattern p = Pattern.compile("-?\\d+");
                        return errorMsgList.stream().map(r->{
                            Matcher matcher = p.matcher(r);
                            boolean find = matcher.find();
                            if (find) {
                                return r.replaceFirst(matcher.group(0), String.valueOf(ele.getIndex()));
                            }
                            return r;
                        }).collect(Collectors.toList());*/

                    }
                }
            }
            if (!CollectionUtils.isEmpty(resultErrorMsgList))
                return resultErrorMsgList;
        }
        return null;
    }
}
