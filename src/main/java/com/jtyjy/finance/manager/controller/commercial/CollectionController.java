package com.jtyjy.finance.manager.controller.commercial;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BusinessPayCollection;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.dto.commission.BusinessPayCollectionErrorDTO;
import com.jtyjy.finance.manager.dto.commission.BusinessPayCollectionImportDTO;
import com.jtyjy.finance.manager.dto.individual.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.query.commission.CommissionQuery;
import com.jtyjy.finance.manager.query.commission.UpdateViewRequest;
import com.jtyjy.finance.manager.query.individual.IndividualFilesQuery;
import com.jtyjy.finance.manager.service.BusinessPayCollectionService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.individual.IndividualEmployeeFilesVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.jtyjy.finance.manager.constants.Constants.IMPORT_COLLECTION;
import static com.jtyjy.finance.manager.constants.Constants.IMPORT_INDIVIDUAL_FILE;

/**
 * Description:
 * Created by ZiYao Lee on 2022/09/16.
 * Time: 10:17
 */@Api(tags = { "????????????????????????" })
@RestController
@RequestMapping("/api/collection")
@Slf4j
public class CollectionController {

     private final BusinessPayCollectionService payCollectionService;
    @Value("${file.shareDir}")
    private String fileShareDir;

    @Value("${redis.file.key.expiretime}")
    private Integer expireTime;
    private final RedisClient redisClient;

    public CollectionController(BusinessPayCollectionService payCollectionService, RedisClient redisClient) {
        this.payCollectionService = payCollectionService;
        this.redisClient = redisClient;
    }


    /********************************??????**********/

    /**
     * payment collection ????????????????????? ??????????????????
     */
    @ApiOperation(value = "????????????????????????", httpMethod = "GET")
    @GetMapping("/selectPage")
    public ResponseEntity<PageResult<BusinessPayCollection>> selectCollectionPage(@ModelAttribute CommissionQuery query) throws Exception {
        IPage<BusinessPayCollection> page = payCollectionService.selectPage(query);
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }


    /**
     * ??????
     */
    @ApiOperation(value = "????????????  ??????", httpMethod = "GET")
    @GetMapping("/exportCollection")
    public ResponseEntity exportCollection(@ModelAttribute CommissionQuery query, HttpServletResponse response) throws Exception {
        try {
            List<BusinessPayCollection> exportDTOList  =  payCollectionService.exportCollection(query);
            EasyExcelUtil.writeExcel(response,exportDTOList,"??????????????????","??????????????????",BusinessPayCollection.class);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * ?????????
     */
    @SneakyThrows
    @ApiOperation(value = "????????????  ??????", httpMethod = "POST")
    @PostMapping("/importCollection")
    public ResponseEntity importCollection(@RequestParam("file") MultipartFile multipartFile, HttpServletResponse response) {
        List<BusinessPayCollectionErrorDTO> errorDTOList = new ArrayList<>();
        try {
            errorDTOList = payCollectionService.importCollection(multipartFile);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        if(CollectionUtils.isNotEmpty(errorDTOList)) {
            try {
                String key = IMPORT_COLLECTION + "_" + UserThreadLocal.get().getUserName();
                String errorFileName = fileShareDir + File.separator + System.currentTimeMillis() + "_????????????.xlsx";
                File file = new File(errorFileName);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                EasyExcel.write(file, IndividualImportErrorDTO.class).sheet("????????????").doWrite(errorDTOList);
                redisClient.set(key, errorFileName, expireTime);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            }
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "?????????????????????,?????????????????????");
        }
        return ResponseEntity.ok();
    }



    /**
     * ???????????????
     */
    @ApiOperation(value = "????????????  ????????????", httpMethod = "GET",produces = "application/octet-stream")
    @GetMapping("/downLoadTemplate")
    public void downLoadTemplate(HttpServletResponse response) throws Exception {
        // ???????????? ?????????????????????swagger ??????????????????????????????????????????????????????postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // ??????URLEncoder.encode???????????????????????? ?????????easyexcel????????????
        String fileName = URLEncoder.encode("????????????????????????", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcelFactory.write(response.getOutputStream(), BusinessPayCollectionImportDTO.class).sheet("????????????????????????").doWrite(new ArrayList());
    }

    private List<IndividualImportDTO> getExamples(){
        List<IndividualImportDTO> examples = new ArrayList<>();
        IndividualImportDTO dto = new IndividualImportDTO();
        dto.setBatchNo("090901");
        dto.setProvinceOrRegion("??????");
        dto.setEmployeeName("??????");
        dto.setEmployeeJobNum(10086);
        dto.setPhone("18797815131");
        dto.setAccountType("??????");

        dto.setSocialSecurityStopDate(new Date());
        dto.setLeaveDate(new Date());

        dto.setAccountName("??????");
        dto.setAccount("909");
        dto.setBankType("??????????????????");
        dto.setDepositBank("???????????????????????????????????????");
        dto.setProvince("??????");
        dto.setCity("?????????");
        dto.setElectronicInterBankNo("102290000017");
        dto.setAnnualQuota(BigDecimal.valueOf(100));
        dto.setIssuedUnit("????????????????????????????????????");

        dto.setReleaseOpinions("??????");
        dto.setServiceAgreement("??????");
        dto.setSelfOrAgency("??????");
        dto.setPlatformCompany("??????");
        dto.setVerificationAudit("??????");
        dto.setRemarks("??????");
        examples.add(dto);
        return examples;
    }
    /**
     * ??????????????? ?????????????????????
     */
    @ApiOperation(value = "????????????????????????", httpMethod = "GET")
    @GetMapping("/downLoadError")
    public void downLoadError(HttpServletResponse response) throws Exception {
        InputStream is = null;
        try {
            if (redisClient.get(IMPORT_COLLECTION + "_" + UserThreadLocal.get().getUserName()) == null) {
                throw new RuntimeException("???????????????????????????????????????");
            }
            String errorFileName = redisClient.get(IMPORT_COLLECTION + "_" + UserThreadLocal.get().getUserName());
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("??????????????????", response)).withTemplate(is).build();
            workBook.finish();
            File file = new File(errorFileName);
            if (file.exists()) file.delete();
            redisClient.delete(IMPORT_COLLECTION + "_" + UserThreadLocal.get().getUserName());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (is != null) is.close();
        }
    }

    /**
     * ?????? ??????????????????
     */
    @ApiOperation(value = "?????? ??????????????????", httpMethod = "POST")
    @PostMapping("/updateView")
    public ResponseEntity updateView(@RequestBody UpdateViewRequest request) throws Exception {
        try {
            payCollectionService.updateView(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.error("????????????");
        }
        return ResponseEntity.ok();
    }




}
