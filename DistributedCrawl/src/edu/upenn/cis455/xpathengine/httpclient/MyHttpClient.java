package edu.upenn.cis455.xpathengine.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
//import org.w3c.dom.Document;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/** 
 * class to make my own http client
 */

public class MyHttpClient {

	
	public MyHttpClient(){		
	}	
	
	public MyHttpResponse getresponsefromrequest(MyHttpRequest HRQ) throws IOException{
		
		
		String hostName = HRQ.getRequestURLInfo().getHostName();
		int portNo = HRQ.getRequestURLInfo().getPortNo();
		Socket requestsocket = new Socket(hostName, portNo);
		
		InputStreamReader reader = new InputStreamReader(requestsocket.getInputStream());
	    BufferedReader in = new BufferedReader(reader);
	    OutputStream outs = requestsocket.getOutputStream();
	    PrintWriter out = new PrintWriter(outs);
	    
	    String filePath = HRQ.getRequestURLInfo().getFilePath();
	    //System.out.println("Client test filepath" + filePath);
	    HashMap<String, String> requestinfo = HRQ.getRequestinfo();
	    
	    
	    String requestmethod = HRQ.getRequestmethod();
	    if(requestmethod.equals("ROBOT")){
	    	requestmethod = "GET";
	    	filePath = "/robots.txt";
	    }	    
	    
	    //Sending the request
	    StringBuffer sb = new StringBuffer();
	    
	    sb.append(requestmethod + " " + filePath +  " HTTP/1.1\r\n");
	    
	    for(String k : requestinfo.keySet()){
	    	sb.append(k+": "+requestinfo.get(k) + "\r\n");	    	
	    }
	    sb.append("\r\n");
	    
	    
	    
	    out.write(sb.toString());
	    out.flush();
	    
	    
	    //start reading the response
	    
	    String firstline = in.readLine();
	    
	    String readresponse;
	    ArrayList<String> responseinfo = new ArrayList<String>();
	    ArrayList<String> body = new ArrayList<String>();
	    //StringBuffer sbb = new StringBuffer();
	    int headersread = 0;
	    while(!requestsocket.isClosed()  && (readresponse = in.readLine())!=null){
	    	if(readresponse.length() != 0){
	    		//System.out.println("client test    "+readresponse);
	    		if(headersread == 0){
	    			responseinfo.add(readresponse);
	    		}
	    		else if(headersread == 1){
	    			body.add(readresponse);
	    		}	    		
	    	}
	    	else{
	    		headersread = 1;
	    	}	    	
	    }
	    
	    
	    MyHttpResponse HRS = new MyHttpResponse();
	    if(firstline == null){
	    	HRS.setResponsestatus(000);
	    }
	    String[] firstlinecontent = firstline.split("\\s+");
	    if(firstlinecontent.length < 3){
	    	HRS.setResponsestatus(000);
	    }
	    else{
	    HRS.setResponsestatus(Integer.parseInt(firstlinecontent[1]));
	    }
	    HRS.setReponseheader(Helper.makehead(responseinfo));
	    HRS.setReponsebodycontent(body);	
	    HRS.setLastaccessed(System.currentTimeMillis());
	    //System.out.println("HTTP CLIENT: Last accessed" + HRS.getLastaccessed());
	    return HRS;	
		
	}
}
