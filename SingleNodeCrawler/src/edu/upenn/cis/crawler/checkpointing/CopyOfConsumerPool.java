package edu.upenn.cis.crawler.checkpointing;

import java.util.ArrayList;


/**
 * 
 * Class to create a threadpool of threads to push different data to S3
 *
 */

public class CopyOfConsumerPool {

	static CopyOfConsumer[] cArr1;
	static CopyOfConsumer[] cArr2;
	static CopyOfConsumer[] cArr3;
	static CopyOfConsumer[] cArr4;
	static CopyOfConsumer[] cArr5;
	static CopyOfConsumer[] cArr6;
	static CopyOfConsumer[] cArr7;

	final ArrayList<String> queue1;
	final ArrayList<String> queue2;
	final ArrayList<String> queue3;
	final ArrayList<String> queue4;
	final ArrayList<String> queue5;
	final ArrayList<String> queue6;
	final ArrayList<String> queue7;

	Object lock1;
	Object lock2;
	Object lock3;
	Object lock4;
	Object lock5;
	Object lock6;
	Object lock7;

	String bucketName1;
	String bucketName2;
	String bucketName3;
	String bucketName4;
	String bucketName5;
	String bucketName6;
	String bucketName7;

	int queueLimit;
	int threadPoolSize;

	static volatile boolean shutdown1 = false;
	static volatile boolean shutdown2 = false;
	static volatile boolean shutdown3 = false;
	static volatile boolean shutdown4 = false;
	static volatile boolean shutdown5 = false;
	static volatile boolean shutdown6 = false;
	static volatile boolean shutdown7 = false;

	static volatile double count1 = 0;
	static volatile double count2 = 0;
	static volatile double count3 = 0;
	static volatile double count4 = 0;
	static volatile double count5 = 0;
	static volatile double count6 = 0;
	static volatile double count7 = 0;
	

	static volatile double maxLimit;

