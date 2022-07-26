package com.jtyjy.finance.manager.controller.budgetorganization;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetMonthEndUnit;
import com.jtyjy.finance.manager.bean.BudgetUnit;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.BudgetAuditDTO;
import com.jtyjy.finance.manager.easyexcel.MonthAgentCollectExcelData;
import com.jtyjy.finance.manager.service.BudgetMonthEndUnitService;
import com.jtyjy.finance.manager.service.BudgetMonthSubjectService;
import com.jtyjy.finance.manager.service.BudgetUnitService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author User
 */
@Api(tags = {"预算编制-预算审核"})
@RestController
@CrossOrigin
@RequestMapping("/api/budgetAudit")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetAuditController extends BaseController<BudgetUnit> {

    private final BudgetUnitService budgetUnitService;
    private final BudgetMonthSubjectService budgetMonthSubjectService;
    private final BudgetMonthEndUnitService budgetMonthEndUnitService;

    @ApiOperation(value = "查询年度审核列表（分页）", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long"),
            @ApiImplicitParam(value = "预算单位名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listYearAuditPage")
    public ResponseEntity<PageResult<BudgetUnit>> listYearAuditPage(Long yearId,
                                                                    String name,
                                                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                    @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetUnitService.listYearAuditPage(page, rows, yearId, name));
    }

    @ApiOperation(value = "年度审核-通过", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id,多个用逗号分割", name = "budgetUnitIds", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/yearBudgetAuditPass")
    public ResponseEntity<String> yearBudgetAuditPass(@RequestParam String budgetUnitIds) throws Exception {
        String msg = this.budgetUnitService.batchYearBudgetAudit(budgetUnitIds,null,1);
        if(StringUtils.isNotBlank(msg)){
            return ResponseEntity.error("审核失败:"+msg);
        }
        return ResponseEntity.ok("审核成功");

    }



    @ApiOperation(value = "年度审核-退回", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id,多个用逗号分割", name = "budgetUnitIds", dataType = "String", required = true),
            @ApiImplicitParam(value = "说明", name = "remark", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/yearBudgetAuditBack")
    public ResponseEntity<String> yearBudgetAuditBack(@RequestParam String budgetUnitIds, @RequestParam String remark) throws Exception {
        String msg = this.budgetUnitService.batchYearBudgetAudit(budgetUnitIds,remark,2);
        if(StringUtils.isNotBlank(msg)){
            return ResponseEntity.error(msg);
        }
        //this.budgetUnitService.yearBudgetAudit(budgetUnitId, remark, 2);
        return ResponseEntity.ok("退回成功");
    }

    @ApiOperation(value = "年度审核-强制退回,多个用逗号分割", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitIds", dataType = "String", required = true),
            @ApiImplicitParam(value = "说明", name = "remark", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping(value = "/yearBudgetAuditForceBack")
    public ResponseEntity<String> yearBudgetAuditForceBack(@RequestParam String budgetUnitIds, String remark) throws Exception {
        String msg = this.budgetUnitService.batchYearBudgetAudit(budgetUnitIds,remark,3);
        if(StringUtils.isNotBlank(msg)){
            return ResponseEntity.error(msg);
        }
        //this.budgetUnitService.yearBudgetAudit(budgetUnitId, remark, 3);
        return ResponseEntity.ok("强制退回成功");
    }

    // ----------------------------------------------------------------------------------------------------

    @ApiOperation(value = "查询月度审核列表（分页）", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long"),
            @ApiImplicitParam(value = "预算单位Id", name = "budgetUnitId", dataType = "Long"),
            @ApiImplicitParam(value = "预算单位名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Long"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/listMonthAuditPage")
    public ResponseEntity<PageResult<BudgetMonthEndUnit>> listMonthAuditPage(Long yearId,
                                                                             Long budgetUnitId,
                                                                             String name,
                                                                             Long monthId,
                                                                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                             @RequestParam(value = "rows", defaultValue = "20") Integer rows) {
        return ResponseEntity.ok(this.budgetMonthEndUnitService.listMonthAuditPage(page, rows, yearId, budgetUnitId, monthId, name));
    }

    @ApiOperation(value = "月度审核-通过", httpMethod = "POST")
    @PostMapping(value = "/monthBudgetAuditPass")
    public ResponseEntity<String> monthBudgetAuditPass(@RequestBody BudgetAuditDTO dto) {
        String msg = this.budgetMonthEndUnitService.batchMonthBudgetAudit(dto, 1);
        if(StringUtils.isNotBlank(msg)){
            return ResponseEntity.error(msg);
        }
        //this.budgetMonthEndUnitService.monthBudgetAudit(budgetUnitId, monthId, null, 1);
        return ResponseEntity.ok("审核成功");
    }

    @ApiOperation(value = "月度审核-退回", httpMethod = "POST")
    @PostMapping(value = "/monthBudgetAuditBack")
    public ResponseEntity<String> monthBudgetAuditBack(@RequestBody BudgetAuditDTO dto) {
        String msg = this.budgetMonthEndUnitService.batchMonthBudgetAudit(dto, 2);
        if(StringUtils.isNotBlank(msg)){
            return ResponseEntity.error(msg);
        }
        //this.budgetMonthEndUnitService.monthBudgetAudit(budgetUnitId, monthId, remark, 2);
        return ResponseEntity.ok("退回成功");
    }

    @ApiOperation(value = "月度审核-强制退回,多个用逗号分割", httpMethod = "POST")
    @PostMapping(value = "/monthBudgetAuditForgeBack")
    public ResponseEntity<String> monthBudgetAuditForgeBack(@RequestBody BudgetAuditDTO dto) {
        String msg = this.budgetMonthEndUnitService.batchMonthBudgetAudit(dto, 3);
        if(StringUtils.isNotBlank(msg)){
            return ResponseEntity.error(msg);
        }
        //this.budgetMonthEndUnitService.monthBudgetAudit(budgetUnitId, monthId, null, 3);
        return ResponseEntity.ok("强制退回成功");
    }

    @ApiOperation(value = "公司月度汇总", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别Id", name = "yearId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "月份Id", name = "monthId", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportCompanyMonthAgentCollect")
    public void exportCompanyMonthAgentCollect(@RequestParam("yearId") Long yearId,
                                               @RequestParam("monthId") Long monthId,
                                               HttpServletResponse response) throws Exception {
        // 查询该预算单位下所有的月度动因
        List<MonthAgentCollectExcelData> excelDataList = this.budgetMonthSubjectService.exportCompanyMonthAgentCollect(yearId, monthId);
        if (excelDataList.isEmpty()) {
            throw new Exception("没有可以导出的公司月度动因汇总信息");
        }
        HashMap<String, Object> headMap = new HashMap<>(2);
        headMap.put("unitName", "");
        headMap.put("budgetTime", "");

        try (InputStream inputStream = EasyExcelUtil.getTemplateInputStream("monthAgentCollectTemplate.xlsx")) {
            ExcelWriter workBook = EasyExcelUtil.getExcelWriter(response, "公司月度汇总表", inputStream, MonthAgentCollectExcelData.class);
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            sheet.setSheetName("月度汇总");
            workBook.fill(excelDataList, sheet);
            workBook.fill(headMap, sheet);
            workBook.finish();
        }
    }

}
