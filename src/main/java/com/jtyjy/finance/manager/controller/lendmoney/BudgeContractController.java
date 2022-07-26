package com.jtyjy.finance.manager.controller.lendmoney;

import com.alibaba.excel.EasyExcel;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetContract;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetContractService;
import com.jtyjy.finance.manager.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author User
 */
@Api(tags = {"借款管理-合同借款"})
@RestController
@CrossOrigin
@RequestMapping("/api/contract")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgeContractController extends BaseController<BudgetContract> {

    private final BudgetContractService budgetContractService;

    @ApiOperation(value = "查询合同列表（分页）", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "合同名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "合同编号", name = "contractCode", dataType = "String"),
            @ApiImplicitParam(value = "合同签订日期", name = "signDate", dataType = "Date"),
            @ApiImplicitParam(value = "合同终止日期", name = "terminationDate", dataType = "Date"),
            @ApiImplicitParam(value = "对方单位名称", name = "otherPartyUnit", dataType = "String"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listContractPage")
    public ResponseEntity<PageResult<BudgetContractVO>> listContractPage(String name,
                                                                         String contractCode,
                                                                         String otherPartyUnit,
                                                                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date signDate,
                                                                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date terminationDate,
                                                                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                         @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("name", name);
        paramMap.put("contractCode", contractCode);
        paramMap.put("otherPartyUnit", otherPartyUnit);
        paramMap.put("signDate", signDate != null ? Constants.FORMAT_10.format(signDate) : null);
        paramMap.put("terminationDate", terminationDate != null ? Constants.FORMAT_10.format(terminationDate) : null);
        return ResponseEntity.ok(this.budgetContractService.listContractPage(page, rows, paramMap));
    }

    @ApiOperation(value = "终止合同", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "合同Id", name = "id", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/stopContract")
    public ResponseEntity<String> stopContract(@RequestParam Long id) {
        this.budgetContractService.stopContract(id);
        return ResponseEntity.ok("合同终止成功");
    }

    @ApiOperation(value = "合同付款明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "合同Id", name = "id", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/getPayMoneyDetail")
    public ResponseEntity<PageResult<BudgetPayMoneyDetailVO>> getPayMoneyDetail(@RequestParam Long id,
                                                                                @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetContractService.getPayMoneyDetail(page, rows, id));
    }

    @ApiOperation(value = "合同冲账明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "合同Id", name = "id", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/getStrikeMoneyDetail")
    public ResponseEntity<PageResult<BudgetStrikeMoneyDetailVO>> getStrikeMoneyDetail(@RequestParam Long id,
                                                                                      @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                      @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetContractService.getStrikeMoneyDetail(page, rows, id));
    }

    // ----------------------------------------------------------------------------------------------------

    @ApiOperation(value = "查询合同借款列表（分页）", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "员工姓名/工号", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "还款状态 0:未还清 1:已还清", name = "repaymentStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "付款状态 0:等待付款；1：接收付款；2：正在付款；3：已经付款", name = "payMoneyStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "借款日期", name = "lendDate", dataType = "Date"),
            @ApiImplicitParam(value = "借款类型 15:合同借款 16:非合同借款", name = "lendType", dataType = "Integer"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listContractLendPage")
    public ResponseEntity<PageResult<BudgetContractLendVO>> listContractLendPage(String name,
                                                                                 Integer repaymentStatus,
                                                                                 Integer payMoneyStatus,
                                                                                 Integer lendType,
                                                                                 @DateTimeFormat(pattern = "yyyy-MM-dd") Date lendDate,
                                                                                 @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                 @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("name", name);
        paramMap.put("repaymentStatus", repaymentStatus);
        paramMap.put("payMoneyStatus", payMoneyStatus);
        paramMap.put("lendDate", lendDate != null ? Constants.FORMAT_10.format(lendDate) : null);
        paramMap.put("lendType", lendType);
        return ResponseEntity.ok(this.budgetContractService.listContractLendPage(page, rows, paramMap));
    }

    @ApiOperation(value = "导出合同报表", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "合同名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "合同编号", name = "contractCode", dataType = "String"),
            @ApiImplicitParam(value = "合同签订日期", name = "signDate", dataType = "Date"),
            @ApiImplicitParam(value = "合同终止日期", name = "terminationDate", dataType = "Date"),
            @ApiImplicitParam(value = "对方单位名称", name = "otherPartyUnit", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportContract")
    public void exportContract(String name, String contractCode, String otherPartyUnit,
                               @DateTimeFormat(pattern = "yyyy-MM-dd") Date signDate,
                               @DateTimeFormat(pattern = "yyyy-MM-dd") Date terminationDate,
                               HttpServletResponse response) throws IOException {
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("name", name);
        paramMap.put("contractCode", contractCode);
        paramMap.put("otherPartyUnit", otherPartyUnit);
        paramMap.put("signDate", signDate != null ? Constants.FORMAT_10.format(signDate) : null);
        paramMap.put("terminationDate", terminationDate != null ? Constants.FORMAT_10.format(terminationDate) : null);
        List<BudgetContractExcelVO> vos = this.budgetContractService.listContract(paramMap);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("合同报表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xlsx\"");
        EasyExcel.write(response.getOutputStream(),BudgetContractExcelVO.class).sheet("合同报表").doWrite(vos);
    }

    @ApiOperation(value = "导出合同支出报表", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "员工姓名/工号", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "还款状态 0:未还清 1:已还清", name = "repaymentStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "付款状态 0:等待付款；1：接收付款；2：正在付款；3：已经付款", name = "payMoneyStatus", dataType = "Integer"),
            @ApiImplicitParam(value = "借款日期", name = "lendDate", dataType = "Date"),
            @ApiImplicitParam(value = "借款类型 15:合同借款 16:非合同借款", name = "lendType", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportContractLend")
    public void exportContractLend(String name,Integer repaymentStatus,Integer payMoneyStatus,
                                   Integer lendType, @DateTimeFormat(pattern = "yyyy-MM-dd") Date lendDate,
                                   HttpServletResponse response ) throws Exception {
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("name", name);
        paramMap.put("repaymentStatus", repaymentStatus);
        paramMap.put("payMoneyStatus", payMoneyStatus);
        paramMap.put("lendDate", lendDate != null ? Constants.FORMAT_10.format(lendDate) : null);
        paramMap.put("lendType", lendType);
        this.budgetContractService.exportContractLend(paramMap,response);
    }

}
