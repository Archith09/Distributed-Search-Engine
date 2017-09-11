package edu.upenn.cis455.crawler.utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *class to provide help methods to Distributed Crawler worker 
 */

public class WorkerHelper {
	
	public static HttpURLConnection sendJob(String dest, String reqType, String job, String parameters) throws IOException {
		URL url = new URL("http://"+ dest + "/" + job);
		System.out.println("Sending request to: " + url.toString());
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod(reqType);
		
		if (reqType.equals("POST")) {
			conn.setRequestProperty("Content-Type", "application/json");
			OutputStream os = conn.getOutputStream();
			byte[] toSend = parameters.getBytes();
			os.write(toSend);
			os.flush();
		} else
			conn.getOutputStream();
		
		return conn;
    }
	
}
