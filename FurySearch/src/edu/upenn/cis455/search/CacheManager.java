package edu.upenn.cis455.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * 
 *
 */

public class CacheManager {
	
	@SuppressWarnings("rawtypes")
	private static HashMap cacheData = new HashMap();
	@SuppressWarnings("rawtypes")
	private static HashMap cacheImageData = new HashMap();
	static {
		try {
			// thread to keep purge expired objects in cache
			Thread purgeThread = new Thread(new Runnable() {
				int sleepTime = 5000;

				@SuppressWarnings("rawtypes")
				public void run() {
					try {
						// infinited loop to keep the cache service running uninterrupted
						while (true) {
							Set objectIDs = cacheData.keySet();
							Iterator ids = objectIDs.iterator();
							while (ids.hasNext()) {
								Object id = ids.next();
								Cacheable data = (Cacheable) cacheData.get(id);

								if (data.isExpired()) {
									cacheData.remove(id);
								}
							}
							
							Set imageDataIDs = cacheData.keySet();
							Iterator imageIDs = imageDataIDs.iterator();
							while (imageIDs.hasNext()) {
								Object id = imageIDs.next();
								Cacheable data = (Cacheable) cacheImageData.get(id);

								if (data.isExpired()) {
									cacheImageData.remove(id);
								}
							}
							Thread.sleep(this.sleepTime);
						}
					} catch (Exception e) {

					}
					return;
				}
			});
			purgeThread.setPriority(Thread.MIN_PRIORITY);
			purgeThread.start();
		} catch (Exception e) {

		}
	}

	public CacheManager() {
		
	}

	@SuppressWarnings("unchecked")
	public static void putCache(Cacheable data) {
		cacheData.put(data.getIdentifier(), data);
	}
	
	@SuppressWarnings("unchecked")
	public static void putImageCache(Cacheable image) {
		cacheImageData.put(image.getIdentifier(), image);
	}

	public static Cacheable getCache(Object object) {
		Cacheable data = (Cacheable) cacheData.get(object);
		if (data == null)
			return null;
		if (data.isExpired()) {
			cacheData.remove(object);
			return null;
		} else {
			return data;
		}
	}
	
	public static Cacheable getImageCache(Object object) {
		Cacheable data = (Cacheable) cacheImageData.get(object);
		if (data == null)
			return null;
		if (data.isExpired()) {
			cacheImageData.remove(object);
			return null;
		} else {
			return data;
		}
	}
}
