package com.jtyjy.finance.manager.yk;

import com.jtyjy.core.spring.SpringTools;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * @author minzhq
 */
@Slf4j
public class YkServer extends Thread{
	private ServerSocket serverSocket = null;
	private final SpringTools springTools;
	public YkServer(int port,SpringTools springTools) {
		this.springTools = springTools;
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		if(null==serverSocket) {
			return ;
		}
		try {
			log.info("======>YK 启动服务成功。");
			log.info("======>等待客户端连接。");
			Socket socket;
			while(true) {
				socket = serverSocket.accept();
				log.info("客户端："+socket.getInetAddress().getHostAddress()+" 连接上了服务器(*^__^*) 嘻嘻……");
				new YkServerThread(socket,springTools).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.error("启动服务失败：%s",e.getMessage());
		}
	}
}

