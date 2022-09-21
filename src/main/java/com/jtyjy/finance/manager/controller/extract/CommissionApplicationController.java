package com.jtyjy.finance.manager.controller.extract;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.auth.anno.ApiDataAuthAnno;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetExtractFeePayDetailBeforeCal;
import com.jtyjy.finance.manager.bean.BudgetExtractImportdetail;
import com.jtyjy.finance.manager.bean.BudgetExtractsum;
import com.jtyjy.finance.manager.converter.CommissionConverter;
import com.jtyjy.finance.manager.dto.commission.CommissionDetailsImportDTO;
import com.jtyjy.finance.manager.dto.commission.FeeImportErrorDTO;
import com.jtyjy.finance.manager.dto.commission.IndividualIssueExportDTO;
import com.jtyjy.finance.manager.dto.individual.IndividualImportErrorDTO;
import com.jtyjy.finance.manager.easyexcel.EasyExcelImportListener;
import com.jtyjy.finance.manager.enmus.ExtractUserTypeEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.query.commission.FeeQuery;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationService;
import com.jtyjy.finance.manager.service.BudgetExtractImportdetailService;
import com.jtyjy.finance.manager.service.BudgetExtractsumService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.ExtractImportDetailVO;
import com.jtyjy.finance.manager.vo.application.BudgetSubjectVO;
import com.jtyjy.finance.manager.vo.application.CommissionApplicationInfoUpdateVO;
import com.jtyjy.finance.manager.vo.application.CommissionApplicationInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jtyjy.finance.manager.constants.Constants.IMPORT_FEE;
import static com.jtyjy.finance.manager.constants.Constants.IMPORT_INDIVIDUAL_TICKET;

/**
 * Description: 支付申请单
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 14:36
 */
@Api(tags = {"提成支付申请单"})
@RestController
@RequestMapping("/api/commissionApplication")
@Slf4j
public class CommissionApplicationController {
    private final BudgetExtractCommissionApplicationService applicationService;
    private final BudgetExtractsumService extractsumService;
    private final BudgetExtractImportdetailService importDetailService;
    private final RedisClient redisClient;
    private final BudgetYearPeriodMapper yearMapper;
    private final CuratorFramework curatorFramework;
    private final static String IMPORT_TYPE = "tc";
    public final static String TCIMPORT = "TCIMPORT";

    @Value("${file.shareDir}")
    private String fileShareDir;

    @Value("${redis.file.key.expiretime}")
    private Integer expireTime;

