package com.samjava.damon.frame;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.Arrays;

public class PushClientUtil {
	
	/**
	 * push service 요청 메소드
	 * 2015.07.08 sam
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	public static String sendToPushService(String msg) throws Exception {
		
		String result = null;
		
		String ip = "192.168.189.145";
		int port = 13233;
		
		int fieldSize = 4;
		
		Socket socket = null;
		BufferedInputStream bufInput = null;
		BufferedOutputStream bufOutput = null;
		
		try {
			
			socket = new Socket(ip, port);
			socket.setReceiveBufferSize(4096);
			socket.setSoTimeout(10000);
			
			bufInput = new BufferedInputStream(socket.getInputStream());
			bufOutput  = new BufferedOutputStream(socket.getOutputStream());			
			
			byte[] requestDataByte = msg.getBytes();
			
			byte[] requestSizeByte = PushClientUtil.makeSizeFile(requestDataByte, fieldSize);
			
			byte[] requestByte = PushClientUtil.mergeByte(requestSizeByte, requestDataByte);
			
			bufOutput.write(requestByte, 0, requestByte.length);
			bufOutput.flush();
			
			
			byte[] sizeArr = PushClientUtil.getData(fieldSize, bufInput);
			int responseDataSize = Integer.parseInt(new String(sizeArr).trim());
			
			byte[] responseDataByte = PushClientUtil.getData(responseDataSize, bufInput);
			
			result = new String(responseDataByte);
			
			
		} catch(Exception e) {
			throw e;
		} finally {
			try {bufInput.close(); } catch(Exception e) {}
			try {bufOutput.close(); } catch(Exception e) {}
			try {socket.close(); } catch(Exception e) {}
		}		
		
		return result;
	}
	
	/**
	 * @param size
	 * @param inBuf
	 * @return
	 * @throws Exception
	 */
	private static byte[] getData(int size, BufferedInputStream inBuf) throws Exception {
		byte[] result = new byte[size];
		int read_size = 0;
		int total_read = 0;

		do {
			read_size = inBuf.read(result, total_read, size - total_read);
			total_read += read_size;
		} while (total_read < size && read_size != -1);    			
			
		return result;
	}	
	
	/**
	 * 입력된 데이타의 길이를 SIZE_FIELD크기의 바이트 생성
	 * @param responseDataByte
	 * @param size
	 * @return
	 */
	private static byte[] makeSizeFile(byte[] responseDataByte, int size) {
		byte[] result = new byte[size];
		Arrays.fill(result,(byte)' ');
		byte[] temp = String.valueOf(responseDataByte.length).getBytes();
		for ( int i = 0 ; i < temp.length ; i++)    {
			result[i] = temp[i];
		}
		return result;
	}

	/**
	 * byte 합치기
	 * @param data1
	 * @param data2
	 * @return
	 */
	private static byte[] mergeByte(byte[] data1, byte[] data2) {
		byte[] result = new byte[data1.length + data2.length];;
		
		System.arraycopy(data1, 0, result, 0, data1.length);
		System.arraycopy(data2, 0, result, data1.length, data2.length);
		
		return result;
	} 	
	
	private PushClientUtil() {
		;;;;
	}

}
