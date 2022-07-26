package com.jtyjy.finance.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jtyjy.finance.manager.bean.BudgetProductCategory;
import com.jtyjy.finance.manager.mapper.BudgetProductCategoryMapper;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.bean.TabChangeLog;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.service.DefaultBaseService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 
 * 产品分类service
 * @author shubo
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager", rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BudgetProductCategoryService extends DefaultBaseService<BudgetProductCategoryMapper, BudgetProductCategory> {

	private final TabChangeLogMapper loggerMapper;
	
	private final BudgetProductCategoryMapper bpcMapper;

	@Override
	public BaseMapper<TabChangeLog> getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLogThreadLocal.set(TabChangeLog.getInstance("budget_product_category"));
	}
	   
    public String add(BudgetProductCategory bean) {
        int insertRet = 0;
        if (null == bean.getPid() || 0 == bean.getPid()) {
            bean.setLevel(1);
            bean.setFullname(bean.getName() + "-");
            bean.setPid(0l);
            bean.setPids("");
        }else {
            BudgetProductCategory fatherBean = this.bpcMapper.selectById(bean.getPid());
            if (null == fatherBean) {
                return "父id无效";
            }
            bean.setLevel(fatherBean.getLevel()+1);
            bean.setFullname(fatherBean.getFullname() + bean.getName() + "-");
            bean.setPids(fatherBean.getPids());
        }
        insertRet = this.bpcMapper.insert(bean);
        if (insertRet > 0) {
            bean.setPids(bean.getPids() + bean.getId() + "-");
            insertRet = this.bpcMapper.updateById(bean);
        }
        return "成功";
    }
    
    public String modify(BudgetProductCategory bean) {
        BudgetProductCategory originalBean = bpcMapper.selectById(bean.getId());
        if (null == originalBean) {
            return "id不存在";
        }
        if (0 == originalBean.getStopflag() && 1 == bean.getStopflag()) {
            //停用产品分类
            List<Map<String, Object>> subjectList = this.bpcMapper.getSubjectByPcId(bean.getId().toString());
            if (null != subjectList && !subjectList.isEmpty()) {
                return "该产品分类已被预算科目【" + subjectList.get(0).get("name") + "】等关联，不能进行停用；";
            }
        }
        String oldFullName = originalBean.getFullname();
        String newFullName = oldFullName.substring(0, oldFullName.indexOf(originalBean.getName())) + bean.getName() + "-";
        bean.setFullname(newFullName);
        bean.setPid(null);
        this.bpcMapper.updateById(bean);
        return "成功";
    }
    
	/**
	 * 移动产品分类
	 * @param id 要移动的id
	 * @param pid 新的父级id（最外层为0）
	 * @return
	 */
	public String movePdCategory(Long id,Long pid) {
	    if (null == id || null == pid) {
	        return "id和pid不能为空";
	    }
        if (id.equals(pid)) {
            return "id和pid不能相同";
        }	    
        if (0 == pid) {//最外层
            BudgetProductCategory originalBean = this.bpcMapper.selectById(id);
            originalBean.setLevel(1);
            originalBean.setFullname(originalBean.getName() + "-");
            originalBean.setPid(0l);
            originalBean.setPids(id + "-");
            this.bpcMapper.updateById(originalBean);
            return "成功";
        }else {
            BudgetProductCategory originalBean = this.bpcMapper.selectById(id);
            BudgetProductCategory fatherBean = this.bpcMapper.selectById(pid);
            if (null == originalBean || null == fatherBean) {
                return "id或pid无效";
            }
            if (1 == fatherBean.getStopflag()) {
                return fatherBean.getName() + "已停用，不能添加子产品分类";
            }
            originalBean.setLevel(fatherBean.getLevel() + 1);
            originalBean.setFullname(fatherBean.getFullname() + originalBean.getName() + "-");
            originalBean.setPid(pid);
            originalBean.setPids(fatherBean.getPids() + id + "-");
            updateSonPids(originalBean);
            this.bpcMapper.updateById(originalBean);
            return "成功";
        }
	}

	/**
	 * 递归更新子级
	 * @param pc
	 */
    private void updateSonPids(BudgetProductCategory pc) {
        List<BudgetProductCategory> pcs = this.bpcMapper.selectList(new QueryWrapper<BudgetProductCategory>().eq("pid", pc.getId()));
        if (null != pcs && pcs.size() > 0) {
            for (BudgetProductCategory tmpPc : pcs) {
                tmpPc.setLevel(pc.getLevel() + 1);
                tmpPc.setFullname(pc.getFullname() + tmpPc.getName() + "-");
                tmpPc.setPids(pc.getPids() + tmpPc.getId() + "-");
                this.bpcMapper.updateById(tmpPc);
                updateSonPids(tmpPc);
            }
        }
    }
	/**
	 * 查询产品分类
	 * @param name
	 * @param unitId 
	 * @param stopflag
	 * @return
	 */
	public List<BudgetProductCategory> getPdCategoryInfo(String name, Long unitId, Integer stopflag) {
	    String pcIds = "";
	    if (null != unitId) {
            pcIds = getPcIds(unitId);
        }
	    return this.bpcMapper.getPdCategoryInfo(name, pcIds, stopflag);
	}

    public String getPcIds(Long unitId) {
        String pcIds;
        List<String> pidList = this.bpcMapper.getPidListByUnitId(unitId);
        if(null != pidList && pidList.size() > 0) {
            Set<String> idSet = new HashSet<>();
            for(String pids : pidList) {
                for(String _pid : pids.split(",")) {
                    idSet.add(_pid);
                    getSonIds(Long.valueOf(_pid), idSet);
                }
            }
            pcIds = idSet.stream().collect(Collectors.joining(","));
        }else {
            pcIds = "0";
        }
        return pcIds;
    }


    /**
     * 递归获取子级
     * @param pc
     */
    private void getSonIds(Long pid, Set<String> idSet) {
        List<BudgetProductCategory> pcs = this.bpcMapper.selectList(new QueryWrapper<BudgetProductCategory>().eq("pid", pid));
        if (null != pcs && pcs.size() > 0) {
            for (BudgetProductCategory tmpPc : pcs) {
                idSet.add(tmpPc.getId().toString());
                getSonIds(tmpPc.getId(), idSet);
            }
        }
    }
}
