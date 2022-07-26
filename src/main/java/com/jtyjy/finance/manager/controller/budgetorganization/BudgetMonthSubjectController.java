package com.jtyjy.finance.manager.controller.budgetorganization;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.spring.SpringTools;
import com.jtyjy.finance.manager.bean.BudgetMonthSubject;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.easyexcel.MonthAgentCollectExcelData;
import com.jtyjy.finance.manager.future.SyncBudgetFutureTask;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetMonthAgentService;
import com.jtyjy.finance.manager.service.BudgetMonthSubjectService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.BudgetMonthAgentVO;
import com.jtyjy.finance.manager.vo.BudgetMonthSubjectVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author User
 */
@Api(tags = {"预算编制-月度预算-月度汇总"})
@RestController
@CrossOrigin
@RequestMapping("/api/monthSubject")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetMonthSubjectController extends BaseController<BudgetMonthSubject> {

    private final static Logger LOGGER = LoggerFactory.getLogger(BudgetMonthSubjectController.class);
    public final static String SYNC_MONTH_BUDGET_REDIS_PREFIX = "SYNC_MONTH_BUDGET_";

    private final RedisClient redisClient;
    private final BudgetMonthAgentService budgetMonthAgentService;
    private final BudgetMonthSubjectService budgetMonthSubjectService;

    @ApiOperation(value = "查询月度汇总", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/monthAgentSubject")
    public ResponseEntity<List<BudgetMonthSubjectVO>> monthAgentSubject(@RequestParam Long budgetUnitId, @RequestParam Long monthId) {
        return ResponseEntity.ok(this.budgetMonthSubjectService.monthAgentSubject(budgetUnitId, monthId));
    }

    @ApiOperation(value = "同步月度动因", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "String", required = true),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/syncMonthAgentData")
    public ResponseEntity<String> syncMonthAgentData(@RequestParam("budgetUnitId") Long budgetUnitId,
                                                     @RequestParam("monthId") Long monthId) {
        if (redisClient.exist(SYNC_MONTH_BUDGET_REDIS_PREFIX + budgetUnitId + "_" + monthId)) {
            return ResponseEntity.error("月度预算正在同步中，请不要重复操作！");
        }
        redisClient.set(SYNC_MONTH_BUDGET_REDIS_PREFIX + budgetUnitId + "_" + monthId, "syncBudgeting");
        try {
            budgetMonthSubjectService.syncMonthAgentData(budgetUnitId, monthId);
            return ResponseEntity.ok("同步月度动因成功");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.error(e.getMessage());
        } finally {
            redisClient.delete(BudgetMonthSubjectController.SYNC_MONTH_BUDGET_REDIS_PREFIX + budgetUnitId + "_" + monthId);
        }
        //FutureTask<Boolean> futureTask = new FutureTask<>(new SyncBudgetFutureTask(budgetUnitId, monthId, this.springTools, redisClient));
        //ExecutorService executorService = Executors.newFixedThreadPool(1);
        //executorService.submit(futureTask);
        // return ResponseEntity.ok("同步月度动因成功");
    }

    @ApiOperation(value = "提交月度预算", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/submitMonthBudget")
    public ResponseEntity<String> submitMonthBudget(@RequestParam("budgetUnitId") Long budgetUnitId,
                                                    @RequestParam("monthId") Long monthId) {
        WbUser wbUser = UserThreadLocal.get();

        this.budgetMonthSubjectService.submitMonthBudget(budgetUnitId, monthId, wbUser.getUserId(), wbUser.getDisplayName());
        return ResponseEntity.ok("提交月度预算成功");
    }

    @ApiOperation(value = "下载月度动因汇总明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportMonthAgentDetail")
    public void exportMonthAgentDetail(@RequestParam("budgetUnitId") Long budgetUnitId,
                                       @RequestParam("monthId") Long monthId,
                                       HttpServletResponse response) throws Exception {
        // 查询该预算单位下所有的月度动因
        Map<String, List<BudgetMonthAgentVO>> templateMap1 = this.budgetMonthAgentService.exportMonthAgent(budgetUnitId, monthId, 1);
        // 查询该预算单位下所有的月度产品
        Map<String, List<BudgetMonthAgentVO>> templateMap2 = this.budgetMonthAgentService.exportMonthAgent(budgetUnitId, monthId, 2);
        // 查询该预算单位下所有的月度分解
        Map<String, List<BudgetMonthAgentVO>> templateMap3 = this.budgetMonthAgentService.exportMonthAgent(budgetUnitId, monthId, 3);

        // 文件导出
        ResponseUtil.exportMonthAgentCollectExcelFile(templateMap1, templateMap2, templateMap3, EasyExcelUtil.getOutputStream("月度汇总明细表", response));
    }

    @ApiOperation(value = "下载月度动因汇总详情", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportMonthAgentCollect")
    public void exportMonthAgentCollect(@RequestParam("budgetUnitId") Long budgetUnitId,
                                        @RequestParam("monthId") Long monthId,
                                        HttpServletResponse response) throws Exception {
        // 查询该预算单位下所有的月度动因
        List<MonthAgentCollectExcelData> excelDataList = this.budgetMonthSubjectService.exportMonthAgentCollect(budgetUnitId, monthId);
        if (excelDataList.isEmpty()) {
            throw new Exception("没有可以导出的月度动因汇总信息");
        }
        MonthAgentCollectExcelData excelData = excelDataList.get(0);
        HashMap<String, Object> headMap = new HashMap<>(2);
        headMap.put("unitName", "预算单位:" + excelData.getUnitName());
        headMap.put("budgetTime", "时间:" + excelData.getBudgetTime());

        try (InputStream inputStream = EasyExcelUtil.getTemplateInputStream("monthAgentCollectTemplate.xlsx")) {
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
            ExcelWriter workBook = EasyExcelUtil.getExcelWriter(response, "月度汇总表", inputStream, MonthAgentCollectExcelData.class);
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            sheet.setSheetName("月度汇总");
            workBook.fill(excelDataList, fillConfig, sheet);
            workBook.fill(headMap, sheet);
            workBook.finish();
        }
    }

}
