package com.jtyjy.finance.manager.controller.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jtyjy.core.result.PageResult;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.finance.manager.service.BudgetMonitorService;
import com.jtyjy.finance.manager.vo.MonthAddDataVO;
import com.jtyjy.finance.manager.vo.MonthExecuteDataVO;
import com.jtyjy.finance.manager.vo.YearAddDataVO;
import com.jtyjy.finance.manager.vo.YearExecuteDataVO;
import com.jtyjy.finance.manager.vo.YearLendDataVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@Api(tags = { "预算监控" })
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("all")
public class BudgetMonitorController {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(BudgetMonitorController.class);
	
	@Autowired
	private BudgetMonitorService monitorService;
	
	@ApiOperation(value = "获取年度执行异常数据",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "预算单位id", name = "unitId", dataType = "Long", required = true),
			//@ApiImplicitParam(value = "预算科目名称", name = "subjectName", dataType = "String", required = false),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getYearExecuteExceptionDataList")
	public ResponseEntity<PageResult<YearExecuteDataVO>> getYearExecuteExceptionDataList(
						@RequestParam(name="yearId",required = true)Long yearId,
						@RequestParam(name="unitId",required = true)Long unitId,
						@RequestParam(name="page",defaultValue = "1") Integer page,
						@RequestParam(name="rows",defaultValue = "20")Integer rows){
		try {
			if(yearId == null || unitId == null) throw new RuntimeException("请先选择届别和预算单位");
			PageResult<YearExecuteDataVO> pageList = monitorService.getYearExecuteDataList(yearId,unitId,page,rows);
			return ResponseEntity.ok(pageList);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "处理年度执行异常数据",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "年度动因id", name = "yearAgentId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/handleYearExecuteExceptionData")
	public ResponseEntity handleYearExecuteExceptionData(
						@RequestParam(name="yearAgentId",required = true)Long yearAgentId,
						@RequestParam(name="page",defaultValue = "1") Integer page,
						@RequestParam(name="rows",defaultValue = "20")Integer rows){
		try {
			monitorService.handleYearExecuteDataList(yearAgentId);
			return ResponseEntity.ok("处理成功");
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	
	@ApiOperation(value = "获取年度追加异常数据",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "预算单位id", name = "unitId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getYearAddExceptionDataList")
	public ResponseEntity<PageResult<YearAddDataVO>> getYearAddExceptionDataList(
						@RequestParam(name="yearId",required = true)Long yearId,
						@RequestParam(name="unitId",required = true)Long unitId,
						@RequestParam(name="page",defaultValue = "1") Integer page,
						@RequestParam(name="rows",defaultValue = "20")Integer rows){
		try {
			if(yearId == null || unitId == null) throw new RuntimeException("请先选择届别和预算单位");
			PageResult<YearAddDataVO> pageList = monitorService.getYearAddExceptionDataList(yearId,unitId,page,rows);
			return ResponseEntity.ok(pageList);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "处理年度追加异常数据",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "年度动因id", name = "yearAgentId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/handleYearAddExceptionData")
	public ResponseEntity handleYearAddExceptionData(
						@RequestParam(name="yearAgentId",required = true)Long yearAgentId,
						@RequestParam(name="page",defaultValue = "1") Integer page,
						@RequestParam(name="rows",defaultValue = "20")Integer rows){
		try {
			monitorService.handleYearAddExceptionData(yearAgentId);
			return ResponseEntity.ok("处理成功");
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	
	@ApiOperation(value = "获取月度执行异常数据",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "预算单位id", name = "unitId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "月id", name = "monthId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getMonthExecuteExceptionDataList")
	public ResponseEntity<PageResult<MonthExecuteDataVO>> getMonthExecuteExceptionDataList(
						@RequestParam(name="yearId",required = true)Long yearId,
						@RequestParam(name="unitId",required = true)Long unitId,
						@RequestParam(name="monthId",required = true)Long monthId,
						@RequestParam(name="page",defaultValue = "1") Integer page,
						@RequestParam(name="rows",defaultValue = "20")Integer rows){
		try {
			if(yearId == null || unitId == null || monthId == null) throw new RuntimeException("请先选择届别、预算单位及月份");
			PageResult<MonthExecuteDataVO> pageList = monitorService.getMonthExecuteExceptionDataList(yearId,unitId,monthId,page,rows);
			return ResponseEntity.ok(pageList);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "处理月度执行异常数据",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "月度动因id", name = "monthAgentId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/handleMonthExecuteExceptionData")
	public ResponseEntity handleMonthExecuteExceptionData(
						@RequestParam(name="monthAgentId",required = true)Long monthAgentId,
						@RequestParam(name="page",defaultValue = "1") Integer page,
						@RequestParam(name="rows",defaultValue = "20")Integer rows){
		try {
			monitorService.handleMonthExecuteExceptionData(monthAgentId);
			return ResponseEntity.ok("处理成功");
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	
	@ApiOperation(value = "获取月度追加异常数据",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "预算单位id", name = "unitId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "月id", name = "monthId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getMonthAddExceptionDataList")
	public ResponseEntity<PageResult<MonthAddDataVO>> getMonthAddExceptionDataList(
						@RequestParam(name="yearId",required = true)Long yearId,
						@RequestParam(name="unitId",required = true)Long unitId,
						@RequestParam(name="monthId",required = true)Long monthId,
						@RequestParam(name="page",defaultValue = "1") Integer page,
						@RequestParam(name="rows",defaultValue = "20")Integer rows){
		try {
			if(yearId == null || unitId == null || monthId == null) throw new RuntimeException("请先选择届别、预算单位及月份");
			PageResult<MonthAddDataVO> pageList = monitorService.getMonthAddExceptionDataList(yearId,unitId,monthId,page,rows);
			return ResponseEntity.ok(pageList);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "处理月度追加异常数据",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "月度动因id", name = "monthAgentId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/handleMonthAddExceptionData")
	public ResponseEntity handleMonthAddExceptionData(
						@RequestParam(name="monthAgentId",required = true)Long monthAgentId,
						@RequestParam(name="page",defaultValue = "1") Integer page,
						@RequestParam(name="rows",defaultValue = "20")Integer rows){
		try {
			monitorService.handleMonthAddExceptionData(monthAgentId);
			return ResponseEntity.ok("处理成功");
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "获取年度拆借异常数据",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "届别id", name = "yearId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "预算单位id", name = "unitId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/getYearLendExceptionDataList")
	public ResponseEntity<PageResult<YearLendDataVO>> getYearLendExceptionDataList(
						@RequestParam(name="yearId",required = true)Long yearId,
						@RequestParam(name="unitId",required = true)Long unitId,
						@RequestParam(name="page",defaultValue = "1") Integer page,
						@RequestParam(name="rows",defaultValue = "20")Integer rows){
		try {
			if(yearId == null || unitId == null) throw new RuntimeException("请先选择届别和预算单位");
			PageResult<YearLendDataVO> pageList = monitorService.getYearLendExceptionDataList(yearId,unitId,page,rows);
			return ResponseEntity.ok(pageList);
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
	
	@ApiOperation(value = "处理年度拆借异常数据",httpMethod="GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "年度动因id", name = "yearAgentId", dataType = "Long", required = true),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer", required = false),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer", required = false)
	})
	@GetMapping("/handleYearLendExceptionData")
	public ResponseEntity handleYearLendExceptionData(
						@RequestParam(name="yearAgentId",required = true)Long yearAgentId,
						@RequestParam(name="page",defaultValue = "1") Integer page,
						@RequestParam(name="rows",defaultValue = "20")Integer rows){
		try {
			monitorService.handleYearLendExceptionData(yearAgentId);
			return ResponseEntity.ok("处理成功");
		}catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(),e);
			return ResponseEntity.error(e.getMessage());	
		}
	}
}
