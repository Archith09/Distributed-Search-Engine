package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import com.sleepycat.persist.EntityStore;

import edu.upenn.cis.crawler.TikaLangDetect.TikaWrapper;
import edu.upenn.cis455.storage.ProjectDBWrapper;
import edu.upenn.cis455.storage.dbentity.DocSecondaryEntity;
import edu.upenn.cis455.storage.dbentity.HostEntity;
import edu.upenn.cis455.storage.dbentity.URLEntity;
import edu.upenn.cis455.storage.dbentity.DocEntity;
import edu.upenn.cis455.xpathengine.RobotsTxtInfo;
import edu.upenn.cis455.xpathengine.httpclient.Helper;
import edu.upenn.cis455.xpathengine.httpclient.MyHttpClient;
import edu.upenn.cis455.xpathengine.httpclient.MyHttpResponse;

/** 
 * class to process the link provided by the frontier
 */


public class MercatorProcessor {

	MyHttpClient MPClient = new MyHttpClient();
	URLFrontier MPmercatortfrontier = XPathCrawler.mercatorfrontier;
	private long MPmaxbyte = XPathCrawler.maxbyte;
	EntityStore store;

	public MercatorProcessor() throws Exception {
		store = XPathCrawler.newWrapper.getStore();
	}

	// Getting robot of each new host to check allowed and disallowed links and
	// crawl delays
	public HostEntity serverobot(URLEntity urle) throws IOException,
			InterruptedException {

		// TODO append robots.txt to url
		URL url;

		URL urlrobo = new URL(urle.getURLPagename());
		String urlrobot = urlrobo.getProtocol() + "://" + urlrobo.getHost()
				+ "/robots.txt";

		// System.out.println(urlrobot + "     ROBOT URL");

		// url = new URL(urle.getURLPagename() + "robots.txt");
		url = new URL(urlrobot);

		// System.out.println("DOING ROBOT ON:::" + url.toString());
		String method = urle.getMethod();
		String protocol = url.getProtocol();

		HttpURLConnection connection = null;
		if (protocol.equals("http")) {
			connection = (HttpURLConnection) url.openConnection();

		} else if (protocol.equals("https")) {
			connection = (HttpsURLConnection) url.openConnection();
		}
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Host", url.getHost());
		connection.setRequestProperty("User-Agent", "cis455crawler");
		connection.setRequestProperty("Connection", "close");
		connection.setRequestProperty("Accept-Language", "en;q=0.7");

		HostEntity hostentity = new HostEntity(url.getHost());
		int responsecode = connection.getResponseCode();
		// System.out.println("MP RES CODE " + responsecode);
		Map<String, List<String>> responseheaders = connection
				.getHeaderFields();
		MyHttpResponse HRS = new MyHttpResponse();
		HRS.setResponsemap(responseheaders);
		HRS.setLastaccessed(System.currentTimeMillis());
		HRS.setResponsestatus(responsecode);

		MercatorInfo.setTotalhits();

		// System.out.println("TIMESTAMP:" + Helper.getGMT(new Date()));
		// Got Robot so parse it and save information

		if (responsecode == 200) {

			InputStream ins = connection.getInputStream();
			// HRS.setResponsecontent(Helper.getbody(ins));
			HRS.setReponsebodycontent(Helper.getbody(ins));

			long delay = -12;
			RobotsTxtInfo robottxt = CrawlerHelper.parseRobot(HRS
					.getReponsebodycontent());
			String crawlerID = "cis455crawler";

			if (robottxt.getdisallowed().containsKey(crawlerID)) {
				crawlerID = "cis455crawler";
			} else if (robottxt.getdisallowed().containsKey("*")) {
				crawlerID = "*";
			} else if (!robottxt.getdisallowed().containsKey(crawlerID)
					&& !robottxt.getdisallowed().containsKey("*")) {
				crawlerID = "nocrawlerfound";
			}

			if (!crawlerID.equals("nocrawlerfound")) {
				hostentity.setAccessdenied(robottxt
						.getDisallowedLinks(crawlerID));
				if (robottxt.getCrawldelay().containsKey(crawlerID)) {
					delay = robottxt.getCrawlDelay(crawlerID); // rishi
					//delay = 0;
					// System.out.println("MPP   delay check for head   :" +
					// delay);
				} else {
					delay = 1;
					// even if we don't find any crawl delay i should set it to
					// 2sec for politeness
				}

			} else {
				delay = 1;
			}
			hostentity.setDelay(delay);
			hostentity.setRobottxt(robottxt);
		}

		else {

			// what if robot.txt request fails we set delay of 2 seconds for
			// politeness
			hostentity.setDelay(1);
			// an empty robots.txt is set to avoid null pointer exceptions in
			// crawler helper
			hostentity.setRobottxt(new RobotsTxtInfo());
		}

		// System.out.println("END OF ROBOT");
		// HostAccessor HA = new HostAccessor(store);
		// HA.put(hostentity);
		if (urle.isHostSeed()) {
			hostentity.setSeed(true);
		}
		XPathCrawler.newWrapper.put(HostEntity.class, hostentity);

		return hostentity;

	}

