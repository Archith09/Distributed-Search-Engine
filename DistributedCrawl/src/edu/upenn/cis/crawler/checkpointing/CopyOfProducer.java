package edu.upenn.cis.crawler.checkpointing;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

import edu.upenn.cis455.crawler.CrawlerThreadPool;
import edu.upenn.cis455.crawler.URLFrontier;
import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.storage.ProjectDBWrapper;
import edu.upenn.cis455.storage.dbentity.DocEntity;
import edu.upenn.cis455.storage.dbentity.ImageEntity;
import edu.upenn.cis455.storage.dbentity.RestoreEntity;
import edu.upenn.cis455.storage.dbentity.URLEntity;

/**
 * Class to periodically push the Berkeley DB to the producer queue
 * 
 */

public class CopyOfProducer<T> extends Thread {

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

	int LIMIT;
	static volatile boolean shutdown = false;
	String env;
	public double count;
	public double maxLimit;
	public double docListEmptyCount = 0;
	public double docListMaxEmptyCount = 3;	

	ProjectDBWrapper newWrapper;

	public static boolean pool1 = false;
	public static boolean pool2 = false;
	public static boolean pool3 = false;
	public static boolean pool4 = false;
	public static boolean pool5 = false;
	public static boolean pool6 = false;
	public static boolean pool7 = false;
	

	public CopyOfProducer(ArrayList<String> sharedQueue1,
			ArrayList<String> sharedQueue2, ArrayList<String> sharedQueue3,
			ArrayList<String> sharedQueue4, ArrayList<String> sharedQueue5,
			ArrayList<String> sharedQueue6, ArrayList<String> sharedQueue7, int queueLimit, String env,
			Object lock1, Object lock2, Object lock3, Object lock4,
			Object lock5, Object lock6, Object lock7, double maxLimit) throws Exception {
		newWrapper = ProjectDBWrapper.getInstance();
		this.queue1 = sharedQueue1;
		this.queue2 = sharedQueue2;
		this.queue3 = sharedQueue3;
		this.queue4 = sharedQueue4;
		this.queue5 = sharedQueue5;
		this.queue6 = sharedQueue6;
		this.queue7 = sharedQueue7;
	

		this.lock1 = lock1;
		this.lock2 = lock2;
		this.lock3 = lock3;
		this.lock4 = lock4;
		this.lock5 = lock5;
		this.lock6 = lock6;
		this.lock7 = lock7;

		this.LIMIT = queueLimit;
		this.env = env;
		this.count = 0;
		this.maxLimit = maxLimit;
		this.maxLimit = maxLimit;
		
	}

