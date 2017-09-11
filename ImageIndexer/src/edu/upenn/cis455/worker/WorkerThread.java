package edu.upenn.cis455.worker;

import java.util.ArrayList;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.storage.CountDataAccessor;
import edu.upenn.cis455.utilities.CountEntry;

/*
 * Simple worker thread which reads a file line by line (producer)
 * 
 * 
 */
public class WorkerThread extends Thread{
	
	private String name;
	// private ArrayList<ImageEntry> batch = new ArrayList<ImageEntry>();
	private ArrayList<CountEntry> batch = new ArrayList<CountEntry>();
	
	public WorkerThread(String name){
		this.name = name;
	}
	
	public void run(){
		
		try {
		
			while(!Worker.isShutDown){
				
				String input;
				input = Worker.getQueue().take();
				Worker.increment();
				// ImageEntry entry = new ImageEntry(input);
				CountEntry entry = new CountEntry(input);
				addToBatch(entry);
				System.out.println("Insert count: " + Worker.getCountBDB());
				if(Worker.isFileReadingComplete && Worker.getQueue().isEmpty()){
					Worker.shutdown();
				}
				
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		
		insert();
		System.out.println("************************************************************************** Thread exited after the total count of: " + Worker.getCountBDB());
		
	}
	
	public String toString(){
		return this.name;
	}
	
	public synchronized void addToBatch(CountEntry entry) throws InterruptedException{
		this.batch.add(entry);
		if(batch.size() == Constants.BATCH_SIZE){
			insert();
		}
	}
	
	public synchronized void insert() {
		CountDataAccessor.putAll(batch);
		batch.clear();
	}
	
}
