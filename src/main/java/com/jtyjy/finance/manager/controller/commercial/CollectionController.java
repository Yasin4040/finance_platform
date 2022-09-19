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
 */@Api(tags = { "商务回款明细接口" })
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


    /********************************回款**********/

    /**
     * payment collection 回款员工个体户 分页模糊查询
     */
    @ApiOperation(value = "回款分页模糊查询", httpMethod = "GET")
    @GetMapping("/selectPage")
    public ResponseEntity<PageResult<BusinessPayCollection>> selectCollectionPage(@ModelAttribute CommissionQuery query) throws Exception {
        IPage<BusinessPayCollection> page = payCollectionService.selectPage(query);
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }


    /**
     * 导出
     */
    @ApiOperation(value = "回款明细  导出", httpMethod = "GET")
    @GetMapping("/exportCollection")
    public ResponseEntity exportCollection(@ModelAttribute CommissionQuery query, HttpServletResponse response) throws Exception {
        try {
            List<BusinessPayCollection> exportDTOList  =  payCollectionService.exportCollection(query);
            EasyExcelUtil.writeExcel(response,exportDTOList,"回款明细信息","回款明细信息",BusinessPayCollection.class);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * 导入。
     */
    @SneakyThrows
    @ApiOperation(value = "回款明细  导入", httpMethod = "POST")
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
                String errorFileName = fileShareDir + File.separator + System.currentTimeMillis() + "_错误信息.xlsx";
                File file = new File(errorFileName);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                EasyExcel.write(file, IndividualImportErrorDTO.class).sheet("错误信息").doWrite(errorDTOList);
                redisClient.set(key, errorFileName, expireTime);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            }
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载");
        }
        return ResponseEntity.ok();
    }



    /**
     * 下载模板。
     */
    @ApiOperation(value = "员工个体户  下载模板", httpMethod = "GET",produces = "application/octet-stream")
    @GetMapping("/downLoadTemplate")
    public void downLoadTemplate(HttpServletResponse response) throws Exception {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("员工个体户信息模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcelFactory.write(response.getOutputStream(), IndividualImportDTO.class).sheet("员工个体户信息模板").doWrite(new ArrayList());
    }

    private List<IndividualImportDTO> getExamples(){
        List<IndividualImportDTO> examples = new ArrayList<>();
        IndividualImportDTO dto = new IndividualImportDTO();
        dto.setBatchNo("090901");
        dto.setProvinceOrRegion("福建");
        dto.setEmployeeName("张三");
        dto.setEmployeeJobNum(10086);
        dto.setPhone("18797815131");
        dto.setAccountType("公户");

        dto.setSocialSecurityStopDate(new Date());
        dto.setLeaveDate(new Date());

        dto.setAccountName("测试");
        dto.setAccount("909");
        dto.setBankType("中国工商银行");
        dto.setDepositBank("工商银行上海市石门一路支行");
        dto.setProvince("上海");
        dto.setCity("市辖区");
        dto.setElectronicInterBankNo("102290000017");
        dto.setAnnualQuota(BigDecimal.valueOf(100));
        dto.setIssuedUnit("江西慧谷文化传播有限公司");

        dto.setReleaseOpinions("测试");
        dto.setServiceAgreement("测试");
        dto.setSelfOrAgency("自办");
        dto.setPlatformCompany("测试");
        dto.setVerificationAudit("核定");
        dto.setRemarks("测试");
        examples.add(dto);
        return examples;
    }
    /**
     * 员工个体户 下载错误明细。
     */
    @ApiOperation(value = "下载回款错误明细", httpMethod = "GET")
    @GetMapping("/downLoadError")
    public void downLoadError(HttpServletResponse response) throws Exception {
        InputStream is = null;
        try {
            if (redisClient.get(IMPORT_COLLECTION + "_" + UserThreadLocal.get().getUserName()) == null) {
                throw new RuntimeException("没有回款错误明细可供下载。");
            }
            String errorFileName = redisClient.get(IMPORT_COLLECTION + "_" + UserThreadLocal.get().getUserName());
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("回款错误明细", response)).withTemplate(is).build();
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
     * 批量 修改是否允许
     */
    @ApiOperation(value = "根据不同登陆用户 获取相应提成数据", httpMethod = "GET")
    @PostMapping("/updateView")
    public ResponseEntity updateView(@RequestBody UpdateViewRequest request) throws Exception {
        try {
            payCollectionService.updateView(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.error("修改失败");
        }
        return ResponseEntity.ok();
    }




}
