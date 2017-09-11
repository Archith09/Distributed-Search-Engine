package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import edu.upenn.cis.crawler.checkpointing.*;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.sleepycat.persist.EntityStore;

import edu.upenn.cis455.storage.ProjectDBWrapper;
import edu.upenn.cis455.storage.dbentity.RestoreEntity;
import edu.upenn.cis455.storage.dbentity.URLEntity;
import edu.upenn.cis455.webserver.requestandresponse.Helper;

/**
 * The main XPathCrawler class 
 */

public class XPathCrawler {

	public static String WebPageURL;
	public static String DBDirectory;
	public static long maxbyte;
	public static int stop = -1;
	public static boolean isCrawling;
	public static URLFrontier mercatorfrontier = new URLFrontier();
	public static int workercount;
	public static ArrayList<MercatorWorker> crawlerthreads = new ArrayList<MercatorWorker>();
	public static CrawlerThreadPool crawlerpool;
	public static long crawlerstarttime = 0;

	public static int NumberofHosts;
	public static int crawlerThreadPoolSize;

	public static ProjectDBWrapper newWrapper;
	
	public static RestoreEntity restoreEntity;
	public static int mercatorTime = 0;

	/* Checkpoing */
	ArrayList<String> sharedCheckPointingQueue1 = new ArrayList<String>();
	ArrayList<String> sharedCheckPointingQueue2 = new ArrayList<String>();
	ArrayList<String> sharedCheckPointingQueue3 = new ArrayList<String>();
	ArrayList<String> sharedCheckPointingQueue4 = new ArrayList<String>();
	ArrayList<String> sharedCheckPointingQueue5 = new ArrayList<String>();
	ArrayList<String> sharedCheckPointingQueue6 = new ArrayList<String>();
	ArrayList<String> sharedCheckPointingQueue7 = new ArrayList<String>();
	

	Object lock1 = new Object();
	Object lock2 = new Object();
	Object lock3 = new Object();
	Object lock4 = new Object();
	Object lock5 = new Object();
	Object lock6 = new Object();
	Object lock7 = new Object();
	
	static String bucketName1 = "test.bucket.rsolanki21";
	static String bucketName2 = "test.bucket.rsolanki21";
	static String bucketName3 = "test.bucket.rsolanki21";
	static String bucketName4 = "test.bucket.rsolanki21";
	static String bucketName5 = "test.bucket.rsolanki21";
	static String bucketName6 = "test.bucket.rsolanki21";
	static String bucketName7 = "test.bucket.rsolanki21";

	static CopyOfConsumerPool consumerPool;
	static String bucketName = "";

	int checkpointingQueueLimit = 20000;

	static int checkpointingThreadPoolSize = 2;
	public static int time;
	static CopyOfProducer<Object> producerThread;
	static PrintStream out1;
	static PrintStream out2;
	static PrintStream out3;
	
	public synchronized static PrintStream getOut1() {
		return out1;
	}
	
	public synchronized static PrintStream getOut2() {
		return out2;
	}
	
	public synchronized static PrintStream getOut3() {
		return out3;
	}
	
	public synchronized static RestoreEntity getRestoreEntity(){
		return restoreEntity;
	}
	
	/**
	 * args[0] => seed url page args[1] => db directory args[2] => max page size
	 * in mb args[3] => max no of download args[4] => crawler thread pool
	 * args[5] => host no args[6] ==> producer delay args[7] ==> checkpointing
	 * threadpool size args[8[ ==> mercatorTime (worker sleep time) arg[9] ==> bucket name
	 */
	public static void main(String args[]) throws Exception

