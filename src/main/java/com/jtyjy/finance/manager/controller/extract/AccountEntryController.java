package com.jtyjy.finance.manager.controller.extract;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.ExtractAccountEntryTask;
import com.jtyjy.finance.manager.dto.commission.EntryCompletedDTO;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.query.AccountEntryQuery;
import com.jtyjy.finance.manager.service.BudgetUnitService;
import com.jtyjy.finance.manager.service.ExtractAccountEntryTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 支付申请单
 * Created by ZiYao Lee on 2022/08/26.
 * Time: 14:36
 */
@Api(tags = {"核算入账"})
@RestController
@RequestMapping("/api/accountEntry")
@Slf4j
public class AccountEntryController {
    private final ExtractAccountEntryTaskService entryTaskService;

    public AccountEntryController(ExtractAccountEntryTaskService entryTaskService) {
        this.entryTaskService = entryTaskService;
    }

    @ApiOperation(value = "获取需要做的核算入账list", httpMethod = "GET")
    @GetMapping("/getList")
    public ResponseEntity<PageResult<ExtractAccountEntryTask>> getList( @ModelAttribute AccountEntryQuery query) {
        try {
            Page<ExtractAccountEntryTask> page = entryTaskService.getList(query);
            return ResponseEntity.ok(PageResult.apply(page.getTotal(),page.getRecords()));
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }

    @ApiOperation(value = "入账完成", httpMethod = "POST")
    @PostMapping(value = "/entryCompleted")
    public ResponseEntity<String> entryCompleted(@RequestBody EntryCompletedDTO dto) {
        try {
            entryTaskService.entryCompleted(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.error(e.getMessage()==null?e.toString():e.getMessage());
        }
        return ResponseEntity.ok();
    }
    @ApiOperation(value = "添加预算", httpMethod = "POST")
    @NoLoginAnno
    @PostMapping(value = "/addEntryTask")
    public ResponseEntity<String> addEntryTask(@RequestBody  String extractMonth) {
        try {
            entryTaskService.addEntryTask(false,new ArrayList<>(),extractMonth);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.error(e.getMessage()==null?e.toString():e.getMessage());
        }
        return ResponseEntity.ok();
    }

}
