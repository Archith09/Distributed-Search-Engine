package edu.upenn.cis455.worker;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.storage.CountDataAccessor;
import edu.upenn.cis455.utilities.ReadThread;

/*
 * Single node implementation of worker for generating index table
 * Used for image index and count index
 * 
 * 
 */
public class Worker {
	
	public static BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	public static ExecutorService threadPool = Executors.newFixedThreadPool(Constants.THREADS);
	public static ArrayList<WorkerThread> threads = new ArrayList<WorkerThread>();
	public static int TotalCount = 0;
	public static boolean isFileReadingComplete = false;
	public static boolean isShutDown = false;
	
	public static synchronized BlockingQueue<String> getQueue(){
		return queue;
	}
	
	public synchronized  static void increment(){
		TotalCount++;
	}
	
	public synchronized static int getCountBDB(){
		return TotalCount;
	}
	
	public static String inputStore;
	public static String dbStore;
	
	public static void main(String[] args) {
		// Get input in the form of IP and storage
		if (args.length < 3) {
			System.out.println("Please provide: <master IP:PORT> <worker Index> <worker IP:PORT> <optinal worker storage for read> <optional DB store index>");
			System.out.println("Note: Providing the optional storage parameter will start over writing index");
			return;
		}
		
		if(args.length > 1){
			String store = args[0];
			store = store.startsWith("/") ? store : "/" + store;
			store = store.endsWith("/") ? store : store + "/";
			
			if(!Files.exists(Paths.get(store))){
				System.out.println("file doesn't exist: " + store);
				return;
			}
			
			inputStore = store;
		}
		
		// Initialize the database here
		if(args.length > 2){
			String store = args[1];
			store = store.startsWith("/") ? store : "/" + store;
			store = store.endsWith("/") ? store : store + "/";
			
			if(!Files.isDirectory(Paths.get(store))){
				System.out.println("Invalid input directory: " + store);
				return;
			}
			
			dbStore = store;
			try {
				//ImageDataAccessor.openDB(dbStore);
				CountDataAccessor.openDB(dbStore);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("No Such directory exists for berkeley database");
				return;
			}
		}
		
		Constants.THREADS = Integer.parseInt(args[2]);
		Constants.BATCH_SIZE = Integer.parseInt(args[3]);
		Constants.READ_SLEEP = Integer.parseInt(args[4]);
		Constants.WRITE_SLEEP = Integer.parseInt(args[5]);
		
		start();

	}

	private static void start() {
		
		ReadThread read = new ReadThread(Worker.inputStore);
		read.setPriority(Thread.MAX_PRIORITY);
		read.start();
		
		for(int i = 1; i <= Constants.THREADS; i++){
			WorkerThread worker = new WorkerThread("" + i);
			Worker.threads.add(worker);
			Worker.threadPool.execute(worker);
		}
	}
	
	public static void shutdown(){
		isShutDown = true;
		ShutDownThread shut = new ShutDownThread();
		shut.start();
		
	}
	
	public static class ShutDownThread extends Thread {

		public ShutDownThread() {
		}

		public void run() {
			for(WorkerThread thread : threads){
				thread.interrupt();
			}
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Shutdown complete!");
			// ImageDataAccessor.closeDB();
		}

	}
	
	
	
}
