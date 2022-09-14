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
    private final BudgetUnitService budgetUnitService;
    public AccountEntryController(ExtractAccountEntryTaskService entryTaskService, BudgetUnitService budgetUnitService) {
        this.entryTaskService = entryTaskService;
        this.budgetUnitService = budgetUnitService;
    }

    @ApiOperation(value = "获取需要做的核算入账list", httpMethod = "GET")
    @GetMapping("/getList")
    public ResponseEntity<PageResult<ExtractAccountEntryTask>> getList( @ModelAttribute AccountEntryQuery query) {
        try {
            //加上人员权限。TODO
            //获取当前人员 拥有哪些预算单位。
            String empNo = UserThreadLocal.getEmpNo();
            List<String> deptIds = budgetUnitService.getBaseUnitIdListByAccountingNo(empNo);
            if(CollectionUtils.isEmpty(deptIds)){
                return ResponseEntity.ok(PageResult.apply(0,null));
            }
            Page<ExtractAccountEntryTask> page = entryTaskService.page(new Page<>(query.getPage(), query.getRows()), new LambdaQueryWrapper<ExtractAccountEntryTask>()
                    .eq(query.getStatus()!=null,ExtractAccountEntryTask::getStatus, query.getStatus())
                    .in(CollectionUtils.isNotEmpty(deptIds),ExtractAccountEntryTask::getDeptId, deptIds)
                    .like(StringUtils.isNotBlank( query.getExtractCode()),ExtractAccountEntryTask::getExtractCode, query.getExtractCode())
                    .like(StringUtils.isNotBlank( query.getExtractMonth()),ExtractAccountEntryTask::getExtractMonth, query.getExtractMonth())
                    .like(StringUtils.isNotBlank( query.getDeptName()),ExtractAccountEntryTask::getDeptName, query.getDeptName()));
            List<ExtractAccountEntryTask> records = page.getRecords();
            for (ExtractAccountEntryTask record : records) {
                record.setStatusName(record.getStatus()==0?"核算中":"入账完成");
            }
            return ResponseEntity.ok(PageResult.apply(page.getTotal(),page.getRecords()));
        } catch (Exception e) {
            return ResponseEntity.error(e.getMessage());
        }
    }

    @ApiOperation(value = "做账完成", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
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
}
