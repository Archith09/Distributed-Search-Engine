package edu.upenn.cis455.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

/*
 * DBWrapper class which supports basic methods like put, get, contains, remove with Berkeley DB
 */
public class DBMethods {

	private static Environment environment;
	private static EntityStore store;

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

	// NOTE: It is assumed that all the key are Strings and properly defined for
	// the entity class
	// whose object is going to be stored in this DB, thats why generic methods
	// for basic operations are used

	// Also Berkeley is thread safe, so no need for synchronization
	// http://www.oracle.com/technetwork/database/berkeleydb/db-faq-095848.html#Whatisthestoryaboutthreadsafety--canasqlite3dbhandlebesharedbetweenthreads
	public static synchronized <E> void put(Class<E> objClass, E obj)
			throws FileNotFoundException {

		checkStore();
		PrimaryIndex pIndex = store.getPrimaryIndex(String.class, objClass);
		pIndex.put(obj);
		environment.sync();
	}

	public static synchronized <E> E get(Class<E> objClass, String key)
			throws FileNotFoundException {

		checkStore();
		PrimaryIndex pIndex = store.getPrimaryIndex(String.class, objClass);
		return (E) pIndex.get(key);
	}

	public static synchronized <E> boolean contains(Class<E> objClass,
			String key) throws FileNotFoundException {

		checkStore();
		PrimaryIndex pIndex = store.getPrimaryIndex(String.class, objClass);
		return pIndex.contains(key);
	}

	public static synchronized <E> void remove(Class<E> objClass, String key)
			throws FileNotFoundException {

		checkStore();
		PrimaryIndex pIndex = store.getPrimaryIndex(String.class, objClass);
		pIndex.delete(key);
		environment.sync();
	}

	public static synchronized <E> HashSet<String> getAllKeys(Class<E> objClass)
			throws FileNotFoundException {

		checkStore();
		PrimaryIndex pIndex = store.getPrimaryIndex(String.class, objClass);

		EntityCursor<String> cursor = pIndex.keys();
		Iterator<String> i = cursor.iterator();
		HashSet<String> result = new HashSet<String>();
		while (i.hasNext()) {
			result.add(i.next());
		}
		cursor.close();
		return result;
	}

	public static synchronized void closeDB() {
		if (store != null)
			store.close();
		if (environment != null)
			environment.close();
	}

	// Checks whether store is open or not
	private static synchronized void checkStore() throws FileNotFoundException {
		if (store == null) {
			throw new FileNotFoundException("Store has not been setup");
		}
	}

	// public static void main(String[] args) throws FileNotFoundException{
	// System.out.println("Hello");
	// DBMethods.openDB();
	// User user = new User("sanidhya", "temp");
	// DBMethods.put(User.class, user);
	// User temp = DBMethods.get(User.class, "sanidhya");
	// System.out.println(temp.getUsername() + ": " + temp.getPassword());
	//
	// }

}
