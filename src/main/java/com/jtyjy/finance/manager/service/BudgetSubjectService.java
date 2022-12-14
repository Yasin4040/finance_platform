package com.jtyjy.finance.manager.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.common.tools.PinyinTools;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.BudgetBaseSubject;
import com.jtyjy.finance.manager.bean.BudgetMonthAgent;
import com.jtyjy.finance.manager.bean.BudgetProduct;
import com.jtyjy.finance.manager.bean.BudgetProductCategory;
import com.jtyjy.finance.manager.bean.BudgetSubject;
import com.jtyjy.finance.manager.bean.BudgetUnitSubject;
import com.jtyjy.finance.manager.bean.BudgetYearAgent;
import com.jtyjy.finance.manager.bean.BudgetYearPeriod;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.finance.manager.easyexcel.BaseSubjectExcelData;
import com.jtyjy.finance.manager.easyexcel.JinDieCodeExcelData;
import com.jtyjy.finance.manager.mapper.BudgetBaseSubjectMapper;
import com.jtyjy.finance.manager.mapper.BudgetMonthAgentMapper;
import com.jtyjy.finance.manager.mapper.BudgetProductCategoryMapper;
import com.jtyjy.finance.manager.mapper.BudgetProductMapper;
import com.jtyjy.finance.manager.mapper.BudgetSubjectMapper;
import com.jtyjy.finance.manager.mapper.BudgetUnitSubjectMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearAgentMapper;
import com.jtyjy.finance.manager.mapper.BudgetYearPeriodMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.utils.EasyExcelUtil;
import com.jtyjy.finance.manager.utils.ResponseUtil;
import com.jtyjy.finance.manager.vo.SubjectInfoVO;

import lombok.RequiredArgsConstructor;

