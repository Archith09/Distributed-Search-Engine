package edu.upenn.cis455.crawler.worker;

import static spark.Spark.setPort;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.crawler.CrawlerThreadPool;
import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.crawler.configuration.Packet;

/**
 * Simple listener for worker creation * 
 * 
 */
public class WorkerServer {
	static Logger log = Logger.getLogger(WorkerServer.class);

	//public static ArrayList<String> peers = new ArrayList<String>(Arrays.asList("52.206.12.58:8001", "34.204.10.73:8001", "52.55.101.123:8001"));
	public static ArrayList<String> peers = new ArrayList<String>(Arrays.asList("107.23.73.225:8000", "54.208.116.106:8000", "52.54.251.242:8000", "54.172.24.81:8000", "54.205.20.19:8000"));
	// IP:PORT of peers
	public static int self;						// Self index
	public static ConcurrentHashMap<Integer, Packet> packetMap;
	
	public static boolean shutDown = false;

	public static XPathCrawler XPChere;
	

	public WorkerServer(int myPort) throws MalformedURLException {

		log.info("Creating server listener at socket " + myPort);

		setPort(myPort);
		final ObjectMapper om = new ObjectMapper();
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		Spark.post(new Route("/query") {

			@Override
			public Object handle(Request arg0, Response arg1) {


				return "query";
			}

		});

		Spark.post(new Route("/insert") {

			@Override
			public  Object handle(Request request, Response response) {
				try {
					
					Packet packet = om.readValue(request.body(), Packet.class);
					System.out.println("ARRAYS RECIVED ********************************************************");
					ArrayList<String> nodearr = packet.getEntities();
					//XPChere.mercatorfrontier.addPageLinks(nodearr);
					
					for(String s : nodearr){
						//XPChere.remoteQ.put(s);
						//XPathCrawler.remoteQ.put(s);
						CrawlerThreadPool.getremoteQ().put(s);
					}
					
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" +
						"+\r\n++++++++++++++++BLOCKING QUEUE ROCKS");
				return "Insert Request recieved well ==================================================\r\n" +
						"===============================================================================\r\n" +
						"===============================================================================\r\n" +
						"================================================================================";
			}
		});

		Spark.post(new Route("/pushdata/:stream") {

			@Override
			public Object handle(Request arg0, Response arg1) {
				try {
					
					return "OK";
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					arg1.status(500);
					return e.getMessage();
				}

			}

		});

		/*
		 * This handles the shutdown call from the master server It kills the
		 * cluster and update thread
		 */
		Spark.post(new Route("/shutdown") {

			@Override
			public Object handle(Request arg0, Response arg1) {
				log.info("Shutdown worker!");
				shutDown = true;
				shutdown();
				return "Shutdown!";
			}
		});
		
		Spark.post(new Route("/begin") {

			@Override
			public Object handle(Request arg0, Response arg1) {
				System.out.println("Begin Index making");
				try {
					XPChere.begin();
					XPChere.beginCheckpointing();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return "Begin accepted";
			}
		});

	}

	public static void createWorker(int index, XPathCrawler XPC) {
		
		self = index;
		XPChere = XPC;
		String myAddress = "http://" + peers.get(index);
		
		// Initialize packet map
		packetMap = new ConcurrentHashMap<Integer, Packet>();
		for(int i = 0; i < peers.size(); i++){
			Packet packet = new Packet(i);
			packetMap.put(i, packet);
		}
		
		System.out.println("Initializing worker " + myAddress);
		log.debug("Initializing worker " + myAddress);

		URL url;
		try {
			url = new URL(myAddress);
			new WorkerServer(url.getPort());
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void shutdown() {
		//TODO
	}

	

}
