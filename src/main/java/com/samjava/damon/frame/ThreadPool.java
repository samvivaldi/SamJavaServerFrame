package com.samjava.damon.frame;

import java.lang.reflect.Constructor;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;


/**
 * WorkerThread 담고 있는 Thread pool
 * @author sam
 */
public class ThreadPool {

	private final ArrayList queue = new ArrayList();
	
	private AtomicInteger theAtomicInteger;
	private static ThreadPool threadPool = null;
	
	
	private static Logger logger = Logger.getLogger(ThreadPool.class.getName());
	
	private int minPoolSize = 0;
	private int maxPoolSize = 0;
	private String workClassName;
	
    /**
     * synchronized 제거 
     * @return
     * @throws Exception
     */
    public static ThreadPool getInstance() throws Exception {
    	return ThreadPool.threadPool;
    }
    		
    /**
     * @param minPoolSize
     * @param maxPoolSize
     * @return
     * @throws Exception
     */
    static ThreadPool init(int minPoolSize, int maxPoolSize, String workClassName) throws Exception {
    	if (ThreadPool.threadPool == null) {
    		ThreadPool.threadPool = new ThreadPool(minPoolSize, maxPoolSize, workClassName);
    	}
    	
    	return ThreadPool.threadPool;
    }
    
	
	private ThreadPool(int minPoolSize, int maxPoolSize, String workClassName) throws Exception {
		this.minPoolSize = minPoolSize;
		this.maxPoolSize = maxPoolSize;
		this.workClassName = workClassName;
    		
		System.out.println("minPoolSize " + minPoolSize);	
		System.out.println("maxPoolSize " + maxPoolSize);	
		System.out.println("workClassName " + workClassName);	
		
		/* pool size 만큼 thread 를 생성하여 ThreadPool에 저장한다 */
		for (int index = 0; index < minPoolSize; index++) {
 
			WorkerThread thread = this.makeObject();
			
			thread.setName("Worker" + (index + 1));
			thread.start();
			queue.add(thread);
			logger.info(thread.getName() + " 초기화 완료");
		}
		
		theAtomicInteger = new AtomicInteger(minPoolSize);
		
	}
		
	/**
	 * @param socket
	 * @return
	 * @throws Exception
	 * ThreadPool에서 Thread를 가져오는 메소드 
	 */
	public synchronized WorkerThread getThread(Socket socket) throws Exception {
		
		WorkerThread worker = null;
		
		if (queue.size() > 0) {
			worker = (WorkerThread) queue.remove(0);
			logger.info("남은 쓰레드 : " + queue.size());
		} else {

			int threadCount = theAtomicInteger.incrementAndGet();
			if (threadCount <= maxPoolSize) {

				worker = this.makeObject();
			
				worker.setName("Worker" + threadCount + "_" + System.currentTimeMillis());
		
				logger.info("min size 초과후 " + threadCount + " 만큼 생성중");
				
				worker.start();	
				
				return worker;
			} else {
				return waitQueue(socket);
			}
		}
	
		return worker;
	}

	/**
	 * @param socket
	 * @return
	 * @throws Exception
	 * ThreadPool에 Thread가 없거나 Max만큼 생성하였을 경우 해당 요청을 대기하도록 하는 메소드 
	 * 대기시간을 초과할경우 요청 socket를 close한다. 
	 */
	private WorkerThread waitQueue(Socket socket) throws Exception {

		WorkerThread returnWorker = null;
		
		long start = System.currentTimeMillis();
		long end = 0L;


		while (queue.isEmpty()) {

			end = System.currentTimeMillis();
			
			if ( queue.isEmpty() && (end - start) >= 1000 ) {
				/* 큐에 남은 Thread가 없고 대기시간을 초가하였을 경우 종료 */
				logger.info("큐 대기시간 초과  timeout(" + 1000 + ")");  

				if (socket != null) try {socket.close(); } catch(Exception e) {}

				throw new Exception("큐 대기시간 초과  timeout");
			}
			
			try { wait(1 * 300); } catch (InterruptedException ignored) { ;;; }
						
		}	
		
		returnWorker = (WorkerThread) queue.remove(0);
		
		logger.info("큐 대기시간 중 리턴"); 			
		return returnWorker;
	}

	/**
	 * @param thread
	 *  사용이 끝난 Thread를 반환하는 메소드 
	 */
	public synchronized void putThread(WorkerThread thread) {
		if (queue.size() < this.minPoolSize) {
			/* queue의 사이즈가 min 사이즈보다 적으면 */
				queue.add(thread);
				notifyAll();
		} else if (theAtomicInteger.intValue() >= this.minPoolSize) {
			/* index가 min 사이즈보다 크면 자원을 해제시킨다.  */
			thread.kill();
			theAtomicInteger.decrementAndGet();		
		}
	}

	private WorkerThread makeObject() throws Exception {
		Class c = Class.forName(this.workClassName);
		Class[] paraType = new Class[]{ThreadPool.class};
		Constructor theConstructor = c.getConstructor(paraType);	
		
		Object[] initPara = new Object[] {this};
		
		return (WorkerThread)theConstructor.newInstance(initPara);
	}

}