	public CopyOfConsumerPool(ArrayList<String> queue1,
			ArrayList<String> queue2, ArrayList<String> queue3,
			ArrayList<String> queue4, ArrayList<String> queue5,
			ArrayList<String> queue6, ArrayList<String> queue7, String bucketName1, String bucketName2,
			String bucketName3, String bucketName4, String bucketName5,
			String bucketName6, String bucketName7, Object lock1, Object lock2, Object lock3,
			Object lock4, Object lock5, Object lock6, Object lock7, int queueLimit,
			double maxLimit, int threadPoolSize) {

		this.queue1 = queue1;
		this.queue2 = queue2;
		this.queue3 = queue3;
		this.queue4 = queue4;
		this.queue5 = queue5;
		this.queue6 = queue6; // backup thread for snapshotting
		this.queue7 = queue7;

		this.bucketName1 = bucketName1;
		this.bucketName2 = bucketName2;
		this.bucketName3 = bucketName3;
		this.bucketName4 = bucketName4;
		this.bucketName5 = bucketName5;
		this.bucketName6 = bucketName6;
		this.bucketName7 = bucketName7;

		this.lock1 = lock1;
		this.lock2 = lock2;
		this.lock3 = lock3;
		this.lock4 = lock4;
		this.lock5 = lock5;
		this.lock6 = lock6;
		this.lock7 = lock7;

		this.queueLimit = queueLimit;
		CopyOfConsumerPool.maxLimit = maxLimit;
		this.threadPoolSize = threadPoolSize;

		cArr1 = new CopyOfConsumer[threadPoolSize];
		cArr2 = new CopyOfConsumer[threadPoolSize];
		cArr3 = new CopyOfConsumer[threadPoolSize];
		cArr4 = new CopyOfConsumer[threadPoolSize];
		cArr5 = new CopyOfConsumer[threadPoolSize];
		cArr6 = new CopyOfConsumer[threadPoolSize];
		cArr7 = new CopyOfConsumer[threadPoolSize];
		

		for (int i = 0; i < cArr1.length; i++) {
			cArr1[i] = new CopyOfConsumer(this.queue1, this.lock1,
					this.queueLimit, this.bucketName1, 1);
			cArr1[i].setName("StorageThread_1" + i);
			cArr1[i].setPriority(Thread.MAX_PRIORITY);
			System.out.println(cArr1[i].getName());
		}

		for (int i = 0; i < cArr2.length; i++) {
			cArr2[i] = new CopyOfConsumer(this.queue2, this.lock2,
					this.queueLimit, this.bucketName2, 2);
			cArr2[i].setName("StorageThread_2" + i);
			cArr2[i].setPriority(Thread.MAX_PRIORITY);
			System.out.println(cArr2[i].getName());
		}

		for (int i = 0; i < cArr3.length; i++) {
			cArr3[i] = new CopyOfConsumer(this.queue3, this.lock3,
					this.queueLimit, this.bucketName3, 3);
			cArr3[i].setName("StorageThread_3" + i);
			cArr3[i].setPriority(Thread.MAX_PRIORITY);
			System.out.println(cArr3[i].getName());
		}

		for (int i = 0; i < cArr4.length; i++) {
			cArr4[i] = new CopyOfConsumer(this.queue4, this.lock4,
					this.queueLimit, this.bucketName4, 4);
			cArr4[i].setName("StorageThread_4" + i);
			cArr4[i].setPriority(Thread.MAX_PRIORITY);
			System.out.println(cArr4[i].getName());
		}

		for (int i = 0; i < cArr5.length; i++) {
			cArr5[i] = new CopyOfConsumer(this.queue5, this.lock5, queueLimit,
					this.bucketName5, 5);
			cArr5[i].setName("StorageThread_5" + i);
			cArr5[i].setPriority(Thread.MAX_PRIORITY);
			System.out.println(cArr5[i].getName());
		}

		/*for (int i = 0; i < cArr6.length; i++) {
			cArr6[i] = new CopyOfConsumer(this.queue6, this.lock6, queueLimit,
					this.bucketName6, 6);
			cArr6[i].setName("StorageThread_6" + i);
			cArr6[i].setPriority(Thread.MAX_PRIORITY);
			System.out.println(cArr6[i].getName());
		}
		
		for (int i = 0; i < cArr7.length; i++) {
			cArr7[i] = new CopyOfConsumer(this.queue7, this.lock7, queueLimit,
					this.bucketName7, 7);
			cArr7[i].setName("StorageThread_7" + i);
			cArr7[i].setPriority(Thread.MAX_PRIORITY);
			System.out.println(cArr7[i].getName());
		}*/
	}

	public void start() {
		// thread pool size for all is equals
		for (int i = 0; i < cArr1.length; i++) {
			cArr1[i].start();
			cArr2[i].start();
			cArr3[i].start();
			cArr4[i].start();
			cArr5[i].start();
			//cArr6[i].start();
			//cArr7[i].start();
		}
	}

	/*
	 * public CopyOfConsumerPool(ArrayList<String> queue, Object lock, int
	 * threadPoolSize, int queueLimit, String bucketName, double maxLimit) {
	 * this.queue = queue; this.lock = lock; this.queueLimit = queueLimit;
	 * this.threadPoolSize = threadPoolSize; this.bucketName = bucketName;
	 * CopyOfConsumerPool.maxLimit = 0; CopyOfConsumerPool.count = 0; cArr = new
	 * Consumer[threadPoolSize]; for (int i = 0; i < cArr.length; i++) { cArr[i]
	 * = new Consumer(this.queue, lock, queueLimit, bucketName);
	 * cArr[i].setName("StorageThread" + i);
	 * 
	 * }
	 * 
	 * }
	 * 
	 * public void start() { for (int i = 0; i < cArr.length; i++) {
	 * cArr[i].start(); } }
	 */

	public synchronized void shutdown(int poolNo) {
		switch (poolNo) {
		case 1:
			shutdown1 = true;
			shutdown(cArr1);
			break;
		case 2:
			shutdown2 = true;
			shutdown(cArr2);
			break;
		case 3:
			shutdown3 = true;
			shutdown(cArr3);
			break;
		case 4:
			shutdown4 = true;
			shutdown(cArr4);
			break;
		case 5:
			shutdown5 = true;
			shutdown(cArr5);
			break;
		case 6:
			shutdown6 = true;
			shutdown(cArr6);
			break;
		
		case 7:
		shutdown7 = true;
		shutdown(cArr7);
		break;
	}
	}

