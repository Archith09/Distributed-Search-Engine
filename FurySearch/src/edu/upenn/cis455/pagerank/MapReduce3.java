package edu.upenn.cis455.pagerank;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.conf.Configuration;

/**
 * 
 * This class determines page rank for each url iteratively
 */
public class MapReduce3 {

	public final static String DNGL = "DNGL";
	public final static String NDNGL = "NDNGL";
	public final static String SEMICOLON = ";;~;;";
//	public final static String ATTHERATE = "@~@";
	public final static String DELIMITER = ";#;";
	public final static int DNGLLEN = DNGL.length();
	public final static int NDNGLLEN = NDNGL.length();
	public final static int SEMICOLONLEN = SEMICOLON.length();
	// public final static int ATTHERATELEN = ATTHERATE.length();
	public final static int DELIMITERLEN = DELIMITER.length();
	public final static int MINLEN = 0;
	public final static int MAXLEN = 9;

	public static class Map_MapReduce3 extends Mapper<Object, Text, Text, Text> {

	    public void map(Object k, Text v, Context c) 
	    throws IOException, InterruptedException {
	    	
	    	String originalUrl = k.toString().trim();
	    	String newUrls = v.toString().trim();
	    	ArrayList<String> correctUrls = new ArrayList<String>();
	    	ArrayList<String> incorrectUrls = new ArrayList<String>();
	    	double result = 0;
	    	
	    	if(k == null || k.toString() == null || originalUrl.equals(""))
				return;
	    	
	    	if(!originalUrl.contains(SEMICOLON))
	    		return;

	    	String sepOriginal[] = originalUrl.split(SEMICOLON);
	    	String original = sepOriginal[0];
	    	
	    	if(sepOriginal[1].length() > MAXLEN)
	    		sepOriginal[1] = sepOriginal[1].substring(MINLEN, MAXLEN);
	    	double rank = Double.parseDouble(sepOriginal[1]);
	    	
	    	if(newUrls == null || newUrls.equals(""))
	    		return;
	    	
//	    	String sepNewUrls[] = newUrls.split(ATTHERATE);
	    	String sepNewUrls[] = newUrls.split(DELIMITER);
	    	int countNewUrls = sepNewUrls.length;
	    	int countCorrectUrls = 0;
	    	
	    	for(String eachUrl : sepNewUrls) {
	    		if(eachUrl.contains(NDNGL)) {
	    			correctUrls.add(eachUrl.substring(MINLEN, eachUrl.length() - SEMICOLONLEN - NDNGLLEN));
	    			countCorrectUrls++;
	    		}
	    		else if(eachUrl.contains(DNGL)) {
	    			incorrectUrls.add(eachUrl.substring(MINLEN, eachUrl.length() - SEMICOLONLEN - DNGLLEN));
	    		}
	    	}
	    	
	    	double correctUrlsResult = rank/(countNewUrls);
	    	if(countCorrectUrls > 0) {
	    		result = rank/countCorrectUrls;
	    		for(String i : correctUrls) {
		    		c.write(new Text(i), new Text(String.valueOf(result)));
		    	}
	    	}
	
	    	for(String i : incorrectUrls) {
	    		c.write(new Text(i), new Text(String.valueOf(correctUrlsResult)));
	    	}
	    	
	    	if(newUrls.length() > 0)
	    		c.write(new Text(original), new Text(newUrls));
	    }
	}

	public static class Reduce_MapReduce3 extends Reducer<Text, Text, Text, Text> {

		public void reduce(Text k, Iterable<Text> v, Context c) throws IOException, InterruptedException {
			
			double rank = 0;
			String url = k.toString().trim();
			String sepUrlVal = null;
			StringBuffer newUrls = new StringBuffer();

			if(k == null || k.toString() == null || url.equals("")){
				return;
			}

			for (Text i : v) {				
				sepUrlVal = i.toString().trim();
				if(sepUrlVal == null || sepUrlVal.equals("")){
					continue;
				}
				if(validateUrl(sepUrlVal)) {
					rank = rank + Double.valueOf(sepUrlVal);
				}
				else {
					newUrls.append(sepUrlVal);
//					newUrls.append(ATTHERATE);
					newUrls.append(DELIMITER);
				}
			}
			
			String nextUrls = newUrls.toString();
			if(nextUrls.endsWith(DELIMITER)){
				// nextUrls = nextUrls.substring(MINLEN, nextUrls.length() - ATTHERATELEN);
				nextUrls = nextUrls.substring(MINLEN, nextUrls.length() - DELIMITERLEN);
			}
			
			rank = 1 - 0.85 + (0.85 * rank);
			c.write(new Text(url + SEMICOLON + rank), new Text(nextUrls));
		}
		
		public static boolean validateUrl (String url) {
			final String strPattern    = ("[\\x00-\\x20]*"+ "[+-]?(" + "NaN|" + "Infinity|" + "(((" + "(\\p{Digit}+)" + "(\\.)?(" + "(\\p{Digit}+)" + "?)(" + "[eE][+-]?(\\p{Digit}+)" + ")?)|" + "(\\.(" + "(\\p{Digit}+)" + ")(" + "[eE][+-]?(\\p{Digit}+)" + ")?)|" + "((" + "(0[xX]" + "(\\p{XDigit}+)" + "(\\.)?)|" + "(0[xX]" + "(\\p{XDigit}+)" + "?(\\.)" + "(\\p{XDigit}+)" + ")" + ")[pP][+-]?" + "(\\p{Digit}+)" + "))" + "[fFdD]?))" + "[\\x00-\\x20]*");
			
			if (Pattern.matches(strPattern, url)){
				return true;
			} else{
				return false;
			}
		}
	}
	
	public static void main(String[] args) throws Exception {

		int iteration = 0;
		int minVal = 2;
		int maxVal = 27;
		
		for(int count = minVal; count < maxVal; count++){

			String jobName = "page rank3";
			String hadoopLoc = "hadoop.tmp.dir";
			String javaLoc = "java.io.tmpdir";
			String mapreduceSeparator = "mapreduce.input.keyvaluelinerecordreader.k.v.separator";

			// set up hadoop mapreduce configuration
			Configuration myConfiguration = new Configuration();
			Job myJob = Job.getInstance(myConfiguration, jobName);
			myConfiguration.set(hadoopLoc, System.getProperty(javaLoc));
			myConfiguration.set(mapreduceSeparator, "\t");

			// set mapper and reducer classes
			myJob.setJarByClass(MapReduce3.class);
			myJob.setMapperClass(Map_MapReduce3.class);
			myJob.setReducerClass(Reduce_MapReduce3.class);

			// set map reduce k and v output classes to default text class
			myJob.setOutputKeyClass(Text.class);
			myJob.setOutputValueClass(Text.class);
			myJob.setInputFormatClass(KeyValueTextInputFormat.class);

			String awsS3input = "s3://test.rsolanki.distributedfury/temp/pagerank_intermediate/output3/output";
			String awsS3output = "s3://test.rsolanki.distributedfury/temp/pagerank_intermediate/output3/output";

			iteration = count + 1;

			FileInputFormat.addInputPath(myJob, new Path(awsS3input + count));
			FileOutputFormat.setOutputPath(myJob, new Path(awsS3output + iteration));

			myJob.waitForCompletion(true);
		}
	}
}
