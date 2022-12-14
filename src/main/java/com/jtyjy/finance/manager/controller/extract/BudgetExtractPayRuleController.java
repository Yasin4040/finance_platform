package com.jtyjy.finance.manager.controller.extract;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetBillingUnit;
import com.jtyjy.finance.manager.bean.BudgetBillingUnitAccount;
import com.jtyjy.finance.manager.bean.BudgetExtractpayRule;
import com.jtyjy.finance.manager.constants.Constants;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetBillingUnitAccountService;
import com.jtyjy.finance.manager.service.BudgetBillingUnitService;
import com.jtyjy.finance.manager.service.BudgetExtractpayRuleService;
import com.jtyjy.finance.manager.vo.ExtractAvoidBankAccountVO;
import com.jtyjy.finance.manager.vo.ExtractPayRuleVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = { "??????????????????" })
@RestController
@RequestMapping("/api/extractPayRule")
@CrossOrigin
@SuppressWarnings("all")
public class BudgetExtractPayRuleController extends BaseController<BudgetExtractpayRule>{
	
	private final static Logger LOGGER = LoggerFactory.getLogger(BudgetExtractPayRuleController.class);
	
	@Autowired
	private BudgetExtractpayRuleService extractpayRuleService;
	
	@Autowired
	private BudgetBillingUnitService unitService;
	
	@Autowired
	private BudgetBillingUnitAccountService unitAccountService;
	
	
	
