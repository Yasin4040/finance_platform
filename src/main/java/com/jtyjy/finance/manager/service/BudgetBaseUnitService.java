package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetBaseUnit;
import com.jtyjy.finance.manager.mapper.BudgetBaseUnitMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.easyexcel.BaseUnitExcelData;
import com.jtyjy.common.tools.PinyinTools;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基础单位service
 * @author shubo
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetBaseUnitService extends DefaultBaseService<BudgetBaseUnitMapper, BudgetBaseUnit> {

	private final TabChangeLogMapper loggerMapper;
	
	private final BudgetBaseUnitMapper baseUnitMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_base_unit"));
	}

	public int importAdd(InputStream inputStream, String coverFlag, List<BaseUnitExcelData> errorList) {
	    List<BaseUnitExcelData> excelList = EasyExcelUtil.getExcelContent(inputStream, BaseUnitExcelData.class);
	    if (null == excelList || excelList.isEmpty()) {
	        BaseUnitExcelData excelData = new BaseUnitExcelData();
	        excelData.setErrMsg("表格解析失败或无有效数据");
	        errorList.add(excelData);
	        return 0;
	    }
	    List<BudgetBaseUnit> saveList = new ArrayList<>();
	    for (BaseUnitExcelData excelData : excelList) {
	        String unitName = excelData.getUnitName();
	        String orderNo = excelData.getOrderNo();
	        if (StringUtils.isBlank(unitName)) {
	            excelData.setErrMsg("基础单位名称不能为空");
	            errorList.add(excelData);
	            continue;
            }
	        if (StringUtils.isBlank(excelData.getOrderNo())) {
	            orderNo = "0";
	        }
	        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
	        if (!pattern.matcher(orderNo).matches()) {
	            excelData.setErrMsg("排序号非法");
	            errorList.add(excelData);
	            continue;
	        }
	        if (StringUtils.isNotBlank(excelData.getRemark()) && excelData.getRemark().length() > 255) {
	            excelData.setErrMsg("备注过长");
	            errorList.add(excelData);
	            continue;
	        }
	        BudgetBaseUnit sameName = baseUnitMapper.selectOne(new QueryWrapper<BudgetBaseUnit>().eq("name", unitName));
	       
            if (null != sameName && unitName.equals(sameName.getName())) {//存在相同的名称
                if ("0".equals(coverFlag)) {//不覆盖
                    excelData.setErrMsg(unitName + "已存在");
                    errorList.add(excelData);
                    continue;
                }else {
                    sameName.setOrderno(Integer.parseInt(orderNo));
                    sameName.setRemark(excelData.getRemark());
                    sameName.setUpdatetime(new Date());
                    saveList.add(sameName);
                }
            }else {
                BudgetBaseUnit newBaseUnit = new BudgetBaseUnit();
                newBaseUnit.setName(unitName);
                newBaseUnit.setFirstspell(PinyinTools.getFirstspell(unitName));
                newBaseUnit.setFullspell(PinyinTools.getPinYin(unitName));
                newBaseUnit.setOrderno(Integer.parseInt(orderNo));
                newBaseUnit.setRemark(excelData.getRemark());
                newBaseUnit.setCreatetime(new Date());
                newBaseUnit.setStopflag(0);
                saveList.add(newBaseUnit);
            }
	    }
	    if (saveList.isEmpty()) {
	        return 0;
	    }else {
	        if (this.saveOrUpdateBatch(saveList)) {
	            return saveList.size();
	        }else {
	            return 0;
	        }
	    }
	}
	/**
	 * 分页查询基础单位
	 * @param name
	 * @param stopflag
	 * @param page
	 * @param rows
	 * @return
	 */
	public Page<BudgetBaseUnit> getBaseUnitPageList(String name, Integer stopflag, Integer page, Integer rows){
	    QueryWrapper<BudgetBaseUnit> queryWrapper = new QueryWrapper<>();
        Page<BudgetBaseUnit> pageBean = new Page<BudgetBaseUnit>(page, rows);
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        if (null != stopflag) {
            queryWrapper.eq("stopflag", stopflag);
        }
        queryWrapper.orderByAsc("orderno");
        Page<BudgetBaseUnit> selectPage = baseUnitMapper.selectPage(pageBean, queryWrapper);
        //List<BudgetBaseUnit> list = selectPage.getRecords();
        return selectPage;
	}
}
