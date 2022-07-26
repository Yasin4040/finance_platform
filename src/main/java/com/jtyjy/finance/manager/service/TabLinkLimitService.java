package com.jtyjy.finance.manager.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jtyjy.core.interceptor.BaseUser;
import com.jtyjy.core.interceptor.LoginThreadLocal;
import com.jtyjy.core.local.DefaultChangeLogThreadLocal;
import com.jtyjy.core.log.DefaultChangeLog;
import com.jtyjy.core.service.DefaultBaseService;
import com.jtyjy.finance.manager.bean.TabDm;
import com.jtyjy.finance.manager.bean.TabLinkLimit;
import com.jtyjy.finance.manager.mapper.TabChangeLogMapper;
import com.jtyjy.finance.manager.mapper.TabLinkLimitMapper;

/**
 * 环节条件表
 */
@Service
@Transactional(transactionManager = "defaultTransactionManager")
@SuppressWarnings("all")
public class TabLinkLimitService extends DefaultBaseService<TabLinkLimitMapper,TabLinkLimit>{

	@Autowired
	private TabChangeLogMapper loggerMapper;

    @Autowired
    private TabLinkLimitMapper llMapper;
    
    @Autowired
    private TabDmService dmService;
    
	@Override
	public BaseMapper getLoggerMapper() {
		return loggerMapper;
	}
	
	@Override
	public void setBaseLoggerBean() {
		DefaultChangeLog theLog = new DefaultChangeLog();
		BaseUser user = LoginThreadLocal.get();
		theLog.setTableName("");
		//当前登录人的主键
		theLog.setOperatorId(user != null ? user.getEmpid().toString() : "1");
		//当前登录人的名称
		theLog.setUsername(user != null ? user.getEmpname() : "系统管理员");
		//当前登录人的其他信息，如工号
		theLog.setOperatorName(user != null ? user.getEmpno() : "10001");
		//日志创建时间
		theLog.setCreateTime(System.currentTimeMillis());
		DefaultChangeLogThreadLocal.set(theLog);
	}
	
    public boolean judgeLinkLimit(Long procedureId, Long subjectId, String linkDm, String subjectName, Double amount){
        Page<TabLinkLimit> pageCond = new Page<>(1, 100);
        List<TabLinkLimit> list = llMapper.getLinkLimitInfo(procedureId, subjectId, linkDm, subjectName, pageCond);
        if (null == list || list.isEmpty()) {
            return true;//无条件默认不做任何判断，即此环节默认进行
        }else {
            TabLinkLimit result = list.get(0);//默认只有一个启用的过程，一个过程下的一个环节的条件为一条记录
            Double minLimit = result.getMinLimit();
            Double maxLimit = result.getMaxLimit();
            if (amount > minLimit) {//金额大于最小限制
                if (null == maxLimit) {//金额小于最大限制
                    return true;//符合条件，执行
                }else if(amount < maxLimit){
                    return true;
                }else {
                    return false;
                }
                
            }else {
                return false;//不符合条件，不执行
            }
        }
    }
	
	public Page<TabLinkLimit> getLinkLimitInfo(Integer page, Integer rows, Long procedureId, Long subjectId, String linkDm, String subjectName){
	    Page<TabLinkLimit> pageCond = new Page<>(page, rows);
	    List<TabLinkLimit> list = llMapper.getLinkLimitInfo(procedureId, subjectId, linkDm, subjectName, pageCond);
	    List<TabDm> dmList = this.dmService.selectAllSubType("link");
        for(TabLinkLimit bean : list) {
	        bean.setLinkName(getLinkName(bean.getLinkDm(), dmList));
	    }
        pageCond.setRecords(list);
	    return pageCond;
	}
	
    /**
     * 获取环节名称
     * @param linkStr
     * @return
     */
    private String getLinkName(String linkStr, List<TabDm> dmList) {
        if (StringUtils.isBlank(linkStr)) {
            return "";
        }
        String[] linkArr = linkStr.split(",");
        StringJoiner linkName = new StringJoiner(",");
        Map<String, String> linkMap = dmList.stream().collect(Collectors.toMap(e->{return e.getDm();}, TabDm::getDmName,(ele1,ele2) -> ele1));
        
        for (String linkCode : linkArr) {
            if (StringUtils.isNotBlank(linkCode)) {
                String dmName = linkMap.get(linkCode);
                if (StringUtils.isNotBlank(dmName)) {
                    linkName.add(dmName);
                }
            }
        }
        return linkName.toString();
    }
    
	public boolean checkData(TabLinkLimit bean, StringBuffer errMsg) {
      if (null == bean) {
          errMsg.append("数据不能为空！");
          return false;
      }
      Page<TabLinkLimit> same = this.getLinkLimitInfo(1, 100, bean.getProcedureId(), bean.getSubjectId(), bean.getLinkDm(), null);
      List<TabLinkLimit> sameList = same.getRecords(); 
      if(null == bean.getId() || 0 == bean.getId().intValue()) {
          if (null != sameList && !sameList.isEmpty()) {
              errMsg.append("环节【" + sameList.get(0).getLinkName() + "】下已存在科目【" + sameList.get(0).getSubjectName() + "】的环节限制！");
              return false;
          }
          
      }else {
          if (null != sameList  && !sameList.isEmpty() && !sameList.get(0).getId().equals(bean.getId())) {
              errMsg.append("环节【" + sameList.get(0).getLinkName() + "】下已存在科目【" + sameList.get(0).getSubjectName() + "】的环节限制！");
              return false;
          }
      }
      return true;
  }
	
	/**
	 * 根据流程主键和预算科目集查询条件
	 * @param pid
	 * @param subjectIds
	 * @return
	 */
	public List<TabLinkLimit> getByPidAndSubjectIds(Long pid, Set<Long> subjectIds){
		QueryWrapper<TabLinkLimit> query = new QueryWrapper<TabLinkLimit>();
		query.eq("procedure_id", pid);
		query.in("subject_id", subjectIds);
		query.eq("is_active", 1);
		return this.list(query);
	}

	public void initLimit() {
		List<Map<String,Object>> list1 = this.llMapper.list1();
		List<Map<String,Object>> list2 = this.llMapper.list2();

		list1.forEach(l->{
			String max_limit = l.get("max_limit").toString();
			String subject_id = l.get("subject_id").toString();
			this.llMapper.updateLimit(max_limit,subject_id,"financial_manage_check");
		});

		list2.forEach(l->{
			String max_limit = l.get("max_limit").toString();
			String subject_id = l.get("subject_id").toString();
			this.llMapper.updateLimit(max_limit,subject_id,"general_manager_check");
		});
	}
}
