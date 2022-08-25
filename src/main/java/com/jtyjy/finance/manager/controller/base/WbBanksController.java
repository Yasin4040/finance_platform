package com.jtyjy.finance.manager.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.result.ResponseResult;
import com.jtyjy.finance.manager.bean.BudgetAgentExecuteView;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.bean.WbRegion;
import com.jtyjy.finance.manager.controller.BaseController;
import com.jtyjy.finance.manager.service.TabDmService;
import com.jtyjy.finance.manager.service.WbBanksService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Api(tags = {"银行信息管理接口"})
@RestController
@RequestMapping("/api/wbbanks")
public class WbBanksController extends BaseController<BudgetAgentExecuteView> {

	@Autowired
	private WbBanksService service;
	@Autowired
	private TabDmService dmService;

	/**
	 * 新增/修改（修改时需送id）
	 */
	@ApiOperation(value = "新增/修改（修改时需送id）", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("addOrUpdate")
	public ResponseEntity<String> addOrUpdate(@RequestBody @Valid WbBanks bean, BindingResult bindingResult) {
		String retError = this.getResult(bindingResult);
		if (StringUtils.isNotBlank(retError)) {
			return ResponseEntity.apply(StatusCodeEnmus.REQUIRE_PARAMS_NULL, retError, null);
		}
		if (bean.getId() == null) {
			WbBanks sameBranchCode = this.service.getOne(new LambdaQueryWrapper<WbBanks>().eq(WbBanks::getSubBranchCode, bean.getSubBranchCode()));
			if (null != sameBranchCode) {
				return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_EXIST, "已存在相同的联行号");
			}
			this.service.save(bean);
		} else {
			int count = this.service.count(new LambdaQueryWrapper<WbBanks>().ne(WbBanks::getId, bean.getId()).eq(WbBanks::getSubBranchCode, bean.getSubBranchCode()));
			if (count > 0) {
				return ResponseEntity.apply(StatusCodeEnmus.DATA_IS_EXIST, "已存在相同的联行号");
			}
			this.service.updateById(bean);
		}
		return ResponseEntity.ok();

	}

	/**
	 * 按照主键批量删除
	 */
	@ApiOperation(value = "按照主键批量删除", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "主键（多个主键以“,”分割）", name = "ids", dataType = "String", required = true),
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("deleteByIds")
	public ResponseResult deleteByIds(String ids) {
		this.service.removeByIds(Arrays.asList(ids.split(",")));
		return ResponseResult.ok();
	}

