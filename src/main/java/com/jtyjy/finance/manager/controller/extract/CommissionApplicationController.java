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
 * Description: ???????????????
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 14:36
 */
@Api(tags = {"?????????????????????"})
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


//    @ApiOperation(value = "???????????????????????????", httpMethod = "GET")
//    @ApiImplicitParams(value = {
//            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
//            @ApiImplicitParam(value = "?????????????????????", name = "query", dataType = "String"),
//            @ApiImplicitParam(value = "????????????", name = "status", dataType = "Integer"),
//            @ApiImplicitParam(value = "????????????", name = "code", dataType = "String"),
//            @ApiImplicitParam(value = "????????????", name = "unitname", dataType = "String"),
//            @ApiImplicitParam(value = "??????????????????1???", name = "page", dataType = "Integer"),
//            @ApiImplicitParam(value = "?????????????????????20???", name = "rows", dataType = "Integer")
//    })
//    @GetMapping("/getExtractInfoList")
//    public ResponseEntity<PageResult<ExtractInfoVO>> getExtractInfoList(@RequestParam(name = "query") String query,
//                                                                        @RequestParam(defaultValue = "1") Integer page,
//                                                                        @RequestParam(defaultValue = "20") Integer rows,
//                                                                        @RequestParam(name = "status", required = false) Integer status,
//                                                                        @RequestParam(name = "code", required = false) String code,
//                                                                        @RequestParam(name = "unitname", required = false) String unitname) {
//        try {
//            if (StringUtils.isBlank(query)) throw new RuntimeException("???????????????????????????????????????");
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
@ApiOperation(value = "????????????????????????", httpMethod = "GET")
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

    @ApiOperation(value = "??????????????? ????????????", httpMethod = "GET")
    @GetMapping("/getApplicationInfo")
    public ResponseEntity<CommissionApplicationInfoVO> getApplicationInfo(@RequestParam String sumId) {
        try {
            CommissionApplicationInfoVO applicationInfoVO = applicationService.getApplicationInfo(sumId);
            return ResponseEntity.ok(applicationInfoVO);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }

    @ApiOperation(value = "??????????????? ????????????", httpMethod = "POST")
    @PostMapping("/updateApplicationInfo")
    public ResponseEntity updateApplicationInfo(@RequestBody CommissionApplicationInfoUpdateVO updateVO) {
        try {
            applicationService.updateApplicationInfo(updateVO);
            return ResponseEntity.ok();
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }

    @ApiOperation(value = "???????????????", httpMethod = "GET")
    @GetMapping("/backApplicationInfo")
    public ResponseEntity backApplicationInfo(@RequestParam String sumId) {
        try {
            //?????????????????? 0??????
            Integer status = 0;
            applicationService.updateStatusBySumId(sumId,status);
            return ResponseEntity.ok();
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }
    @ApiOperation(value = "???????????????", httpMethod = "GET")
    @GetMapping("/abolishApplicationInfo")
    public ResponseEntity abolishApplicationInfo(@RequestParam String sumId) {
        try {
            //?????????????????? -2 ?????? ???
            Integer status = -2;
            applicationService.updateStatusBySumId(sumId,status);
            return ResponseEntity.ok();
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }
    @ApiOperation(value = "????????????", httpMethod = "GET")
    @GetMapping("/taxReturn")
    public ResponseEntity taxReturn(@RequestParam String sumId) {
        try {
            //?????????????????? -2 ?????? ???
            Integer status = -1;
            applicationService.updateStatusBySumId(sumId,status);
            return ResponseEntity.ok();
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }


    @ApiOperation(value = "??????????????????", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importTemplate")
    public ResponseEntity importTemplate(@RequestParam(name = "file") MultipartFile file,@RequestParam(name = "batchNo",required = false) String batchNo) throws IOException {
        InputStream is = null;
        int headRows = 3; //???????????????4???
        int colNum = 43; //43??????
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
        //?????????????????????????????????????????????????????????????????????
        List<String> headErrorMsg = extractListener.getHeadErrorMsg();
        //???????????????????????????
        Map<Integer, Map<Integer, String>> errorMap = extractListener.getErrorMap();
        //????????????????????????
        Map<Integer, Map<Integer, String>> allDataMap = extractListener.getAllDataMap();
        if (!headErrorMsg.isEmpty() || !errorMap.isEmpty()) {

            List<CommissionDetailsImportDTO> details = new ArrayList<>();
            //?????????????????????
            Map<String, String> heads = new HashMap<>();
            //????????????????????????
            populateData(details, heads, allDataMap, headErrorMsg, errorMap);

            InputStream iss = null;
            try {
                iss = this.getClass().getClassLoader().getResourceAsStream("template/extractImportTemplateNewError.xlsx");
                String key = IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName();
//                String errorFileName = fileShareDir + File.separator + System.currentTimeMillis() + "_????????????.xlsx";
                String errorFileName =  System.currentTimeMillis() + "_????????????????????????.xlsx";
                ExcelWriter workBook = EasyExcel.write(new File(errorFileName), CommissionDetailsImportDTO.class).withTemplate(iss).build();
                WriteSheet sheet = EasyExcel.writerSheet(0).build();
                sheet.setSheetName("????????????????????????");
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
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "?????????????????????,?????????????????????");
        }
        return ResponseEntity.ok("????????????");
    }



    /**
     * ???????????????
     */
    @ApiOperation(value = "????????????  ????????????", httpMethod = "GET")
    @GetMapping("/downLoadTemplate")
    public void downLoadTemplate(HttpServletResponse response) throws Exception {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/extractImportTemplateNew.xlsx")) {
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("??????????????????", response)).withTemplate(is).build();
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


    @ApiOperation(value = "??????????????????????????????", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/downImportExtractErrorDetail")
    public void downImportExtractErrorDetail(HttpServletResponse response, HttpServletRequest request) throws Exception {

        InputStream is = null;
        try {
            if (redisClient.get(IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName()) == null) {
                throw new RuntimeException("???????????????????????????????????????");
            }
            String errorFileName = redisClient.get(IMPORT_TYPE + "_" + UserThreadLocal.get().getUserName());
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("????????????????????????", response)).withTemplate(is).build();
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
     * ?????????????????????????????????
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
        String year = headMap.get(1); //??????
        String extractMonth = headMap.get(5); //????????????
        String unitname = headMap.get(9); //????????????
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
        String businessType = data.get(0); //????????????  ??????
        //????????????????????????

        String empNo = data.get(1); //??????
        String empName = data.get(2); //??????
        String isDebt = data.get(3); //????????????   index = 3,????????????4???

        String extractType = data.get(4);//????????????
        String tcPeriod = data.get(5); //???????????? ???6???

        //??????????????????
        String totalPrice = data.get(6);//??????  ????????????7???,index= 6
        String actualPrice = data.get(7);//??????
        String collection = data.get(8);//??????
        String income = data.get(9);  //??????  ???10???

        String helpCollectionHost = data.get(10);//   ???????????????????????????
        String strippingReceivedFunds = data.get(11);//????????????
        String regularCommission = data.get(12);//????????????
        String takeOverTheCommission = data.get(13);//????????????
        String specialCommission = data.get(14);//????????????

        String totalRoyalty = data.get(15);//?????????  16???

        String paidCommission = data.get(16);//????????????
        String reservedCommission = data.get(17);//????????????
        String shouldSendExtract = data.get(18);//????????????


        //???????????????
        String tax = data.get(19);//????????????
        String taxReduction = data.get(20);//???????????????
        String consotax = data.get(21);//?????????
        String invoiceExcessTax = data.get(22);//??????????????????
        String invoiceExcessTaxReduction = data.get(23);//?????????????????????
        String excessTaxPreviousInvoices = data.get(24);//????????????????????????  ???25???

        //????????????
        String lateFee = data.get(25);//?????????
        String deliveryLogisticsFee = data.get(26);//???????????????
        String shippingCost = data.get(27);//????????????
        String sampleIssuingCost = data.get(28);//????????????
        String returnLogisticsFee = data.get(29);//???????????????   30 ???

        //????????????--??????
        String returnCost = data.get(30);//????????????
        String distributionCost = data.get(31);//????????????
        String shiftPackingFee = data.get(32);//???????????????
        String giftFee = data.get(33);//?????????
        String badDebtAssessment = data.get(34);//????????????   30 ???
        String nonConformancePenalty = data.get(35);//???????????????   30 ???

        //????????????
        //????????????
        String previousCost = data.get(36);//????????????
        String currentDeduction = data.get(37);//????????????
        String deductionGuarantee = data.get(38);//?????????
        String deductCreditInformation = data.get(39);//?????????

        String salesmanAdvance = data.get(40);//???????????????
        String otherTypesDeduction = data.get(41);//???????????? ??????
        String subtotalOfDeduction = data.get(42);//????????????
        String copeextract = data.get(43);//????????????


//        extractImportdetail.setExtractsumid(extractSum.getId());
        //?????? ????????????
//        setUserTypeValue(isCompanyEmp, empNo, empName, extractImportdetail);

        extractImportdetail.setEmpno(empNo);
        extractImportdetail.setEmpname(empName);
        extractImportdetail.setIfBadDebt(isDebt);
        extractImportdetail.setYearName(tcPeriod);
        extractImportdetail.setBusinessType(businessType);
        extractImportdetail.setExtractType(extractType);



        //??????????????????
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


        //???????????????
        extractImportdetail.setTax(getBigDecimal(tax));
        extractImportdetail.setTaxReduction(getBigDecimal(taxReduction));
        extractImportdetail.setConsotax(getBigDecimal(consotax));
        extractImportdetail.setInvoiceExcessTax(getBigDecimal(invoiceExcessTax));
        extractImportdetail.setInvoiceExcessTaxReduction(getBigDecimal(invoiceExcessTaxReduction));
        extractImportdetail.setExcessTaxPreviousInvoices(getBigDecimal(excessTaxPreviousInvoices));


        //????????????
        extractImportdetail.setLateFee(getBigDecimal(lateFee));
        extractImportdetail.setDeliveryLogisticsFee(getBigDecimal(deliveryLogisticsFee));
        extractImportdetail.setShippingCost(getBigDecimal(shippingCost));
        extractImportdetail.setSampleIssuingCost(getBigDecimal(sampleIssuingCost));
        extractImportdetail.setReturnLogisticsFee(getBigDecimal(returnLogisticsFee));

        //????????????--??????
        extractImportdetail.setReturnCost(getBigDecimal(returnCost));
        extractImportdetail.setDistributionCost(getBigDecimal(distributionCost));
        extractImportdetail.setShiftPackingFee(getBigDecimal(shiftPackingFee));
        extractImportdetail.setGiftFee(getBigDecimal(giftFee));
        extractImportdetail.setBadDebtAssessment(getBigDecimal(badDebtAssessment));
        extractImportdetail.setNonConformancePenalty(getBigDecimal(nonConformancePenalty));

        //????????????
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



    @ApiOperation(value = "???????????????????????????????????????", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????Id", name = "yearId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "????????????Id", name = "budgetUnitId", dataType = "Long"),
            @ApiImplicitParam(value = "????????????", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "????????????", name = "oauth", dataType = "Boolean"),
            @ApiImplicitParam(value = "??????Id", name = "monthId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
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
     * ???????????????????????????
     */
    @ApiOperation(value = "????????????????????????", httpMethod = "GET",produces = "application/octet-stream")
    @GetMapping("/downLoadIssuedTemplate")
    public void downLoadIssuedTemplate(HttpServletResponse response) throws Exception {
        // ???????????? ?????????????????????swagger ??????????????????????????????????????????????????????postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // ??????URLEncoder.encode???????????????????????? ?????????easyexcel????????????
        String fileName = URLEncoder.encode("????????????????????????", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcelFactory.write(response.getOutputStream(), IndividualIssueExportDTO.class).sheet("????????????????????????").doWrite(new ArrayList<>());
    }

    /**
     * ??????????????????
     */
    @ApiOperation(value = "??????????????????", httpMethod = "GET")
    @GetMapping("/exportIssuedTemplate")
    public ResponseEntity exportIssuedTemplate(@RequestParam("extractMonth") String extractMonth, HttpServletResponse response) throws Exception {
        try {
            if(StringUtils.isNotBlank(extractMonth)) {
                extractMonth = extractMonth.split("-")[2];
            }
            applicationService.validStatusIsAllVerify(extractMonth);
            List<IndividualIssueExportDTO> exportDTOList  =  applicationService.exportIssuedTemplate(extractMonth);
            EasyExcelUtil.writeExcel(response,exportDTOList,"??????????????????","??????????????????",IndividualIssueExportDTO.class);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }


    /**
     * ????????????????????????????????????????????????.???????????????????????????sumId
     */
    @ApiOperation(value = "??????????????????", httpMethod = "POST")
    @PostMapping("/importFeeTemplate")
    public ResponseEntity importFeeTemplate(@RequestParam("file") MultipartFile multipartFile,@RequestParam("extractMonth") String extractMonth,HttpServletResponse response) throws Exception {
        try {
            extractMonth = extractMonth.split("-")[2];
            //??????????????????  //????????????
            applicationService.validateExtractMonth(extractMonth);

            List<FeeImportErrorDTO> errorDTOList = applicationService.importFeeTemplate(multipartFile,extractMonth);
            if(CollectionUtils.isNotEmpty(errorDTOList)) {
                try {
                    String key = IMPORT_FEE + "_" + UserThreadLocal.get().getUserName();
                    String errorFileName = fileShareDir + File.separator + System.currentTimeMillis() + "_????????????.xlsx";
                    File file = new File(errorFileName);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    EasyExcel.write(file, FeeImportErrorDTO.class).sheet("????????????").doWrite(errorDTOList);

//                    ExcelWriter workBook = EasyExcel.write(new File(errorFileName), IndividualImportErrorDTO.class).build();
//                    WriteSheet sheet = EasyExcel.writerSheet(0).build();
//                    workBook.fill(errorDTOList, sheet);
//                    workBook.finish();
                    redisClient.set(key, errorFileName, expireTime);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage(), e);
                }
                return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "?????????????????????,?????????????????????");

//                EasyExcelUtil.writeExcel(response, errorDTOList, "???????????????????????????", "???????????????????????????", FeeImportErrorDTO.class);
//                return null;
            }
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * ???????????? ??????????????????
     */
    @ApiOperation(value = "???????????? ??????????????????", httpMethod = "GET")
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
     *  ??????sumId ??????????????????
     */
    @ApiOperation(value = "??????sumId ??????????????????", httpMethod = "GET")
    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletResponse response,@RequestParam String sumId) throws Exception {
        BudgetExtractsum extractSum = extractsumService.getById(sumId);
        List<BudgetExtractImportdetail> importDetailList = importDetailService.lambdaQuery().eq(BudgetExtractImportdetail::getExtractsumid, sumId).list();
        List<CommissionDetailsImportDTO> dtoList = new ArrayList<>();
        for (BudgetExtractImportdetail entity : importDetailList) {
            CommissionDetailsImportDTO dto = CommissionConverter.INSTANCE.toDTO(entity);
            dtoList.add(dto);
            dto.setBusinessType(ExtractUserTypeEnum.getValue(entity.getBusinessType()) );
            dto.setIfBadDebt(entity.getIsbaddebt()?"???":"???");
            String yearName = yearMapper.getNameById(entity.getYearid());
            dto.setYearName(yearName);
        }
        try {
            //??????????????? ????????????fill????????? ???????????? ??????????????????????????????????????????dtoList????????????????????? ???????????????
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/extractImportTemplateNew.xlsx");
            ExcelWriter excelWriter = EasyExcel.write(EasyExcelUtil.getOutputStream("??????????????????", response),CommissionDetailsImportDTO.class).withTemplate(is).build();
            WriteSheet writeSheet = EasyExcel.writerSheet(0).build();
            // ??????????????????
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
     * ?????????????????????
     */
    @ApiOperation(value = "??????????????????????????????", httpMethod = "GET")
    @GetMapping("/downLoadFeeError")
    public void downLoadError(HttpServletResponse response) throws Exception {
        InputStream is = null;
        try {
            if (redisClient.get(IMPORT_FEE + "_" + UserThreadLocal.get().getUserName()) == null) {
                throw new RuntimeException("???????????????????????????????????????");
            }
            String errorFileName = redisClient.get(IMPORT_FEE + "_" + UserThreadLocal.get().getUserName());
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("??????????????????", response)).withTemplate(is).build();
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
