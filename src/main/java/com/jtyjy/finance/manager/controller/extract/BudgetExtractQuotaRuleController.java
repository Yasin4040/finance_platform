package com.jtyjy.finance.manager.controller.extract;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetBillingUnit;
import com.jtyjy.finance.manager.bean.BudgetExtractquotaRule;
import com.jtyjy.finance.manager.bean.BudgetExtractquotaRuledetail;
import com.jtyjy.finance.manager.service.BudgetBillingUnitService;
import com.jtyjy.finance.manager.service.BudgetExtractquotaRuleService;
import com.jtyjy.finance.manager.service.BudgetExtractquotaRuledetailService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = { "??????????????????" })
@RestController
@RequestMapping("/api/extractQuotaRule")
@CrossOrigin
@SuppressWarnings("all")
public class BudgetExtractQuotaRuleController {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(BudgetExtractQuotaRuleController.class);
	
	@Autowired
	private BudgetExtractquotaRuleService quotaRuleService;
	
	@Autowired
	private BudgetExtractquotaRuledetailService quotaRuleDetailService;
	
	@Autowired
	private BudgetBillingUnitService unitService;
	
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
	@PostMapping("/addExtractQuotaRule")
	public ResponseEntity addExtractQuotaRule(@RequestBody @Valid BudgetExtractquotaRule entity, BindingResult bindingResult){
		try {
			
			List<BudgetExtractquotaRule> quotaRuleList = this.quotaRuleService.list();
			if (!quotaRuleList.isEmpty()) {
				long count = quotaRuleList.stream().filter(e->e.getName().equals(entity.getName())).count();
				if(count>0) throw new RuntimeException("?????????????????????!");
			}
			if(StringUtils.isNotBlank(entity.getBillunitids())) {
				StringBuffer unames = new StringBuffer();
				String[] billUnitIdArr = entity.getBillunitids().split(",");
				if(billUnitIdArr.length<=0) throw new RuntimeException("??????????????????????????????????????????");
				List<BudgetBillingUnit> unitList =  unitService.list(new QueryWrapper<BudgetBillingUnit>().in("id", Arrays.asList(entity.getBillunitids().split(","))));
				for(String billUnitId : billUnitIdArr) {
					BudgetBillingUnit unit = unitList.stream().filter(e->e.getId().toString().equals(billUnitId)).collect(Collectors.toList()).get(0);
					quotaRuleList.forEach(payrule->{
						String[] unitidArr = payrule.getBillunitids().split(",");
						for(String unitid : unitidArr){
							if(billUnitId.equals(unitid)) throw new RuntimeException(unit.getName()+"???????????????"+payrule.getName()+"??????!");
						}
					});
					unames.append(unit.getName() + ",");
				}
				entity.setBillunitnames(unames.toString().substring(0,unames.toString().length()-1));
			}
			entity.setCreatetime(new Date());
			entity.setUpdatetime(entity.getCreatetime());
			quotaRuleService.save(entity);			
			return ResponseEntity.ok("????????????");
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
	
	
	@ApiOperation(value = "??????????????????????????????",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/addExtractQuotaRuleDetail")
	public ResponseEntity addExtractQuotaRuleDetail(@RequestBody @Valid BudgetExtractquotaRuledetail entity, BindingResult bindingResult){
		try {
			
			entity.setCreatetime(new Date());
			entity.setUpdatetime(entity.getCreatetime());
			if(entity.getMinsalary().compareTo(entity.getMaxsalary()) >= 0) throw new RuntimeException("????????????????????????????????????");
			quotaRuleDetailService.list(new QueryWrapper<BudgetExtractquotaRuledetail>().eq("extractquotaruleid", entity.getExtractquotaruleid())).forEach(e->{
				BigDecimal min = e.getMinsalary();
				BigDecimal max = e.getMaxsalary();
				boolean flag = false;
				if(entity.getMinsalary().compareTo(min)<0 && entity.getMaxsalary().compareTo(min)<=0) flag=true;
				if(entity.getMinsalary().compareTo(max)>=0 && entity.getMaxsalary().compareTo(max)>0) flag=true;
				if(!flag) throw new RuntimeException("????????????????????????????????????");
			});
			quotaRuleDetailService.save(entity);			
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
	@PostMapping("/updateExtractQuotaRule")
	public ResponseEntity updateExtractQuotaRule(@RequestBody @Valid BudgetExtractquotaRule entity, BindingResult bindingResult){
		try {
			if(Objects.isNull(entity.getId())) throw new RuntimeException("??????id????????????");
			
			List<BudgetExtractquotaRule> quotaRuleList = this.quotaRuleService.list(new QueryWrapper<BudgetExtractquotaRule>().ne("id", entity.getId()));
			if (!quotaRuleList.isEmpty()) {
				long count = quotaRuleList.stream().filter(e->e.getName().equals(entity.getName())).count();
				if(count>0) throw new RuntimeException("?????????????????????!");
			}
			BudgetExtractquotaRule oldRecord = quotaRuleService.getById(entity.getId());			
			BeanUtils.copyProperties(entity, oldRecord);
			if(StringUtils.isNotBlank(entity.getBillunitids())) {
				StringBuffer unames = new StringBuffer();
				String[] billUnitIdArr = entity.getBillunitids().split(",");
				if(billUnitIdArr.length<=0) throw new RuntimeException("??????????????????????????????????????????");
				List<BudgetBillingUnit> unitList =  unitService.list(new QueryWrapper<BudgetBillingUnit>().in("id", Arrays.asList(entity.getBillunitids().split(","))));
				for(String billUnitId : billUnitIdArr) {
					BudgetBillingUnit unit = unitList.stream().filter(e->e.getId().toString().equals(billUnitId)).collect(Collectors.toList()).get(0);
					quotaRuleList.forEach(payrule->{
						String[] unitidArr = payrule.getBillunitids().split(",");
						for(String unitid : unitidArr){
							if(billUnitId.equals(unitid)) throw new RuntimeException(unit.getName()+"???????????????"+payrule.getName()+"??????!");
						}
					});
					unames.append(unit.getName() + ",");
				}
				oldRecord.setBillunitnames(unames.toString().substring(0,unames.toString().length()-1));
			}
			oldRecord.setUpdatetime(new Date());
			quotaRuleService.updateById(oldRecord);
			return ResponseEntity.ok();
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
			
	@ApiOperation(value = "??????????????????????????????",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/updateExtractQuotaRuleDetail")
	public ResponseEntity updateExtractQuotaRuleDetail(@RequestBody @Valid BudgetExtractquotaRuledetail entity, BindingResult bindingResult){
		try {
			if(Objects.isNull(entity.getId())) throw new RuntimeException("??????id????????????");
			BudgetExtractquotaRuledetail oldRecord = quotaRuleDetailService.getById(entity.getId());
			
			if(entity.getMinsalary().compareTo(entity.getMaxsalary()) >= 0) throw new RuntimeException("????????????????????????????????????");
			quotaRuleDetailService.list(new QueryWrapper<BudgetExtractquotaRuledetail>().ne("id", entity.getId()).eq("extractquotaruleid", entity.getExtractquotaruleid())).forEach(e->{
				BigDecimal min = e.getMinsalary();
				BigDecimal max = e.getMaxsalary();
				boolean flag = false;
				if(entity.getMinsalary().compareTo(min)<0 && entity.getMaxsalary().compareTo(min)<=0) flag=true;
				if(entity.getMinsalary().compareTo(max)>=0 && entity.getMaxsalary().compareTo(max)>0) flag=true;
				if(!flag) throw new RuntimeException("????????????????????????????????????");
			});
			BeanUtils.copyProperties(entity, oldRecord);
			oldRecord.setUpdatetime(new Date());
			quotaRuleDetailService.updateById(oldRecord);
			return ResponseEntity.ok();
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
	
	
	@ApiOperation(value = "??????????????????????????????",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "id", name = "id", dataType = "Long", required = true)
	})
	@PostMapping("/deleteExtractQuotaRuleDetail")
	public ResponseEntity deleteExtractQuotaRuleDetail(@RequestParam(name="id",required = true) Long id){
		try {
			quotaRuleDetailService.removeById(id);
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
	@GetMapping("/getExtractQuotaRuleList")
	public ResponseEntity<PageResult<BudgetExtractquotaRule>> getExtractQuotaRuleList(@RequestParam(name="ruleName",required = false)String ruleName,
			@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "20")Integer rows){
		try {
			PageResult<BudgetExtractquotaRule> pageList = quotaRuleService.getExtractQuotaRuleList(page,rows,ruleName);
			return ResponseEntity.ok(pageList);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
	
	
	@ApiOperation(value = "??????????????????",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "id", name = "id", dataType = "String", required = true)
	})
	@GetMapping("/getExtractQuotaRuleDetail")
	public ResponseEntity<BudgetExtractquotaRule> getExtractQuotaRuleDetail(@RequestParam(name="id",required = true)Long id){
		try {
			BudgetExtractquotaRule detail = this.quotaRuleService.getById(id);
			return ResponseEntity.ok(detail);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
	
	@ApiOperation(value = "????????????????????????",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "id", name = "id", dataType = "String", required = true)
	})
	@GetMapping("/getExtractQuotaRuleDetailDetail")
	public ResponseEntity<BudgetExtractquotaRuledetail> getExtractQuotaRuleDetailDetail(@RequestParam(name="id",required = true)Long id){
		try {
			BudgetExtractquotaRuledetail detail = this.quotaRuleDetailService.getById(id);
			return ResponseEntity.ok(detail);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
	
	@ApiOperation(value = "????????????????????????????????????",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "??????????????????", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "??????????????????id", name = "ruleId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "??????????????????1???", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "?????????????????????20???", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getExtractQuotaRuleDetailList")
	public ResponseEntity<PageResult<BudgetExtractquotaRuledetail>> getExtractQuotaRuleDetailList(@RequestParam(name="ruleId",required = true)Long ruleId,
			@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "20")Integer rows){
		try {
			Page<BudgetExtractquotaRuledetail> pageCond = new Page<BudgetExtractquotaRuledetail>(page,rows);
			pageCond = quotaRuleDetailService.page(pageCond, new QueryWrapper<BudgetExtractquotaRuledetail>().eq("extractquotaruleid", ruleId).orderByDesc("createtime"));			
			return ResponseEntity.ok(PageResult.apply(pageCond.getTotal(), pageCond.getRecords()));
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
}
