package com.jtyjy.finance.manager.controller.extract;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplication;
import com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplicationLog;
import com.jtyjy.finance.manager.query.PageQuery;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationLogService;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Description: 支付申请单
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 14:36
 */
@Api(tags = {"提成支付申请单日志"})
@RestController
@RequestMapping("/api/applicationLog")
@Slf4j
public class CommissionApplicationLogController {
    private final BudgetExtractCommissionApplicationLogService logService;
    private final BudgetExtractCommissionApplicationService applicationService;

    public CommissionApplicationLogController(BudgetExtractCommissionApplicationLogService logService, BudgetExtractCommissionApplicationService applicationService) {
        this.logService = logService;
        this.applicationService = applicationService;
    }

    @ApiOperation(value = "获取申请单 流转记录", httpMethod = "GET")
    @GetMapping("/getList")
    public ResponseEntity<PageResult<BudgetExtractCommissionApplicationLog>> getList(@RequestParam String sumId,@ModelAttribute PageQuery query) {
        try {
            Optional<BudgetExtractCommissionApplication> applicationBySumId = applicationService.getApplicationBySumId(sumId);
            if (applicationBySumId.isPresent()) {
                Page<BudgetExtractCommissionApplicationLog> page = logService.page(new Page<>(query.getPageNum(), query.getPageSize())
                        , new LambdaQueryWrapper<BudgetExtractCommissionApplicationLog>()
                                .eq(BudgetExtractCommissionApplicationLog::getApplicationId, sumId));
                return ResponseEntity.ok(PageResult.apply(page.getTotal(),page.getRecords()));
            }
            return ResponseEntity.ok();
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }


//    @ApiOperation(value = "记录 OA的审批 拒绝记录", httpMethod = "GET")
//    @GetMapping("/getList")
//    public ResponseEntity<PageResult<BudgetExtractCommissionApplicationLog>> getList(@RequestParam String sumId,@ModelAttribute PageQuery query) {
//        try {
//            Optional<BudgetExtractCommissionApplication> applicationBySumId = applicationService.getApplicationBySumId(sumId);
//            if (applicationBySumId.isPresent()) {
//                Page<BudgetExtractCommissionApplicationLog> page = logService.page(new Page<>(query.getPageNum(), query.getPageSize())
//                        , new LambdaQueryWrapper<BudgetExtractCommissionApplicationLog>()
//                                .eq(BudgetExtractCommissionApplicationLog::getApplicationId, sumId));
//                return ResponseEntity.ok(PageResult.apply(page.getTotal(),page.getRecords()));
//            }
//            return ResponseEntity.ok();
//        } catch (Exception e) {
//            return ResponseEntity.error(e.getMessage());
//        }
//    }

}
