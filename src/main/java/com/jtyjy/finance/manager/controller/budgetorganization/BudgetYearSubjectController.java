package com.jtyjy.finance.manager.controller.budgetorganization;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.jtyjy.core.auth.anno.ApiDataAuthAnno;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.spring.SpringTools;
import com.jtyjy.finance.manager.bean.BudgetYearSubject;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.easyexcel.YearAgentCollectExcelData;
import com.jtyjy.finance.manager.easyexcel.YearAgentDetailExcelData;
import com.jtyjy.finance.manager.future.SyncBudgetFutureTask;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetYearSubjectService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.vo.BudgetUnitTree;
import com.jtyjy.finance.manager.vo.BudgetYearSubjectVO;
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
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author User
 */
@Api(tags = {"预算编制-年度预算-年度汇总"})
@RestController
@CrossOrigin
@RequestMapping("/api/yearSubject")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetYearSubjectController extends BaseController<BudgetYearSubject> {

    private final static Logger LOGGER = LoggerFactory.getLogger(BudgetYearSubjectController.class);
    public final static String SYNC_BUDGET_REDIS_PREFIX = "SYNC_BUDGET_";

    private final RedisClient redisClient;
    private final SpringTools springTools;
    private final BudgetYearSubjectService budgetYearSubjectService;

    @ApiOperation(value = "预算单位目录树(权限控制)", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @ApiDataAuthAnno
    @GetMapping(value = "/listUnit")
    public ResponseEntity<List<BudgetUnitTree>> listUnit(@RequestParam(value = "yearId") Long yearId) {
        return ResponseEntity.ok(this.budgetYearSubjectService.listUnit(yearId));
    }

    @ApiOperation(value = "查询年度汇总", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/yearAgentSubject")
    public ResponseEntity<List<BudgetYearSubjectVO>> yearAgentSubject(@RequestParam Long budgetUnitId) {
        return ResponseEntity.ok(this.budgetYearSubjectService.yearAgentSubject(budgetUnitId));
    }

    @ApiOperation(value = "同步年度动因", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/syncYearAgentData")
    public ResponseEntity<String> syncYearAgentData(@RequestParam Long budgetUnitId) {
        if (redisClient.exist(SYNC_BUDGET_REDIS_PREFIX + budgetUnitId)) {
            return ResponseEntity.error("年度预算正在同步中，请不要重复操作！");
        }
        redisClient.set(SYNC_BUDGET_REDIS_PREFIX + budgetUnitId, "syncBudgeting");

        try {
            budgetYearSubjectService.syncYearAgentData(budgetUnitId);
            return ResponseEntity.ok("同步年度动因成功");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.error(e.getMessage());
        } finally {
            redisClient.delete(BudgetYearSubjectController.SYNC_BUDGET_REDIS_PREFIX + budgetUnitId);
        }

       // FutureTask<Boolean> futureTask = new FutureTask<>(new SyncBudgetFutureTask(budgetUnitId, null, this.springTools, redisClient));
       // ExecutorService executorService = Executors.newFixedThreadPool(1);
       // executorService.submit(futureTask);
       // return ResponseEntity.ok("同步年度动因成功");
    }

    @ApiOperation(value = "提交年度预算", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/submitYearBudget")
    public ResponseEntity<String> submitYearBudget(@RequestParam Long budgetUnitId) {
        WbUser wbUser = UserThreadLocal.get();

        this.budgetYearSubjectService.submitYearBudget(budgetUnitId, wbUser.getUserId(), wbUser.getDisplayName());
        return ResponseEntity.ok("提交年度预算成功");
    }

    @ApiOperation(value = "下载年度动因汇总明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportYearAgentDetail")
    public void exportYearAgentDetail(@RequestParam("budgetUnitId") Long budgetUnitId,
                                      HttpServletResponse response) throws Exception {
        // 查询该预算单位下所有的年度动因
        List<YearAgentDetailExcelData> excelDataList = this.budgetYearSubjectService.exportYearAgentDetail(budgetUnitId);
        if (excelDataList.isEmpty()) {
            throw new Exception("没有可以导出的年度动因汇总信息");
        }
        HashMap<String, Object> headMap = new HashMap<>(2);
        headMap.put("year", excelDataList.get(0).getYearName());
        headMap.put("currentTime", Constants.FORMAT_10.format(new Date()));

        try (InputStream inputStream = EasyExcelUtil.getTemplateInputStream("yearAgentDetailTemplate.xlsx")) {
            ExcelWriter workBook = EasyExcelUtil.getExcelWriter(response, "年度动因汇总明细表", inputStream, YearAgentDetailExcelData.class);
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            sheet.setSheetName("年度动因汇总明细");
            workBook.fill(excelDataList, sheet);
            workBook.fill(headMap, sheet);
            workBook.finish();
        }
    }

    @ApiOperation(value = "下载年度动因汇总详情", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportYearAgentCollect")
    public void exportYearAgentCollect(@RequestParam("budgetUnitId") Long budgetUnitId,
                                       HttpServletResponse response) throws Exception {
        // 查询该预算单位下所有的年度动因
        List<YearAgentCollectExcelData> excelDataList = this.budgetYearSubjectService.exportYearAgentCollect(budgetUnitId);

        BigDecimal totalMoney = BigDecimal.ZERO;
        for (YearAgentCollectExcelData v : excelDataList) {
            totalMoney = totalMoney.add(v.getTotal());
        }
        HashMap<String, Object> headMap = new HashMap<>(2);
        headMap.put("totalMoney", totalMoney);

        try (InputStream inputStream = EasyExcelUtil.getTemplateInputStream("yearAgentCollectTemplate.xlsx")) {
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
            ExcelWriter workBook = EasyExcelUtil.getExcelWriter(response, "年度汇总表", inputStream, YearAgentCollectExcelData.class);
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            sheet.setSheetName("年度汇总");
            workBook.fill(excelDataList, fillConfig, sheet);
            workBook.fill(headMap, sheet);
            workBook.finish();
        }
    }

}
