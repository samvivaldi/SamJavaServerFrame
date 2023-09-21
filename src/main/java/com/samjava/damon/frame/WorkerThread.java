package com.samjava.damon.frame;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.apache.log4j.Logger;


/**
 * ServerSocket로 들어온 Socket 처리하는 Thread
 * @author sam
 */
public abstract class WorkerThread extends Thread{				

	 
	protected ThreadPool pool;
	
	protected Socket socket;
	protected boolean isKill = false;
	
	private static Logger logger = Logger.getLogger(WorkerThread.class.getName());
	
	
    /**
     * @param pool
     * @param server
     * 생성자 
     */
    public WorkerThread(ThreadPool pool) {  
		this.pool = pool; 
		
	}

   
    public void run() {
    	
    	logger.info("Thread : " + getName() + " is ready");
    	
	    while (true) {
	    	
			if (isKill == true) break;
			
//			try {
//				if (socket == null) this.wait();
//			} catch (InterruptedException e) {       
//				logger.error("thread interrrupt", e);
//			}

			
			synchronized (this) {
			    if (socket == null) {
			        try {
			            this.wait();
			        } catch (InterruptedException e) {
			            logger.error("thread interrrupt", e);
			        }
			    }
			}	
			
			if (socket == null) {
				System.out.println("socket is  null ???  " + getName());
				pool.putThread(this);
				continue;
			}
			
			try {
				requestProcess(socket);
			} catch(Exception e) {
				logger.error("requestProcess 오류발생", e);				
			} finally {
				
				try { if (socket!= null && !socket.isClosed()) socket.close(); } catch(Exception e) {}
				
				socket = null;			
				pool.putThread(this);
			}	
	    }
	}		
	
	/**
	 * @param socket
	 * 접속된 socket을 넘겨 주며 wait된 Thread를 notify하여 요청을 처리하도록 하는 메소드 
	 */
	public synchronized void searchInfo(Socket socket) {
		this.socket = socket;
		notifyAll();
		
	}
	
	/**
	 * @param socket ServerSocket.accept() 의 리턴값(clinet와 연결된 socket)
	 * @throws Exception
	 */
	protected abstract void requestProcess(Socket socket) throws Exception;
	
	
	/**
	 * @param sizeArr
	 * @param inBuf
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 * 사이즈 만큼 inputStream을 받아오는 메소드 
	 */
//	protected byte[] getData(int size, BufferedInputStream inBuf) throws Exception {
//			
//		byte[] result = new byte[size];
//		int read_size = 0;
//		int total_read = 0;
//
//		do {
//			read_size = inBuf.read(result, total_read, size - total_read);
//			total_read += read_size;
//		} while (total_read < size && read_size != -1);    			
//			
//		return result;
//	}
	
	protected byte[] getData(int len, InputStream in) throws IOException {
        int bcount = 0;
        int n = 0;
        int read_retry_count = 0;
        byte buf[] = new byte[len];
        while(bcount < len) {
            n = in.read(buf, bcount, len - bcount);
            if(n > 0) {
            	bcount += n;
            } else if(n == -1) {
            	throw new IOException("Inputstream has returned an unexpected EOF");
            } else if(n == 0 && ++read_retry_count == 5) {
            	throw new IOException("Inputstream-read-retry-count exceed !");
            }
        }
        return buf;
    }	
	
	/**
	 * @param size
	 * @param data
	 * @return
	 * 두개의 바이트 배열을 하나의 바이트 배열로 만드는 메소드 
	 */
	protected byte[] mergeByte(byte[] size, byte[] data) {
		byte[] result = new byte[size.length + data.length];;
		
		System.arraycopy(size, 0, result, 0, size.length);
		System.arraycopy(data, 0, result, size.length, data.length);
		
		return result;
		
	}

	/**
	 * 해당 스레드를 종료 하고자 할 경우 호출 하는 메소드 
	 */
	public void kill() {
		this.isKill = true;
	}
	
}
