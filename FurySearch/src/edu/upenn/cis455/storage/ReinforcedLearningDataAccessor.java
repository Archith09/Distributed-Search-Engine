package edu.upenn.cis455.storage;

import java.io.File;
import java.io.FileNotFoundException;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import edu.upenn.cis455.configuration.ReinforcedResponse;
import edu.upenn.cis455.utilities.ReinforcedEntry;

/*
 * 
 */

public class ReinforcedLearningDataAccessor {

	private static Environment environment;
	private static EntityStore store;
	public static int BDBInsert = 0;

	public static synchronized void openDB(String filePath)
			throws FileNotFoundException {

		File dbDirectory = new File(filePath);

		// Check if directory exists, if not create one
		if (!dbDirectory.exists()) {
			if (!dbDirectory.mkdirs()) {
				throw new FileNotFoundException(
						"Couldn't create environment directory");
			}
		}

		EnvironmentConfig eConfig = new EnvironmentConfig();
		StoreConfig sConfig = new StoreConfig();

		eConfig.setAllowCreate(true);
		sConfig.setAllowCreate(true);

		environment = new Environment(dbDirectory, eConfig);
		store = new EntityStore(environment, "EntityStore", sConfig);

	}
	
	public static synchronized void put(ReinforcedEntry obj) {
		PrimaryIndex<String, ReinforcedEntry> pIndex = store.getPrimaryIndex(String.class, ReinforcedEntry.class);
		System.out.print("Putting in BDB & printing count" + BDBInsert++);		
		pIndex.put(obj);
		environment.sync();
	}
	
	/*public static synchronized void putAll(ArrayList<ReinforcedEntry> obj) {
		
		if(obj == null || obj.size() == 0){
			return;
		}
		
		PrimaryIndex<String, ReinforcedEntry> pIndex = store.getPrimaryIndex(String.class, ReinforcedEntry.class);
		System.out.print("Line read: " + ReadThread.count + " and file reading is " + Worker.isFileReadingComplete + "Putting in BDB & printing count" + BDBInsert++);
		for(ReinforcedEntry entry : obj){
			pIndex.put(entry);
		}
		environment.sync();
	}
	*/
	
	public static synchronized ReinforcedResponse getResults(String query){
		PrimaryIndex<String, ReinforcedEntry> pIndex = store.getPrimaryIndex(String.class, ReinforcedEntry.class);
		ReinforcedEntry count = pIndex.get(query);
		ReinforcedResponse response = new ReinforcedResponse(query, 0);
		if(count != null){
			 response = new ReinforcedResponse(query, count.getCount());
		}
		
		return response;
	}
	
	public static synchronized void closeDB() {
		if (store != null)
			store.close();
		if (environment != null)
			environment.close();
	}
	
}
