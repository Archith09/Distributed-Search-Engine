package edu.upenn.cis455.utilities;

import java.util.regex.Pattern;

/*
 * This class is not used. A duplicate class is present in the index package which should be used
 * 
 * 
 * 
 */
public class NLP {
	
	private static Pattern specialChar = Pattern.compile("[^a-zA-Z0-9 ]");
	
	public static String process(String input){
		// Remove Special Characters
		input = specialChar.matcher(input).replaceAll("");

		// Stems the input String
		Stemmer stemmer = new Stemmer();
		input = stemmer.stemWord(input);
		

		return input.toLowerCase().trim();
	}
	
	
}
