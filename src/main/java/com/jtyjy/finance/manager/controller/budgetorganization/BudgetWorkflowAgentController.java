package com.jtyjy.finance.manager.controller.budgetorganization;

import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.finance.manager.bean.BudgetUnit;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetMonthAgentaddinfoService;
import com.jtyjy.finance.manager.service.BudgetYearAgentaddinfoService;
import com.jtyjy.finance.manager.service.BudgetYearAgentlendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author User
 */
@Api(tags = {"OA流程-预算编制"})
@RestController
@CrossOrigin
@RequestMapping("/api/workflowAgent")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetWorkflowAgentController extends BaseController<BudgetUnit> {

    private final BudgetYearAgentaddinfoService budgetYearAgentaddinfoService;
    private final BudgetMonthAgentaddinfoService budgetMonthAgentaddinfoService;
    private final BudgetYearAgentlendService budgetYearAgentlendService;

    @ApiOperation(value = "年度动因追加审核通过", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
    @PostMapping(value = "/endYearAgentAdd")
    public ResponseEntity<String> endYearAgentAdd(@RequestBody EcologyParams params) throws Exception {
        this.budgetYearAgentaddinfoService.endYearAgentAdd(params);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "年度动因追加退回", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
    @PostMapping(value = "/rejectYearAgentAdd")
    public ResponseEntity<String> rejectYearAgentAdd(@RequestBody EcologyParams params) {
        this.budgetYearAgentaddinfoService.rejectYearAgentAdd(params);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "月度动因追加审核通过", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
    @PostMapping(value = "/endMonthAgentAdd")
    public ResponseEntity<String> endMonthAgentAdd(@RequestBody EcologyParams params) throws Exception {
        this.budgetMonthAgentaddinfoService.endMonthAgentAdd(params);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "月度动因追加退回", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
    @PostMapping(value = "/rejectMonthAgentAdd")
    public ResponseEntity<String> rejectMonthAgentAdd(@RequestBody EcologyParams params) {
        this.budgetMonthAgentaddinfoService.rejectMonthAgentAdd(params);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "年度动因拆借审核通过", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
    @PostMapping(value = "/endYearAgentLend")
    public ResponseEntity<String> endYearAgentLend(@RequestBody EcologyParams params) throws Exception {
        this.budgetYearAgentlendService.endYearAgentLend(params);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "年度动因拆借退回", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
    @PostMapping(value = "/rejectYearAgentLend")
    public ResponseEntity<String> rejectYearAgentLend(@RequestBody EcologyParams params) {
        this.budgetYearAgentlendService.rejectYearAgentLend(params);
        return ResponseEntity.ok();
    }

}
