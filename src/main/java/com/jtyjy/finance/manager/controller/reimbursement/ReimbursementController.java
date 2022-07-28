package com.jtyjy.finance.manager.controller.reimbursement;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.common.tools.QRCodeTool;
import com.jtyjy.core.auth.anno.ApiDataAuthAnno;
import com.jtyjy.core.auth.anno.NoLoginAnno;
import com.jtyjy.core.constant.Constants;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.redis.RedisClient;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.tools.DeviceTools;
import com.jtyjy.core.tools.FileTools;
import com.jtyjy.easy.excel.ImportHelper;
import com.jtyjy.finance.manager.bean.*;
import com.jtyjy.finance.manager.constants.ReimbursementStepHelper;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.CheckPassRequest;
import com.jtyjy.finance.manager.dto.ReimbursementRequest;
import com.jtyjy.finance.manager.dto.bxExcel.*;
import com.jtyjy.finance.manager.easyexcel.EasyExcelImportListener;
import com.jtyjy.finance.manager.easyexcel.ExtractInfoExportExcelData;
import com.jtyjy.finance.manager.easyexcel.PayErrorImportExcelData;
import com.jtyjy.finance.manager.easyexcel.PayVerifyExcelData;
import com.jtyjy.finance.manager.event.bx.BxCodeRequest;
import com.jtyjy.finance.manager.event.bx.GunBxCodeRequest;
import com.jtyjy.finance.manager.event.bx.WeChatBxCodeRequest;
import com.jtyjy.finance.manager.interceptor.UserThreadLocal;
import com.jtyjy.finance.manager.service.*;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.HtmlUtil;
import com.jtyjy.finance.manager.utils.RequestAnswerTool;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.*;
import com.jtyjy.weixin.message.MessageSender;
import com.jtyjy.weixin.message.QywxTextMsg;
import com.klcwqy.easy.lock.impl.ZookeeperShareLock;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.curator.framework.CuratorFramework;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报销接口
 * @author User
 *
 */
@Api(tags = "报销接口")
@RestController
@RequestMapping("/api/reimbursement")
@CrossOrigin
public class ReimbursementController {
	
	private static final FastDateFormat FULL_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	private BudgetReimbursementorderService service;
	
	@Autowired
	private ReimbursementWorker worker;
	
	@Autowired
	private ReimbursementFlowWorker flowWorker;
	
	@Autowired
	private WeChatService weChatService;
	
	@Autowired
	private RedisClient redis;
	
	@Autowired
	private MessageSender messageSender;
	
	@Autowired
	private BudgetPaybatchService payBatchService;
	   
    @Autowired
    private BudgetPaymoneyService payMoneyService;
    
	@Autowired
	private WbUserService userService;
    
    @Autowired
    private HrService hrService;   

    
    @Autowired
    private TabDmService dmService;
		
    @Autowired
    private BudgetReimbursementorderEntertainService entertainService;
    
    @Autowired
    private BudgetReimbursementorderTravelService travelService;
    
    @Autowired
    private BudgetReimburmentTimedetailService timeDetailService;
    
	@Value("${service.domain}")
	private String domain;

	@Value("${bx.code.domain}")
	private String qywxDomain;
	
	@Value("${bx.step.key}")
	private String bx_step_key;
	
	@Value("${bx.step.ttl}")
	private Integer bx_step_ttl;
	
	public static final String STEP_OPT_SPLIT = "_";
	
	public static final String REDIS_BXDH = "BXDH";
	
	public static final String REDIS_BXDID = "BXDID";
	
	public static final String BXIMPORT = "BXIMPORT";
		   
    public final static String PEIMPORT = "PEIMPORT";  

    public final static String PVIMPORT = "PVIMPORT"; 
    
    @Value("${file.shareDir}") 
    private String fileShareDir;
        
    @Value("${redis.file.key.expiretime}") 
    private Integer expiretime;   
    
    @Value("${file.share.template}") 
    private String file_share_template;

	@Autowired
	private CuratorFramework curatorFramework;
    
    private final static Map<String,String> fileMap = new HashMap<>();


//    @GetMapping("initReimcode")
//    @NoLoginAnno
//    public void initReimcode(){
//	    service.initReimcode();
//    }

	@PostMapping("opt")
	@ApiOperation(value = "保存、修改报销单", httpMethod = "POST")
	@ApiImplicitParams(
			 @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
			)
	public ResponseEntity<String> opt(@RequestBody ReimbursementRequest request) throws Exception{
		//是否月结
		this.service.checkIsMonthEnd(request.getOrder(),request.getOrderAllocated());
		String result = null;
		//是否提交
		boolean submit = "1".equals(request.getSubmit());
		if(ReimbursementRequest.SAVE.equals(request.getRequestType())) {
			//保存
			result = this.worker.save(request,submit);
		}
		if(ReimbursementRequest.MODIFY.equals(request.getRequestType())) {
			//修改/修改提交
			result = this.worker.update(request, submit);
		}
//		if(ReimbursementRequest.SUBMIT.equals(request.getRequestType())) {
//			//提交
//			result = this.worker.submit(request);
//		}
		//仅提交
		if(StringUtils.isEmpty(result)) {
			return ResponseEntity.ok("操作成功!");
		}
		return ResponseEntity.error(result);
	}

