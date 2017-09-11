package edu.upenn.cis455.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

import edu.upenn.cis455.configuration.QueryResponse;
import edu.upenn.cis455.utilities.IndexEntry;
import edu.upenn.cis455.utilities.ReadThread;
import edu.upenn.cis455.worker.Worker;

/*
 * 
 */
public class IndexDataAccessor {

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
	
	public static synchronized void put(IndexEntry obj) {
		PrimaryIndex<String, IndexEntry> pIndex = store.getPrimaryIndex(String.class, IndexEntry.class);
		System.out.print("Putting in BDB & printing count" + BDBInsert++);		
		pIndex.put(obj);
		environment.sync();
	}
	
	public static synchronized void putAll(ArrayList<IndexEntry> obj) {
		
		if(obj == null || obj.size() == 0){
			return;
		}
		
		PrimaryIndex<String, IndexEntry> pIndex = store.getPrimaryIndex(String.class, IndexEntry.class);
		System.out.print("Line read: " + ReadThread.count + " and file reading is " + Worker.isFileReadingComplete + "Putting in BDB & printing count" + BDBInsert++);
		for(IndexEntry entry : obj){
			pIndex.put(entry);
		}
		environment.sync();
	}
	
	public static synchronized QueryResponse getResults(String query, int top){
		
		// Return only top 2000 results sorted by the score value
		PrimaryIndex<String, IndexEntry> pIndex = store.getPrimaryIndex(String.class, IndexEntry.class);
		SecondaryIndex<String, String, IndexEntry> scoreIndex = store.getSecondaryIndex(pIndex, String.class, "tfIdf");
		
		query = query + ";";
		char[] stop = query.toCharArray();
		final int lastCharIndex = stop.length - 1;
		stop[lastCharIndex]++;
		
		EntityCursor<IndexEntry> cursor = scoreIndex.entities(query, true, String.valueOf(stop), false);
		HashMap<String, IndexEntry> results = new HashMap<String, IndexEntry>();
		QueryResponse response = new QueryResponse();
		try {
			
			int count = 1;
			IndexEntry row = cursor.last();
			if(row == null){
				return response;
			}
			
			results.put(row.getWordUrl(), row);
			while(count < top){
				row = cursor.prev();
				
				if(row == null)
					break;
				
				results.put(row.getWordUrl(), row);
				count++;
			}
			
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		
		response.setResult(results);
		
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
