package edu.upenn.cis455.crawler;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import org.eclipse.jetty.util.ConcurrentHashSet;

import edu.upenn.cis455.crawler.worker.WorkerServer;
//import edu.upenn.cis455.crawler.worker.WorkerServer;
import edu.upenn.cis455.storage.dbentity.HostEntity;
import edu.upenn.cis455.storage.dbentity.URLEntity;

/**
 *  Mercator Frontier to input and output the URLs for mercator processor
 */


public class URLFrontier {

	
	private DelayQueue<URLEntity> frontierlist = new DelayQueue<URLEntity>();
	private HashSet<String> duplicatecheck = new HashSet<String>();
	private HashMap<String, ArrayList<URLEntity>> backlist = new HashMap<String, ArrayList<URLEntity>>();
	private HashMap<String, ArrayList<URLEntity>> secondarybacklist = new HashMap<String, ArrayList<URLEntity>>();

	public URLFrontier() {

	}

	public synchronized DelayQueue<URLEntity> getfrontqueue() {
		return this.frontierlist;
	}

	public synchronized HashMap<String, ArrayList<URLEntity>> getbacklist() {
		return this.backlist;
	}

	public synchronized HashMap<String, ArrayList<URLEntity>> getsecondarylist() {
		return this.secondarybacklist;
	}

	public synchronized HashSet<String> getDUE() {
		return this.duplicatecheck;
	}

	/**
	 * Method to add URLs to back queue
	 * 
	 * @throws MalformedURLException
	 */
	public synchronized int putcheck(URLEntity urle)
			throws MalformedURLException {

		URL url = new URL(urle.getURLPagename());
		String urlnew = CrawlerHelper.getniceurl(url);

		int check = -1;

		String hostname = url.getHost();

		int nodeID = this.getNodeNumber(hostname);

		
		//Getting URLs from remote nodes
		if (nodeID != WorkerServer.self) {	
			WorkerServer.packetMap.get(nodeID).add(urle.getURLPagename());
			return -1;
		}

		if (duplicatecheck.contains(urlnew) && urle.getMethod().equals("ROBOT")) {
			return -1;
		}

		duplicatecheck.add(urlnew);

		if (backlist.containsKey(url.getHost()) == false) {
			if (backlist.size() <= XPathCrawler.NumberofHosts) {
				backlist.put(url.getHost(), new ArrayList<URLEntity>());
				backlist.get(url.getHost()).add(urle);
				check = 1;
			} else {
				if (secondarybacklist.containsKey(url.getHost())) {
					secondarybacklist.get(url.getHost()).add(urle);
				} else {
					secondarybacklist.put(url.getHost(),
							new ArrayList<URLEntity>());
					secondarybacklist.get(url.getHost()).add(urle);
				}
			}

		} else {
			backlist.get(url.getHost()).add(urle);
		}

		return check;
	}

	public synchronized Map.Entry<String, ArrayList<URLEntity>> getfromsecondary() {

		if (secondarybacklist.isEmpty()) {
			return null;
		}
		Map.Entry<String, ArrayList<URLEntity>> header = secondarybacklist
				.entrySet().iterator().next();
		secondarybacklist.remove(header.getKey());
		return header;

	}

