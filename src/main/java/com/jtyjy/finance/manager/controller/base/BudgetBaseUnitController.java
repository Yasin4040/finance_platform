package com.jtyjy.finance.manager.controller.base;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.common.tools.PinyinTools;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.BudgetBaseUnit;
import com.jtyjy.finance.manager.bean.BudgetUnit;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.easyexcel.BaseUnitExcelData;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.BudgetBaseUnitService;
import com.jtyjy.finance.manager.service.BudgetUnitService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.core.redis.RedisClient;
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
import java.util.Objects;

/**
 * @author Admin
 */
@Api(tags = { "基础单位管理接口" })
@RestController
@RequestMapping("/api/base/baseUnit")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetBaseUnitController extends BaseController<BudgetAgentExecuteView> {

	
	private final BudgetBaseUnitService service;
	
	private final BudgetUnitService unitService;
	   
    @Autowired
    private RedisClient redis;
    
    public final static String BUIMPORT = "BUIMPORT"; 
    
    @Value("${file.shareDir}") 
    private String fileShareDir;
        
    @Value("${redis.file.key.expiretime}") 
    private Integer expiretime;
	/**
     * 新增/修改（修改时需送id）
     */
    @ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "基础单位id（修改必送）", name = "id", dataType = "Integer"),
            @ApiImplicitParam(value = "基础单位名称", name = "name", dataType = "String", required = true),
            @ApiImplicitParam(value = "停用标识 0：启用 1：停用", name = "stopflag", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "排序号", name = "orderno", dataType = "Integer", required = true, defaultValue = "0"),
            @ApiImplicitParam(value = "备注", name = "remark", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("addOrUpdate")
    public ResponseEntity<String> addOrUpdate(@Valid BudgetBaseUnit bean, BindingResult bindingResult) {
        String retError = this.getResult(bindingResult);
        if (StringUtils.isNotBlank(retError)) {
            return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
        }
        bean.setFirstspell(PinyinTools.getFirstspell(bean.getName()));
        bean.setFullspell(PinyinTools.getPinYin(bean.getName()));
        BudgetBaseUnit sameName = this.service.getOne(new QueryWrapper<BudgetBaseUnit>().eq("name", bean.getName()));
        
        if(null == bean.getId() || 0 == bean.getId().intValue()) {
            if (null != sameName) {
                return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_EXIST, bean.getName() + "已存在！");
            }
            bean.setCreatetime(new Date());
            this.service.save(bean);
            return ResponseEntity.ok();
        }else {
            if (null != sameName && !sameName.getId().equals(bean.getId())) {
                return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_EXIST, bean.getName() + "已存在！");
            }
            if (1 == bean.getStopflag().intValue()) {
                List<BudgetUnit> unitList = this.unitService.list(new QueryWrapper<BudgetUnit>().eq("baseunitid", bean.getId()));
                if (null != unitList && unitList.size() > 0) {
                    return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_USED, bean.getName() + "下存在预算单位，无法停用！");
                }
            }
            bean.setUpdatetime(new Date());
            this.service.updateById(bean);
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
        this.service.removeByIds(Arrays.asList(ids.split(",")));
        return ResponseResult.ok();
    }

    /**
     * 分页查询基础单位
     */
    @ApiOperation(value = "分页查询基础单位", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "单位名称", name = "name", dataType = "String"),
            @ApiImplicitParam(value = "停用标识 0：启用，1：停用", name = "stopflag", dataType = "Integer"),
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("page")
    public ResponseEntity<Page<BudgetBaseUnit>> baseUnitInfoPage(
            @RequestParam(value = "name") String name, 
            @RequestParam(value = "stopflag") Integer stopflag,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "rows", required = false, defaultValue = "20") Integer rows) throws Exception {
       
        
        Page<BudgetBaseUnit> voList = this.service.getBaseUnitPageList(name, stopflag, page, rows);
        return ResponseEntity.ok(voList);
    }
    
    @ApiOperation(value = "基础单位模板下载", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping(value = "/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        // 文件导出
        ResponseUtil.exportBaseUnit(null, EasyExcelUtil.getOutputStream("导入基础单位模板", response), null);
    }
    
    @ApiOperation(value = "导入基础单位", httpMethod = "POST")
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
        List<BaseUnitExcelData> errorList = new ArrayList<BaseUnitExcelData>();
        int success = this.service.importAdd(srcFile.getInputStream(), coverFlag, errorList);
        if (!errorList.isEmpty()) {
            String key = BUIMPORT +"_" + UserThreadLocal.get().getUserName();
            String errorFileName = fileShareDir + File.separator + System.currentTimeMillis()+"_错误信息.xlsx";   
            List<List<String>> dataList = new ArrayList<>();
            for (BaseUnitExcelData data : errorList) {
                List<String> colList = new ArrayList<>();
                colList.add(data.getUnitName());
                colList.add(data.getOrderNo());
                colList.add(data.getRemark());
                colList.add(data.getErrMsg());
                dataList.add(colList);
            }
            ResponseUtil.exportBaseUnit(dataList, null, errorFileName);
            this.redis.set(key, errorFileName, expiretime);
            return ResponseResult.apply(StatusCodeEnmus.ERROR_FORMAT, "文件导入有错误,请点击此处下载!");
        } else {
            request.getSession().setAttribute("baseUnitErrorDatas", new HashMap<>());
            return ResponseResult.ok(success);
        }
    }    

    @ApiOperation(value = "导出错误明细")
    @GetMapping("/exportErrors")
    public void exportErrors(HttpServletRequest request, HttpServletResponse response) throws Exception {
        InputStream is = null;
        try {
            if(redis.get(BUIMPORT+ "_" + UserThreadLocal.get().getUserName()) ==null) {
                throw new RuntimeException("没有基础单位导入的错误明细可供下载。");
            }
            String errorFileName = redis.get(BUIMPORT+ "_" + UserThreadLocal.get().getUserName());               
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("基础单位导入错误明细", response)).withTemplate(is).build();
            workBook.finish();  
            File file = new File(errorFileName);
            if(file.exists()) file.delete();
            redis.delete(BUIMPORT+ "_" + UserThreadLocal.get().getUserName());
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
