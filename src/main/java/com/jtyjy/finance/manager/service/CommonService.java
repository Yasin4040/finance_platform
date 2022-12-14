package com.jtyjy.finance.manager.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.core.anno.JdbcSelector;
import com.jtyjy.core.interceptor.LoginThreadLocal;
import com.jtyjy.core.log.DefaultChangeLog;
import com.jtyjy.core.log.LoggerAction;
import com.jtyjy.core.service.BaseService;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.dto.TabPayOrderVO;
import com.jtyjy.finance.manager.easyexcel.*;
import com.jtyjy.finance.manager.enmus.ExtractPayTemplateEnum;
import com.jtyjy.finance.manager.enmus.PaymoneyTypeEnum;
import com.jtyjy.finance.manager.mapper.BudgetPaybatchMapper;
import com.jtyjy.finance.manager.mapper.WbBanksMapper;
import com.jtyjy.finance.manager.query.UploadQuery;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.FileUtils;
import com.jtyjy.finance.manager.utils.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@JdbcSelector(value = "defaultJdbcTemplateService")
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
public class CommonService extends DefaultBaseService<WbBanksMapper, WbBanks> {

    @Autowired
    private WbBanksMapper wbMapper;

    @Autowired
    private TabDmService dmService;
    @Autowired
    private StorageClient storageClient;
    @Autowired
    private BudgetCommonAttachmentService attachmentService;
    @Autowired
    private BudgetPaybatchMapper paybatchMapper;
    @Autowired
    private BudgetPaymoneyService paymoneyService;
    @Autowired
    private BudgetExtractPayService extractPayService;

    @Value("${sunpay.add.url}")
    private String sunPayUrl;

    @Value("${auth.role.user.url}")
    private String authRoleUserUrl;

    @Override
    public void doLog(LoggerAction loggerAction, DefaultChangeLog changeLog) throws Exception {

    }

    @Override
    public BaseMapper getLoggerMapper() {
        return null;
    }

    @Override
    public void setBaseLoggerBean() {

    }

    /*
     * Author: ldw
     * Description: ???????????????????????????????????????????????????????????????????????????
     * Date: 2021/4/23 15:00
     */
    public List<String> getDistinctBankTypes() {
        /*QueryWrapper<WbBanks>  wrapper = new QueryWrapper<>();
        wrapper.*/
        String sql = "SELECT DISCINCT bank_name FROM wb_banks";
        System.err.println(this.jdbcTemplateService);
        List<String> query = this.jdbcTemplateService.getColumnValue(String.class,"wb_banks","bank_name",null);
        HashSet set = new HashSet<String>(query);
        query.clear();
        query.addAll(set);
        return query;
    }

