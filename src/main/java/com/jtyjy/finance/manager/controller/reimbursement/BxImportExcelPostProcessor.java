package com.jtyjy.finance.manager.controller.reimbursement;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.iamxiongx.util.message.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.jtyjy.finance.manager.bean.BudgetBillingUnit;
import com.jtyjy.finance.manager.bean.BudgetLendmoney;
import com.jtyjy.finance.manager.bean.BudgetMonthAgent;
import com.jtyjy.finance.manager.bean.BudgetReimbursementorder;
import com.jtyjy.finance.manager.bean.BudgetSubject;
import com.jtyjy.finance.manager.bean.BudgetUnit;
import com.jtyjy.finance.manager.bean.BudgetYearPeriod;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.finance.manager.bean.WbDept;
import com.jtyjy.finance.manager.bean.WbPerson;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.dto.bxExcel.BxDetailDto;
import com.jtyjy.finance.manager.dto.bxExcel.BxInfoSheetDto;
import com.jtyjy.finance.manager.dto.bxExcel.EntertainDetailDto;
import com.jtyjy.finance.manager.dto.bxExcel.HbDetailDto;
import com.jtyjy.finance.manager.dto.bxExcel.StrickDetailDto;
import com.jtyjy.finance.manager.dto.bxExcel.TransferDetailDto;
import com.jtyjy.finance.manager.dto.bxExcel.TravelDetailDto;
import com.jtyjy.finance.manager.dto.bxExcel.TravelSheetDto;
import com.jtyjy.finance.manager.enmus.ReimbursementTypeEnmu;
import com.jtyjy.finance.manager.mapper.BudgetUnitMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.mapper.WbDeptMapper;
import com.jtyjy.finance.manager.mapper.WbPersonMapper;
import com.jtyjy.finance.manager.mapper.WbUserMapper;
import com.jtyjy.finance.manager.mapper.response.MonthAgentMoneyInfo;
import com.jtyjy.finance.manager.service.BudgetBankAccountService;
import com.jtyjy.finance.manager.service.BudgetBillingUnitAccountService;
import com.jtyjy.finance.manager.service.BudgetBillingUnitService;
import com.jtyjy.finance.manager.service.BudgetLendmoneyService;
import com.jtyjy.finance.manager.service.BudgetMonthAgentService;
import com.jtyjy.finance.manager.service.BudgetReimbursementorderService;
import com.jtyjy.finance.manager.service.BudgetSubjectService;
import com.jtyjy.finance.manager.service.TabDmService;
import com.jtyjy.finance.manager.vo.BankAccountVO;
import com.jtyjy.finance.manager.vo.BillingUnitAccountVO;
import com.klcwqy.easyexcel.imported.ExcelImportHelper;
import com.klcwqy.easyexcel.processor.ImportPostProcessor;

/**
 * ??????????????????????????????
 * @author User
 *
 */
@Component
public class BxImportExcelPostProcessor implements ImportPostProcessor{

    public static final String BX_SHEET = "????????????";
    public static final String CZ_SHEET = "????????????";
    public static final String ZZ_SHEET = "????????????";
    public static final String CL_SHEET = "????????????";
    public static final String ZD_SHEET = "????????????";
    public static final String HB_SHEET = "????????????";
    private Long yearId;
    private Long monthId;
    private Long unitId;
    private Integer bxType;
	@Autowired
	private BudgetYearPeriodMapper yearMapper;
	
	@Autowired
	private BudgetUnitMapper unitMapper;
	
	@Autowired
	private WbDeptMapper deptMapper;
	
	@Autowired
	private WbUserMapper userMapper;
	
	@Autowired
	private WbPersonMapper personMapper;
	
	@Autowired
    private TabDmService dmService;
	
	@Autowired
    private BudgetBillingUnitService billingUnitService;
	
	@Autowired
	private BudgetBillingUnitAccountService billingUnitAccountService;
	
	@Autowired
    private BudgetSubjectService subjectService;
    
	@Autowired
    private BudgetLendmoneyService lendMoneyService;
	
    @Autowired
    private BudgetBankAccountService bankAccountService;
	
	@Autowired
	private BudgetMonthAgentService monthAgentService;
	
    @Autowired
    private BudgetReimbursementorderService orderService;

