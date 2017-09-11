package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.net.whois.WhoisClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.upenn.cis455.storage.ProjectDBWrapper;
import edu.upenn.cis455.storage.dbentity.ImageEntity;
import edu.upenn.cis455.xpathengine.httpclient.Helper;

/**
 * 
 * class to parse the htmls and extract important information and images from the pages
 */
public class JSOUPParseJob {

	
	private Document doc1;

	
	/**
	 * Method to extract links from the html pages	
	 */
	public ArrayList<String> extractlinks(String pagecontent, String url)
			throws MalformedURLException {

		ArrayList<String> parsedlinks = new ArrayList<String>();

		this.doc1 = Jsoup.parse(pagecontent, url);
		Elements links = this.doc1.select("a[href]");
		for (Element l : links) {
			String anchorhref = l.attr("abs:href");
			// ##########

			// System.out.println("ahref of parsed URls::"
			// + anchorhref);

			String[] slashcheck = anchorhref.split("/");

			if (anchorhref != null && !anchorhref.isEmpty()
					&& slashcheck.length <= 20) {
				URL parsed = new URL(anchorhref);
				String host = parsed.getHost();
				if (parsed.getProtocol().equals("http")
						|| parsed.getProtocol().equals("https")) {
					if (!host.matches(".*[pP][oO][rR][nN].*")
							&& !host.matches(".*[xX][xX][xX].*")
							&& !host.matches(".*[fF][uU][cC][kK].*")
							&& !host.matches(".*[tT][uU][mM][bB][lL][rR].*")) {
						parsedlinks.add(CrawlerHelper.getniceurl(parsed));
					}
				}
			}
		}

		return parsedlinks;
	}

	/**
	 * 
	 * Method to extract images src and alt text from the html page
	 */
	public HashMap<String, String> extractImg() {
		Elements headings = this.doc1.select("img");
		HashMap<String, String> output = new HashMap<String, String>();

		if (headings != null) {

			for (Element element : headings) {
				String urlimage = element.attr("abs:src");
				String alt = headings.attr("alt");
				// System.out.println(urlimage + "  " + alt);
				if (urlimage != null && alt != null && urlimage.length() > 0
						&& alt.length() > 0) {
					output.put(urlimage, alt);

					/*
					 * ImageEntity image = new ImageEntity();
					 * image.setImageURL(urlimage); if(urlimage.contains("/")){
					 * urlimage =
					 * urlimage.substring(urlimage.lastIndexOf("/")+1);
					 * if(urlimage.contains(".")){
					 * image.setImageName(urlimage.substring
					 * (0,urlimage.indexOf("."))); }else{
					 * image.setImageName(urlimage); } }else{
					 * image.setImageName(urlimage); }
					 * 
					 * image.setImageSRC(alt);
					 * XPathCrawler.newWrapper.put(ImageEntity.class, image);
					 */
				}

			}
		}
		return output;

	}

	public static String extractBody(String pagecontent) {
		String content = Jsoup.parse(pagecontent).text();
		content = content.trim().replaceAll("[\r\n]+", " ");
		return content;
	}

	public static String extractTitle(String pagecontent) {
		Elements titles = Jsoup.parse(pagecontent).select("title");
		StringBuffer sb = new StringBuffer();
		for (Element element : titles) {
			sb.append(element.text().replaceAll("[\r\n]+", " "));
		}
		return sb.toString();
	}

	public static String extractHeadings(String rawContent) {
		Elements headings = Jsoup.parse(rawContent).select(
				"h1,h2,h3,h4,h5,h6, b, bold, em");
		StringBuffer sb = new StringBuffer();
		for (Element element : headings) {
			sb.append(element.text().replaceAll("[\r\n]+", " "));
			sb.append(" ");
		}
		return sb.toString();
	}

	public static String extractMetaData(String pagecontent) {
		Elements metaTags = Jsoup.parse(pagecontent).select("meta");
		// System.out.println(metaTags);
		StringBuffer sb = new StringBuffer();
		for (Element element : metaTags) {
			sb.append(element.attr("name") + " " + element.attr("content"));
			sb.append(" ");
		}
		return sb.toString();
	}

	public static String getdomaininfo(String host) {

		WhoisClient domaininfo = new WhoisClient();

		StringBuilder sb = new StringBuilder();

		try {
			domaininfo.connect(WhoisClient.DEFAULT_HOST);
			String Data = domaininfo.query(host);
			sb.append(Data);
			domaininfo.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();

	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String urltry = "https://www.w3schools.com";
		URL url = new URL(urltry);
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
		int responsecode = connection.getResponseCode();
		Map<String, List<String>> responseheaders = connection
				.getHeaderFields();

		InputStream ins = connection.getInputStream();
		ArrayList<String> rawdata = Helper.getbody(ins);
		StringBuffer sb = new StringBuffer();
		for (String s : rawdata) {

			sb.append(s);
		}

		// Using JSoup to parse the document

		String html = sb.toString();
		System.out.println("metadata: " + extractMetaData(html));
		System.out.println("title: " + extractTitle(html));
		System.out.println("body: " + extractBody(html));

		System.out.println("heading: " + extractHeadings(html));


	}

}
