package edu.upenn.cis455.crawler;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

//import edu.upenn.cis455.crawler.worker.WorkerServer;
import edu.upenn.cis455.storage.dbentity.HostEntity;
import edu.upenn.cis455.storage.dbentity.RestoreEntity;
import edu.upenn.cis455.storage.dbentity.URLEntity;
import edu.upenn.cis455.webserver.requestandresponse.Helper;

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
	
		if (duplicatecheck.contains(urlnew) && urle.getMethod().equals("ROBOT")) {
			return -1;
		}

		duplicatecheck.add(urlnew);

		if (getbacklist().containsKey(url.getHost()) == false) {
			if (getbacklist().size() <= XPathCrawler.NumberofHosts) {
				getbacklist().put(url.getHost(), new ArrayList<URLEntity>());			
				getbacklist().get(url.getHost()).add(urle);
			
				
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
			getbacklist().get(url.getHost()).add(urle);
			
			
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

		if (getbacklist().get(h) != null && getbacklist().get(h).isEmpty()) { // also
																	// check
																	// null
			getbacklist().remove(h);
			Map.Entry<String, ArrayList<URLEntity>> header = getfromsecondary();

			if (header != null) {
				getbacklist().put(header.getKey(), header.getValue());
				addhost(header.getKey(), 0);

			}

			if (getbacklist().isEmpty() && frontierlist.isEmpty()) {
				System.out.println("SHUT DOWNNNNNNNNN");
				XPathCrawler.xpathcrawlershutdown();
			}
		} else if (getbacklist().get(h) != null) {

			URLEntity hosturl = getbacklist().get(h).remove(0);
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
	public synchronized void addPageLinks(ArrayList<String> scrapedlinks)
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
				}
				else{
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

	

}