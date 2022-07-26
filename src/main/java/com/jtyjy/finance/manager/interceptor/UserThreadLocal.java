package com.jtyjy.finance.manager.interceptor;

import com.jtyjy.finance.manager.bean.WbUser;

public class UserThreadLocal {
	
	private static final ThreadLocal<WbUser> LOCAL = new ThreadLocal<WbUser>();
	
	public static final void set(WbUser user) {
		LOCAL.set(user);
	}
	
	public static final void remove() {
		LOCAL.remove();
	}
	
	public static final WbUser get() {
		return LOCAL.get();
	}
	
	public static final String getEmpNo() {
		return get().getUserName();
	}
	
	public static final String getEmpName() {
		return get().getDisplayName();
	}
	

}
