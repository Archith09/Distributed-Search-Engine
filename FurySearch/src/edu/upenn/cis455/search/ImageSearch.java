package edu.upenn.cis455.search;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.index.NLP;
import edu.upenn.cis455.storage.ImageDataAccessor;
import edu.upenn.cis455.utilities.ImageEntry;

/*
 * This class implements the html search algorithm. The UI calls methods from this class to receive
 * the final set of result. The results produced from this class are also kept in cache for 
 * a specific amount of time 
 * 
 * 
 */

public class ImageSearch {

	private static NLP nlp = new NLP();
	private String[] words;
	private int resultsPageNum;
	private int noOfResults;
	private String query = "";
	public PrintStream out1;
	
	public LinkedHashMap<String, ImageEntry> firstPageResults = new LinkedHashMap<String, ImageEntry>();
	private ArrayList<HashMap<String, ImageEntry>> results = new ArrayList<HashMap<String, ImageEntry>>();
	private LinkedHashMap<String, Double> urlsScoreList = new LinkedHashMap<String, Double>();
	public LinkedHashMap<String, LinkedHashMap<String, ImageEntry>> finalResults = new LinkedHashMap<String, LinkedHashMap<String, ImageEntry>>();
	public static ConcurrentHashMap<String, HashMap<String, ImageEntry>> queryMap = new ConcurrentHashMap<String, HashMap<String, ImageEntry>>();
	private static CachedObject cachedResults;
	private HashMap<String, String> log;
	
	public ImageSearch() throws FileNotFoundException {
		out1 = new PrintStream(new FileOutputStream("output1.txt"));
		ImageDataAccessor.openDB(Constants.imageDB);
	}

	@SuppressWarnings("static-access")
	public LinkedHashMap<String, UrlInformation> queryFromUI(String queryParams, String location, int pageNum, int resultCount) {
		
		if(queryParams.isEmpty()){
			return null;
		}
		
		long begin = System.currentTimeMillis();
		// Remove stop words from query string
		query = queryParams;
		words = nlp.removeStopWords(query).split(" ");
		
		// TODO this needs to be provided from the UI
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
			System.out.println(word);
			word = nlp.process(word);
			word = nlp.stem(word);
			HashMap<String, ImageEntry> map = ImageDataAccessor.getResults(word, 1000).getResult();
			if(map != null){
				queryMap.put(word, map);
			}
			
		}
		long threadPoolEndTime = System.currentTimeMillis() - threadPoolTimeIndexer;
		out1.println("Total indexer time " + (threadPoolEndTime) +"  " +  words.toString() + "\n");
		
		// Take union of all urls
		HashSet<String> urls = new HashSet<String>();
		for(Entry<String, HashMap<String, ImageEntry>> entry : queryMap.entrySet()){
			urls.addAll(entry.getValue().keySet());
		}
		
		HashMap<String, Double> totalScore = calculateTotalScore(urls, queryWeights, location);
		
		LinkedHashMap<String, Double> sortedResults = sortMap(totalScore);
		
		LinkedHashMap<String, UrlInformation> response = new LinkedHashMap<String, UrlInformation>();
		
		long processingTime = System.currentTimeMillis() - begin;
		int i = 1;
		System.out.println("The final sorted urls are -> ");
		for(Entry<String, Double> entry : sortedResults.entrySet()){
			String url = entry.getKey();
			String title = "";
			String excerpt = "";
			for(Entry<String, HashMap<String, ImageEntry>> wordMap : queryMap.entrySet()){
				HashMap<String, ImageEntry> word = wordMap.getValue();
				if(word.containsKey(url)){
					title = word.get(url).getWord();
					excerpt = word.get(url).getUrl();
					response.put(url, new UrlInformation(url, title, excerpt, log.get(url), threadPoolEndTime, processingTime));
					break;
				}
			}
			
//			System.out.println(i);
//			System.out.println(entry.getKey() + " with total score of " + entry.getValue());
//			System.out.println("Score Distribution:");
//			System.out.println(log.get(entry.getKey()));
			i++;
		}
		
