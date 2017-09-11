package edu.upenn.cis455.crawler;

import edu.upenn.cis455.storage.dbentity.URLEntity;

/**
 * crawler worker thread class
 */

public class MercatorWorker extends Thread {

	public MercatorWorker() {

	}

	public void run() {
		while (XPathCrawler.isCrawling) {
			try {
				 Thread.sleep(XPathCrawler.mercatorTime);
				URLEntity url = XPathCrawler.mercatorfrontier.get();
			
					if (url != null) {
						MercatorProcessor MP = new MercatorProcessor();
						MP.MercatorURLProcess(url);
				
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.interrupt();
	}

	public synchronized void StopThread() throws InterruptedException {

		this.interrupt();

	}
}
