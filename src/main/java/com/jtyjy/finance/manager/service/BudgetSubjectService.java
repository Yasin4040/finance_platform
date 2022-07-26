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
     * <p>Description: 科目列表</p>  
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
     * 重新排序，得到树形结构（同级按排序号排序，同一父节点的子级在父级后面）
     * @param datas 数据库查询到的集合，按层级、父节点id、排序号依次排序
     * @return
     */
    public List<SubjectInfoVO> getTreeList(List<SubjectInfoVO> datas) {
        int currentLevel = 1;//当前遍历元素的层级
        int currentParentIndex = 0;//当前遍历元素父节点所在位置
        List<SubjectInfoVO> newList = new ArrayList<>();
        for(SubjectInfoVO vo : datas) {//遍历原集合
            if ( 1 == vo.getLevel()) {//最上级
                newList.add(0, vo);//将降序改为升序
            }else if (currentLevel == vo.getLevel()) {//非最上级且当前元素和上一元素同层级
                vo.setName(getEmptyStr(currentLevel) + vo.getName());//子级前面增加空格
                if (newList.get(currentParentIndex - 1).getId().equals(vo.getParentid())) {//当前元素和上一元素父级相同
                    newList.add(currentParentIndex, vo);//sql查询是按orderno降序排序，前端插入又改为升序了
                }else {//当前元素和上一元素父级不同，重新获取当前元素的父级
                    for(int i = 0; i < newList.size(); i++) {
                        if(newList.get(i).getId().equals(vo.getParentid())) {//遍历新集合，获取当前元素父节点在新集合中的坐标
                            currentParentIndex = i + 1;
                            newList.add(currentParentIndex, vo);//往当前元素父节点后插入当前元素
                            break;
                        }
                    }
                }
            }else {//非最上级且当前元素和上一元素不同层级
                currentLevel = vo.getLevel();//层级更新
                vo.setName(getEmptyStr(currentLevel) + vo.getName());//子级前面增加空格
                for(int i = 0;i < newList.size(); i++) {//遍历新集合，获取当前元素父节点在新集合中的坐标
                    if(newList.get(i).getId().equals(vo.getParentid())) {
                        currentParentIndex = i+1;
                        newList.add(currentParentIndex, vo);//往当前元素父节点后插入当前元素
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
            emptyStr = emptyStr + "　";
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
//	          throw new RuntimeException("年度预算已启动，不能添加预算科目");
//	      }
	    checkbudgetsubjectname(budgetSubject.getYearid(), budgetSubject.getName(), null);
	    checkbudgetsubjectcode(budgetSubject.getYearid(), budgetSubject.getCode(), null);
	    checkbudgetsubject(budgetSubject.getYearid(), budgetSubject.getSubjectid());
        budgetSubject.setId(null);
        budgetSubject.setParentid(0L);
        //名字为空取 基本科目的名字
        BudgetBaseSubject budgetBaseSubject = this.bbsMapper.selectById(budgetSubject.getSubjectid());
        BudgetYearPeriod budgetYearPeriod = this.bypMapper.selectById(budgetSubject.getYearid());
        if(StringUtils.isEmpty(budgetSubject.getName())) {
            budgetSubject.setName(budgetBaseSubject.getName());
        }
        budgetSubject.setFirstspell(PinyinTools.getFirstspell(budgetSubject.getName()));
        budgetSubject.setFullspell(PinyinTools.getPinYin(budgetSubject.getName()));
        budgetSubject.setCode(budgetBaseSubject.getCode());
        //码洋占比科目
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
            throw new RuntimeException("新增失败，分解科目、公式科目、产品科目属性互斥，不能同时存在。");
        }
        //公式科目
        if(!budgetSubject.getFormulaflag())budgetSubject.setFormula(null);
        else {
            if(StringUtils.isEmpty(budgetSubject.getFormula())) {
                throw new RuntimeException("请填写计算公式。");
            }
            /*if(budgetSubject.getUpsumflag()) {
                throw new RuntimeException("公式科目不能设置为向上汇总。");
            }*/
            BigDecimal dd = null;
            try {
                dd = js(budgetSubject.getFormula().replace("[this]", "(0)").trim(),null);
            }catch(Exception e) {}
            if(null!=dd) {
                throw new RuntimeException("计算公式不能为常量。");
            }
            
            try {
                jse.eval(formulareplace(budgetSubject.getFormula()));
            } catch (ScriptException e) {
                e.printStackTrace();
                throw new RuntimeException("计算公式有误。");
            }
            //辅助
            //budgetSubject.setAssistflag(false);
            //拆借
            budgetSubject.setCostlendflag(false);
            //分解
            budgetSubject.setCostsplitflag(false);
            //关联产品
            budgetSubject.setJointproductflag(false);
            budgetSubject.setProcategoryid(null);
            //budgetSubject.set
        }
        //产品科目
        BudgetProductCategory budgetProductCategory = null;
        List<BudgetProductCategory> budgetProductCategorys = null;
        if(!budgetSubject.getJointproductflag()) budgetSubject.setProcategoryid(null);
        else{
            if(null == budgetSubject.getProcategoryid()) {
                 throw new RuntimeException("请选择产品分类.");
            }
            //budgetProductCategory = (BudgetProductCategory) oneEntityManagerByFk(budgetSubject.getProcategoryid(), BudgetProductCategory.class);
            if (StringUtils.isNotBlank(budgetSubject.getProcategoryid())) {
                String[] procategoryArr = budgetSubject.getProcategoryid().split(",");
                List procategoryList = Arrays.asList(procategoryArr);
                budgetProductCategorys = this.bpcMapper.selectBatchIds(procategoryList);
            }
            //拆借
            //budgetSubject.setCostlendflag(false);
            //分解
            //budgetSubject.setCostsplitflag(false);
            //公式
            budgetSubject.setFormulaflag(false);
            budgetSubject.setFormula(null);
        }
        //分解
        if(budgetSubject.getCostsplitflag()) {
            //辅助
            //budgetSubject.setAssistflag(false);
            //拆借
            budgetSubject.setCostlendflag(false);
            //公式科目
            budgetSubject.setFormulaflag(false);
            budgetSubject.setFormula(null);
            //关联产品
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
     * <p>Description: 修改科目</p>  
     * @param basesubject
     * @return  
     * @see com.jtyjy.budget.service.BudgetSubjectService#updatesubject(com.jtyjy.budget.model.BudgetBaseSubject)
     */
    public void updatesubject(BudgetSubject budgetSubject) {
        //System.out.println("--------------budgetSubject:"+JSON.toJSONString(budgetSubject));
        BudgetSubject oldbudgetSubject = this.bsMapper.selectById(budgetSubject.getId());
        if (null == oldbudgetSubject) {
            throw new RuntimeException("预算科目id[" + budgetSubject.getId() +"]不存在。");
        }
        BudgetSubject newbudgetSubject = new BudgetSubject();
        BeanUtils.copyProperties(oldbudgetSubject, newbudgetSubject);
        BeanUtils.copyProperties(budgetSubject, newbudgetSubject);
        //使用标志
        if(StringUtils.isNotEmpty(budgetSubject.getName())) {
            checkbudgetsubjectname(budgetSubject.getYearid(), budgetSubject.getName(), budgetSubject.getId());
        }
        if(StringUtils.isNotEmpty(budgetSubject.getCode())) {
            checkbudgetsubjectcode(budgetSubject.getYearid(), budgetSubject.getCode(), budgetSubject.getId());
        }
        
        BudgetBaseSubject budgetBaseSubject = this.bbsMapper.selectById(newbudgetSubject.getSubjectid());
        BudgetYearPeriod budgetYearPeriod = this.bypMapper.selectById(newbudgetSubject.getYearid());
        //名称有改变
        if(StringUtils.isNotEmpty(budgetSubject.getName())) {
            newbudgetSubject.setFirstspell(PinyinTools.getFirstspell(newbudgetSubject.getName()));
            budgetSubject.setFullspell(PinyinTools.getPinYin(budgetSubject.getName()));
        }
        //科目有改变
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
            throw new RuntimeException("修改失败，分解科目、公式科目、产品科目属性互斥，不能同时存在。");
        }
        if (oldbudgetSubject.getFormulaflag() != newbudgetSubject.getFormulaflag() 
                || oldbudgetSubject.getJointproductflag() != newbudgetSubject.getJointproductflag()
                    || oldbudgetSubject.getCostsplitflag() != newbudgetSubject.getCostsplitflag()) {
            //修改了公式/产品/分解标志
            Integer count = this.byaMapper.countYearSubject(newbudgetSubject.getYearid(), newbudgetSubject.getId(), null);
            if(count > 0) {
                throw new RuntimeException("当前科目存在动因，不允许修改预算科目类型!");
            }
            
        }
        BudgetUnitSubject busEntity = new BudgetUnitSubject();
        
        //公式科目
        if(!newbudgetSubject.getFormulaflag()) {
            newbudgetSubject.setFormula(null);
            busEntity.setFormula("");
            busMapper.update(busEntity,new UpdateWrapper<BudgetUnitSubject>().eq("subjectid", newbudgetSubject.getId()));
            
        }else {
            
            if(StringUtils.isEmpty(newbudgetSubject.getFormula())) {
                throw new RuntimeException("请填写计算公式。");
            }
            if(!oldbudgetSubject.getLeafflag()) {
                throw new RuntimeException("非叶子科目不能设置计算公式。");
            }
            /*
            if(newbasesubject.getUpsumflag()) {
                throw new RuntimeException("公式科目不能设置为向上汇总。");
            }*/
            BigDecimal dd = null;
            try {
                dd = js(newbudgetSubject.getFormula().replace("[this]", "(0)").trim(),null);
            }catch(Exception e) {}
            if(null!=dd) {
                throw new RuntimeException("计算公式不能为常量。");
            }
            try {
                jse.eval(formulareplace(newbudgetSubject.getFormula()));
            } catch (ScriptException e) {
                e.printStackTrace();
                throw new RuntimeException("计算公式有误。");
            }
            //辅助
            //newbasesubject.setAssistflag(false);
            //拆借
            newbudgetSubject.setCostlendflag(false);
            //追加
            newbudgetSubject.setCostaddflag(false);
            //分解
            newbudgetSubject.setCostsplitflag(false);
            //关联产品
            newbudgetSubject.setJointproductflag(false);
            newbudgetSubject.setProcategoryid(null);
            busEntity.setFormula(newbudgetSubject.getFormula());
            busMapper.update(busEntity,new UpdateWrapper<BudgetUnitSubject>().eq("subjectid", newbudgetSubject.getId()));
            //budgetSubject.set 
            
            bmaMapper.delete(new UpdateWrapper<BudgetMonthAgent>().eq("yearid", newbudgetSubject.getYearid()).eq("subjectid", newbudgetSubject.getId()));
            byaMapper.delete(new UpdateWrapper<BudgetYearAgent>().eq("yearid", newbudgetSubject.getYearid()).eq("subjectid", newbudgetSubject.getId()));
            
        }
        //产品科目
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
                    //删除这个产品的动因
                    for(BudgetProduct product:products) {
                        //long count = countEntityManager(" select count(0)  FROM budget_year_agent _agent INNER JOIN budget_unit _unit ON _agent.yearid = _unit.yearid AND _agent.unitid = _unit.id WHERE _agent.yearid=? AND _agent.subjectid=? AND _agent.productid=? AND _unit.requeststatus > 0 ", new Object[] {oldbudgetSubject.getYearid(),oldbudgetSubject.getId(),product.getId()});
//                      long count = countEntityManager(" select count(0)  FROM budget_year_agent _agent INNER JOIN budget_unit _unit ON _agent.yearid = _unit.yearid AND _agent.unitid = _unit.id WHERE _agent.yearid=? AND _agent.subjectid=? AND _agent.productid=? AND _agent.total > 0 ", new Object[] {oldbudgetSubject.getYearid(),oldbudgetSubject.getId(),product.getId()});
//                      if(count > 0) {
//                          throw new RuntimeException("所选科目设置的产品已经使用，不能修改。");
//                      }
                        try {
                            //executeUpdate("delete from budget_unit_product WHERE productid=? AND unitid in (SELECT id FROM budget_unit WHERE yearid=?) ",new Object[] {product.getId(),oldbudgetSubject.getYearid()});
                            bmaMapper.delete(new UpdateWrapper<BudgetMonthAgent>().eq("yearid", oldbudgetSubject.getYearid()).eq("subjectid", oldbudgetSubject.getId()));
                            byaMapper.delete(new UpdateWrapper<BudgetYearAgent>().eq("yearid", oldbudgetSubject.getYearid()).eq("subjectid", oldbudgetSubject.getId()));
                            
                            
                        }catch(Exception e) {
                            throw new RuntimeException("所选科目设置的产品已经使用，不能修改。");
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
                throw new RuntimeException("请选择产品分类.");
            }
            if(!oldbudgetSubject.getLeafflag()) {
                throw new RuntimeException("非叶子科目不能设置产品。");
            }
            busEntity.setProcategoryid(budgetSubject.getProcategoryid());
            busMapper.update(busEntity, new UpdateWrapper<BudgetUnitSubject>().eq("subjectid", newbudgetSubject.getId()));
            
            if(StringUtils.isNotEmpty(oldbudgetSubject.getProcategoryid())) {
                //
                for(String id:oldbudgetSubject.getProcategoryid().split(",")) {
                    if(!(","+newbudgetSubject.getProcategoryid()+",").contains(id)) {
                        List<BudgetProduct> products =  bpMapper.getPdInfoByCid(id);
                        //删除这个产品的动因
                        for(BudgetProduct product:products) {
                            Integer count = this.byaMapper.countYearSubject(oldbudgetSubject.getYearid(), oldbudgetSubject.getId(), product.getId());
                            if(count > 0) {
                                throw new RuntimeException("所选产品分类已经被使用，不能修改。");
                            }
                            try {
                                //executeUpdate("delete from budget_unit_product WHERE productid=? AND unitid in (SELECT id FROM budget_unit WHERE yearid=?) ",new Object[] {product.getId(),oldbudgetSubject.getYearid()});
                                bmaMapper.delete(new UpdateWrapper<BudgetMonthAgent>().eq("yearid", oldbudgetSubject.getYearid()).eq("subjectid", oldbudgetSubject.getId()).eq("productid", product.getId()));
                                byaMapper.delete(new UpdateWrapper<BudgetYearAgent>().eq("yearid", oldbudgetSubject.getYearid()).eq("subjectid", oldbudgetSubject.getId()).eq("productid", product.getId()));
                            }catch(Exception e) {
                                throw new RuntimeException("所选产品分类已经被使用，不能修改。");
                            }
                        }
                    }
                }
            }else{
                Integer count = this.byaMapper.countYearSubject(newbudgetSubject.getYearid(), newbudgetSubject.getId(), null);
                if(count > 0) {
                    throw new RuntimeException("所选科目已做预算，不允许修改为产品科目!");
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
            //拆借
            //newbasesubject.setCostlendflag(false);
            //追加
            //newbasesubject.setCostaddflag(false);
            //分解
            newbudgetSubject.setCostsplitflag(false);
            //公式
            newbudgetSubject.setFormulaflag(false);
            newbudgetSubject.setFormula(null);
        }
        
        if(newbudgetSubject.getCostlendflag() && !oldbudgetSubject.getLeafflag()) {
            if(!oldbudgetSubject.getLeafflag()) {
                throw new RuntimeException("非叶子科目不能设置拆借。");
            }
        }
        if(newbudgetSubject.getCostaddflag() && !oldbudgetSubject.getLeafflag()) {
            if(!oldbudgetSubject.getLeafflag()) {
                throw new RuntimeException("非叶子科目不能设置追加。");
            }
        }
        //分解
        if(newbudgetSubject.getCostsplitflag()) {
            if(!oldbudgetSubject.getLeafflag()) {
                throw new RuntimeException("非叶子科目不能设置分解。");
            }
            //辅助
            //newbasesubject.setAssistflag(false);
            //拆借
            //newbasesubject.setCostlendflag(false);
            //公式科目
            newbudgetSubject.setFormulaflag(false);
            newbudgetSubject.setFormula(null);
            //关联产品
            newbudgetSubject.setJointproductflag(false);
            newbudgetSubject.setProcategoryid(null);
            budgetProductCategory = null;
        }else {
            List<String> unitName = busMapper.getUnitNameBySubId(newbudgetSubject.getId());
            if(null != unitName && !unitName.isEmpty()) {
                throw new RuntimeException("预算单位【"+unitName.get(0)+"】等已有预算科目【"+newbudgetSubject.getName()+"】的分解权限。");         
            }
            //BudgetSubject subject_ = (BudgetSubject) oneEntityManagerByFk(Long.valueOf(myid), BudgetSubject.class);
            //throw new RuntimeException("预算单位【"+unit_.getName()+"】已有预算科目【"+subject_.getName()+"】的分解权限。");
            //executeUpdate(" Update budget_unit_subject SET splitflag = 0 WHERE subjectid = ?  ", new Object[] {newbasesubject.getId()});
        }
        if (!oldbudgetSubject.getStopflag() && newbudgetSubject.getStopflag()) {//若修改停用，更新所有子级为停用状态
            Integer count = this.byaMapper.countYearSubject(newbudgetSubject.getYearid(), newbudgetSubject.getId(), null);
            if(count > 0) {
                throw new RuntimeException("当前科目存在动因，不允许停用!");
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
                    throw new RuntimeException("科目【" + mysubject.getName() + "】存在动因，不允许停用父级!");
                }
                mysubject.setStopflag(true);
                this.bsMapper.updateById(mysubject);
                updateSonStopflag(mysubject);
            } 
        }
    }
    
    /**
    * <p>Description:删除预算科目</p>
    * @param deleteParams
    * @Note 已在当前届别预算单位中勾选的预算科目不允许删除，直接做物理删除
    * <p>Author: ldw</p>
    * <p>Date:2019年3月26日</p>
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
            if (subject.getParentid() == 0) { //根节点
                topSubs.add(subject);
            }
        }
        List<String> Ids = new ArrayList<String>(Arrays.asList(ids));
        List<String> subIds = new ArrayList<String>();
        if (null != topSubs && topSubs.size() > 0) { //根节点
            for (BudgetSubject topSubject : topSubs) {
                List<BudgetSubject> subLists = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().like("pids", topSubject.getId()+"-"));
                for (BudgetSubject bdgSbt : subLists) {
                    subIds.add(bdgSbt.getId().toString());
                    if (isInBgtunit_subject(bdgSbt.getSubjectid())) {
                        throw new RuntimeException("【"+bdgSbt.getName()+"】科目已在预算单位中设置，无法删除！");
                    }else {
                        this.bsMapper.deleteById(bdgSbt.getId());
                    }   
                }           
            }
        }
        if (null != subIds && subIds.size() >0) {//剔除根节点及其相应子节点
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
            if (!subject.getLeafflag()) {//剔除根节点后的非叶子节点
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
                    throw new RuntimeException("【"+budgetSubject.getName()+"】科目已在预算单位中设置，无法删除！");
                }else {
                    //entityManager.remove(budgetSubject);

                    this.bsMapper.deleteById(budgetSubject.getId());
                }   
            }
        }
        if (null != subIds2 && subIds2.size() > 0) {
            for (String id : subIds2) {//剔除非叶子节点及其子节点
                if (Ids.contains(id)) {
                    Ids.remove(id);
                }
            }
        }
        for (String id : Ids) {
            BudgetSubject subject = this.bsMapper.selectById(Long.valueOf(id));
            if (isInBgtunit_subject(subject.getSubjectid())) {
                throw new RuntimeException("【"+subject.getName()+"】科目已在预算单位中设置，无法删除！");
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
     * <p>Description: 更新上级id</p>  
     * @param id
     * @param pid  
     * @see com.jtyjy.budget.service.BudgetSubjectService#updateparentid(java.lang.String, java.lang.String)
     */
    public void updateparentid(Integer id, Integer pid) {
        if (null == id || null == pid) {
            throw new RuntimeException("id和pid不能为空");
        }
        if (id.equals(pid)) {
            throw new RuntimeException("id和pid不能为同一个");
        }
        BudgetSubject budgetSubject = this.bsMapper.selectById(Long.valueOf(id));
        if(null == budgetSubject) {
            return ;
        }
        /*BudgetYearStartup yearstartup = this.bysMapper.selectOne(new QueryWrapper<BudgetYearStartup>().eq("yearid", budgetSubject.getYearid()));
        if(yearstartup.isStartbudgetflag()) {
            throw new RuntimeException("年度预算已启动，不能修改预算科目。");
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
                throw new RuntimeException("科目["+pbudgetSubject.getName()+"]已停用，不能添加子科目。");
            }
            if(pbudgetSubject.getCostaddflag()) {
                throw new RuntimeException("科目["+pbudgetSubject.getName()+"]设置了追加，不能为非叶子科目。");
            }
            if(pbudgetSubject.getCostlendflag()) {
                throw new RuntimeException("科目["+pbudgetSubject.getName()+"]设置了拆借，不能为非叶子科目。");
            }
            if(pbudgetSubject.getCostsplitflag()) {
                throw new RuntimeException("科目["+pbudgetSubject.getName()+"]设置了分解，不能为非叶子科目。");
            }
            if(pbudgetSubject.getJointproductflag()) {
                throw new RuntimeException("科目["+pbudgetSubject.getName()+"]设置了产品，不能为非叶子科目。");
            }
            if(pbudgetSubject.getFormulaflag()) {
                throw new RuntimeException("科目["+pbudgetSubject.getName()+"]设置了公式，不能为非叶子科目。");
            }
            Integer agentcount = this.byaMapper.selectCount(new QueryWrapper<BudgetYearAgent>().eq("subjectid", Long.valueOf(pid)));
            if(agentcount > 0) {
                throw new RuntimeException("科目["+pbudgetSubject.getName()+"]已做预算，不能为非叶子科目。");
            }
            //新的上级节点为非叶子节点
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
                throw new RuntimeException("数据不对。");
            }
            
            /**
             * 存在这种情况：update by minzhq
             *  所拖动的科目已经被预算单位使用。
             *  这时候预算单位也要将此拖动科目的父级也要勾中。
             */
            List<BudgetUnitSubject> unitSubjectList = busMapper.selectList(new QueryWrapper<BudgetUnitSubject>().eq("subjectid", id));
            List<Long> parentUnitIds = busMapper.getUnitIdsBySubjectId(Long.valueOf(pid));//防止父科目已有对应单位，添加导致错误
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
     * <p>Description: 更新排序号</p>  
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
    
    //检查名字
    private void checkbudgetsubjectname(Long yearid, String name, Long orgId) {
        List<BudgetSubject> result = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().eq("yearid", yearid).eq("name", name));
        if(null != result && result.size() > 0) {
            if (null != orgId && orgId.equals(result.get(0).getId())) {//原名称未修改
                return ;
            }else {
                throw new RuntimeException("预算科目名称【" + name + "】已存在");
            }
        }
    }
    //检查code
    private void checkbudgetsubjectcode(Long yearid, String code, Long orgId) {
        List<BudgetSubject> result = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().eq("yearid", yearid).eq("code", code));
        if(null != result && result.size() > 0) {
            if (null != orgId && orgId.equals(result.get(0).getId())) {//原编号未修改
                return ;
            }else {
                throw new RuntimeException("预算科目编号【" + code + "】已存在");
            }
        }
    }
    private void checkbudgetsubject(Long yearid, Long subjectid) {
        List<BudgetSubject> result = this.bsMapper.selectList(new QueryWrapper<BudgetSubject>().eq("yearid", yearid).eq("subjectid", subjectid));
        if(null != result && result.size() > 0) {
            throw new RuntimeException("所属基础科目已存在");
        }
    }
    
	//js 计算公式
    public final ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript"); 
    public final String formulareplace(String formula) {
        String numberstr = "0123456789";
        if(StringUtils.isEmpty(formula)) {
                return "";
        }else {
                if(formula.startsWith(".") || formula.endsWith(".")) {
                    throw new RuntimeException("计算公式错误");
                }
                for(int i = 0;i < formula.length();i++) {
                    if(".".equals(formula.charAt(i)+"") && (
                            !numberstr.contains((formula.charAt(i+1)+""))  
                            || !numberstr.contains((formula.charAt(i-1)+""))) ) {
                        throw new RuntimeException("计算公式错误。");
                    }
                }
                int index_ = formula.indexOf("[");
                int _index = formula.indexOf("]");
                while(index_ >= 0 && _index >= 0) {
                    if(index_ > 1) {
                        String tmp = formula.substring(index_ - 1, index_);
                        if("]".equals(tmp) || ".".equals(tmp) || numberstr.contains(tmp)) {
                            throw new RuntimeException("计算公式错误。");
                        }
                    }
                    if(_index < formula.length()) {
                        String tmp = formula.substring(_index, _index + 1);
                        if("[".equals(tmp) || ".".equals(tmp) || numberstr.contains(tmp)) {
                            throw new RuntimeException("计算公式错误。");
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
     * <p>Description:计算</p>
     * @param formula  公式
     * @param self     本身值
     * @return
     * @throws NumberFormatException
     * @throws ScriptException 
     * @return Float
     * @Note
     * <p>Author: <a href="https://gitee.com/iamxiongx" target="_blank">xiongx</a></p>
     * <p>Date: 2018年11月1日 下午5:27:24</p>
     * <p>Version: 1.0</p>
     */
    public final BigDecimal js(String formula,Float self) throws NumberFormatException, ScriptException {
        if(null!=self) {
            formula = formula.replace("[this]", "("+self.toString()+")");
        }
        return new BigDecimal(jse.eval("("+formula+").toFixed(4)").toString());
    }
	  
    /**
     * 同步预算部门下指定预算科目的执行数，会自动向上汇总
     * @param yearId 界别主键
     * @param unitId 预算单位主键
     * @param monthId 月份主键
     * @param subjectId 待同步的预算科目主键
     * @param opt 1：追加 2：执行 3：拆进 4：拆出
     * @param money 金额
     */
    public void syncBudgetSubjectExecuteMoney(long yearId, long unitId,long monthId, long subjectId, int opt, BigDecimal money) {
    	BudgetSubject subject = this.getById(subjectId);
    	if(subject == null) {
    		return;
    	}
    	//获取待同步且按照父子顺序排序的预算科目
    	List<BudgetSubject> subjectList = this.getSortedByPidOrderBudgetSubjectList(subject);
    	//同步月度科目执行数
    	this.syncBudgetSubjectExecuteMoney(subjectList,yearId,unitId,monthId,opt,money);
    }
   
    /**
     * 同步预算部门月度科目执行数
     * @param subjectList 按照父子顺序排好序的预算科目
     * @param yearId 
     * @param unitId
     * @param monthId
     * @param opt 
     * @param money
     */
    private void syncBudgetSubjectExecuteMoney(List<BudgetSubject> subjectList, long yearId, long unitId, long monthId,int opt, BigDecimal money) {
    	//排除掉从第一个不向上汇总的预算科目
    	if(subjectList == null || subjectList.size() == 0) {
    		return;
    	}
    	List<Long> realSubjectIds = new ArrayList<Long>();
    	for (BudgetSubject ele : subjectList) {
            /**
             * update by minzhq
             * 去掉这个限制。  可能会由于先走单子，后面禁用，导致数据没有。
             */
    		//if(!ele.getStopflag()) {
    			realSubjectIds.add(ele.getId());
    		//}
    		if(!ele.getUpsumflag()) {
    			break;
    		}
		}
    	//按照预算科目主键，年度，月度，预算单位查询预算单位月度预算科目
    	this.monthSubjectService.doSyncBudgetSubjectExecuteMoney(realSubjectIds,yearId,unitId,monthId,opt,money);
    	//按照预算科目主键，年度，预算单位查询预算单位预算科目
    	this.yearSubjectService.doSyncBudgetSubjectExecuteMoney(realSubjectIds,yearId,unitId,opt,money);
	}

	/**
	 * 根据预算科目查询按照父级排好序的集合:数据为从顶级到本级
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
		//根据趋散查询
		QueryWrapper<BudgetSubject> wrapper = new QueryWrapper<BudgetSubject>();
		wrapper.in("id", idsList);
		//update by minzhq 防止科目禁用 budget_month_subject表数据不会累加
		//wrapper.eq("stopflag", 0);
		List<BudgetSubject> list = this.list(wrapper);
		//排序
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
	 * 初始化科目
	 * @param fromYearid 数据来源届别
	 * @param toYearid 要更新的届别
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
	 * 递归新增/修改子级科目
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
            ResponseUtil.exportSubjectJindie(null, EasyExcelUtil.getOutputStream("导入金蝶科目代码模板", response), null);
            return;
        }
        List<Map<String, Object>> mapList = this.bsMapper.getJindieCodeByYearId(yearId);
        if (null == mapList || mapList.isEmpty()) {
            ResponseUtil.exportSubjectJindie(null, EasyExcelUtil.getOutputStream("导入金蝶科目代码模板", response), null);
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
        ResponseUtil.exportSubjectJindie(dataList, EasyExcelUtil.getOutputStream(yearName + "金蝶科目代码", response), null);
        return;
    }
    
    public int importUpdateJindie(InputStream inputStream, Long yearId, List<JinDieCodeExcelData> errorList) {
        List<JinDieCodeExcelData> excelList = EasyExcelUtil.getExcelContent(inputStream, JinDieCodeExcelData.class);
        if (null == excelList || excelList.isEmpty()) {
            JinDieCodeExcelData excelData = new JinDieCodeExcelData();
            excelData.setErrMsg("表格解析失败或无有效数据");
            errorList.add(excelData);
            return 0;
        }
        List<BudgetSubject> saveList = new ArrayList<>();
        for (JinDieCodeExcelData excelData : excelList) {
            BudgetSubject bean = this.bsMapper.selectOne(new QueryWrapper<BudgetSubject>().eq("name", excelData.getName().trim()).eq("yearid", yearId));
            if (null == bean) {
                excelData.setErrMsg("届别下不存在此科目名称");
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
