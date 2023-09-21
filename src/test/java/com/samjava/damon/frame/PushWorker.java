package com.samjava.damon.frame;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import org.apache.log4j.Logger;


/**
 * @author sam
 */
@ServerConfig(minThread=10,maxThread=20,timeout=5000)
public class PushWorker extends WorkerThread {

	private final static int SIZE_FIELD = 4;

	private static Logger logger = Logger.getLogger(PushWorker.class.getName());
	
	public PushWorker(ThreadPool pool) {
		super(pool);
	}
	
	
	/**
	 * @param socket
	 * @throws Exception
	 * 실제 개발원 데이타 요청전문과 응답전문을 처리하는 메소드 
	 */
	protected void requestProcess(Socket socket) throws Exception {
		
		InetAddress inet = socket.getInetAddress();
		String clientIp = inet.getHostAddress();
		
		logger.error(this.getName() + " 전문 처리 시작 ");
		String message = "";
				
		byte[] sizeArr = null;
		byte[] data = null;
		byte[] merge = null;
		int size = 0;
		
				
		BufferedInputStream bufInput = null;
		BufferedOutputStream bufOutput  = null;
		
		try {
			
			socket.setReceiveBufferSize(40880);
			bufInput = new BufferedInputStream(socket.getInputStream());
			bufOutput  = new BufferedOutputStream(socket.getOutputStream());
			
			sizeArr = this.getData(PushWorker.SIZE_FIELD, bufInput);
			
			size = Integer.parseInt(new String(sizeArr).trim());
			data = this.getData(size, bufInput);
			
			String requestData = new String(data);
			
			logger.info("client 전문 :" + new String(sizeArr) + requestData);
			
			
			/* push 서비스 호출 */
			String result = "Y";
			
			String responseData = result + ";" + requestData;
			byte[] responseDataByte = responseData.getBytes();
			byte[] responseSizeByte = this.makeSizeFile(responseDataByte);			
			
			
			merge = mergeByte(responseSizeByte, responseDataByte);
			
			/* 응답을 client 에게 전송 */
			bufOutput.write(merge, 0, merge.length);
			bufOutput.flush();
			
		} catch(java.net.SocketTimeoutException socTimeOut) {
			logger.error(message + " 중 오류 발생 : (" + this.getName() + "): "+ socTimeOut);
			logger.error(new String(merge));
			
			throw socTimeOut;
		} catch(Exception e) {
			logger.error(message + " 중 오류 발생 : (" + this.getName() + "): "+ e);
			logger.error(new String(merge));
			throw e;
		} finally {
			try { bufInput.close(); } catch(Exception e) {}
			try { bufOutput.close(); } catch(Exception e) {}
			try { socket.close(); } catch(Exception e) {}
		}
		logger.info(this.getName() + " 전문 처리 종료  ");
	}
	
	
	
	/**
	 * 입력된 데이타의 길이를 SIZE_FIELD크기의 바이트 생성
	 * @param responseDataByte
	 * @return
	 */
	private byte[] makeSizeFile(byte[] responseDataByte) {
		byte[] result = new byte[this.SIZE_FIELD];
		Arrays.fill(result,(byte)' ');
		byte[] temp = String.valueOf(responseDataByte.length).getBytes();
		for ( int i = 0 ; i < temp.length ; i++)    {
			result[i] = temp[i];
		}
		return result;
	}
}