	@Override
	public void instanceProcess(ExcelImportHelper helper, Class<?> arg1, Row row, Map<String, Object> headMap, Object obj)
			throws Exception {
		if(headMap.get("headError")!=null && (Boolean)headMap.get("headError")) return;
		String sheetName = row.getSheet().getSheetName();
		try {
		    if (BX_SHEET.equals(sheetName)) {
		        BxInfoSheetDto head = JSONObject.parseObject(JSONObject.toJSONString(headMap),BxInfoSheetDto.class);
		        BxDetailDto detail = (BxDetailDto)obj;
		        String errorInfo = BaseController.validate(detail);
		        if(StringUtils.isNotBlank(errorInfo)) throw new RuntimeException(errorInfo);
                
		        if (!"???".equals(detail.getInclude().trim()) && !"???".equals(detail.getInclude().trim())) throw new Exception("????????????????????????/??????");
                
		        BudgetBillingUnit unitInfo = this.billingUnitService.getOne(new QueryWrapper<BudgetBillingUnit>().eq("name", detail.getUnitName()));
		        if (null == unitInfo) throw new Exception("???????????????"+ detail.getUnitName() +"???????????????");
		        detail.setUnitId(unitInfo.getId());
		        BudgetSubject subjectInfo = this.subjectService.getOne(new QueryWrapper<BudgetSubject>().eq("yearid", this.yearId).eq("name", detail.getSubjectName()));
		        if (null == subjectInfo) throw new Exception("???????????????"+ detail.getSubjectName() +"???????????????");
		        BudgetMonthAgent monthAgent =  this.monthAgentService.getOne(new QueryWrapper<BudgetMonthAgent>().eq("unitid", unitId).eq("monthid", monthId).eq("subjectid", subjectInfo.getId()).eq("name", detail.getAgentName()));
		        if (null == monthAgent) throw new Exception("?????????"+ detail.getAgentName() +"???????????????");
		        MonthAgentMoneyInfo monthAgentMoneyInfo = new MonthAgentMoneyInfo();
		        monthAgentMoneyInfo.setMonthAgentId(monthAgent.getId());
		        monthAgentMoneyInfo.setMonthId(this.monthId);
		        monthAgentMoneyInfo.setUnitId(this.unitId);
		        monthAgentMoneyInfo.setYearId(this.yearId);
		        monthAgentMoneyInfo = this.monthAgentService.getUnitMonthAgentInfo(monthAgentMoneyInfo);
		        monthAgentMoneyInfo.setMonthAgentId(monthAgent.getId());
		        detail.setAgentMoneyInfo(monthAgentMoneyInfo);
		        
		    }else if(CZ_SHEET.equals(sheetName)) {
		        StrickDetailDto detail = (StrickDetailDto)obj;
		        StringBuilder errorInfo = new StringBuilder(); 
		        int errorNum = BaseController.validate(detail, errorInfo);
		        if(errorNum == 3) {
		            //???????????????????????????
		            return;
		        }
		        if(errorNum > 0) throw new RuntimeException(errorInfo.toString());
                
		        BudgetLendmoney lendMoney = this.lendMoneyService.getOne(new QueryWrapper<BudgetLendmoney>().eq("lendmoneycode", detail.getJkCode()).eq("empname", detail.getJkName()));
		        if (null == lendMoney) throw new Exception("???????????????????????????");
		        detail.setLendMoneyInfo(lendMoney);
		    }else if(ZZ_SHEET.equals(sheetName)) {
                TransferDetailDto detail = (TransferDetailDto)obj;
                StringBuilder errorInfo = new StringBuilder(); 
                int errorNum = BaseController.validate(detail, errorInfo);
                if(errorNum == 4) {
                    //???????????????????????????
                    return;
                }
                if(errorNum > 0) throw new RuntimeException(errorInfo.toString());
                String unitName = detail.getUnit().replace("???", "(").replace("???", ")");
                
                BankAccountVO bankAccount = this.bankAccountService.getBankAccountByAccount(detail.getAccount(), detail.getName());
                if (null == bankAccount) throw new Exception("???????????????????????????");
                detail.setBankAccountInfo(bankAccount);
                BudgetBillingUnit unitInfo = this.billingUnitService.getOne(new QueryWrapper<BudgetBillingUnit>().eq("name", unitName));
                if (null == unitInfo) throw new Exception("???????????????"+ unitName +"???????????????");
                Page<BillingUnitAccountVO> accountPage = this.billingUnitAccountService.getUnitAccountPageList(unitInfo.getId().toString(), 0, null, 1, 20);
                if (null == accountPage || 0l == accountPage.getTotal()) throw new Exception("???????????????"+ unitName +"???????????????????????????????????????");
                detail.setUnitAccountInfo(accountPage.getRecords().get(0));
		    }else if(CL_SHEET.equals(sheetName)) {
		        if (ReimbursementTypeEnmu.TRAVAL.getCode().equals(this.bxType) || ReimbursementTypeEnmu.TRAVALSUBSIDIES.getCode().equals(this.bxType) ) {
		            TravelDetailDto detail = (TravelDetailDto)obj;
	                StringBuilder errorInfo = new StringBuilder(); 
	                int errorNum = BaseController.validate(detail, errorInfo);
	                if(errorNum > 0) throw new RuntimeException(errorInfo.toString());
	                
	                TabDm vehicleDm = this.dmService.getOne(new QueryWrapper<TabDm>().eq("dm_name", detail.getGj()).eq("dm_type", "vehicle"));
	                if (null == vehicleDm) throw new RuntimeException("???????????????" + detail.getGj() +"????????????");
	                detail.setVehicleType(Integer.valueOf(vehicleDm.getDmValue()));
	                Date startDate, endDate;
	                try {
	                	if(detail.getStart().length() != 19 || detail.getEnd().length() != 19) throw new RuntimeException("error");
	                    startDate = Constants.FULL_FORMAT2.parse(detail.getStart());
	                    endDate = Constants.FULL_FORMAT2.parse(detail.getStart());
	                }catch (ParseException dateParseE){
	                    throw new RuntimeException("????????????????????????????????????????????????(???2021/07/15 09:10:20)???");
	                }
	                if (startDate.getTime() > endDate.getTime()) throw new RuntimeException("???????????????????????????????????????");
                    
		        
		        } 
            }else if(ZD_SHEET.equals(sheetName)) {
                if (ReimbursementTypeEnmu.ENTERTAIN.getCode().equals(this.bxType) || ReimbursementTypeEnmu.ENTERTAINSPREAD.getCode().equals(this.bxType)) {
                    StringBuilder errorInfo = new StringBuilder(); 
                    EntertainDetailDto detail = (EntertainDetailDto)obj;
                    int errorNum = BaseController.validate(detail, errorInfo);
                    if(errorNum > 0) throw new RuntimeException(errorInfo.toString());
                    try {
	                    if(detail.getDate().length() != 10) throw new RuntimeException("error");
                        Date zdDate = Constants.FORMAT2_10.parse(detail.getDate());
                    }catch (ParseException dateParseE){
                        throw new RuntimeException("???????????????"+ detail.getDate() +"????????????????????????(???2021/07/15)???");
                    }
                }
            }else if(HB_SHEET.equals(sheetName)) {
                HbDetailDto detail = (HbDetailDto)obj;
                StringBuilder errorInfo = new StringBuilder(); 
                int errorNum = BaseController.validate(detail, errorInfo);
                if(errorNum == 4) {
                    //???????????????????????????
                    return;
                }
                if (errorNum > 0) throw new RuntimeException(errorInfo.toString());
                BudgetUnit unitInfo = this.unitMapper.selectOne(new QueryWrapper<BudgetUnit>().eq("yearid", this.yearId).eq("name", detail.getUnitName()));
                if (null == unitInfo) throw new Exception("???????????????"+ detail.getUnitName() +"???????????????");
                BudgetSubject subjectInfo = this.subjectService.getOne(new QueryWrapper<BudgetSubject>().eq("yearid", this.yearId).eq("name", detail.getSubName()));
                if (null == subjectInfo) throw new Exception("???????????????"+ detail.getSubName() +"???????????????");
                BudgetMonthAgent monthAgent =  this.monthAgentService.getOne(new QueryWrapper<BudgetMonthAgent>().eq("unitid", unitInfo.getId()).eq("monthid", monthId).eq("subjectid", subjectInfo.getId()).eq("name", detail.getAgentName()));
                if (null == monthAgent) throw new Exception("?????????"+ detail.getSubName() +"???????????????");
                MonthAgentMoneyInfo monthAgentMoneyInfo = new MonthAgentMoneyInfo();
                monthAgentMoneyInfo.setMonthAgentId(monthAgent.getId());
                monthAgentMoneyInfo.setMonthId(this.monthId);
                monthAgentMoneyInfo.setUnitId(unitInfo.getId());
                monthAgentMoneyInfo.setYearId(this.yearId);
                monthAgentMoneyInfo = this.monthAgentService.getUnitMonthAgentInfo(monthAgentMoneyInfo);
                detail.setAgentMoneyInfo(monthAgentMoneyInfo);
                
            }
			
		}catch(Exception e) {
			e.printStackTrace();
			Cell cell = row.createCell(row.getLastCellNum());
			CellStyle cellStyle = helper.getWorkbook().createCellStyle();
			Font font = helper.getWorkbook().createFont();
			font.setColor(Font.COLOR_RED);
			cellStyle.setFont(font);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(StringUtils.isBlank(e.getMessage())?"?????????":e.getMessage());
			helper.setExportError(true);
		}		
	}


