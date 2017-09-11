package edu.upenn.cis455.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

/**
 * 
 * Class for Berkeley DB wrapper for the final project
 *
 */

public class ProjectDBWrapper {

	public static String envDirectory = null;
	public static Environment myEnv;
	public static EntityStore store;

	/* Rishi */ 
	public static ProjectDBWrapper obj = null;
	
	public static ProjectDBWrapper getInstance() throws Exception{
		if(ProjectDBWrapper.envDirectory == null)
			throw new Exception ("Set environment directory");
		
		if(obj == null){
			obj = new ProjectDBWrapper(ProjectDBWrapper.envDirectory);
		}
		
		return obj;
	}
	/* Rishi */
	
	public ProjectDBWrapper(String s) {
		// envDirectory = s;

		envDirectory = s;
		initialize(envDirectory);
	}

	// starting the database
	public static void initialize(String s) {

		// create storage directory if it does not exist
		File envDirectoryFile = new File(s);

		if (!envDirectoryFile.exists()) {
			try {
				envDirectoryFile.mkdirs();

			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (Exception fe) {
				fe.printStackTrace();
			}
		}

		try {
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(true);
			myEnv = new Environment(envDirectoryFile, envConfig);
			StoreConfig sc = new StoreConfig();
			sc.setAllowCreate(true);

			store = new EntityStore(myEnv, "EntityStore", sc);

		} catch (DatabaseException dbecx) {
			dbecx.printStackTrace();

		}
	}

	public Environment getMyEnv() {
		return myEnv;
	}

	public EntityStore getStore() {
		return store;
	}

	// closing the store and the database
	public synchronized void close() {
		
		System.out.println("closing DBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");

		if (store != null) {
			try {
				store.close();
			} catch (DatabaseException dbe) {
			}
		}

		if (myEnv != null) {
			try {
				// Finally, close environment.

				myEnv.close();
			} catch (DatabaseException dbe) {
			}
		}
	}


	public <T> T put(Class<T> entityClass, T value) {
		
		synchronized(obj){
			PrimaryIndex<String, T> primaryIndex = store.getPrimaryIndex(
					String.class, entityClass);

			T oldValue = primaryIndex.put(value);

			myEnv.sync();
			
			obj.notifyAll();	
			return oldValue;
		}
			

	}

	public <T> T deleteandget(Class<T> entityClass, String key) {
		synchronized(obj){
			PrimaryIndex<String, T> primaryIndex = store.getPrimaryIndex(
					String.class, entityClass);

			T value = primaryIndex.get(key);
			primaryIndex.delete(key);
			obj.notifyAll();
			return value;
		}
		

	}

	public <T> T get(Class<T> entityClass, String key) {
		
		synchronized(obj){
			PrimaryIndex<String, T> primaryIndex = store.getPrimaryIndex(
					String.class, entityClass);

			T value = primaryIndex.get(key);
			obj.notifyAll();
			return value;
		}
		
	}

	public <T> Map<String, T> map(Class<T> entityClass) {
		synchronized(obj){
			PrimaryIndex<String, T> primaryIndex = store.getPrimaryIndex(
					String.class, entityClass);

			obj.notifyAll();
			return primaryIndex.map();
		}
		
		
		
	}
	
	
	
	
	public <T> ArrayList<T> getallentities(Class<T> entityclass) {
		ArrayList<T> docList = new ArrayList<T>();			
			
			synchronized(obj){
				PrimaryIndex<String, T> primaryIndex = store.getPrimaryIndex(String.class, entityclass);
				EntityCursor<T> cursor = primaryIndex.entities();
				
				for (T d : cursor) {
					docList.add(d);
				}
				cursor.close();	
				obj.notifyAll();
				return docList;
			}
		
	}
	

	public <T> ArrayList<T> getAndRemoveallentities(Class<T> entityclass) {
		ArrayList<T> doclist = new ArrayList<T>();

			synchronized(obj){
				PrimaryIndex<String, T> primaryIndex = store.getPrimaryIndex(String.class, entityclass);
				EntityCursor<T> cursor = primaryIndex.entities();
				
				for (T d : cursor) {
					doclist.add(d);
					cursor.delete();
				}
				cursor.close();
				obj.notifyAll();
			}
			return doclist;
	}	

}