	{
		DetectorFactory
		.loadProfile("resources/profiles");
		
		if (args.length < 3) {
			System.out.println("Please enter valid arguments");
			return;
		} else if (args.length == 3) {
			try {
				XPathCrawler XPC = new XPathCrawler(args[0], args[1],
						Integer.parseInt(args[2]) * 1024);
				XPC.begin();

				XPC.beginCheckpointing();

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} else if (args.length == 4) {
			try {
				XPathCrawler XPC = new XPathCrawler(args[0], args[1],
						Integer.parseInt(args[2]) * 1024,
						Integer.parseInt(args[3]));
				XPC.begin();

				// crawlerpool.isRunning();
				XPC.beginCheckpointing();

			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} else if (args.length == 7) {
			System.out.println("============================================");
			System.out.println("7 variable constructor");
			System.out.println("============================================");

			XPathCrawler XPC = new XPathCrawler(args[0], args[1],
					Integer.parseInt(args[2]) * 1024,
					Integer.parseInt(args[3]), Integer.parseInt(args[4]),
					Integer.parseInt(args[5]), Integer.parseInt(args[6]));

			try {
				XPC.begin();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			XPC.beginCheckpointing();

		} else if (args.length == 10) {
			System.out.println("============================================");
			System.out.println("10 variable constructor");
			System.out.println("============================================");

			bucketName = args[9];
			bucketName1 = bucketName;
			bucketName2 = bucketName;
			bucketName3 = bucketName;
			bucketName4 = bucketName;
			bucketName5 = bucketName;
			bucketName6 = bucketName;
			bucketName7 = bucketName;
			
			
			out1 = new PrintStream(new FileOutputStream("output1.txt"));
			out2 = new PrintStream(new FileOutputStream("output2.txt"));
			out3 = new PrintStream(new FileOutputStream("output3.txt"));

			restoreEntity = new RestoreEntity();
			
			
			checkpointingThreadPoolSize = Integer.parseInt(args[7]);
			mercatorTime = Integer.parseInt(args[8]);
			XPathCrawler XPC = new XPathCrawler(args[0], args[1],
					Integer.parseInt(args[2]) * 1024,
					Integer.parseInt(args[3]), Integer.parseInt(args[4]),
					Integer.parseInt(args[5]), Integer.parseInt(args[6]));

			try {
				XPC.begin();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			XPC.beginCheckpointing();

		}

	}

	/** constructor with three rquired parameters
	 * 
	 * @param weburl
	 * @param dbdirectory
	 * @param max
	 * @throws Exception
	 */
	public XPathCrawler(String weburl, String dbdirectory, long max)
			throws Exception {
		WebPageURL = weburl;
		DBDirectory = dbdirectory;
		maxbyte = max;

		System.out.println("============================================");
		System.out.println("3 variable constructor");
		System.out.println("============================================");

		/* checkpointing */
		/*
		 * producerThread = new Producer<Object>(sharedCheckPointingQueue,
		 * checkpointingQueueLimit, dbdirectory, lock, 100); consumerPool = new
		 * ConsumerPool(sharedCheckPointingQueue, lock,
		 * checkpointingThreadPoolSize, checkpointingQueueLimit, bucketName,
		 * 100);
		 */
		
		ProjectDBWrapper.envDirectory = DBDirectory;
		newWrapper = ProjectDBWrapper.getInstance();

		producerThread = new CopyOfProducer(sharedCheckPointingQueue1,
				sharedCheckPointingQueue2, sharedCheckPointingQueue3,
				sharedCheckPointingQueue4, sharedCheckPointingQueue5,
				sharedCheckPointingQueue6, sharedCheckPointingQueue7, checkpointingQueueLimit,
				dbdirectory, lock1, lock2, lock3, lock4, lock5, lock6, lock7, 10000);

		producerThread.setPriority(Thread.MAX_PRIORITY);

		consumerPool = new CopyOfConsumerPool(sharedCheckPointingQueue1,
				sharedCheckPointingQueue2, sharedCheckPointingQueue3,
				sharedCheckPointingQueue4, sharedCheckPointingQueue5,
				sharedCheckPointingQueue6, sharedCheckPointingQueue7, bucketName1, bucketName2,
				bucketName3, bucketName4, bucketName5, bucketName6, bucketName7, lock1,
				lock2, lock3, lock4, lock5, lock6, lock7, checkpointingQueueLimit,
				10000, checkpointingThreadPoolSize);
	}

	/** constructor with 4 parameters for optional number of downloads to stop crawling
	 * 
	 * @param weburl
	 * @param dbdirectory
	 * @param max
	 * @param sp
	 * @throws Exception
	 */
	
	public XPathCrawler(String weburl, String dbdirectory, long max, int sp)
			throws Exception {
		WebPageURL = weburl;
		DBDirectory = dbdirectory;
		maxbyte = max;
		stop = sp;

		System.out.println("============================================");
		System.out.println("4 variable constructor");
		System.out.println("============================================");

	

		ProjectDBWrapper.envDirectory = DBDirectory;
		newWrapper = ProjectDBWrapper.getInstance();

		producerThread = new CopyOfProducer<Object>(sharedCheckPointingQueue1,
				sharedCheckPointingQueue2, sharedCheckPointingQueue3,
				sharedCheckPointingQueue4, sharedCheckPointingQueue5,
				sharedCheckPointingQueue6, sharedCheckPointingQueue7, checkpointingQueueLimit,
				dbdirectory, lock1, lock2, lock3, lock4, lock5, lock6, lock7, stop);

		consumerPool = new CopyOfConsumerPool(sharedCheckPointingQueue1,
				sharedCheckPointingQueue2, sharedCheckPointingQueue3,
				sharedCheckPointingQueue4, sharedCheckPointingQueue5,
				sharedCheckPointingQueue6, sharedCheckPointingQueue7, bucketName1, bucketName2,
				bucketName3, bucketName4, bucketName5, bucketName6, bucketName7, lock1,
				lock2, lock3, lock4, lock5, lock6, lock7, checkpointingQueueLimit,
				stop, checkpointingThreadPoolSize);

	}

	/** constructor with 4 parameters for optional number of downloads to stop crawling
	 * 
	 * @param weburl
	 * @param dbdirectory
	 * @param max
	 * @param sp
	 * @param noOfCrawlerThreadPool
	 * @param noOfHost
	 * @param time
	 * @throws Exception
	 */
	
	public XPathCrawler(String weburl, String dbdirectory, long max, int sp,
			int noOfCrawlerThreadPool, int noOfHost, int time) throws Exception {
		WebPageURL = weburl;
		DBDirectory = dbdirectory;
		maxbyte = max;
		stop = sp;
		NumberofHosts = noOfHost;
		crawlerThreadPoolSize = noOfCrawlerThreadPool;
		XPathCrawler.time = time;

		System.out.println("============================================");
		System.out.println("7 variable constructor:: Download count  " + stop + "**  Host number"
				+ NumberofHosts + "**  crawler threadpool size" + crawlerThreadPoolSize);
		System.out.println("============================================");

	
		ProjectDBWrapper.envDirectory = DBDirectory;
		newWrapper = ProjectDBWrapper.getInstance();
		

		producerThread = new CopyOfProducer<Object>(sharedCheckPointingQueue1,
				sharedCheckPointingQueue2, sharedCheckPointingQueue3,
				sharedCheckPointingQueue4, sharedCheckPointingQueue5,
				sharedCheckPointingQueue6, sharedCheckPointingQueue7, checkpointingQueueLimit,
				dbdirectory, lock1, lock2, lock3, lock4, lock5, lock6, lock7, stop);

		consumerPool = new CopyOfConsumerPool(sharedCheckPointingQueue1,
				sharedCheckPointingQueue2, sharedCheckPointingQueue3,
				sharedCheckPointingQueue4, sharedCheckPointingQueue5,
				sharedCheckPointingQueue6, sharedCheckPointingQueue7, bucketName1, bucketName2,
				bucketName3, bucketName4, bucketName5, bucketName6, bucketName7, lock1,
				lock2, lock3, lock4, lock5, lock6, lock7, checkpointingQueueLimit,
				stop, checkpointingThreadPoolSize);

	}

	/** Begin crawling
	 * 
	 * @throws Exception
	 */
	public void begin() throws Exception {

		
		EntityStore store = newWrapper.getStore();

		URLEntity url = new URLEntity();
		crawlerstarttime = System.currentTimeMillis();

		File f = new File("resources/seedURLs.txt");
		if (f.exists()) {
			System.out.println("READING FROM FILE SEEDURLS ....");
			BufferedReader br = new BufferedReader(new FileReader(f));
			String urlseed = null;
			ArrayList<String> seedurls = new ArrayList<String>();
			while ((urlseed = br.readLine()) != null) {
				if (urlseed.startsWith("http"))
					seedurls.add(urlseed);
			}

			mercatorfrontier.addSeedPageLinks(seedurls);

		} else {
			URL urli = new URL(WebPageURL);
			String niceurl = CrawlerHelper.getniceurl(urli);
			url.setURLname(niceurl);

			url.setMethod("ROBOT");
			// urla.put(url);
			mercatorfrontier.enqueuefrontier(url);
		}

		isCrawling = true;

		// Please change the number of threads here for multi-threaded crawling
		crawlerpool = new CrawlerThreadPool(crawlerThreadPoolSize);
	}

	// begin checkpointing
	public void beginCheckpointing() throws InterruptedException {
		System.out.println("============================================");
		System.out.println("Starting producer and consumer");
		System.out.println("============================================");

		producerThread.start();
		consumerPool.start();

		System.out.println("============================================");
		System.out.println("Started producer and consumer");
		System.out.println("============================================");

	}

	// shutdown the threadpool
	public static void xpathcrawlershutdown() throws InterruptedException {

	

		if (producerThread.count < producerThread.maxLimit
				&& producerThread.docListEmptyCount <= producerThread.docListMaxEmptyCount) {
			//
			producerThread.join();
		}

		System.out.println("Sutdownnnnnnnnnnnnnnnnnnn calledddddddddddddddddd");
		crawlerpool.stop();
		System.out.println("=========================================");
		System.out.println("Crawler stopped");
		System.out.println("=========================================");
		crawlerpool.stopCompletely();

		
		System.out.println("CRAWLER STARTED AT::::::"
				+ Helper.getGMT(new Date(crawlerstarttime)));
		System.out.println("CRAWLE SHUTDOWN AT:::::::"
				+ Helper.getGMT(new Date()));

		System.out.println("download count " + MercatorInfo.getTotal());
		System.out.println("request count " + MercatorInfo.getTotalhits());
		System.out.println("allformats count " + MercatorInfo.getallformats());
	}

}