	@InitBinder
    protected void init(HttpServletRequest request, ServletRequestDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));/*TimeZone??????????????????8???????????????*/
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }
	
	
	@ApiOperation(value = "????????????????????????",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/addExtractPayRule")
	public ResponseEntity addExtractPayRule(@RequestBody @Valid BudgetExtractpayRule entity, BindingResult bindingResult){
		try {
			
			String retError = this.getResult(bindingResult);
	        if (StringUtils.isNotBlank(retError)) {
	            return ResponseEntity.error(retError);
	        }
			List<BudgetExtractpayRule> payRuleList = this.extractpayRuleService.list();
			
			if (!payRuleList.isEmpty()) {
				long count = payRuleList.stream().filter(e->e.getName().equals(entity.getName())).count();
				if(count>0) throw new RuntimeException("?????????????????????!");
			}
			
			List<BudgetBillingUnit> unitList =  unitService.list(new QueryWrapper<BudgetBillingUnit>().in("id", Arrays.asList(entity.getBillunitids().split(","))));
			String[] split = entity.getBillunitids().split(",");
			StringBuffer unames = new StringBuffer();
			for (String uid : split) {
				//???????????????????????????????????????
				BudgetBillingUnit unit = unitList.stream().filter(e->e.getId().toString().equals(uid)).collect(Collectors.toList()).get(0);
				payRuleList.forEach(payrule->{
					String[] unitidArr = payrule.getBillunitids().split(",");
					for(String unitid : unitidArr){
						if(uid.equals(unitid)) throw new RuntimeException(unit.getName()+"???????????????"+payrule.getName()+"??????!");
					}
				});
				unames.append(unit.getName() + ",");	
			}
			entity.setCreatetime(new Date());
			entity.setUpdatetime(entity.getCreatetime());
			entity.setSalarypayunitnames(unames.toString().substring(0,unames.toString().length()-1));
			extractpayRuleService.save(entity);			
			return ResponseEntity.ok("????????????");
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "????????????????????????",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/updateExtractPayRule")
	public ResponseEntity updateExtractPayRule(@RequestBody @Valid BudgetExtractpayRule entity, BindingResult bindingResult){
		try {
			String retError = this.getResult(bindingResult);
	        if (StringUtils.isNotBlank(retError)) {
	            return ResponseEntity.error(retError);
	        }
			if(Objects.isNull(entity.getId())) throw new RuntimeException("??????id????????????");
			
			List<BudgetExtractpayRule> payRuleList = this.extractpayRuleService.list(new QueryWrapper<BudgetExtractpayRule>().ne("id", entity.getId()));
			long count = payRuleList.stream().filter(e->e.getName().equals(entity.getName())).count();
			if(count>0) throw new RuntimeException("?????????????????????!");
			
			String uids = entity.getBillunitids().toString();
			String[] unitids = uids.split(",");
			StringBuffer unames = new StringBuffer();
			List<BudgetBillingUnit> unitList =  unitService.list(new QueryWrapper<BudgetBillingUnit>().in("id", Arrays.asList(entity.getBillunitids().split(","))));
			for (String uid : unitids) {
				BudgetBillingUnit unit = unitList.stream().filter(e->e.getId().toString().equals(uid)).collect(Collectors.toList()).get(0);
				payRuleList.forEach(payrule->{
					String[] unitidArr = payrule.getBillunitids().split(",");
					for(String unitid : unitidArr){
						if(uid.equals(unitid)) throw new RuntimeException(unit.getName()+"???????????????"+payrule.getName()+"??????!");
					}
				});
				unames.append(unit.getName() + ",");
			}
			BudgetExtractpayRule oldRecord = extractpayRuleService.getById(entity.getId());			
			BeanUtils.copyProperties(entity, oldRecord);
			oldRecord.setSalarypayunitnames(unames.toString().substring(0,unames.toString().length() - 1));
			oldRecord.setUpdatetime(new Date());
			extractpayRuleService.updateById(oldRecord);
			return ResponseEntity.ok();
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
	
	
	@ApiOperation(value = "??????????????????????????????",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "????????????", name = "ruleName", dataType = "String", required = false),
			@ApiImplicitParam(value = "??????????????????1???", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "?????????????????????20???", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getExtractPayRuleList")
	public ResponseEntity<PageResult<ExtractPayRuleVO>> getExtractPayRuleList(@RequestParam(name="ruleName",required = false)String ruleName,
			@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "20")Integer rows){
		try {
			PageResult<ExtractPayRuleVO> pageList = extractpayRuleService.getExtractPayRuleList(page,rows,ruleName);
			return ResponseEntity.ok(pageList);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
	
	
	@ApiOperation(value = "????????????????????????????????????",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "?????????", name = "keyword", dataType = "String", required = false)
	})
	@GetMapping("/getAvoidUnitAccountList")
	public ResponseEntity<List<ExtractAvoidBankAccountVO>> getAvoidUnitAccountList(@RequestParam(name="keyword",required = false)String keyword) {
		try {
			//????????????map
			Map<Long, String> unitId2NameMap = unitService.list().stream().collect(Collectors.toMap(BudgetBillingUnit::getId, BudgetBillingUnit::getName));
			//????????????????????????
			List<BudgetBillingUnitAccount> unitAccountList = unitAccountService.list(new QueryWrapper<BudgetBillingUnitAccount>()
				.like(StringUtils.isNotBlank(keyword),"bankaccount", keyword)
				.eq("stopflag", "0"));
			List<ExtractAvoidBankAccountVO> resultList = unitAccountList.stream().map(e->{
				ExtractAvoidBankAccountVO vo = new ExtractAvoidBankAccountVO();
				vo.setUnitId(e.getBillingunitid());
				vo.setAvoidUnitAccountId(e.getId());
				vo.setBankaccount(e.getBankaccount());
				vo.setBillingUnitName(unitId2NameMap.get(e.getBillingunitid()));				
				return vo;
			}).collect(Collectors.toList());
			return ResponseEntity.ok(resultList);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
	
	
	@ApiOperation(value = "??????",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "id", name = "id", dataType = "String", required = true)
	})
	@GetMapping("/getExtractPayRuleDetail")
	public ResponseEntity<ExtractPayRuleVO> getExtractPayRuleDetail(@RequestParam(name="id",required = true)Long id){
		try {
			BudgetExtractpayRule detail = this.extractpayRuleService.getById(id);
			ExtractPayRuleVO vo = new ExtractPayRuleVO();
			BeanUtils.copyProperties(detail, vo);
			vo.setCreatetime(Constants.FULL_FORMAT.format(detail.getCreatetime()));
			vo.setEffectdate(Constants.FORMAT_14.format(detail.getEffectdate()));
			if(detail.getPersonunitid()!=null) {
				//????????????id
				BudgetBillingUnitAccount unitAccount = unitAccountService.getById(detail.getPersonunitid());
				BudgetBillingUnit billingUnit = unitService.getById(unitAccount.getBillingunitid());
				vo.setPersonunitname(billingUnit.getName());
			}
			return ResponseEntity.ok(vo);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
}