    /**
     * <p></p>
     * @author minzhq
     * @date 2022/8/6 10:15
     * @param opt 1 ??????  2 ??????
     * @param fineCount ????????????
     */
    public void createBudgetFine(int opt,int fineCount,String empNo,String creator){
        try{

            TabDm dm = dmService.getByPrimaryKey("is_test_fine", "is_test_fine");
            String fineEmpNo = empNo;
            if("1".equals(dm.getDmValue())){
                TabDm dm1 = dmService.getByPrimaryKey("test_fine_notice", "test_fine_notice");
                fineEmpNo = dm1.getDmValue();
            }
            HttpUtil.doGet(sunPayUrl+"?empNo="+fineEmpNo+"&opt="+opt+"&count="+fineCount+"&creator="+creator);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /*
     * Java???????????? ?????????????????????????????????
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
    @SneakyThrows
    public void uploadFile(UploadQuery query) {

        String loginUser = LoginThreadLocal.get().getEmpno();
        for (MultipartFile file : query.getFiles()) {
            String[] urls = storageClient.upload_file("group1", file.getSize(), new UploadCallback() {

                @Override
                public int send(OutputStream outputStream) throws IOException {
                    outputStream.write(file.getBytes());
                    return 0;
                }
            }, FileUtils.getFileType(file.getOriginalFilename()), null);

            String printUrl = StringUtils.join(urls, "/");
            BudgetCommonAttachment attachment = new BudgetCommonAttachment();
            attachment.setFileUrl(printUrl);
            attachment.setFileType(1);//??????1 ????????????
            attachment.setFileExtName(FileUtils.getFileType(file.getOriginalFilename()));
            attachment.setFileName(getFileNameNoEx(file.getOriginalFilename()));
            attachment.setContactId(query.getContactId());
            attachment.setCreator(loginUser);
            attachment.setCreateTime(new Date());
            attachmentService.save(attachment);
        }

    }

    public List<BudgetCommonAttachment> viewAttachment(String contactId) {
        return attachmentService.lambdaQuery().eq(BudgetCommonAttachment::getContactId,contactId).list();
    }

    public void delAttachment(String id) {
        attachmentService.removeById(id);
    }

    @SneakyThrows
    public String upload(CommonsMultipartFile file) {
        String[] urls = storageClient.upload_file("group1", file.getSize(), new UploadCallback() {
            @Override
            public int send(OutputStream outputStream) throws IOException {
                outputStream.write(file.getBytes());
                return 0;
            }
        }, FileUtils.getFileType(file.getOriginalFilename()), null);
        return StringUtils.join(urls, "/");

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

            if(paybatchid!=null){
                BudgetPaybatch budgetPaybatch = paybatchMapper.selectById(paybatchid);
                if(budgetPaybatch.getPayTemplateType() != ExtractPayTemplateEnum.OLD.type){
                    this.exportOtherPreparePay(budgetPaybatch.getPayTemplateType(), Arrays.stream(budgetPaybatch.getPaymoneyids().split(",")).map(Long::parseLong).collect(Collectors.toList()),response);
                    return;
                }
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
                paymoneySystem = "-OA-??????";
            }else{
                if(paymoneyTypeCountMap.containsKey(PaymoneyTypeEnum.REIMBURSEMENT_PAY.type)){
                    paymoneySystem = "-??????";
                }
                if(paymoneyTypeCountMap.containsKey(PaymoneyTypeEnum.LEND_PAY.type)){
                    paymoneySystem = "-OA";
                }
            }
            Set<String> unitNameSet = new LinkedHashSet<>();//????????????set
            Map<String, List<PayeeDetailExcelData>> unitNameListMap = new HashMap<>();
            Set<PayUnitBankSumExcelData> sumDataSet = new LinkedHashSet<>();
            Map<String, WbBanks> banksMap = this.wbMapper.queryAllBanks();//????????????????????????
            for (int i = 0; i < list.size(); i++) {
                BudgetPaymoney pm = list.get(i);
                if (Constants.PAY_TYPE.CASH.equals(pm.getPaytype())) {
                    //???????????????
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
                PayeeDetailExcelData sumData = new PayeeDetailExcelData("", "", "", "", "?????????", totalMoney, "", "");
                detailList.add(sumData);//?????????????????????
                unitAndBankListMap.put(excelData.getUnitName() + "-" + excelData.getBankName(), detailList);
                sumAllMoney = sumAllMoney.add(excelData.getPayMoney());//??????????????????
            }
            ExcelWriter excelWriter = EasyExcel.write(EasyExcelUtil.getOutputStream("???????????????"+paymoneySystem, response)).build();

            WriteSheet sumSheet = EasyExcel.writerSheet("??????????????????").build();
            WriteTable sumTable = EasyExcel.writerTable(0).head(PayUnitBankSumExcelData.class).needHead(true).build();
            List<PayUnitBankSumExcelData> sumList = new ArrayList<>(sumDataSet);
            PayUnitBankSumExcelData allSumData = new PayUnitBankSumExcelData("", "?????????", sumAllMoney);
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
                PayeeDetailExcelData sumData = new PayeeDetailExcelData("", "", "", "", "?????????", totalMoney, "", "");
                tempList.add(sumData);//?????????????????????
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

    public void exportOtherPreparePay(Integer payTemplateType, List<Long> payMoneyIds,HttpServletResponse response) throws Exception {
        ClassPathResource resource = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream is = null;
        try{
            List<Map<String,Object>> extractPayBatchDetailList = null;
            if(payTemplateType == ExtractPayTemplateEnum.ZS_BATCH.type){
                resource = new ClassPathResource("template/zhbatchpay.xlsx");
                extractPayBatchDetailList = this.extractPayService.getExtractPayBatchDetailList(payMoneyIds,ExtractPayTemplateEnum.ZS_BATCH.type);
                exportWorkBook(extractPayBatchDetailList,resource,bos,is,response, BudgetExtractZhBatchPayExcelData.class);
            }else if(payTemplateType == ExtractPayTemplateEnum.ZS_DF.type){
                resource = new ClassPathResource("template/zhdfpay.xlsx");
                extractPayBatchDetailList = this.extractPayService.getExtractPayBatchDetailList(payMoneyIds,ExtractPayTemplateEnum.ZS_DF.type);
                exportWorkBook(extractPayBatchDetailList,resource,bos,is,response, BudgetExtractZhDfPayExcelData.class);
            }
        }catch (Exception e){
            throw e;
        }finally {
            if (is != null) is.close();
            if(bos!=null) bos.close();
        }
    }

    private  void  exportWorkBook(List<Map<String,Object>> extractPayBatchDetailList,ClassPathResource resource,ByteArrayOutputStream bos,InputStream is,HttpServletResponse response,Class clazz) throws Exception {
        if(!CollectionUtils.isEmpty(extractPayBatchDetailList)){
            Map<String, Object> totalMap = extractPayBatchDetailList.get(0);
            String firsetSheetName = totalMap.keySet().stream().collect(Collectors.joining(","));
            List<BudgetPayTotalExcelData> totalExcelData = (List<BudgetPayTotalExcelData>) totalMap.get(firsetSheetName);
            totalExcelData.add(new BudgetPayTotalExcelData(null,"?????????",totalExcelData.stream().map(BudgetPayTotalExcelData::getPayMoney).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,BigDecimal.ROUND_HALF_UP)));
            XSSFWorkbook workbook = new XSSFWorkbook(resource.getInputStream());
            workbook.setSheetName(0, firsetSheetName);
            Map<String,Object> totalDetailMap = new HashMap<>();
            List<String> nameList = new ArrayList<>();
            Map<String, Object> detailMap = extractPayBatchDetailList.get(1);
            totalDetailMap.putAll(detailMap);
            String name = detailMap.keySet().stream().collect(Collectors.joining(","));
            workbook.setSheetName(1, name);
            nameList.add(name);
            for (int i = 2; i < extractPayBatchDetailList.size(); i++) {
                Map<String, Object> detailMapTemp = extractPayBatchDetailList.get(i);
                detailMapTemp.forEach((k,v)->{
                    workbook.cloneSheet(1, k);
                    nameList.add(k);
                });
                totalDetailMap.putAll(detailMapTemp);
            }
            workbook.write(bos);
            is = new ByteArrayInputStream(bos.toByteArray());
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("?????????????????????", response), clazz).withTemplate(is).build();
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            workBook.fill(totalExcelData,sheet);
            totalDetailMap.forEach((outUnitName,obj)->{
                WriteSheet sheet1 = EasyExcel.writerSheet(nameList.indexOf(outUnitName)+1).build();
                workBook.fill(obj, sheet1);
            });
            workBook.finish();
        }
    }

    /**
     * <p>?????????????????????????????????</p>
     * @author minzhq
     * @date 2022/9/19 9:39
     * @param roleName
     */
    public List<String> getEmpNoListByRoleNames(String roleName){
        String result = HttpUtil.doGet(this.authRoleUserUrl + roleName);
        return JSON.parseArray(result, String.class);
    }

}
