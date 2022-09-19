package com.jtyjy.finance.manager.controller.common;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.jtyjy.api.OAServiceProxy;
import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.enmus.*;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.query.UploadQuery;
import com.jtyjy.finance.manager.utils.HttpUtil;
import io.swagger.annotations.*;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.mapper.WbBanksMapper;
import com.jtyjy.finance.manager.service.BudgetAuthorfeesumService;
import com.jtyjy.finance.manager.service.CommonService;
import com.jtyjy.finance.manager.service.WbDeptService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;

/*
 * Author: ldw
 * Description: 财务管理公共控制器
 * Date: 2021/4/22 15:27
 */
@Api(tags = { "通用接口" })
@RestController
@RequestMapping("/api/common")
public class CommonController extends BaseController {

    @Autowired
    private CommonService commonService;
    
    @Autowired
    private WbDeptService wdService;
    
    @Autowired
    private BudgetYearPeriodMapper yearMapper;
    
    @Autowired
    private WbBanksMapper bankMapper;
    @Value("${hit.flush.uesr.role.url}")
    private String userRoleUrl;
    @Value("${app.id}")
    private String serverId;
    /*
     * Author: ldw
     * Description: 获取系统内所有的银行类型，如：中国银行，招商银行等
     * Date: 2021/4/22 15:32
     */
//    @GetMapping("/getDistinctBankTypes")
//    @NoLoginAnno
//    public List getDistinctBankTypes(){
//        List<String> list = commonService.getDistinctBankTypes();
//        return list;
//    }

	@GetMapping("test")
	@NoLoginAnno
	public String test(){
		return "test";
	}


    /**
     * author minzhq
     */
    @ApiOperation(value = "获取届别列表",httpMethod="GET")
    @GetMapping("/getYearList")
    public List<BudgetYearPeriod> getYearList(){
    	return yearMapper.selectList(new QueryWrapper<BudgetYearPeriod>().orderByDesc("code"));
    }