		return response;
	}
	
	private HashMap<String, Double> calculateTotalScore(HashSet<String> urls, HashMap<String, Double> queryWeights, String location) {
		
		HashMap<String, Double> result = new HashMap<String, Double>();
		log = new HashMap<String, String>();
		
		for(String url : urls){
			
			double totalScore = 0;
			StringBuffer buffer = new StringBuffer();
			
			for(Entry<String, Double> qw : queryWeights.entrySet()){
				
				// Get the appropriate query map first (queryMap)
				HashMap<String, ImageEntry> wordMap = queryMap.get(qw.getKey());
				buffer.append(qw.getKey() + ":");
				
				if(wordMap != null && wordMap.containsKey(url)){
					double score = getScore(wordMap.get(url), buffer, location);
					totalScore += qw.getValue() * score;
					buffer.append(" X qw " + qw.getValue() + "\n");
				} else {
					buffer.append("[ 0 ] X " + qw.getValue() + "\n");
				}
			}
			buffer.append("Total Score = " + totalScore);
			log.put(url, buffer.toString());
			result.put(url, totalScore);
		}
		
		return result;
	}

	private double getScore(ImageEntry entry, StringBuffer buffer, String location) {
		
		double locationBoost = 0.0;
		if(location != null && !location.isEmpty()){
			if(queryMap.containsKey(location)){
				if(queryMap.get(location).containsKey(entry.getUrl())){
					locationBoost = 1.0; // Change it
				}
			}
		}
		
		Double score = (entry.getTf() * entry.getIdf()) + locationBoost;
		buffer.append("[(" + entry.getTf() + " * " + entry.getIdf() + ") + " + locationBoost + "]->" + score);
		return score;
	}

	@SuppressWarnings("static-access")
	private HashMap<String, Double> getQueryWeight(String[] words) {
		
		HashMap<String, Double> result = new HashMap<String, Double>();
		double max = 1;
		for(String word : words){
			word = nlp.process(word);
			word = nlp.stem(word);
			if(result.containsKey(word)){
				double val = result.get(word);
				if(val > max){
					max = val + 1.0;
				}
				result.put(word, val + 1);
			} else {
				result.put(word, 1.0);
			}
		}
		
		for(Entry<String, Double> entry : result.entrySet()){
			result.put(entry.getKey(), entry.getValue() / max);
		}
		
		return result;
	}
	
	// Method to sort list of urls in decreasing order of score
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

	// Method to compute final results for a given query
	public void setFinalResults() {

		LinkedHashMap<String, ImageEntry> tempMap = new LinkedHashMap<String, ImageEntry>();

		// get urls sorted by score
		for (String key : urlsScoreList.keySet()) {
			for (int i = 0; i < results.size(); i++) {
				if (results.get(i).containsKey(key)) {
					tempMap.put(key, results.get(i).get(key));
					tempMap.get(key).setTotalScore(urlsScoreList.get(key));
				}
			}
		}

		finalResults.clear();
		finalResults.put(query, tempMap);
		
		// put computed results into cache
		updateCache();

//		 System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ Complete results ########################");
//		 for (String key : tempMap.keySet())
//			 System.out.println(key + " -------> " + tempMap.get(key));
//		 System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ End of complete results ########################");

		// build result set for first page
		int counter = noOfResults;
		// return results only for first display page
		firstPageResults.clear();
		for (String key : tempMap.keySet()) {
			firstPageResults.put(key, tempMap.get(key));
			counter--;
			if (counter == 0)
				break;
		}
	}

	public void updateCache() {
		// set the object time to live to 10 minutes, also a unique identifier
		// to it.
		ObjectMapper om = new ObjectMapper();
		Object tempObject = om.convertValue(finalResults, Object.class);
		// create a new cache object with query string as identifier and set
		// time to live
		CachedObject co = new CachedObject(tempObject, query, 20);
		// push search results into cache
		CacheManager.putCache(co);
	}

	// this method returns results from previously queried results for further
	// pages i.e., when a user clicks on page numbers 2, 3, 4 etc
	@SuppressWarnings({ "unchecked", "unused" })
	public LinkedHashMap<String, ImageEntry> getFinalResults(String query, int pageNum, int resultCount) {
		LinkedHashMap<String, ImageEntry> pageResults = new LinkedHashMap<String, ImageEntry>();
		LinkedHashMap<String, ImageEntry> tempMap = new LinkedHashMap<String, ImageEntry>();
		ObjectMapper objectMapper = new ObjectMapper();

		resultsPageNum = pageNum;

//		System.out.println("\n-------------------------------YOU ARE IN getFinalResults method-------------------------------");

		if (resultsPageNum == 1) {

//			 System.out.println("\n-------------------------------First Page Results-------------------------------");
//			 for (String key : firstPageResults.keySet())
//				 System.out.println(key + " -------> " + firstPageResults.get(key));
//			 System.out.println("-------------------------------End of First Page Results-------------------------------");

			return firstPageResults;
		} else {
			cachedResults = (CachedObject) CacheManager.getCache(query);

			if (cachedResults == null) {
				System.out.println("Requested object could not be found in the cache");
			} else {
				// user search query exists in cache
				System.out.println("SUCCESS! Found requested object in cache");
				finalResults.clear();
				finalResults = objectMapper.convertValue(cachedResults.object, LinkedHashMap.class);
			}

			// retrieve results from cache
			if (finalResults.containsKey(query)) {
				for (String key : finalResults.get(query).keySet()) {
					ImageEntry tempCache = objectMapper.convertValue(finalResults.get(query).get(key),
							ImageEntry.class);
					tempMap.put(key, tempCache);
				}
			}

			int counter = 0;
			// return results according to page number clicked by user
			for (String key : tempMap.keySet()) {
				if (counter < (pageNum * resultCount) - resultCount) {
					counter++;
					continue;
				} else if (counter > pageNum * resultCount)
					break;
				else {
					pageResults.put(key, tempMap.get(key));
					counter++;
				}
			}

//			 System.out.println("-------------------------------Results from cache-------------------------------\n");
//			 for (String key : pageResults.keySet())
//			 System.out.println(key + " -------> " + pageResults.get(key));
//			 System.out.println("-------------------------------End of results from cache-------------------------------\n");
//			 System.out.println("-------------------------------END of getFinalResults method-------------------------------\n");

			return pageResults;
		}
	}

	public HashMap<String, String> getLog() {
		return log;
	}

	public void setLog(HashMap<String, String> log) {
		this.log = log;
	}
	
	
	
}