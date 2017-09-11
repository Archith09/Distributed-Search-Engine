package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

/**
 * A wrapper class to BerkeleyDB creating put, get, open, close and other
 * methods
 */

public class DBWrapper {

	public static String envDirectory = null;
	public static Environment myEnv;
	public static EntityStore store;

	public DBWrapper(String s) {
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

	public static Environment getMyEnv() {
		return myEnv;
	}

	public static EntityStore getStore() {
		return store;
	}

	// closing the store and the database
	public static void close() {

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
}
