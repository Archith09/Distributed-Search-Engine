package edu.upenn.cis455.crawler;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.upenn.cis455.storage.dbentity.HostEntity;
import edu.upenn.cis455.xpathengine.RobotsTxtInfo;
import edu.upenn.cis455.xpathengine.httpclient.URLInfo;

/**
 * class with methods to help crawler mercator processor worker
 * 
 */

public class CrawlerHelper {

	public static boolean linkNotAllowed(HostEntity he, String filepath) {
		// System.out.println("HELPER     should check here for notallowed links       1");
		HashMap<String, ArrayList<String>> disallowed = he.getRobottxt()
				.getdisallowed();
		// System.out.println("HELPER     should check here for notallowed links");
		ArrayList<String> cis455crawlerlist = disallowed.get("cis455crawler");
		ArrayList<String> universallist = disallowed.get("*");

		if (cis455crawlerlist != null) {
			for (String s : cis455crawlerlist) {
				if (filepath.startsWith(s)) {

					return true;
				}
			}
		} else if (universallist != null) {
			for (String s : universallist) {
				if (filepath.startsWith(s)) {

					return true;
				}
			}

		}
		return false;

	}

	public static RobotsTxtInfo parseRobot(ArrayList<String> arr) {
		RobotsTxtInfo robottxt = new RobotsTxtInfo();
		String currentuser = null;

		for (String s : arr) {
			String parse = s.trim().toUpperCase(); // just to compare case
													// insensitive header name;
			String realparse = s.trim();
			/*
			 * if(parse.startsWith("#")){
			 * 
			 * continue; }
			 */
			if (parse.startsWith("USER-AGENT")) {

				String[] parsedtoken = realparse.split(":|#");
				if (parsedtoken.length > 1) {
					String agent = parsedtoken[1].trim();
					if (agent.length() > 0) {
						robottxt.addUserAgent(agent);
						currentuser = agent;
						// System.out.println("HELPER print User_Agent:"+currentuser);
						robottxt.addCrawlDelay(currentuser, 0);
						// System.out.println("helper parse robot   1");
					}

				}
			} else if (parse.startsWith("DISALLOW")) {
				String[] parsedtoken = realparse.split(":|#");
				if (parsedtoken.length > 1) {
					String notallowed = parsedtoken[1].trim();
					// System.out.println("HELPER 1  DisAllow:" + notallowed);
					if (currentuser != null) {
						// System.out.println("HELPER 2  DisAllow:" +
						// notallowed);
						robottxt.addDisallowedLink(currentuser, notallowed);
						// System.out.println("helper parse robot   2");
					}
				}
			} else if (parse.startsWith("ALLOW")) {
				String[] parsedtoken = realparse.split(":|#");
				if (parsedtoken.length > 1) {
					String allowed = parsedtoken[1].trim();
					// System.out.println("HELPER 1 DisAllow:" + allowed);
					if (currentuser != null) {
						// System.out.println("HELPER 2 DisAllow:" + allowed);
						robottxt.addAllowedLink(currentuser, allowed);
						// System.out.println("helper parse robot   3");
					}
				}
			}

			else if (parse.startsWith("CRAWL-DELAY")) {
				String[] parsedtoken = realparse.split(":|#");
				if (parsedtoken.length > 1) {
					String crawlwait = parsedtoken[1].trim();
					// System.out.println("HELPER 1 Crawl-delay:" + crawlwait);
					if (currentuser != null) {
						robottxt.addCrawlDelay(currentuser,
								Integer.parseInt(crawlwait));
						// System.out.println("HELPER 2 Crawl-delay:" +
						// crawlwait);
						// System.out.println("helper parse robot   4");
					}
				}
			}

			else if (parse.startsWith("SITEMAP")) {
				String[] parsedtoken = realparse.split(":", 2);
				if (parsedtoken.length > 1) {
					String sitemaplinks = parsedtoken[1].trim();
					if (currentuser != null) {
						robottxt.addSitemapLink(sitemaplinks);
						// System.out.println("helper parse robot   5");
					}
				}
			}

		}

		return robottxt;

	}

	public static String getniceurl(URL urli) {

		if (urli.getPort() != -1) {
			return urli.getProtocol() + "://" + urli.getHost() + ":"
					+ urli.getPort() + urli.getPath();
		} else {
			return urli.getProtocol() + "://" + urli.getHost() + ":"
					+ urli.getDefaultPort() + urli.getPath();
		}

	}

	public static boolean headercheck(HashMap<String, String> headers) {

		boolean check = true;

		if (!headers.containsKey("content-type")) {
			return false;
		}

		if (headers.containsKey("content-type")
				&& !headers.get("content-type").contains("html")) {

			return false;
		}

		if (headers.containsKey("content-language")) {

			if (!headers.get("content-language").contains("en")) {

				return false;
			}
		}
		if (headers.containsKey("x-language")) {

			if (!headers.get("x-language").contains("en")) {

				return false;
			}
		}

		if (headers.containsKey("content-length")) {

			if (Long.parseLong(headers.get("content-length")) > XPathCrawler.maxbyte) {

				return false;
			}
		}
		return true;

	}
}
