package edu.upenn.cis.crawler.TikaLangDetect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

import org.apache.tika.language.LanguageIdentifier;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;


/**
 * Tika library class to detect language of the content being downloaded
 * by crawler
 */

public class TikaWrapper {
	static PrintStream out1;
	static PrintStream out2;
	static File f;
	
	public TikaWrapper(){	
				
	}

	
	
	public static boolean detectlangShuyo(String s) {
		Detector detector;
		try {
			if (s.length() > 0) {
				detector = DetectorFactory.create();
				detector.append(s);
				String lang = detector.detect();
				//System.out.println("HERE IN TIKAAAAAAAAAAAAA     " + lang);
				//out1.println(detector.getProbabilities().toString());
				if(lang.equals("en")){
				return true;
				}
			} else
				return false;
		} catch (LangDetectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public static boolean detectLangTika(String content) {
		LanguageIdentifier identifier = new LanguageIdentifier(content);
		String lang = identifier.getLanguage();
		// System.out.println(lang +
		// "..........................................");
		if (lang.equals("en"))
			return true;
		else
			return false;

	}
/*
	public static void main(String[] args) throws IOException,
			LangDetectException {
		
		BufferedReader br = new BufferedReader(new FileReader(f));

		String s = "";
		while ((s = br.readLine()) != null) {
			out1.println(s.indexOf("\t") + 1);
			String t = s.substring(s.indexOf("\t") + 1, s.length());
			out1.println(t);
			out1.println(s.substring(0, s.indexOf("\t")) + " :::: "
					+ detectlangShuyo(t));

			out2.println(t);
			out2.println(s.substring(0, s.indexOf("\t")) + " :::: "
					+ detectLangTika(t));

		}

	}
	*/
}
