package edu.upenn.cis455.worker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.storage.BackupDataAccessor;
import edu.upenn.cis455.storage.IndexDataAccessor;

/*
 * This class extends the WorkerServer and takes command line arguments to initialize a work node
 * in the distributed cluster
 * 
 * This is the house of the process queues and worker thread pools
 * and a worker member of the distributed cluster
 * 
 * 
 */
public class Worker extends WorkerServer {
	
	public static BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	public static ExecutorService threadPool = Executors.newFixedThreadPool(Constants.THREADS);
	public static ArrayList<WorkerThread> threads = new ArrayList<WorkerThread>();
	public static int TotalCount = 0;
	public static boolean isFileReadingComplete = false;
	
	public static synchronized BlockingQueue<String> getQueue(){
		return queue;
	}
	
	public Worker(int myPort) throws MalformedURLException {
		super(myPort);
	}
	
	public synchronized  static void increment(){
		TotalCount++;
	}
	
	public synchronized static int getCountBDB(){
		return TotalCount;
	}
	
	
	private static String master;
	private static String port;
	private static boolean updateMaster = true;
	public static String inputStore;
	public static String dbStore;
	public static String backupStore;
	
	
	public static void main(String[] args) {
		// Get input in the form of IP and storage
		if (args.length < 3) {
			System.out.println("Please provide: <master IP:PORT> <worker Index> <worker IP:PORT> <optinal worker storage for read> <optional DB store index>");
			System.out.println("Note: Providing the optional storage parameter will start over writing index");
			return;
		}
		
		master = args[0];
		int workerIndex = Integer.parseInt(args[1]);
		String workerAddress = args[2];
		port = workerAddress.split(":")[1];
		if(!Constants.peers.get(workerIndex).equals(workerAddress)){
			System.out.println("WorkerIndex and workerAddress mismatch");
			System.out.println("Worker hardcoded with peers:");
			System.out.println(Constants.peers.toString());
			return;
		}
		
		if(args.length > 3){
			String store = args[3];
//			store = store.startsWith("/") ? store : "/" + store;
//			store = store.endsWith("/") ? store : store + "/";
			
			if(!Files.exists(Paths.get(store))){
				System.out.println("file doesn't exist: " + store);
				return;
			}
			
			inputStore = store;
				
		}
		
		// Initialize the database here
		if(args.length > 4){
			String store = args[4];
			store = store.startsWith("/") ? store : "/" + store;
			store = store.endsWith("/") ? store : store + "/";
			
			if(!Files.isDirectory(Paths.get(store))){
				System.out.println("Invalid input directory: " + store);
				return;
			}
			
			dbStore = store;
			try {
				IndexDataAccessor.openDB(dbStore);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("No Such directory exists for berkeley database");
				return;
			}
		}
		
		// Initialize the backup store here  
		if(args.length > 5){
			String store = args[5];
			store = store.startsWith("/") ? store : "/" + store;
			store = store.endsWith("/") ? store : store + "/";
			
			if(!Files.isDirectory(Paths.get(store))){
				System.out.println("Invalid input directory: " + store);
				return;
			}
			
			backupStore = store;
			try {
				BackupDataAccessor.openDB(backupStore);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("No Such directory exists for backup berkeley database");
				return;
			}
		}
		
		
		Constants.THREADS = Integer.parseInt(args[6]);
		Constants.BATCH_SIZE = Integer.parseInt(args[7]);
		Constants.PACKET_SIZE = Integer.parseInt(args[8]);
		Constants.READ_SLEEP = Integer.parseInt(args[9]);
		Constants.WRITE_SLEEP = Integer.parseInt(args[10]);
		
		Worker.createWorker(workerIndex);

	}

	/*
	 * This method makes the worker node starting listening on a specific port
	 * and starts the update thread, which pings the master server about
	 * worker's status
	 */
	public static void createWorker(int workerIndex) {
		WorkerServer.createWorker(workerIndex);
		if(updateMaster){
			UpdateThread ping = new UpdateThread();
			ping.start();
		}
		
	}

	/*
	 * Update Thread implements, pings the master server about the worker heart beat
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
					
					InputStream ins = conn.getInputStream();
			        while (ins.read() > 0) { }
			        ins.close();
			        
					Thread.sleep(10000);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			System.exit(0);
		}

	}

	/*
	 * This method sends the heartbeat message to the master
	 */
	private static String getQuery() throws UnsupportedEncodingException {
		String result = "port=" + port + "&alive=true"; 
		return result;
	}

}
