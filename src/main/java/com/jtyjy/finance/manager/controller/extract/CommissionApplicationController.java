package com.jtyjy.finance.manager.controller.extract;

import com.alibaba.excel.EasyExcelFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.dto.individual.IndividualImportDTO;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationService;
import com.jtyjy.finance.manager.vo.application.CommissionApplicationInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Description: 支付申请单
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 14:36
 */
@Api(tags = {"提成支付申请单"})
@RestController
@RequestMapping("/api/commissionApplication")
public class CommissionApplicationController {
    private final BudgetExtractCommissionApplicationService applicationService;

    public CommissionApplicationController(BudgetExtractCommissionApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * 提成支付申请单
     */
    @ApiOperation(value = "提成支付申请单", httpMethod = "GET")
    @ApiImplicitParam(value = "提成单号", name = "extractSumId", dataType = "String", required = true)
    @GetMapping("/getApplicationInfo")
    public ResponseEntity<PageResult<CommissionApplicationInfoVO>> getApplicationInfo(Integer extractSumId) throws Exception {
        Page<CommissionApplicationInfoVO> page = new Page<>();
//        Commission
        return ResponseEntity.ok(PageResult.apply(page.getTotal(), page.getRecords()));
    }

    /**
     * 下载模板。
     */
    @ApiOperation(value = "提成明细  下载模板", httpMethod = "GET",produces = "application/octet-stream")
    @GetMapping("/downLoadTemplate")
    public void downLoadTemplate(HttpServletResponse response) throws Exception {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("员工个体户信息模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcelFactory.write(response.getOutputStream(), IndividualImportDTO.class).sheet("员工个体户信息模板").doWrite(new ArrayList<>());
    }

    @ApiOperation(value = "提成导入模板（商务提成组）", httpMethod = "POST")
    @PostMapping("/importTemplate")
    public ResponseEntity importTemplate(@RequestParam("file") MultipartFile multipartFile) {
        try {
            applicationService.importIndividual(multipartFile);
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
        return ResponseEntity.ok();
    }

}