    public CommissionApplicationController(BudgetExtractCommissionApplicationService applicationService, BudgetExtractsumService extractsumService, BudgetExtractImportdetailService importDetailService, RedisClient redisClient, BudgetYearPeriodMapper yearMapper, CuratorFramework curatorFramework) {
        this.applicationService = applicationService;
        this.extractsumService = extractsumService;
        this.importDetailService = importDetailService;
        this.redisClient = redisClient;
        this.yearMapper = yearMapper;
        this.curatorFramework = curatorFramework;
    }


//    @ApiOperation(value = "获取提成主数据列表", httpMethod = "GET")
//    @ApiImplicitParams(value = {
//            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
//            @ApiImplicitParam(value = "导航栏查询条件", name = "query", dataType = "String"),
//            @ApiImplicitParam(value = "单据状态", name = "status", dataType = "Integer"),
//            @ApiImplicitParam(value = "提成单号", name = "code", dataType = "String"),
//            @ApiImplicitParam(value = "预算单位", name = "unitname", dataType = "String"),
//            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
//            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer")
//    })
//    @GetMapping("/getExtractInfoList")
//    public ResponseEntity<PageResult<ExtractInfoVO>> getExtractInfoList(@RequestParam(name = "query") String query,
//                                                                        @RequestParam(defaultValue = "1") Integer page,
//                                                                        @RequestParam(defaultValue = "20") Integer rows,
//                                                                        @RequestParam(name = "status", required = false) Integer status,
//                                                                        @RequestParam(name = "code", required = false) String code,
//                                                                        @RequestParam(name = "unitname", required = false) String unitname) {
//        try {
//            if (StringUtils.isBlank(query)) throw new RuntimeException("请先选择导航栏的一个参数。");
//            Map<String, Object> params = new HashMap<>();
//            params.put("query", query);
//            params.put("status", status);
//            params.put("code", code);
//            params.put("unitname", unitname);
//            PageResult<ExtractInfoVO> pageList = extractsumService.getExtractInfoList(params, page, rows);
//            return ResponseEntity.ok(pageList);
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error(e.getMessage(), e);
//            return ResponseEntity.error(e.getMessage());
//        }
//    }
@ApiOperation(value = "获取导入明细列表", httpMethod = "GET")
@GetMapping("/getExtractImportDetails")
public ResponseEntity<PageResult<ExtractImportDetailVO>> getExtractImportDetails(@RequestParam(name = "id", required = true) Long sumId,
                                                                                 @RequestParam(defaultValue = "1") Integer page,
                                                                                 @RequestParam(defaultValue = "20") Integer rows,
                                                                                 @RequestParam(name = "yearid", required = false) Long yearid,
                                                                                 @RequestParam(name = "iscompanyemp", required = false) Integer iscompanyemp,
                                                                                 @RequestParam(name = "isbaddebt", required = false) Integer isbaddebt,
                                                                                 @RequestParam(name = "empno", required = false) String empno,
                                                                                 @RequestParam(name = "idnumber", required = false) String idnumber) {


    try {
        Map<String, Object> params = new HashMap<>();
        params.put("sumId", sumId);
        params.put("yearid", yearid);
        params.put("iscompanyemp", iscompanyemp);
        params.put("isbaddebt", isbaddebt);
        params.put("empno", empno);
        params.put("idnumber", idnumber);
        PageResult<ExtractImportDetailVO> pageList = extractsumService.getExtractImportDetails(params, page, rows);
        return ResponseEntity.ok(pageList);
    } catch (Exception e) {
        e.printStackTrace();
        log.error(e.getMessage(), e);
        return ResponseEntity.error(e.getMessage());
    }

}

