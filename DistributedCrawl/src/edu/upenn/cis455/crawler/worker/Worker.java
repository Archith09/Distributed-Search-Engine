package edu.upenn.cis455.crawler.worker;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import edu.upenn.cis455.crawler.CrawlerThreadPool;
import edu.upenn.cis455.crawler.MercatorWorker;
import edu.upenn.cis455.crawler.URLFrontier;
import edu.upenn.cis455.crawler.XPathCrawler;

/**
 * This class extends the WorkerServer and takes command line arguments to initialize a work node
 * in the distributed crawler
 */
public class Worker extends WorkerServer {

	public static String WebPageURL;
	public static String DBDirectory;
	public static long maxbyte;
	public static int stop = -1;
	public static boolean isCrawling;
	
	public static int NumberofHosts = 64;
	public static int workercount;
	public static ArrayList<MercatorWorker> crawlerthreads = new ArrayList<MercatorWorker>();
	public static CrawlerThreadPool crawlerpool;
	public static long crawlerstarttime = 0;

	public Worker(int myPort) throws MalformedURLException {
		super(myPort);
	}

	private static String master;
	private static String port;
	private static boolean updateMaster = true;
	public static String inputStore;
	public static String dbStore;

	public static XPathCrawler XPC;

	public static void main(String[] args) {
		// Get input in the form of IP and storage

		master = args[0];
		int workerIndex = Integer.parseInt(args[1]);
		String workerAddress = args[2];
		port = workerAddress.split(":")[1];
		if (!WorkerServer.peers.get(workerIndex).equals(workerAddress)) {
			System.out.println("WorkerIndex and workerAddress mismatch");
			System.out.println("Worker hardcoded with peers:");
			System.out.println(WorkerServer.peers.toString());
			return;
		}

		/*if (args.length < 3) {
			System.out.println("Please enter valid arguments");
			return;
		} else if (args.length == 6) {
			try {
				XPC = new XPathCrawler(args[3], args[4], Integer.parseInt(args[5]) * 1024);

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} else if (args.length == 7) {
			try {
				XPC = new XPathCrawler(args[3], args[4], Integer.parseInt(args[5]) * 1024, Integer.parseInt(args[6]));

			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}*/

		/*else*/ if (args.length == 13) {
			try {
				/*
				 * args[0] ==> Master IP args[1] ==> Node No args[2] ==> Self IP
				 * args[3] => seed url page args[4] => db directory args[5] =>
				 * max page size args[6] => max no of download args[7] =>
				 * crawler thread pool size args[8] => host no args[9] =>
				 * producer delay args[10] => checkpointing thread pool size
				 * args[11] => mercator worker delay args[12] =>bucket name
				 */

				XPC = new XPathCrawler(args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11],
						args[12]);

			} catch (Exception e3) {
			}
		}

		Worker.createWorker(workerIndex);

	}

	/*
	 * This method makes the worker node starting listening on a specific port
	 * and starts the update thread, which pings the master server about
	 * worker's status
	 */
	public static void createWorker(int workerIndex) {
		WorkerServer.createWorker(workerIndex, XPC);
		if (updateMaster) {
			UpdateThread ping = new UpdateThread();
			ping.start();
		}

	}

	/*
	 * Update Thread implements, pings the master server about the worker status
	 */
	public static class UpdateThread extends Thread {

		public UpdateThread() {
		}

		public void run() {
			while (!shutDown) {

				try {
					URL url = new URL("http://" + master + "/workerstatus?" + getQuery());
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setDoOutput(true);
					conn.setRequestMethod("GET");

					if (conn.getResponseCode() != 200) {
						System.out.println(conn.getResponseCode() + " Worker didn't recieve 200 back from master");
					}

					Thread.sleep(10000);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			System.exit(0);
		}

	}

	/*
	 * This method actually creates the status parameters of the workers as
	 * query, which will be sent to master server as http request
	 */
	private static String getQuery() throws UnsupportedEncodingException {
		String result = "port=" + port + "&alive=true";
		return result;
	}

}
