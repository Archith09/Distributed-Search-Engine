package edu.upenn.cis455.index;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

/*
 * This class performs NLP tasks such as stemming, removing stop words, removing special chars
 * and extracting the small excerpt around the word from a text (Extra Credit implementation) 
 * 
 * 
 */
public class NLP {
	
	// Regex for special chars
	private static Pattern P1 = Pattern.compile("n't");
	private static Pattern P2 = Pattern.compile("'m");
	private static Pattern P3 = Pattern.compile("'ll");
	private static Pattern P4 = Pattern.compile("'ve");
	private static Pattern P5 = Pattern.compile("'s");
	private static Pattern P6 = Pattern.compile("'d");
	// private static Pattern P7 = Pattern.compile("\\.\\s+");
	// private static Pattern P8 = Pattern.compile("\\.\\.+");
	private static Pattern specialChar = Pattern.compile("[^a-zA-Z0-9 ]");
	
	// Stop words
	private static HashSet<String> stopwords;
	private static Stemmer stemmer;
	
	// Location Extraction
	private static InputStream is;
	private static TokenNameFinderModel tnf;
	private static NameFinderME nf;
	
	
	public NLP(){
		init();
	}
	
	public static String removeSpace(String input){
		return input.replaceAll(" +", " ").trim();
	}
	
	public static String process(String input){
		
		// Convert to lowercase
		input = input.toLowerCase();
		
		// Remove Special Char
		input = removeSpecialChar(input);
				
		// Stop Words and special char
		input = removeStopWords(input);
		
		return input.trim();
	}
	
	private static String removeSpecialChar(String input){
		
		input = P1.matcher(input).replaceAll("nt");
		input = P2.matcher(input).replaceAll("");
		input = P3.matcher(input).replaceAll(" will");
		input = P4.matcher(input).replaceAll(" have");
		input = P4.matcher(input).replaceAll(" have");
		input = P5.matcher(input).replaceAll("");
		input = P6.matcher(input).replaceAll("");
		// input = P7.matcher(input).replaceAll(" ");
		// input = P8.matcher(input).replaceAll(" ");
		input = specialChar.matcher(input).replaceAll(" ");
		
		// TODO Removed trim from here
		return input.trim();
	}
	
	public static String removeStopWords(String input) {
		initStopWods();
		StringBuffer result = new StringBuffer();
		String[] temp = input.split(" +"); 
		for(String word : temp){
			if(!stopwords.contains(word)){
				result.append(word);
				result.append(" ");
			}
		}
		
		return result.toString().trim();
	}

	public static String stem(String input){
		initStemmer();
		return stemmer.stemWord(input).trim();
	}
	
	/*
	 * This method returns a small excerpt around a word from a given text
	 */
	
	public static String excerpt(String find, String text){
		int length = 15;
		
		String[] words = text.split(" +");
	    int i = 0;
	    for(int j = 0; j < words.length; j++){
	    	if(words[j].toLowerCase().contains(find)){
	    		i = j;
	    		break;
	    	}
	    }
	    
	    String result = "";
	    StringBuffer temp = new StringBuffer();
	    
    	if(i < (length / 2)){
    		for(int j = 0; (j < length + 1) && (j < words.length); j++){
    			temp.append(words[j]);
    			temp.append(" ");
    		}
    		result = temp.toString();
    	} else if (i > words.length - (length / 2) - 1){
    		
    		for(int j =  (words.length - length) > 0 ? (words.length - length) : 0; j < words.length; j++){
    			temp.append(words[j]);
    			temp.append(" ");
    		}
    		result = temp.toString();
    	} else {
    		for(int j = i - (length / 2); j <  i + (length / 2) + 1; j++){
    			temp.append(words[j]);
    			temp.append(" ");
    		}
    		result = temp.toString();
    	}
	    return result;
	}
	
	public static String excerpt(int i, String[] text){
		int length = 20;
		
		String result = "";
	    StringBuffer temp = new StringBuffer();
	    
    	if(i < (length / 2)){
    		for(int j = 0; (j < length + 1) && (j < text.length); j++){
    			temp.append(text[j]);
    			temp.append(" ");
    		}
    		result = temp.toString();
    	} else if (i > text.length - (length / 2) - 1){
    		
    		for(int j =  (text.length - length) > 0 ? (text.length - length) : 0; j < text.length; j++){
    			temp.append(text[j]);
    			temp.append(" ");
    		}
    		result = temp.toString();
    	} else {
    		for(int j = i - (length / 2); j <  i + (length / 2) + 1; j++){
    			temp.append(text[j]);
    			temp.append(" ");
    		}
    		result = temp.toString();
    	}
	    return result;
	}
	
