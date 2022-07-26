package com.jtyjy.finance.manager.controller.base;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.BudgetSubject;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.easyexcel.BaseSubjectExcelData;
import com.jtyjy.finance.manager.easyexcel.BudgetSubjectExcelData;
import com.jtyjy.finance.manager.easyexcel.JinDieCodeExcelData;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetSubjectService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.SubjectInfoVO;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.result.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author shubo
 */
@Api(tags = { "预算科目管理接口" })
@RestController
@RequestMapping("/api/base/budgetSubject")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetSubjectController extends BaseController<BudgetAgentExecuteView> {	
	   
    @Autowired
    private RedisClient redis;
    
    public final static String JDIMPORT = "JDIMPORT"; 
    
    @Value("${file.shareDir}") 
    private String fileShareDir;
        
    @Value("${redis.file.key.expiretime}") 
    private Integer expiretime;
    
    private final BudgetSubjectService service;

	/**
     * 新增/修改（修改时需送id）
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "预算科目id（修改必送）", name = "id", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "基础科目id", name = "subjectid", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "科目名称", name = "name", dataType = "String", required = true),
            @ApiImplicitParam(value = "届别id", name = "yearid", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "金蝶科目代码", name = "jindiecode", dataType = "String"),
            @ApiImplicitParam(value = "金蝶科目名称", name = "jindiename", dataType = "String"),
            @ApiImplicitParam(value = "停用标识 0：启用 1：停用", name = "stopflag", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "排序号", name = "orderno", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "备注", name = "remark", dataType = "String"),
            @ApiImplicitParam(value = "辅助指标", name = "assistflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "向上汇总", name = "upsumflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "费用分解", name = "costsplitflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "费用拆借", name = "costlendflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "费用追加", name = "costaddflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "公式标识", name = "formulaflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "关联产品标识", name = "jointproductflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "产品分类id（多个用,隔开）", name = "procategoryid", dataType = "Boolean"),
            @ApiImplicitParam(value = "计算公式", name = "formula", dataType = "String"),
            @ApiImplicitParam(value = "计算顺序", name = "formulaorderno", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(@Valid SubjectInfoVO bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }
        BudgetSubject budgetSubject = new BudgetSubject();
        BeanUtils.copyProperties(bean, budgetSubject);
        if(null == bean.getId() || 0 == bean.getId().intValue()) {
            this.service.addsubject(budgetSubject);
            return ResponseEntity.ok();
        }else {
            this.service.updatesubject(budgetSubject);
            return ResponseEntity.ok();
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
        this.service.deletesubject(ids);
        return ResponseResult.ok();
    }

    @ApiOperation(value = "导出预算科目表",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "科目名称（模糊查询）", name = "subName", dataType = "String"),
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "int", required = true),
            @ApiImplicitParam(value = "停用标识 0：启用，1：停用", name = "stopFlag", dataType = "int")
    })
    @GetMapping("/export")
    public void export(@RequestParam(value = "yearId")Integer yearId, @RequestParam(value = "subName") String subName, @RequestParam(value = "stopFlag",required = false) Integer stopFlag, HttpServletResponse response) throws Exception {        
        List<SubjectInfoVO> list = this.service.subjectlist(yearId, subName, stopFlag);
        if (StringUtils.isBlank(subName)) {
            list = this.service.getTreeList(list);
        }
        List<BudgetSubjectExcelData> details = new ArrayList<>();
        for(SubjectInfoVO vo :list) {
            BudgetSubjectExcelData excelData = new BudgetSubjectExcelData();
            BeanUtils.copyProperties(vo, excelData);
            details.add(excelData);
        }
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/budgetSubjectExportTemplate.xlsx");
        ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("预算科目表", response), BudgetSubjectExcelData.class).withTemplate(is).build();
        WriteSheet sheet = EasyExcel.writerSheet(0).build();
        sheet.setSheetName("预算科目");
        workBook.fill(details, sheet);
        workBook.finish();
        
    }

    /**
     * 查询预算科目
     */
    @ApiOperation(value = "查询预算科目", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别id（送空默认为当前届别）", name = "yearId", dataType = "int"),
            @ApiImplicitParam(value = "科目名称（模糊查询）", name = "subName", dataType = "String"),
            @ApiImplicitParam(value = "停用标识 0：启用，1：停用", name = "stopFlag", dataType = "int"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("info")
    public ResponseEntity<List<SubjectInfoVO>> info(
            @RequestParam(value = "yearId", defaultValue = "") Integer yearId,
            @RequestParam(value = "subName", defaultValue = "") String subName,
            @RequestParam(value = "stopFlag", defaultValue = "") Integer stopFlag) throws Exception {

        List<SubjectInfoVO> voList = this.service.subjectlist(yearId, subName, stopFlag);
        return ResponseEntity.ok(voList);
    }
    
    /**
     * 预算科目移动
     */
    @ApiOperation(value = "预算科目移动", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "要移动的id", name = "id", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "新的父级id（最外层为0）", name = "pid", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("move")
    public ResponseResult move(@RequestParam(value = "id") Integer id, @RequestParam(value = "pid") Integer pid) {
        this.service.updateparentid(id, pid);
        return ResponseResult.ok();
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
    
    /**
     * 预算科目初始化
     */
    @ApiOperation(value = "预算科目初始化", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "数据来源届别id", name = "fromYearid", dataType = "int", required = true),
            @ApiImplicitParam(value = "要更新的届别id", name = "toYearid ", dataType = "int", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("init")
    public ResponseResult init(@RequestParam(value = "fromYearid") Long fromYearid, @RequestParam(value = "toYearid") Long toYearid) {
        return ResponseResult.ok(this.service.initSubject(fromYearid, toYearid));
    }    
    
    @ApiOperation(value = "导出金蝶科目代码", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Long"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportJinDie")
    public void exportJinDie(@RequestParam(value = "yearId", defaultValue = "") Long yearId, HttpServletResponse response) throws Exception {
        // 文件导出
        this.service.exportJindie(yearId, response); 
    }
    
    @ApiOperation(value = "导入金蝶科目代码", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importJinDie")
    public ResponseResult importJinDie(@RequestParam("yearId") Long yearId, @RequestParam("file") MultipartFile srcFile, HttpServletRequest request) throws Exception {
        // 文件后缀名判断
        String fileExtension = EasyExcelUtil.getFileExtension(Objects.requireNonNull(srcFile.getOriginalFilename()));
        if (!"xls".equals(fileExtension) && !"xlsx".equals(fileExtension)) {
            return ResponseResult.apply(StatusCodeEnmus.OTHER, "导入失败!只支持导入excel文件!");
        }
        List<JinDieCodeExcelData> errorList = new ArrayList<JinDieCodeExcelData>();
        int success = this.service.importUpdateJindie(srcFile.getInputStream(), yearId, errorList);
        if (!errorList.isEmpty()) {
            
            String key = JDIMPORT +"_" + UserThreadLocal.get().getUserName();
            String errorFileName = fileShareDir + File.separator + System.currentTimeMillis()+"_错误信息.xlsx";   
            List<List<String>> dataList = new ArrayList<>();
            for (JinDieCodeExcelData data : errorList) {
                List<String> colList = new ArrayList<>();
                colList.add(data.getCode());  
                colList.add(data.getName());
                colList.add(data.getJindiecode());
                colList.add(data.getErrMsg());
                dataList.add(colList);
            }
            ResponseUtil.exportSubjectJindie(dataList, null, errorFileName);
            this.redis.set(key, errorFileName, expiretime);
            return ResponseResult.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载!");
        } else {
            return ResponseResult.ok(success);
        }
    }
        
    @ApiOperation(value = "下载金蝶科目代码错误明细")
    @GetMapping("/downloadJinDieErrors")
    public void downloadJinDieErrors(HttpServletRequest request, HttpServletResponse response) throws Exception {
        InputStream is = null;
        try {
            if(redis.get(JDIMPORT+ "_" + UserThreadLocal.get().getUserName()) ==null) {
                throw new RuntimeException("没有金蝶科目代码导入的错误明细可供下载。");
            }
            String errorFileName = redis.get(JDIMPORT+ "_" + UserThreadLocal.get().getUserName());               
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("金蝶科目代码导入错误明细", response)).withTemplate(is).build();
            workBook.finish();  
            File file = new File(errorFileName);
            if(file.exists()) file.delete();
            redis.delete(JDIMPORT+ "_" + UserThreadLocal.get().getUserName());
       } catch (Exception e) {
           e.printStackTrace();
           throw e;
       }finally {
           if(is!=null) is.close();
       } 
    }
}