	// ########################################################################################################################
	// serving a head request
	public int checkheadtocontinue(URLEntity urle) throws IOException,
			InterruptedException {
		URL url = new URL(urle.getURLPagename());
		String method = urle.getMethod();
		String protocol = url.getProtocol();
		// System.out.println("DOING HEAD ON:::" + url.toString());
		HttpURLConnection connection = null;
		if (protocol.equals("http")) {
			connection = (HttpURLConnection) url.openConnection();
		} else if (protocol.equals("https")) {
			connection = (HttpsURLConnection) url.openConnection();
		}
		connection.setRequestMethod(method);
		connection.setRequestProperty("Host", url.getHost());
		connection.setRequestProperty("User-Agent", "cis455crawler");
		connection.setRequestProperty("Connection", "close");
		connection.setRequestProperty("Accept-Language", "en;q=0.7");
		urle = XPathCrawler.newWrapper.get(URLEntity.class,
				CrawlerHelper.getniceurl(url));
		long previousaccess = -1;
		if (urle != null) {
			previousaccess = urle.getlastaccesstime();
			// System.out.println("last accessed ::" +
			// urle.getlastaccesstime());
		}
		if (previousaccess != -1) {
			connection.setRequestProperty("IF-MODIFIED-SINCE",
					Helper.getGMT(new Date(previousaccess)));
		}
		// MyHttpRequest HRQ = new MyHttpRequest(url, method);
		// HRQ.setRequestinfo(Helper.crawlerHeaderslist(url));
		HostEntity hostentity = new HostEntity(url.getHost());
		int responsecode = connection.getResponseCode();
		// System.out.println("HEAD RESPONSE CODE" + responsecode);
		Map<String, List<String>> responseheaders = connection
				.getHeaderFields();
		MyHttpResponse HRS = new MyHttpResponse();
		HRS.setResponsemap(responseheaders);
		HRS.setLastaccessed(System.currentTimeMillis());
		HRS.setResponsestatus(responsecode);
		// URLAccessor URLA = new URLAccessor(store);
		// ########

		// System.out.println("TIMESTAMP:" + Helper.getGMT(new Date()));

		MercatorInfo.setTotalhits();
		String redirect = "";
		String contenttype = "";
		String contentlength = "";

		for (Map.Entry<String, List<String>> entry : responseheaders.entrySet()) {
			if (entry.getKey() != null) {
				String key = entry.getKey().toLowerCase().trim();
				// System.out.println("headerker:::" + key);
				String value = entry.getValue().get(0).trim();
				// System.out.println("headervalue::" + value);

				if (key.equals("content-type")) {
					contenttype = value.split(";")[0].toLowerCase().trim();

				}
				if (key.equals("content-length")) {
					contentlength = value.split(";")[0].toLowerCase().trim();
					// System.out.println("contentlength::" + contentlength);
				}
				if (key.equals("location")) {
					redirect = value;

				}
				HRS.getReponseheader().put(key, value);
			}
		}

		if (responsecode == 200) {

			InputStream ins = connection.getInputStream();
			HRS.setReponsebodycontent(Helper.getbody(ins));
			// HRS.setResponsecontent(Helper.getbody(ins));

			if (CrawlerHelper.headercheck(HRS.getReponseheader())) {

				// TODO check if size comparison is correct

				return 1;
			}
		}
		// Not modified
		else if (responsecode == 304 && urle != null) {
			// System.out.println("last accessed ::304 thrown");
			// DocAccessor doc = new DocAccessor(store);
			// String docCode = URLA.get(url.getURL()).getDocCode();
			// #######
			DocSecondaryEntity document;
			String docCode = XPathCrawler.newWrapper.get(URLEntity.class,
					CrawlerHelper.getniceurl(url)).getDocCode();
			document = XPathCrawler.newWrapper.get(DocSecondaryEntity.class,
					docCode);
			// ######

			// System.out.println(url.toString() + " : is not modified");
			if (document != null) {
				if (document.getDoctype().equals("html")) {
					// make function addpagelinks in frontier

					MPmercatortfrontier.addPageLinks(document.getParsedlinks());

				}
			}

			return -1;

		}

		else if (responsecode == 301) {
			// TODO should it be in capital
			// String redirect = HRS.getReponseheader().get("LOCATION");
			if (!redirect.equals("")) {
				URL urlred = new URL(redirect);
				// System.out.println("301::::" + urlred.toString());
				URLEntity urlredirect = new URLEntity();
				urlredirect.setURLname(redirect);
				urlredirect.setMethod("ROBOT");
				// System.out.println("REDIRECTED");
				MPmercatortfrontier.enqueuefrontier(urlredirect);
			}

			return -1;
		}

		else {

			// System.out.println("FAILED HEAD:" + url.toString());
		}

		return -1;
	}

