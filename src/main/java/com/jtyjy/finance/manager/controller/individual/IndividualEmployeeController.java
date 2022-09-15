package com.jtyjy.finance.manager.controller.individual;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.IndividualEmployeeFiles;
import com.jtyjy.finance.manager.dto.individual.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.query.individual.IndividualFilesQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeFilesService;
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

import static com.jtyjy.finance.manager.constants.Constants.IMPORT_INDIVIDUAL_FILE;
import static com.jtyjy.finance.manager.constants.Constants.IMPORT_INDIVIDUAL_TICKET;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/25.
 * Time: 15:28
 */
@Api(tags = {"员工个体户档案"})
@RestController
@RequestMapping("/api/individualEmployee")
@Slf4j
public class IndividualEmployeeController {
    //员工个体户
    private  final IndividualEmployeeFilesService filesService;
    @Value("${file.shareDir}")
    private String fileShareDir;

    @Value("${redis.file.key.expiretime}")
    private Integer expireTime;
    private final RedisClient redisClient;
    public IndividualEmployeeController(IndividualEmployeeFilesService filesService, RedisClient redisClient) {
        this.filesService = filesService;
        this.redisClient = redisClient;
    }

    /**
     * 员工个体户 分页模糊查询
     */
    @ApiOperation(value = "分页模糊查询", httpMethod = "GET")
    @GetMapping("/selectPage")
    public ResponseEntity<PageResult<IndividualEmployeeFilesVO>> selectPage(@ModelAttribute IndividualFilesQuery query) throws Exception {
        IPage<IndividualEmployeeFilesVO> page = filesService.selectPage(query);
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }

