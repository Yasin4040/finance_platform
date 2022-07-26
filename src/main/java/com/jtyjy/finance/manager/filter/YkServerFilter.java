package com.jtyjy.finance.manager.filter;

import com.jtyjy.core.spring.SpringTools;
import com.jtyjy.finance.manager.yk.YkServer;

import javax.servlet.*;
import java.io.IOException;

public class YkServerFilter implements Filter {

	private SpringTools springTools;
	private int port;

	public YkServerFilter(SpringTools springTools, int port) {
		this.springTools = springTools;
		this.port = port;
	}
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//new YkServer(port,springTools).start();
	}
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		chain.doFilter(request,response);
	}
	@Override
	public void destroy() {
	}
}
