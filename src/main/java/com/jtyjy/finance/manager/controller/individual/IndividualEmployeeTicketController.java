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
import com.jtyjy.finance.manager.dto.commission.FeeImportErrorDTO;
import com.jtyjy.finance.manager.dto.individual.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.query.individual.IndividualTicketQuery;
import com.jtyjy.finance.manager.service.IndividualEmployeeTicketReceiptInfoService;
import com.jtyjy.finance.manager.service.IndividualEmployeeTicketReceiptService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketPageVO;
import com.jtyjy.finance.manager.vo.individual.IndividualTicketVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.jtyjy.finance.manager.constants.Constants.IMPORT_INDIVIDUAL_TICKET;

/**
 * Description:
 * Created by ZiYao Lee on 2022/08/25.
 * Time: 15:28
 */
@Api(tags = {"员工个体户收票信息"})
@RestController
@RequestMapping("/api/individualTicket")
@Slf4j
public class IndividualEmployeeTicketController {
    //员工个体户收票信息
    //员工个体户
    private  final IndividualEmployeeTicketReceiptInfoService ticketService;
    @Value("${file.shareDir}")
    private String fileShareDir;

    @Value("${redis.file.key.expiretime}")
    private Integer expireTime;
    private final RedisClient redisClient;
    //员工个体户收票信息
    //员工个体户
    private  final IndividualEmployeeTicketReceiptService mainService;

    public IndividualEmployeeTicketController(IndividualEmployeeTicketReceiptInfoService ticketService, RedisClient redisClient, IndividualEmployeeTicketReceiptService mainService) {
        this.ticketService = ticketService;
        this.redisClient = redisClient;
        this.mainService = mainService;
    }

    /**
     * 员工个体户 档案 分页模糊查询
     */
    @ApiOperation(value = "分页模糊查询", httpMethod = "GET")
    @GetMapping("/selectPage")
    public ResponseEntity<PageResult<IndividualTicketVO>> selectPage(@ModelAttribute IndividualTicketQuery query) throws Exception {
        IPage<IndividualTicketVO> page = ticketService.selectPage(query);
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }

    /**
     * 员工个体户 档案 分页模糊查询
     */
    @ApiOperation(value = "新 分页模糊查询", httpMethod = "GET")
    @GetMapping("/selectMainPage")
    public ResponseEntity<PageResult<IndividualTicketPageVO>> selectMainPage(@ModelAttribute IndividualTicketQuery query) throws Exception {
        IPage<IndividualTicketPageVO> page = mainService.selectPage(query);
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }
    /**
     * 获取个体户 收票明细
     */
    @ApiOperation(value = "获取个体户 收票明细", httpMethod = "GET")
    //individualId
    @GetMapping("/getIndividualInfo")
    public ResponseEntity<IndividualTicketInfoDTO> getIndividualInfo(@RequestParam String ticketId){
        IndividualTicketInfoDTO dto  = ticketService.getIndividualInfo(ticketId);
        return ResponseEntity.ok(dto);
    }

    /**
     * add 新增收票信息
     */
    @ApiOperation(value = " add 新增收票信息", httpMethod = "POST")
    @PostMapping("/addTicket")
    public ResponseEntity addTicket(@RequestBody IndividualTicketDTO dto) {

        try {
            ticketService.addTicket(dto);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }




    /**
     * 员工个体户  修改信息
     */
    @ApiOperation(value = "员工个体户  修改信息", httpMethod = "POST")
    @PostMapping("/updateTicket")
    public ResponseEntity updateTicket(@RequestBody IndividualTicketDTO dto) {
        try {
            ticketService.updateTicket(dto);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * 删除主表
     */
    @ApiOperation(value = "删除主表", httpMethod = "POST")
    @PostMapping("/delMainTicket")
    public ResponseEntity delMainTicket(@RequestBody List<Long> ids) {
        try {
            ticketService.delTicket(ids);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }
    /**
     * 员工个体户发票维护  删除
     */
    @ApiOperation(value = "个体户收票信息模板    删除信息", httpMethod = "POST")
    @PostMapping("/deleteTicket")
    public ResponseEntity deleteTicket(String id) {
        try {
            ticketService.removeById(id);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * 导出
     */
    @ApiOperation(value = "个体户收票信息  导出", httpMethod = "GET")
    @GetMapping("/exportTicket")
    public ResponseEntity exportTicket(@ModelAttribute IndividualTicketQuery query, HttpServletResponse response) throws Exception {
        try {
            query.setPage(1);
            query.setRows(-1);
            IPage<IndividualTicketVO> individualTicketVOIPage = ticketService.selectPage(query);
            List<IndividualTicketVO> records = individualTicketVOIPage.getRecords();

            EasyExcelUtil.writeExcel(response,records,"员工个体户信息","员工个体户信息", IndividualTicketVO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * 导入。
     */
    @ApiOperation(value = "个体户收票信息 导入", httpMethod = "POST")
    @PostMapping("/importTicket")
    public ResponseEntity importTicket(@RequestParam("file") MultipartFile multipartFile,HttpServletResponse response) throws Exception {
        try {
            List<IndividualTicketImportErrorDTO> errorDTOList = ticketService.importTicket(multipartFile);
            if (CollectionUtils.isNotEmpty(errorDTOList)) {
                InputStream iss = null;
                try {
                    String key = IMPORT_INDIVIDUAL_TICKET + "_" + UserThreadLocal.get().getUserName();
                    String errorFileName = fileShareDir + File.separator + System.currentTimeMillis() + "_错误信息.xlsx";
                    File file = new File(errorFileName);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    EasyExcel.write(file, IndividualTicketImportErrorDTO.class).sheet("错误信息").doWrite(errorDTOList);
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
            }
        }catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

    /**
     * 下载模板。
     */
    @ApiOperation(value = "个体户收票信息模板  下载模板", httpMethod = "GET",produces = "application/octet-stream")
    @GetMapping("/downLoadTemplate")
    public void downLoadTemplate(HttpServletResponse response) throws Exception {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("个体户收票信息模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcelFactory.write(response.getOutputStream(), IndividualTicketImportDTO.class).sheet("个体户收票信息模板").doWrite(getExamples());
    }

    private List<IndividualTicketImportDTO> getExamples(){
        List<IndividualTicketImportDTO> examples = new ArrayList<>();
        IndividualTicketImportDTO dto = new IndividualTicketImportDTO();
        dto.setEmployeeName("张三");
        dto.setEmployeeJobNum(10086);
        dto.setIndividualName("张三个体户名称");
        dto.setMonth(10);
        dto.setYear(2022);
        examples.add(dto);
        return examples;
    }

    /**
     * 下载错误明细。
     */
    @ApiOperation(value = "下载个体户收票错误明细", httpMethod = "GET")
    @GetMapping("/downLoadError")
    public void downLoadError(HttpServletResponse response) throws Exception {
        InputStream is = null;
        try {
            if (redisClient.get(IMPORT_INDIVIDUAL_TICKET + "_" + UserThreadLocal.get().getUserName()) == null) {
                throw new RuntimeException("没有个体户收票错误明细可供下载。");
            }
            String errorFileName = redisClient.get(IMPORT_INDIVIDUAL_TICKET + "_" + UserThreadLocal.get().getUserName());
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("个体户收票错误明细", response)).withTemplate(is).build();
            workBook.finish();
            File file = new File(errorFileName);
            if (file.exists()) file.delete();
            redisClient.delete(IMPORT_INDIVIDUAL_TICKET + "_" + UserThreadLocal.get().getUserName());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (is != null) is.close();
        }
    }
}
