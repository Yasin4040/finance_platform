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
@Api(tags = { "????????????????????????" })
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
     * ??????/????????????????????????id???
     */
    @ApiOperation(value = "??????/????????????????????????id???", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "????????????id??????????????????", name = "id", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "????????????id", name = "subjectid", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "????????????", name = "name", dataType = "String", required = true),
            @ApiImplicitParam(value = "??????id", name = "yearid", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "??????????????????", name = "jindiecode", dataType = "String"),
            @ApiImplicitParam(value = "??????????????????", name = "jindiename", dataType = "String"),
            @ApiImplicitParam(value = "???????????? 0????????? 1?????????", name = "stopflag", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "?????????", name = "orderno", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "??????", name = "remark", dataType = "String"),
            @ApiImplicitParam(value = "????????????", name = "assistflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "????????????", name = "upsumflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "????????????", name = "costsplitflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "????????????", name = "costlendflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "????????????", name = "costaddflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "????????????", name = "formulaflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "??????????????????", name = "jointproductflag", dataType = "Boolean"),
            @ApiImplicitParam(value = "????????????id????????????,?????????", name = "procategoryid", dataType = "Boolean"),
            @ApiImplicitParam(value = "????????????", name = "formula", dataType = "String"),
            @ApiImplicitParam(value = "????????????", name = "formulaorderno", dataType = "Integer"),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
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
     * ????????????????????????
     */
    @ApiOperation(value = "????????????????????????", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "???????????????????????????,????????????", name = "ids", dataType = "String", required = true),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @PostMapping("deleteByIds")
    public ResponseResult deleteByIds(String ids) {
        this.service.deletesubject(ids);
        return ResponseResult.ok();
    }

    @ApiOperation(value = "?????????????????????",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "??????????????????????????????", name = "subName", dataType = "String"),
            @ApiImplicitParam(value = "??????id", name = "yearId", dataType = "int", required = true),
            @ApiImplicitParam(value = "???????????? 0????????????1?????????", name = "stopFlag", dataType = "int")
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
        ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("???????????????", response), BudgetSubjectExcelData.class).withTemplate(is).build();
        WriteSheet sheet = EasyExcel.writerSheet(0).build();
        sheet.setSheetName("????????????");
        workBook.fill(details, sheet);
        workBook.finish();
        
    }

    /**
     * ??????????????????
     */
    @ApiOperation(value = "??????????????????", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????id?????????????????????????????????", name = "yearId", dataType = "int"),
            @ApiImplicitParam(value = "??????????????????????????????", name = "subName", dataType = "String"),
            @ApiImplicitParam(value = "???????????? 0????????????1?????????", name = "stopFlag", dataType = "int"),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
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
     * ??????????????????
     */
    @ApiOperation(value = "??????????????????", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "????????????id", name = "id", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "????????????id???????????????0???", name = "pid", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @PostMapping("move")
    public ResponseResult move(@RequestParam(value = "id") Integer id, @RequestParam(value = "pid") Integer pid) {
        this.service.updateparentid(id, pid);
        return ResponseResult.ok();
    } 
    
    /**
     * ??????ID??????
     */
    @ApiOperation(value = "??????????????????", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????", name = "id", dataType = "Serializable", required = true),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @GetMapping("getById")
    public ResponseResult getById(Serializable id) {
        return ResponseResult.ok(this.service.getById(id));
    }    
    
    /**
     * ?????????????????????
     */
    @ApiOperation(value = "?????????????????????", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????????????????id", name = "fromYearid", dataType = "int", required = true),
            @ApiImplicitParam(value = "??????????????????id", name = "toYearid ", dataType = "int", required = true),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @PostMapping("init")
    public ResponseResult init(@RequestParam(value = "fromYearid") Long fromYearid, @RequestParam(value = "toYearid") Long toYearid) {
        return ResponseResult.ok(this.service.initSubject(fromYearid, toYearid));
    }    
    
    @ApiOperation(value = "????????????????????????", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????id", name = "yearId", dataType = "Long"),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/exportJinDie")
    public void exportJinDie(@RequestParam(value = "yearId", defaultValue = "") Long yearId, HttpServletResponse response) throws Exception {
        // ????????????
        this.service.exportJindie(yearId, response); 
    }
    
    @ApiOperation(value = "????????????????????????", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????id", name = "yearId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importJinDie")
    public ResponseResult importJinDie(@RequestParam("yearId") Long yearId, @RequestParam("file") MultipartFile srcFile, HttpServletRequest request) throws Exception {
        // ?????????????????????
        String fileExtension = EasyExcelUtil.getFileExtension(Objects.requireNonNull(srcFile.getOriginalFilename()));
        if (!"xls".equals(fileExtension) && !"xlsx".equals(fileExtension)) {
            return ResponseResult.apply(StatusCodeEnmus.OTHER, "????????????!???????????????excel??????!");
        }
        List<JinDieCodeExcelData> errorList = new ArrayList<JinDieCodeExcelData>();
        int success = this.service.importUpdateJindie(srcFile.getInputStream(), yearId, errorList);
        if (!errorList.isEmpty()) {
            
            String key = JDIMPORT +"_" + UserThreadLocal.get().getUserName();
            String errorFileName = fileShareDir + File.separator + System.currentTimeMillis()+"_????????????.xlsx";   
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
            return ResponseResult.apply(StatusCodeEnmus.ERROR_FORMAT, "?????????????????????,?????????????????????!");
        } else {
            return ResponseResult.ok(success);
        }
    }
        
    @ApiOperation(value = "????????????????????????????????????")
    @GetMapping("/downloadJinDieErrors")
    public void downloadJinDieErrors(HttpServletRequest request, HttpServletResponse response) throws Exception {
        InputStream is = null;
        try {
            if(redis.get(JDIMPORT+ "_" + UserThreadLocal.get().getUserName()) ==null) {
                throw new RuntimeException("????????????????????????????????????????????????????????????");
            }
            String errorFileName = redis.get(JDIMPORT+ "_" + UserThreadLocal.get().getUserName());               
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("????????????????????????????????????", response)).withTemplate(is).build();
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
