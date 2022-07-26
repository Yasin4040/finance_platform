package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.finance.manager.bean.BudgetBaseSubject;
import com.jtyjy.finance.manager.bean.BudgetSubject;
import com.jtyjy.finance.manager.bean.BudgetUnit;
import com.jtyjy.finance.manager.mapper.BudgetBaseSubjectMapper;
import com.jtyjy.finance.manager.mapper.BudgetSubjectMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.easyexcel.BaseSubjectExcelData;
import com.jtyjy.common.enmus.StatusCodeEnmus;
import com.jtyjy.common.tools.PinyinTools;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.result.ResponseEntity;
import com.jtyjy.core.service.DefaultBaseService;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基础科目service
 * @author shubo
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetBaseSubjectService extends DefaultBaseService<BudgetBaseSubjectMapper, BudgetBaseSubject> {

	private final TabChangeLogMapper loggerMapper;
	
	private final BudgetBaseSubjectMapper bbsMapper;
	
	private final BudgetSubjectMapper bsMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_base_subject"));
	}
	
	
	public int importAdd(InputStream inputStream, String coverFlag, List<BaseSubjectExcelData> errorList) {
	    List<BaseSubjectExcelData> excelList = EasyExcelUtil.getExcelContent(inputStream, BaseSubjectExcelData.class);
        if (null == excelList || excelList.isEmpty()) {
            BaseSubjectExcelData excelData = new BaseSubjectExcelData();
            excelData.setErrMsg("表格解析失败或无有效数据");
            errorList.add(excelData);
            return 0;
        }
        List<BudgetBaseSubject> saveList = new ArrayList<>();
        for (BaseSubjectExcelData excelData : excelList) {
            String subName = excelData.getSubName();
            String orderNo = excelData.getOrderNo();
            String subCode = excelData.getSubCode();
            if (StringUtils.isBlank(subCode)) {
                excelData.setErrMsg("科目代码为空；");
                errorList.add(excelData);
                continue;
            }
            if (StringUtils.isBlank(subName)) {
                excelData.setErrMsg("科目名称为空；");
                errorList.add(excelData);
                continue;
            }
            if (StringUtils.isBlank(excelData.getOrderNo())) {
                orderNo = "0";
            }
            Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
            if (!pattern.matcher(orderNo).matches()) {
                excelData.setErrMsg("数据排序号非法；");
                errorList.add(excelData);
                continue;
            }
            if (StringUtils.isBlank(excelData.getRemark())) {
                excelData.setRemark(null);
            }
            BudgetBaseSubject sameName = bbsMapper.selectOne(new QueryWrapper<BudgetBaseSubject>().eq("name", subName));
            BudgetBaseSubject sameCode = bbsMapper.selectOne(new QueryWrapper<BudgetBaseSubject>().eq("code", subCode));
            if ("0".equals(coverFlag)) {//不覆盖
                if (null != sameName) {
                    excelData.setErrMsg(subName + "已存在；");
                    errorList.add(excelData);
                    continue;
                }
                if (null != sameCode) {
                    excelData.setErrMsg(subCode + "已存在；");
                    errorList.add(excelData);
                    continue; 
                }
                BudgetBaseSubject newBaseSub = new BudgetBaseSubject();
                newBaseSub.setCode(subCode);
                newBaseSub.setName(subName);
                newBaseSub.setFirstspell(PinyinTools.getFirstspell(subName));
                newBaseSub.setFullspell(PinyinTools.getPinYin(subName));
                newBaseSub.setOrderno(Integer.parseInt(orderNo));
                newBaseSub.setRemark(excelData.getRemark());
                newBaseSub.setCreatetime(new Date());
                newBaseSub.setStopflag(0);
                saveList.add(newBaseSub);
            }else {
                if (null != sameName && null != sameCode) {
                    if (sameName.getId().equals(sameCode.getId())) {
                        sameName.setOrderno(Integer.parseInt(orderNo));
                        sameName.setRemark(excelData.getRemark());
                        sameName.setUpdatetime(new Date());
                        saveList.add(sameName);
                    }else {
                        excelData.setErrMsg("科目代码与科目名称不匹配；");
                        errorList.add(excelData);
                        continue; 
                    }
                }else if (null != sameName && null == sameCode) {//根据名称修改
                    sameName.setOrderno(Integer.parseInt(orderNo));
                    sameName.setRemark(excelData.getRemark());
                    sameName.setUpdatetime(new Date());
                    saveList.add(sameName);
                }else if (null == sameName && null != sameCode) {//根据代码修改
                    sameCode.setOrderno(Integer.parseInt(orderNo));
                    sameCode.setRemark(excelData.getRemark());
                    sameCode.setUpdatetime(new Date());
                    saveList.add(sameCode);
                }else {//新增
                    BudgetBaseSubject newBaseSub = new BudgetBaseSubject();
                    newBaseSub.setCode(subCode);
                    newBaseSub.setName(subName);
                    newBaseSub.setFirstspell(PinyinTools.getFirstspell(subName));
                    newBaseSub.setFullspell(PinyinTools.getPinYin(subName));
                    newBaseSub.setOrderno(Integer.parseInt(orderNo));
                    newBaseSub.setRemark(excelData.getRemark());
                    newBaseSub.setCreatetime(new Date());
                    newBaseSub.setStopflag(0);
                    saveList.add(newBaseSub);
                }
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
	
    public Page<BudgetBaseSubject> getBaseSubjectPageList(String name, Integer stopflag, Integer page, Integer rows){
        Page<BudgetBaseSubject> pageCond = new Page<>(page, rows);
        List<BudgetBaseSubject> retList = bbsMapper.getBaseSubjectPageList(pageCond, name, stopflag, JdbcSqlThreadLocal.get());
        pageCond.setRecords(retList);
        return pageCond;
    }
    
    public boolean checkData(BudgetBaseSubject bean, StringBuffer errMsg) {

        if (null == bean) {
            errMsg.append("数据不能为空");
            return false;
        }
        BudgetBaseSubject sameName = this.getOne(new QueryWrapper<BudgetBaseSubject>().eq("name", bean.getName()));
        BudgetBaseSubject sameCode = this.getOne(new QueryWrapper<BudgetBaseSubject>().eq("code", bean.getCode()));
        
        if(null == bean.getId() || 0 == bean.getId().intValue()) {
            if (null != sameName && bean.getName().equals(sameName.getName())) {
                errMsg.append(bean.getName() + "名称已存在！");
                return false;
            }
            if (null != sameCode && bean.getCode().equals(sameCode.getCode())) {
                errMsg.append(bean.getCode() + "编号已存在！");
                return false;
            }
            
        }else {
            if (null != sameName && !sameName.getId().equals(bean.getId())) {
                errMsg.append(bean.getName() + "名称已存在！");
                return false;
            }
            if (null != sameCode && !sameCode.getId().equals(bean.getId())) {
                errMsg.append(bean.getCode() + "编号已存在！");
                return false;
            }
            if (1 == bean.getStopflag().intValue()) {
                List<BudgetSubject> subList = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().eq("subjectid", bean.getId()));
                if (null != subList && subList.size() > 0) {
                    errMsg.append(bean.getName() + "下存在预算科目，无法停用！");
                    return false;
                }
            }
        }

        return true;
    }
}
