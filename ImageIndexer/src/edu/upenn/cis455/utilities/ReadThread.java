package edu.upenn.cis455.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.worker.Worker;

public class ReadThread extends Thread{
	
	private String store;
	public static int count = 1;
	public ReadThread(String store){
		this.store = store;
	}
	
	public void run(){
		
		File dir = new File(store);
		
		for(File file : dir.listFiles()){
			
			try {
				
				System.out.println("Reading File: " + file.getName() + "::::::::::::::::::::::::::::::::::::");
				
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = "";
				
				while ((line = reader.readLine()) != null) {
	                
					// If line is not empty
					if(!line.isEmpty()){
						System.out.println("Line " + count + " line = " + line);
						count++;
						Thread.sleep(Constants.READ_SLEEP);
						Worker.getQueue().put(line);
					}
					
	            }
				
				reader.close();
				Thread.sleep(100);
				
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
			
		}
		
		Worker.isFileReadingComplete = true;
		System.out.println("#####################################################################");
		System.out.println("Node finished reading all the files");
		System.out.println("#####################################################################");
		
	}
	
}
