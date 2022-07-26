package com.jtyjy.finance.manager.controller.msg;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.tools.FileTools;
import com.jtyjy.easy.excel.ImportHelper;
import com.jtyjy.finance.manager.bean.BudgetMsgtemplete;
import com.jtyjy.finance.manager.bean.BudgetMsgtempleteDetailNew;
import com.jtyjy.finance.manager.bean.BudgetMsgtempleteParameter;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.controller.authorfee.excel.ContributionFeeExcelHead;
import com.jtyjy.finance.manager.controller.msg.excel.MsgTemplateImportExcelPostProcessor;
import com.jtyjy.finance.manager.enmus.MsgTypeEnum;
import com.jtyjy.finance.manager.enmus.TemplateCategoryEnum;
import com.jtyjy.finance.manager.enmus.TemplateParameterFieldTypeEnum;
import com.jtyjy.finance.manager.enmus.TemplateTypeEnum;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.mapper.BudgetMsgtempleteParameterMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.service.BudgetMsgService;
import com.jtyjy.finance.manager.service.BudgetMsgtempleteDetailNewService;
import com.jtyjy.finance.manager.service.BudgetMsgtempleteParameterService;
import com.jtyjy.finance.manager.service.BudgetYearPeriodService;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.HtmlUtil;
import com.jtyjy.finance.manager.utils.PoiExcelUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 消息通知模块
 * @author minzhq
 * date 2021-06-21
 */
@Api(tags = { "消息通知" })
@RestController
@RequestMapping("/api/msg")
@CrossOrigin
@SuppressWarnings("all")
public class BudgetMsgController extends BaseController<BudgetMsgtemplete>{	
	
	private final static Logger LOGGER = LoggerFactory.getLogger(BudgetMsgController.class);
	
	@Autowired
	private BudgetMsgService msgService;
	
	@Autowired
	private BudgetYearPeriodService yearPeriodService;
	
	@Autowired
	private BudgetMsgtempleteParameterService parameterService;
	
	@Autowired
	private BudgetMsgtempleteDetailNewService detailService;
	
	private final static String IMPORT_TYPE = "msg";
	
	@Autowired
    private RedisClient redis;
    
    @Value("${file.shareDir}") 
    private String fileShareDir;

	@Value("${service.domain}")
	private String serverUrl;
    
    @Value("${redis.file.key.expiretime}") 
    private Integer expiretime;
    