    /**
     * 员工个体户  新增
     */
    @ApiOperation(value = "员工个体户  新增", httpMethod = "POST")
    @PostMapping("/add")
    public ResponseEntity addIndividual(@RequestBody IndividualEmployeeFilesDTO dto) throws Exception {

        try {
            filesService.addIndividual(dto);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }
    /**
     * 户名是否重复 查询
     */
    @ApiOperation(value = "员工个体户  新增 返回数量 >0 就存在", httpMethod = "POST")
    @PostMapping("/findRepeat")
    public ResponseEntity findRepeat(@RequestBody IndividualRepeatDTO dto) throws Exception {
        try {
           Integer exist = filesService.findRepeat(dto);
            return ResponseEntity.ok(exist);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }

    /**
     * 员工个体户  修改信息
     */
    @ApiOperation(value = "员工个体户  修改信息", httpMethod = "POST")
    @PostMapping("/update")
    public ResponseEntity updateIndividual(@RequestBody IndividualEmployeeFiles file) {
        try {
            filesService.updateIndividual(file);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }
    /**
     * 员工个体户  修改状态  停用 启用。
     */
    @ApiOperation(value = "员工个体户  修改状态  停用 启用。", httpMethod = "POST")
    @PostMapping("/updateStatus")
    public ResponseEntity updateIndividualStatus(@RequestBody IndividualEmployeeFilesStatusDTO statusDTO) {
        try {
            filesService.updateIndividualStatus(statusDTO);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     *  新增发票。获取基础信息
     */
    @ApiOperation(value = "新增发票。获取基础信息", httpMethod = "GET")
    @GetMapping("/getIndividualInfoList")
    public ResponseEntity getIndividualInfoList(@RequestParam String name) throws Exception {
            IndividualFilesQuery query = new IndividualFilesQuery();
            query.setAccountName(name);
            query.setPage(1);
            query.setRows(-1);
        return this.selectPage(query);
    }

    /**
     * 导出
     */
    @ApiOperation(value = "员工个体户  导出", httpMethod = "GET")
    @GetMapping("/exportIndividual")
    public ResponseEntity exportIndividual(@ModelAttribute IndividualFilesQuery query,HttpServletResponse response) throws Exception {
        // writeExcel(HttpServletResponse response, List<? extends Object> data, String fileName, String sheetName, Class clazz)
        //   @ExcelProperty("字符串标题")

        try {
            List<IndividualExportDTO> exportDTOList  =  filesService.exportIndividual(query);
//            for (int i = 1; i < exportDTOList.size()+1; i++) {
//                IndividualExportDTO individualExportDTO = exportDTOList.get(0);
//                individualExportDTO.setId(i);
//            }
            EasyExcelUtil.writeExcel(response,exportDTOList,"员工个体户信息","员工个体户信息",IndividualExportDTO.class);
//            return null;
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * 导入。
     */
    @SneakyThrows
    @ApiOperation(value = "员工个体户 导入", httpMethod = "POST")
    @PostMapping("/importIndividual")
    public ResponseEntity importIndividual(@RequestParam("file") MultipartFile multipartFile,HttpServletResponse response) {
        List<IndividualImportErrorDTO> errorDTOList = new ArrayList<>();
        try {
             errorDTOList = filesService.importIndividual(multipartFile);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        if(CollectionUtils.isNotEmpty(errorDTOList)) {

                InputStream iss = null;
                try {
                    String key = IMPORT_INDIVIDUAL_FILE + "_" + UserThreadLocal.get().getUserName();
                    String errorFileName = fileShareDir + File.separator + System.currentTimeMillis() + "_错误信息.xlsx";
                    ExcelWriter workBook = EasyExcel.write(new File(errorFileName), IndividualImportErrorDTO.class).build();
                    WriteSheet sheet = EasyExcel.writerSheet(0).build();
                    workBook.fill(errorDTOList, sheet);
                    workBook.finish();
                    redisClient.set(key, errorFileName, expireTime);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage(), e);
                }
                return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载");

//            EasyExcelUtil.writeExcel(response, errorDTOList, "员工个体户错误明细", "员工个体户错误明细", IndividualImportErrorDTO.class);
//
//            return null;
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
        EasyExcelFactory.write(response.getOutputStream(), IndividualImportDTO.class).sheet("员工个体户信息模板").doWrite(getExamples());
    }

    private List<IndividualImportDTO> getExamples(){
        List<IndividualImportDTO> examples = new ArrayList<>();
        IndividualImportDTO dto = new IndividualImportDTO();
        dto.setEmployeeName("张三");
        dto.setEmployeeJobNum(20297);
        dto.setAnnualQuota(BigDecimal.valueOf(100));
        dto.setIssuedUnit("江西金太阳教育");
        dto.setAccount("账号");
        dto.setAccountName("户名");
        dto.setAccountType("公户");
        dto.setPhone("18797815131");
        dto.setPlatformCompany("...");
        dto.setBatchNo("20220901");
        dto.setDepositBank("招商银行");
        dto.setSocialSecurityStopDate(new Date());
        dto.setLeaveDate(new Date());
        examples.add(dto);
        return examples;
    }
    /**
     * 员工个体户 下载错误明细。
     */
    @ApiOperation(value = "下载员工个体户错误明细", httpMethod = "GET")
    @GetMapping("/downLoadError")
    public void downLoadError(HttpServletResponse response) throws Exception {
        InputStream is = null;
        try {
            if (redisClient.get(IMPORT_INDIVIDUAL_FILE + "_" + UserThreadLocal.get().getUserName()) == null) {
                throw new RuntimeException("没有员工个体户错误明细可供下载。");
            }
            String errorFileName = redisClient.get(IMPORT_INDIVIDUAL_FILE + "_" + UserThreadLocal.get().getUserName());
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("员工个体户错误明细", response)).withTemplate(is).build();
            workBook.finish();
            File file = new File(errorFileName);
            if (file.exists()) file.delete();
            redisClient.delete(IMPORT_INDIVIDUAL_FILE + "_" + UserThreadLocal.get().getUserName());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (is != null) is.close();
        }
    }
}
