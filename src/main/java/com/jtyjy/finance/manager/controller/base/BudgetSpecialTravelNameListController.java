package com.jtyjy.finance.manager.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.bean.BudgetSpecialTravelNameList;
import com.jtyjy.finance.manager.bean.WbDept;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.BudgetSpecialTravelNameListService;
import com.jtyjy.finance.manager.service.WbDeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = { "出差特殊人员接口" })
@RestController
@RequestMapping("/api/base/budgetSpecialTravel")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetSpecialTravelNameListController extends BaseController<BudgetSpecialTravelNameList> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BudgetSpecialTravelNameListController.class);

	@Autowired
	private BudgetSpecialTravelNameListService service;

	@Autowired
	private WbDeptService deptService;

	@ApiOperation(value = "新增",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("save")
	public ResponseEntity<String> save(@Valid BudgetSpecialTravelNameList bean, BindingResult bindingResult){
		try {
			String retError = this.getResult(bindingResult);
			if(StringUtils.isNotBlank(retError)){
				return ResponseEntity.error(retError);
			}
			bean.setCreateTime(new Date());
			bean.setUpdateTime(new Date());
			this.service.save(bean);
			if(LOGGER.isInfoEnabled()){
				LOGGER.info("新增成功......");
			}
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}

	}

	@ApiOperation(value = "修改",httpMethod="POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("update")
	public ResponseEntity<String> update(@Valid BudgetSpecialTravelNameList bean, BindingResult bindingResult){
		try {
			if(bean.getId() == null) return ResponseEntity.error("缺少id参数");
			String retError = this.getResult(bindingResult);
			if(StringUtils.isNotBlank(retError)){
				return ResponseEntity.error(retError);
			}
			bean.setUpdateTime(new Date());
			this.service.updateById(bean);
			if(LOGGER.isInfoEnabled()){
				LOGGER.info("修改成功......");
			}
			return ResponseEntity.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
	}

	@ApiOperation(value = "获取列表",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "姓名或者工号查询", name = "displayName", dataType = "String", required = false),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "是否停用,false否 true是", name = "stopFlag", dataType = "Boolean", required = true)
	})
	@GetMapping("list")
	public ResponseEntity<PageResult<BudgetSpecialTravelNameList>> list(@RequestParam(defaultValue = "1") Integer page,
	                                       @RequestParam(defaultValue = "20")Integer rows,
	                                       @RequestParam(value = "displayName",required = false)String empNo
										, @RequestParam(value = "stopFlag",required = false)Boolean stopFlag){
		try {
			Map<String, WbDept> deptMap = deptService.list(null).stream().collect(Collectors.toMap(e -> e.getDeptId(), e -> e));
			Page<BudgetSpecialTravelNameList> pageCond = new Page<>(page,rows);
			LambdaQueryWrapper<BudgetSpecialTravelNameList> qw = new LambdaQueryWrapper<>();
			if(stopFlag !=null) qw.eq(BudgetSpecialTravelNameList::getStopFlag,stopFlag);
			if(StringUtils.isNotBlank(empNo)) {
				qw.and(qw1->{
					qw1.like(BudgetSpecialTravelNameList::getEmpNo,empNo).or().like(BudgetSpecialTravelNameList::getEmpName,empNo);
				});
			}
			pageCond = this.service.page(pageCond, qw.orderByDesc(BudgetSpecialTravelNameList::getUpdateTime));
			pageCond.getRecords().stream().forEach(e->{
				if(e.getDeptId()!=null) {
					e.setDeptFullName(deptMap.get(e.getDeptId().toString()).getDeptFullname());
					e.setDeptName(deptMap.get(e.getDeptId().toString()).getDeptName());
				}
			});
			return ResponseEntity.ok(PageResult.apply(pageCond.getTotal(),pageCond.getRecords()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.error(e.getMessage());
		}
	}
}