	/**
	 * 分页查询省市区
	 */
	@ApiOperation(value = "分页查询省市区", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "区域等级 1：省份 2：城市 3：区县", name = "level", dataType = "String", required = true),
			@ApiImplicitParam(value = "上级代码", name = "pcode", dataType = "String"),
			@ApiImplicitParam(value = "地区名称", name = "name", dataType = "String"),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("queryArea")
	public ResponseEntity<Page<WbRegion>> queryArea(
			@RequestParam(value = "level") Integer level,
			@RequestParam(value = "pcode", required = false) String pcode,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "20") Integer rows) throws Exception {

		Page<WbRegion> voList = this.service.getAreaInfo(level, pcode, name, page, rows);
		return ResponseEntity.ok(voList);
	}

	/**
	 * 获取银行类型
	 */
	@ApiOperation(value = "获取银行类型", httpMethod = "POST")
	@ApiImplicitParams(value = {@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)})
	@PostMapping("getBankType")
	public ResponseEntity<List> getBankType() throws Exception {
		//List result = this.service.getBankType();
		List<TabDm> bankTypeList = dmService.list(new LambdaQueryWrapper<TabDm>().eq(TabDm::getDmType, "bank_type").eq(TabDm::getDmStatus, 1));
		List<String> result = bankTypeList.stream().map(TabDm::getDmName).collect(Collectors.toList());
		return ResponseEntity.ok(result);
	}


	@ApiIgnore
	@GetMapping("initBankType")
	public ResponseEntity<String> initBankType() throws Exception {
		List<String> result = this.service.getBankType();
		List<TabDm> bankTypeList = dmService.list(new LambdaQueryWrapper<TabDm>().eq(TabDm::getDmType, "bank_type").eq(TabDm::getDmStatus, 1));
		if (CollectionUtils.isEmpty(bankTypeList)) {
			bankTypeList = result.stream().map(e -> {
				return new TabDm("bank_type", "1", 1, e, e, "", "");
			}).collect(Collectors.toList());
			if(!CollectionUtils.isEmpty(bankTypeList)){
				dmService.saveBatch(bankTypeList);
			}
		}

		return ResponseEntity.ok();
	}

	@GetMapping("addBankType")
	@ApiOperation(value = "新增银行类型", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true),
			@ApiImplicitParam(value = "银行类型", name = "bankType", dataType = "String", required = true)
	})
	public ResponseEntity<String> addBankType(@RequestParam(required = true,value = "bankType") String bankType){
		if(StringUtils.isBlank(bankType)) return ResponseEntity.error("请填写银行类型。");
		int sameBankTypeCount = dmService.count(new LambdaQueryWrapper<TabDm>().eq(TabDm::getDmType, "bank_type").eq(TabDm::getDm,bankType));
		if(sameBankTypeCount>0){
			return ResponseEntity.error("该银行类型已存在。");
		}
		dmService.save(new TabDm("bank_type","1",1,bankType,bankType,"",""));
		return ResponseEntity.ok();
	}

	/**
	 * 查询银行信息
	 */
	@ApiOperation(value = "查询银行信息", httpMethod = "POST")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "支行名称（模糊查询）", name = "branchName", dataType = "String"),
			@ApiImplicitParam(value = "联行号（模糊查询）", name = "branchCode", dataType = "String"),
			@ApiImplicitParam(value = "省份代码", name = "province", dataType = "String"),
			@ApiImplicitParam(value = "城市代码", name = "city", dataType = "String"),
			@ApiImplicitParam(value = "银行类型（模糊查询）", name = "bankName", dataType = "String"),
			@ApiImplicitParam(value = "当前页（默认1）", name = "page", dataType = "Integer"),
			@ApiImplicitParam(value = "每页条数（默认20）", name = "rows", dataType = "Integer"),
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@PostMapping("queryBank")
	public ResponseEntity<Page<WbBanks>> queryBank(
			@RequestParam(value = "branchName", required = false) String branchName,
			@RequestParam(value = "branchCode", required = false) String branchCode,
			@RequestParam(value = "province", required = false) String province,
			@RequestParam(value = "city", required = false) String city,
			@RequestParam(value = "bankName", required = false) String bankName,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
			@RequestParam(value = "rows", required = false, defaultValue = "20") Integer rows) throws Exception {
		Map<String, Object> map = new HashMap<>();
		if (StringUtils.isNotBlank(branchName)) {
			//例: “南昌市-赣江”即可查询所有南昌市的银行且支行名称中带有“赣江”的支行；“北京市-”即可查询所有北京市的银行
			if (branchName.contains("-")) {
				String cityName = branchName.split("-")[0];
				branchName = branchName.substring(branchName.indexOf("-") + 1);
				map.put("cityName", cityName);
			}
			map.put("branchName", branchName);
		}
		map.put("branchCode", branchCode);
		map.put("province", province);
		map.put("city", city);
		map.put("bankName", bankName);
		Page<WbBanks> voList = this.service.getBankPageInfo(map, page, rows);
		return ResponseEntity.ok(voList);
	}

	/**
	 * 按照ID查询
	 */
	@ApiOperation(value = "按照主键查询", httpMethod = "GET")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(value = "主键", name = "id", dataType = "Serializable", required = true),
			@ApiImplicitParam(value = "登录唯一标识", name = "token", dataType = "String", required = true)
	})
	@GetMapping("getById")
	public ResponseResult getById(Serializable id) {
		return ResponseResult.ok(this.service.getById(id));
	}
}
