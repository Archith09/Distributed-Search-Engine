package edu.upenn.cis455.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.configuration.QueryResponse;
import edu.upenn.cis455.index.NLP;

public class SearchHelper {
	
	final static ObjectMapper om = new ObjectMapper();
	
	public static QueryResponse searchWord(String query, int top){
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		query = NLP.process(query);
		query = NLP.stem(query);
		int node = getNodeNumber(query);
		System.out.println(query + " this was mapped to " + node + " by the master");
		return sendIndexRequest(node, query, top);
	}
	
	public static String getResults(String query, int top){
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		query = NLP.process(query);
		query = NLP.stem(query);
		int node = getNodeNumber(query);
		System.out.println(query + " this was mapped to " + node + " by the master");
		String html = "<h3>" + 
					  sendIndexRequest(node, query, top) +
					  "</h3>";
						
		return html;
	}
	
	public static QueryResponse sendIndexRequest(int node, String query, int top) {
		String response;
		QueryResponse result = null;
		try {
			URL url = new URL("http://" + Constants.peers.get(node) + "/query?search=" + query + "&top=" + top);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			
			if(conn.getResponseCode() != 200){
				System.out.println("node no::: "+ node + "   query::" + query + "   response code::" + conn.getResponseCode() + "MAIN ++++++++++++++++++++++++++");
				throw new IOException();
			}
			
			InputStream ins = conn.getInputStream();
			response = convertStreamToString(ins);
			ins.close();
			
			result = om.readValue(response, QueryResponse.class);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// TODO make backup request
			int backup = (node + 1) % Constants.peers.size();
			System.out.println("Sending backup request to node " + backup);
			QueryResponse backUpResponse = backUpRequest(backup, query, top);
			
			if(backUpResponse != null){
				System.out.println("Backup didn't return null");
				result = backUpResponse;
			} else {
				System.out.println("Backup resulted null");
			}
			
		}
		
		return result;
	}
	
	public static QueryResponse backUpRequest(int node, String query, int top){
		String response;
		QueryResponse result = null;
		try {
			
			URL url = new URL("http://" + Constants.peers.get(node) + "/backup?search=" + query + "&top=" + top);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			
			if(conn.getResponseCode() != 200){
				System.out.println("node no::: "+ node + "   query::" + query + "   response code::" + conn.getResponseCode() + "BACKUP=================================================");
				throw new IOException();
			}
			
			InputStream ins = conn.getInputStream();
			response = convertStreamToString(ins);
			ins.close();
			
			result = om.readValue(response, QueryResponse.class);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	static String convertStreamToString(java.io.InputStream is) {
	    @SuppressWarnings("resource")
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	public static int getNodeNumber(String key) {

		int nodename = 0;
		try {

			MessageDigest mDigest = MessageDigest.getInstance("SHA1");
			byte[] result = mDigest.digest(key.getBytes());

			BigInteger hashValue = new BigInteger(result);
			BigInteger numWorkers = new BigInteger(Integer.toString(Constants.peers.size()));
			BigInteger modulus = hashValue.mod(numWorkers);
			nodename = modulus.intValue();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return Math.abs(nodename);
	}
	
	
}