	// #######################################################################################################################
	// serving a get request
	public void processgetrequest(URLEntity urle) throws IOException,
			InterruptedException, NoSuchAlgorithmException {
		URL url = new URL(urle.getURLPagename());
		// System.out.println("DOING GET ON::" + url.toString());
		String method = urle.getMethod();
		String protocol = url.getProtocol();
		HttpURLConnection connection = null;
		if (protocol.equals("http")) {
			connection = (HttpURLConnection) url.openConnection();
		} else if (protocol.equals("https")) {
			connection = (HttpsURLConnection) url.openConnection();
		}
		connection.setRequestMethod(method);
		connection.setRequestProperty("Host", url.getHost());
		connection.setRequestProperty("User-Agent", "cis455crawler");
		connection.setRequestProperty("Connection", "close");
		connection.setRequestProperty("Accept-Language", "en;q=0.7");
		int responsecode = connection.getResponseCode();

		Map<String, List<String>> responseheaders = connection
				.getHeaderFields();
		MyHttpResponse HRS = new MyHttpResponse();
		HRS.setResponsemap(responseheaders);

		HRS.setResponsestatus(responsecode);
		HRS.setLastaccessed(System.currentTimeMillis());

		MercatorInfo.setTotalhits();
		// System.out.println("TIMESTAMP:" + Helper.getGMT(new Date()));
		String contenttype = "";
		String contentlength = "";
		// System.out.println("GET OK");

		for (Map.Entry<String, List<String>> entry : responseheaders.entrySet()) {
			if (entry.getKey() != null) {
				String key = entry.getKey().toLowerCase().trim();
				String value = entry.getValue().get(0).trim();

				if (key.equals("content-type")) {
					contenttype = value.split(";")[0].toLowerCase().trim();
					// value = contentType;
				} else if (key.equals("content-length")) {
					contentlength = value.split(";")[0].toLowerCase().trim();
				}

				HRS.getReponseheader().put(key, value);
			}
		}

		// GET is OK
		if (HRS.getResponsestatus() == 200) {

			MercatorInfo.setallformats();
			InputStream ins = connection.getInputStream();
			HRS.setReponsebodycontent(Helper.getbody(ins));

			ArrayList<String> body = HRS.getReponsebodycontent();
			StringBuffer sb = new StringBuffer();
			for (String s : body) {
				sb.append(s);
			}

			// Using JSoup to parse the document
			String html = sb.toString();

			// String docCode = Integer.toString(HRS.getReponsebodycontent()
			// .hashCode());
			// System.out.println("=================================================");
			// System.out.println(TikaWrapper.detectLang(html));
			// System.out.println("=================================================");

			try {
				if (TikaWrapper.detectlangShuyo(html)) {
					String docCode = generateSHA1string(html);

					// System.out.println("MP DOCOCDE SET:" + docCode);
					urle.setDocCode(docCode);
					DocSecondaryEntity doccheck;
					doccheck = XPathCrawler.newWrapper.get(
							DocSecondaryEntity.class, docCode);
					if (doccheck != null) {
						// content seen no more further processing
						// System.out
						// .println("MP: Content has been already seen no need to process it");
						// System.out.println("content seen:   " +
						// url.toString());

					} else {
						doccheck = new DocSecondaryEntity();
						DocEntity docfullinfo = new DocEntity();

						// #######
						doccheck.setAddress(CrawlerHelper.getniceurl(url));
						docfullinfo.setAddress(CrawlerHelper.getniceurl(url));

						// doccheck.setAddress(geturl.getURL());
						doccheck.setDocCode(docCode);
						docfullinfo.setDocCode(docCode);
						// docfullinfo.setContent(HRS.getReponsebodycontent());

						if (contenttype.length() > 0) {
							if (contenttype.contains("html")) {

								// process html
								// add parsedurls to the doccheck list
								ArrayList<String> parsedlinks = new ArrayList<String>();
								JSOUPParseJob jsoup = new JSOUPParseJob();
								parsedlinks = jsoup.extractlinks(html,
										url.toString());
								// parsedlinks =
								// JSOUPParseJob.extractlinks(html,
								// url.toString());

								//HashMap<String, String> imgMap = jsoup
									//	.extractImg(); // TITLY
								
								
								// JSOUPParseJob.extractImage(html,
								// url.toString());
								/* Rishi */
								/*
								 * StanfordNER r = new StanfordNER(); // init
								 * model LinkedHashMap<String,
								 * LinkedHashSet<String>> identifyNER =
								 * r.identifyNER(html); //docfullinfo.setPerson
								 * (identifyNER.get("PERSON"));
								 * docfullinfo.setLocation
								 * (identifyNER.get("LOCATION"));
								 * System.out.println(
								 * "============================================="
								 * );
								 * System.out.println(docfullinfo.getLocation(
								 * )); System.out.println(
								 * "============================================="
								 * );
								 */
								// docfullinfo.setOrganization(identifyNER.get("ORGANIZATION"));
								/* Rishi */

								// Setting the title, metadata and the body
								// content
								// of
								// the html page
								docfullinfo.setMetadata(JSOUPParseJob
										.extractMetaData(html));
								docfullinfo.setTitle(JSOUPParseJob
										.extractTitle(html));
								docfullinfo.setBody(JSOUPParseJob
										.extractBody(html));
								docfullinfo.setHeadings(JSOUPParseJob
										.extractHeadings(html));
								//docfullinfo.setImageMap(imgMap); //TITLY
								MPmercatortfrontier.addPageLinks(parsedlinks);

								// Setting the extracted links of the html page
								doccheck.setParsedlinks(parsedlinks);
								docfullinfo.setParsedlinks(parsedlinks);
								System.out.println(MercatorInfo.getTotal()
										+ "::" + MercatorInfo.getallformats()
										+ ":  " + url.toString());

								if (MercatorInfo.getTotal() % 10000 == 0) {
									HashMap<String, ArrayList<URLEntity>> mp = MPmercatortfrontier
											.getbacklist();
									XPathCrawler.getOut3().println(
											Helper.getGMT(new Date()));
									System.out
											.println("ENTERING THE CHECKPOINT $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
									for (String host : mp.keySet()) {
										ArrayList<URLEntity> arr = mp.get(host);
										int size = 0;
										if (arr.size() >= 15)
											size = 15;
										else
											size = arr.size();
										if (arr.size() != 0) {
											for (int j = 0; j < size; j++) {
												URLEntity ue = arr.get(j);
												XPathCrawler.getOut3().println(
														ue.getURLPagename());
												XPathCrawler.getOut3().println(
														"\n");
											}
										}
										arr.clear();
									}
									mp.clear();
									// XPathCrawler.getOut3().println("\n");
								}
								// System.out.println("parsed links should be added MPPPPPPP");
								doccheck.setDoctype("html");
								docfullinfo.setDoctype("html");

								// 2 changes i made after storm implementation
								doccheck.setType(HRS.getReponseheader().get(
										"content-type"));
								docfullinfo.setType(HRS.getReponseheader().get(
										"content-type"));

								XPathCrawler.newWrapper.put(
										DocSecondaryEntity.class, doccheck);
								XPathCrawler.newWrapper.put(DocEntity.class,
										docfullinfo);
								MercatorInfo.setTotal();

								HostEntity hostentity = XPathCrawler.newWrapper
										.get(HostEntity.class, url.getHost());
								hostentity.setCount();
								XPathCrawler.newWrapper.put(HostEntity.class,
										hostentity);
								// XPathCrawler.getOut1().println(url.toString()
								// + "     " + MercatorInfo.getTotal());

							}
						}

						// any raw data

						// ProjectDBWrapper.put(DocSecondaryEntity.class,
						// doccheck);
						// ProjectDBWrapper.put(DocEntity.class, docfullinfo);

					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		else {
			// MercatorInfo.setTotal();
			// System.out.println("FAiled::::" + url.toString());
			// get failed;
		}
		urle.setlastaccesstime(HRS.getLastaccessed());
		// System.out.println("MP:: last access set" + HRS.getLastaccessed()
		// + "for " + urle.getURLPagename());
		XPathCrawler.newWrapper.put(URLEntity.class, urle);
	}

	public void MercatorURLProcess(URLEntity urle) throws IOException,
			InterruptedException, NoSuchAlgorithmException {

		URL url = new URL(urle.getURLPagename());
		String method = urle.getMethod();

		String hostname = url.getHost();

		String filepath = url.getPath();

		// HostAccessor HA = new HostAccessor(DBWrapper.store);
		HostEntity hostentity;
		hostentity = XPathCrawler.newWrapper.get(HostEntity.class, hostname);
		long delay = 1; // previous -12

		if (hostentity != null) {
			 delay = hostentity.getDelay();
			//delay = 0;
			// System.out.println("CHECKING DELAY IN MP: " + delay);
		}

		// GET working
		if (method.equals("GET")) {
			processgetrequest(urle);
			// System.out.println("AFter first get");
			MPmercatortfrontier.addhost(hostname, delay);
		}

		// HEAD working

		else if (method.equals("HEAD")) {
			int checkwhethertoget = checkheadtocontinue(urle);
			if (checkwhethertoget == 1) {
				urle.setMethod("GET");
				// we add url only wen it passes all tests and then work on it
				MPmercatortfrontier.addurlentity(urle, delay);
			} else if (checkwhethertoget == -1) {
				// we add url only wen it passes all tests and then work on it
				// get head value of host
				MPmercatortfrontier.addhost(hostname, delay);
			}
		}

		// ROBOT working
		else if (method.equals("ROBOT")) {
			if (hostentity == null) {
				// hit the server and get its robot.txt
				hostentity = serverobot(urle);
				if (hostentity != null) {
					delay = hostentity.getDelay();
					//delay = 0;

				}
			} else {
				delay = 1;
			}

			if (CrawlerHelper.linkNotAllowed(hostentity, filepath)) {

				delay = 1;
				// get head value of host since this link wont be processed
				MPmercatortfrontier.addhost(hostname, delay);

			} else {
				urle.setMethod("HEAD");
				// System.out.println("MP should now set delay for head request      "
				// + delay);
				MPmercatortfrontier.addurlentity(urle, delay);
			}
		}
	}

	public String generateSHA1string(String input)
			throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return sb.toString();
	}
}
