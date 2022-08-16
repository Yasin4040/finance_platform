package com.jtyjy.finance.manager.controller.budgetorganization;

import com.alibaba.fastjson.JSON;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.auth.anno.ApiDataAuthAnno;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetMonthAgent;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.response.MonthAgentMoneyInfo;
import com.jtyjy.finance.manager.service.BudgetMonthAgentService;
import com.jtyjy.finance.manager.service.FineOAService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.BudgetMonthAgentVO;
import com.jtyjy.finance.manager.vo.BudgetSubjectAgentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author User
 */
@Api(tags = {"预算编制-月度预算-月度动因、产品、分解"})
@RestController
@CrossOrigin
@RequestMapping("/api/monthAgent")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetMonthAgentController extends BaseController<BudgetMonthAgent> {

    @Value("${file.shareDir}")
    private String fileShareDir;

    @Value("${redis.file.key.expiretime}")
    private Integer expireTime;

    private final RedisClient redisClient;
    private final BudgetMonthAgentService budgetMonthAgentService;



    @ApiOperation(value = "查询月度动因（分页）", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "预算科目Id", name = "budgetSubjectId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "类型（1普通 2产品 3分解）", name = "type", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "动因名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "产品分类", name = "category", dataType = "String"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/monthAgentPage")
    public ResponseEntity<PageResult<BudgetMonthAgentVO>> yearAgentPage(@RequestParam(value = "budgetUnitId") Long budgetUnitId,
                                                                        @RequestParam(value = "budgetSubjectId") Long budgetSubjectId,
                                                                        @RequestParam(value = "monthId") Integer monthId,
                                                                        @RequestParam(value = "type") Integer type,
                                                                        @RequestParam(value = "name", required = false) String name,
                                                                        @RequestParam(value = "category", required = false) String category,
                                                                        @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                        @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetMonthAgentService.monthAgentPage(budgetUnitId, budgetSubjectId, monthId, type, name, page, rows,category));
    }

    @ApiOperation(value = "查询月度动因明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "月度动因Id", name = "monthAgentId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/monthAgentDetail")
    public ResponseEntity<List<Map<String, Object>>> monthAgentDetail(@RequestParam(value = "monthAgentId") Long monthAgentId) throws Exception {
        return ResponseEntity.ok(this.budgetMonthAgentService.monthAgentDetail(monthAgentId));
    }

    @ApiOperation(value = "修改月度动因", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "月度动因明细", name = "monthAgentId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "预算金额", name = "monthMoney", dataType = "Double", required = true),
            @ApiImplicitParam(value = "活动说明", name = "monthBusiness", dataType = "String", required = false),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/updateMonthAgent")
    public ResponseEntity<String> updateMonthAgent(@RequestParam(value = "monthAgentId") Long monthAgentId,
                                                   @RequestParam(value = "monthMoney") BigDecimal monthMoney,
                                                   @RequestParam(value = "monthBusiness",required = false) String monthBusiness) throws Exception {
        if(monthMoney.compareTo(new BigDecimal(0)) == 1 && StringUtils.isBlank(monthBusiness)){
            return ResponseEntity.error("月度预算大于零时动因说明为必填项");
        }
        this.budgetMonthAgentService.updateMonthAgent(monthAgentId, monthMoney, monthBusiness);
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "删除月度动因", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "月度动因Ids", name = "monthAgentIds", dataType = "List", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/deleteMonthAgent")
    public ResponseEntity<String> deleteMonthAgent(@RequestBody List<Long> monthAgentIds) {
        if (monthAgentIds == null || monthAgentIds.isEmpty()) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, "月度动因Ids不能为空");
        }
        this.budgetMonthAgentService.deleteMonthAgent(monthAgentIds);
        return ResponseEntity.ok("删除月度动因成功");
    }

    // 月度动因（普通、产品、分解）导入、导出 ----------------------------------------------------------------------------------------------------

    @ApiOperation(value = "月度动因导入模板下载", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "下载类型（1普通 2产品 3分解）", name = "type", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportMonthAgent")
    public void exportMonthAgent(@RequestParam("budgetUnitId") Long budgetUnitId,
                                 @RequestParam("monthId") Long monthId,
                                 @RequestParam("type") Integer type,
                                 HttpServletResponse response) throws Exception {
        // 查询该预算单位下所有的月度动因
        Map<String, List<BudgetMonthAgentVO>> templateMap = this.budgetMonthAgentService.exportMonthAgent(budgetUnitId, monthId, type);

        // 文件导出
        ResponseUtil.exportMonthAgentExcelFile(templateMap, type, EasyExcelUtil.getOutputStream(this.getMonthAgentFileName(type) + "导入模板", response));
    }

    @ApiOperation(value = "月度动因导入", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "文件", name = "file", dataType = "File", required = true),
            @ApiImplicitParam(value = "导入类型（1普通 2产品 3分解）", name = "type", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importMonthAgent")
    public ResponseEntity<String> importMonthAgent(@RequestParam("budgetUnitId") Long budgetUnitId,
                                                   @RequestParam("file") MultipartFile srcFile,
                                                   @RequestParam("type") Integer type) throws Exception {
        Map<String, List<List<String>>> errorDataMap = new LinkedHashMap<>();
        Map<String, List<List<String>>> excelDataMap = ResponseUtil.getMultipleExcelContent(srcFile);

        this.budgetMonthAgentService.importMonthAgentExcel(budgetUnitId, type, excelDataMap, errorDataMap);
        if (!errorDataMap.isEmpty()) {
            String empNo = UserThreadLocal.get().getUserName();
            String errorFileName = this.fileShareDir + File.separator + empNo + "_" + this.getMonthAgentFileName(type)
                    + "错误明细_" + System.currentTimeMillis() + ".json";

            // 创建错误明细文件
            FileUtils.writeStringToFile(new File(errorFileName), JSON.toJSONString(errorDataMap), "UTF-8");

            // 存入Redis键值记录, 并设置过期时间
            this.redisClient.set(empNo + "_monthAgentErrorData", errorFileName, this.expireTime);
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载!");
        }
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "月度动因文件导入错误明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "导入类型（1普通 2产品 3分解）", name = "type", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/exportErrors")
    public void exportErrors(@RequestParam("type") Integer type, HttpServletResponse response) throws Exception {
        String redisKey = UserThreadLocal.get().getUserName() + "_monthAgentErrorData";
        String redisValue = this.redisClient.get(redisKey);
        if (redisValue == null) {
            throw new RuntimeException(this.getMonthAgentFileName(type) + "错误明细不存在或已删除");
        }
        File file = new File(redisValue);
        String errorData = FileUtils.readFileToString(file, "UTF-8");
        HashMap<String, List<List<String>>> errorMap = JSON.parseObject(errorData, HashMap.class);

        // 文件导出
        ResponseUtil.exportMonthAgentExcelFile(errorMap, type, EasyExcelUtil.getOutputStream(this.getMonthAgentFileName(type) + "导入错误明细", response));

        // 删除文件
        FileUtils.forceDeleteOnExit(file);
        this.redisClient.delete(redisKey);
    }

    private String getMonthAgentFileName(Integer type) {
        String name = "月度动因";
        switch (type) {
            case 2:
                name = "月度产品";
                break;
            case 3:
                name = "月度分解";
                break;
            default:
        }
        return name;
    }

    @ApiOperation(value = "获取单位月度动因信息", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/getUnitMonthAgentInfo")
    public ResponseEntity<MonthAgentMoneyInfo> getUnitMonthAgentInfo(MonthAgentMoneyInfo bean) {
        String error = BaseController.validate(bean);
        if (StringUtils.isNotBlank(error)) {
            return ResponseEntity.error(error);
        }
        bean = this.budgetMonthAgentService.getUnitMonthAgentInfo(bean);
        return ResponseEntity.ok(bean);
    }

    @ApiOperation(value = "根据预算单位及月份查询动因", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long"),
            @ApiImplicitParam(value = "搜索名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "权限认证", name = "oauth", dataType = "Boolean"),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @ApiDataAuthAnno
    @GetMapping(value = "/listSubjectMonthAgent")
    public ResponseEntity<PageResult<BudgetSubjectAgentVO>> listSubjectMonthAgent(Long budgetUnitId,
                                                                                  String name,
                                                                                  Boolean oauth,
                                                                                  @RequestParam Long yearId,
                                                                                  @RequestParam Integer monthId,
                                                                                  @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                                  @RequestParam(value = "rows", defaultValue = "20") Integer rows) {

        HashMap<String, Object> paramMap = new HashMap<>(10);
        paramMap.put("yearId", yearId);
        paramMap.put("budgetUnitId", budgetUnitId);
        paramMap.put("monthId", monthId);
        paramMap.put("name", name);
        if (oauth == null || oauth) {
            paramMap.put("userId", UserThreadLocal.get().getUserId());
            paramMap.put("authSql", JdbcSqlThreadLocal.get());
        }
        return ResponseEntity.ok(this.budgetMonthAgentService.listSubjectMonthAgent(paramMap, page, rows));
    }

}