    @ApiOperation(value = "获取申请单 单据详情", httpMethod = "GET")
    @GetMapping("/getApplicationInfo")
    public ResponseEntity<CommissionApplicationInfoVO> getApplicationInfo(@RequestParam String sumId) {
        try {
            CommissionApplicationInfoVO applicationInfoVO = applicationService.getApplicationInfo(sumId);
            return ResponseEntity.ok(applicationInfoVO);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }

    @ApiOperation(value = "修改申请单 单据详情", httpMethod = "POST")
    @PostMapping("/updateApplicationInfo")
    public ResponseEntity updateApplicationInfo(@RequestBody CommissionApplicationInfoUpdateVO updateVO) {
        try {
            applicationService.updateApplicationInfo(updateVO);
            return ResponseEntity.ok();
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }

    @ApiOperation(value = "撤回申请单", httpMethod = "GET")
    @GetMapping("/backApplicationInfo")
    public ResponseEntity backApplicationInfo(@RequestParam String sumId) {
        try {
            //撤回申请单。 0草稿
            Integer status = 0;
            applicationService.updateStatusBySumId(sumId,status);
            return ResponseEntity.ok();
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }
    @ApiOperation(value = "作废申请单", httpMethod = "GET")
    @GetMapping("/abolishApplicationInfo")
    public ResponseEntity abolishApplicationInfo(@RequestParam String sumId) {
        try {
            //撤回申请单。 -2 作废 。
            Integer status = -2;
            applicationService.updateStatusBySumId(sumId,status);
            return ResponseEntity.ok();
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }
    @ApiOperation(value = "税务退回", httpMethod = "GET")
    @GetMapping("/taxReturn")
    public ResponseEntity taxReturn(@RequestParam String sumId) {
        try {
            //撤回申请单。 -2 作废 。
            Integer status = -1;
            applicationService.updateStatusBySumId(sumId,status);
            return ResponseEntity.ok();
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }


    @ApiOperation(value = "导入提成明细", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importTemplate")
    public ResponseEntity importTemplate(@RequestParam(name = "file") MultipartFile file,@RequestParam(name = "batchNo",required = false) String batchNo) throws IOException {
        InputStream is = null;
        int headRows = 3; //表示表头有4行
        int colNum = 43; //43列数
        EasyExcelImportListener extractListener = new EasyExcelImportListener(extractsumService, TCIMPORT, headRows ,colNum,batchNo);
        try {
            is = file.getInputStream();
            EasyExcel.read(is, extractListener).sheet(0).doReadSync();
        } catch (IOException e1) {
            e1.printStackTrace();
            log.error(e1.getMessage(), e1);
            return ResponseEntity.error(e1.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
                return ResponseEntity.error(e.getMessage());
            }
        }
        //表头错误明细（如果表头报错明细数据将不会校验）
        List<String> headErrorMsg = extractListener.getHeadErrorMsg();
        //明细数据的错误明细
        Map<Integer, Map<Integer, String>> errorMap = extractListener.getErrorMap();
        //导入的所有的数据
        Map<Integer, Map<Integer, String>> allDataMap = extractListener.getAllDataMap();
        if (!headErrorMsg.isEmpty() || !errorMap.isEmpty()) {

            List<CommissionDetailsImportDTO> details = new ArrayList<>();
            //最终的表头数据
            Map<String, String> heads = new HashMap<>();
            //填充错误明细数据
            populateData(details, heads, allDataMap, headErrorMsg, errorMap);

            InputStream iss = null;
            try {
                iss = this.getClass().getClassLoader().getResourceAsStream("template/extractImportTemplateNewError.xlsx");
                String key = IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName();
//                String errorFileName = fileShareDir + File.separator + System.currentTimeMillis() + "_错误信息.xlsx";
                String errorFileName =  System.currentTimeMillis() + "_提成导入错误信息.xlsx";
                ExcelWriter workBook = EasyExcel.write(new File(errorFileName), CommissionDetailsImportDTO.class).withTemplate(iss).build();
                WriteSheet sheet = EasyExcel.writerSheet(0).build();
                sheet.setSheetName("提成导入错误明细");
                workBook.fill(heads, sheet);
                workBook.fill(details, sheet);
                workBook.finish();
                redisClient.set(key, errorFileName, expireTime);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            } finally {
                if (iss != null) iss.close();
            }
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载");
        }
        return ResponseEntity.ok("导入成功");
    }



    /**
     * 下载模板。
     */
    @ApiOperation(value = "提成明细  下载模板", httpMethod = "GET")
    @GetMapping("/downLoadTemplate")
    public void downLoadTemplate(HttpServletResponse response) throws Exception {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/extractImportTemplateNew.xlsx")) {
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("提成导入模板", response)).withTemplate(is).build();
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            List<Map<String, Object>> list = new ArrayList<>();
            workBook.fill(list, sheet);
            workBook.finish();

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw e;
        }
    }


    @ApiOperation(value = "下载导入提成错误明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/downImportExtractErrorDetail")
    public void downImportExtractErrorDetail(HttpServletResponse response, HttpServletRequest request) throws Exception {

        InputStream is = null;
        try {
            if (redisClient.get(IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName()) == null) {
                throw new RuntimeException("没有提成错误明细可供下载。");
            }
            String errorFileName = redisClient.get(IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName());
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("提成导入错误明细", response)).withTemplate(is).build();
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            workBook.finish();
            File file = new File(errorFileName);
            if (file.exists()) file.delete();
            redisClient.delete(IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (is != null) is.close();
        }
    }


    /**
     * 填充导入提成错误的数据
     *
     * @param details
     * @param heads
     * @param allDataMap
     * @param headErrorMsg
     * @param errorMap
     */
    private void populateData(List<CommissionDetailsImportDTO> details,
                              Map<String, String> heads, Map<Integer, Map<Integer, String>> allDataMap, List<String> headErrorMsg, Map<Integer, Map<Integer, String>> errorMap) {
        Map<Integer, String> headMap = allDataMap.get(1);
        String year = headMap.get(1); //届别
        String extractMonth = headMap.get(5); //提成期间
        String unitname = headMap.get(9); //预算单位
        if (!headErrorMsg.isEmpty()) {
            for (int i = 4; i <= allDataMap.size(); i++) {
                CommissionDetailsImportDTO dto = new CommissionDetailsImportDTO();
                dto  =  setValue(allDataMap.get(i));
                dto.setErrMsg(headErrorMsg.stream().collect(Collectors.joining(",")));
                details.add(dto);
            }
        } else if (!errorMap.isEmpty()) {
            errorMap.forEach((i, data) -> {
                CommissionDetailsImportDTO dto = new CommissionDetailsImportDTO();
                dto  =  setValue(allDataMap.get(i));
                details.add(dto);
            });
        }
        heads.put("yearPeriod", year);
        heads.put("extractMonth", extractMonth);
        heads.put("unitName", unitname);
    }

    private CommissionDetailsImportDTO setValue(Map<Integer, String> data){
        CommissionDetailsImportDTO extractImportdetail = new CommissionDetailsImportDTO();
        String businessType = data.get(0); //业务类型  ？？
        //新增员工个体户。

        String empNo = data.get(1); //工号
        String empName = data.get(2); //姓名
        String isDebt = data.get(3); //是否坏账   index = 3,实际是第4列

        String extractType = data.get(4);//提成类型
        String tcPeriod = data.get(5); //提成届别 第6列

        //应发提成计算
        String totalPrice = data.get(6);//码洋  实际是第7列,index= 6
        String actualPrice = data.get(7);//实洋
        String collection = data.get(8);//回款
        String income = data.get(9);  //收入  第10列

        String helpCollectionHost = data.get(10);//   在职帮离职回款成本
        String strippingReceivedFunds = data.get(11);//到款剥离
        String regularCommission = data.get(12);//常规提成
        String takeOverTheCommission = data.get(13);//接手提成
        String specialCommission = data.get(14);//特价提成

        String totalRoyalty = data.get(15);//总提成  16列

        String paidCommission = data.get(16);//已发提成
        String reservedCommission = data.get(17);//预留提成
        String shouldSendExtract = data.get(18);//应发提成


        //代收代缴款
        String tax = data.get(19);//提成个税
        String taxReduction = data.get(20);//返提成个税
        String consotax = data.get(21);//综合税
        String invoiceExcessTax = data.get(22);//发票超额税金
        String invoiceExcessTaxReduction = data.get(23);//返发票超额税金
        String excessTaxPreviousInvoices = data.get(24);//往届发票超额税金  第25列

        //业务扣款
        String lateFee = data.get(25);//滞纳金
        String deliveryLogisticsFee = data.get(26);//发货物流费
        String shippingCost = data.get(27);//发件费用
        String sampleIssuingCost = data.get(28);//发样成本
        String returnLogisticsFee = data.get(29);//退货物流费   30 列

        //业务扣款--费用
        String returnCost = data.get(30);//退货成本
        String distributionCost = data.get(31);//铺货成本
        String shiftPackingFee = data.get(32);//分班打包费
        String giftFee = data.get(33);//礼品费
        String badDebtAssessment = data.get(34);//坏账考核   30 列
        String nonConformancePenalty = data.get(35);//未达标罚款   30 列

        //其他罚款
        //其他罚款
        String previousCost = data.get(36);//往届成本
        String currentDeduction = data.get(37);//往来扣款
        String deductionGuarantee = data.get(38);//扣担保
        String deductCreditInformation = data.get(39);//扣征信

        String salesmanAdvance = data.get(40);//业务员垫支
        String otherTypesDeduction = data.get(41);//其他类型 扣款
        String subtotalOfDeduction = data.get(42);//扣款小计
        String copeextract = data.get(43);//实发金额


//        extractImportdetail.setExtractsumid(extractSum.getId());
        //赋值 员工类型
//        setUserTypeValue(isCompanyEmp, empNo, empName, extractImportdetail);

        extractImportdetail.setEmpno(empNo);
        extractImportdetail.setEmpname(empName);
        extractImportdetail.setIfBadDebt(isDebt);
        extractImportdetail.setYearName(tcPeriod);
        extractImportdetail.setBusinessType(businessType);
        extractImportdetail.setExtractType(extractType);



        //应发提成计算
        extractImportdetail.setTotalPrice(getBigDecimal(totalPrice));
        extractImportdetail.setActualPrice(getBigDecimal(actualPrice));
        extractImportdetail.setCollection(getBigDecimal(collection));
        extractImportdetail.setIncome(getBigDecimal(income));

        extractImportdetail.setHelpCollectionHost(getBigDecimal(helpCollectionHost));
        extractImportdetail.setStrippingReceivedFunds(getBigDecimal(strippingReceivedFunds));
        extractImportdetail.setRegularCommission(getBigDecimal(regularCommission));
        extractImportdetail.setTakeOverTheCommission(getBigDecimal(takeOverTheCommission));
        extractImportdetail.setSpecialCommission(getBigDecimal(specialCommission));
        extractImportdetail.setTotalRoyalty(getBigDecimal(totalRoyalty));


        extractImportdetail.setPaidCommission(getBigDecimal(paidCommission));
        extractImportdetail.setReservedCommission(getBigDecimal(reservedCommission));
        extractImportdetail.setShouldSendExtract(getBigDecimal(shouldSendExtract));


        //代收代缴款
        extractImportdetail.setTax(getBigDecimal(tax));
        extractImportdetail.setTaxReduction(getBigDecimal(taxReduction));
        extractImportdetail.setConsotax(getBigDecimal(consotax));
        extractImportdetail.setInvoiceExcessTax(getBigDecimal(invoiceExcessTax));
        extractImportdetail.setInvoiceExcessTaxReduction(getBigDecimal(invoiceExcessTaxReduction));
        extractImportdetail.setExcessTaxPreviousInvoices(getBigDecimal(excessTaxPreviousInvoices));


        //业务扣款
        extractImportdetail.setLateFee(getBigDecimal(lateFee));
        extractImportdetail.setDeliveryLogisticsFee(getBigDecimal(deliveryLogisticsFee));
        extractImportdetail.setShippingCost(getBigDecimal(shippingCost));
        extractImportdetail.setSampleIssuingCost(getBigDecimal(sampleIssuingCost));
        extractImportdetail.setReturnLogisticsFee(getBigDecimal(returnLogisticsFee));

        //业务扣款--费用
        extractImportdetail.setReturnCost(getBigDecimal(returnCost));
        extractImportdetail.setDistributionCost(getBigDecimal(distributionCost));
        extractImportdetail.setShiftPackingFee(getBigDecimal(shiftPackingFee));
        extractImportdetail.setGiftFee(getBigDecimal(giftFee));
        extractImportdetail.setBadDebtAssessment(getBigDecimal(badDebtAssessment));
        extractImportdetail.setNonConformancePenalty(getBigDecimal(nonConformancePenalty));

        //其他罚款
        extractImportdetail.setPreviousCost(getBigDecimal(previousCost));
        extractImportdetail.setCurrentDeduction(getBigDecimal(currentDeduction));
        extractImportdetail.setDeductionGuarantee(getBigDecimal(deductionGuarantee));
        extractImportdetail.setDeductCreditInformation(getBigDecimal(deductCreditInformation));
        extractImportdetail.setSalesmanAdvance(getBigDecimal(salesmanAdvance));
        extractImportdetail.setOtherTypesDeduction(getBigDecimal(otherTypesDeduction));
        extractImportdetail.setSubtotalOfDeduction(getBigDecimal(subtotalOfDeduction));
        extractImportdetail.setCopeextract(getBigDecimal(copeextract));
        String errMsg= data.get(44)!=null?data.get(44): data.get(43)!=null?data.get(43):"";
        extractImportdetail.setErrMsg(errMsg);
        return extractImportdetail;
    }
    private BigDecimal getBigDecimal(String object){
        if (StringUtils.isNotBlank(object)) {
            if (StringUtils.isNumeric(object)) {
                return new BigDecimal(object);
            }else{
                 return BigDecimal.ZERO;
            }
        }else {
            return BigDecimal.ZERO;
        }
    }



    @ApiOperation(value = "根据预算单位及月份查询动因", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long"),
            @ApiImplicitParam(value = "搜索名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "权限认证", name = "oauth", dataType = "Boolean"),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @ApiDataAuthAnno
    @GetMapping(value = "/listSubjectMonthAgent")
    public ResponseEntity<PageResult<BudgetSubjectVO>> listSubjectMonthAgent(Long budgetUnitId,
                                                                             String name,
                                                                             Boolean oauth,
                                                                             @RequestParam Long yearId,
                                                                             @RequestParam Integer monthId,
                                                                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                             @RequestParam(value = "rows", defaultValue = "20") Integer rows) {

        HashMap<String, Object> paramMap = new HashMap<>(10);
        paramMap.put("yearId", yearId);
        paramMap.put("budgetUnitId", budgetUnitId);
        paramMap.put("monthId", monthId);
        paramMap.put("name", name);
        if (oauth == null || oauth) {
            paramMap.put("userId", UserThreadLocal.get().getUserId());
            paramMap.put("authSql", JdbcSqlThreadLocal.get());
        }
        return ResponseEntity.ok(applicationService.listSubjectMonthAgent(paramMap, page, rows));
    }


    /**
     * 下载发放明细模板。
     */
    @ApiOperation(value = "下载发放明细模板", httpMethod = "GET",produces = "application/octet-stream")
    @GetMapping("/downLoadIssuedTemplate")
    public void downLoadIssuedTemplate(HttpServletResponse response) throws Exception {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("员工发放名单模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcelFactory.write(response.getOutputStream(), IndividualIssueExportDTO.class).sheet("员工发放名单模板").doWrite(new ArrayList<>());
    }

    /**
     * 导出发放明细
     */
    @ApiOperation(value = "导出发放明细", httpMethod = "GET")
    @GetMapping("/exportIssuedTemplate")
    public ResponseEntity exportIssuedTemplate(@RequestParam("extractMonth") String extractMonth, HttpServletResponse response) throws Exception {
        try {
            if(StringUtils.isNotBlank(extractMonth)) {
                extractMonth = extractMonth.split("-")[2];
            }
            applicationService.validStatusIsAllVerify(extractMonth);
            List<IndividualIssueExportDTO> exportDTOList  =  applicationService.exportIssuedTemplate(extractMonth);
            EasyExcelUtil.writeExcel(response,exportDTOList,"发放明细信息","发放明细信息",IndividualIssueExportDTO.class);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }


    /**
     * 先导出发放明细，填写费用，再导入.导入时，前端需要传sumId
     */
    @ApiOperation(value = "导入费用明细", httpMethod = "POST")
    @PostMapping("/importFeeTemplate")
    public ResponseEntity importFeeTemplate(@RequestParam("file") MultipartFile multipartFile,@RequestParam("extractMonth") String extractMonth,HttpServletResponse response) throws Exception {
        try {
            extractMonth = extractMonth.split("-")[2];
            //计算发放之前  //发放之后
            applicationService.validateExtractMonth(extractMonth);

            List<FeeImportErrorDTO> errorDTOList = applicationService.importFeeTemplate(multipartFile,extractMonth);
            if(CollectionUtils.isNotEmpty(errorDTOList)) {
                try {
                    String key = IMPORT_FEE + "_" + UserThreadLocal.get().getUserName();
                    String errorFileName = fileShareDir + File.separator + System.currentTimeMillis() + "_错误信息.xlsx";
                    File file = new File(errorFileName);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    EasyExcel.write(file, FeeImportErrorDTO.class).sheet("错误信息").doWrite(errorDTOList);

//                    ExcelWriter workBook = EasyExcel.write(new File(errorFileName), IndividualImportErrorDTO.class).build();
//                    WriteSheet sheet = EasyExcel.writerSheet(0).build();
//                    workBook.fill(errorDTOList, sheet);
//                    workBook.finish();
                    redisClient.set(key, errorFileName, expireTime);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage(), e);
                }
                return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载");

//                EasyExcelUtil.writeExcel(response, errorDTOList, "员工个体户错误明细", "员工个体户错误明细", FeeImportErrorDTO.class);
//                return null;
            }
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * 查看批次 费用明细列表
     */
    @ApiOperation(value = "查看批次 费用明细列表", httpMethod = "GET")
    @GetMapping("/selectFeePage")
    public ResponseEntity<PageResult<BudgetExtractFeePayDetailBeforeCal>> selectFeePage(@ModelAttribute FeeQuery query) throws Exception {
        if (StringUtils.isNotBlank(query.getExtractMonth())) {
            query.setExtractMonth( query.getExtractMonth().split("-")[2]);
        }
        IPage<BudgetExtractFeePayDetailBeforeCal> page = applicationService.selectFeePage(query);
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }

    //exportTemplate
    /**
     *  根据sumId 导出提成明细
     */
    @ApiOperation(value = "根据sumId 导出提成明细", httpMethod = "GET")
    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletResponse response,@RequestParam String sumId) throws Exception {
        BudgetExtractsum extractSum = extractsumService.getById(sumId);
        List<BudgetExtractImportdetail> importDetailList = importDetailService.lambdaQuery().eq(BudgetExtractImportdetail::getExtractsumid, sumId).list();
        List<CommissionDetailsImportDTO> dtoList = new ArrayList<>();
        for (BudgetExtractImportdetail entity : importDetailList) {
            CommissionDetailsImportDTO dto = CommissionConverter.INSTANCE.toDTO(entity);
            dtoList.add(dto);
            dto.setBusinessType(ExtractUserTypeEnum.getValue(entity.getBusinessType()) );
            dto.setIfBadDebt(entity.getIsbaddebt()?"是":"否");
            String yearName = yearMapper.getNameById(entity.getYearid());
            dto.setYearName(yearName);
        }
        try {
            //模板用错了 导致一直fill错误。 因为没有 那个对象吗。有那个对象，但是dtoList没有值。？？？ 是缓存吗？
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/extractImportTemplateNew.xlsx");
            ExcelWriter excelWriter = EasyExcel.write(EasyExcelUtil.getOutputStream("导出提成明细", response),CommissionDetailsImportDTO.class).withTemplate(is).build();
            WriteSheet writeSheet = EasyExcel.writerSheet(0).build();
            // 直接写入数据
            excelWriter.fill(dtoList, writeSheet);
            Map<String,String>  map = new HashMap();
            map.put("yearPeriod", yearMapper.getNameById(extractSum.getYearid()));
            map.put("extractMonth", extractSum.getExtractmonth());
            map.put("unitName", extractSum.getDeptname());
            excelWriter.fill(map,writeSheet);
            excelWriter.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    /**
     * 下载错误明细。
     */
    @ApiOperation(value = "下载费用明细错误明细", httpMethod = "GET")
    @GetMapping("/downLoadFeeError")
    public void downLoadError(HttpServletResponse response) throws Exception {
        InputStream is = null;
        try {
            if (redisClient.get(IMPORT_FEE + "_" + UserThreadLocal.get().getUserName()) == null) {
                throw new RuntimeException("没有费用错误明细可供下载。");
            }
            String errorFileName = redisClient.get(IMPORT_FEE + "_" + UserThreadLocal.get().getUserName());
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("费用错误明细", response)).withTemplate(is).build();
            workBook.finish();
            File file = new File(errorFileName);
            if (file.exists()) file.delete();
            redisClient.delete(IMPORT_FEE + "_" + UserThreadLocal.get().getUserName());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (is != null) is.close();
        }
    }
}