	public synchronized static void shut(int poolNo) {
		switch (poolNo) {
		case 1:
			shutdown1 = true;
			shut(cArr1);
			break;
		case 2:
			shutdown2 = true;
			shut(cArr2);
			break;
		case 3:
			shutdown3 = true;
			shut(cArr3);
			break;
		case 4:
			shutdown4 = true;
			shut(cArr4);
			break;
		case 5:

			shutdown5 = true;
			shut(cArr5);
			break;
		case 6:
			shutdown6 = true;
			shut(cArr6);
			break;
		
		case 7:
			shutdown7 = true;
			shut(cArr7);
			break;
		}
	}

	public synchronized static double getCount(int poolNo) {
		double count = 0;
		switch (poolNo) {
		case 1:
			count = count1;
			break;
		case 2:
			count = count2;
			break;
		case 3:
			count = count3;
			break;
		case 4:
			count = count4;
			break;
		case 5:
			count = count5;
			break;
		case 6:
			count = count6;
			break;
			
		case 7:
			count = count7;
			break;
		}
		return count;
	}

	public synchronized static void incrementCount(int poolNo) {

		switch (poolNo) {
		case 1:
			count1++;
			break;
		case 2:
			count2++;
			break;
		case 3:
			count3++;
			break;
		case 4:
			count4++;
			break;
		case 5:
			count5++;
			break;
		case 6:
			count6++;
			break;
		case 7:
			count7++;
			break;
		}
	}

	public synchronized static boolean getShutdown(int poolNo) {
		boolean flag = false;
		switch (poolNo) {
		case 1:
			flag = shutdown1;
			break;
		case 2:
			flag = shutdown2;
			break;
		case 3:
			flag = shutdown3;
			break;
		case 4:
			flag = shutdown4;
			break;
		case 5:
			flag = shutdown5;
			break;
		case 6:
			flag = shutdown6;
			break;
		case 7:
			flag = shutdown7;
			break;
		}
		return flag;
	}
	
	public synchronized static void setShutdown(int poolNo) {
		switch (poolNo) {
		case 1:
			shutdown1 = true;
			break;
		case 2:
			shutdown2 = true;
			break;
		case 3:
			shutdown3 = true;
			break;
		case 4:
			shutdown4 = true;
			break;
		case 5:
			shutdown5 = true;
			break;
		case 6:
			shutdown6 = true;
			break;
		case 7:
			shutdown7 = true;
			break;
		}
	}

	public synchronized void shutdown(CopyOfConsumer[] cArr) {
		System.out.println("================================================");
		System.out.println("Shutting down consumer thread pool ...");
		System.out.println("================================================");
		for (int i = 0; i < cArr.length - 2 /* TITLY */; i++) {
			System.out.println("Shutting down " + cArr[i].getName());
			if (cArr[i].getState() == Thread.State.RUNNABLE) {
				// do nothing
			} else {
				cArr[i].interrupt();
			}
		}
	}

	public synchronized static void shut(CopyOfConsumer[] cArr) {

		System.out.println("================================================");
		System.out.println("Shut down consumer thread pool ...");
		System.out.println("================================================");

		for (int i = 0; i < cArr.length - 2 /* TITLY */; i++) {
			System.out.println("Shutting down " + cArr[i].getName());
			cArr[i].interrupt();
		}

	}

	public static void join(int i) {
		// TODO Auto-generated method stub
		switch(i){
		case 1:
			for(int j = 0; j < cArr1.length; j++){
				try {
					cArr1[j].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case 2:
			for(int j = 0; j < cArr2.length; j++){
				try {
					cArr2[j].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
			
		case 3: 
			for(int j = 0; j < cArr3.length; j++){
				try {
					cArr3[j].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
			
		case 4: 
			for(int j = 0; j < cArr4.length; j++){
				try {
					cArr4[j].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
			
		case 5:
			for(int j = 0; j < cArr5.length; j++){
				try {
					cArr5[j].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
			
		case 6:
			for(int j = 0; j < cArr6.length; j++){
				try {
					cArr6[j].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
			
		case 7:
			for(int j = 0; j < cArr7.length; j++){
				try {
					cArr7[j].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		}
	}
}
