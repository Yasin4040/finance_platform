package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.jdbc.JdbcTemplateService;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.local.JdbcSqlThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetAuthor;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.bean.WbBanks;
import com.jtyjy.finance.manager.easyexcel.AuthorExcelData;
import com.jtyjy.finance.manager.mapper.BudgetAuthorMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.mapper.WbBanksMapper;
import com.jtyjy.finance.manager.mapper.response.BankInfo;
import com.jtyjy.finance.manager.utils.CheckIdCard;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.BudgetAuthorVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetAuthorService extends DefaultBaseService<BudgetAuthorMapper, BudgetAuthor> {

	private final TabChangeLogMapper loggerMapper;
	private final BudgetAuthorMapper mapper;
	private final WbBanksMapper bankMapper;
	
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_author"));
	}

	/**
	 * 根据稿件作者获取银行信息
	 * @param authorCodeList
	 * @return
	 * @throws Exception 
	 */
	public List<BankInfo> getBankInfoByAuthorCode(List<String> authorCodeList) throws Exception {
		String inSql = JdbcTemplateService.getInSql(authorCodeList, "'");
		return this.mapper.getBankInfoByAuthorCode(inSql);
	}
	
	public void addBudgetAuthor(BudgetAuthor ba) throws Exception {
        this.save(ba);
        ba.setCode("ZZ00"+ba.getId());
        this.updateById(ba);
        return ;
	}

	public Page<BudgetAuthorVO> queryAuthorPage(Map<String, Object> conditionMap, Integer page, Integer rows){
	    Page<BudgetAuthorVO> pageCond = new Page<>(page, rows);
        List<BudgetAuthorVO> retList = this.mapper.queryAuthorPageInfo(pageCond, conditionMap, JdbcSqlThreadLocal.get());
        pageCond.setRecords(retList);
        return pageCond;
	}
	

    public int importAdd(MultipartFile srcFile, String coverFlag, List<AuthorExcelData> errorList) throws Exception {
		List<AuthorExcelData> excelList = new ArrayList<>();
	    Map<String, List<List<String>>> excelDataMap = ResponseUtil.getMultipleExcelContent(srcFile);
	    for (Map.Entry<String, List<List<String>>> entry : excelDataMap.entrySet()) {
		    List<List<String>> sheetContent = entry.getValue();
		    int size = sheetContent.size();
		    for (int i = 0; i < size; i++) {
			    // 表格正文从第三行开始
			    if (i < 1) {
				    continue;
			    }
			    List<String> rows = sheetContent.get(i);
			    AuthorExcelData excelData = new AuthorExcelData();
			    excelData.setAuthor(rows.get(0));
			    excelData.setIdnumber(rows.get(1));
			    excelData.setTaxpayernumber(rows.get(2));
			    excelData.setAuthortype(rows.get(3));
			    excelData.setCompany(rows.get(4));
			    excelData.setBankaccount(rows.get(5));
			    excelData.setBranchcode(rows.get(6));
			    excelData.setBankName(rows.get(7));
			    excelData.setProvince(rows.get(8));
			    excelData.setCity(rows.get(9));
			    excelData.setChildBankName(rows.get(10));
			    excelData.setRemark(rows.get(11));
			    excelList.add(excelData);
		    }
	    }
	    //List<AuthorExcelData> excelList = EasyExcelUtil.getExcelContent(inputStream, AuthorExcelData.class);
        if (excelList.isEmpty()) {
            AuthorExcelData authorData = new AuthorExcelData();
            authorData.setErrMsg("表格解析失败或无有效数据");
            errorList.add(authorData);
            return 0;
        }
        int success = 0;
        for (AuthorExcelData excelData : excelList) {
            StringBuffer errMsg = new StringBuffer();
            BudgetAuthor bean = new BudgetAuthor(excelData);
            
            if (this.checkData(bean, errMsg, "1".equals(coverFlag))) {
                if (null == bean.getId()) {
                    this.save(bean);
                    bean.setCode("ZZ00"+bean.getId());
                    this.updateById(bean);
                }else {
                    this.updateById(bean);
                }
                success ++;
            }else {
                excelData.setErrMsg(errMsg.toString());
                errorList.add(excelData);
            }
        }
        return success;
    }
	
	public Boolean checkData(BudgetAuthor ba, StringBuffer errMsg, boolean coverFlag) throws Exception {
	    if (StringUtils.isBlank(ba.getAuthor())) {
	        errMsg.append("作者名字不能为空！");
            return false;
	    }
        if (null == ba.getAuthortype()) {
            errMsg.append("是否公司员工不能为空！");
            return false;
        }
        if (StringUtils.isBlank(ba.getCompany())) {
            errMsg.append("所在单位不能为空！");
            return false;
        }
        if (StringUtils.isBlank(ba.getBankaccount())) {
            errMsg.append("收款银行账号不能为空！");
            return false;
        }
        if (StringUtils.isBlank(ba.getBranchcode())) {
            errMsg.append("电子联行号不能为空！");
            return false;
        }else{
	        WbBanks banks = bankMapper.selectOne(new QueryWrapper<WbBanks>().eq("sub_branch_code", ba.getBranchcode()));
	        if(banks == null){
	        	errMsg.append("电子银联号【"+ba.getBranchcode()+"】不存在。请在银行信息模块中添加开户行。");
	        	return false;
	        }
        }
        if ((StringUtils.isNotBlank(ba.getIdnumber()) && StringUtils.isNotBlank(ba.getTaxpayernumber()))
                || (null == ba.getIdnumber() && null == ba.getTaxpayernumber())) {
            errMsg.append("身份证号或纳税人识别号有且只能有一个！");
            return false;
        }

        //验证身份证号
        if (null != ba.getIdnumber() && StringUtils.isNotBlank(ba.getIdnumber())) {
            boolean isTrueIDC = CheckIdCard.validate(ba.getIdnumber());
            if (!isTrueIDC) {
                errMsg.append("【").append(ba.getIdnumber()).append("】").append("该身份证号填写有误，请检查！");
                return false;
            }
            BudgetAuthor sameIdnum = this.mapper.selectOne(new QueryWrapper<BudgetAuthor>().eq("idnumber", ba.getIdnumber()));
            if (null != sameIdnum && !sameIdnum.getId().equals(ba.getId())) {
                if (coverFlag) {
                    ba.setId(sameIdnum.getId());
                    ba.setCode(sameIdnum.getCode());
                }else {
                    errMsg.append("【").append(ba.getIdnumber()).append("】该身份证号已存在，请检查！");
                    return false; 
                }
            }
        }
        
        //验证纳税人识别号
        if (null != ba.getTaxpayernumber() && StringUtils.isNotBlank(ba.getTaxpayernumber())) {
            BudgetAuthor sameTaxNum = this.mapper.selectOne(new QueryWrapper<BudgetAuthor>().eq("taxpayernumber", ba.getTaxpayernumber()));
            if (null != sameTaxNum && !sameTaxNum.getId().equals(ba.getId())) {
                if (coverFlag) {
                    ba.setId(sameTaxNum.getId());
                    ba.setCode(sameTaxNum.getCode());
                }else {
                    errMsg.append("【").append(ba.getTaxpayernumber()).append("】该纳税人识别号已存在，请检查！");
                    return false;
                }
            }
        }
        BudgetAuthor sameAccount = this.mapper.selectOne(new QueryWrapper<BudgetAuthor>().eq("bankaccount", ba.getBankaccount()));
        if (null != sameAccount && !sameAccount.getId().equals(ba.getId())
                || (null == ba.getIdnumber() && null == ba.getTaxpayernumber())) {
            errMsg.append("该银行账号已存在！");
            return false;
        }
        if (StringUtils.isBlank(ba.getRemark())) {
            ba.setRemark("");
        }
        if (StringUtils.isBlank(ba.getIdnumber())) {
            ba.setIdnumber(null);
        }
        if (StringUtils.isBlank(ba.getTaxpayernumber())) {
            ba.setTaxpayernumber(null);
        }
        if (null != ba.getIdnumber() && ba.getIdnumber().contains("x")) {
            String idNumber = ba.getIdnumber().replace("x", "X");
            ba.setIdnumber(idNumber);
        }
        return true;
	}
}