	/** adding a URLEntity to the frontier queue
	 * @throws InterruptedException
	 * @throws MalformedURLException
	 */
	public synchronized void enqueuefrontier(URLEntity urle)
			throws InterruptedException, MalformedURLException {

		if (putcheck(urle) == 1) {

			URL url = new URL(urle.getURLPagename());
			String hostname = url.getHost();
			addhost(hostname, 0);

		}
	}

	
	/**
	 * Method to add host entity to host queue
	 */
	public synchronized void addhost(String h, long d)
			throws InterruptedException {

		if (XPathCrawler.stop != -1) {
			if (MercatorInfo.total == XPathCrawler.stop) {
				System.out
						.println("Shutdown on countTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
				XPathCrawler.xpathcrawlershutdown();
			}
		}

		if (backlist.get(h) != null && backlist.get(h).isEmpty()) { // also
																	// check
																	// null
			backlist.remove(h);
			Map.Entry<String, ArrayList<URLEntity>> header = getfromsecondary();

			if (header != null) {
				backlist.put(header.getKey(), header.getValue());
				addhost(header.getKey(), 0);

			}

			if (backlist.isEmpty() && frontierlist.isEmpty()) {
				System.out.println("SHUT DOWNNNNNNNNN");
				XPathCrawler.xpathcrawlershutdown();
			}
		} else if (backlist.get(h) != null) {

			URLEntity hosturl = backlist.get(h).remove(0);
			hosturl.setNextaccessetime(System.currentTimeMillis() + 1000 * d);
			frontierlist.offer(hosturl);
		}

	}

	public synchronized void addurlentity(URLEntity urlentity, long d)
			throws InterruptedException, MalformedURLException {
		URL url = new URL(urlentity.getURLPagename());

		// check method method but isnt the method supposed to be robot
		// exclusion always

		putcheck(urlentity);
		addhost(url.getHost(), d);

	}

	/** get out a URL entity from the front queue
	 *	 
	 * @throws InterruptedException
	 */
	public URLEntity get() throws InterruptedException {
		return frontierlist.take();
	}

	/** adding JSOUP links to the back queue
	 * 
	 * @param scrapedlinks
	 * @throws InterruptedException
	 * @throws MalformedURLException
	 */
	public synchronized  void addPageLinks(ArrayList<String> scrapedlinks)
			throws InterruptedException, MalformedURLException {
		for (String s : scrapedlinks) {
			URLEntity urle = new URLEntity();
			URL url = new URL(s);
			urle.setURLname(s);
			String hostname = url.getHost();
			urle.setMethod("ROBOT");
			HostEntity he = XPathCrawler.newWrapper.get(HostEntity.class,
					hostname);
			if (he != null && he.isSeed()) {

				if (he.getCount() < 20000) {

					int check = putcheck(urle);
					if (check == 1) {
						addhost(hostname, 0);
					}
				} else {
					XPathCrawler.getOut2().println(
							he.getHostaddress() + "  " + he.getCount()
									+ "    >20000");

				}
			} else if (he != null && (!he.isSeed())) {
				if (he.getCount() < 2000) {
					int check = putcheck(urle);
					if (check == 1) {
						addhost(hostname, 0);
					}
				} else {
					XPathCrawler.getOut2().println(
							he.getHostaddress() + "  " + he.getCount()
									+ "    >2000");

				}
			}
			int check = putcheck(urle);
			if (check == 1) {
				addhost(hostname, 0);
			}

		}
		
		ArrayList<String> remoteList =new ArrayList<String>();
		int i =0;
		
		//BlockingQueue<String> remoteQ = XPathCrawler.getremoteQ();
		BlockingQueue<String> remoteQ = CrawlerThreadPool.getremoteQ();
		
		//while(XPathCrawler.remoteQ != null && !XPathCrawler.remoteQ.isEmpty() && i < 250){
			while(remoteQ != null && !remoteQ.isEmpty() && i < 7000){
			System.out.println("IN THE BQ");
			remoteList.add(remoteQ.take());
			i = i + 1;
		}
			
			
		/**
		 * Keeps a check if the URL is seed or not and saves only upto 20000 pages for seed
		 * and 2000 pages for the other URLs
		 */
			
		for (String s : scrapedlinks) {
			URLEntity urle = new URLEntity();
			URL url = new URL(s);
			urle.setURLname(s);
			String hostname = url.getHost();
			urle.setMethod("ROBOT");
			HostEntity he = XPathCrawler.newWrapper.get(HostEntity.class,
					hostname);
			if (he != null && he.isSeed()) {

				if (he.getCount() < 20000) {

					int check = putcheck(urle);
					if (check == 1) {
						addhost(hostname, 0);
					}
				} else {
					XPathCrawler.getOut2().println(
							he.getHostaddress() + "  " + he.getCount()
									+ "    >20000");

				}
			} else if (he != null && (!he.isSeed())) {
				if (he.getCount() < 2000) {
					int check = putcheck(urle);
					if (check == 1) {
						addhost(hostname, 0);
					}
				} else {
					XPathCrawler.getOut2().println(
							he.getHostaddress() + "  " + he.getCount()
									+ "    >2000");

				}
			}
			int check = putcheck(urle);
			if (check == 1) {
				addhost(hostname, 0);
			}

		}
		
	}

	/**
	 * Method to add seed urls to the frontier
	 * @param scrapedlinks
	 * @throws InterruptedException
	 * @throws MalformedURLException
	 */
	public synchronized void addSeedPageLinks(ArrayList<String> scrapedlinks)
			throws InterruptedException, MalformedURLException {
		for (String s : scrapedlinks) {
			URLEntity urle = new URLEntity();
			URL url = new URL(s);
			urle.setURLname(s);
			String hostname = url.getHost();
			urle.setMethod("ROBOT");
			urle.setHostSeed(true);
			int check = putcheck(urle);
			if (check == 1) {
				addhost(hostname, 0);
			}
		}
	}

	public int getnodeID(String hostName) {

		int nodeID = 0;

		int hosthash = hostName.hashCode();

		nodeID = Math.abs(hosthash) % WorkerServer.peers.size();

		return nodeID;
	}

	public int getNodeNumber(String host) {

		int nodename = 0;
		try {

			MessageDigest mDigest = MessageDigest.getInstance("SHA1");
			byte[] result = mDigest.digest(host.getBytes());

			BigInteger hashValue = new BigInteger(result);
			BigInteger numWorkers = new BigInteger(
					Integer.toString(WorkerServer.peers.size()));
			BigInteger modulus = (hashValue).abs().mod(numWorkers);
			nodename = modulus.intValue();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();

		}
		return nodename;
	}

}