	@Override
	public void process(ExcelImportHelper helper, Class<?> arg1, Map<String, Object> headMap, Sheet sheet, int row)
			throws Exception {
	    String sheetName = sheet.getSheetName();
		/**
		 * ????????????
		 */
		try {
		    if(BX_SHEET.equals(sheetName)) {
	            BxInfoSheetDto head = JSONObject.parseObject(JSONObject.toJSONString(headMap),BxInfoSheetDto.class);
	            String errorInfo = BaseController.validate(head);
	            if(StringUtils.isNotBlank(errorInfo)) throw new RuntimeException(errorInfo);
	            if(null == head.getH3()) head.setH3(0d);	            BudgetYearPeriod yearPeriod = this.yearMapper.selectOne(new QueryWrapper<BudgetYearPeriod>().eq("period", head.getB2()));
	            if(Objects.isNull(yearPeriod)) throw new RuntimeException("?????????"+head.getB2()+"???????????????");
	            //head.setYearPeriod(yearPeriod);
	            this.yearId = yearPeriod.getId();
                headMap.put("yearId", this.yearId);
	            String month = head.getD2();
	            String regEx = "[^0-9]";
	            Pattern p = Pattern.compile(regEx);
	            Matcher m = p.matcher(month);
	            Integer monthid = Integer.parseInt(m.replaceAll("").trim());
	            if (monthid > 12 || monthid < 1) throw new RuntimeException("?????????"+ month +"????????????");
	            this.monthId = Long.valueOf(monthid);
	            headMap.put("monthId", this.monthId);
	            BudgetUnit unit = unitMapper.selectOne(new QueryWrapper<BudgetUnit>().eq("yearid", yearPeriod.getId()).eq("name", head.getF2()));
	            if(Objects.isNull(unit)) throw new RuntimeException("???"+head.getB2()+"??????????????????????????????"+head.getF2()+"??????");
	            //head.setUnit(unit);
	            this.unitId = unit.getId();
                headMap.put("unitId", this.unitId);
	            TabDm bxTypeDm = this.dmService.getOne(new QueryWrapper<TabDm>().eq("dm_name", head.getD4()).eq("dm_type", "bx_type"));
	            if (null == bxTypeDm) throw new RuntimeException("???????????????" + head.getD4() +"????????????");
	            this.bxType = Integer.valueOf(bxTypeDm.getDm());
                headMap.put("bxType", this.bxType);
                String empNo = head.getB3();
                String userName = head.getD3();
                WbUser bxrInfo = this.userMapper.selectOne(new QueryWrapper<WbUser>().eq("USER_NAME", empNo).eq("DISPLAY_NAME", userName));
                if (null == bxrInfo) throw new RuntimeException("???????????????????????????");
                headMap.put("bxrId", bxrInfo.getUserId());
                headMap.put("bxrName", bxrInfo.getDisplayName());
                if (StringUtils.isNotBlank(head.getF4())) {
                    BudgetReimbursementorder orgOrder = this.orderService.getOne(new QueryWrapper<BudgetReimbursementorder>().eq("reimcode", head.getF4()));
                    if (null == orgOrder) throw new RuntimeException("???????????????"+ head.getF4() +"???????????????");
                    headMap.put("id", orgOrder.getId());                
                }
                try {
                    String date = head.getF3();
                    if(date.length() != 10){
                    	throw new RuntimeException("error");
                    }
	                Date bxDate = DateUtil.getDateByStr(date, "yyyy/MM/dd");
	                headMap.put("bxDate", bxDate);
                }catch (Exception dateParseE){
                    throw new RuntimeException("????????????????????????????????????????????????????????????(???2021/07/15)");
                }
                /**
	             * ??????????????????????????????????????????
	             */
	            validateReimbursementEmp(unit, empNo);

	        }else if (CL_SHEET.equals(sheetName)) {
	            TravelSheetDto head = JSONObject.parseObject(JSONObject.toJSONString(headMap),TravelSheetDto.class);
                String name = head.getName();
                String reason = head.getReason();
                if (ReimbursementTypeEnmu.TRAVAL.getCode().equals(this.bxType) || ReimbursementTypeEnmu.TRAVALSUBSIDIES.getCode().equals(this.bxType) ) {
                    String errorInfo = "";
                    if (StringUtils.isBlank(reason)) {
                        errorInfo += "???????????????????????????";
                    }else if(StringUtils.isBlank(name)) {
                        errorInfo += "???????????????????????????";
                    }
                    if (StringUtils.isNotBlank(errorInfo)) {
                        throw new Exception(errorInfo);
                    }
                }
                
	        }
			
		}catch(Exception e) {
			e.printStackTrace();
			Row curRow = sheet.getRow(row);
			Cell cell = curRow.createCell(curRow.getLastCellNum());
			CellStyle cellStyle = helper.getWorkbook().createCellStyle();
			Font font = helper.getWorkbook().createFont();
			font.setColor(Font.COLOR_RED);
			cellStyle.setFont(font);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(StringUtils.isBlank(e.getMessage())?"?????????":e.getMessage());
			helper.setExportError(true);
			//throw e;
			headMap.put("headError", true);
		}		
	}

