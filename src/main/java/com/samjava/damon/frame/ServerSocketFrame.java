package com.samjava.damon.frame;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * Server 처리를 위한 시작 클래스
 * @author 20150721
 */
public class ServerSocketFrame {
	
	private Socket theSocket;
	private ServerSocket theServerSocket;
	private int timeout = 3000;
	
	private ThreadPool threadPool;
		
	private InetAddress inet;
	private int port;
	private int minThread;
	private int maxThread;
	private String workClassName;
	private List<String> ignoreIplist = new ArrayList<String>();
	
	private Logger logger;
	/**
	 * @param inet Server IP
	 * @param port Server port
	 * @param workClassName WorkerThread를 상속받은 클래스 이름
	 * @throws ClassNotFoundException
	 */
	public ServerSocketFrame(InetAddress inet, int port, String workClassName) throws ClassNotFoundException {
		this.inet = inet;
		this.port = port;
		this.workClassName = workClassName;
		
		Class c = Class.forName(workClassName);
		
		for(Annotation a : c.getAnnotations()){
			if (a instanceof ServerConfig) {
				this.minThread = ((ServerConfig)a).minThread();
				this.maxThread = ((ServerConfig)a).maxThread();
				this.timeout = ((ServerConfig)a).timeout();
				System.out.println("this.minThread :" + this.minThread);
				System.out.println("this.maxThread :" + this.maxThread);
				System.out.println("this.timeout :" + this.timeout);
			} 
		}
		
		if (minThread <= 0 || maxThread <= 0) {
			throw new RuntimeException(workClassName + "class ServerConfig Annotations 을 찾을수 없습니다.예)@ServerConfig(minThread=50,maxThread=100,timeout=2000)");
		}
		if (minThread > maxThread) {
			throw new RuntimeException("minThread 값이 maxThread 작을수 없습니다.(" + minThread + "," + maxThread + ")");
		}		
		
	}	
	
	public void loadServerSocketFrame() throws Exception  {
		
		System.out.println("start loadServerSocketFrame");
		
		try {
			
//			PropertyConfigurator.configure("D:\\workspace_rse\\SamJavaServerFrame\\log4j.properties"); 
			
			if (System.getProperty("log4j.configuration") == null) { 
				System.out.println("-Dlog4j.configuration is null, default console ");
				BasicConfigurator.configure();
			} else {
				System.out.println("log4j.configuration:" + System.getProperty("log4j.configuration"));
				PropertyConfigurator.configure(System.getProperty("log4j.configuration")); 
			}
			
			
			this.logger = Logger.getLogger(this.getClass());
			
			
			if ("".equals(workClassName)) {
				throw new IllegalArgumentException("Worker class 이름이 정의 되어 있지 않습니다.");
			}
			
//			theServerSocket = new ServerSocket(this.port, -1, inet);
						
			theServerSocket = new ServerSocket();
			theServerSocket.setReuseAddress(true);
			theServerSocket.bind(new InetSocketAddress("localhost", 13233));
			
			
			
            threadPool = ThreadPool.init(this.minThread, this.maxThread, this.workClassName);
            
        	logger.info("minThread:" + minThread + ", maxThread:" + maxThread);
            logger.info(inet.toString() + ":" + port + " is ready ");
            System.out.println(inet.toString() + ":" + port + " is ready ");

            this.startServer();
            
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("서버 초기화중 오류 발생 : " + e);
			throw e;
		} 
	}
	
	private void startServer() {
		
		System.out.println("Server is started");
				
		InetAddress inet = null;
		String clientIp = null;
		
		Logger logger = Logger.getLogger(this.getClass());
		
		try {
			while (true) {
				
				theSocket = theServerSocket.accept();
				inet = theSocket.getInetAddress();
				clientIp = inet.getHostAddress();
				
				if (ignoreIplist.contains(clientIp)) {
					/* 헬스체크 하는 ip로 처리 불필요 */
					logger.trace("health check:" + clientIp);
					try { if (theSocket != null) theSocket.close(); } catch(Exception e) {}
					continue;
				}				
				
				logger.error(clientIp + " 접속");
								
				theSocket.setSoTimeout(this.timeout);
				
				service(theSocket);	
			}      
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("서버 동작중 오류 발생 : " + e);
		} 
	}
	
	private void service(Socket socket) {
		try {
			WorkerThread worker = threadPool.getThread(socket);
			worker.searchInfo(socket);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void setIgnoreIplist(List<String> ignoreIplist) {
		if (ignoreIplist == null) {
			return;
		}
		this.ignoreIplist = ignoreIplist;
	}

}
