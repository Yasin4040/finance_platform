package com.jtyjy.finance.manager.controller.extract;

import java.util.Date;
import java.util.Objects;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetExtractOuterperson;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetExtractOuterpersonService;
import com.jtyjy.finance.manager.service.BudgetUnitService;
import com.jtyjy.finance.manager.service.WbBanksService;
import com.jtyjy.finance.manager.service.WbUserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = { "提成外部人员" })
@RestController
@RequestMapping("/api/extractOutPerson")
@CrossOrigin
@SuppressWarnings("all")
public class BudgetExtractOutPersonController extends BaseController<BudgetExtractOuterperson>{
	
	private final static Logger LOGGER = LoggerFactory.getLogger(BudgetExtractOutPersonController.class);
	
	@Autowired
	private BudgetExtractOuterpersonService extractOuterpersonService;
	
	@Autowired
	private BudgetUnitService unitService;
	
	@Autowired
	private WbBanksService bankService;
	
	@Autowired
	private WbUserService userService;
	
	
	@ApiOperation(value = "新增提成外部人员",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/addExtractOutPerson")
	public ResponseEntity addExtractOutPerson(@RequestBody @Valid BudgetExtractOuterperson entity, BindingResult bindingResult){
		try {
			String retError = this.getResult(bindingResult);
	        if (StringUtils.isNotBlank(retError)) {
	            return ResponseEntity.error(retError);
	        }
			BudgetExtractOuterperson extractOuterperson = this.extractOuterpersonService.getOne(new QueryWrapper<BudgetExtractOuterperson>().eq("empno", entity.getEmpno()).eq("name", entity.getName()));
			if(extractOuterperson !=null) throw new RuntimeException("添加失败！编号【"+entity.getName()+"("+entity.getEmpno()+")】已经存在！");		
			BudgetExtractOuterperson extractOuterperson1 = this.extractOuterpersonService.getOne(new QueryWrapper<BudgetExtractOuterperson>().eq("idnumber", entity.getIdnumber()));
			if(extractOuterperson1 !=null) throw new RuntimeException("添加失败！身份证号【"+entity.getIdnumber()+")】已经存在！");
			
			WbUser user = userService.getOne(new QueryWrapper<WbUser>().eq("ID_NUMBER", entity.getIdnumber()));
			if(user!=null) throw new RuntimeException("此外部人员身份证号与公司员工【"+user.getDisplayName()+"("+user.getUserName()+")"+"】身份证重复");
			entity.setCreatetime(new Date());
			entity.setUpdatetime(entity.getCreatetime());
			extractOuterpersonService.save(entity);
			return ResponseEntity.ok();
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "修改提成外部人员",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("/updateExtractOutPerson")
	public ResponseEntity updateExtractOutPerson(@RequestBody @Valid BudgetExtractOuterperson entity, BindingResult bindingResult){
		try {
			String retError = this.getResult(bindingResult);
	        if (StringUtils.isNotBlank(retError)) {
	            return ResponseEntity.error(retError);
	        }
			if(Objects.isNull(entity.getId())) throw new RuntimeException("缺少id必填字段");
			
			BudgetExtractOuterperson extractOuterperson = this.extractOuterpersonService.getOne(new QueryWrapper<BudgetExtractOuterperson>().eq("empno", entity.getEmpno()).eq("name", entity.getName()).ne("id", entity.getId()));
			if(extractOuterperson !=null) throw new RuntimeException("修改失败！编号【"+entity.getName()+"("+entity.getEmpno()+")】已经存在！");		
			BudgetExtractOuterperson extractOuterperson1 = this.extractOuterpersonService.getOne(new QueryWrapper<BudgetExtractOuterperson>().eq("idnumber", entity.getIdnumber()).ne("id", entity.getId()));
			if(extractOuterperson1 !=null) throw new RuntimeException("修改失败！身份证号【"+entity.getIdnumber()+")】已经存在！");
			WbUser user = userService.getOne(new QueryWrapper<WbUser>().eq("ID_NUMBER", entity.getIdnumber()));
			if(user!=null) throw new RuntimeException("此外部人员身份证号与公司员工【"+user.getDisplayName()+"("+user.getUserName()+")"+"】身份证重复");
			BudgetExtractOuterperson oldRecord = extractOuterpersonService.getById(entity.getId());			
			BeanUtils.copyProperties(entity, oldRecord);	
			oldRecord.setUpdatetime(new Date());
			extractOuterpersonService.updateById(oldRecord);
			if(entity.getBudgetbillingunitid() == null) {
				extractOuterpersonService.update("update budget_extract_outerperson set budgetbillingunitid=null where id ="+oldRecord.getId(), null);
			}
			return ResponseEntity.ok();
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	
	@ApiOperation(value = "获取提成外部人员列表",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "名称", name = "name", dataType = "String", required = false),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getExtractOutPersonList")
	public ResponseEntity<PageResult<BudgetExtractOuterperson>> getExtractOutPersonList(@RequestParam(name="name",required = false)String name,
			@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "20")Integer rows){
		try {
			PageResult<BudgetExtractOuterperson> pageList = extractOuterpersonService.getExtractOutPersonList(name,page,rows);
			return ResponseEntity.ok(pageList);
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
	@GetMapping("/getExtractOutPersonDetail")
	public ResponseEntity<BudgetExtractOuterperson> getExtractOutPersonDetail(@RequestParam(name="id",required = true)Long id){
		try {
			BudgetExtractOuterperson detail = extractOuterpersonService.getById(id);
			if(detail.getBudgetbillingunitid()!=null) {
				detail.setBillingUnitName(this.unitService.getById(detail.getBudgetbillingunitid()).getName());
			}
			if(StringUtils.isNotBlank(detail.getBranchcode())) {
				detail.setOpenBank(this.bankService.getOne(new QueryWrapper<WbBanks>().eq("sub_branch_code", detail.getBranchcode())).getSubBranchName());
			}
			return ResponseEntity.ok(detail);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());
		}
	}
}
