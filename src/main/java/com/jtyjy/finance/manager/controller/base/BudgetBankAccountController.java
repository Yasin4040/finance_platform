package com.jtyjy.finance.manager.controller.base;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.BudgetBankAccount;
import com.jtyjy.finance.manager.bean.DbInvokeRecord;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.easyexcel.BankAccountExcelData;
import com.jtyjy.finance.manager.easyexcel.BatchStopExcelData;
import com.jtyjy.finance.manager.easyexcel.ImportBankAccountExcelData;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetBankAccountService;
import com.jtyjy.finance.manager.service.InvokeRecordService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.PoiExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.BankAccountVO;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.result.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shubo
 */
@Api(tags = { "银行账户管理接口" })
@RestController
@RequestMapping("/api/base/bankAccount")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetBankAccountController extends BaseController<BudgetAgentExecuteView> {	
	
    private final BudgetBankAccountService service;
    private final InvokeRecordService invokeRecordService;

    public final static String BAIMPORT = "BAIMPORT";

    @Value("${file.shareDir}")
    private String fileShareDir;

    @Value("${redis.file.key.expiretime}")
    private Integer expiretime;

    private final RedisClient redis;
    /**
     * 新增/修改（修改时需送id）
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "银行账户id", name = "id", dataType = "Integer"),
            @ApiImplicitParam(value = "工号", name = "code", dataType = "String", required = true),
            @ApiImplicitParam(value = "名称", name = "pname", dataType = "String", required = true),
            @ApiImplicitParam(value = "户名", name = "accountName", dataType = "String"),
            @ApiImplicitParam(value = "账户类型 1：对内，2：对外", name = "accountType", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "账号", name = "bankAccount", dataType = "String", required = true),
            @ApiImplicitParam(value = "工资账户 0：否 1：是", name = "wagesFlag", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "开户行电子联行号", name = "branchCode", dataType = "String", required = true),
            @ApiImplicitParam(value = "停用标志 0：启用 1：停用", name = "stopFlag", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "排序号", name = "orderNo", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "备注", name = "remark", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(@Valid BankAccountVO vo, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }
        BudgetBankAccount bean = new BudgetBankAccount(vo);
        bean.setUpdateTime(new Date());
        WbUser wbUser = UserThreadLocal.get();
        bean.setUpdateTime(new Date());
        bean.setUpdateBy(wbUser.getDisplayName() + "(" + wbUser.getUserName() + ")");
        if(null == vo.getId() || 0 == vo.getId().intValue()) {
            retError = this.service.addUserAccount(bean, wbUser);
            if (StringUtils.isBlank(retError)){
                return ResponseEntity.ok();
            }else {
                return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_EXIST, retError);
            }
        }else {
            retError = this.service.editUserAccount(bean, wbUser);
            if (StringUtils.isBlank(retError)) {
                return ResponseEntity.ok();
            }else {
                return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_EXIST, retError);
            }
        }
        
    }

    /**
     * 按照主键批量删除
     */
    @ApiOperation(value = "按照主键批量删除", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键（多个主键以“,”分割）", name = "ids", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("deleteByIds")
    public ResponseResult deleteByIds(String ids) {
        this.service.removeByIds(Arrays.asList(ids.split(",")));
        return ResponseResult.ok();
    }

    /**
     * 分页查询银行账户
     */
    @ApiOperation(value = "分页查询银行账户", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "工号", name = "code", dataType = "String"),
            @ApiImplicitParam(value = "部门id（多个以,隔开）", name = "deptId", dataType = "String"),
            @ApiImplicitParam(value = "名称（模糊查询）", name = "pname", dataType = "String"),
            @ApiImplicitParam(value = "账户类型 1：对内，2：对外", name = "accountType", dataType = "Integer"),
            @ApiImplicitParam(value = "账号", name = "bankAccount", dataType = "String"),
            @ApiImplicitParam(value = "工资账户 0：否 1：是", name = "wagesFlag", dataType = "Integer"),
            @ApiImplicitParam(value = "电子联行号", name = "branchCode", dataType = "String"),
            @ApiImplicitParam(value = "停用标志 0：启用 1：停用", name = "stopFlag", dataType = "Integer"),
            @ApiImplicitParam(value = "工号/名字/条件同时查询", name = "queryText", dataType = "String"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("page")
    public ResponseEntity<Page<BankAccountVO>> page(BankAccountVO bean,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "rows", required = false, defaultValue = "20") Integer rows) throws Exception {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("code", bean.getCode());
        String deptId = bean.getDeptId();
        if (StringUtils.isNotBlank(deptId) && deptId.indexOf("'") == -1) {
            String[] deptArr = deptId.split(",");
            deptId = "";
            for (String tmp : deptArr) {
                deptId += "'" + tmp + "'" + ",";
            }
            deptId = deptId.substring(0, deptId.length() - 1);
        }
        conditionMap.put("deptId", deptId);
        conditionMap.put("pname", bean.getPname());
        conditionMap.put("accountType", bean.getAccountType());
        conditionMap.put("bankAccount", bean.getBankAccount());
        conditionMap.put("wagesFlag", bean.getWagesFlag());
        conditionMap.put("branchCode", bean.getBranchCode());
        conditionMap.put("stopFlag", bean.getStopFlag());
        conditionMap.put("queryText", bean.getQueryText());
        Page<BankAccountVO> voList = this.service.getBankInfo(conditionMap, page, rows);
        return ResponseEntity.ok(voList);
    }

    @ApiOperation(value = "查看操作日志", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("queryLog")
    public ResponseEntity<List> queryLog(Long id) {
        List<DbInvokeRecord> list = invokeRecordService.list(Wrappers.<DbInvokeRecord>lambdaQuery().eq(DbInvokeRecord::getBankId, id).orderByDesc(DbInvokeRecord::getCreateTime));
        return ResponseEntity.ok(list);
    }

    /**
     * 导出银行账户
     */
    @ApiOperation(value = "导出银行账户", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "工号", name = "code", dataType = "String"),
            @ApiImplicitParam(value = "部门id（多个以,隔开）", name = "deptId", dataType = "String"),
            @ApiImplicitParam(value = "名称（模糊查询）", name = "pname", dataType = "String"),
            @ApiImplicitParam(value = "账户类型 1：对内，2：对外", name = "accountType", dataType = "Integer"),
            @ApiImplicitParam(value = "账号", name = "bankAccount", dataType = "String"),
            @ApiImplicitParam(value = "工资账户 0：否 1：是", name = "wagesFlag", dataType = "Integer"),
            @ApiImplicitParam(value = "电子联行号", name = "branchCode", dataType = "String"),
            @ApiImplicitParam(value = "停用标志 0：启用 1：停用", name = "stopFlag", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("export")
    public void export(BankAccountVO bean, HttpServletResponse response) throws Exception {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("code", bean.getCode());
        String deptId = bean.getDeptId();
        if (StringUtils.isNotBlank(deptId) && deptId.indexOf("'") == -1) {
            String[] deptArr = deptId.split(",");
            deptId = "";
            for (String tmp : deptArr) {
                deptId += "'" + tmp + "'" + ",";
            }
            deptId = deptId.substring(0, deptId.length() - 1);
        }
        conditionMap.put("deptId", deptId);
        conditionMap.put("pname", bean.getPname());
        conditionMap.put("accountType", bean.getAccountType());
        conditionMap.put("bankAccount", bean.getBankAccount());
        conditionMap.put("wagesFlag", bean.getWagesFlag());
        conditionMap.put("branchCode", bean.getBranchCode());
        conditionMap.put("stopFlag", bean.getStopFlag());
        List<BankAccountExcelData> details = this.service.getExcelInfo(conditionMap);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/bankAccountExportTemplate.xlsx");
        ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("银行账户表", response), BankAccountExcelData.class).withTemplate(is).build();
        WriteSheet sheet = EasyExcel.writerSheet(0).build();
        sheet.setSheetName("银行账户");
        workBook.fill(details, sheet);
        workBook.finish();
    }

    /**
     * 按照ID查询
     */
    @ApiOperation(value = "按照主键查询", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "主键", name = "id", dataType = "Serializable", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("getById")
    public ResponseResult getById(Serializable id) {
        return ResponseResult.ok(this.service.getById(id));
    }

    @ApiOperation(value = "银行账户导入模板下载", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        // 文件导出
        ResponseUtil.exportBankAccount(null, EasyExcelUtil.getOutputStream("导入银行账户模板", response), null);
    }

    @ApiOperation(value = "导入银行账户", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importSave")
    public ResponseEntity importSave(@RequestParam("file") MultipartFile srcFile) throws Exception {
        // 文件后缀名判断
        String fileExtension = EasyExcelUtil.getFileExtension(Objects.requireNonNull(srcFile.getOriginalFilename()));
        if (!"xls".equals(fileExtension) && !"xlsx".equals(fileExtension)) {
            return ResponseEntity.apply(StatusCodeEnmus.OTHER, "导入失败!只支持导入excel文件!");
        }
        List<ImportBankAccountExcelData> errorList = new ArrayList<>();
        int success = service.importAdd(srcFile.getInputStream(), errorList);
        if (!errorList.isEmpty()) {
            String key = BAIMPORT +"_" + UserThreadLocal.get().getUserName();
            String errorFileName = fileShareDir + File.separator + System.currentTimeMillis()+"_错误信息.xlsx";
            List<List<String>> dataList = new ArrayList<>();
            for (ImportBankAccountExcelData data : errorList) {
                List<String> colList = new ArrayList<>();
                colList.add(data.getCode());
                colList.add(data.getPname());
                colList.add(data.getAccountType());
                colList.add(data.getAccountName());
                colList.add(data.getBankAccount());
                colList.add(data.getBranchCode());
                colList.add(data.getWagesFlag());
                colList.add(data.getRemark());
                colList.add(data.getErrMsg());
                dataList.add(colList);
            }
            ResponseUtil.exportBankAccount(dataList, null, errorFileName);
            this.redis.set(key, errorFileName, expiretime);
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载!");
        } else {
            return ResponseEntity.ok(success);
        }
    }

    @ApiOperation(value = "导出错误明细")
    @GetMapping("/exportErrors")
    @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    public void exportErrors(HttpServletResponse response) throws Exception {
        InputStream is = null;
        try {
            if(redis.get(BAIMPORT+ "_" + UserThreadLocal.get().getUserName()) ==null) {
                throw new RuntimeException("没有银行账户导入的错误明细可供下载。");
            }
            String errorFileName = redis.get(BAIMPORT+ "_" + UserThreadLocal.get().getUserName());
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("银行账户导入错误明细", response)).withTemplate(is).build();
            workBook.finish();
            File file = new File(errorFileName);
            if(file.exists()) file.delete();
            redis.delete(BAIMPORT+ "_" + UserThreadLocal.get().getUserName());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally {
            if(is!=null) is.close();
        }
    }

    @ApiOperation(value = "停用导入模板下载", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/stopTemplate")
    public void stopTemplate(HttpServletResponse response) throws Exception {
        // 文件导出
        List<String> columnNames = new ArrayList<>();
        columnNames.add("银行账号");
        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(20 * 256);
        PoiExcelUtil.exportExcelFile("待停用银行账号", columnNames, columnWidths, null, null, EasyExcelUtil.getOutputStream("银行账号停用导入模板", response));

    }

    /**
     * 批量停用
     */
    @ApiOperation(value = "批量停用", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "银行账号（多个主键以“,”分割）", name = "accounts", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("batchStop")
    public ResponseEntity batchStop(String accounts) {
        this.service.batchStop(Arrays.asList(accounts.split(",")));
        return ResponseEntity.ok();
    }

    @ApiOperation(value = "导入停用", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importStop")
    public ResponseEntity importStop(@RequestParam("file") MultipartFile srcFile) throws Exception {
        List<BatchStopExcelData> accountList = EasyExcelUtil.getExcelContent(srcFile.getInputStream(), BatchStopExcelData.class);
        if (!CollectionUtils.isEmpty(accountList)) {
            List<String> list = accountList.stream().map(BatchStopExcelData::getBankAccount).collect(Collectors.toList());
            this.service.batchStop(list);
            return ResponseEntity.ok();
        } else {
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT, "导入失败!");

        }
    }
}