	/**
	 * 固定资产专属
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiIgnore
	@PostMapping("optForAsset")
	@NoLoginAnno
	public ResponseEntity<String> optForAsset(@RequestBody ReimbursementRequest request) throws Exception{
		//是否月结
		this.service.checkIsMonthEnd(request.getOrder(),request.getOrderAllocated());
		String result = null;
		//是否提交
		boolean submit = "1".equals(request.getSubmit());
		if(ReimbursementRequest.SAVE.equals(request.getRequestType())) {
			//保存
			result = this.worker.save(request,submit);
		}
		if(ReimbursementRequest.MODIFY.equals(request.getRequestType())) {
			//修改/修改提交
			result = this.worker.update(request, submit);
		}
		//仅提交
		if(StringUtils.isEmpty(result)) {
			return ResponseEntity.ok("操作成功!");
		}
		return ResponseEntity.error(result);
	}


    @ApiOperation(value = "下载申请报销导入模板",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/downBxApplyImportTemplate")
    public void downBxApplyImportTemplate(HttpServletResponse response) throws Exception {  
        try {
            String filePath = file_share_template + File.separator + "bxApplyImportTemplate.xls";
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getXlsOutputStream("申请报销导入模板", response)).withTemplate(filePath).build();
            WriteSheet sheet = EasyExcel.writerSheet(0).build();
            List<Map<String,Object>> list = new ArrayList<>();
            workBook.fill(list, sheet);
            workBook.finish();
       } catch (Exception e) {
           e.printStackTrace();
       }
    }
    
    @Autowired
    private BxImportExcelPostProcessor processor;    

    @ApiOperation(value = "报销申请导入", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importApply")
    public ResponseEntity<String> importApply(@RequestParam(name="file") CommonsMultipartFile file,HttpServletResponse response) throws Exception {
	    String extension = EasyExcelUtil.getFileExtension(file.getFileItem().getName());
	    if(!"XLS".equals(extension.toUpperCase())) return ResponseEntity.error("只支持导入xls格式的文件！");
        Object[] obj = FileTools.commonsMultipartFile2File(file, this.fileShareDir);
        
        ImportHelper helper = new ImportHelper(obj[0].toString());
        helper.addProcessors(processor);
        List<Sheet> sheets;
        sheets = helper.loadSheet();
        Map<String, Object> map = helper.doImport(sheets, BxInfoSheetDto.class);
        Map<String, Object> cz_map = helper.doImport(sheets, StrickSheetDto.class);
        Map<String, Object> zz_map = helper.doImport(sheets, TransferSheetDto.class);
        Map<String, Object> cl_map = helper.doImport(sheets, TravelSheetDto.class);
        Map<String, Object> zd_map = helper.doImport(sheets, EntertainSheetDto.class);
        Map<String, Object> hb_map = helper.doImport(sheets, HbSheetDto.class);
        if(helper.getErrorFile()) {
            helper.end(true);
            File file2 = (File)obj[1];
            if(file2.exists())file2.delete();
            String fileExtension = EasyExcelUtil.getFileExtension(obj[0].toString());
            String fileNameNotExtension = EasyExcelUtil.getFileNameNotExtension(obj[0].toString());
            String errorFileName = fileNameNotExtension+"_错误信息."+fileExtension;
            String key = BXIMPORT +"_" + UserThreadLocal.get().getUserName();
            this.redis.set(key, errorFileName, expiretime);
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT,"文件导入有错误,请点击此处下载");
        }
        
        ReimbursementRequest dataRequest = new ReimbursementRequest();
        BudgetReimbursementorder order = new BudgetReimbursementorder();
        order.setYearid((Long)map.get("yearId"));
        order.setUnitid((Long)map.get("unitId"));
        order.setMonthid((Long)map.get("monthId"));
        order.setReimperonsid((String)map.get("bxrId"));
        order.setReimperonsname((String)map.get("bxrName"));
        order.setReimdate((Date) map.get("bxDate"));
        order.setOthermoney(null == map.get("f3") ? BigDecimal.ZERO : new BigDecimal((Double)map.get("f3")));
        order.setAttachcount((Integer)map.get("b4"));
        order.setBxtype((Integer)map.get("bxType"));
        order.setRemark((String)map.get("f4"));
        order.setTraveler((String)cl_map.get("name"));
        order.setTravelreason((String)cl_map.get("reason"));
        order.setId(null == map.get("id") ? null : (Long)map.get("id"));;
	    BigDecimal bxje = BigDecimal.ZERO, nonje = BigDecimal.ZERO, czje = BigDecimal.ZERO, zzje = BigDecimal.ZERO, xjje = BigDecimal.ZERO, hbje = BigDecimal.ZERO;
        //明细信息组装
        List<BudgetReimbursementorderDetail> orderDetail = new ArrayList<>();
        List<BxDetailDto> bxDetailList= (List)map.get("detail");
        for(BxDetailDto dto : bxDetailList) {
            orderDetail.add(new BudgetReimbursementorderDetail(dto));
            bxje = bxje.add(new BigDecimal(String.valueOf(dto.getBxAmount())));
            if ("否".equals(dto.getInclude())) {
                nonje = nonje.add(new BigDecimal(String.valueOf(dto.getBxAmount())));
            }
        };
        //冲账信息组装
        List<BudgetReimbursementorderPayment> orderPayment = new ArrayList<>();
        List<StrickDetailDto> czDetailList = (List)cz_map.get("detail");
        for(StrickDetailDto dto : czDetailList){
            if (StringUtils.isBlank(BaseController.validate(dto))) {
                orderPayment.add(new BudgetReimbursementorderPayment(dto));
                czje = czje.add(new BigDecimal(String.valueOf(dto.getCzMoney())));
            }
        }
        //转账信息组装
        List<BudgetReimbursementorderTrans> orderTrans = new ArrayList<>();
        List<TransferDetailDto> zzDetailList = (List)zz_map.get("detail");
        for(TransferDetailDto dto :zzDetailList){
            if (StringUtils.isBlank(BaseController.validate(dto))) {
                orderTrans.add(new BudgetReimbursementorderTrans(dto));
                zzje = zzje.add(new BigDecimal(String.valueOf(dto.getMoney())));
            }
        }
        //差旅信息组装
        List<BudgetReimbursementorderTravel> orderTravel = new ArrayList<>();
        List<TravelDetailDto> clDetailList = (List)cl_map.get("detail");
        clDetailList.stream().filter(dto -> StringUtils.isBlank(BaseController.validate(dto))).forEach(dto -> {orderTravel.add(new BudgetReimbursementorderTravel(dto));});
        //招待信息组装
        List<BudgetReimbursementorderEntertain> orderEntertain = new ArrayList<>();
        List<EntertainDetailDto> zdDetailList = (List)zd_map.get("detail");
        zdDetailList.stream().filter(dto -> StringUtils.isBlank(BaseController.validate(dto))).forEach(dto -> {orderEntertain.add(new BudgetReimbursementorderEntertain(dto));});
        //划拨信息组装
        List<BudgetReimbursementorderAllocated> orderAllocated = new ArrayList<>();
        List<HbDetailDto> hbDetailList = (List)hb_map.get("detail");
        for(HbDetailDto dto : hbDetailList){
            if (StringUtils.isBlank(BaseController.validate(dto))) {
                orderAllocated.add(new BudgetReimbursementorderAllocated(dto));
                hbje=hbje.add(new BigDecimal(String.valueOf(dto.getHbMoney())));
            }
        }
        order.setReimmoney(bxje);
        order.setNonreimmoney(nonje);
        order.setPaymentmoney(czje);
        order.setAllocatedmoney(hbje);
        order.setTransmoney(zzje);
        order.setCashmoney(xjje);
        dataRequest.setOrder(order);
        dataRequest.setOrderDetail(orderDetail);;
        dataRequest.setOrderPayment(orderPayment);
        dataRequest.setOrderTrans(orderTrans);
        dataRequest.setOrderAllocated(orderAllocated);
        dataRequest.setOrderTravel(orderTravel);
        dataRequest.setOrderEntertain(orderEntertain);
        String result;
	    //报销单号不存在
        if (null == map.get("f4") || StringUtils.isBlank((String)map.get("f4"))) {
            dataRequest.setRequestType(ReimbursementRequest.SAVE);
            dataRequest.setSubmit("0");
            result = this.worker.save(dataRequest, false);
        }else {
            dataRequest.setRequestType(ReimbursementRequest.MODIFY);
            dataRequest.setSubmit("0");
            result = this.worker.update(dataRequest, false);
        }
        if(StringUtils.isEmpty(result)) {
            return ResponseEntity.ok("导入成功!");
        }
        return ResponseEntity.error(result);
        
    }
    
    @ApiOperation(value = "下载申请报销导入的错误明细",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/downBxApplyImportError")
    public void downBxApplyImportError(HttpServletResponse response) throws Exception {
        try {
            if(redis.get(BXIMPORT+ "_" + UserThreadLocal.get().getUserName()) ==null) {
                throw new RuntimeException("没有报销导入错误明细可供下载。");
            }
            String errorFileName = redis.get(BXIMPORT+ "_" + UserThreadLocal.get().getUserName());
	        ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getXlsOutputStream("申请报销导入错误明细", response)).withTemplate(errorFileName).build();
	        WriteSheet sheet = EasyExcel.writerSheet(0).build();
	        workBook.finish();
            File file = new File(errorFileName);
            if(file.exists()) file.delete();
            redis.delete(BXIMPORT+ "_" + UserThreadLocal.get().getUserName());
       } catch (Exception e) {
           e.printStackTrace();
           throw e;
       }
    } 
    
    @ApiOperation(value = "导出报销单明细",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销主键", name = "id", dataType = "Long", required = true)
    })
    @GetMapping("/exportBxInfo")
    public void exportBxInfo(Long id, HttpServletResponse response) throws Exception {  
        this.service.exportBxInfo(id, response);
    }       
    
    @ApiOperation(value = "导出冲账支付",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销主键", name = "id", dataType = "Long", required = true)
    })
    @GetMapping("/exportStrick")
    public void exportStrick(Long id, HttpServletResponse response) throws Exception {
       this.service.exportStrick(id, response);
    }  
    
    @GetMapping("withDraw")
    @ApiOperation(value = "报销单撤回", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销主键", name = "id", dataType = "Long", required = true)
    })
    public ResponseEntity<String> withDraw(Long id) {
        String result = this.service.withDraw(id);
        if (StringUtils.isBlank(result)) {
            return ResponseEntity.ok(result);
        }else {
            return ResponseEntity.error(result);
        }
    }
	
	/**
	 * 分页查询
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@PostMapping("pageLike")
	@ApiOperation(value = "分页查询", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "是否预算员", name = "budgeterflag", dataType = "Boolean", required = true),
            @ApiImplicitParam(value = "报销单号", name = "reimcode", dataType = "String", required = false),
			@ApiImplicitParam(value = "报销单状态", name = "reuqeststatus", dataType = "Long", required = false),
			@ApiImplicitParam(value = "界别", name = "yearid", dataType = "Long", required = false),
			@ApiImplicitParam(value = "月份", name = "monthid", dataType = "Long", required = false),
			@ApiImplicitParam(value = "预算单位名称（模糊查询）", name = "ysdw", dataType = "String", required = false),
			@ApiImplicitParam(value = "报销人（模糊查询）", name = "bxr", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销日期（yyyy-mm-dd）", name = "bxrq", dataType = "String", required = false),
			@ApiImplicitParam(value = "报销金额前区间", name = "bxMoneyStart", dataType = "BigDecimal", required = false),
			@ApiImplicitParam(value = "报销金额后区间", name = "bxMoneyEnd", dataType = "BigDecimal", required = false),
			@ApiImplicitParam(value = "科目名称", name = "subjectName", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销金额", name = "bxje", dataType = "Double", required = false),
            @ApiImplicitParam(value = "冲账金额", name = "czje", dataType = "Double", required = false),
            @ApiImplicitParam(value = "转账金额", name = "zzje", dataType = "Double", required = false),
            @ApiImplicitParam(value = "现金金额", name = "xjje", dataType = "Double", required = false),
            @ApiImplicitParam(value = "划拨金额", name = "hbje", dataType = "Double", required = false),
            @ApiImplicitParam(value = "其他金额", name = "othermoney", dataType = "Double", required = false),
            @ApiImplicitParam(value = "提交日期（yyyy-mm-dd）", name = "submittime", dataType = "String", required = false),
            @ApiImplicitParam(value = "申请日期（yyyy-mm-dd）", name = "applicanttime", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销类型，多个用逗号分隔,1：通用，2:差旅报销，3:招待报销,4:差旅补贴,5:推广招待", name = "bxType", dataType = "String", required = false),
            @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
	})
	@ApiDataAuthAnno
	public ResponseEntity<Page<ReimbursementInfoVO>> pageLike(Boolean budgeterflag,
	                                                          String reimcode, Integer reuqeststatus, Integer yearid, Integer monthid, String ysdw,
	                                                            String bxr, String bxrq, Double bxje, Double czje, Double zzje,
	                                                          Double xjje, Double hbje, Double othermoney, String submittime,
	                                                          String applicanttime,String bxType,BigDecimal bxMoneyStart,BigDecimal bxMoneyEnd,String subjectName,
	                                                          @RequestParam(defaultValue = "1") Integer page,
			                                                  @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception{
        List<Long> bxTypes = splitStringToLong(bxType);
	    Map<String, Object> conditionMap = new HashMap<>();
	    conditionMap.put("reuqeststatus", reuqeststatus);
	    conditionMap.put("reimcode", reimcode);
	    conditionMap.put("yearid", yearid);
	    conditionMap.put("monthid", monthid);
	    conditionMap.put("bxr", bxr);
	    conditionMap.put("bxrq", bxrq);
	    conditionMap.put("bxje", bxje);
	    conditionMap.put("czje", czje);
	    conditionMap.put("zzje", zzje);
	    conditionMap.put("xjje", xjje);
        conditionMap.put("hbje", hbje);
        conditionMap.put("bxMoneyStart", bxMoneyStart);
        conditionMap.put("bxMoneyEnd", bxMoneyEnd);
        conditionMap.put("subjectName", subjectName);
        conditionMap.put("othermoney", othermoney);
        conditionMap.put("ysdw", ysdw);
        conditionMap.put("submittime", submittime);
        conditionMap.put("applicanttime", applicanttime);
        conditionMap.put("bxTypes",bxTypes);
        String authSql = "";
        if (budgeterflag) {
        	//增加权限控制
        	authSql = JdbcSqlThreadLocal.get();
        	//List<String> baseUnitIdList = unitService.getBaseUnitIdListByAuthCenter(authSql);
        	//conditionMap.put("baseUnitIdList", baseUnitIdList);
            conditionMap.put("managers", UserThreadLocal.get().getUserId());
        }else {       	
            conditionMap.put("applicantid", UserThreadLocal.get().getUserId());
        }
	    Page<ReimbursementInfoVO> result = this.service.pageLike(page, rows, conditionMap,authSql);
		return ResponseEntity.ok(result);
	}
	
	@GetMapping("detail")
	@ApiOperation(value = "报销单详情", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "报销主键", name = "id", dataType = "Long", required = true)
			
	})
	public ResponseEntity<ReimbursementRequest> detail(long id) {
		ReimbursementRequest result = this.service.detail(id);
		return ResponseEntity.ok(result);
	}	
	   
    @GetMapping("redFlush")
    @ApiOperation(value = "报销单冲红", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销主键", name = "id", dataType = "Long", required = true)
            
    })
    public ResponseEntity<ReimbursementRequest> redFlush(long id) throws Exception {
        this.service.redFlush(id);
        return ResponseEntity.ok();
    }
	
	@PostMapping("agentDetail")
	@ApiOperation(value = "报销明细", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "是否预算员", name = "budgeterflag", dataType = "Boolean", required = true),
            @ApiImplicitParam(value = "报销单号（模糊查询）", name = "reimcode", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销单状态", name = "reuqeststatus", dataType = "String", required = false),
            @ApiImplicitParam(value = "届别", name = "yearid", dataType = "String", required = false),
            @ApiImplicitParam(value = "月份", name = "monthid", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销人（模糊查询）", name = "bxr", dataType = "String", required = false),
            @ApiImplicitParam(value = "科目名称（模糊查询）", name = "subjectname", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销金额", name = "reimmoney", dataType = "Double", required = false),
            @ApiImplicitParam(value = "计入执行 true：是 false：否", name = "reimflag", dataType = "Boolean", required = false),
            @ApiImplicitParam(value = "报销种类（划拨/报销）", name = "reimflagtype", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销单位（模糊查询）", name = "bxunitname", dataType = "String", required = false),
            @ApiImplicitParam(value = "预算单位（模糊查询）", name = "unitname", dataType = "String", required = false),
            @ApiImplicitParam(value = "动因名称（模糊查询）", name = "monthagentname", dataType = "String", required = false),
            @ApiImplicitParam(value = "摘要（模糊查询）", name = "summary", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销金额前区间", name = "bxMoneyStart", dataType = "BigDecimal", required = false),
            @ApiImplicitParam(value = "报销金额后区间", name = "bxMoneyEnd", dataType = "BigDecimal", required = false),
            @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
            @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
	@ApiDataAuthAnno
    public ResponseEntity<Page<BxDetailVO>> agentDetail(Boolean budgeterflag, String reimcode, String reuqeststatus, String yearid, String monthid, String bxr, String subjectname, Double reimmoney, Boolean reimflag, String reimflagtype, String bxunitname,
            String unitname, String monthagentname, String summary,BigDecimal bxMoneyStart,BigDecimal bxMoneyEnd,
                                                        @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception{
        List<Long> requestStatusIds = splitStringToLong(reuqeststatus);
        List<Long> yearIds = splitStringToLong(yearid);
        List<Long> monthIds = splitStringToLong(monthid);
        Map<String, Object> conditionMap = new HashMap<String, Object>();
	    conditionMap.put("reimcode", reimcode);
	    conditionMap.put("reuqeststatus", requestStatusIds);
        conditionMap.put("yearid", yearIds);
        conditionMap.put("monthid", monthIds);
        conditionMap.put("bxr", bxr);
        conditionMap.put("subjectname", subjectname);
        conditionMap.put("reimmoney", reimmoney);
        conditionMap.put("reimflag", reimflag);
        conditionMap.put("reimflagtype", reimflagtype);
        conditionMap.put("bxunitname", bxunitname);
        conditionMap.put("unitname", unitname);
        conditionMap.put("monthagentname", monthagentname);
        conditionMap.put("bxMoneyStart",bxMoneyStart);
        conditionMap.put("bxMoneyEnd",bxMoneyEnd);
        conditionMap.put("summary",summary);
        if (budgeterflag) {
            conditionMap.put("managers", UserThreadLocal.get().getUserId());
		}else {
		    conditionMap.put("applicantid", UserThreadLocal.get().getUserId());
		}
        Page<BxDetailVO> result = this.service.agentDetail(page, rows, conditionMap);
		return ResponseEntity.ok(result);
	}

    private List<Long> splitStringToLong(String str) {
        List<Long> requestStatusIds = new ArrayList<>();
        if(StringUtils.isNotBlank(str)){
            String[] dutyMan = str.split(",");
            Long[] dutyIds = (Long[]) ConvertUtils.convert(dutyMan,Long.class);
            requestStatusIds = Arrays.asList(dutyIds);
        }
        return requestStatusIds;
    }


    @GetMapping("exportBxDetails")
    @ApiOperation(value = "导出报销明细", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "是否预算员", name = "budgeterflag", dataType = "Boolean", required = true),
            @ApiImplicitParam(value = "报销单号（模糊查询）", name = "reimcode", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销单状态", name = "reuqeststatus", dataType = "String", required = false),
            @ApiImplicitParam(value = "届别", name = "yearid", dataType = "String", required = false),
            @ApiImplicitParam(value = "月份", name = "monthid", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销人（模糊查询）", name = "bxr", dataType = "String", required = false),
            @ApiImplicitParam(value = "科目名称（模糊查询）", name = "subjectname", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销金额", name = "reimmoney", dataType = "Double", required = false),
            @ApiImplicitParam(value = "计入执行 true：是 false：否", name = "reimflag", dataType = "Boolean", required = false),
            @ApiImplicitParam(value = "报销种类（划拨/报销）", name = "reimflagtype", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销单位（模糊查询）", name = "bxunitname", dataType = "String", required = false),
            @ApiImplicitParam(value = "预算单位（模糊查询）", name = "unitname", dataType = "String", required = false),
            @ApiImplicitParam(value = "动因名称（模糊查询）", name = "monthagentname", dataType = "String", required = false),
            @ApiImplicitParam(value = "摘要（模糊查询）", name = "summary", dataType = "String", required = false)
    })
    @ApiDataAuthAnno
    public void exportBxDetails(@RequestParam(name="budgeterflag",required = true)Boolean budgeterflag, @RequestParam(name="reimcode", defaultValue = "")String reimcode, @RequestParam(name="reuqeststatus", defaultValue = "")String reuqeststatus, @RequestParam(name="yearid", defaultValue = "")String yearid, @RequestParam(name="monthid", defaultValue = "")String monthid,
            @RequestParam(name="bxr", defaultValue = "")String bxr, @RequestParam(name="subjectname", defaultValue = "")String subjectname, @RequestParam(name="reimmoney", defaultValue = "")Double reimmoney, @RequestParam(name="reimflag", defaultValue = "")Boolean reimflag, @RequestParam(name="reimflagtype", defaultValue = "")String reimflagtype, @RequestParam(name="bxunitname", defaultValue = "")String bxunitname, @RequestParam(name="unitname", defaultValue = "")String unitname, @RequestParam(name="monthagentname", defaultValue = "")String monthagentname,
                                String summary,HttpServletResponse response) throws Exception{
        List<Long> requestStatusIds = splitStringToLong(reuqeststatus);
        List<Long> yearIds = splitStringToLong(yearid);
        List<Long> monthIds = splitStringToLong(monthid);
	    Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("reimcode", reimcode);
        conditionMap.put("reuqeststatus", requestStatusIds);
        conditionMap.put("yearid", yearIds);
        conditionMap.put("monthid", monthIds);
        conditionMap.put("bxr", bxr);
        conditionMap.put("subjectname", subjectname);
        conditionMap.put("reimmoney", reimmoney);
        conditionMap.put("reimflag", reimflag);
        conditionMap.put("reimflagtype", reimflagtype);
        conditionMap.put("bxunitname", bxunitname);
        conditionMap.put("unitname", unitname);
        conditionMap.put("monthagentname", monthagentname);
        conditionMap.put("summary",summary);
        if (budgeterflag) {
            conditionMap.put("managers", UserThreadLocal.get().getUserId());
        }else {
            conditionMap.put("applicantid", UserThreadLocal.get().getUserId());
        }
        List<BxDetailVO> voList = this.service.agentDetailNoPage(conditionMap);
        List<List<String>> dataList = new ArrayList<>();
        for (BxDetailVO vo : voList) {
            List<String> colList = new ArrayList<>();
            colList.add(vo.getReuqeststatusText());
            colList.add(vo.getReimcode());
            colList.add(vo.getBxunitname());
            colList.add(vo.getYearname());
            colList.add(vo.getMonthname());
            colList.add(vo.getUnitname());
            colList.add(vo.getBxr());
            colList.add(vo.getBunitname());
            colList.add(vo.getSubjectname());
            colList.add(vo.getMonthagentname());
            colList.add(vo.getReimmoney().toString());
            colList.add(vo.getReimflag() ? "是" : "否");
            colList.add(vo.getReimflagtype());
            colList.add(vo.getRemark());
            colList.add(vo.getPayunitname());
            dataList.add(colList);
        }
        ResponseUtil.exportBxDetailed(dataList,  EasyExcelUtil.getOutputStream("导出报销明细", response));
    }   
    
    @ApiOperation(value = "导出时间节点表",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Long"),
            @ApiImplicitParam(value = "月份id", name = "monthId", dataType = "Long")
    })
    @GetMapping("/exportBxTime")
    public void exportBxTime(@RequestParam(name="yearId", defaultValue = "")Long yearId, @RequestParam(name="monthId", defaultValue = "")Long monthId, HttpServletResponse response) throws Exception{
        String authSql = "";
        //增加权限控制
        authSql = JdbcSqlThreadLocal.get();
        this.timeDetailService.exportBxTime(yearId, monthId, authSql, response);
    }
    
    @ApiOperation(value = "导出招待汇总",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销类型（3：招待报销；5：推广招待）", name = "bxType", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "月份id", name = "monthId", dataType = "Long")
    })
    @GetMapping("/exportEntertainSum")
    public void exportEntertainSum(@RequestParam(name="bxType",required = true)Integer bxType, @RequestParam(name="yearId",required = true)Long yearId, @RequestParam(name="monthId")Long monthId, HttpServletResponse response) throws Exception{
        this.entertainService.exportEntertainSum(bxType, yearId, monthId, response);
    }

    
    @ApiOperation(value = "导出差旅汇总",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销类型（2：差旅报销；4：差旅补贴）", name = "bxType", dataType = "Integer", required = true),
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "月份id", name = "monthId", dataType = "Long")
    })
    
    @GetMapping("/exportTravelSum")
    public void exportTravelSum(@RequestParam(name="bxType",required = true)Integer bxType, @RequestParam(name="yearId",required = true)Long yearId, @RequestParam(name="monthId")Long monthId, HttpServletResponse response) throws Exception{
        this.travelService.exportTravelSum(bxType, yearId, monthId, response);
    }
    
    @ApiOperation(value = "导出退回原因汇总",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Long"),
            @ApiImplicitParam(value = "月份id", name = "monthId", dataType = "Long")
    })
    @GetMapping("/exportReturnReason")
    public void exportReturnReason(@RequestParam(name="yearId", defaultValue = "")Long yearId, @RequestParam(name="monthId", defaultValue = "")Long monthId, HttpServletResponse response) throws Exception{
        this.service.exportReturnReason(yearId, monthId, response);
    }
    
	@GetMapping("payPage")
	@ApiOperation(value = "付款明细", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "付款状态", name = "paymoneystatus", dataType = "Integer", required = false),
            @ApiImplicitParam(value = "付款批次号（模糊查询）", name = "paybatchcode", dataType = "String", required = false),
            @ApiImplicitParam(value = "付款金额", name = "paymoney", dataType = "Double", required = false),
            @ApiImplicitParam(value = "报销单号（模糊查询）", name = "paymoneyobjectcode", dataType = "String", required = false),
            @ApiImplicitParam(value = "付款单位（模糊查询）", name = "bunitname", dataType = "String", required = false),
            @ApiImplicitParam(value = "付款银行", name = "bunitaccountbranchname", dataType = "String", required = false),
            @ApiImplicitParam(value = "收款人名称（模糊查询）", name = "bankaccountname", dataType = "String", required = false),
            @ApiImplicitParam(value = "支付日期（yyyy-mm-dd）", name = "paytime", dataType = "String", required = false),
			@ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
	})
	public ResponseEntity<Page<BudgetPaymoney>> payPage(Integer paymoneystatus, String paybatchcode, Double paymoney, String paymoneyobjectcode, String bunitname, String bunitaccountbranchname, String bankaccountname, String paytime, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception {
	    Map<String, Object> conditionMap = new HashMap<>();
	    conditionMap.put("paymoneystatus", paymoneystatus);
	    conditionMap.put("paybatchcode", paybatchcode);
	    conditionMap.put("paymoney", paymoney);
	    conditionMap.put("paymoneyobjectcode", paymoneyobjectcode);
	    conditionMap.put("bunitname", bunitname);
	    conditionMap.put("bunitaccountbranchname", bunitaccountbranchname);
	    conditionMap.put("bankaccountname", bankaccountname);
	    conditionMap.put("paytime", paytime);
	    Page<BudgetPaymoney> result = this.payMoneyService.payPage(page, rows, conditionMap);
		return ResponseEntity.ok(result);
	}
	
    @GetMapping("liuzhuanPage")
    @ApiOperation(value = "流转记录", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销单号", name = "reimcode", dataType = "String", required = true),
            @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
            @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
    public ResponseEntity<Page<BxLiuZhuanVO>> liuzhuanPage(String reimcode, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception {
        Page<BxLiuZhuanVO> vo = this.service.liuzhuanPage(page, rows, reimcode);
        return ResponseEntity.ok(vo);
    }	
	  
    @GetMapping("errPayPage")
    @ApiOperation(value = "异常付款分页查询", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "付款单号（模糊查询）", name = "paymoneycode", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销号（模糊查询）", name = "paymoneyobjectcode", dataType = "String", required = false),
            @ApiImplicitParam(value = "收款人名称（模糊查询）", name = "bankaccountname", dataType = "String", required = false),
            @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
            @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
    @ApiDataAuthAnno
    public ResponseEntity<Page<BudgetPaymoney>> errPayPage(String paymoneycode, String paymoneyobjectcode, String bankaccountname, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception { 
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("paymoneycode", paymoneycode);
        conditionMap.put("paymoneyobjectcode", paymoneyobjectcode);
        conditionMap.put("bankaccountname", bankaccountname);
        Page<BudgetPaymoney> vo = this.service.getErrorPaymoney(page, rows, conditionMap);
        return ResponseEntity.ok(vo);
    } 
    
    @GetMapping("oneErrPay")
    @ApiOperation(value = "根据付款单号获取付款失败详情", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "付款单号", name = "paymoneycode", dataType = "String", required = true)
    })
    @NoLoginAnno
    public ResponseEntity<List<BudgetPaymoney>> oneErrPay(String paymoneycode) throws Exception { 

        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("paymoneycode", paymoneycode);
        List<BudgetPaymoney> vo = this.service.getErrorPaymoney(1, 100, conditionMap).getRecords();
        return ResponseEntity.ok(vo);
    }
    
    @GetMapping("progressPage")
    @ApiOperation(value = "报销审核列表分页查询", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "环节代码（报销审核：1&2；单据接收：1；预算审核：2；分单扫描：3；分单确认：4；会计做账：6；凭证审核：7；法人公司抽单：8；财务总监审核：10；总经理审核：11）", name = "linkCode", dataType = "String", required = true),
            @ApiImplicitParam(value = "是否历史数据（4/6/7无历史数据）", name = "isHis", dataType = "Boolean", required = true),
            @ApiImplicitParam(value = "报销单号（模糊查询）", name = "reimcode", dataType = "String"),
            @ApiImplicitParam(value = "报销金额", name = "reimmoney", dataType = "Double"),
            @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
            @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
    public ResponseEntity<Page<ReimbursementInfoVO>> progressPage(String linkCode, Boolean isHis, String reimcode, Double reimmoney, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception {
        Page<ReimbursementInfoVO> vo = this.service.progressPage(page, rows, linkCode, isHis, reimcode, reimmoney);
        return ResponseEntity.ok(vo);
    }           
    
    @GetMapping("accountTaskPage")
    @ApiOperation(value = "做账任务分页查询", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销单号（模糊查询）", name = "reimcode"),
            @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
            @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
    public ResponseEntity<Page<MakeAccountTaskVO>> accountTaskPage(String reimcode, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception {
        Page<MakeAccountTaskVO> vo = this.service.accountTaskPage(page, rows, reimcode);
        return ResponseEntity.ok(vo);
    }   
    
    @GetMapping("fdDetailPage")
    @ApiOperation(value = "分单任务分页查询", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销单号（模糊查询）", name = "reimcode"),
            @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
            @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
    public ResponseEntity<Page<MakeAccountTaskVO>> fdDetailPage(String reimcode, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception {
        Page<MakeAccountTaskVO> vo = this.service.getFdDetailPage(page, rows, reimcode);
        return ResponseEntity.ok(vo);
    } 
    
    @PostMapping("cashPageInfo")
    @ApiOperation(value = "出纳付款分页查询", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销单号（模糊查询）", name = "paymoneyobjectcode", dataType = "String"),
            @ApiImplicitParam(value = "付款单号（模糊查询）", name = "paymoneycode", dataType = "String"),
            @ApiImplicitParam(value = "收款人（模糊查询）", name = "bankaccountname", dataType = "String"),
            @ApiImplicitParam(value = "收款银行（模糊查询）", name = "bankaccountbranchname", dataType = "String"),
            @ApiImplicitParam(value = "付款单位（模糊查询）", name = "bunitname", dataType = "String"),
            @ApiImplicitParam(value = "批次号（模糊查询）", name = "paybatchcode", dataType = "String"),
            @ApiImplicitParam(value = "付款状态 0:等待付款；1：接收付款；2：正在付款；3：已经付款", name = "paymoneystatus", dataType = "Integer"),
            @ApiImplicitParam(value = "付款单类型：1：报销转账付款 2：付款失败修改发放付款 3：(日常)借款付款 4：资金调拨付款 5:项目现金付款 6:项目转账付款（借款）", name = "paymoneytype", dataType = "Integer"),
            @ApiImplicitParam(value = "支付类型：0:现金；1:转账；2:报销", name = "paytype", dataType = "Integer"),
            @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
            @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false),
		    @ApiImplicitParam(value = "来源类型(1:预算系统，2：oa系统)", name = "sourceType", dataType = "Integer", required = false)
    })
    public ResponseEntity<Page<BudgetPaymoney>> cashPageInfo(String paymoneyobjectcode, String paymoneycode, String bankaccountname, String bankaccountbranchname, String bunitname, String paybatchcode, Integer paymoneystatus, Integer paymoneytype, Integer paytype, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows,Integer sourceType) throws Exception {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("paymoneyobjectcode", paymoneyobjectcode);
        conditionMap.put("paymoneycode", paymoneycode);
        conditionMap.put("bankaccountname", bankaccountname);
        conditionMap.put("bankaccountbranchname", bankaccountbranchname);
        conditionMap.put("bunitname", bunitname);
        conditionMap.put("paybatchcode", paybatchcode);
        conditionMap.put("paymoneystatus", paymoneystatus);
        conditionMap.put("paymoneytype", paymoneytype);
        conditionMap.put("paytype", paytype);
        conditionMap.put("sourceType", sourceType);
        Page<BudgetPaymoney> vo = this.payMoneyService.cashPageInfo(page, rows, conditionMap);
        return ResponseEntity.ok(vo);
    }
    
    @PostMapping("naturalPayPage")
    @ApiOperation(value = "正常付款记录分页查询", httpMethod = "POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销单号（模糊查询）", name = "paymoneyobjectcode", dataType = "String"),
            @ApiImplicitParam(value = "付款单号（模糊查询）", name = "paymoneycode", dataType = "String"),
            @ApiImplicitParam(value = "收款人（模糊查询）", name = "bankaccountname", dataType = "String"),
            @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
            @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
    public ResponseEntity<Page<BudgetPaymoney>> naturalPayPage(String paymoneyobjectcode, String paymoneycode, String bankaccountname, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("paymoneyobjectcode", paymoneyobjectcode);
        conditionMap.put("paymoneycode", paymoneycode);
        conditionMap.put("bankaccountname", bankaccountname);
        Page<BudgetPaymoney> vo = this.payMoneyService.getNaturalPayPage(page, rows, conditionMap);
        return ResponseEntity.ok(vo);
    }    
    
    @GetMapping("payHisPage")
    @ApiOperation(value = "付款历史分页查询", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "批次号（模糊查询）", name = "paybatchcode"),
            @ApiImplicitParam(value = "备注（模糊查询）", name = "remark"),
            @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
            @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
    public ResponseEntity<Page<BudgetPaybatch>> payHisPage(String paybatchcode, String remark, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception {
        Page<BudgetPaybatch> vo = this.payBatchService.getPayHis(page, rows, paybatchcode, remark);
        return ResponseEntity.ok(vo);
    } 
    
    @GetMapping("payDetails")
    @ApiOperation(value = "付款明细分页查询", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "付款批次id", name = "batchId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
            @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
    public ResponseEntity<Page<BudgetPaymoney>> payDetails(Long batchId, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception {
        if (null == batchId) {
            return ResponseEntity.error("batchId不能为空");
        }
        Page<BudgetPaymoney> vo = this.payBatchService.getPayMoneyByBatchId(page, rows, batchId);
        return ResponseEntity.ok(vo);
    } 
    
	@GetMapping("print")
	@ApiOperation(value = "打印", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "报销主键ids", name = "ids", dataType = "String", required = true)
	})
	public ResponseEntity<List<ReimbursementMainPrintVo>> print(@RequestParam(name="ids",required = true) String ids) throws Exception {
		List<ReimbursementMainPrintVo> voList = new ArrayList<>();
		Arrays.asList(ids.split(",")).forEach(id->{
			ReimbursementRequest result = this.service.printDetail(Long.valueOf(id));
			ReimbursementMainPrintVo vo = ReimbursementMainPrintVo.apply(result);
			voList.add(vo);
		});
		return ResponseEntity.ok(voList);
	}
	
	@GetMapping("printDetail")
	@ApiOperation(value = "打印详情", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "报销主键", name = "ids", dataType = "String", required = true)
	})
	public ResponseEntity<List<ReimbursementPrintVo>> printDetail(String ids) throws Exception {
		List<ReimbursementPrintVo> voList = new ArrayList<>();
		Arrays.asList(ids.split(",")).forEach(id->{
			ReimbursementRequest result = this.service.detail(Long.valueOf(id));
			ReimbursementPrintVo vo = ReimbursementPrintVo.apply(result);
			if(CollectionUtils.isEmpty(vo.getTransList()) && CollectionUtils.isEmpty(vo.getAllocatedList()) && CollectionUtils.isEmpty(vo.getCashList()) && CollectionUtils.isEmpty(vo.getPaymentList())) return;
			voList.add(vo);
		});
		return ResponseEntity.ok(voList);
	}
		
	/**
	 * 删除
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@GetMapping("delete")
	@ApiOperation(value = "删除", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "报销单主键", name = "id", dataType = "Long", required = true)
	})
	public ResponseEntity<String> delete(Long id) throws Exception{
		String result = this.worker.delete(id);
		if(StringUtils.isEmpty(result)) {
			return ResponseEntity.ok("删除成功！");
		}
		return ResponseEntity.error(result);
	}
	
	@GetMapping("before")
	@ApiOperation(value = "检查扫描页面设置（响应字段{result：true 当前环节为设置环节 false 当前环节非设置环节或未设置环节；message：报错信息；refresh：true 需要刷新列表 false 不需要；reimcode：报销单号，不为null需弹出对应报销单窗口}）", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销单流程环节【1：单据接收  2：预算审核 3：分单扫描 4：分单确认 5：出纳付款 6：会计做账 7：凭证审核 8：法人公司抽单 10：财务总监审核 11：总经理审核】", name = "step", dataType = "String", required = true)
	})
	public ResponseEntity<Map<String, Object>> before(String step) throws Exception{
		WbUser user = UserThreadLocal.get();
		String stepAndOpt = this.redis.get(this.bx_step_key + user.getUserName());
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("refresh", false);
		if(StringUtils.isNotBlank(stepAndOpt)) {
		    String message = "";
			String[] values = stepAndOpt.split(STEP_OPT_SPLIT);
            String setStep = values[0];
            String setOpt = values[1];
            if (!setStep.equals(step)) {
                message += "您已处于【" + ReimbursementStepHelper.getName(setStep) + "】环节，";
                message += "【" + ("1".equals(setOpt) ? "接收" : "审核") + "】模式";
                retMap.put("result", false);
                retMap.put("message", message);
            }else {
                retMap.put("result", true);
                String reimcode = this.redis.get(REDIS_BXDH + user.getUserName());
                if(StringUtils.isNotBlank(reimcode)) {//报销单号
                    retMap.put("refresh", true);
                    if ("2".equals(setOpt)) {
                        retMap.put("id", this.redis.get(REDIS_BXDID + user.getUserName()));
                        retMap.put("reimcode", reimcode);
                    }
                    this.redis.delete(REDIS_BXDH + user.getUserName());//删除
                    this.redis.delete(REDIS_BXDID + user.getUserName());
                }
            }
            retMap.put("message", message);
		}else {
		    retMap.put("result", false);
		    retMap.put("message", "扫描页面未设置");
		    
		}
		return ResponseEntity.ok(retMap);
	}
	
	@GetMapping("open")
	@ApiOperation(value = "设置扫码页面", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "审核或接收【1：接收 2：审核】", name = "opt", dataType = "String", required = true),
			@ApiImplicitParam(value = "报销单流程环节【1：单据接收  2：预算审核 3：分单扫描 4：分单确认 5：出纳付款 6：会计做账 7：凭证审核 8：法人公司抽单 9：财务经理审核 10：总经理审核】", name = "step", dataType = "String", required = true)
	})
	public ResponseEntity<String> open(String opt, String step) throws Exception{
		if(StringUtils.isBlank(opt) || StringUtils.isBlank(step)) {
			return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL);
		}
		WbUser user = UserThreadLocal.get();
		this.redis.set(this.bx_step_key + user.getUserName(), step + STEP_OPT_SPLIT + opt, this.bx_step_ttl);
		return ResponseEntity.ok("操作成功！");
	}
	
	@GetMapping("close")
	@ApiOperation(value = "关闭扫码页面", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "报销单流程环节【1：单据接收  2：预算审核 3：分单扫描 4：分单确认 5：出纳付款 6：会计做账 7：凭证审核 8：法人公司抽单 9：财务经理审核 10：总经理审核】", name = "step", dataType = "String", required = true)
	})
	public ResponseEntity<String> close(String step) throws Exception{
	    
		WbUser user = UserThreadLocal.get();
		String stepAndOpt = this.redis.get(this.bx_step_key + user.getUserName());  
        if (StringUtils.isNotBlank(stepAndOpt)) {
            String[] values = stepAndOpt.split(STEP_OPT_SPLIT);
            String setStep = values[0];
            String setOpt = values[1];
            if (setStep.equals(step)) {
                this.redis.delete(this.bx_step_key + user.getUserName());
            }
        }
		return ResponseEntity.ok("操作成功！");
	}

    
	/**
	 * 扫码
	 * 1.用户是否打开页面
	 * 2.当前环节是否已经被接收或审核
	 * 3.报销单是否存在
	 * 4.报销单是否提交
	 * 5.扫描版本与当前版本是否一致
	 * 6.分单环节是否已完成分单
	 * 7.做账环节是否是会计校验
	 * 8.做账会计是否是本公司的
	 * 9.结束内部流转校验
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@GetMapping("code")
	@NoLoginAnno
	public void code(HttpServletRequest request,HttpServletResponse response) throws Exception{
		BxCodeRequest codeRequest = null;
		boolean mobileDevice = DeviceTools.isMobileDevice(request);
		//是否企业微信
		boolean weChat = DeviceTools.iswxWork(request);
		if(mobileDevice && !weChat) {
			HtmlUtil.draw(HtmlUtil.html("报销审核", "扫码", "失败", "请使用企业微信或扫码枪扫码！！", FULL_FORMAT.format(new Date())), response);
			return;
			//throw new Exception("请使用企业微信或扫码枪扫码！！");
		}
		//从企业微信获取
		if(mobileDevice && weChat) {
			String code = request.getParameter("code");
			if(StringUtils.isEmpty(code)) {
				String _url = this.qywxDomain + RequestAnswerTool.getUrlSAndParameter(request);
				String redirect_uri = URLEncoder.encode(_url,"UTF-8");
				String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx08fc8ba1546d5bac&redirect_uri="+redirect_uri+"&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect";
				 response.sendRedirect(url);
				 return;
			}
			try {
				codeRequest = WeChatBxCodeRequest.getRequest(request, this.weChatService);
			} catch (Exception e) {
				HtmlUtil.draw(HtmlUtil.html("报销审核", "扫码", "失败", e.getMessage(), FULL_FORMAT.format(new Date())), response);
				e.printStackTrace();
				return;
			}
		}
		//从优库扫码枪获取
		if(codeRequest == null) {
			try {
				codeRequest = GunBxCodeRequest.getRequest(request);
			} catch (Exception e) {
				HtmlUtil.draw(HtmlUtil.html("报销审核", "扫码", "失败", e.getMessage(), FULL_FORMAT.format(new Date())), response);
				e.printStackTrace();
				return;
			}
		}
		if(codeRequest == null) {
			HtmlUtil.draw(HtmlUtil.html("报销审核", "扫码", "失败", "获取扫码信息失败！！", FULL_FORMAT.format(new Date())), response);
			return;
			//throw new Exception("");
		}
		//设置用户
		WbUser user = this.userService.getByEmpNo(codeRequest.getEmpNo());
		UserThreadLocal.set(user);
		//判断用户是否打开工作台
		String stepAndOpt = this.redis.get(this.bx_step_key + codeRequest.getEmpNo());
		if(StringUtils.isBlank(stepAndOpt)) {
			messageSender.sendQywxMsg(new QywxTextMsg(codeRequest.getEmpNo(), null, null, 0, "您未打开工作台界面！！", 0));
			HtmlUtil.draw(HtmlUtil.html("报销审核", "扫码", "失败", "您未打开工作台界面！！", FULL_FORMAT.format(new Date())), response);
			return;
		}
		//刷新redis
		this.redis.expire(this.bx_step_key + codeRequest.getEmpNo(), this.bx_step_ttl);
		//校验
		BudgetReimbursementorder order = this.service.getById(codeRequest.getOrderId());
		String result = this.flowWorker.flowValidate(codeRequest,stepAndOpt,order);
		if(StringUtils.isNotBlank(result)) {
			messageSender.sendQywxMsg(new QywxTextMsg(codeRequest.getEmpNo(), null, null, 0, result, 0));
			HtmlUtil.draw(HtmlUtil.html("报销审核", "扫码", "失败", result, FULL_FORMAT.format(new Date())), response);
			return;
		}
		
		String[] values = stepAndOpt.split(STEP_OPT_SPLIT);
        String setStep = ReimbursementStepHelper.getName(values[0]);
        //String setOpt = "1".equals(values[1]) ? "接收" : "审核";
        
        /**
         * add by minzhq 
         * 增加结束流转标志
         */
       if(values[0].equals(ReimbursementStepHelper.ACCOUNTING_DO_BILL) && !"1".equals(values[1])) {
        	//会计做账--结束流转
        	setStep = "会计做账--结束流转";
        }
        
