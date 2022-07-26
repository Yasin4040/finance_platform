package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.ecology.EcologyClient;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.ecology.EcologyWorkFlowValue;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.enmus.LendTypeEnum;
import com.jtyjy.finance.manager.enmus.PaymoneyStatusEnum;
import com.jtyjy.finance.manager.enmus.PaymoneyTypeEnum;
import com.jtyjy.finance.manager.mapper.*;
import com.jtyjy.finance.manager.trade.DistributedNumber;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.*;
import com.xxl.job.core.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetContractService extends DefaultBaseService<BudgetContractMapper, BudgetContract> {

    private final TabChangeLogMapper loggerMapper;
    private final BudgetContractMapper budgetContractMapper;
    private final BudgetLendmoneyMapper budgetLendmoneyMapper;
    private final BudgetPaymoneyMapper budgetPaymoneyMapper;
    private final BudgetBillingUnitMapper budgetBillingUnitMapper;
    private final WbBanksMapper wbBanksMapper;
    private final WbUserMapper wbUserMapper;

    private final DistributedNumber distributedNumber;

    @Override
    public BaseMapper<TabChangeLog> getLoggerMapper() {
        return loggerMapper;
    }

    @Override
    public void setBaseLoggerBean() {
        DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_contract"));
    }

    /**
     * 查询合同列表（分页）
     */
    public PageResult<BudgetContractVO> listContractPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {
        Page<BudgetContractVO> pageBean = new Page<>(page, rows);
        List<BudgetContractVO> resultList = this.budgetContractMapper.listContractPage(pageBean, paramMap);
        resultList.forEach(v -> {
            // 如果未冲账金额为空, 未冲账金额即为合同金额
            if (v.getUnRepaidMoney() == null) {
                v.setUnRepaidMoney(v.getContractMoney());
            }
            // 如果已支付金额为空, 未支付金额即为合同金额
            if (v.getPaidMoney() == null) {
                v.setUnPaidMoney(v.getContractMoney());
            } else {
                v.setUnPaidMoney(v.getContractMoney().subtract(v.getPaidMoney()));
            }
        });
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * 终止合同
     */
    public void stopContract(Long id) {
        BudgetContract budgetContract = this.budgetContractMapper.selectById(id);
        if (budgetContract == null) {
            throw new RuntimeException("合同不存在");
        }
        if (budgetContract.getTerminationflag() == 1) {
            throw new RuntimeException("合同 【" + budgetContract.getContractname() + "】已经终止!");
        }
        BudgetContract updateContract = new BudgetContract();
        updateContract.setId(budgetContract.getId());
        updateContract.setTerminationflag(1);
        updateContract.setTerminationdate(new Date());
        this.budgetContractMapper.updateById(updateContract);
    }

    /**
     * 合同付款明细
     */
    public PageResult<BudgetPayMoneyDetailVO> getPayMoneyDetail(Integer page, Integer rows, Long id) {
        Page<BudgetPayMoneyDetailVO> pageBean = new Page<>(page, rows);
        HashMap<String, Object> paramMap = new HashMap<>(2);
        paramMap.put("contractId", id);
        List<BudgetPayMoneyDetailVO> payMoneyDetail = this.budgetLendmoneyMapper.getPayMoneyDetail(pageBean, paramMap);
        payMoneyDetail.forEach(v -> v.setLendTypeDesc(LendTypeEnum.getValue(v.getLendType())));
        return PageResult.apply(pageBean.getTotal(), payMoneyDetail);
    }

    /**
     * 合同冲账明细
     */
    public PageResult<BudgetStrikeMoneyDetailVO> getStrikeMoneyDetail(Integer page, Integer rows, Long id) {
        Page<BudgetStrikeMoneyDetailVO> pageBean = new Page<>(page, rows);
        List<BudgetStrikeMoneyDetailVO> resultList = this.budgetContractMapper.getStrikeMoneyDetail(pageBean, id);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * 查询合同借款列表（分页）
     */
    public PageResult<BudgetContractLendVO> listContractLendPage(Integer page, Integer rows, HashMap<String, Object> paramMap) {
        Page<BudgetContractLendVO> pageBean = new Page<>(page, rows);
        List<BudgetContractLendVO> resultList = this.budgetContractMapper.listContractLendPage(pageBean, paramMap);
        return PageResult.apply(pageBean.getTotal(), resultList);
    }

    /**
     * 合同签订
     */
    public void contractSigning(EcologyParams params) {
        String requestId = params.getRequestid();
        Integer count = this.budgetLendmoneyMapper.selectCount(new QueryWrapper<BudgetLendmoney>().eq("requestid", requestId));
        if (count > 0) {
            throw new RuntimeException("合同签订归档错误!");
        }

        EcologyWorkFlowValue value = EcologyClient.getWorkflowValue(params);
        Map<String, String> mainTableValue = value.getMaintablevalue();

        // 申请人
        String applyPerson = mainTableValue.get("sqr");
        // 合同名称
        String contractName = mainTableValue.get("htmc");
        // 合同编号
        String contractCode = mainTableValue.get("htbh");
        // 合同类型
        String contractType = mainTableValue.get("htlx_new").split("_")[1];
        // 合同/预估金额
        String contractAmtStr = mainTableValue.get("htje");
        // 签订时间
        String signDateStr = mainTableValue.get("qdsj");
        // 对方单位名称
        String oppoCompany = mainTableValue.get("dfdwmc");
        // 签订份数
        String signCopies = mainTableValue.get("qjfs");
        // 合同分配
        String contractDistribute = mainTableValue.get("htfp");
        // 地址和电话
        String addrTel = mainTableValue.get("txdzjdh");
        // 约定结算方式
        String agreeSumType = mainTableValue.get("htydjsfs");
        // 合同内容摘要
        String contextDigest = mainTableValue.get(" htnrzy");

        if (StringUtils.isNotBlank(agreeSumType)) {
            agreeSumType = agreeSumType.replace("&nbsp;", " ");
        }

        BudgetContract budgetContract = new BudgetContract();
        budgetContract.setContractname(contractName);
        budgetContract.setContractcode(contractCode);
        budgetContract.setContracttype(contractType);
        budgetContract.setContractmoney(Float.parseFloat(contractAmtStr));
        try {
            budgetContract.setSigndate(Constants.FORMAT_10.parse(signDateStr));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("签订日期时间格式转换出错！");
        }
        budgetContract.setContractcopies(signCopies);
        budgetContract.setCreatetime(new Date());
        budgetContract.setTerminationflag(0);
        budgetContract.setAgreesumtype(agreeSumType);
        budgetContract.setContextdigest(contextDigest);
        budgetContract.setOtherpartyunit(oppoCompany);
        budgetContract.setRequestid(requestId);

        // 拼接一些额外信息
        budgetContract.setOtherinfo("申请人：" + applyPerson + ", 对方地址和电话：" + addrTel + ", 合同分配：" + contractDistribute);
        this.budgetContractMapper.insert(budgetContract);
    }

    /**
     * 合同借款
     */
    public void contractLendMoney(EcologyParams params) {
        String requestId = params.getRequestid();
        Integer count = this.budgetLendmoneyMapper.selectCount(new QueryWrapper<BudgetLendmoney>().eq("requestid", requestId));
        if (count > 0) {
            throw new RuntimeException("合同借款归档错误!");
        }

        EcologyWorkFlowValue value = EcologyClient.getWorkflowValue(params);
        Map<String, String> mainTableValue = value.getMaintablevalue();
        Map<String, List<Map<String, String>>> detailTableValues = value.getDetailtablevalues();

        // 工号
        String empNo = mainTableValue.get("gh");
        if (StringUtils.isNotBlank(empNo) && empNo.length() > 5) {
            empNo = empNo.substring(0, 5);
        }
        // 经办日期
        String lendDateStr = mainTableValue.get("jbrq");
        // 预计报销日期
        String planDateStr = mainTableValue.get("yjbxrq");
        // 付款届别Id
        String yearId = mainTableValue.get("bxjb");
        // 合同Id
        String contractNameId = mainTableValue.get("htmc");
        // 合同总额
        String nonContractAmtStr = mainTableValue.get("xmzje");
        // 付款事由
        String payRemark = mainTableValue.get("fksy");
        // 支付方式  0 现金；1 转账
        String payType = mainTableValue.get("yqzffs");
        // 流程编号
        String requestCode = mainTableValue.get("lcbh");

        // 是否签订合同  0 是 ，1 否
        String isContract = mainTableValue.get("sfqdht");
        // 非合同名称
        String nonContractName = mainTableValue.get("xmmc");

        if (StringUtils.isNotBlank(payRemark)) {
            payRemark = payRemark.replace("&nbsp;", " ");
        }

        int lendType;
        BudgetContract budgetContract;
        if ("1".equals(isContract)) {
            // 项目借款需要保存项目信息, 要求不能和已存在的重名
            // update minzhq 2021-09-15  取消这个验证
            //Integer exist = this.budgetContractMapper.selectCount(new QueryWrapper<BudgetContract>().eq("contractname", nonContractName));
            //if (exist > 0) {
            //    throw new RuntimeException("已存在名为【" + nonContractName + "】的项目，请重新取名！");
            //}
            budgetContract = new BudgetContract();
            budgetContract.setContractname(nonContractName);
            budgetContract.setContractmoney(Float.parseFloat(nonContractAmtStr));
            budgetContract.setCreatetime(new Date());
            budgetContract.setTerminationflag(0);
            budgetContract.setRequestid(requestId);
            budgetContract.setOtherinfo(payRemark);
            this.budgetContractMapper.insert(budgetContract);

            lendType = LendTypeEnum.LEND_TYPE_16.getType();
        } else {
            budgetContract = this.budgetContractMapper.selectById(Long.parseLong(contractNameId));
            if (budgetContract == null) {
                throw new RuntimeException("该合同不存在");
            }
            lendType = LendTypeEnum.LEND_TYPE_15.getType();
        }

        WbUser user = this.wbUserMapper.selectOne(new QueryWrapper<WbUser>().eq("user_name", empNo));
        if (user == null) {
            throw new RuntimeException("工号【" + empNo + "】用户不存在");
        }

        if (detailTableValues != null && !detailTableValues.isEmpty()) {
            // 获取明细表数据(一个发放单位对应一个收款单位，有多条记录)
            String finalPayRemark = payRemark;
            detailTableValues.forEach((str, values) -> {
                values.forEach(detail -> {
                    // 收款单位信息：(外部单位)
                    String gatherId = detail.get("wbdw");
                    String gatherAccountId = detail.get("wbdwyhzh");
                    String openBank = detail.get("khh");
                    String je = detail.get("je");
                    WbBanks bank = this.wbBanksMapper.selectByAccountId(gatherAccountId);
                    if (bank == null) {
                        throw new RuntimeException("收款银行账户【" + gatherAccountId + "】不存在或已停用");
                    }

                    // 付款单位信息：付款单位和单位账户不是联动关系，单位账户没填的时候就以选择的付款单位的卡号为准，选了卡号的以卡号为准
                    String payUnitId = detail.get("kpdw");
                    String payAccount = detail.get("kpdwyhzh");
                    String payOpenBank = detail.get("kpdwkhh");
                    WbBanks unitBank;
                    if (StringUtils.isBlank(payAccount)) {
                        List<WbBanks> unitBanks = this.wbBanksMapper.selectByBillingUnitId(payUnitId);
                        if(CollectionUtils.isEmpty(unitBanks)){
                            throw new RuntimeException("付款单位【" + payUnitId + "】不存在或已停用");
                        }
                        unitBank = unitBanks.get(0);
                    } else {
                        unitBank = this.wbBanksMapper.selectByUnitAccount(payAccount);
                    }
                    if (unitBank == null) {
                        throw new RuntimeException("付款单位账户【" + payAccount + "】不存在或已停用");
                    }

                    // 借款单(合同借款为外部人员借款与经办人的关系，实际上并不是经办人的借款)
                    BudgetLendmoney lendMoney = new BudgetLendmoney();
                    lendMoney.setEmpno(bank.getBankCode());
                    lendMoney.setEmpname(bank.getAccountName());
                    lendMoney.setOperatorEmpId(user.getUserId());
                    lendMoney.setOperatorEmpNo(user.getUserName());
                    lendMoney.setOperatorEmpName(user.getDisplayName());
                    lendMoney.setLendmoneycode(this.distributedNumber.getLendNum());
                    lendMoney.setLendmoney(new BigDecimal(je));
                    lendMoney.setContractid(budgetContract.getId());
                    lendMoney.setLendtype(lendType);
                    lendMoney.setRepaidmoney(BigDecimal.ZERO);
                    try {
                        lendMoney.setLenddate(Constants.FORMAT_10.parse(lendDateStr));
                        lendMoney.setPlanpaydate(Constants.FORMAT_10.parse(planDateStr));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        throw new RuntimeException("时间格式转换出错！");
                    }
                    lendMoney.setCreatetime(new Date());
                    lendMoney.setRemark(finalPayRemark);
                    lendMoney.setRequestid(requestId);
                    lendMoney.setDeleteflag(false);
                    lendMoney.setYearid(Long.valueOf(yearId));
                    lendMoney.setInterestmoney(BigDecimal.ZERO);
                    lendMoney.setFlushingflag(false);
                    lendMoney.setEffectflag(true);
                    lendMoney.setChargebillflag(false);
                    lendMoney.setMakeaccountflag(false);
                    lendMoney.setRequestcode(requestCode);
                    this.budgetLendmoneyMapper.insert(lendMoney);

                    // 生成付款单
                    BudgetPaymoney payMoney = new BudgetPaymoney();
                    payMoney.setId(null);
                    payMoney.setPaymoneytype(PaymoneyTypeEnum.LEND_PAY.type);
                    payMoney.setPaymoneystatus(PaymoneyStatusEnum.RECEIVE_PAY.type);
                    payMoney.setReceivetime(new Date());
                    payMoney.setPaymoney(new BigDecimal(je));
                    payMoney.setLendtype(lendMoney.getLendtype());
                    payMoney.setPaytype(Integer.parseInt(payType));
                    payMoney.setMonth(Constants.FORMAT_6.format(new Date()));
                    payMoney.setCreatetime(new Date());
                    payMoney.setBunitname(unitBank.getBillingUnitName());
                    payMoney.setBunitbankaccount(unitBank.getBankAccount());
                    payMoney.setBunitaccountbranchcode(unitBank.getSubBranchCode());
                    payMoney.setBunitaccountbranchname(unitBank.getBankName());

                    BudgetBillingUnit budgetBillingUnit = this.budgetBillingUnitMapper.selectById(payUnitId);
                    if (budgetBillingUnit != null) {
                        payMoney.setBunitname(budgetBillingUnit.getName());
                    }

                    payMoney.setBankaccount(bank.getBankAccount());
                    payMoney.setBankaccountbranchcode(bank.getSubBranchCode());
                    payMoney.setBankaccountbranchname(bank.getBankName());
                    payMoney.setBankaccountname(bank.getAccountName());
                    payMoney.setOpenbank(openBank);
                    payMoney.setPaymoneycode(this.distributedNumber.getPaymoneyNum());
                    payMoney.setPaymoneyobjectid(lendMoney.getId());
                    payMoney.setPaymoneyobjectcode(lendMoney.getLendmoneycode());
                    this.budgetPaymoneyMapper.insert(payMoney);
                });
            });
        }
    }

    public List<BudgetContractExcelVO> listContract(HashMap<String, Object> paramMap) {
        List<BudgetContractVO> resultList = this.budgetContractMapper.listContractPage(null, paramMap);
        List<BudgetContractExcelVO> vos = new ArrayList<>();
        resultList.forEach(v -> {
            // 如果未冲账金额为空, 未冲账金额即为合同金额
            if (v.getUnRepaidMoney() == null) {
                v.setUnRepaidMoney(v.getContractMoney());
            }
            // 如果已支付金额为空, 未支付金额即为合同金额
            if (v.getPaidMoney() == null) {
                v.setUnPaidMoney(v.getContractMoney());
            } else {
                v.setUnPaidMoney(v.getContractMoney().subtract(v.getPaidMoney()));
            }
            BudgetContractExcelVO vo = new BudgetContractExcelVO();
            BeanUtils.copyProperties(v,vo);
            if(0==v.getTerminationFlag()){
                vo.setTerminationFlagName("进行中");
            }else if(1==v.getTerminationFlag()){
                vo.setTerminationFlagName("已终止");
            }
            vos.add(vo);
        });
        return vos;
    }

    public void exportContractLend(HashMap<String, Object> paramMap, HttpServletResponse response) throws Exception {
        List<BudgetContractLendVO> resultList = this.budgetContractMapper.listContractLendPage(null, paramMap);
        List<List<String>> dataList = new ArrayList<>();
        for (BudgetContractLendVO vo : resultList) {
            List<String> colList = new ArrayList<>();
            colList.add(vo.getRepaymentStatus()?"已付清":"未付清");
            colList.add(vo.getLendMoneyCode());
            String payMoneyStatusName = "";
            if(0==vo.getPayMoneyStatus()){
                payMoneyStatusName = "等待付款";
            }else if(1==vo.getPayMoneyStatus()){
                payMoneyStatusName = "接收付款";
            }else if(2==vo.getPayMoneyStatus()){
                payMoneyStatusName = "正在付款";
            }else if(3==vo.getPayMoneyStatus()){
                payMoneyStatusName = "已经付款";
            }
            colList.add(payMoneyStatusName);
            colList.add(vo.getEmpNo());
            colList.add(vo.getEmpName());
            colList.add(vo.getLendMoney().toString());
            colList.add(vo.getRepaidMoney().toString());
            colList.add(vo.getUnRepaidMoney().toString());
            colList.add(DateUtil.format(vo.getLendDate(),"yyyy-MM-dd"));
            if(Objects.isNull(vo.getPayPlanDate())){
                colList.add("");
            }else{
                colList.add(DateUtil.format(vo.getPayPlanDate(),"yyyy-MM-dd"));
            }
            colList.add(vo.getContractName());
            colList.add(vo.getAgreeSumType());
            colList.add(vo.getRemark());
            dataList.add(colList);
        }
        ResponseUtil.exportContractLend(dataList,  EasyExcelUtil.getOutputStream("导出合同支出", response));

    }
}
