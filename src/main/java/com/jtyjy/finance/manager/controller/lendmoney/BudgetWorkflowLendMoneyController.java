package com.jtyjy.finance.manager.controller.lendmoney;

import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.ecology.EcologyParams;
import com.jtyjy.finance.manager.bean.BudgetLendmoney;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetContractService;
import com.jtyjy.finance.manager.service.BudgetLendmoneyService;
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
@Api(tags = {"OA流程-借款管理"})
@RestController
@CrossOrigin
@RequestMapping("/api/workflowLendMoney")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetWorkflowLendMoneyController extends BaseController<BudgetLendmoney> {

    private final BudgetLendmoneyService budgetLendmoneyService;
    private final BudgetContractService budgetContractService;

    @ApiOperation(value = "个人借款", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
    @PostMapping(value = "/personalLendMoney")
    public ResponseEntity<String> personalLendMoney(@RequestBody EcologyParams params) throws Exception{
        this.budgetLendmoneyService.personalLendMoney(params);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "费用借款/备用金借款", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
    @PostMapping(value = "/costLendMoney")
    public ResponseEntity<String> costLendMoney(@RequestBody EcologyParams params) throws Exception{
        this.budgetLendmoneyService.costLendMoney(params);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "销售政策支持借款", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
    @PostMapping(value = "/projectLendMoney")
    public ResponseEntity<String> projectLendMoney(@RequestBody EcologyParams params) {
        this.budgetLendmoneyService.projectLendMoney(params);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "合同签订", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
    @PostMapping(value = "/contractSigning")
    public ResponseEntity<String> contractSigning(@RequestBody EcologyParams params) {
        this.budgetContractService.contractSigning(params);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "合同借款", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "params", dataType = "EcologyParams", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @NoLoginAnno
    @PostMapping(value = "/contractLendMoney")
    public ResponseEntity<String> contractLendMoney(@RequestBody EcologyParams params) {
        this.budgetContractService.contractLendMoney(params);
        return ResponseEntity.ok();
    }

}
