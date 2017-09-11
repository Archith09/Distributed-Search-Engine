package edu.upenn.cis455.crawler.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.crawler.utilities.RequestFailException;
import edu.upenn.cis455.crawler.utilities.WorkerHelper;
import edu.upenn.cis455.storage.dbentity.URLEntity;

import edu.upenn.cis455.crawler.worker.WorkerServer;

/**
 * Class to open the connection to send batches of URLs to remote nodes
 * 
 */

public class Packet {
	
	int index; // To identify wo whom this packet belongs
	ArrayList<String> entities;
	
	public Packet(){}
	
	public Packet(int index){
		this.index = index;
		this.entities = new ArrayList<String>();
	}
	
	public synchronized void add(String entity){
		
		this.entities.add(entity);
		if(entities.size() >= Constants.PACKET_SIZE){
			sendPacket();
			System.out.println("PACKET SENT TO NODEEEEEEEEEEEEEEEEE: "+index + "XXXXXXXXXXXXXXXXXXXXXXXX+" +
					"\r\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		}
		
	}
	
	public ArrayList<String> getEntities(){
		return this.entities;
	}

	private void sendPacket() {
		
		
		/*try{
		
		ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        
        String parameter = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        String destination = WorkerServer.peers.get(this.index);
        int response = WorkerHelper.sendJob(destination, "POST", "insert", parameter).getResponseCode();
        
        if(response != 200){
        	throw new RequestFailException("Request to peer: " + destination + " failed with response " + response +
        			"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\r\nXXXXXX" +
        			"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\r\nXXXXXXXXXX" +
        			"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        }
        
        this.entities.clear();
		
		}*/
		ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        HttpURLConnection conn = null;
		
        try {
        	String parameter = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            String destination = WorkerServer.peers.get(this.index);
    		conn = WorkerHelper.sendJob(destination, "POST", "insert", parameter);
	       int response = conn.getResponseCode();
	       if(response != 200){
	       	throw new RequestFailException("Request to peer: " + destination + " failed with response " + response);
	       }
	       
	       InputStream ins = conn.getInputStream();
	       int ret = 0;
	       while ((ret = ins.read()) > 0) {
	         System.out.print((char) ret);
	       }
	       // close the inputstream to make the Http connection reusable
	       ins.close();
	       System.out.println("Packet was sent successfully. Check in Packet Class");
	       this.entities.clear();
		}
		
		
	/*catch (JsonProcessingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (RequestFailException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
		
		catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			try {
				InputStream es = ((HttpURLConnection)conn).getErrorStream();
				int ret = 0;
				// read the error response body
				while ((ret = es.read()) > 0) {
					System.out.print((char) ret);
				}
				// close the error stream for the connection to be re used 
				es.close();
			} catch (IOException ex) {
				// deal with the exception
				e.printStackTrace();
			}
			
		} catch (RequestFailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

		
	//}
}
