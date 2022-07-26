
package com.jtyjy.finance.manager.cache;

import com.jtyjy.finance.manager.bean.WbPerson;
import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.service.WbPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class PersonCache extends BaseCache{
	
	@Autowired
	private WbPersonService personService;
	

	public static Map<String, WbPerson> USERID_MAP = new HashMap<String, WbPerson>();

	public static Map<String,WbPerson> EMPNO_USER_MAP = new HashMap<String,WbPerson>();
	

	public static WbPerson getPersonByUserId(String userId) {
		return USERID_MAP.get(userId);
	}
	
	public static WbPerson getPersonByEmpNo(String empNo) {
		return EMPNO_USER_MAP.get(empNo);
	}
	

	@Override
	public void cache() throws Exception {
		this.doJob();
	}


	@Override
	public void recache() throws Exception {
		this.doJob();
	}
	

	private void doJob() throws Exception {
		String sql = "SELECT * FROM wb_person";
		List<WbPerson> users = personService.query(sql, WbPerson.class);
		setMapByList(users, USERID_MAP, "userId", WbPerson.class);
		setMapByList(users, EMPNO_USER_MAP, "personCode", WbPerson.class);
	}
	
}
