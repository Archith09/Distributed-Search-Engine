package edu.upenn.cis455.utilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * This class provides abstract method to send request to the spell check server
 * and returns the corrected string 
 * 
 * 
 */

public class SpellCheck {
	
	public static void stop(){
		spellCheck("shutdown spell check");
	}
	
	public static String spellCheck(String query){
		  
		  String result = "";
		  try {
			Socket check = new Socket("localhost", 9001);
			PrintWriter out = new PrintWriter(check.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(check.getInputStream()));
			out.write(query);
			out.flush();
			
			// Read the corrected String from spell server
			StringBuffer body = new StringBuffer();
			int readChar;
			while((readChar = in.read()) != -1){
				char letter = (char) readChar;
		    	body.append(letter);
			}
			
			System.out.println("Corrected String: " + body.toString());
			check.close();
			result = body.toString();
		  } catch (IOException e1) {
			  // TODO Auto-generated catch block
			  e1.printStackTrace();
		  }
		  
		  return result;
	  }
	
	
}
