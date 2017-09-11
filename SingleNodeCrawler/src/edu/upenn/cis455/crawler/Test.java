package edu.upenn.cis455.crawler;

/**
 * Test class to run Seed URL files
 * 
 */
  

import java.io.File;

public class Test {

	public static void main(String[] args){
		File f = new File("resources/seedURLs.txt");
		System.out.println(f.exists());
	}
}
