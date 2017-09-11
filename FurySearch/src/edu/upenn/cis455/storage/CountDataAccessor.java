package edu.upenn.cis455.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import edu.upenn.cis455.configuration.CountResponse;
import edu.upenn.cis455.utilities.CountEntry;
import edu.upenn.cis455.utilities.ReadThread;
import edu.upenn.cis455.worker.Worker;

/*
 * 
 */
public class CountDataAccessor {

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
	
	public static synchronized void put(CountEntry obj) {
		PrimaryIndex<String, CountEntry> pIndex = store.getPrimaryIndex(String.class, CountEntry.class);
		System.out.print("Putting in BDB & printing count" + BDBInsert++);		
		pIndex.put(obj);
		environment.sync();
	}
	
	public static synchronized void putAll(ArrayList<CountEntry> obj) {
		
		if(obj == null || obj.size() == 0){
			return;
		}
		
		PrimaryIndex<String, CountEntry> pIndex = store.getPrimaryIndex(String.class, CountEntry.class);
		System.out.print("Line read: " + ReadThread.count + " and file reading is " + Worker.isFileReadingComplete + "Putting in BDB & printing count" + BDBInsert++);
		for(CountEntry entry : obj){
			pIndex.put(entry);
		}
		environment.sync();
	}
	
	public static synchronized CountResponse getResults(String query){
		PrimaryIndex<String, CountEntry> pIndex = store.getPrimaryIndex(String.class, CountEntry.class);
		CountEntry count = pIndex.get(query);
		CountResponse response = new CountResponse(query, 0);
		if(count != null){
			 response = new CountResponse(query, count.getCount());
		}
		
		return response;
	}
	
	public static synchronized void closeDB() {
		if (store != null)
			store.close();
		if (environment != null)
			environment.close();
	}
	
//	public static void main(String[] args){
//		
//		IndexEntry one = new IndexEntry("pennslyvania;#;url1", "pennslyvania", "pennslyvania;#;2.00000");
//		IndexEntry two = new IndexEntry("penn;#;url2", "penn", "penn;#;22.0");
//		IndexEntry three = new IndexEntry("peno;#;url3", "peno", "peno;#;29.0");
//		IndexEntry four = new IndexEntry("penn;#;url4", "penn", "penn;#;21.01");
//		IndexEntry eight = new IndexEntry("penn;#;url5", "penn", "penn;#;21.01");
//		IndexEntry nine = new IndexEntry("penn;#;url6", "penn", "penn;#;2.01");
//		IndexEntry five = new IndexEntry("uni;#;url1", "uni", "uni;#;23.0");
//		IndexEntry six = new IndexEntry("uni;#;url2", "uni", "uni;#;24.0");
//		IndexEntry seven = new IndexEntry("uni;#;url3", "uni", "uni;#;21.0");
//		try {
//			IndexDataAccessor.openDB("/home/cis455/testIndexDB/index1");
//			IndexDataAccessor.put(one);
//			IndexDataAccessor.put(two);
//			IndexDataAccessor.put(three);
//			IndexDataAccessor.put(four);
//			IndexDataAccessor.put(five);
//			IndexDataAccessor.put(six);
//			IndexDataAccessor.put(seven);
//			IndexDataAccessor.put(eight);
//			IndexDataAccessor.put(nine);
//			
//			QueryResponse result = IndexDataAccessor.getResults("september");
//			System.out.println(result.toString());
//			
//			IndexDataAccessor.closeDB();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//	}
	
}
