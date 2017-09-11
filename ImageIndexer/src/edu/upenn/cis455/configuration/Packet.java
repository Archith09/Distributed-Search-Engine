package edu.upenn.cis455.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.utilities.RequestFailException;
import edu.upenn.cis455.utilities.WorkerHelper;

public class Packet {
	
	int index; // To identify wo whom this packet belongs
	ArrayList<String> entities = new ArrayList<String>();
	
	public Packet(){}
	
	public Packet(int index){
		this.index = index;
		//this.entities = new ArrayList<String>();
	}
	
	public void add(String entity){
		
		this.entities.add(entity);
		if(entities.size() == Constants.PACKET_SIZE){
			sendPacket();
		}
		
	}
	
	public ArrayList<String> getEntities(){
		return this.entities;
	}

	public void sendPacket() {
		
		ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        HttpURLConnection conn = null;
		
        try {
        	String parameter = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            String destination = Constants.peers.get(this.index);
    		conn = WorkerHelper.sendJob(destination, "POST", "insert", parameter);
	        int response = conn.getResponseCode();
	        if(response != 200){
	        	throw new RequestFailException("Request to peer: " + destination + " failed with response " + response);
	        }
	        
	        // This is to ensure that the Http connection is kept alive and is re usable
	        InputStream ins = conn.getInputStream();
	        while (ins.read() > 0) { }
	        ins.close();
	        
	        this.entities.clear();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			try {
				InputStream es = ((HttpURLConnection) conn).getErrorStream();
				while (es.read() > 0) {	}
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
	
}
