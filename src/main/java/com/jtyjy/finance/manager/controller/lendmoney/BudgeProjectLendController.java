package com.jtyjy.finance.manager.controller.lendmoney;

import com.alibaba.fastjson.JSON;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.ProjectLendReimbursementDTO;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetProjectlendsumService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author User
 */
@Api(tags = {"借款管理-项目借款"})
@RestController
@CrossOrigin
@RequestMapping("/api/projectLend")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgeProjectLendController extends BaseController<BudgetProjectlendsum> {

    @Value("${file.shareDir}")
    private String fileShareDir;

    @Value("${redis.file.key.expiretime}")
    private Integer expireTime;

    private final RedisClient redisClient;
    private final BudgetProjectlendsumService budgetProjectlendsumService;

    @ApiOperation(value = "查询项目借款（分页）", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "项目名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "项目类型 1项目预领 2项目借支", name = "type", dataType = "Integer"),
            @ApiImplicitParam(value = "审核状态 0未审核 1已审核", name = "verifyFlag", dataType = "Integer"),
            @ApiImplicitParam(value = "预算单位名称", name = "unitName", dataType = "String"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listProjectLendPage")
    public ResponseEntity<PageResult<BudgetProjectLendSumVO>> listProjectLendPage(String name,
                                                                                  Integer type,
                                                                                  Integer verifyFlag,
                                                                                  String unitName,
                                                                                  @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                  @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        HashMap<String, Object> paramMap = new HashMap<>(5);
        paramMap.put("name", name);
        paramMap.put("type", type);
        paramMap.put("verifyFlag", verifyFlag);
        paramMap.put("unitName", unitName);
        return ResponseEntity.ok(this.budgetProjectlendsumService.listProjectLendPage(page, rows, paramMap));
    }

    @ApiOperation(value = "修改转账付款单位", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "项目借款Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "开票单位Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/updatePayMoneyUnitId")
    public ResponseEntity<String> updatePayMoneyUnitId(@RequestParam Long id, @RequestParam Long bUnitId) {
        this.budgetProjectlendsumService.updatePayMoneyUnitId(id, bUnitId);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "审核数据", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/verify")
    public ResponseEntity<String> verify(@RequestParam Long id) {
        this.budgetProjectlendsumService.verify(id);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "查询借款明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listLendMoneyDetail")
    public ResponseEntity<PageResult<BudgetProjectLendDetailVO>> listLendMoneyDetail(Long id,
                                                                                     @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                     @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetProjectlendsumService.listLendMoneyDetail(page, rows, id));
    }

    @ApiOperation(value = "查询还款明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listRepayMoneyDetail")
    public ResponseEntity<PageResult<BudgetProjectRepayDetailVO>> listRepayMoneyDetail(Long id,
                                                                                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                       @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetProjectlendsumService.listRepayMoneyDetail(page, rows, id));
    }

    @ApiOperation(value = "删除借款", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "ids", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/deleteLendMoney")
    public ResponseEntity<String> deleteLendMoney(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL);
        }
        this.budgetProjectlendsumService.deleteLendMoney(ids);
        return ResponseEntity.ok("删除借款成功");
    }

    // ----------------------------------------------------------------------------------------------------

    @ApiOperation(value = "批量“达标/完成”项目借款单", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Ids", name = "ids", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/batchReachStandard")
    public ResponseEntity<String> batchReachStandard(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL);
        }
        this.budgetProjectlendsumService.batchReachStandard(ids);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "批量“不达标/未完成”项目借款单", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Ids", name = "ids", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/batchNotReachStandard")
    public ResponseEntity<String> batchNotReachStandard(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL);
        }
        this.budgetProjectlendsumService.batchNotReachStandard(ids);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "项目借款单允许还款", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Ids", name = "ids", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/allowBuckleMoney")
    public ResponseEntity<String> allowBuckleMoney(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL);
        }
        this.budgetProjectlendsumService.allowBuckleMoney(ids);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "项目借款明细导出", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportProjectLend")
    public void exportProjectLend(@RequestParam Long id, HttpServletResponse response) throws Exception {
        List<List<String>> resultList = this.budgetProjectlendsumService.exportProjectLend(id);

        // 文件导出
        ResponseUtil.exportProjectLendDetailExcelFile(resultList, EasyExcelUtil.getOutputStream("项目借款明细导出", response));
    }

    // ----------------------------------------------------------------------------------------------------

    @ApiOperation(value = "查询利息规则", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listInterestRules")
    public ResponseEntity<List<BudgetLendInterestRule>> listInterestRules(Long id) {
        return ResponseEntity.ok(this.budgetProjectlendsumService.listInterestRules(id));
    }

    @ApiOperation(value = "新增利息规则", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "bean", dataType = "BudgetLendInterestRule", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/addInterestRule")
    public ResponseEntity<String> addInterestRule(@Valid @RequestBody BudgetLendInterestRule bean) {
        this.budgetProjectlendsumService.addInterestRule(bean);
        return ResponseEntity.ok("新增利息规则成功");
    }

    @ApiOperation(value = "修改利息规则", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "bean", dataType = "BudgetLendInterestRule", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/updateInterestRule")
    public ResponseEntity<String> updateInterestRule(@Valid @RequestBody BudgetLendInterestRule bean) {
        this.budgetProjectlendsumService.updateInterestRule(bean);
        return ResponseEntity.ok("修改利息规则成功");
    }

    @ApiOperation(value = "删除利息规则", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "bean", dataType = "BudgetLendInterestRule", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/deleteInterestRules")
    public ResponseEntity<String> deleteInterestRules(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL);
        }
        this.budgetProjectlendsumService.deleteInterestRules(ids);
        return ResponseEntity.ok("删除利息规则成功");
    }

    // ----------------------------------------------------------------------------------------------------

    @ApiOperation(value = "验证模板导出", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportValidate")
    public void exportValidate(@RequestParam Long id, HttpServletResponse response) throws Exception {
        Map<String, List<List<String>>> dataMap = this.budgetProjectlendsumService.exportValidate(id);

        // 文件导出
        ResponseUtil.exportProjectValidateExcelFile(dataMap, EasyExcelUtil.getOutputStream("验证模板", response));
    }

    @ApiOperation(value = "达标验证导入模板", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportValidateTemplate")
    public void exportValidateTemplate(HttpServletResponse response) throws Exception {
        // 文件导出
        ResponseUtil.exportProjectValidateExcelFile(null, EasyExcelUtil.getOutputStream("达标验证导入模板", response));
    }

    @ApiOperation(value = "达标验证导入", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importValidate")
    public ResponseEntity<String> importValidate(@RequestParam Long id,
                                                 @RequestParam("file") MultipartFile srcFile) throws Exception {
        List<List<String>> excelDataList = ResponseUtil.getSingleExcelContent(srcFile);

        List<List<String>> errorDataList = this.budgetProjectlendsumService.importValidateComplete(id, excelDataList);
        if (!errorDataList.isEmpty()) {
            String empNo = UserThreadLocal.get().getUserName();
            String errorFileName = this.fileShareDir + File.separator + empNo + "_达标验证导入错误明细_" + System.currentTimeMillis() + ".json";

            // 创建错误明细文件
            FileUtils.writeStringToFile(new File(errorFileName), JSON.toJSONString(errorDataList), "UTF-8");

            // 存入Redis键值记录, 并设置过期时间
            this.redisClient.set(empNo + "_validateErrorData", errorFileName, this.expireTime);
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载!");
        }
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "达标验证导入错误明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/exportValidateErrors")
    public void exportValidateErrors(HttpServletResponse response) throws Exception {
        String redisKey = UserThreadLocal.get().getUserName() + "_validateErrorData";
        String redisValue = this.redisClient.get(redisKey);
        if (redisValue == null) {
            throw new RuntimeException("达标验证导入错误明细不存在或已删除");
        }
        File file = new File(redisValue);
        String errorData = FileUtils.readFileToString(file, "UTF-8");
        List<List<String>> errorList = JSON.parseObject(errorData, List.class);

        // 文件导出
        HashMap<String, List<List<String>>> resultMap = new HashMap<>(1);
        resultMap.put("达标验证导入错误明细", errorList);
        ResponseUtil.exportProjectValidateExcelFile(resultMap, EasyExcelUtil.getOutputStream("达标验证导入错误明细", response));

        // 删除文件
        FileUtils.forceDeleteOnExit(file);
        this.redisClient.delete(redisKey);
    }

    @ApiOperation(value = "利息模板导出", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportInterest")
    public void exportInterest(@RequestParam Long id, HttpServletResponse response) throws Exception {
        Map<String, List<List<String>>> dataMap = this.budgetProjectlendsumService.exportInterest(id);

        // 文件导出
        ResponseUtil.exportInterestExcelFile(dataMap, EasyExcelUtil.getOutputStream("利息模板", response));
    }

    @ApiOperation(value = "下载利息导入模板", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportInterestTemplate")
    public void exportInterestTemplate(HttpServletResponse response) throws Exception {
        // 文件导出
        ResponseUtil.exportInterestExcelFile(null, EasyExcelUtil.getOutputStream("利息导入模板", response));
    }

    @ApiOperation(value = "利息导入", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importInterest")
    public ResponseEntity<String> importInterest(@RequestParam Long id,
                                                 @RequestParam("file") MultipartFile srcFile) throws Exception {
        List<List<String>> excelDataList = ResponseUtil.getSingleExcelContent(srcFile);

        List<List<String>> errorDataList = this.budgetProjectlendsumService.importInterest(id, excelDataList);
        if (!errorDataList.isEmpty()) {
            String empNo = UserThreadLocal.get().getUserName();
            String errorFileName = this.fileShareDir + File.separator + empNo + "_利息导入错误明细_" + System.currentTimeMillis() + ".json";

            // 创建错误明细文件
            FileUtils.writeStringToFile(new File(errorFileName), JSON.toJSONString(errorDataList), "UTF-8");

            // 存入Redis键值记录, 并设置过期时间
            this.redisClient.set(empNo + "_interestErrorData", errorFileName, this.expireTime);
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载!");
        }
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "利息导入错误明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/exportInterestErrors")
    public void exportInterestErrors(HttpServletResponse response) throws Exception {
        String redisKey = UserThreadLocal.get().getUserName() + "_interestErrorData";
        String redisValue = this.redisClient.get(redisKey);
        if (redisValue == null) {
            throw new RuntimeException("利息导入错误明细不存在或已删除");
        }
        File file = new File(redisValue);
        String errorData = FileUtils.readFileToString(file, "UTF-8");
        List<List<String>> errorList = JSON.parseObject(errorData, List.class);

        // 文件导出
        HashMap<String, List<List<String>>> resultMap = new HashMap<>(1);
        resultMap.put("利息导入错误明细", errorList);
        ResponseUtil.exportInterestExcelFile(resultMap, EasyExcelUtil.getOutputStream("利息导入错误明细", response));

        // 删除文件
        FileUtils.forceDeleteOnExit(file);
        this.redisClient.delete(redisKey);
    }

    @ApiOperation(value = "项目还款记录明细导出", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/exportRepayMoneyDetail")
    public void exportRepayMoneyDetail(@RequestParam Long id, HttpServletResponse response) throws Exception {
        Map<String, List<List<String>>> resultMap = this.budgetProjectlendsumService.exportRepayMoneyDetail(id);

        // 文件导出
        ResponseUtil.exportProjectRepayMoneyExcelFile(resultMap, EasyExcelUtil.getOutputStream("项目还款记录明细导出", response));
    }

    @ApiOperation(value = "下载项目借款导入模板", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportLendTemplate")
    public void exportLendTemplate(HttpServletResponse response) throws Exception {
        // 文件导出
        ResponseUtil.exportProjectLendExcelFile(null, EasyExcelUtil.getOutputStream("项目借款导入模板", response));
    }

    @ApiOperation(value = "项目借款导入", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importProjectLend")
    public ResponseEntity<String> importProjectLend(@RequestParam Long id,
                                                    @RequestParam("file") MultipartFile srcFile) throws Exception {
        List<List<String>> excelDataList = ResponseUtil.getSingleExcelContent(srcFile);

        List<List<String>> errorDataList = this.budgetProjectlendsumService.importProjectLend(id, excelDataList);
        if (!errorDataList.isEmpty()) {
            String empNo = UserThreadLocal.get().getUserName();
            String errorFileName = this.fileShareDir + File.separator + empNo + "_项目借款导入错误明细_" + System.currentTimeMillis() + ".json";

            // 创建错误明细文件
            FileUtils.writeStringToFile(new File(errorFileName), JSON.toJSONString(errorDataList), "UTF-8");

            // 存入Redis键值记录, 并设置过期时间
            this.redisClient.set(empNo + "_projectLendErrorData", errorFileName, this.expireTime);
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载!");
        }
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "项目借款导入错误明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/exportLendErrors")
    public void exportLendErrors(HttpServletResponse response) throws Exception {
        String redisKey = UserThreadLocal.get().getUserName() + "_projectLendErrorData";
        String redisValue = this.redisClient.get(redisKey);
        if (redisValue == null) {
            throw new RuntimeException("项目借款导入错误明细不存在或已删除");
        }
        File file = new File(redisValue);
        String errorData = FileUtils.readFileToString(file, "UTF-8");
        List<List<String>> errorList = JSON.parseObject(errorData, List.class);

        // 文件导出
        ResponseUtil.exportProjectLendExcelFile(errorList, EasyExcelUtil.getOutputStream("项目借款导入错误明细", response));

        // 删除文件
        FileUtils.forceDeleteOnExit(file);
        this.redisClient.delete(redisKey);
    }

    // ----------------------------------------------------------------------------------------------------

    @ApiOperation(value = "获取报销开票单位", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/curUserInvoiceUnit")
    public ResponseEntity<List<BudgetBillingUnit>> curUserInvoiceUnit() {
        return ResponseEntity.ok(this.budgetProjectlendsumService.curUserInvoiceUnit());
    }

    @ApiOperation(value = "获取付款开票单位账户", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "开票单位Ids", name = "ids", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/curUserPaymentUnitAccount")
    public ResponseEntity<List<PaymentUnitVO>> curUserPaymentUnitAccount(@RequestBody List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL);
        }
        return ResponseEntity.ok(this.budgetProjectlendsumService.curUserPaymentUnitAccount(ids));
    }

    @ApiOperation(value = "获取可报销月度动因", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listMonthAgentByBx")
    public ResponseEntity<List<BxMonthAgentVO>> listMonthAgentByBx(@RequestParam Long yearId,
                                                                   @RequestParam Long budgetUnitId,
                                                                   @RequestParam Long monthId) {
        return ResponseEntity.ok(this.budgetProjectlendsumService.listMonthAgentByBx(yearId, budgetUnitId, monthId));
    }

    @ApiOperation(value = "项目借款报销冲账明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/projectLendBxPaymentDetail")
    public ResponseEntity<List<BudgetProjectlendbxpayment>> projectLendBxPaymentDetail(@RequestParam Long id) {
        return ResponseEntity.ok(this.budgetProjectlendsumService.projectLendBxPaymentDetail(id));
    }

    @ApiOperation(value = "项目借款报销转账明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/projectLendBxTransDetail")
    public ResponseEntity<List<BudgetProjectlendbxtrans>> projectLendBxTransDetail(@RequestParam Long id) {
        return ResponseEntity.ok(this.budgetProjectlendsumService.projectLendBxTransDetail(id));
    }

    @ApiOperation(value = "项目借款报销明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键Id", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/projectLendBxDetail")
    public ResponseEntity<List<BudgetProjectlendbxdetail>> projectLendBxDetail(@RequestParam Long id) {
        return ResponseEntity.ok(this.budgetProjectlendsumService.projectLendBxDetail(id));
    }

    @ApiOperation(value = "项目借款保存报销单", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "Object", name = "bean", dataType = "ProjectLendReimbursementDTO", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/saveReimbursementData")
    public ResponseEntity<String> saveReimbursementData(@Valid @RequestBody ProjectLendReimbursementDTO bean) throws Exception {
        this.budgetProjectlendsumService.saveReimbursementData(bean);
        return ResponseEntity.ok("保存成功");
    }

}
