package edu.upenn.cis455.crawler;

import java.util.ArrayList;

/**
 * class to create and initialize thread pool for a multi-threaded crawler
 * 
 */

public class CrawlerThreadPool {
	
	public static int crawlerThreadPoolCount;

	public static ArrayList<MercatorWorker> crawlerthreads = new ArrayList<MercatorWorker>();
	
	public CrawlerThreadPool(int noofthreads) throws InterruptedException {
		crawlerThreadPoolCount = noofthreads;
		// threadpool initialize
		for (int i = 1; i <= noofthreads; i++) {
			MercatorWorker worker = new MercatorWorker();
			worker.start();

			crawlerthreads.add(worker);
		}
	}

	// stop this pool
	public synchronized void stop() throws InterruptedException {

		XPathCrawler.isCrawling = false;

		for (MercatorWorker thread : crawlerthreads) {

			// thread.join();

			if (thread.getState() == Thread.State.RUNNABLE) {
				continue;
			}

			thread.StopThread();

		}
	}

	public ArrayList<MercatorWorker> getThreads() {
		return crawlerthreads;
	}

	// Rishi
	public synchronized static void stopCompletely()
			throws InterruptedException {

		XPathCrawler.isCrawling = false;

		for (MercatorWorker thread : crawlerthreads) {

			// thread.join();
			//if (thread.getState() == Thread.State.RUNNABLE)
				thread.StopThread();

		}
	}

}
