package com.jtyjy.finance.manager.yk;

/**
 * @author minzhq
 */
public class YkArg {
	public static int port = 90;
	private boolean dubug = true;
	private String url = "";
	private String prefix = "GET";
	private String postfix = "HTTP/1.1";
	private String split = " ";
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public boolean isDubug() {
		return dubug;
	}
	public void setDubug(boolean dubug) {
		this.dubug = dubug;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getPostfix() {
		return postfix;
	}
	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}
	public String getSplit() {
		return split;
	}
	public void setSplit(String split) {
		this.split = split;
	}
	@Override
	public String toString() {
		return String.format("YkArg[%s,%s,%s,%s,%s,%s]", port+"",dubug+"",url,prefix,postfix,split);
	}
}
