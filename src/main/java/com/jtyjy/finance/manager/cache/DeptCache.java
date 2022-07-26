package com.jtyjy.finance.manager.cache;


import com.jtyjy.finance.manager.bean.WbDept;
import com.jtyjy.finance.manager.service.WbDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门缓存
 * @author minzhq
 */
@SuppressWarnings("all") 
@Component
public class DeptCache extends BaseCache{
	
	@Autowired
	private WbDeptService service;
	
	/**
	 * 部门缓存
	 */
	public static Map<String, WbDept> DEPT_MAP = new HashMap<String, WbDept>();

	public static WbDept getByDeptId(String key) {
		return DEPT_MAP.get(key);
	}

	public static Collection<WbDept> getAllDept(){
		return DEPT_MAP.values();
	}
	
	@Override
	public void cache() throws Exception {
		this.doJob(DEPT_MAP);
	}


	@Override
	public void recache() throws Exception {
		Map<String, WbDept> dept_map = new HashMap<String, WbDept>();
		this.doJob(dept_map);
		DEPT_MAP = dept_map;
	}

	private void doJob(Map<String, WbDept> deptMap) throws Exception {
		String sql = "SELECT * FROM wb_dept";
		List<WbDept> depts = service.query(sql, WbDept.class);
		setMapByList(depts, deptMap, "deptId", WbDept.class);
	}
}