	/**
	 * ??????????????????????????????????????????
	 * @param unit
	 * @param empNo
	 */
	private void validateReimbursementEmp(BudgetUnit unit, String empNo) {
		String budgetdepts = unit.getBudgetdepts();
		String budgetusers = unit.getBudgetusers();
		boolean isExistUser = false;
		if(StringUtils.isNotBlank(budgetusers)) {
			List<String> budgetUserIdList = Arrays.asList(budgetusers.split(","));
			isExistUser = userMapper.selectList(new QueryWrapper<WbUser>().in("USER_ID",budgetUserIdList)).stream().filter(e->e.getUserName().equals(empNo)).findFirst().isPresent();
		}
		if(isExistUser) return; //???????????????????????????????????????????????????????????????????????????
		boolean isExistDept = false;
		if(StringUtils.isNotBlank(budgetdepts)) {
			List<WbDept> deptList = deptMapper.selectList(null);
			Map<String, WbDept> deptMap = deptList.stream().collect(Collectors.toMap(WbDept::getDeptId, Function.identity()));
			//??????????????????
			List<WbDept> allChildDeptList = new ArrayList<>();
			for(String deptid : budgetdepts.split(",")) {
				WbDept dept = deptMap.get(deptid);
				List<WbDept> childrenDepts = deptList.stream().filter(e->e.getParentIds().startsWith(dept.getParentIds())).collect(Collectors.toList());
				allChildDeptList.addAll(childrenDepts);
			}
			//????????????id
			List<String> allChildDeptIdList = allChildDeptList.stream().map(e->e.getDeptId()).distinct().collect(Collectors.toList());
			isExistDept = personMapper.selectList(new QueryWrapper<WbPerson>().in("DEPT_Id", allChildDeptIdList)).stream().filter(e->e.getPersonCode().equals(empNo)).findFirst().isPresent();
		}
		if(!isExistUser && !isExistDept) throw new RuntimeException("???????????????"+unit.getName()+"???????????????????????????"+empNo+"??????");
	}

}