	private static void initStopWods() {
		String[] words = new String[] { "a", "able", "about", "across",
				"after", "all", "almost", "also", "am", "among", "an", "and",
				"any", "are", "as", "at", "be", "because", "been", "but", "by",
				"can", "cannot", "could", "dear", "did", "do", "does",
				"either", "else", "ever", "every", "for", "from", "get", "got",
				"had", "has", "have", "he", "her", "hers", "him", "his", "how",
				"however", "i", "if", "in", "into", "is", "it", "its", "just",
				"least", "let", "like", "likely", "may", "me", "might", "most",
				"must", "my", "neither", "no", "nor", "not", "of", "off",
				"often", "on", "only", "or", "other", "our", "own", "rather",
				"said", "say", "says", "she", "should", "since", "so", "some",
				"than", "that", "the", "their", "them", "then", "there",
				"these", "they", "this", "tis", "to", "too", "twas", "us",
				"wants", "was", "we", "were", "what", "when", "where", "which",
				"while", "who", "whom", "why", "will", "with", "would", "yet",
				"you", "your", "a", "b", "c", "d", "e", "f", "g", "h", "i",
				"j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
				"v", "w", "x", "y", "z", "www", "0", "1", "2", "3", "4", "5",
				"6", "7", "8", "9", "a", "about", "above", "after", "again",
				"against", "all", "am", "an", "and", "any", "are", "aren't",
				"as", "at", "be", "because", "been", "before", "being",
				"below", "between", "both", "but", "by", "can't", "cannot",
				"could", "couldn't", "did", "didn't", "do", "does", "doesn't",
				"doing", "don't", "down", "during", "each", "few", "for",
				"from", "further", "had", "hadn't", "has", "hasn't", "have",
				"haven't", "having", "he", "he'd", "he'll", "he's", "her",
				"here", "here's", "hers", "herself", "him", "himself", "his",
				"how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in",
				"into", "is", "isn't", "it", "it's", "its", "itself", "let's",
				"me", "more", "most", "mustn't", "my", "myself", "no", "nor",
				"not", "of", "off", "on", "once", "only", "or", "other",
				"ought", "our", "ours	ourselves", "out", "over", "own", "same",
				"shan't", "she", "she'd", "she'll", "she's", "should",
				"shouldn't", "so", "some", "such", "than", "that", "that's",
				"the", "their", "theirs", "them", "themselves", "then",
				"there", "there's", "these", "they", "they'd", "they'll",
				"they're", "they've", "this", "those", "through", "to", "too",
				"under", "until", "up", "very", "was", "wasn't", "we", "we'd",
				"we'll", "we're", "we've", "were", "weren't", "what", "what's",
				"when", "when's", "where", "where's", "which", "while", "who",
				"who's", "whom", "why", "why's", "with", "won't", "would",
				"wouldn't", "you", "you'd", "you'll", "you're", "you've",
				"your", "yours", "yourself", "yourselves" };

		stopwords = new HashSet<String>(Arrays.asList(words));
	}
	
	public static void initStemmer(){
		stemmer = new Stemmer();
	}
	
	public void initLocation(){
		try {
			Class c = Class.forName(NLP.class.getCanonicalName());
			is = c.getResourceAsStream("en-ner-location.bin");
			tnf = new TokenNameFinderModel(is);
		    nf = new NameFinderME(tnf);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String findLocation(String content){
		content = removeSpecialChar(content);
		content = removeStopWords(content);
		String[] tokens = content.split("\\s+");
		Span sp[] = nf.find(tokens);
		
		String locations[] = Span.spansToStrings(sp, tokens);
		StringBuffer buffer = new StringBuffer("LOCATION@");
		
		for(String location : new HashSet<String>(Arrays.asList(locations))){
			buffer.append(location.toLowerCase() + ";~;");
		}
		
		return buffer.toString();
	}
	
	public void init(){
		initStopWods();
		initLocation();
		initStemmer();
	}
	
//	public static void main(String[] args) throws IOException {
//	    String test = "Boston Dynamics: Dedicated to the Science and Art of How Things Move.     Home Robots LS3 Atlas Petman Cheetah BigDog SandFlea RHex RiSE LittleDog Jobs Contact About Us RiSE: The Amazing Climbing Robot RiSE is a robot that climbs vertical terrain such as walls, trees and fences. RiSE uses feet with micro-claws to climb on textured surfaces. RiSE changes posture to conform to the curvature of the climbing surface and its tail helps RiSE balance on steep ascents. RiSE is 0.25 m long, weighs 2 kg, and travels 0.3 m/s. Each of RiSE's six legs is powered by a pair of electric motors. An onboard computer controls leg motion, manages communications, and services a variety of sensors, including joint position sensors, leg strain sensors and foot contact sensors. Boston Dynamics developed RiSE in conjunction with researchers at University of Pennsylvania, Carnegie Mellon, UC Berkeley, Stanford, and Lewis and Clark University. RiSE was funded by DARPA. To download a video of RiSE in action, click here. ©2016 Boston Dynamics.  text/html; charset=iso-8859-1 description Boston Dynamics is the leading provider of human simulation software, tools, and solutions. Organizations worldwide use its products and services for simulation-based training, mission-planning, analysis, and virtual prototyping applications. keywords Human simulation, real-time simulation, real time, realtime, 3D scenario generation, interactive 3D, virtual prototyping, virtual reality, human animation, control systems, motion, dismounted infantry, computer-generated forces, mission planning, characters, virtual humans, training applications, military technology, biomechanical analysis, Objective Force Warrior, human factors, PC, software, API, GUI, physics-based, dynamic, robot, lifelike, runtime, COTS  Boston Dynamics: Dedicated to the Science and Art of How Things Move.";
//	    test = NLP.removeSpace(test);
//		test = NLP.process(test);
//	    String[] tokens = test.split("\\s+");
//	    // System.out.println(excerpt("lion", "lion kills elephant in sparta lions"));
//	    
//	    NLP nlp = new NLP();
//	    System.out.println(excerpt(12, tokens));
//	    
//	}

	
	
}
