package com.jtyjy.finance.manager.yk;

import com.iamxiongx.util.http.HttpUtil;
import com.jtyjy.core.spring.SpringTools;
import com.jtyjy.finance.manager.service.BudgetReimbursementorderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author minzhq
 */
@Slf4j
public class YkServerThread extends Thread {
	private BudgetReimbursementorderService budgetReimbursementService;
	private final SpringTools springTools;
	private Socket socket;
	private String ip;
	private String ykid;
	public YkServerThread(Socket socket,SpringTools springTools) {
		this.socket = socket;
		this.ip = socket.getInetAddress().getHostAddress();
		this.springTools = springTools;
	}
	private YkMsg getYkMsg(String msg) {
		int index = msg.indexOf("&");
		String idinfo = msg.substring(0, index);
		String id = idinfo.substring(idinfo.indexOf("=")+1);
		String codeinfo = msg.substring(index+1);
		String code = codeinfo.substring(codeinfo.indexOf("=")+1);
		YkMsg ykmsg = new YkMsg();
		ykmsg.setId(id);
		ykmsg.setCode(code);
		return ykmsg;
	}
	@SuppressWarnings("unused")
	@Override
	public void run() {
		if (null == socket) {
			return;
		}
		try {
			socket.sendUrgentData(0);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			is = socket.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
		} catch (IOException e) {
			e.printStackTrace();
			try {
				if (null != socket && socket.isConnected())
					socket.close();
			} catch (Exception e1) {
			} finally {
				is = null;
				isr = null;
				br = null;
			}
		}
		if(null==br) {
			return ;
		}
		String msg = "";
		String empno = "";
		int sendcount = 1;
		try {
			while ((msg=br.readLine())!=null) {
				log.info("======>msg:"+msg);
				if(StringUtils.isNotEmpty(msg)
						&& (msg.startsWith("GET") || msg.startsWith("get")) && (msg.endsWith("HTTP/1.1") || msg.endsWith("http/1.1"))
						&& msg.split(" ").length==3
				) {
					msg = msg.split(" ")[1];
					YkMsg ykmsg = getYkMsg(msg);
					ykmsg.setIp(getIp());
					try {
						empno = ykmsg.getId().toLowerCase().replaceAll("yk", "");
						empno = Integer.valueOf(empno).toString();
					}catch(Exception e) {}
					if(null!=springTools && null==budgetReimbursementService) {
						budgetReimbursementService = springTools.getBean(BudgetReimbursementorderService.class);
					}
					if("star".equalsIgnoreCase(ykmsg.getCode())) {
						this.ykid = ykmsg.getId();
						log.info("======>客户端[id:"+ykmsg.getId()+",ip:"+ykmsg.getIp()+"]已经连上："+ykmsg.getCode());
						if(null!=budgetReimbursementService && StringUtils.isNotEmpty(empno)) {
							budgetReimbursementService.sendQYWXTextMsg("你的扫描枪已经连接上服务器，如果不是本人操作请联系管理员。", empno);
						}
					}else {
						if(null!=budgetReimbursementService && 1==sendcount && StringUtils.isNotEmpty(empno)) {
							budgetReimbursementService.sendQYWXTextMsg("你的扫描枪正在被使用("+ykmsg.toString()+")，如果不是本人操作请联系管理员。", empno);
						}
						sendcount ++;
						if(sendcount >= 20) {
							sendcount = 1;
						}
						log.info("======>客户端[id:"+ykmsg.getId()+",ip:"+ykmsg.getIp()+"]准备干活了："+ykmsg.getCode());
						String code = ykmsg.getCode();
						if((code.startsWith("http") || code.startsWith("https")) && code.contains("?")) {
							String url = code.split("\\?")[0];
							String codeinfo = code.split("\\?")[1].split("&")[0];
							String c0 = codeinfo.split("=")[0];
							String c1 = codeinfo.split("=")[1];
							c1 = c1+"-"+empno;
							if(c1.startsWith("0-")) {
								c1 = "1" + c1.substring(1);
							}else {
								c1 = "1-" + c1;
							}
							String getResultstr = "";
							try {
								getResultstr = HttpUtil.doGet(url+"?"+c0+"="+c1, "");
							}catch(Exception e) {
								if(null!=budgetReimbursementService && StringUtils.isNotEmpty(empno)) {
									budgetReimbursementService.sendQYWXTextMsg("扫描失败："+ykmsg.toString(), empno);
								}
							}finally {
								log.info("======>结果[id:"+ykmsg.getId()+",ip:"+ykmsg.getIp()+",result:"+getResultstr+"]");
							}
						}else {
							log.info("======>客户端[id:"+ykmsg.getId()+",ip:"+ykmsg.getIp()+"]不是我的任务");
							if(null!=budgetReimbursementService && StringUtils.isNotEmpty(empno)) {

								budgetReimbursementService.sendQYWXTextMsg("扫描内容【"+msg+"】无法识别。", empno);
							}
						}
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			if(null != socket && socket.isConnected()) {
				System.out.println("--------------------------------:"+empno);
			}
			if(null!=springTools && null==budgetReimbursementService) {
				budgetReimbursementService = springTools.getBean(BudgetReimbursementorderService.class);
			}
			if(null!=budgetReimbursementService  && StringUtils.isNotEmpty(empno)) {
				budgetReimbursementService.sendQYWXTextMsg("你的扫描枪长时间未使用或者已经断开连接，如果有疑问请联系管理员。", empno);
			}
			try {
				br.close();
				isr.close();
				is.close();
				if (null != socket && socket.isConnected()) socket.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				is = null;
				isr = null;
				br = null;
				socket = null;
			}
		}
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getYkid() {
		return ykid;
	}
	public void setYkid(String ykid) {
		this.ykid = ykid;
	}
}
