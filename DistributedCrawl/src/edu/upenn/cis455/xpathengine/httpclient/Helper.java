package edu.upenn.cis455.xpathengine.httpclient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;


/**
 * helper class for the http client
 * 
 */

public class Helper {
	
	//Helper function to make headers for response
	public static HashMap<String, String> makehead(ArrayList<String> head){
	
		HashMap<String, String> header = new HashMap<String, String>();
			
		String[] headerKeyValue;
	 	String mostrecentKey = "";
	 	
	 	for(String request : head){              
	 		//while((request = reader.readLine()) != null ){   	     		 
	 		headerKeyValue = request.split(":",2);
	 		if(headerKeyValue.length > 2){    			 
	 			return null;
	 		}
	 		else if (headerKeyValue.length == 2){
	 			String key = headerKeyValue[0].trim().toUpperCase(); 
			 
	 			String value = headerKeyValue[1].trim();    			
			 
	 			//CASE handling when multiple headers have same keys, we append all the values by comma separation
	 			if((header.containsKey(key))){
	 				String initialvalue = header.get(key);
	 				header.put(key, initialvalue +", " + value); 
				 
	 			}
			 
	 			else if(!key.equals("")){    					  
	 				mostrecentKey = key;
	 				header.put(key, value);         //check if we want to handle upper case or not // check multiple same keys
	 			}
				     			 
	 		}    		 
		 
	 		else if(headerKeyValue.length == 1){  
			 
	 			if(header.containsKey(mostrecentKey)&& Character.isWhitespace(headerKeyValue[0].charAt(0))){    				 
	 				String mostrecentValue = header.get(mostrecentKey); 
	 				//System.out.println(headerKeyValue[0] + "   rmp3");
	 				header.put(mostrecentKey,mostrecentValue + " " + headerKeyValue[0].trim());  // i think i missed the space here in MS1    				 
				 
	 			}  		   			     			 
	 		}	 
	 	}      
	
	 	return header;
	}
	
	
	
	
	//Create headers for the requesr to be sent by the crawler
	public static HashMap<String, String> crawlerHeaderslist(URLInfo url){
		
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Host", url.getHostName());
		//System.out.println("HELPER hostname  " + url.getHostName());
		headers.put("User-Agent", "cis455crawler");		
		headers.put("Connection", "close");		
		return headers;
		
	}
	

	
	//Date Parsing Methods
	static String date1 = "EEE, dd MMM yyyy HH:mm:ss zzz";
	static String date2 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
	static String date3 = "EEE MMM d HH:mm:ss yyyy";
	static String date4 = "EEE MMM d HH:mm:ss zzz yyyy";
	
	
	public static String getGMT(Date date){
		SimpleDateFormat gmtformat = new SimpleDateFormat(date1,Locale.US);
		TimeZone gmtTime = TimeZone.getTimeZone("GMT");
		gmtformat.setTimeZone(gmtTime);
		return gmtformat.format(date);
	}
	
	public static Date parseDate(String date){
		
		
		
		if(date==null || date.equals("")){
			return null;
					
		}
		int check = 0;
		SimpleDateFormat textformat;
		textformat = new SimpleDateFormat(date1);
		//textformat.setTimeZone(TimeZone.getTimeZone("GMT"));
		try{
			textformat.parse(date);	
			check = 1;
			return textformat.parse(date);
		}
		catch(ParseException e){
			
		}
		
		
		if(check == 0){
		textformat = new SimpleDateFormat(date2);
		//textformat.setTimeZone(TimeZone.getTimeZone("GMT"));
		try{
			textformat.parse(date);	
			check = 1;
			return textformat.parse(date);			
		}
		catch(ParseException e){
			
		}}
		
		
		if(check == 0){
		textformat = new SimpleDateFormat(date3);
		//textformat.setTimeZone(TimeZone.getTimeZone("GMT"));
		try{
			textformat.parse(date);	
			check = 1;
			return textformat.parse(date);			
		}
		catch(ParseException e){
			
		}}
		
		if(check == 0){
			textformat = new SimpleDateFormat(date4);
			//textformat.setTimeZone(TimeZone.getTimeZone("GMT"));
			try{
				textformat.parse(date);	
				check = 1;
				return textformat.parse(date);			
			}
			catch(ParseException e){
				
			}}
		
		
		
			return null;				
	}
	
	
	public static Date parseDateforServlet(String date) {
		
		
		try{
			if(date==null || date.equals("")){
				return null;
					
			}
		
		int check = 0;
		
		SimpleDateFormat textformat;
		if(check == 0){
		textformat = new SimpleDateFormat(date1);
		//textformat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
			textformat.parse(date);	
			check = 1;
			return textformat.parse(date);
		
		}
		
		if(check == 0){
		textformat = new SimpleDateFormat(date2);
		//textformat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
			textformat.parse(date);	
			check = 1;
			return textformat.parse(date);			
		}
		
		
		if(check == 0){
		textformat = new SimpleDateFormat(date3);
		//textformat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
			textformat.parse(date);	
			check = 1;
			return textformat.parse(date);			
		}
		
		if(check == 0){
			textformat = new SimpleDateFormat(date4);
			//textformat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
				textformat.parse(date);	
				check = 1;
				return textformat.parse(date);			
		
		
		}
		}
		
		catch(ParseException e){
			throw new IllegalArgumentException();
		}	
		
		return null;
					
	}	
	
	
	public static ArrayList<String> getbody(InputStream ins) throws IOException{
	
		InputStreamReader isr = new InputStreamReader(ins);
	    BufferedReader in = new BufferedReader(isr);
	    ArrayList<String> body = new ArrayList<String>();
	    StringBuilder sb = new StringBuilder();
	    String responseLine;
	    
	    while ((responseLine = in.readLine()) != null){
	    	//if(responseBuffer.toString().getBytes().length > Crawler.maxSize)
	    		//break;
	    	//sb.append(responseLine + "\n");
	    	body.add(responseLine);    	
	    }    
	    in.close();
	    return body;
	    //return sb.toString();
	}
	
}
