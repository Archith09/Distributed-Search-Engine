package edu.upenn.cis455.search;

import java.io.*;

public class Weather {

	public static void main(String args[]) {

		String s = null;

		try {
			Process p = Runtime.getRuntime().exec("ruby /Users/archith/Desktop/Archith/UPenn/CIS555/HWandPROJECT/Project/G16/src/edu/upenn/cis455/search/weather.rb philadelphia PA");
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			// read the output from the command
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}
		} catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