    @ApiOperation(value = "getOAUserinfo",httpMethod="GET")
    @NoLoginAnno
    @GetMapping("/getOAUserinfo")
    public String getOAUserinfo(String empNo){
        OAServiceProxy oaServiceProxy = new OAServiceProxy("http://api.jtyjy.com/services/OAService?wsdl");
        String result = null;
        try {
            result = oaServiceProxy.getOAUserinfo(empNo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        return result;
    }

    /**
     * author minzhq
     */
    @ApiOperation(value = "获取提成状态列表",httpMethod="GET")
    @GetMapping("/getExtractStatusList")
    public List<Map<String,Object>> getExtractStatusList(){   	
    	return Arrays.stream(ExtractStatusEnum.values()).map(e->{
    		Map<String,Object> map = new HashMap<>();
    		map.put("type", e.getType());
    		map.put("value", ExtractStatusEnum.getValue(e.getType()));
    		return map;
    	}).collect(Collectors.toList());
    }

	@ApiOperation(value = "获取提成支付模板列表",httpMethod="GET")
	@GetMapping("/getExtractPayTemplateList")
	public List<Map<String,Object>> getExtractPayTemplateList(){
		return Arrays.stream(ExtractPayTemplateEnum.values()).map(e->{
			Map<String,Object> map = new HashMap<>();
			map.put("type", e.getType());
			map.put("value", ExtractPayTemplateEnum.getValue(e.getType()));
			return map;
		}).collect(Collectors.toList());
	}
    
    /**
     * author minzhq
     */
    @ApiOperation(value = "获取提成超额状态列表",httpMethod="GET")
    @GetMapping("/getExtractExcessStatusList")
    public List<Map<String,Object>> getExtractExcessStatusList(){   	
    	return Arrays.stream(ExtractExcessTypeEnum.values()).map(e->{
    		Map<String,Object> map = new HashMap<>();
    		map.put("type", e.getType());
    		map.put("value", ExtractExcessTypeEnum.getValue(e.getType()));
    		return map;
    	}).collect(Collectors.toList());
    }
    /**
     * author minzhq
     */
    @ApiOperation(value = "获取稿费状态列表",httpMethod="GET")
    @GetMapping("/getAuthorFeeStatusList")
    public List<Map<String,Object>> getAuthorFeeStatusList(){   	
    	return Arrays.stream(AuthorFeeStatusEnum.values()).map(e->{
    		Map<String,Object> map = new HashMap<>();
    		map.put("type", e.getType());
    		map.put("value", AuthorFeeStatusEnum.getValue(e.getType()));
    		return map;
    	}).collect(Collectors.toList());
    }
    
    /**
     * author minzhq
     */
    @ApiOperation(value = "获取稿费作者类型",httpMethod="GET")
    @GetMapping("/getAuthorType")
    public List<Map<String,Object>> getAuthorType(){   	
    	List<Map<String,Object>> resultList = new ArrayList<>();
    	Map<String,Object> map = new HashMap<>();
    	map.put("text", BudgetAuthorfeesumService.AUTHOR_TYPE_INNER);
    	map.put("value", 1);
    	Map<String,Object> map1 = new HashMap<>();
    	map1.put("text", BudgetAuthorfeesumService.AUTHOR_TYPE_OUTER);
    	map1.put("value", 0);
    	resultList.add(map1);
    	resultList.add(map);
    	return resultList;
    }
    /**
     * author minzhq
     */
    @ApiOperation(value = "获取银行类型（稿费专属）",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "关键字", name = "keyword", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/getBankType")
    public ResponseEntity<PageResult<String>> getBankType(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer rows,
    						@RequestParam(name="keyword") String keyword){   
    	Page<WbBanks> pageCond = new Page<>(page,rows);
    	pageCond = bankMapper.selectPage(pageCond, new QueryWrapper<WbBanks>().like(StringUtils.isNotBlank(keyword),"bank_name", keyword).select("DISTINCT bank_name"));
    	return ResponseEntity.ok(PageResult.apply(pageCond.getTotal(), pageCond.getRecords().stream().map(e->e.getBankName()).distinct().collect(Collectors.toList())));
    }
    
    /**
     * author shubo
     */
    @ApiOperation(value = "获取部门信息",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "部门名称（模糊查询）", name = "deptName", dataType = "String"),
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/getDept")
    public ResponseEntity<List<WbDept>> getDept(String deptName){   
        List<WbDept> deptList = this.wdService.getDeptInfo(deptName);
        return ResponseEntity.ok(deptList);
    }
    
    /**
     * author minzhq
     */
    @ApiOperation(value = "获取消息模板类别列表",httpMethod="GET")
    @GetMapping("/getMsgTemplateCategoryList")
    public List<Map<String,Object>> getMsgTemplateCategoryList(){   	
    	return Arrays.stream(TemplateCategoryEnum.values()).map(e->{
    		Map<String,Object> map = new HashMap<>();
    		map.put("type", e.getType());
    		map.put("value", TemplateCategoryEnum.getValue(e.getType()));
    		return map;
    	}).collect(Collectors.toList());
    }
    
    /**
     * author minzhq
     */
    @ApiOperation(value = "获取消息模板类型列表",httpMethod="GET")
    @GetMapping("/getMsgTemplateTypeList")
    public List<Map<String,Object>> getMsgTemplateTypeList(){   	
    	return Arrays.stream(TemplateTypeEnum.values()).map(e->{
    		Map<String,Object> map = new HashMap<>();
    		map.put("type", e.getType());
    		map.put("value", TemplateTypeEnum.getValue(e.getType()));
    		return map;
    	}).collect(Collectors.toList());
    }
    /**
     * author minzhq
     */
    @ApiOperation(value = "获取消息模板参数类型列表",httpMethod="GET")
    @GetMapping("/getMsgTemplateParameterTypeList")
    public List<Map<String,Object>> getMsgTemplateParameterTypeList(){   	
    	return Arrays.stream(TemplateParameterFieldTypeEnum.values()).map(e->{
    		Map<String,Object> map = new HashMap<>();
    		map.put("type", e.getType());
    		map.put("value", TemplateParameterFieldTypeEnum.getValue(e.getType()));
    		return map;
    	}).collect(Collectors.toList());
    }
    
    /**
     * author minzhq
     */
    @ApiOperation(value = "获取模板消息类型列表",httpMethod="GET")
    @GetMapping("/getTemplateTypeList")
    public List<Map<String,Object>> getTemplateTypeList(){   	
    	return Arrays.stream(MsgTypeEnum.values()).map(e->{
    		Map<String,Object> map = new HashMap<>();
    		map.put("type", e.getCode());
    		map.put("value", MsgTypeEnum.getValue(e.getCode()));
    		return map;
    	}).collect(Collectors.toList());
    }

    //    @NoLoginAnno
    @ApiOperation(value = "上传文件",httpMethod="POST")
    @PostMapping(value = "/uploadFile")
    public ResponseEntity uploadFile(@ModelAttribute UploadQuery query) {
        if(query.getFiles()==null || query.getFiles().length ==0) {
            return ResponseEntity.error("文件不存在");
        }
        commonService.uploadFile(query);
        return ResponseEntity.ok();
    }
    @ApiOperation(value = "上传文件",httpMethod="POST")
    @PostMapping(value = "/upload")
    public ResponseEntity upload(@RequestParam(name="file") CommonsMultipartFile file) {
        return ResponseEntity.ok(commonService.upload(file));
    }

    @ApiOperation(value = "查看附件",httpMethod="GET")
    @GetMapping(value = "/viewAttachment")
    public ResponseEntity viewAttachment(String contactId) {
        List<BudgetCommonAttachment> result = commonService.viewAttachment(contactId);
        return ResponseEntity.ok(result);
    }
    @ApiOperation(value = "删除附件",httpMethod="POST")
    @PostMapping(value = "/delAttachment")
    public ResponseEntity delAttachment(String id) {
        try {
            commonService.delAttachment(id);
            return ResponseEntity.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.error(e.getMessage());
        }
    }

    @ApiOperation(value = "获取角色list",httpMethod="GET")
    @GetMapping(value = "/getRoleList")
    public ResponseEntity getRoleList() {
        String result = HttpUtil.doGet(this.userRoleUrl + UserThreadLocal.getEmpNo()+"&serverId="+serverId);
        List<String> roles = JSON.parseArray(result, String.class);
        return ResponseEntity.ok(roles);
    }
}
