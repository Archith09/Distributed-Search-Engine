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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.upenn.cis.crawler.checkpointing.*;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.sleepycat.persist.EntityStore;

import edu.upenn.cis455.storage.ProjectDBWrapper;
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
	
	
	//public static BlockingQueue<String> remoteQ = new LinkedBlockingQueue<String>();
	public static int crawlerThreadPoolSize;
	//public static BlockingQueue<String> remoteQ = new LinkedBlockingQueue<String>();
	
	
	public static ProjectDBWrapper newWrapper;
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
	
	/*public synchronized static BlockingQueue<String> getremoteQ() {
		return remoteQ;
	}
	*/
	public synchronized static PrintStream getOut2() {
		return out2;
	}

	public synchronized static PrintStream getOut3() {
		return out3;
	}
	
	public XPathCrawler(String seedurlpage, String dbDirectory, String maxPageSize, String maxDownload,
            String crawlerThreadPoolSize, String hostNo, String producerDelay, String checkpointingThreadPoolSize,
            String mercatorDelay, String bucketName) throws NumberFormatException, Exception {
        
		DetectorFactory
		.loadProfile("resources/profiles");
		

		out1 = new PrintStream(new FileOutputStream("resources/output1.txt"));
		out2 = new PrintStream(new FileOutputStream("resources/output2.txt"));
		out3 = new PrintStream(new FileOutputStream("resources/output3.txt"));


		
        XPathCrawler.bucketName = bucketName;
        bucketName1 = bucketName;
        bucketName2 = bucketName;
        bucketName3 = bucketName;
        bucketName4 = bucketName;
        bucketName5 = bucketName;
        bucketName6 = bucketName;

        XPathCrawler.checkpointingThreadPoolSize = Integer.parseInt(checkpointingThreadPoolSize);
        XPathCrawler.mercatorTime = Integer.parseInt(mercatorDelay);
        				
		WebPageURL = seedurlpage;
		DBDirectory = dbDirectory;
		maxbyte = Integer.parseInt(maxPageSize) * 1024;
		stop = Integer.parseInt(maxDownload);
		NumberofHosts = Integer.parseInt(hostNo);
		XPathCrawler.crawlerThreadPoolSize = Integer.parseInt(crawlerThreadPoolSize);
		XPathCrawler.time = Integer.parseInt(producerDelay);

		
		System.out.println("============================================");
		System.out.println("10-7 variable constructor" + stop + "**"
				+ NumberofHosts + "**" + crawlerThreadPoolSize);
		System.out.println("============================================");

		ProjectDBWrapper.envDirectory = DBDirectory;
		newWrapper = ProjectDBWrapper.getInstance();


		producerThread = new CopyOfProducer<Object>(sharedCheckPointingQueue1,
				sharedCheckPointingQueue2, sharedCheckPointingQueue3,
				sharedCheckPointingQueue4, sharedCheckPointingQueue5,
				sharedCheckPointingQueue6, sharedCheckPointingQueue7, checkpointingQueueLimit,
				dbDirectory, lock1, lock2, lock3, lock4, lock5, lock6, lock7, stop);

		consumerPool = new CopyOfConsumerPool(sharedCheckPointingQueue1,
				sharedCheckPointingQueue2, sharedCheckPointingQueue3,
				sharedCheckPointingQueue4, sharedCheckPointingQueue5,
				sharedCheckPointingQueue6, sharedCheckPointingQueue7, bucketName1, bucketName2,
				bucketName3, bucketName4, bucketName5, bucketName6, bucketName7, lock1,
				lock2, lock3, lock4, lock5, lock6, lock7, checkpointingQueueLimit,
				stop, XPathCrawler.checkpointingThreadPoolSize);


    }
	
	/*
	 * args[0] => seed url page args[1] => db directory args[2] => max page size
	 * in mb args[3] => max no of download args[4] => crawler thread pool
	 * args[5] => host no args[6] ==> producer delay args[7] ==> checkpointing
	 * threadpool size args[8[ ==> mercatorTime (worker sleep time) arg[9] ==> bucket name
	 */
	
	// Begin crawling
	public void begin() throws Exception {

		
		EntityStore store = newWrapper.getStore();

		URLEntity url = new URLEntity();
		crawlerstarttime = System.currentTimeMillis();

		File f = new File("resources/seeds.txt");
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

		// mercatorfrontier.getFrontierlist().clear();
		// mercatorfrontier.getBacklist().clear();

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
