package com.jtyjy.finance.manager.controller.extract;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplication;
import com.jtyjy.finance.manager.bean.BudgetExtractCommissionApplicationLog;
import com.jtyjy.finance.manager.enmus.LogStatusEnum;
import com.jtyjy.finance.manager.enmus.OperationNodeEnum;
import com.jtyjy.finance.manager.query.PageQuery;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationLogService;
import com.jtyjy.finance.manager.service.BudgetExtractCommissionApplicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
            query.setPage(1);
            query.setRows(-1);
            Optional<BudgetExtractCommissionApplication> applicationBySumId = applicationService.getApplicationBySumId(sumId);
            if (applicationBySumId.isPresent()) {
                Page<BudgetExtractCommissionApplicationLog> page = logService.page(new Page<>(query.getPage(), query.getRows())
                        , new LambdaQueryWrapper<BudgetExtractCommissionApplicationLog>()
                                .eq(BudgetExtractCommissionApplicationLog::getApplicationId, applicationBySumId.get().getId()));
                List<BudgetExtractCommissionApplicationLog> records = page.getRecords();
                for (BudgetExtractCommissionApplicationLog record : records) {
                    record.setStatusName(LogStatusEnum.getValue(record.getStatus()));

                    record.setNodeName(StringUtils.isNotBlank(record.getNodeName())?record.getNodeName():OperationNodeEnum.getValue(record.getNode()));
                }
                return ResponseEntity.ok(PageResult.apply(page.getTotal(),page.getRecords()));
            }
            return ResponseEntity.ok();
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }

    @ApiOperation(value = "记录 OA的审批 拒绝记录", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
    @PostMapping(value = "/doRecordOA")
    public ResponseEntity<String> doRecordOA(@RequestBody EcologyParams params) throws Exception{
        logService.doRecordOA(params);
        return ResponseEntity.ok();
    }
}
