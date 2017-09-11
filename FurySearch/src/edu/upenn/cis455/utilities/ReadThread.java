package edu.upenn.cis455.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.configuration.Packet;
import edu.upenn.cis455.worker.Worker;
import edu.upenn.cis455.worker.WorkerServer;

/*
 * This class is part of the distributed structure
 * This class implements read thread which reads a file line by line
 * and decides an appropriate node id to which the line should map and send it to its packet
 * If line maps to itself it moves the line to process queue
 * 
 * 
 */

public class ReadThread extends Thread{
	
	private final String DELIMITER = ";#;";
	private String store;
	public static int count = 1;
	public ReadThread(String store){
		this.store = store;
	}
	
	public void run(){
		
		// File dir = new File(store);
		File file = new File(store);
		
		// for(File file : dir.listFiles()){
			
			try {
				
				System.out.println("Reading File: " + file.getName() + "::::::::::::::::::::::::::::::::::::");
				
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = "";
				
				while ((line = reader.readLine()) != null) {
	                
					// If line is not empty
					if(!line.isEmpty()){
						// System.out.println(line);
						
						// TODO Assuming it for now, will have to change it later
						String key = line.split(DELIMITER)[0].trim();
						
						System.out.println("Line " + count + ": Key = " + key);
						count++;
						
//						if(count >= 3000000){
//							Constants.PACKET_SIZE = 5000;
//						}
//						
//						if(count >= 4000000){
//							Constants.PACKET_SIZE = 2500;
//						}
						
						Thread.sleep(Constants.READ_SLEEP);
						
						// Keeping a replica in the next node for fault failure
						int primary = WorkerHelper.getNodeNumber(key);
						//int secondary = (primary + 1) % Constants.peers.size();
						
						if(primary == WorkerServer.self){
							// System.out.println("Local primary: " + primary + " update: " + line);
							Worker.getQueue().put(line);
						} else {
							// System.out.println("Primary: " + primary + "Moving primary update: " + line + " to " + WorkerServer.peers.get(primary));
							WorkerServer.packetMap.get(primary).add(line);
						}
						
						/*if(secondary == WorkerServer.self){
							// System.out.println("Local secondary: " + secondary + " update: " + line);
							Worker.queue.put(line);
						} else {
							// System.out.println("Secondary: " + secondary + "Moving secondary update: " + line + "to " + WorkerServer.peers.get(secondary));
							WorkerServer.packetMap.get(secondary).add(line);
						}*/
						
					}
					
					// Check if remote queue has something to offer add it to the main queue
//					if(!WorkerServer.remoteQueue.isEmpty()){
//						System.out.println("Entered the remote Queue ! QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
//						for(int i = 0; i <= 100000; i++){
//							if(WorkerServer.remoteQueue.isEmpty()){
//								break;
//							}
//							Worker.queue.put(WorkerServer.remoteQueue.take());
//						}
//					}
					
	            }
				
				reader.close();
				Worker.isFileReadingComplete = true;
				
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//		}
		
		// The thread has finished reading the files so send whatever is in the packet
		for(java.util.Map.Entry<Integer, Packet> entry : WorkerServer.packetMap.entrySet()){
			
			if(entry.getKey() != WorkerServer.self){
				entry.getValue().sendPacket();
				// System.out.println(entry.getKey() + " was sent the final packet");
			}
			
		}
		System.out.println("#####################################################################");
		System.out.println("Node " + WorkerServer.self + " finished reading all the files");
		System.out.println("#####################################################################");
		
//		try {
//			Thread.sleep(1000 * 60 * 1);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
//		while(!WorkerServer.getRemoteQueue().isEmpty()){
//			System.out.println("Extracting objects from remote queue !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//			try {
//				String str = WorkerServer.getRemoteQueue().take()
//				Worker.queue.put(WorkerServer.getRemoteQueue().take());
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
//		for(int i = 1; i <= 128; i++){
//			Runnable worker = new WorkerThread("" + i);
//			Worker.threadPool.execute(worker);
//		}
		
	}
	
}