        //数据入库
      	this.flowWorker.save(codeRequest,stepAndOpt,order);
        
		String content = "扫码成功！";
		content = "报销单【" + order.getReimcode() + "】-【" + setStep + "】" +"扫描成功。";
		messageSender.sendQywxMsg(new QywxTextMsg(codeRequest.getEmpNo(), null, null, 0, content, 0));
		this.redis.set(REDIS_BXDH + codeRequest.getEmpNo(), order.getReimcode());
		this.redis.set(REDIS_BXDID + codeRequest.getEmpNo(), order.getId().toString());
		HtmlUtil.draw(HtmlUtil.html("报销审核", setStep, "成功", content, FULL_FORMAT.format(new Date())), response);
		UserThreadLocal.remove();
		return;
	}
	   
    @GetMapping("updateLevel")
    @ApiOperation(value = "报销级别调整", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销单主键", name = "id", dataType = "Long", required = true),
            @ApiImplicitParam(value = "操作类型（up：加急；back：还原）", name = "type", dataType = "String", required = true)
    })
    public ResponseEntity<String> updateLevel(Long id, String type) throws Exception{    
        String message = this.service.updateRequestLevel(id, type);
        return ResponseEntity.ok(message);
    }
	
    @GetMapping("oneKeyCheck")
    @ApiOperation(value = "一键审核", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销单ids（多个,隔开）", name = "ids", dataType = "String", required = true)
    })
    public ResponseEntity<String> oneKeyCheck(String ids) throws Exception{
        this.service.oneKeyCheck(ids);
        return ResponseEntity.ok("操作成功！");
    }  
    
	@PostMapping("checkPass")
	@ApiOperation(value = "审核通过", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	public ResponseEntity<String> checkPass(@RequestBody @Valid CheckPassRequest checkPassRequest) throws Exception{
		String empNo = UserThreadLocal.get().getUserName();
		String stepAndOpt = this.redis.get(this.bx_step_key + empNo);
		String step = "";
		//是否是报销审核界面所做的操作
		boolean isBxVerify = false;
		if(StringUtils.isNotBlank(checkPassRequest.getOption())) {
		    step = checkPassRequest.getOption();
		    isBxVerify = true;
		}else {
	        if ((StringUtils.isBlank(stepAndOpt))) {
	            return ResponseEntity.error("您未打开扫码页面！！");
	        }else {
	            step = stepAndOpt.split(STEP_OPT_SPLIT)[0];
	        }
	        isBxVerify = false;
		}
		String message = this.service.checkPass(checkPassRequest.getOrderId(), step, checkPassRequest.getOrderDetail(), checkPassRequest.getOrderTrans(),checkPassRequest.getDeletedTranList(),isBxVerify);
		if(StringUtils.isNotEmpty(message)) {
			return ResponseEntity.error(message);
		}
		return ResponseEntity.ok("操作成功！");
	}
	
	@GetMapping("back")
	@ApiOperation(value = "退回", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "报销单主键", name = "orderId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "退回类型【1：退回纸质，2：全部退回】", name = "backType", dataType = "Integer", required = true),
			@ApiImplicitParam(value = "退单类型", name = "type",dataType = "String", required = true),
            @ApiImplicitParam(value = "退回说明", name = "remark", dataType = "String", required = false)
	})
	public ResponseEntity<String> back(Long orderId, String backType,String type, String remark) throws Exception{
		String empNo = UserThreadLocal.get().getUserName();
		String stepAndOpt = this.redis.get(this.bx_step_key + empNo);
		String step = "";
		if(StringUtils.isBlank(stepAndOpt)) {
		    step = "1";
		}else {
		    step = stepAndOpt.split(STEP_OPT_SPLIT)[0];
		}
		String message = this.service.back(orderId, step, backType, remark,type);
		if(StringUtils.isNotEmpty(message)) {
			return ResponseEntity.error(message);
		}
		return ResponseEntity.ok("操作成功！");
	}
	
	@GetMapping("splitOrder")
	@ApiOperation(value = "分单", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "报销单主键", name = "orderId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "主做账单位主键", name = "bunitid", dataType = "Long", required = true)
	})
	public ResponseEntity<String> splitOrder(Long orderId,Long bunitid) throws Exception{
		String empNo = UserThreadLocal.get().getUserName();
		String stepAndOpt = this.redis.get(this.bx_step_key + empNo);
		String message = this.service.splitOrder(orderId,bunitid,stepAndOpt.split(STEP_OPT_SPLIT)[0]);
		if(StringUtils.isNotEmpty(message)) {
			return ResponseEntity.error(message);
		}
		return ResponseEntity.ok("操作成功！");
	}
	
	
	@PostMapping("doAccount")
	@ApiOperation(value = "完成做账", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	public ResponseEntity<String> doAccount(@RequestBody @Valid CheckPassRequest checkPassRequest) throws Exception{

		String message = null;
		String lockKey = "/finance-platform/budgetReimbursement/doAccount/"+checkPassRequest.getOrderId();
		ZookeeperShareLock zookeeperShareLock = new ZookeeperShareLock(this.curatorFramework, lockKey, null);
		try {
			zookeeperShareLock.tryLock();
			message = this.service.doAccount(checkPassRequest);
		}catch(Exception e) {
			e.printStackTrace();
			message = StringUtils.isBlank(e.getMessage())?"空指针":e.getMessage();
		}finally {
			zookeeperShareLock.unLock();
		}
		if(StringUtils.isNotEmpty(message)) {
			return ResponseEntity.error(message);
		}
		return ResponseEntity.ok("操作成功！");
	}	
    
    @GetMapping("getReimcodePage")
    @ApiOperation(value = "获取报销单号（reimcode：报销单号；query：下拉框文本）", httpMethod = "GET")
    @ApiImplicitParams(value = {
           @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
           @ApiImplicitParam(value = "查询条件（报销单号/报销人/报销金额）", name = "condition", dataType = "String"),
           @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
           @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
    public ResponseEntity<Page<Map<String, String>>> getReimcodePage(String condition, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception { 
        Page<Map<String, String>> vo = this.payMoneyService.getReimcodePage(page, rows, condition);
        return ResponseEntity.ok(vo);
    }

    @GetMapping("getTccodePage")
    @ApiOperation(value = "获取付款失败修改单号", httpMethod = "GET")
    @ApiImplicitParams(value = {
           @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
           @ApiImplicitParam(value = "查询条件（付款失败修改单号）", name = "condition", dataType = "String"),
           @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
           @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
    public ResponseEntity<Page<String>> getTccodePage(String condition, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception { 
        Page<String> vo = this.payMoneyService.getTccodePage(page, rows, condition);
        return ResponseEntity.ok(vo);
    }   
  
    @GetMapping("getXmcodePage")
    @ApiOperation(value = "获取项目编号（xmcode：项目编号；query：下拉框文本）", httpMethod = "GET")
    @ApiImplicitParams(value = {
           @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
           @ApiImplicitParam(value = "查询条件（项目名称/项目编号）", name = "condition", dataType = "String"),
           @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
           @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
    public ResponseEntity<Page<Map<String, String>>> getXmcodePage(String condition, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception { 
        Page<Map<String, String>> vo = this.payMoneyService.getXmcodePage(page, rows, condition);
        return ResponseEntity.ok(vo);
    }
   
    @GetMapping("queryCanPay")
    @ApiOperation(value = "查询可付款记录（可按报销单类型或付款方式）", httpMethod = "GET")
    @ApiImplicitParams(value = {
           @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
           @ApiImplicitParam(value = "付款单类型（1：报销；2：付款失败修改；6：项目）", name = "paymoneytype", dataType = "Integer"),
           @ApiImplicitParam(value = "付款方式（0：现金；1：转账）", name = "paytype", dataType = "Integer"),
           @ApiImplicitParam(value = "（报销/付款失败修改/项目）单据号", name = "objectcode", dataType = "String"),
           @ApiImplicitParam(value = "收款人名称（模糊查询）", name =  "bankaccountname", dataType = "String"),
           @ApiImplicitParam(value = "按其他方式查询必送（仅查ids中包含的id）", name = "ids", dataType = "String"),
    })
    public ResponseEntity<List<BudgetPaymoney>> queryCanPay(Integer paymoneytype, Integer paytype, String objectcode, String bankaccountname, String ids) throws Exception {
        List<BudgetPaymoney> result = new ArrayList<>();
        if(null != paymoneytype && null != paytype) {
            result = this.payMoneyService.getCanPayMoneyByPmtype(paymoneytype, objectcode,paytype); 
        }else if(null == paymoneytype && null != paytype) {
            result = this.payMoneyService.getCanPayMoneyByFkType(paytype, objectcode, bankaccountname, ids);        
        }
        return ResponseEntity.ok(result);
    }   
    
    @GetMapping("otherAddQuery")
    @ApiOperation(value = "待添加付款列表查询", httpMethod = "GET")
    @ApiImplicitParams(value = {
           @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
           @ApiImplicitParam(value = "单据号（模糊查询）", name = "paymoneyobjectcode", dataType = "String"),
           @ApiImplicitParam(value = "付款单类型（1：报销；2：付款失败修改；3：借款；5：项目现金；6：项目转账）", name = "paymoneytype", dataType = "Integer"),
           @ApiImplicitParam(value = "付款单位名称（模糊查询）", name = "bunitname", dataType = "String"),
           @ApiImplicitParam(value = "收款银行（模糊查询）", name = "bankaccountbranchname", dataType = "String"),
           @ApiImplicitParam(value = "收款人名称（模糊查询）", name = "bankaccountname", dataType = "String"),
           @ApiImplicitParam(value = "已添加的付款单id（多个，隔开）", name = "ids", dataType = "String"),
           @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
           @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false),
		    @ApiImplicitParam(value = "来源类型(1:预算系统，2：oa系统)", name = "sourceType", dataType = "Integer", required = true)
    })
    public ResponseEntity<Page<BudgetPaymoney>> otherAddQuery(@RequestParam(value = "paymoneyobjectcode",required = false) String paymoneyobjectcode,
                                                              @RequestParam(value = "paymoneytype",required = false)Integer paymoneytype,
                                                              @RequestParam(value = "bunitname",required = false)String bunitname,
                                                              @RequestParam(value = "bankaccountbranchname",required = false)String bankaccountbranchname,
                                                              @RequestParam(value = "bankaccountname",required = false)String bankaccountname,
                                                              @RequestParam(value = "ids",required = false)String ids,
                                                              @RequestParam(value = "sourceType",required = true)Integer sourceType, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception {
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("paymoneyobjectcode", paymoneyobjectcode);
        conditionMap.put("paymoneytype", paymoneytype);
        conditionMap.put("bunitname", bunitname);
        conditionMap.put("bankaccountbranchname", bankaccountbranchname);
        conditionMap.put("bankaccountname", bankaccountname);
        conditionMap.put("ids", ids);
        conditionMap.put("sourceType", sourceType);
        Page<BudgetPaymoney> result = this.payMoneyService.otherAddQuery(page, rows, conditionMap);
        return ResponseEntity.ok(result);
    }
    
	@GetMapping("preparePay")
	@ApiOperation(value = "准备付款", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "付款方式【0：其它 1：报销 2：付款失败修改3：现金 4:项目付款】", name = "payBatchType", dataType = "String", required = true),
			@ApiImplicitParam(value = "以逗号分割的付款单主键", name = "payids", dataType = "String", required = true),
			@ApiImplicitParam(value = "备注", name = "remark", dataType = "String", required = false)
	})
	public ResponseEntity<String> preparePay(String payBatchType,String remark,String payids) throws Exception{
		String message = this.payBatchService.preparePay(payBatchType,remark,payids);
		if(StringUtils.isNotEmpty(message)) {
			return ResponseEntity.error(message);
		}
		return ResponseEntity.ok("操作成功！");
	}	
	
    @ApiOperation(value = "导出准备付款明细表（准备付款成功后）",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "以逗号分割的付款单主键", name = "payids", dataType = "String"),
            @ApiImplicitParam(value = "付款批次号主键", name = "paybatchid", dataType = "Long")
    })
    @GetMapping("/exportPreparePay")
    public void exportPreparePay(@RequestParam(name="payids", defaultValue = "")String payids, @RequestParam(name="paybatchid", defaultValue = "")Long paybatchid, HttpServletResponse response) throws Exception{
        this.service.exportPreparePay(payids, paybatchid, response);
    }	
    @GetMapping("setPayFail")
    @ApiOperation(value = "设置付款失败", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "付款单主键", name = "payId", dataType = "Long", required = true),
            @ApiImplicitParam(value = "失败原因", name = "verifyremark", dataType = "String", required = true)
    })
    public ResponseEntity<String> setPayFail(Long payId, String verifyremark, HttpServletRequest request) throws Exception{
        if (null == request) {
            return null;
        }
        String path = request.getContextPath();
        String basePath = request.getScheme() + "://" + request.getServerName();
        if (80 != request.getServerPort()) {
            basePath = basePath + ":" + request.getServerPort();
        }
        basePath = basePath + path + "/";
        String message = this.payMoneyService.exceptionPay(payId, verifyremark, basePath);
        if(StringUtils.isNotEmpty(message)) {
            return ResponseEntity.error(message);
        }
        return ResponseEntity.ok("操作成功！");
    }
	
    @GetMapping("backPayFail")
    @ApiOperation(value = "还原付款失败", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "付款单主键", name = "payId", dataType = "Long", required = true)
    })
    public ResponseEntity<String> backPayFail(Long payId) throws Exception{
        String message = this.payMoneyService.reexceptionPay(payId);
        if(StringUtils.isNotEmpty(message)) {
            return ResponseEntity.error(message);
        }
        return ResponseEntity.ok("操作成功！");
    }
    
    @GetMapping("getTravelEmp")
    @ApiOperation(value = "分页查询出差人员", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "名称或工号模糊查询", name = "queryText", dataType = "String"),
            @ApiImplicitParam(value = "页码", name = "page", dataType = "Integer", required = false),
            @ApiImplicitParam(value = "每页条数", name = "rows", dataType = "Integer", required = false)
    })
    public ResponseEntity<Page<Map<String, Object>>> getTravelEmp(String queryText, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = Constants.ROWS)Integer rows) throws Exception{
        TabDm dm = this.dmService.getOne(new QueryWrapper<TabDm>().eq("dm_type", "TRAVELERQUERY").eq("dm", "date"));
        String date;
        if (null == dm) {//默认查询31天，可配置
            date = "date_sub(NOW(),interval 31 day)";
        }else {
            date = dm.getDmValue();
        }

        Page<WbUser> pageCond = new Page<>(page,rows);
	    Page<WbUser> pageResult = userService.page(pageCond, new LambdaQueryWrapper<WbUser>().apply("1=1").and(StringUtils.isNotBlank(queryText), qw -> {
		    qw.like(WbUser::getUserName, queryText).or().like(WbUser::getDisplayName, queryText);
	    }));
		/**
		 * 2022-06-14 出差人员不从hr获取。
		 */
	    List<Map<String, Object>> records = pageResult.getRecords().stream().map(user -> {
		    Map<String, Object> map = new HashMap<>();
		    map.put("empId", user.getUserId());
		    map.put("empNo", user.getUserName());
		    map.put("empName", user.getDisplayName());
		    return map;
	    }).collect(Collectors.toList());

	    Page<Map<String, Object>> p = new Page<Map<String, Object>>();
	    p.setTotal(pageResult.getTotal());
	    p.setRecords(records);
	    //ResponseEntity.ok(this.hrService.getTravelEmpPage(page, rows, date, queryText));
	    return ResponseEntity.ok(p);
    }
    
    @GetMapping("getQrCode")
    @ApiOperation(value = "根据报销单号查询二维码", httpMethod = "GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "报销单号（模糊查询）", name = "reimcode", dataType = "String", required = true)
    })
    public ResponseEntity<List<Map<String, Object>>> getQrCode(String reimcode) throws Exception{
        List<Map<String, Object>> list = this.service.getQrCodeByReimcode(reimcode);
        return ResponseEntity.ok(list);
    }    

    @ApiOperation(value = "下载付款失败修改导入模板",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/downPayErrorImportTemplate")
    public void downPayErrorImportTemplate(HttpServletResponse response) throws Exception {  
        InputStream is = null;
        try {//String templateName = File.separator+"payErrorImportTemplate.xlsx";
            is = this.getClass().getClassLoader().getResourceAsStream("template/payErrorImportTemplate.xlsx");
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("付款失败修改导入模板", response)).withTemplate(is).build();
            WriteSheet sheet = EasyExcel.writerSheet(0).build();            
            List<Map<String,Object>> list = new ArrayList<>();
            workBook.fill(list, sheet);
            workBook.finish();
            
       } catch (Exception e) {
           e.printStackTrace();
           throw e;
       }finally {
           if(is!=null) is.close();
       }
    }
    
    @ApiOperation(value = "导入付款失败修改",httpMethod="POST")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @PostMapping("/importPayError")
    public ResponseEntity importPayError(@RequestParam(name="file") MultipartFile file,HttpServletResponse response,HttpServletRequest request) throws IOException { 
        
        //MultipartHttpServletRequest multipartRequest = multipartResolver.resolveMultipart(request);       
        //List<MultipartFile> files = multipartRequest.getFiles("file");
        //MultipartFile file = files.get(0);
        InputStream is = null;
        int headRows = 1; //表示表头有2行
        EasyExcelImportListener listener = new EasyExcelImportListener(payMoneyService,PEIMPORT,headRows,9);
        try {
            is = file.getInputStream();
            EasyExcel.read(is, listener).sheet(0).doReadSync();
        } catch (IOException e1) {          
            e1.printStackTrace();
            return ResponseEntity.error(e1.getMessage());
        }finally {
            try {
                is.close();
            } catch (IOException e) {               
                e.printStackTrace();
                return ResponseEntity.error(e.getMessage());
            }
        }
        //明细数据的错误明细
        Map<Integer, Map<Integer, String>> errorMap = listener.getErrorMap();
        //导入的所有的数据
        Map<Integer, Map<Integer, String>> allDataMap = listener.getAllDataMap();
        if(!errorMap.isEmpty()) {
            
            List<PayErrorImportExcelData> details = populateData(allDataMap,errorMap);
            
            InputStream iss = null;
            try {
                iss = this.getClass().getClassLoader().getResourceAsStream("template/payErrorImportTemplate.xlsx");
                String key = PEIMPORT +"_" + UserThreadLocal.get().getUserName();
                String errorFileName = fileShareDir + File.separator + System.currentTimeMillis()+"_错误信息.xlsx";
                ExcelWriter workBook = EasyExcel.write(new File(errorFileName),ExtractInfoExportExcelData.class).withTemplate(iss).build();
                WriteSheet sheet = EasyExcel.writerSheet(0).build();
                sheet.setSheetName("付款失败修改导入错误明细");
                workBook.fill(details, sheet);
                workBook.finish();
                this.redis.set(key, errorFileName,expiretime);
           } catch (Exception e) {
               e.printStackTrace();                     
           }finally {              
               if(iss!=null) iss.close();
           }            
            return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT,"文件导入有错误,请点击此处下载");
        }
        return ResponseEntity.ok("导入成功");
    }   
    
    @ApiOperation(value = "下载导入付款失败修改的错误明细",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/downImporPayErrorDetail")
    public void downImporPayErrorDetail(HttpServletResponse response,HttpServletRequest request) throws Exception {
        
        InputStream is = null;
        try {
            if(redis.get(PEIMPORT+ "_" + UserThreadLocal.get().getUserName()) ==null) {
                throw new RuntimeException("没有付款失败修改错误明细可供下载。");
            }
            String errorFileName = redis.get(PEIMPORT+ "_" + UserThreadLocal.get().getUserName());               
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("付款失败修改导入错误明细", response)).withTemplate(is).build();
            WriteSheet sheet = EasyExcel.writerSheet(0).build();            
            workBook.finish();  
            File file = new File(errorFileName);
            if(file.exists()) file.delete();
            redis.delete(PEIMPORT+ "_" + UserThreadLocal.get().getUserName());
       } catch (Exception e) {
           e.printStackTrace();
           throw e;
       }finally {
           if(is!=null) is.close();
       } 
    }
    

    @ApiOperation(value = "下载付款验证导入模板（传入batchId可导出对应批次号的付款单明细）",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "付款批次id", name = "batchId", dataType = "Long")
    })
    @GetMapping("/downPayVerifyImportTemplate")
    public void downPayVerifyImportTemplate(@RequestParam(name = "batchId", defaultValue = "")Long batchId, HttpServletResponse response) throws Exception {  
        InputStream is = null;
        try {//String templateName = File.separator+"payErrorImportTemplate.xlsx";
            is = this.getClass().getClassLoader().getResourceAsStream("template/payVerifyExportTemplate.xlsx");
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("付款验证导入模板", response),PayVerifyExcelData.class).withTemplate(is).build();
            WriteSheet sheet = EasyExcel.writerSheet(0).build();            
            List<PayVerifyExcelData> list = new ArrayList<>();
            if (null != batchId) {
                List<BudgetPaymoney> paymoneys = this.payMoneyService.list(new QueryWrapper<BudgetPaymoney>().eq("paybatchid", batchId));
                if (null != paymoneys && !paymoneys.isEmpty()) {
                    for (BudgetPaymoney payMoney :paymoneys) {
                        PayVerifyExcelData excelData = new PayVerifyExcelData();
                        BeanUtils.copyProperties(payMoney, excelData);
                        excelData.setPaymoney(payMoney.getPaymoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        list.add(excelData);;
                    }
                }
            }
            workBook.fill(list, sheet);
            workBook.finish();
            
       } catch (Exception e) {
           e.printStackTrace();
           throw e;
       }finally {
           if(is!=null) is.close();
       }
    }    

    @ApiOperation(value = "导入付款验证")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "付款批次id", name = "batchId", dataType = "Long", required = true)
    })
    @PostMapping("/importPayVerify")
    public ResponseEntity<String> importPayVerify(@RequestParam("batchId")Long batchId, @RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
     // 文件后缀名判断
        String fileExtension = EasyExcelUtil.getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
        if (!"xls".equals(fileExtension) && !"xlsx".equals(fileExtension)) {
            return ResponseEntity.apply(StatusCodeEnmus.OTHER, "导入失败!只支持导入excel文件!");
        }
        List<PayVerifyExcelData> errorList = new ArrayList<>();
        int success = this.payBatchService.importVerify(file.getInputStream(), batchId, errorList);
        
        if (!errorList.isEmpty()) {
            InputStream iss = null;
            try {
                iss = this.getClass().getClassLoader().getResourceAsStream("template/payVerifyExportTemplate.xlsx");
                String key = PVIMPORT +"_" + UserThreadLocal.get().getUserName();
                String errorFileName = fileShareDir + File.separator + System.currentTimeMillis()+"_错误信息.xlsx";
                ExcelWriter workBook = EasyExcel.write(new File(errorFileName),PayVerifyExcelData.class).withTemplate(iss).build();
                WriteSheet sheet = EasyExcel.writerSheet(0).build();
                sheet.setSheetName("错误明细");
                workBook.fill(errorList, sheet);
                workBook.finish();
                this.redis.set(key, errorFileName,expiretime);
                return ResponseEntity.apply(StatusCodeEnmus.ERROR_FORMAT,"文件导入有错误,请点击此处下载");
           } catch (Exception e) {
               e.printStackTrace();                     
           }finally {              
               if(iss!=null) iss.close();
           }  
        }
        return ResponseEntity.ok("导入成功");
    }
    
    @ApiOperation(value = "下载导入付款验证的错误明细",httpMethod="GET")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
    })
    @GetMapping("/downImporPayVerifyDetail")
    public void downImporPayVerifyDetail(HttpServletResponse response,HttpServletRequest request) throws Exception {
        
        InputStream is = null;
        try {
            if(redis.get(PVIMPORT+ "_" + UserThreadLocal.get().getUserName()) ==null) {
                throw new RuntimeException("没有付款验证错误明细可供下载。");
            }
            String errorFileName = redis.get(PVIMPORT+ "_" + UserThreadLocal.get().getUserName());               
            is = new FileInputStream(errorFileName);
            ExcelWriter workBook = EasyExcel.write(EasyExcelUtil.getOutputStream("付款验证导入错误明细", response)).withTemplate(is).build();
            WriteSheet sheet = EasyExcel.writerSheet(0).build();            
            workBook.finish();  
            File file = new File(errorFileName);
            if(file.exists()) file.delete();
            redis.delete(PVIMPORT+ "_" + UserThreadLocal.get().getUserName());
       } catch (Exception e) {
           e.printStackTrace();
           throw e;
       }finally {
           if(is!=null) is.close();
       } 
    }
   
    
    /**
     * 填充导入付款失败修改错误的数据
     * @param details
     * @param allDataMap
     * @param headErrorMsg
     * @param errorMap
     */
    private List<PayErrorImportExcelData> populateData(Map<Integer, Map<Integer, String>> allDataMap, Map<Integer, Map<Integer, String>> errorMap) {
        List<PayErrorImportExcelData> details = new ArrayList<>();
        errorMap.forEach((i,data)->{
            PayErrorImportExcelData ed = new PayErrorImportExcelData(data);
            details.add(ed);
        });
        return details;
    }

    @GetMapping("/backType")
    @ApiOperation(value = "获取退回类型", httpMethod = "GET")
    @NoLoginAnno
    public ResponseEntity<List<String>> backType(){
        List<String> backTypes = this.service.listBackType();
        return ResponseEntity.ok(backTypes);
    }

    @ApiOperation(value = "导出预算员报销",httpMethod="GET")
    @GetMapping("/exportExpense")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
            @ApiImplicitParam(value = "是否预算员", name = "budgeterflag", dataType = "Boolean", required = true),
            @ApiImplicitParam(value = "报销单号", name = "reimcode", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销单状态", name = "reuqeststatus", dataType = "Long", required = false),
            @ApiImplicitParam(value = "界别", name = "yearid", dataType = "Long", required = false),
            @ApiImplicitParam(value = "月份", name = "monthid", dataType = "Long", required = false),
            @ApiImplicitParam(value = "预算单位名称（模糊查询）", name = "ysdw", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销人（模糊查询）", name = "bxr", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销日期（yyyy-mm-dd）", name = "bxrq", dataType = "String", required = false),
            @ApiImplicitParam(value = "报销金额", name = "bxje", dataType = "Double", required = false),
            @ApiImplicitParam(value = "冲账金额", name = "czje", dataType = "Double", required = false),
            @ApiImplicitParam(value = "转账金额", name = "zzje", dataType = "Double", required = false),
            @ApiImplicitParam(value = "现金金额", name = "xjje", dataType = "Double", required = false),
            @ApiImplicitParam(value = "划拨金额", name = "hbje", dataType = "Double", required = false),
            @ApiImplicitParam(value = "其他金额", name = "othermoney", dataType = "Double", required = false),
            @ApiImplicitParam(value = "报销类型，多个用逗号分隔,1：通用，2:差旅报销，3:招待报销,4:差旅补贴,5:推广招待", name = "bxType", dataType = "String", required = false),
            @ApiImplicitParam(value = "提交日期（yyyy-mm-dd）", name = "submittime", dataType = "String", required = false),
            @ApiImplicitParam(value = "申请日期（yyyy-mm-dd）", name = "applicanttime", dataType = "String", required = false)
    })
    @ApiDataAuthAnno
    public void exportExpense(Boolean budgeterflag, String reimcode, Integer reuqeststatus, Integer yearid, Integer monthid, String ysdw,
                              String bxr, String bxrq, Double bxje, Double czje, Double zzje, Double xjje, Double hbje, Double othermoney,
                              String bxType,String submittime, String applicanttime,HttpServletResponse response) throws Exception {
        List<Long> bxTypes = splitStringToLong(bxType);
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("reuqeststatus", reuqeststatus);
        conditionMap.put("reimcode", reimcode);
        conditionMap.put("yearid", yearid);
        conditionMap.put("monthid", monthid);
        conditionMap.put("bxr", bxr);
        conditionMap.put("bxrq", bxrq);
        conditionMap.put("bxje", bxje);
        conditionMap.put("czje", czje);
        conditionMap.put("zzje", zzje);
        conditionMap.put("xjje", xjje);
        conditionMap.put("hbje", hbje);
        conditionMap.put("othermoney", othermoney);
        conditionMap.put("ysdw", ysdw);
        conditionMap.put("submittime", submittime);
        conditionMap.put("applicanttime", applicanttime);
        conditionMap.put("bxType",bxTypes);
        String authSql = "";
        if (budgeterflag) {
            //增加权限控制
            authSql = JdbcSqlThreadLocal.get();
            //List<String> baseUnitIdList = unitService.getBaseUnitIdListByAuthCenter(authSql);
            //conditionMap.put("baseUnitIdList", baseUnitIdList);0047AO5FU3YDE
            conditionMap.put("managers", UserThreadLocal.get().getUserId());
            //conditionMap.put("managers","0047AO5FU3YDE");
        }else {
            conditionMap.put("applicantid", UserThreadLocal.get().getUserId());
        }
        //List<ExpenseInfoVO> vos = this.service.getExpenseInfo(conditionMap,authSql);
        this.service.exportExpense(conditionMap,authSql,response);
    }

    /*@ApiDataAuthAnno
    @ApiOperation(value = "导出预算员报销22",httpMethod="GET")
    @GetMapping("/exportExpense2")
    public void exportExpense2(ReimBursementDTO dto,HttpServletResponse response){
        this.service.exportExpense2(dto, response);
    }*/


    public static void main(String[] args) throws Exception {
		QRCodeTool.codeSave2Path("http://systest.jtyjy.com/klc/api/reimbursement/code?c=123-0", "D:\\Users\\User\\Desktop\\code.png");
	}
}
