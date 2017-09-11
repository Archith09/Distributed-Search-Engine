package edu.upenn.cis455.search;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.index.NLP;
import edu.upenn.cis455.utilities.IndexEntry;

/*
 * This class implements the html search algorithm. The UI calls methods from this class to receive
 * the final set of result. The results produced from this class are also kept in cache for 
 * a specific amount of time 
 * 
 * 
 * 
 */

public class Search {

	private static NLP nlp = new NLP();
	private String[] words;
	private String query = "";
	public PrintStream out1;

	public static BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	public static ExecutorService threadPool = Executors.newFixedThreadPool(5);
	public static ConcurrentHashMap<String, HashMap<String, IndexEntry>> queryMap = new ConcurrentHashMap<String, HashMap<String, IndexEntry>>();
	private HashMap<String, String> log;
	private ObjectMapper objectMapper = new ObjectMapper();

	public Search() throws FileNotFoundException {
		out1 = new PrintStream(new FileOutputStream("output1.txt"));
		for (int i = 0; i < 5; i++) {
			Runnable worker = new SearchWorker("Worker" + i);
			threadPool.execute(worker);
		}
	}

	@SuppressWarnings("static-access")
	public LinkedHashMap<String, UrlInformation> queryFromUI(String queryParams, String location, int pageNum, int resultCount) {
		
		long requestTimeFromIndexer = System.currentTimeMillis();
		CachedObject cachedResults = (CachedObject) CacheManager.getCache(queryParams);
		LinkedHashMap<String, UrlInformation> response = new LinkedHashMap<String, UrlInformation>();
		LinkedHashMap<String, UrlInformation> pageResults = new LinkedHashMap<String, UrlInformation>();

		if(cachedResults == null){
			
			if(queryParams == null || queryParams.isEmpty()){
				return null;
			}
			
			long begin = System.currentTimeMillis();
			// Remove stop words from query string
			query = queryParams;
			words = nlp.removeStopWords(query).split(" ");
			
			// Also put the location in the query parameter if you are giving the query here
			location = nlp.process(location);
			location = nlp.stem(location);
			
			// Calculate query weights
			HashMap<String, Double> queryWeights = getQueryWeight(words);
			
			// Remove duplicate query words
			HashSet<String> duplicate = new HashSet<String>(Arrays.asList(words));
			
			queryMap.clear();
			
			long threadPoolTimeIndexer = System.currentTimeMillis();
			for(String word : duplicate){
				try {
//					System.out.println(word);
					word = nlp.process(word);
					word = nlp.stem(word);
					queue.put(word);				
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			while(queryMap.size() != duplicate.size()){
	//			System.out.println("Waiting for every index retrieval one to finish");
			}
			
			long threadPoolEndTime = System.currentTimeMillis() - threadPoolTimeIndexer;
			out1.println("Total indexer time " + (threadPoolEndTime) +"  " +  words.toString() + "\n");
			
			// Take union of all urls
			HashSet<String> urls = new HashSet<String>();
			for(Entry<String, HashMap<String, IndexEntry>> entry : queryMap.entrySet()){
				urls.addAll(entry.getValue().keySet());
			}
			
			HashMap<String, Double> totalScore = calculateTotalScore(urls, queryWeights, location);
			
			LinkedHashMap<String, Double> sortedResults = sortMap(totalScore);
			
			long processingTime = System.currentTimeMillis() - begin;
			int i = 1;
//			System.out.println("The final sorted urls are -> ");
			for(Entry<String, Double> entry : sortedResults.entrySet()){
				String url = entry.getKey();
				String title = "";
				String excerpt = "";
				for(Entry<String, HashMap<String, IndexEntry>> wordMap : queryMap.entrySet()){
					HashMap<String, IndexEntry> word = wordMap.getValue();
					if(word.containsKey(url)){
						title = word.get(url).getTitle();
						excerpt = word.get(url).getExcerpt();
						response.put(url, new UrlInformation(url, title, excerpt, log.get(url), threadPoolEndTime, processingTime));
						break;
					}
				}
				
//				System.out.println(i);
//				System.out.println(entry.getKey() + " with total score of " + entry.getValue());
//				System.out.println("Score Distribution:");
//				System.out.println(log.get(entry.getKey()));
				i++;
			}
			
			updateCache(queryParams, response);
			pageResults = computePageResults(response, pageNum, resultCount);
			
			System.out.println("Computed Results");
		}
		else{
			LinkedHashMap<String, UrlInformation> tempMap = new LinkedHashMap<String, UrlInformation>();
			tempMap = objectMapper.convertValue(cachedResults.object, LinkedHashMap.class);

//			System.out.println("Retrieved from cache");
			for(String key: tempMap.keySet()){
				UrlInformation tempCache = objectMapper.convertValue(tempMap.get(key),
						UrlInformation.class);
				response.put(key, tempCache);
			}

			pageResults = computePageResults(response, pageNum, resultCount);
		}
//
//		for(String key : pageResults.keySet()){
//			System.out.println(key + " ------------> " + pageResults.get(key));
//		}
		
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		System.out.println("Results from search returned in " + (System.currentTimeMillis() - requestTimeFromIndexer) + " for query: " + queryParams);
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		
		return pageResults;
	}
	

	private HashMap<String, Double> calculateTotalScore(HashSet<String> urls, HashMap<String, Double> queryWeights,
			String location) {

		HashMap<String, Double> result = new HashMap<String, Double>();
		log = new HashMap<String, String>();

		for (String url : urls) {

			double totalScore = 0;
			StringBuffer buffer = new StringBuffer();

			for (Entry<String, Double> qw : queryWeights.entrySet()) {

				// Get the appropriate query map first (queryMap)
				HashMap<String, IndexEntry> wordMap = queryMap.get(qw.getKey());
				buffer.append(qw.getKey() + ":");
				if (wordMap != null && wordMap.containsKey(url)) {
					double score = getScore(wordMap.get(url), buffer, location);
					totalScore += qw.getValue() * score;
					buffer.append(" X qw " + qw.getValue() + "\n");
				} else {
					buffer.append("[ 0 ] X " + qw.getValue() + "\n");
				}
			}
			
			double locationBoost = 0.0;
			if (location != null && !location.isEmpty()) {
				if (queryMap.containsKey(location)) {
					if (queryMap.get(location).containsKey(url)) {
						locationBoost = 1.0; // Change it as you wish
					}
				}
			}
			
			totalScore = totalScore + locationBoost;
			
			buffer.append("Total Score = " + totalScore + " with location boost of " + locationBoost);
			log.put(url, buffer.toString());
			result.put(url, totalScore);
		}

		return result;
	}

	private double getScore(IndexEntry entry, StringBuffer buffer, String location) {

		double inhostcount = 0;

		try {
			URL url = new URL(entry.getUrl());
			if (url.getHost().contains(entry.getWord())) {
				inhostcount = 1;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Double score = (entry.getTf() * entry.getIdf() * entry.getPageRank()) 
				+ 0.25 * entry.getUrlOccurenceCount() + 0.2 * entry.getTitleCount() + 0.01 * entry.getAnchorCount()
				+ 1.4 * inhostcount;
		buffer.append("[(" + entry.getTf() + " * " + entry.getIdf() + " * " + entry.getPageRank() + ") + "
				+ " + " + 0.25 * entry.getUrlOccurenceCount() + " + " + 0.2 * entry.getTitleCount()
				+ " + " + 0.1 * entry.getAnchorCount() + " + " + 1.4 * inhostcount +    " ]->" + score);
		
		return score ;
	}

	@SuppressWarnings("static-access")
	private HashMap<String, Double> getQueryWeight(String[] words) {

		HashMap<String, Double> result = new HashMap<String, Double>();
		double max = 1;
		for (String word : words) {
			word = nlp.process(word);
			word = nlp.stem(word);
			if (result.containsKey(word)) {
				double val = result.get(word);
				if (val > max) {
					max = val + 1.0;
				}
				result.put(word, val + 1);
			} else {
				result.put(word, 1.0);
			}
		}

		for (Entry<String, Double> entry : result.entrySet()) {
			result.put(entry.getKey(), entry.getValue() / max);
		}

		return result;
	}

	// method to sort list of urls in decreasing order of score
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LinkedHashMap<String, Double> sortMap(HashMap<String, Double> map) {
		Object[] a = map.entrySet().toArray();
		LinkedHashMap<String, Double> sortedList = new LinkedHashMap<String, Double>();
		Arrays.sort(a, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Map.Entry<String, Double>) o2).getValue()
						.compareTo(((Map.Entry<String, Double>) o1).getValue());
			}
		});
		for (Object e : a) {
			sortedList.put(((Map.Entry<String, Double>) e).getKey(), ((Map.Entry<String, Double>) e).getValue());
		}
		return sortedList;
	}

	public void updateCache(String query, LinkedHashMap<String, UrlInformation> response) {
		// set the object time to live to 10 minutes, also a unique identifier
		// to it.
		ObjectMapper om = new ObjectMapper();
		Object tempObject = om.convertValue(response, Object.class);
		// create a new cache object with query string as identifier and set
		// time to live
		CachedObject co = new CachedObject(tempObject, query, 40);
		// push search results into cache
		CacheManager.putCache(co);
	}

	// return results according to page number clicked by user
	public LinkedHashMap<String, UrlInformation> computePageResults(LinkedHashMap<String, UrlInformation> map, int pageNum, int resultCount){
		int counter = 1;
		LinkedHashMap<String, UrlInformation> tempMap = new LinkedHashMap<String, UrlInformation>();

		for (String key : map.keySet()) {
			if (counter < (pageNum * resultCount) - resultCount) {
				counter++;
				continue;
			} else if (counter > pageNum * resultCount)
				break;
			else {
				tempMap.put(key, map.get(key));
				counter++;
			}
		}

		return tempMap;
	}

	public HashMap<String, String> getLog() {
		return log;
	}

	public void setLog(HashMap<String, String> log) {
		this.log = log;
	}

}