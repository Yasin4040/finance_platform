package com.jtyjy.finance.manager.controller.base;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.result.ResponseResult;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.BudgetAuthor;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.easyexcel.AuthorExcelData;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetAuthorService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.BudgetAuthorVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.*;

/**
 * @author Admin
 */
@Api(tags = {"稿费作者管理接口"})
@RestController
@RequestMapping("/api/base/author")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetAuthorController extends BaseController<BudgetAgentExecuteView> {

    private final BudgetAuthorService service;
    
    private final static String AUIMPORT = "AUIMPORT";
    
    @Autowired
    private RedisClient redis;
    
    @Value("${file.shareDir}") 
    private String fileShareDir;
    
    @Value("${redis.file.key.expiretime}") 
    private Integer expiretime;

    /**
     * 新增/修改（修改时需送id）
     *
     * @throws Exception
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "稿费作者id", name = "id", dataType = "Integer"),
            @ApiImplicitParam(value = "是否公司员工", name = "authortype", dataType = "Boolean", required = true),
            @ApiImplicitParam(value = "作者", name = "author", dataType = "String", required = true),
            @ApiImplicitParam(value = "身份证号", name = "idnumber", dataType = "String"),
            @ApiImplicitParam(value = "纳税人识别号", name = "taxpayernumber", dataType = "String"),
            @ApiImplicitParam(value = "电子联行号", name = "branchcode", dataType = "String", required = true),
            @ApiImplicitParam(value = "银行账号", name = "bankaccount", dataType = "String", required = true),
            @ApiImplicitParam(value = "所在单位", name = "company", dataType = "String", required = true),
            @ApiImplicitParam(value = "备注", name = "remark", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(@Valid BudgetAuthorVO vo, BindingResult bindingResult) throws Exception {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }
        BudgetAuthor bean = new BudgetAuthor();
        BeanUtils.copyProperties(vo, bean);
        StringBuffer errMsg = new StringBuffer();
        if (this.service.checkData(bean, errMsg, false)) {
            if (null == bean.getId() || 0 == bean.getId().intValue()) {
                this.service.addBudgetAuthor(bean);
                return ResponseEntity.ok();
            } else {
                this.service.updateById(bean);
                return ResponseEntity.ok();
            }
        } else {
            return ResponseEntity.apply(StatusCodeEnmus.OTHER, errMsg.toString());
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
        UpdateWrapper<BudgetAuthor> wrapper = new UpdateWrapper<BudgetAuthor>();
        wrapper.set("is_delete", "1");
        wrapper.in("id", Arrays.asList(ids.split(",")));
        this.service.update(wrapper);
        //this.service.removeByIds(Arrays.asList(ids.split(",")));
        return ResponseResult.ok();
    }

    /**
     * 分页查询稿费作者
     */
    @ApiOperation(value = "分页查询稿费作者", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "编号（模糊查询）", name = "code", dataType = "String"),
            @ApiImplicitParam(value = "是否公司员工", name = "authortype", dataType = "Boolean"),
            @ApiImplicitParam(value = "作者（模糊查询）", name = "author", dataType = "String"),
            @ApiImplicitParam(value = "身份证号（模糊查询）", name = "idnumber", dataType = "String"),
            @ApiImplicitParam(value = "纳税人识别号（模糊查询）", name = "taxpayernumber", dataType = "String"),
            @ApiImplicitParam(value = "银行类型（模糊查询）", name = "banktype", dataType = "String"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("page")
    public ResponseEntity<Page<BudgetAuthorVO>> page(
            @RequestParam(value = "code") String code,
            @RequestParam(value = "authortype") Boolean authortype,
            @RequestParam(value = "author") String author,
            @RequestParam(value = "idnumber") String idnumber,
            @RequestParam(value = "taxpayernumber") String taxpayernumber,
            @RequestParam(value = "banktype") String banktype,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "rows", required = false, defaultValue = "20") Integer rows) throws Exception {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("code", code);
        conditionMap.put("authortype", authortype);
        conditionMap.put("author", author);
        conditionMap.put("idnumber", idnumber);
        conditionMap.put("taxpayernumber", taxpayernumber);
        conditionMap.put("banktype", banktype);
        Page<BudgetAuthorVO> voList = this.service.queryAuthorPage(conditionMap, page, rows);
        return ResponseEntity.ok(voList);
    }

    @ApiOperation(value = "稿费作者模板下载", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        // 文件导出
        ResponseUtil.exportAuthor(null, EasyExcelUtil.getOutputStream("导入稿费作者模板", response), null);
    }

    @ApiOperation(value = "导入稿费作者", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "是否允许覆盖 0：否 1：是", name = "coverFlag", dataType = "String", required = true),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importSave")
    public ResponseResult importSave(@RequestParam("coverFlag") String coverFlag, @RequestParam("file") MultipartFile srcFile, HttpServletRequest request) throws Exception {
        // 文件后缀名判断
        String fileExtension = EasyExcelUtil.getFileExtension(Objects.requireNonNull(srcFile.getOriginalFilename()));
        if (!"xls".equals(fileExtension) && !"xlsx".equals(fileExtension)) {
            return ResponseResult.apply(StatusCodeEnmus.OTHER, "导入失败!只支持导入excel文件!");
        }
        List<AuthorExcelData> errorList = new ArrayList<AuthorExcelData>();
        int success = this.service.importAdd(srcFile, coverFlag, errorList);
        if (!errorList.isEmpty()) {
            String key = AUIMPORT +"_" + UserThreadLocal.get().getUserName();
            String errorFileName = fileShareDir + File.separator + System.currentTimeMillis()+"_错误信息.xlsx";   
            List<List<String>> dataList = new ArrayList<>();
            for (AuthorExcelData data : errorList) {
                List<String> colList = new ArrayList<>();
                colList.add(data.getAuthor());
                colList.add(data.getIdnumber());
                colList.add(data.getTaxpayernumber());
                colList.add(data.getAuthortype());
                colList.add(data.getCompany());
                colList.add(data.getBankaccount());
                colList.add(data.getBranchcode());
                colList.add(data.getBankName());
                colList.add(data.getProvince());
                colList.add(data.getCity());
                colList.add(data.getChildBankName());
                colList.add(data.getRemark());
                colList.add(data.getErrMsg());
                dataList.add(colList);
            }
            ResponseUtil.exportAuthor(dataList, null, errorFileName);
            this.redis.set(key, errorFileName, expiretime);
            return ResponseResult.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载!");
        } else {
            request.getSession().setAttribute("authorErrorDatas", new HashMap<>());
            return ResponseResult.ok(success);
        }
    }

    @ApiOperation(value = "导出错误明细")
    @GetMapping("/exportErrors")
    public void exportErrors(HttpServletRequest request, HttpServletResponse response) throws Exception {
        InputStream is = null;
        try {
            if(redis.get(AUIMPORT+ "_" + UserThreadLocal.get().getUserName()) ==null) {
                throw new RuntimeException("没有稿费作者导入的错误明细可供下载。");
            }
            String errorFileName = redis.get(AUIMPORT+ "_" + UserThreadLocal.get().getUserName());               
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("稿费作者导入错误明细", response)).withTemplate(is).build();
            workBook.finish();  
            File file = new File(errorFileName);
            if(file.exists()) file.delete();
            redis.delete(AUIMPORT+ "_" + UserThreadLocal.get().getUserName());
       } catch (Exception e) {
           e.printStackTrace();
           throw e;
       }finally {
           if(is!=null) is.close();
       } 
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
}
