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
import com.jtyjy.finance.manager.cache.DeptCache;
import com.jtyjy.finance.manager.cache.PersonCache;
import com.jtyjy.finance.manager.dto.common.UserBaseDTO;
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
 * Description: ???????????????????????????
 * Date: 2021/4/22 15:27
 */
@Api(tags = { "????????????" })
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
     * Description: ???????????????????????????????????????????????????????????????????????????
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
    @ApiOperation(value = "??????????????????",httpMethod="GET")
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
    @ApiOperation(value = "????????????????????????",httpMethod="GET")
    @GetMapping("/getExtractStatusList")
    public List<Map<String,Object>> getExtractStatusList(){   	
    	return Arrays.stream(ExtractStatusEnum.values()).map(e->{
    		Map<String,Object> map = new HashMap<>();
    		map.put("type", e.getType());
    		map.put("value", ExtractStatusEnum.getValue(e.getType()));
    		return map;
    	}).collect(Collectors.toList());
    }

	@ApiOperation(value = "??????????????????????????????",httpMethod="GET")
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
    @ApiOperation(value = "??????????????????????????????",httpMethod="GET")
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
    @ApiOperation(value = "????????????????????????",httpMethod="GET")
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
    @ApiOperation(value = "????????????????????????",httpMethod="GET")
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
    @ApiOperation(value = "????????????????????????????????????",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????????????????1???", name = "page", dataType = "Integer"),
            @ApiImplicitParam(value = "?????????????????????20???", name = "rows", dataType = "Integer"),
            @ApiImplicitParam(value = "?????????", name = "keyword", dataType = "String"),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
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
    @ApiOperation(value = "??????????????????",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "??????????????????????????????", name = "deptName", dataType = "String"),
            @ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/getDept")
    public ResponseEntity<List<WbDept>> getDept(String deptName){   
        List<WbDept> deptList = this.wdService.getDeptInfo(deptName);
        return ResponseEntity.ok(deptList);
    }
    
    /**
     * author minzhq
     */
    @ApiOperation(value = "??????????????????????????????",httpMethod="GET")
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
    @ApiOperation(value = "??????????????????????????????",httpMethod="GET")
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
    @ApiOperation(value = "????????????????????????????????????",httpMethod="GET")
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
    @ApiOperation(value = "??????????????????????????????",httpMethod="GET")
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
    @ApiOperation(value = "????????????",httpMethod="POST")
    @PostMapping(value = "/uploadFile")
    public ResponseEntity uploadFile(@ModelAttribute UploadQuery query) {
        if(query.getFiles()==null || query.getFiles().length ==0) {
            return ResponseEntity.error("???????????????");
        }
        commonService.uploadFile(query);
        return ResponseEntity.ok();
    }
    @ApiOperation(value = "????????????",httpMethod="POST")
    @PostMapping(value = "/upload")
    public ResponseEntity upload(@RequestParam(name="file") CommonsMultipartFile file) {
        return ResponseEntity.ok(commonService.upload(file));
    }

    @ApiOperation(value = "????????????",httpMethod="GET")
    @GetMapping(value = "/viewAttachment")
    public ResponseEntity viewAttachment(String contactId) {
        List<BudgetCommonAttachment> result = commonService.viewAttachment(contactId);
        return ResponseEntity.ok(result);
    }
    @ApiOperation(value = "????????????",httpMethod="POST")
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

    @ApiOperation(value = "????????????list",httpMethod="GET")
    @GetMapping(value = "getRoleList")
    public ResponseEntity getRoleList() {
        UserBaseDTO baseDTO = new UserBaseDTO();
        String result = HttpUtil.doGet(this.userRoleUrl + UserThreadLocal.getEmpNo()+"&serverId="+serverId);
        List<String> roles = JSON.parseArray(result, String.class);
        String mainRole = "";
        //???????????????????????????????????????????????????????????????????????????
        if (roles.contains(RoleNameEnum.COMMERCIAL_COMMISSION.getValue())) {
            mainRole = RoleNameEnum.COMMERCIAL_COMMISSION.getValue();
        }else if(roles.contains(RoleNameEnum.BIG_MANAGER.getValue())){
            mainRole = RoleNameEnum.BIG_MANAGER.getValue();
        }else if(roles.contains(RoleNameEnum.MANAGER.getValue())){
            mainRole = RoleNameEnum.MANAGER.getValue();
        }
        baseDTO.setRoleName(mainRole);
        WbPerson person = PersonCache.getPersonByEmpNo(UserThreadLocal.getEmpNo());
        baseDTO.setDeptId(person.getDeptId());
        WbDept dept = DeptCache.getByDeptId(person.getDeptId());
        baseDTO.setDeptName(dept.getDeptFullname());
        return ResponseEntity.ok(baseDTO);
    }
}