	public void run() {

		while (!CopyOfProducer.checkShutdown() && count < maxLimit) {
			pool1 = false;
			pool2 = false;
			pool3 = false;
			pool4 = false;
			pool5 = false;
			pool6 = false;
			pool7  = false;

			System.out.println("=================================");
			System.out.println("Init Db instance");
			System.out.println("=================================");

			// ##################################################################
			// POOLS: ...
			try {
				addToSharedQueue();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// ##################################################################

			if (pool1) {
				docListEmptyCount++;
				if (docListEmptyCount > docListMaxEmptyCount) {
					// need to shutdown producer thread
					System.out.println("=========================");
					System.out.println("DocListEmptyCount -"
							+ docListEmptyCount + ":" + docListMaxEmptyCount);
					System.out.println("==========================");
					CopyOfProducer.setShutdown();
				}
			}

			try {
				System.out.println("==================================");

				System.out.println("Producer thread sleeping.............");
				System.out.println("==================================");

				sleep(1000 * 60 * XPathCrawler.time); // sleep for 5 mins
				System.out.println("==================================");

				System.out.println("Producer thread awake ..............");
				System.out.println("==================================");

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		System.out
				.println("====================OUT OF PRODUCER =========================");
		System.out.println("==================================");
		System.out.println("Exiting of the producer while loop");
		System.out.println("==================================");
		synchronized (newWrapper) {
			newWrapper.close();
			newWrapper.notifyAll();
		}
		System.out.println("DB Close");

		System.out.println("==================================");
		if (queue1.size() != 0) {
			CopyOfConsumerPool.join(1);
		}
		CopyOfConsumerPool.setShutdown(1);
		System.out.println("Queue1 size" + queue1.size()
				+ "ConsumerPoolShutdown: " + CopyOfConsumerPool.getShutdown(1));

		if (queue2.size() != 0) {
			CopyOfConsumerPool.join(2);
		}
		CopyOfConsumerPool.setShutdown(2);
		System.out.println("Queue2 size" + queue2.size()
				+ "ConsumerPoolShutdown: " + CopyOfConsumerPool.getShutdown(2));

		if (queue3.size() != 0) {
			CopyOfConsumerPool.join(3);
		}
		CopyOfConsumerPool.setShutdown(3);
		System.out.println("Queue3 size" + queue3.size()
				+ "ConsumerPoolShutdown: " + CopyOfConsumerPool.getShutdown(3));

		if (queue4.size() != 0) {
			CopyOfConsumerPool.join(4);
		}
		CopyOfConsumerPool.setShutdown(4);
		System.out.println("Queue4 size" + queue4.size()
				+ "ConsumerPoolShutdown: " + CopyOfConsumerPool.getShutdown(4));

		if (queue5.size() != 0) {
			CopyOfConsumerPool.join(5);
		}
		CopyOfConsumerPool.setShutdown(5);
		System.out.println("Queue5 size" + queue5.size()
				+ "ConsumerPoolShutdown: " + CopyOfConsumerPool.getShutdown(5));
		
		/*
		if (queue6.size() != 0) {
			CopyOfConsumerPool.join(6);
		}
		CopyOfConsumerPool.setShutdown(6);
		System.out.println("Queue6 size" + queue6.size()
				+ "ConsumerPoolShutdown: " + CopyOfConsumerPool.getShutdown(6));

		if (queue7.size() != 0) {
			CopyOfConsumerPool.join(7);
		}
		CopyOfConsumerPool.setShutdown(7);
		System.out.println("Queue7 size" + queue7.size()
				+ "ConsumerPoolShutdown: " + CopyOfConsumerPool.getShutdown(7));

		*/
		System.out.println("==================================");

		System.out.println("SHUTTING ANY MERCATOR THREADS");
		try {
			CrawlerThreadPool.stopCompletely();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// save the last few batches inside the queue by yourself
		CopyOfConsumerPool.shut(1); // shut consumerpool
		CopyOfConsumerPool.shut(2); // shut consumerpool
		CopyOfConsumerPool.shut(3); // shut consumerpool
		CopyOfConsumerPool.shut(4); // shut consumerpool
		CopyOfConsumerPool.shut(5); // shut consumerpool
//		CopyOfConsumerPool.shut(6); // shut consumerpool TITLY
//		CopyOfConsumerPool.shut(7); // shut consumerpool TITLY

		/*
		 * if (queue.size() > 0) { Consumer c = new Consumer(queue, lock, LIMIT,
		 * env); c.saveLastBatchInS3(); }
		 */
		this.interrupt();

	}

	private void addToSharedQueue() throws Exception {
		// TODO Auto-generated method stub
		ArrayList<DocEntity> doclist;
		ArrayList<RestoreEntity> restoreList;
		ArrayList<ImageEntity> imgList;

		if (newWrapper == null) {
			ProjectDBWrapper.envDirectory = this.env;
			newWrapper = ProjectDBWrapper.getInstance();

		}
		synchronized (newWrapper) {

			System.out.println("========================");
			System.out.println("GOT ACCESS TO PROJECT DB CLASS");
			System.out.println("========================");

				doclist = newWrapper.getAndRemoveallentities(DocEntity.class);
				restoreList = newWrapper.getAndRemoveallentities(RestoreEntity.class);
				//imgList = newWrapper.getAndRemoveallentities(ImageEntity.class);
			newWrapper.notifyAll();
		}

		System.out.println("Deleted berkeley db and starting to save in S3 "
				+ doclist.size());

		if (doclist.size() > 0) {

			System.out.println("************************************");
			System.out.println("INSIDE DOCLIST>0" + doclist.size());
			System.out.println("************************************");

			StringBuffer buf = new StringBuffer();

			synchronized (queue1) {
				try {
					buf = getFile1(doclist);
					
					queue1.add(buf.toString());

					buf.delete(0, buf.length());

					queue1.notifyAll();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			synchronized (queue2) {
				try {
					buf = getFile2(doclist);
					queue2.add(buf.toString());
					buf.delete(0, buf.length());
					queue2.notifyAll();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			synchronized (queue3) {
				try {
					buf = getFile3(doclist);
					queue3.add(buf.toString());
					buf.delete(0, buf.length());
					queue3.notifyAll();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			synchronized (queue4) {
				try {
					buf = getFile4(doclist);
					queue4.add(buf.toString());
					buf.delete(0, buf.length());
					queue4.notifyAll();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			synchronized (queue5) {
				try {
					buf = getFile5(doclist);
					queue5.add(buf.toString());
					buf.delete(0, buf.length());
					queue5.notifyAll();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			/*		TITLY
			synchronized(queue7){			
				try {
					System.out.println("????????????????????????????????");
					System.out.println("ENTERED LOCK 7 SYNC");
					System.out.println("?????????????????????????????????");

					buf = new StringBuffer();
					
					buf = getFile7(doclist);

					queue7.add(buf.toString());
					buf.delete(0, buf.length());
					queue7.notifyAll();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			
			}*/

		} else {
			pool1 = true;
		}
		/* TITLY
		synchronized (queue6) {
			try {
				System.out.println("????????????????????????????????");
				System.out.println("ENTERED LOCK 6 SYNC");
				System.out.println("?????????????????????????????????");

				StringBuffer buf = new StringBuffer();
				
				buf = getFile6(restoreList);

				queue6.add(buf.toString());
				buf.delete(0, buf.length());
				queue6.notifyAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/

	}

	private synchronized StringBuffer getFile7(ArrayList<DocEntity> doclist){
		String key = toPaddedHex();
		StringBuffer buf = new StringBuffer();

		buf.append(key); // key for s3
		buf.append(" ");
		HashSet<String> urlSet = new HashSet<String>();
		for(int i = 0; i < doclist.size(); i++){
			DocEntity ie = doclist.get(i);
			HashMap<String, String> map = ie.getImageMap();
			
			for(String url: map.keySet()){
				if(!urlSet.contains(url)){
					buf.append(url);
					buf.append("\t");
					buf.append(map.get(url));
					buf.append(" ");
					buf.append(getImgName(url));
					buf.append("\n");
					urlSet.add(url);
				}
			}
			map.clear();
		}	
		urlSet.clear();
		System.out.println("========================");
		System.out.println("FILE 7 ADDED TO QUEUE (Img List)");
		System.out.println("========================");
		return buf;
		
	}
	
	private String getImgName(String url) {
		// TODO Auto-generated method stub
		if(url.contains("/")){
			url = url.substring(url.lastIndexOf("/"));
			if(url.contains(".")){
				return url.substring(url.lastIndexOf("/")+1, url.lastIndexOf("."));
			}else{
				return url.substring(url.indexOf("/")+1);
			}
		}else{
			return url;
		}
	}

	private synchronized StringBuffer getFile6(ArrayList<RestoreEntity> restoreList) {
		// TODO Auto-generated method stub
		StringBuffer buf = new StringBuffer();
		
		String key = toPaddedHex();
		buf.append(key);
		buf.append(" ");
		
		for(int i = 0; i < restoreList.size(); i++){
			RestoreEntity re = restoreList.get(i);
			buf.append(re.getQueue().toString());
			buf.append("\n");
		}
		System.out.println("========================");
		System.out.println("FILE 6 ADDED TO QUEUE (SNAPSHOTS)");
		System.out.println("========================");
		return buf;
	}
	
	private StringBuffer getFile5(ArrayList<DocEntity> doclist) {
		// TODO Auto-generated method stub

		String key = toPaddedHex();
		StringBuffer buf = new StringBuffer();

		buf.append(key); // key for s3
		buf.append(" ");

		for (int i = 0; i < doclist.size(); i++) {
			DocEntity de = (DocEntity) doclist.get(i);

			buf.append(de.getAddress());
			buf.append("\t");
			ArrayList<String> tp = de.getParsedlinks();
			for(int j = 0; j < tp.size(); j++){
				buf.append(tp.get(j));
				if(j != tp.size()-1){
					buf.append(";#;");
				}	
			}
			buf.append("\n");

			System.out.println("========================");
			System.out.println("FILE 5 ADDED TO QUEUE");
			System.out.println("========================");
		}

		return buf;
	}

	private StringBuffer getFile4(ArrayList<DocEntity> doclist) {
		// TODO Auto-generated method stub
		String key = toPaddedHex();
		StringBuffer buf = new StringBuffer();

		buf.append(key); // key for s3
		buf.append(" ");

		for (int i = 0; i < doclist.size(); i++) {
			DocEntity de = (DocEntity) doclist.get(i);

			buf.append(de.getAddress());
			buf.append("\t");
			buf.append(de.getHeadings());
			buf.append(" ");
			buf.append(de.getMetadata());
			buf.append("\n");
			System.out.println("========================");
			System.out.println("FILE 4 ADDED TO QUEUE");
			System.out.println("========================");
		}

		return buf;

	}

	private StringBuffer getFile3(ArrayList<DocEntity> doclist) {
		// TODO Auto-generated method stub
		String key = toPaddedHex();
		StringBuffer buf = new StringBuffer();

		buf.append(key); // key for s3
		buf.append(" ");

		for (int i = 0; i < doclist.size(); i++) {
			DocEntity de = (DocEntity) doclist.get(i);

			buf.append(de.getAddress());
			buf.append("\t");
			buf.append(de.getTitle());
			buf.append("\n");
			System.out.println("========================");
			System.out.println("FILE 3 ADDED TO QUEUE");
			System.out.println("========================");
		}

		return buf;
	}

	private StringBuffer getFile2(ArrayList<DocEntity> doclist) {
		// TODO Auto-generated method stub
		String key = toPaddedHex();
		StringBuffer buf = new StringBuffer();

		buf.append(key); // key for s3
		buf.append(" ");
		for (int i = 0; i < doclist.size(); i++) {
			DocEntity de = (DocEntity) doclist.get(i);
			buf.append(de.getAddress());
			buf.append("\t");
			buf.append("TITLE@");
			buf.append(de.getTitle());
			//buf.append("");
			//buf.append("LOCATION@");
			//buf.append(de.getLocation());
			//buf.append("");
			// buf.append("@DOMAIN[");
			// buf.append(de.getDomain());
			// buf.append("]");
			//buf.append("@META[");
			//buf.append(de.getMetadata());
			//buf.append("]");
			buf.append("\n");
			System.out.println("========================");
			System.out.println("FILE 2 ADDED TO QUEUE");
			System.out.println("========================");
		}

		return buf;
	}

	private StringBuffer getFile1(ArrayList<DocEntity> doclist) {
		String key = toPaddedHex();
		StringBuffer buf = new StringBuffer();

		buf.append(key); // key for s3
		buf.append(" ");

		for (int i = 0; i < doclist.size(); i++) {
			DocEntity de = (DocEntity) doclist.get(i);
			buf.append(de.getAddress());
			buf.append("\t");
			buf.append(de.getBody());
			buf.append(" ");
			buf.append(de.getMetadata());
			buf.append(" ");
			buf.append(de.getTitle());
			buf.append(" ");
			buf.append(de.getHeadings());
			buf.append("\n");
			count++;
			System.out.println("========================");
			System.out.println("ADDED TO QUEUE1..  COUNT = " + count);
			System.out.println("========================");
		}

		return buf;
	}

	public static boolean checkShutdown() {
		return CopyOfProducer.shutdown;
	}

	public static void setShutdown() {
		CopyOfProducer.shutdown = true;
	}

	public synchronized void shutdown() {
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXxx");
		System.out.println("Shutting downn producer thread ...");
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		setShutdown();

		if (this.getState() == Thread.State.RUNNABLE) {
			// do nothing
		} else {
			this.interrupt();
		}

	}

	public static String toPaddedHex() {
		return UUID.randomUUID().toString();
		// Random r = new Random();
		// return String.format("%04X", r.nextInt(16 ^ 4));
	}
}
