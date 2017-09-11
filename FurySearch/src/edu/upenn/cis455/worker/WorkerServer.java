package edu.upenn.cis455.worker;

import static spark.Spark.setPort;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.configuration.Packet;
import edu.upenn.cis455.storage.BackupDataAccessor;
import edu.upenn.cis455.storage.IndexDataAccessor;
import edu.upenn.cis455.utilities.ReadThread;

/*
 * This class implements spark listener in order to facilitate worker communication in a 
 * distributed clustrer
 * It supports method to recieve the database query, insert request and begin computation request
 * 
 * 
 */
public class WorkerServer {
	static Logger log = Logger.getLogger(WorkerServer.class);

	public static int self;						// Self index
	public static HashMap<Integer, Packet> packetMap;
	
	public static boolean shutDown = false;
	public static BlockingQueue<String> remoteQueue = new LinkedBlockingQueue<String>();
	private static int count = 1;
	
	public WorkerServer(int myPort) throws MalformedURLException {

		log.info("Creating server listener at socket " + myPort);

		setPort(myPort);
		final ObjectMapper om = new ObjectMapper();
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		
		Spark.get(new Route("/query") {

			@Override
			public Object handle(Request request, Response response) {
				
				String query = request.queryParams("search");
				int top = Integer.parseInt(request.queryParams("top"));
				System.out.println(query + " was asked to query");
				try {
					return om.writerWithDefaultPrettyPrinter().writeValueAsString(IndexDataAccessor.getResults(query, top));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					System.out.println("JSON EXCEPTION");
					e.printStackTrace();
					
				}
				response.status(500);
				return "Request failed";
			}

		});
		
		
		Spark.get(new Route("/backup") {

			@Override
			public Object handle(Request request, Response response) {
				
				String query = request.queryParams("search");
				int top = Integer.parseInt(request.queryParams("top"));
				System.out.println(query + " was asked to query");
				try {
					return om.writerWithDefaultPrettyPrinter().writeValueAsString(BackupDataAccessor.getResults(query, top));
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					System.out.println("JSON EXCEPTION");
					e.printStackTrace();
					
				}
				response.status(500);
				return "Request failed";
			}

		});

		Spark.post(new Route("/insert") {

			@Override
			public Object handle(Request request, Response response) {
				try {
					
					Packet packet = om.readValue(request.body(), Packet.class);
					System.out.println("*********** Recieved packet *********");
					for(String input : packet.getEntities()){
						WorkerServer.getRemoteQueue().put(input);
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
				return "Insert Request recieved well";
			}
		});

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
				log.info("Begin Index making");
				ReadThread read = new ReadThread(Worker.inputStore);
				read.setPriority(Thread.MAX_PRIORITY);
				read.start();
				
				for(int i = 1; i <= Constants.THREADS; i++){
					WorkerThread worker = new WorkerThread("" + i);
					Worker.threads.add(worker);
					Worker.threadPool.execute(worker);
				}
				
				return "Begin accepted";
			}
		});

	}

	public static void createWorker(int index) {
		
		self = index;
		String myAddress = "http://" + Constants.peers.get(index);
		
		// Initialize packet map
		packetMap = new HashMap<Integer, Packet>();
		for(int i = 0; i < Constants.peers.size(); i++){
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
		IndexDataAccessor.closeDB();
		BackupDataAccessor.closeDB();
		System.out.println("DB closed permanently! Bye !");
		System.exit(0);
		
	}

	public synchronized static BlockingQueue<String> getRemoteQueue(){
		return remoteQueue;
	}

}
