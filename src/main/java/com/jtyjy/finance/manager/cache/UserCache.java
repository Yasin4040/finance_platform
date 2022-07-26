
package com.jtyjy.finance.manager.cache;

import com.jtyjy.finance.manager.bean.WbUser;
import com.jtyjy.finance.manager.service.WbUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Component
public class UserCache extends BaseCache{
	
	@Autowired
	private WbUserService userService;
	
	/**
	 * 人员缓存
	 */
	public static Map<String, WbUser> USERID_MAP = new HashMap<String, WbUser>();

	public static Map<String,WbUser> EMPNO_USER_MAP = new HashMap<String,WbUser>();
	

	public static WbUser getUserByUserId(String userId) {
		return USERID_MAP.get(userId);
	}
	
	public static WbUser getUserByEmpNo(String empNo) {
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
		String sql = "SELECT * FROM wb_user";
		List<WbUser> users = userService.query(sql, WbUser.class);
		setMapByList(users, EMPNO_USER_MAP, "userName", WbUser.class);
		setMapByList(users, USERID_MAP, "userId", WbUser.class);
	}
	
}