    @GetMapping("/redirectPublicMsgPage")
    @NoLoginAnno
    public void redirectPublicMsgPage(@RequestParam(required = true) Long id,HttpServletResponse response) throws Exception {
	    Map<String,Object> result = detailService.getPublicMsg(id);
	    Boolean isObjection = (Boolean) result.get("isObjection");
	    String msg = (String) result.get("msg");
	    BudgetMsgtempleteDetailNew detailNew = detailService.getById(id);
	    detailNew.setIspreview(true);
	    detailService.updateById(detailNew);
	    HtmlUtil.draw(HtmlUtil.msgHtml("公示消息", serverUrl, id.toString(), msg, isObjection), response);
    }
    
    
	@ApiOperation(value = "获取消息模板首页",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getMsgTemplateList")
	public ResponseEntity<PageResult<BudgetMsgtemplete>> getMsgTemplateList(@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "20")Integer rows){
		try {
			PageResult<BudgetMsgtemplete> pageList = msgService.getMsgTemplateList(page,rows);
			return ResponseEntity.ok(pageList);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "新增消息模板",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/addMsgTemplate")
	public ResponseEntity addMsgTemplate(@RequestBody @Valid BudgetMsgtemplete entity, BindingResult bindingResult){
		try {
			msgService.save(entity);
			return ResponseEntity.ok("添加成功");
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "修改消息模板",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/updateMsgTemplate")
	public ResponseEntity updateMsgTemplate(@RequestBody @Valid BudgetMsgtemplete entity, BindingResult bindingResult){
		try {
			msgService.updateById(entity);
			return ResponseEntity.ok("修改成功");
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "详情",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "id", name = "id", dataType = "String", required = true)
	})
	@GetMapping("/getMsgTemplateDetail")
	public ResponseEntity<BudgetMsgtemplete> getMsgTemplateDetail(@RequestParam(name="id",required = true)Long id){
		try {
			BudgetMsgtemplete detail = msgService.getById(id);
			detail.setYearName(yearPeriodService.getById(detail.getYearid()).getPeriod());
			detail.setTempletecategoryName(TemplateCategoryEnum.getValue(detail.getTempletecategory()));
			detail.setTempletetypeName(TemplateTypeEnum.getValue(detail.getTempletetype()));
			return ResponseEntity.ok(detail);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
	
	@ApiOperation(value = "删除消息模板",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "id", name = "id", dataType = "String", required = true)
	})
	@GetMapping("/deleteMsgTemplate")
	public ResponseEntity deleteMsgTemplate(@RequestParam(name="id",required = true)Long id){
		try {
			msgService.removeById(id);
			return ResponseEntity.ok("删除成功");
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
	
	@ApiOperation(value = "下载预警模板",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "模板id", name = "templateId", dataType = "Long", required = true)
	})
	@GetMapping("/downWarningTemplate")
	public void downWarningTemplate(HttpServletResponse response,@RequestParam(name="templateId",required = true) Long templateId) throws Exception{
		BudgetMsgtemplete msgtemplete = msgService.getById(templateId);
		if(msgtemplete.getTempletecategory() != TemplateCategoryEnum.BACK_MONEY.getType()) throw new RuntimeException("此功能只支持下载回款消息模板");
		//动态获取表头
		List<List<String>> oneSheetHeadList =  getsheetHead(templateId,MsgTypeEnum.WARNING.getCode());
		ExcelWriter excelWriter = EasyExcel.write(EasyExcelUtil.getOutputStream("消息预警模板", response)).build();
		WriteTable writeTable = EasyExcel.writerTable(0).head(oneSheetHeadList).needHead(true).build();
		WriteSheet sumSheet = EasyExcel.writerSheet("消息预警模板").build();
		excelWriter.write(Lists.newArrayList(), sumSheet, writeTable);
		excelWriter.finish();
	}
	
	@ApiOperation(value = "下载公示模板",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "模板id", name = "templateId", dataType = "Long", required = true)
	})
	@GetMapping("/downPublicTemplate")
	public void downPublicTemplate(HttpServletResponse response,@RequestParam(name="templateId",required = true) Long templateId) throws Exception{
		BudgetMsgtemplete msgtemplete = msgService.getById(templateId);
		if(msgtemplete.getTempletecategory() != TemplateCategoryEnum.BACK_MONEY.getType()) throw new RuntimeException("此功能只支持下载回款消息模板");
		//动态获取表头
		List<List<String>> oneSheetHeadList =  getsheetHead(templateId,MsgTypeEnum.PUBLIC.getCode());
		ExcelWriter excelWriter = EasyExcel.write(EasyExcelUtil.getOutputStream("消息公示模板", response)).build();
		WriteTable writeTable = EasyExcel.writerTable(0).head(oneSheetHeadList).needHead(true).build();
		WriteSheet sumSheet = EasyExcel.writerSheet("消息公示模板").build();
		excelWriter.write(Lists.newArrayList(), sumSheet, writeTable);
		excelWriter.finish();
	}

	@ApiOperation(value = "下载结果模板",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "模板id", name = "templateId", dataType = "Long", required = true)
	})
	@GetMapping("/downResultTemplate")
	public void downResultTemplate(HttpServletResponse response,@RequestParam(name="templateId",required = true) Long templateId) throws Exception{
		BudgetMsgtemplete msgtemplete = msgService.getById(templateId);
		if(msgtemplete.getTempletecategory() != TemplateCategoryEnum.BACK_MONEY.getType()) throw new RuntimeException("此功能只支持下载回款消息模板");
		//动态获取表头
		List<List<String>> oneSheetHeadList =  getsheetHead(templateId,MsgTypeEnum.RESULT.getCode());
		ExcelWriter excelWriter = EasyExcel.write(EasyExcelUtil.getOutputStream("消息结果模板", response)).build();
		WriteTable writeTable = EasyExcel.writerTable(0).head(oneSheetHeadList).needHead(true).build();
		WriteSheet sumSheet = EasyExcel.writerSheet("消息结果模板").build();
		excelWriter.write(Lists.newArrayList(), sumSheet, writeTable);
		excelWriter.finish();
	}

	private List<List<String>> getsheetHead(Long templateId,Integer msgType) {
		List<List<String>> headList = new ArrayList<>();
		
		List<String> head0 = new ArrayList<>();		
		head0.add("消息类型(预警、公示、结果)");
		headList.add(head0);
		
		List<String> head1 = new ArrayList<>();
		head1.add("工号");
		headList.add(head1);
		
		List<BudgetMsgtempleteParameter> parameters = getParameters(templateId, msgType);
		parameters.forEach(p->{
			List<String> head = new ArrayList<>();
			head.add(p.getChinesename());
			headList.add(head);
		});
		return headList;
	}
	
	private List<BudgetMsgtempleteParameter> getParameters(Long templateId,Integer msgType){
		return parameterService.list(new QueryWrapper<BudgetMsgtempleteParameter>().eq("templeteid", templateId).eq("msgtype", msgType).orderByAsc("orderno"));
	}
	
	@ApiOperation(value = "回款消息",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "模板id", name = "templateId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "工号", name = "empno", dataType = "String", required = false),
			@ApiImplicitParam(value = "消息类型", name = "msgType", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "是否发送(0否1是) ", name = "isSend", dataType = "Integer", required = false)
	})
	@GetMapping("/getMsgTemplateDetailForBackMoneyList")
	public ResponseEntity<PageResult<BudgetMsgtempleteDetailNew>> getMsgTemplateDetailForBackMoneyList(@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "20")Integer rows,
															@RequestParam(name="templateId",required = true)Long templateId,												
															@RequestParam(name="empno",required = false)String empno,
															@RequestParam(name="msgType",required = false)Integer msgType,
															@RequestParam(name="isSend",required = false)Integer isSend){
		try {
			Map<String,Object> params = new HashMap<>();
			params.put("templateId", templateId);
			if(StringUtils.isNotBlank(empno))params.put("empno", empno);
			if(msgType!=null)params.put("msgType", msgType);
			if(isSend!=null)params.put("isSend", isSend);
			PageResult<BudgetMsgtempleteDetailNew> pageList = msgService.getMsgTemplateDetailForBackMoneyList(page,rows,params);
			return ResponseEntity.ok(pageList);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "模板参数",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "模板id", name = "templateId", dataType = "Long", required = false),
	})
	@GetMapping("/getMsgTemplateParameterList")
	public ResponseEntity<PageResult<BudgetMsgtempleteParameter>> getMsgTemplateParameterList(@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "20")Integer rows,
								@RequestParam(name="templateId",required = true)Long templateId){
		try {
			Page<BudgetMsgtempleteParameter> pageCond = new Page<BudgetMsgtempleteParameter>(page, rows);
			pageCond = parameterService.page(pageCond, new QueryWrapper<BudgetMsgtempleteParameter>().eq("templeteid", templateId));
			List<BudgetMsgtempleteParameter> records = pageCond.getRecords();
			records.forEach(e->{
				e.setMsgtypeName(MsgTypeEnum.getValue(e.getMsgtype()));
				e.setTypeName(TemplateParameterFieldTypeEnum.getValue(e.getType()));
			});
			return ResponseEntity.ok(PageResult.apply(pageCond.getTotal(), records));
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "新增模板参数",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/addTemplateParameter")
	public ResponseEntity addTemplateParameter(@RequestBody  BudgetMsgtempleteParameter entity){
		try {
			String retError = this.validate(entity);
	        if (StringUtils.isNotBlank(retError)) {
	            return ResponseEntity.error(retError);
	        }
			parameterService.save(entity);
			return ResponseEntity.ok();
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "修改模板参数",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/updateTemplateParameter")
	public ResponseEntity updateTemplateParameter(@RequestBody BudgetMsgtempleteParameter entity){
		try {
			if(Objects.isNull(entity.getId())) throw new RuntimeException("缺少id必填字段");
			String retError = this.validate(entity);
	        if (StringUtils.isNotBlank(retError)) {
	            return ResponseEntity.error(retError);
	        }
			parameterService.updateById(entity);
			return ResponseEntity.ok();
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "删除模板参数",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "模板参数id", name = "id", dataType = "Long", required = true)
	})
	@PostMapping("/deleteTemplateParameter")
	public ResponseEntity deleteTemplateParameter(@RequestParam(name="id",required = true)Long id){
		try {
			parameterService.removeById(id);
			return ResponseEntity.ok();
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "导入回款消息明细",httpMethod="POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "模板id", name = "templateId", dataType = "Long", required = true)
    })
    @PostMapping("/importMsgDetail")
    public ResponseEntity importMsgDetail(@RequestParam(name="templateId",required = true)Long templateId,@RequestParam(name="file") CommonsMultipartFile file,HttpServletResponse response, HttpServletRequest request) throws Exception{
		List<List<String>> datas = PoiExcelUtil.getSingleSheet(file, null);
		datas.remove(0);
		int historyColumnSize = 0;
		if(!datas.isEmpty()) {
			historyColumnSize = datas.get(0).size();
		}
		int historyColumnSizeTemp = historyColumnSize;
		validateImportData(datas);
		//验证有错误的数据 
		List<List<String>> validateErrorDatas = datas.stream().filter(e->e.size() > historyColumnSizeTemp).collect(Collectors.toList());
		List<List<String>> validateTrueDatas = datas.stream().filter(e->e.size() <= historyColumnSizeTemp).collect(Collectors.toList());
		if(!validateErrorDatas.isEmpty()) {
			List<List<String>> exportErrorList = new ArrayList<>();
			exportErrorList.addAll(validateTrueDatas);
			exportErrorList.addAll(validateErrorDatas);						
			generateErrorDetail(templateId,exportErrorList);						
			return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT,"文件导入有错误！请点击此处下载");
		}else {
			try {
				msgService.saveMsgDetail(datas,templateId);
			}catch(Exception e) {
				e.printStackTrace();
				datas.forEach(d->d.add(e.getMessage()));							
				generateErrorDetail(templateId,datas);
				return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT,"文件导入有错误！请点击此处下载");
			}
		}
		return ResponseEntity.ok("导入成功");
    }
	
	private void generateErrorDetail(Long templateId,List<List<String>> exportErrorList) {
		String key = IMPORT_TYPE +"_" + UserThreadLocal.get().getUserName();
		String errorFileName = fileShareDir + File.separator + System.currentTimeMillis()+"_错误信息.xlsx";
		String msgType = StringUtils.isBlank(exportErrorList.get(0).get(0))?MsgTypeEnum.WARNING.getMsg():exportErrorList.get(0).get(0);
		Integer code = MsgTypeEnum.getCode(msgType) == null?MsgTypeEnum.WARNING.getCode():MsgTypeEnum.getCode(msgType);
		List<List<String>> oneSheetHeadList =  getsheetHead(templateId,code);
		ExcelWriter excelWriter = EasyExcel.write(new File(errorFileName)).build();
		WriteTable writeTable = EasyExcel.writerTable(0).head(oneSheetHeadList).needHead(true).build();
		WriteSheet sumSheet = EasyExcel.writerSheet("消息模板").build();
		excelWriter.write(exportErrorList, sumSheet, writeTable);
		excelWriter.finish();
		redis.set(key, errorFileName,expiretime);
	}

	@ApiOperation(value = "下载消息导入错误明细",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@GetMapping("/downMsgImportErrorDetail")
    public void downMsgImportErrorDetail(HttpServletResponse response,HttpServletRequest request) throws Exception {  
		
		InputStream is = null;
		try {
			if(redis.get(IMPORT_TYPE+ "_" + UserThreadLocal.get().getUserName()) ==null) {
				throw new RuntimeException("没有消息模板错误明细可供下载。");
			}
			String errorFileName = redis.get(IMPORT_TYPE+ "_" + UserThreadLocal.get().getUserName());    			
			is = new FileInputStream(errorFileName);
			ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("消息模板导入错误明细", response)).withTemplate(is).build();
			WriteSheet sheet = EasyExcel.writerSheet(0).build();			
			workBook.finish();  
			File file = new File(errorFileName);
			if(file.exists()) file.delete();
			redis.delete(IMPORT_TYPE+ "_" + UserThreadLocal.get().getUserName());
       } catch (Exception e) {
           e.printStackTrace();
           LOGGER.error(e.getMessage(),e);
           throw e;
       }finally {
    	   if(is!=null) is.close();
       } 		
    }  
	
	private void validateImportData(List<List<String>> datas) {
		datas.forEach(e->{
			try {
				String msgType = e.get(0);
				String empno = e.get(1);
				if(StringUtils.isBlank(msgType)) throw new RuntimeException("消息类型不能为空");
				if(StringUtils.isBlank(empno)) throw new RuntimeException("工号不能为空");
				if(!msgType.equals(MsgTypeEnum.WARNING.getMsg()) && 
						!msgType.equals(MsgTypeEnum.PUBLIC.getMsg()) 
						   && !msgType.equals(MsgTypeEnum.RESULT.getMsg()))
									throw new RuntimeException("消息类型只能填【预警、公示、结果】");
			}catch(Exception ex) {
				ex.printStackTrace();
				e.add(ex.getMessage());
			}
		});
		int size = datas.stream().collect(Collectors.groupingBy(e->e.get(0))).size();
		if(1 != size) {datas.forEach(e->e.add("只能导入同一种消息类型！"));}
	}
	
	@ApiOperation(value = "批量发送消息",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "消息id，以逗号,分隔", name = "ids", dataType = "String", required = true)
	})
	@PostMapping("/sendMsg")
	public ResponseEntity sendMsg(@RequestParam(name="ids",required = true) String ids) {
		try {
			if(StringUtils.isBlank(ids)) throw new RuntimeException("缺少必传的参数");
			detailService.sendMsg(ids);
			return ResponseEntity.ok();
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "批量删除消息",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "消息id，以逗号,分隔", name = "ids", dataType = "String", required = true)
	})
	@GetMapping("/deleteMsg")
	public ResponseEntity deleteMsg(@RequestParam(name="ids",required = true) String ids) {
		try {
			if(StringUtils.isBlank(ids)) throw new RuntimeException("缺少必传的参数");
			detailService.removeByIds(Arrays.asList(ids.split(",")));
			return ResponseEntity.ok();
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "获取公示消息",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "消息id", name = "id", dataType = "Long", required = true)
	})
	@GetMapping("/getPublicMsg")
	@NoLoginAnno
	public ResponseEntity getPublicMsg(@RequestParam(name="id",required = true) Long id) {
		try {
			Map<String,Object> result = detailService.getPublicMsg(id);
			return ResponseEntity.ok(result);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "提出异议",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "消息id", name = "id", dataType = "Long", required = true),
			@ApiImplicitParam(value = "异议详情", name = "remark", dataType = "String", required = true)
	})
	@PostMapping("/objection")
	@NoLoginAnno
	public ResponseEntity objection(@RequestParam(name="id",required = true) Long id,
							@RequestParam(name="remark",required = true) String remark) {
		try {
			if(StringUtils.isBlank(remark)) return ResponseEntity.error("请填写异议内容");
			detailService.objection(id,remark);
			return ResponseEntity.ok("成功");
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
}