/**
 * @author Admin
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetSubjectService extends DefaultBaseService<BudgetSubjectMapper, BudgetSubject> {

	private final TabChangeLogMapper loggerMapper;
	private final BudgetBaseSubjectMapper bbsMapper;
    private final BudgetYearAgentMapper byaMapper;
    //private final BudgetYearStartupMapper bysMapper;
	private final BudgetYearPeriodMapper bypMapper;
	private final BudgetProductMapper bpMapper;
	private final BudgetProductCategoryMapper bpcMapper;
	private final BudgetUnitSubjectMapper busMapper;
	private final BudgetUnitSubjectService busService;
    private final BudgetMonthAgentMapper bmaMapper;
	private final BudgetSubjectMapper bsMapper;
	private final BudgetMonthSubjectService monthSubjectService;
	private final BudgetYearSubjectService yearSubjectService;
	private final BudgetYearPeriodService bypService;
	
	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_subject"));
	}
	   /**
     * 
     * (non-Javadoc) 
     * <p>Title: subjectlist</p>  
     * <p>Description: ????????????</p>  
	 * @param subName 
     * @param params
     * @return  
     * @see com.jtyjy.budget.service.BudgetSubjectService#subjectlist(com.iamxiongx.qb.common.messgae.QBGridQueryParams)
     */
    public List<SubjectInfoVO> subjectlist(Integer yearId, String subName, Integer stopFlag) {
        Long year;
        if (null == yearId) {
            BudgetYearPeriod nowYear = this.bypService.getNowYearPeriod();
            year = nowYear.getId();
        }else {
            year = Long.valueOf(yearId);
        }
        List<SubjectInfoVO> datas = this.bsMapper.getSubjectByYearId(year, subName, stopFlag);
        for(SubjectInfoVO vo : datas) {
            String pcids = vo.getProcategoryid();
            if (StringUtils.isNotBlank(pcids)) {
                String pcName = "";
                for(String id : pcids.split(",")) {
                    BudgetProductCategory bpc = bpcMapper.selectById(Long.valueOf(id));
                    if (null != bpc) {
                        pcName += bpc.getName() + ",";
                    }
                }
                if (StringUtils.isNotBlank(pcName)) {
                    vo.setProcategoryname(pcName.substring(0, pcName.length()-1));
                }
            }
        }
        return datas;
    }
    
    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @param datas ???????????????????????????????????????????????????id????????????????????????
     * @return
     */
    public List<SubjectInfoVO> getTreeList(List<SubjectInfoVO> datas) {
        int currentLevel = 1;//???????????????????????????
        int currentParentIndex = 0;//???????????????????????????????????????
        List<SubjectInfoVO> newList = new ArrayList<>();
        for(SubjectInfoVO vo : datas) {//???????????????
            if ( 1 == vo.getLevel()) {//?????????
                newList.add(0, vo);//?????????????????????
            }else if (currentLevel == vo.getLevel()) {//???????????????????????????????????????????????????
                vo.setName(getEmptyStr(currentLevel) + vo.getName());//????????????????????????
                if (newList.get(currentParentIndex - 1).getId().equals(vo.getParentid())) {//???????????????????????????????????????
                    newList.add(currentParentIndex, vo);//sql????????????orderno?????????????????????????????????????????????
                }else {//???????????????????????????????????????????????????????????????????????????
                    for(int i = 0; i < newList.size(); i++) {
                        if(newList.get(i).getId().equals(vo.getParentid())) {//?????????????????????????????????????????????????????????????????????
                            currentParentIndex = i + 1;
                            newList.add(currentParentIndex, vo);//?????????????????????????????????????????????
                            break;
                        }
                    }
                }
            }else {//??????????????????????????????????????????????????????
                currentLevel = vo.getLevel();//????????????
                vo.setName(getEmptyStr(currentLevel) + vo.getName());//????????????????????????
                for(int i = 0;i < newList.size(); i++) {//?????????????????????????????????????????????????????????????????????
                    if(newList.get(i).getId().equals(vo.getParentid())) {
                        currentParentIndex = i+1;
                        newList.add(currentParentIndex, vo);//?????????????????????????????????????????????
                        break;
                    }
                }
            }
        }
        
        return newList;
    }
    
    private static String getEmptyStr(int level){
        String emptyStr = "";
        for (int i = 0; i < level - 1; i++) {
            emptyStr = emptyStr + "???";
        }
        return emptyStr;
    }
    
    private BudgetYearPeriod getYearPeriod(String id) {
        if(!StringUtils.isEmpty(id)) {
            return this.bypMapper.selectById(Long.valueOf(id));
        }else {
            return this.bypMapper.selectOne(new QueryWrapper<BudgetYearPeriod>().eq("currentflag", 1));
        }
    }
    
	public SubjectInfoVO addsubject(BudgetSubject budgetSubject) {
	    
//	      BudgetYearStartup yearstartup = getBudgetYearStartup(budgetSubject.getYearid());
//	      if(yearstartup.isStartbudgetflag()) {
//	          throw new RuntimeException("????????????????????????????????????????????????");
//	      }
	    checkbudgetsubjectname(budgetSubject.getYearid(), budgetSubject.getName(), null);
	    checkbudgetsubjectcode(budgetSubject.getYearid(), budgetSubject.getCode(), null);
	    checkbudgetsubject(budgetSubject.getYearid(), budgetSubject.getSubjectid());
        budgetSubject.setId(null);
        budgetSubject.setParentid(0L);
        //??????????????? ?????????????????????
        BudgetBaseSubject budgetBaseSubject = this.bbsMapper.selectById(budgetSubject.getSubjectid());
        BudgetYearPeriod budgetYearPeriod = this.bypMapper.selectById(budgetSubject.getYearid());
        if(StringUtils.isEmpty(budgetSubject.getName())) {
            budgetSubject.setName(budgetBaseSubject.getName());
        }
        budgetSubject.setFirstspell(PinyinTools.getFirstspell(budgetSubject.getName()));
        budgetSubject.setFullspell(PinyinTools.getPinYin(budgetSubject.getName()));
        budgetSubject.setCode(budgetBaseSubject.getCode());
        //??????????????????
//	      BudgetBaseSubject myzbsubject = null;
//	      if(null!=budgetSubject.getMyzbsubjectid()) {
//	          myzbsubject = (BudgetBaseSubject) oneEntityManagerByFk(budgetSubject.getMyzbsubjectid(), BudgetBaseSubject.class);
//	      }
        budgetSubject.setLeafflag(true);
        budgetSubject.setLevel(1);
        
        int num = 0;
        if(budgetSubject.getFormulaflag()) num++;
        if(budgetSubject.getJointproductflag()) num++;
        if(budgetSubject.getCostsplitflag()) num++;
        if(num>1) {
            throw new RuntimeException("?????????????????????????????????????????????????????????????????????????????????????????????");
        }
        //????????????
        if(!budgetSubject.getFormulaflag())budgetSubject.setFormula(null);
        else {
            if(StringUtils.isEmpty(budgetSubject.getFormula())) {
                throw new RuntimeException("????????????????????????");
            }
            /*if(budgetSubject.getUpsumflag()) {
                throw new RuntimeException("??????????????????????????????????????????");
            }*/
            BigDecimal dd = null;
            try {
                dd = js(budgetSubject.getFormula().replace("[this]", "(0)").trim(),null);
            }catch(Exception e) {}
            if(null!=dd) {
                throw new RuntimeException("??????????????????????????????");
            }
            
            try {
                jse.eval(formulareplace(budgetSubject.getFormula()));
            } catch (ScriptException e) {
                e.printStackTrace();
                throw new RuntimeException("?????????????????????");
            }
            //??????
            //budgetSubject.setAssistflag(false);
            //??????
            budgetSubject.setCostlendflag(false);
            //??????
            budgetSubject.setCostsplitflag(false);
            //????????????
            budgetSubject.setJointproductflag(false);
            budgetSubject.setProcategoryid(null);
            //budgetSubject.set
        }
        //????????????
        BudgetProductCategory budgetProductCategory = null;
        List<BudgetProductCategory> budgetProductCategorys = null;
        if(!budgetSubject.getJointproductflag()) budgetSubject.setProcategoryid(null);
        else{
            if(null == budgetSubject.getProcategoryid()) {
                 throw new RuntimeException("?????????????????????.");
            }
            //budgetProductCategory = (BudgetProductCategory) oneEntityManagerByFk(budgetSubject.getProcategoryid(), BudgetProductCategory.class);
            if (StringUtils.isNotBlank(budgetSubject.getProcategoryid())) {
                String[] procategoryArr = budgetSubject.getProcategoryid().split(",");
                List procategoryList = Arrays.asList(procategoryArr);
                budgetProductCategorys = this.bpcMapper.selectBatchIds(procategoryList);
            }
            //??????
            //budgetSubject.setCostlendflag(false);
            //??????
            //budgetSubject.setCostsplitflag(false);
            //??????
            budgetSubject.setFormulaflag(false);
            budgetSubject.setFormula(null);
        }
        //??????
        if(budgetSubject.getCostsplitflag()) {
            //??????
            //budgetSubject.setAssistflag(false);
            //??????
            budgetSubject.setCostlendflag(false);
            //????????????
            budgetSubject.setFormulaflag(false);
            budgetSubject.setFormula(null);
            //????????????
            budgetSubject.setJointproductflag(false);
            budgetSubject.setProcategoryid(null);
        }
        
        this.save(budgetSubject);
        budgetSubject.setPids(budgetSubject.getId()+"-");
        this.updateById(budgetSubject);
        SubjectInfoVO vo = JSON.parseObject(JSON.toJSONString(budgetSubject), SubjectInfoVO.class);
        vo.setId(budgetSubject.getId());
        vo.setBasename(budgetBaseSubject.getName());
        vo.setYearname(budgetYearPeriod.getPeriod());
        vo.setSubjectid(budgetBaseSubject.getId());
        vo.setYearid(budgetYearPeriod.getId());
//	      if(null!=budgetProductCategory) {
//	          vo.setProcategoryname(budgetProductCategory.getName());
//	          vo.setProcategoryid(budgetProductCategory.getId());
//	      }
        if(null!=budgetProductCategorys && budgetProductCategorys.size() > 0) {
            String ids = "";
            String names = "";
            for(BudgetProductCategory bpc:budgetProductCategorys) {
                if(StringUtils.isEmpty(ids)) {
                    ids = ""+bpc.getId();
                    names = bpc.getName();
                }else {
                    ids += ","+bpc.getId();
                    names = ","+bpc.getName();
                }
            }
            vo.setProcategoryid(ids);
            vo.setProcategoryname(names);
        }
        return vo;
	    
	}
	
	   /**
     * 
     * (non-Javadoc) 
     * <p>Title: updatesubject</p>  
     * <p>Description: ????????????</p>  
     * @param basesubject
     * @return  
     * @see com.jtyjy.budget.service.BudgetSubjectService#updatesubject(com.jtyjy.budget.model.BudgetBaseSubject)
     */
    public void updatesubject(BudgetSubject budgetSubject) {
        //System.out.println("--------------budgetSubject:"+JSON.toJSONString(budgetSubject));
        BudgetSubject oldbudgetSubject = this.bsMapper.selectById(budgetSubject.getId());
        if (null == oldbudgetSubject) {
            throw new RuntimeException("????????????id[" + budgetSubject.getId() +"]????????????");
        }
        BudgetSubject newbudgetSubject = new BudgetSubject();
        BeanUtils.copyProperties(oldbudgetSubject, newbudgetSubject);
        BeanUtils.copyProperties(budgetSubject, newbudgetSubject);
        //????????????
        if(StringUtils.isNotEmpty(budgetSubject.getName())) {
            checkbudgetsubjectname(budgetSubject.getYearid(), budgetSubject.getName(), budgetSubject.getId());
        }
        if(StringUtils.isNotEmpty(budgetSubject.getCode())) {
            checkbudgetsubjectcode(budgetSubject.getYearid(), budgetSubject.getCode(), budgetSubject.getId());
        }
        
        BudgetBaseSubject budgetBaseSubject = this.bbsMapper.selectById(newbudgetSubject.getSubjectid());
        BudgetYearPeriod budgetYearPeriod = this.bypMapper.selectById(newbudgetSubject.getYearid());
        //???????????????
        if(StringUtils.isNotEmpty(budgetSubject.getName())) {
            newbudgetSubject.setFirstspell(PinyinTools.getFirstspell(newbudgetSubject.getName()));
            budgetSubject.setFullspell(PinyinTools.getPinYin(budgetSubject.getName()));
        }
        //???????????????
        if(StringUtils.isEmpty(budgetSubject.getName())) {
            newbudgetSubject.setName(budgetBaseSubject.getName());
            newbudgetSubject.setCode(budgetBaseSubject.getCode());
            newbudgetSubject.setFirstspell(PinyinTools.getFirstspell(newbudgetSubject.getName()));
            newbudgetSubject.setFullspell(PinyinTools.getPinYin(newbudgetSubject.getName()));
        }
        
        int num = 0;
        if(newbudgetSubject.getFormulaflag()) num++;
        if(newbudgetSubject.getJointproductflag()) num++;
        if(newbudgetSubject.getCostsplitflag()) num++;
        if(num>1) {
            throw new RuntimeException("?????????????????????????????????????????????????????????????????????????????????????????????");
        }
        if (oldbudgetSubject.getFormulaflag() != newbudgetSubject.getFormulaflag() 
                || oldbudgetSubject.getJointproductflag() != newbudgetSubject.getJointproductflag()
                    || oldbudgetSubject.getCostsplitflag() != newbudgetSubject.getCostsplitflag()) {
            //???????????????/??????/????????????
            Integer count = this.byaMapper.countYearSubject(newbudgetSubject.getYearid(), newbudgetSubject.getId(), null);
            if(count > 0) {
                throw new RuntimeException("????????????????????????????????????????????????????????????!");
            }
            
        }
        BudgetUnitSubject busEntity = new BudgetUnitSubject();
        
        //????????????
        if(!newbudgetSubject.getFormulaflag()) {
            newbudgetSubject.setFormula(null);
            busEntity.setFormula("");
            busMapper.update(busEntity,new UpdateWrapper<BudgetUnitSubject>().eq("subjectid", newbudgetSubject.getId()));
            
        }else {
            
            if(StringUtils.isEmpty(newbudgetSubject.getFormula())) {
                throw new RuntimeException("????????????????????????");
            }
            if(!oldbudgetSubject.getLeafflag()) {
                throw new RuntimeException("??????????????????????????????????????????");
            }
            /*
            if(newbasesubject.getUpsumflag()) {
                throw new RuntimeException("??????????????????????????????????????????");
            }*/
            BigDecimal dd = null;
            try {
                dd = js(newbudgetSubject.getFormula().replace("[this]", "(0)").trim(),null);
            }catch(Exception e) {}
            if(null!=dd) {
                throw new RuntimeException("??????????????????????????????");
            }
            try {
                jse.eval(formulareplace(newbudgetSubject.getFormula()));
            } catch (ScriptException e) {
                e.printStackTrace();
                throw new RuntimeException("?????????????????????");
            }
            //??????
            //newbasesubject.setAssistflag(false);
            //??????
            newbudgetSubject.setCostlendflag(false);
            //??????
            newbudgetSubject.setCostaddflag(false);
            //??????
            newbudgetSubject.setCostsplitflag(false);
            //????????????
            newbudgetSubject.setJointproductflag(false);
            newbudgetSubject.setProcategoryid(null);
            busEntity.setFormula(newbudgetSubject.getFormula());
            busMapper.update(busEntity,new UpdateWrapper<BudgetUnitSubject>().eq("subjectid", newbudgetSubject.getId()));
            //budgetSubject.set 
            
            bmaMapper.delete(new UpdateWrapper<BudgetMonthAgent>().eq("yearid", newbudgetSubject.getYearid()).eq("subjectid", newbudgetSubject.getId()));
            byaMapper.delete(new UpdateWrapper<BudgetYearAgent>().eq("yearid", newbudgetSubject.getYearid()).eq("subjectid", newbudgetSubject.getId()));
            
        }
        //????????????
        BudgetProductCategory budgetProductCategory = null;
        List<BudgetProductCategory> budgetProductCategorys = null;

        busEntity = new BudgetUnitSubject();
        if(!newbudgetSubject.getJointproductflag()) {
            newbudgetSubject.setProcategoryid(null);
            busEntity.setProcategoryid("");
            busMapper.update(busEntity, new UpdateWrapper<BudgetUnitSubject>().eq("subjectid", newbudgetSubject.getId()));
            if(StringUtils.isNotEmpty(oldbudgetSubject.getProcategoryid())) {
                //
                for(String id:oldbudgetSubject.getProcategoryid().split(",")) {
                    List<BudgetProduct> products = bpMapper.getPdInfoByCid(id);
                    //???????????????????????????
                    for(BudgetProduct product:products) {
                        //long count = countEntityManager(" select count(0)  FROM budget_year_agent _agent INNER JOIN budget_unit _unit ON _agent.yearid = _unit.yearid AND _agent.unitid = _unit.id WHERE _agent.yearid=? AND _agent.subjectid=? AND _agent.productid=? AND _unit.requeststatus > 0 ", new Object[] {oldbudgetSubject.getYearid(),oldbudgetSubject.getId(),product.getId()});
//                      long count = countEntityManager(" select count(0)  FROM budget_year_agent _agent INNER JOIN budget_unit _unit ON _agent.yearid = _unit.yearid AND _agent.unitid = _unit.id WHERE _agent.yearid=? AND _agent.subjectid=? AND _agent.productid=? AND _agent.total > 0 ", new Object[] {oldbudgetSubject.getYearid(),oldbudgetSubject.getId(),product.getId()});
//                      if(count > 0) {
//                          throw new RuntimeException("?????????????????????????????????????????????????????????");
//                      }
                        try {
                            //executeUpdate("delete from budget_unit_product WHERE productid=? AND unitid in (SELECT id FROM budget_unit WHERE yearid=?) ",new Object[] {product.getId(),oldbudgetSubject.getYearid()});
                            bmaMapper.delete(new UpdateWrapper<BudgetMonthAgent>().eq("yearid", oldbudgetSubject.getYearid()).eq("subjectid", oldbudgetSubject.getId()));
                            byaMapper.delete(new UpdateWrapper<BudgetYearAgent>().eq("yearid", oldbudgetSubject.getYearid()).eq("subjectid", oldbudgetSubject.getId()));
                            
                            
                        }catch(Exception e) {
                            throw new RuntimeException("?????????????????????????????????????????????????????????");
                        }
                    }
                }
            }
            //oldbudgetSubject.getProcategoryid();
            //String ids = oldbudgetSubject.getProcategoryid();
            /*
            StringBuffer deletesql = new StringBuffer();
            deletesql.append(" DELETE unitproduct FROM budget_unit_product unitproduct , budget_product product , budget_product_category productcate ")
                     .append(" WHERE unitproduct.productid = product.id AND product.procategoryid = productcate.id AND (unitproduct.unitid,substring_index(productcate.pids,'-',1)) NOT IN ")
                     .append(" ( ")
                     .append(" SELECT unitsubject.unitid, substring_index(cate.pids,'-',1) pids ")
                     .append(" FROM budget_unit_subject AS unitsubject ")
                     .append(" INNER JOIN budget_product_category AS cate ON unitsubject.procategoryid = cate.id ")
                     .append(" ) ")
                     ;
            executeUpdate(deletesql.toString(), null);*/
        } else{
            if(null == newbudgetSubject.getProcategoryid()) {
                throw new RuntimeException("?????????????????????.");
            }
            if(!oldbudgetSubject.getLeafflag()) {
                throw new RuntimeException("????????????????????????????????????");
            }
            busEntity.setProcategoryid(budgetSubject.getProcategoryid());
            busMapper.update(busEntity, new UpdateWrapper<BudgetUnitSubject>().eq("subjectid", newbudgetSubject.getId()));
            
            if(StringUtils.isNotEmpty(oldbudgetSubject.getProcategoryid())) {
                //
                for(String id:oldbudgetSubject.getProcategoryid().split(",")) {
                    if(!(","+newbudgetSubject.getProcategoryid()+",").contains(id)) {
                        List<BudgetProduct> products =  bpMapper.getPdInfoByCid(id);
                        //???????????????????????????
                        for(BudgetProduct product:products) {
                            Integer count = this.byaMapper.countYearSubject(oldbudgetSubject.getYearid(), oldbudgetSubject.getId(), product.getId());
                            if(count > 0) {
                                throw new RuntimeException("???????????????????????????????????????????????????");
                            }
                            try {
                                //executeUpdate("delete from budget_unit_product WHERE productid=? AND unitid in (SELECT id FROM budget_unit WHERE yearid=?) ",new Object[] {product.getId(),oldbudgetSubject.getYearid()});
                                bmaMapper.delete(new UpdateWrapper<BudgetMonthAgent>().eq("yearid", oldbudgetSubject.getYearid()).eq("subjectid", oldbudgetSubject.getId()).eq("productid", product.getId()));
                                byaMapper.delete(new UpdateWrapper<BudgetYearAgent>().eq("yearid", oldbudgetSubject.getYearid()).eq("subjectid", oldbudgetSubject.getId()).eq("productid", product.getId()));
                            }catch(Exception e) {
                                throw new RuntimeException("???????????????????????????????????????????????????");
                            }
                        }
                    }
                }
            }else{
                Integer count = this.byaMapper.countYearSubject(newbudgetSubject.getYearid(), newbudgetSubject.getId(), null);
                if(count > 0) {
                    throw new RuntimeException("?????????????????????????????????????????????????????????!");
                }
                bmaMapper.delete(new UpdateWrapper<BudgetMonthAgent>().eq("yearid", newbudgetSubject.getYearid()).eq("subjectid", newbudgetSubject.getId()));
                byaMapper.delete(new UpdateWrapper<BudgetYearAgent>().eq("yearid", newbudgetSubject.getYearid()).eq("subjectid", newbudgetSubject.getId()));
                
            }
            if (StringUtils.isNotBlank(newbudgetSubject.getProcategoryid())) {
                String[] procategoryArr = newbudgetSubject.getProcategoryid().split(",");
                List procategoryList = Arrays.asList(procategoryArr);
                budgetProductCategorys = this.bpcMapper.selectBatchIds(procategoryList);
            }
            //budgetProductCategory = (BudgetProductCategory) oneEntityManagerByFk(newbasesubject.getProcategoryid(), BudgetProductCategory.class);
            //??????
            //newbasesubject.setCostlendflag(false);
            //??????
            //newbasesubject.setCostaddflag(false);
            //??????
            newbudgetSubject.setCostsplitflag(false);
            //??????
            newbudgetSubject.setFormulaflag(false);
            newbudgetSubject.setFormula(null);
        }
        
        if(newbudgetSubject.getCostlendflag() && !oldbudgetSubject.getLeafflag()) {
            if(!oldbudgetSubject.getLeafflag()) {
                throw new RuntimeException("????????????????????????????????????");
            }
        }
        if(newbudgetSubject.getCostaddflag() && !oldbudgetSubject.getLeafflag()) {
            if(!oldbudgetSubject.getLeafflag()) {
                throw new RuntimeException("????????????????????????????????????");
            }
        }
        //??????
        if(newbudgetSubject.getCostsplitflag()) {
            if(!oldbudgetSubject.getLeafflag()) {
                throw new RuntimeException("????????????????????????????????????");
            }
            //??????
            //newbasesubject.setAssistflag(false);
            //??????
            //newbasesubject.setCostlendflag(false);
            //????????????
            newbudgetSubject.setFormulaflag(false);
            newbudgetSubject.setFormula(null);
            //????????????
            newbudgetSubject.setJointproductflag(false);
            newbudgetSubject.setProcategoryid(null);
            budgetProductCategory = null;
        }else {
            List<String> unitName = busMapper.getUnitNameBySubId(newbudgetSubject.getId());
            if(null != unitName && !unitName.isEmpty()) {
                throw new RuntimeException("???????????????"+unitName.get(0)+"???????????????????????????"+newbudgetSubject.getName()+"?????????????????????");         
            }
            //BudgetSubject subject_ = (BudgetSubject) oneEntityManagerByFk(Long.valueOf(myid), BudgetSubject.class);
            //throw new RuntimeException("???????????????"+unit_.getName()+"????????????????????????"+subject_.getName()+"?????????????????????");
            //executeUpdate(" Update budget_unit_subject SET splitflag = 0 WHERE subjectid = ?  ", new Object[] {newbasesubject.getId()});
        }
        if (!oldbudgetSubject.getStopflag() && newbudgetSubject.getStopflag()) {//???????????????????????????????????????????????????
            Integer count = this.byaMapper.countYearSubject(newbudgetSubject.getYearid(), newbudgetSubject.getId(), null);
            if(count > 0) {
                throw new RuntimeException("??????????????????????????????????????????!");
            }
            updateSonStopflag(newbudgetSubject);
        }
        this.bsMapper.updateById(newbudgetSubject);
        
    }   
    
    private void updateSonStopflag(BudgetSubject subject) {
        List<BudgetSubject> subjects = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().eq("parentid", subject.getId()));
        if (null != subjects && subjects.size() > 0) {
            for(BudgetSubject mysubject:subjects) {
                Integer count = this.byaMapper.countYearSubject(mysubject.getYearid(), mysubject.getId(), null);
                if(count > 0) {
                    throw new RuntimeException("?????????" + mysubject.getName() + "???????????????????????????????????????!");
                }
                mysubject.setStopflag(true);
                this.bsMapper.updateById(mysubject);
                updateSonStopflag(mysubject);
            } 
        }
    }
    
    /**
    * <p>Description:??????????????????</p>
    * @param deleteParams
    * @Note ?????????????????????????????????????????????????????????????????????????????????????????????
    * <p>Author: ldw</p>
    * <p>Date:2019???3???26???</p>
    * <p>Version: 1.0</p>
    */
    @Transactional
    public void deletesubject(String deleteParams) {
        String[] ids = deleteParams.split(",");
        List<BudgetSubject> allBudgetSubjects = new ArrayList<BudgetSubject>();
        List<BudgetSubject> topSubs = new ArrayList<BudgetSubject>();
        for (String id : ids) {
            BudgetSubject subject = this.bsMapper.selectById(Long.valueOf(id));
            allBudgetSubjects.add(subject);
            if (subject.getParentid() == 0) { //?????????
                topSubs.add(subject);
            }
        }
        List<String> Ids = new ArrayList<String>(Arrays.asList(ids));
        List<String> subIds = new ArrayList<String>();
        if (null != topSubs && topSubs.size() > 0) { //?????????
            for (BudgetSubject topSubject : topSubs) {
                List<BudgetSubject> subLists = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().like("pids", topSubject.getId()+"-"));
                for (BudgetSubject bdgSbt : subLists) {
                    subIds.add(bdgSbt.getId().toString());
                    if (isInBgtunit_subject(bdgSbt.getSubjectid())) {
                        throw new RuntimeException("???"+bdgSbt.getName()+"??????????????????????????????????????????????????????");
                    }else {
                        this.bsMapper.deleteById(bdgSbt.getId());
                    }   
                }           
            }
        }
        if (null != subIds && subIds.size() >0) {//????????????????????????????????????
            for (String id : subIds) {
                if (Ids.contains(id)) {
                    Ids.remove(id);
                }
            }
        }
        List<BudgetSubject> noLeafSubs = new ArrayList<BudgetSubject>();        
        List<String> noLeafIds = new ArrayList<String>();
        for (String id : Ids) {
            BudgetSubject subject = this.bsMapper.selectById(Long.valueOf(id));
            if (!subject.getLeafflag()) {//????????????????????????????????????
                noLeafIds.add(subject.getId().toString());
                noLeafSubs.add(subject);
            }
        }
        List<String> subIds2 = new ArrayList<String>();
        for (BudgetSubject subject : noLeafSubs) {
            List<BudgetSubject> subLists = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().eq("pids", subject.getPids()));
            for (BudgetSubject budgetSubject : subLists) {
                subIds2.add(budgetSubject.getId().toString());
                if (isInBgtunit_subject(budgetSubject.getSubjectid())) {
                    throw new RuntimeException("???"+budgetSubject.getName()+"??????????????????????????????????????????????????????");
                }else {
                    //entityManager.remove(budgetSubject);

                    this.bsMapper.deleteById(budgetSubject.getId());
                }   
            }
        }
        if (null != subIds2 && subIds2.size() > 0) {
            for (String id : subIds2) {//????????????????????????????????????
                if (Ids.contains(id)) {
                    Ids.remove(id);
                }
            }
        }
        for (String id : Ids) {
            BudgetSubject subject = this.bsMapper.selectById(Long.valueOf(id));
            if (isInBgtunit_subject(subject.getSubjectid())) {
                throw new RuntimeException("???"+subject.getName()+"??????????????????????????????????????????????????????");
            }else {
                //entityManager.remove(subject);
                this.bsMapper.deleteById(subject.getId());
            }   
        }
    }
    
    /**
     * 
     * (non-Javadoc) 
     * <p>Title: updateparentid</p>  
     * <p>Description: ????????????id</p>  
     * @param id
     * @param pid  
     * @see com.jtyjy.budget.service.BudgetSubjectService#updateparentid(java.lang.String, java.lang.String)
     */
    public void updateparentid(Integer id, Integer pid) {
        if (null == id || null == pid) {
            throw new RuntimeException("id???pid????????????");
        }
        if (id.equals(pid)) {
            throw new RuntimeException("id???pid??????????????????");
        }
        BudgetSubject budgetSubject = this.bsMapper.selectById(Long.valueOf(id));
        if(null == budgetSubject) {
            return ;
        }
        /*BudgetYearStartup yearstartup = this.bysMapper.selectOne(new QueryWrapper<BudgetYearStartup>().eq("yearid", budgetSubject.getYearid()));
        if(yearstartup.isStartbudgetflag()) {
            throw new RuntimeException("???????????????????????????????????????????????????");
        }*/
        if(null == pid || 0 == pid) {
            //BudgetSubject parentsubject = entityManager.find(BudgetSubject.class,budgetSubject.getParentid());
            Integer childsubjectcount = this.bsMapper.selectCount(new QueryWrapper<BudgetSubject>().eq("parentid", budgetSubject.getParentid()));
            if(childsubjectcount == 1) {
                budgetSubject.setLeafflag(true);
                //executeUpdate("update budget_subject set leafflag=1 where id=?", new Object[]{budgetSubject.getParentid()});
            }
            budgetSubject.setParentid(0l);
            budgetSubject.setPids(budgetSubject.getId() + "-");
            budgetSubject.setLevel(1);
            
        }else {
            BudgetSubject pbudgetSubject = this.bsMapper.selectById(Long.valueOf(pid));
            if(pbudgetSubject.getStopflag()) {
                throw new RuntimeException("??????["+pbudgetSubject.getName()+"]????????????????????????????????????");
            }
            if(pbudgetSubject.getCostaddflag()) {
                throw new RuntimeException("??????["+pbudgetSubject.getName()+"]?????????????????????????????????????????????");
            }
            if(pbudgetSubject.getCostlendflag()) {
                throw new RuntimeException("??????["+pbudgetSubject.getName()+"]?????????????????????????????????????????????");
            }
            if(pbudgetSubject.getCostsplitflag()) {
                throw new RuntimeException("??????["+pbudgetSubject.getName()+"]?????????????????????????????????????????????");
            }
            if(pbudgetSubject.getJointproductflag()) {
                throw new RuntimeException("??????["+pbudgetSubject.getName()+"]?????????????????????????????????????????????");
            }
            if(pbudgetSubject.getFormulaflag()) {
                throw new RuntimeException("??????["+pbudgetSubject.getName()+"]?????????????????????????????????????????????");
            }
            Integer agentcount = this.byaMapper.selectCount(new QueryWrapper<BudgetYearAgent>().eq("subjectid", Long.valueOf(pid)));
            if(agentcount > 0) {
                throw new RuntimeException("??????["+pbudgetSubject.getName()+"]??????????????????????????????????????????");
            }
            //????????????????????????????????????
            pbudgetSubject.setLeafflag(false);
            this.bsMapper.updateById(pbudgetSubject);
            
            budgetSubject.setParentid(pbudgetSubject.getId());
            String pids = pbudgetSubject.getPids();
            if(!pids.endsWith("-")) {
                pids += "-";
            }
            budgetSubject.setPids(pids+budgetSubject.getId() + "-");
            budgetSubject.setLevel(pbudgetSubject.getLevel() + 1);
            if(budgetSubject.getYearid().longValue() != pbudgetSubject.getYearid().longValue()) {
                throw new RuntimeException("???????????????");
            }
            
            /**
             * ?????????????????????update by minzhq
             *  ????????????????????????????????????????????????
             *  ?????????????????????????????????????????????????????????????????????
             */
            List<BudgetUnitSubject> unitSubjectList = busMapper.selectList(new QueryWrapper<BudgetUnitSubject>().eq("subjectid", id));
            List<Long> parentUnitIds = busMapper.getUnitIdsBySubjectId(Long.valueOf(pid));//??????????????????????????????????????????????????????
            if(!CollectionUtils.isEmpty(unitSubjectList)) {
            	List<BudgetUnitSubject> parentUnitSubjectList = unitSubjectList.stream().filter(e -> !parentUnitIds.contains(e.getUnitid())).map(e->{
            	    BudgetUnitSubject bus = new BudgetUnitSubject();
            		BeanUtils.copyProperties(e, bus, new String[] {"unitid","subjectid","procategoryid","ccratioformula","preccratioformula","revenueformula","formula"});
            		bus.setUnitid(e.getUnitid());
            		bus.setSubjectid(Long.valueOf(pid));
            		bus.setHidden(false);           		
            		return bus;
            	}).collect(Collectors.toList());
            	if(!CollectionUtils.isEmpty(parentUnitSubjectList)) busService.saveBatch(parentUnitSubjectList);
            }
        }
        updatesubpids(budgetSubject);
        this.bsMapper.updateById(budgetSubject);
    }
    
    private void updatesubpids(BudgetSubject subject) {
        List<BudgetSubject> subjects = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().eq("parentid", subject.getId()));
        for(BudgetSubject mysubject:subjects) {
            String pids = subject.getPids();
            if(!pids.endsWith("-")) {
                pids += "-";
            }
            mysubject.setPids(pids+mysubject.getId()+"-");
            mysubject.setLevel(subject.getLevel() + 1);
            this.bsMapper.updateById(mysubject);
            updatesubpids(mysubject);
        }
    }
    
    /**
     * 
     * (non-Javadoc) 
     * <p>Title: updateorderno</p>  
     * <p>Description: ???????????????</p>  
     * @param ids  
     * @see com.jtyjy.budget.service.BudgetSubjectService#updateorderno(java.lang.String)
     */
    public void updateorderno(String ids) {
        String[] idstr = ids.split(",");
        for(int i=0;i<idstr.length;i++) {
            String id = idstr[i];
            BudgetSubject budgetSubject = this.bsMapper.selectById(Long.valueOf(id));
            budgetSubject.setOrderno(i);
            this.bsMapper.updateById(budgetSubject);
        }
    }
    
    private Boolean isInBgtunit_subject(Long subjectId){
        List<BudgetSubject> listEntityManager = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().eq("subjectid",subjectId));
        if (null != listEntityManager&& listEntityManager.size() > 0) {
            return true;
        }
        return false;
    }
    
    //????????????
    private void checkbudgetsubjectname(Long yearid, String name, Long orgId) {
        List<BudgetSubject> result = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().eq("yearid", yearid).eq("name", name));
        if(null != result && result.size() > 0) {
            if (null != orgId && orgId.equals(result.get(0).getId())) {//??????????????????
                return ;
            }else {
                throw new RuntimeException("?????????????????????" + name + "????????????");
            }
        }
    }
    //??????code
    private void checkbudgetsubjectcode(Long yearid, String code, Long orgId) {
        List<BudgetSubject> result = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().eq("yearid", yearid).eq("code", code));
        if(null != result && result.size() > 0) {
            if (null != orgId && orgId.equals(result.get(0).getId())) {//??????????????????
                return ;
            }else {
                throw new RuntimeException("?????????????????????" + code + "????????????");
            }
        }
    }
    private void checkbudgetsubject(Long yearid, Long subjectid) {
        List<BudgetSubject> result = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().eq("yearid", yearid).eq("subjectid", subjectid));
        if(null != result && result.size() > 0) {
            throw new RuntimeException("???????????????????????????");
        }
    }
    
	//js ????????????
    public final ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript"); 
    public final String formulareplace(String formula) {
        String numberstr = "0123456789";
        if(StringUtils.isEmpty(formula)) {
                return "";
        }else {
                if(formula.startsWith(".") || formula.endsWith(".")) {
                    throw new RuntimeException("??????????????????");
                }
                for(int i = 0;i < formula.length();i++) {
                    if(".".equals(formula.charAt(i)+"") && (
                            !numberstr.contains((formula.charAt(i+1)+""))  
                            || !numberstr.contains((formula.charAt(i-1)+""))) ) {
                        throw new RuntimeException("?????????????????????");
                    }
                }
                int index_ = formula.indexOf("[");
                int _index = formula.indexOf("]");
                while(index_ >= 0 && _index >= 0) {
                    if(index_ > 1) {
                        String tmp = formula.substring(index_ - 1, index_);
                        if("]".equals(tmp) || ".".equals(tmp) || numberstr.contains(tmp)) {
                            throw new RuntimeException("?????????????????????");
                        }
                    }
                    if(_index < formula.length()) {
                        String tmp = formula.substring(_index, _index + 1);
                        if("[".equals(tmp) || ".".equals(tmp) || numberstr.contains(tmp)) {
                            throw new RuntimeException("?????????????????????");
                        }
                    }
                    String repalcestr = formula.substring(index_, _index+1);
                    formula = formula.replace(repalcestr, "1");
                    index_ = formula.indexOf("[");
                    _index = formula.indexOf("]");
            }
        }
        return formula;
    }
    /**
     * 
     * <p>Description:??????</p>
     * @param formula  ??????
     * @param self     ?????????
     * @return
     * @throws NumberFormatException
     * @throws ScriptException 
     * @return Float
     * @Note
     * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
     * <p>Date: 2018???11???1??? ??????5:27:24</p>
     * <p>Version: 1.0</p>
     */
    public final BigDecimal js(String formula,Float self) throws NumberFormatException, ScriptException {
        if(null!=self) {
            formula = formula.replace("[this]", "("+self.toString()+")");
        }
        return new BigDecimal(jse.eval("("+formula+").toFixed(4)").toString());
    }
	  
    /**
     * ???????????????????????????????????????????????????????????????????????????
     * @param yearId ????????????
     * @param unitId ??????????????????
     * @param monthId ????????????
     * @param subjectId ??????????????????????????????
     * @param opt 1????????? 2????????? 3????????? 4?????????
     * @param money ??????
     */
    public void syncBudgetSubjectExecuteMoney(long yearId, long unitId,long monthId, long subjectId, int opt, BigDecimal money) {
    	BudgetSubject subject = this.getById(subjectId);
    	if(subject == null) {
    		return;
    	}
    	//?????????????????????????????????????????????????????????
    	List<BudgetSubject> subjectList = this.getSortedByPidOrderBudgetSubjectList(subject);
    	//???????????????????????????
    	this.syncBudgetSubjectExecuteMoney(subjectList,yearId,unitId,monthId,opt,money);
    }
   
    /**
     * ???????????????????????????????????????
     * @param subjectList ??????????????????????????????????????????
     * @param yearId 
     * @param unitId
     * @param monthId
     * @param opt 
     * @param money
     */
    private void syncBudgetSubjectExecuteMoney(List<BudgetSubject> subjectList, long yearId, long unitId, long monthId,int opt, BigDecimal money) {
    	//???????????????????????????????????????????????????
    	if(subjectList == null || subjectList.size() == 0) {
    		return;
    	}
    	List<Long> realSubjectIds = new ArrayList<Long>();
    	for (BudgetSubject ele : subjectList) {
            /**
             * update by minzhq
             * ?????????????????????  ??????????????????????????????????????????????????????????????????
             */
    		//if(!ele.getStopflag()) {
    			realSubjectIds.add(ele.getId());
    		//}
    		if(!ele.getUpsumflag()) {
    			break;
    		}
		}
    	//?????????????????????????????????????????????????????????????????????????????????????????????
    	this.monthSubjectService.doSyncBudgetSubjectExecuteMoney(realSubjectIds,yearId,unitId,monthId,opt,money);
    	//??????????????????????????????????????????????????????????????????????????????
    	this.yearSubjectService.doSyncBudgetSubjectExecuteMoney(realSubjectIds,yearId,unitId,opt,money);
	}

	/**
	 * ??????????????????????????????????????????????????????:???????????????????????????
	 * @param id
	 */
	public List<BudgetSubject> getSortedByPidOrderBudgetSubjectList(BudgetSubject bean) {
		List<BudgetSubject> result = new ArrayList<BudgetSubject>();
		if(0 == bean.getParentid()) {
			result.add(bean);
			return result;
		}
		String[] everyIds = bean.getPids().split("-");
		List<String> idsList = Arrays.asList(everyIds);
		Collections.reverse(idsList);
		Map<String, Integer> locationMap = new HashMap<String, Integer>();
		for (int i = 0; i < idsList.size(); i++) {
			locationMap.put(idsList.get(i), i);
		}
		//??????????????????
		QueryWrapper<BudgetSubject> wrapper = new QueryWrapper<BudgetSubject>();
		wrapper.in("id", idsList);
		//update by minzhq ?????????????????? budget_month_subject?????????????????????
		//wrapper.eq("stopflag", 0);
		List<BudgetSubject> list = this.list(wrapper);
		//??????
		list.sort(new Comparator<BudgetSubject>() {

			@SuppressWarnings("unlikely-arg-type")
			@Override
			public int compare(BudgetSubject o1, BudgetSubject o2) {
				Integer one = locationMap.get(o1.getId().toString());
				Integer two = locationMap.get(o2.getId().toString());
				return one - two;
			}
		});
		return list;
	}
	
	/**
	 * ???????????????
	 * @param fromYearid ??????????????????
	 * @param toYearid ??????????????????
	 */
    int success = 0;
	public int initSubject(Long fromYearid,Long toYearid) {
	    List<BudgetSubject> bsList = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().eq("stopflag", 0).eq("yearid", fromYearid));
	    success = 0;
	    bsList.stream().filter(e->e.getParentid().toString().equals("0")).forEach(e->{
            
            BudgetSubject bs = this.bsMapper.selectOne(new QueryWrapper<BudgetSubject>().eq("subjectid", e.getSubjectid()).eq("yearid", toYearid));
            if(bs == null) {                
                bs = new BudgetSubject();
                try {
                    BeanUtils.copyProperties(e, bs);
                } catch (Exception e1) {                
                    e1.printStackTrace();
                    throw new RuntimeException("err");
                }
                bs.setId(null);
                bs.setStopflag(false);
                bs.setYearid(toYearid);
                success += this.bsMapper.insert(bs);
                bs.setPids(bs.getId().toString()+"-");
                this.bsMapper.updateById(bs);
                sync(bsList,e.getId(),bs,toYearid);
            }else{
                bs.setPids(bs.getId().toString()+"-");
                success += this.bsMapper.updateById(bs);
                sync(bsList,e.getId(),bs,toYearid);
            }
        });
	    return success;
	}
	
	/**
	 * ????????????/??????????????????
	 * @param bsList
	 * @param subjectid
	 * @param parentSubject
	 * @param toYearid
	 */
    private void sync(List<BudgetSubject> bsList,Long subjectid,BudgetSubject parentSubject,Long toYearid){
        bsList.stream().filter(c->c.getParentid().toString().equals(subjectid.toString())).forEach(c->{            
            BudgetSubject bs = this.bsMapper.selectOne(new QueryWrapper<BudgetSubject>().eq("subjectid", c.getSubjectid()).eq("yearid", toYearid));
            if(bs == null) {                
                bs = new BudgetSubject();
                try {
                    BeanUtils.copyProperties(c, bs);
                } catch (Exception e1) {                
                    e1.printStackTrace();
                    throw new RuntimeException("err");
                }
                bs.setId(null);
                bs.setStopflag(false);
                bs.setParentid(parentSubject.getId());              
                bs.setYearid(toYearid);
                success += this.bsMapper.insert(bs);
                bs.setPids(parentSubject.getPids()+bs.getId().toString()+"-");
                this.bsMapper.updateById(bs);
                sync(bsList,c.getId(),bs,toYearid);
            }else{
                bs.setPids(parentSubject.getPids()+bs.getId().toString()+"-");
                success += this.bsMapper.updateById(bs);
                sync(bsList,c.getId(),bs,toYearid);
            }
        });
    }
    
    public void exportJindie(Long yearId, HttpServletResponse response) throws Exception {
        
        if (null == yearId) {
            ResponseUtil.exportSubjectJindie(null, EasyExcelUtil.getOutputStream("??????????????????????????????", response), null);
            return;
        }
        List<Map<String, Object>> mapList = this.bsMapper.getJindieCodeByYearId(yearId);
        if (null == mapList || mapList.isEmpty()) {
            ResponseUtil.exportSubjectJindie(null, EasyExcelUtil.getOutputStream("??????????????????????????????", response), null);
            return;
        }
        List<List<String>> dataList = new ArrayList<>();
        String yearName = (String)mapList.get(0).get("period");
        for (Map<String, Object> data : mapList) {
            List<String> colList = new ArrayList<>();
            colList.add((String)data.get("code"));
            colList.add((String)data.get("name"));
            colList.add(null == data.get("jindiecode") ? "" : (String)data.get("jindiecode"));
            dataList.add(colList);
            
        }
        ResponseUtil.exportSubjectJindie(dataList, EasyExcelUtil.getOutputStream(yearName + "??????????????????", response), null);
        return;
    }
    
    public int importUpdateJindie(InputStream inputStream, Long yearId, List<JinDieCodeExcelData> errorList) {
        List<JinDieCodeExcelData> excelList = EasyExcelUtil.getExcelContent(inputStream, JinDieCodeExcelData.class);
        if (null == excelList || excelList.isEmpty()) {
            JinDieCodeExcelData excelData = new JinDieCodeExcelData();
            excelData.setErrMsg("????????????????????????????????????");
            errorList.add(excelData);
            return 0;
        }
        List<BudgetSubject> saveList = new ArrayList<>();
        for (JinDieCodeExcelData excelData : excelList) {
            BudgetSubject bean = this.bsMapper.selectOne(new QueryWrapper<BudgetSubject>().eq("name", excelData.getName().trim()).eq("yearid", yearId));
            if (null == bean) {
                excelData.setErrMsg("?????????????????????????????????");
                errorList.add(excelData);
                continue;
            }
            bean.setJindiename(excelData.getJindiename());
            bean.setJindiecode(excelData.getJindiecode());
            saveList.add(bean);
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

    public List<BudgetSubject> getSubjectInfoForAsset(HashMap<String, Object> paramMap) {

        List<BudgetSubject> list= bsMapper.getAssetSubjectInfo(paramMap);

        return list;
    }
}
