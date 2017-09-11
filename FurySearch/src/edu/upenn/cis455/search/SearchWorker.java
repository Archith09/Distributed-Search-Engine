package edu.upenn.cis455.search;

import java.util.HashMap;

import edu.upenn.cis455.configuration.QueryResponse;
import edu.upenn.cis455.utilities.IndexEntry;
import edu.upenn.cis455.utilities.SearchHelper;

/*
 * This method implements a worker program whose job is to query the index service and get results
 * These workers are part of the threadpool
 * 
 * 
 */

public class SearchWorker implements Runnable {

	private Thread t;
	private String threadName;
	
	SearchWorker(String name) {
		threadName = name;
	}

	@Override
	public void run() {
		while (true) {
			String word = "";

			try {
				word = Search.queue.take();
				long requestTimeFromIndexer = System.currentTimeMillis();
				HashMap<String, IndexEntry> response = new HashMap<String, IndexEntry>();
				
				QueryResponse qr = SearchHelper.searchWord(word, 2000);
				if(qr != null){
					response = qr.getResult();
					
					if(response == null){
						response = new HashMap<String, IndexEntry>();
					}

				}
				
				long requestReceptionTimeIndexer = System.currentTimeMillis();
				System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
				System.out.println("Results from Indexer returned in " + (requestReceptionTimeIndexer - requestTimeFromIndexer) + " for word: " + word);
				System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
				
				Search.queryMap.put(word, response);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void start() {
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

	public String getName() {
		return threadName;
	}

}
