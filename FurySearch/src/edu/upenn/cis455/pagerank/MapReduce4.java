package edu.upenn.cis455.pagerank;

import java.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/**
 * 
 * This class calculates the final page rank for each url
 */

public class MapReduce4{

	public final static String SEMICOLON = ";;~;;";


	public static class Map_MapReduce4 extends Mapper<Object, Text, Text, Text>{
		public void map(Object k, Text v, Context c) throws IOException, InterruptedException{
			String url = k.toString();
			String originalUrl = url.trim();

			if(k == null || url == null || originalUrl.equals("")){
				return;
			}
			c.write(new Text(originalUrl), new Text("1"));
		}
	}



	public static class Reduce_MapReduce4 extends Reducer<Text, Text, Text, Text>{
		public void reduce(Text k, Iterable<Text> v, Context c) throws IOException, InterruptedException{
			String url = k.toString();
			String originalUrl = url.trim();
			String sepUrl[] = url.split(SEMICOLON);
			String extractedUrl = "";
			String extractedRank = "";
			// String originalRank = rank.trim();
			// StringBuffer nextUrls = new StringBuffer("");

			if(k == null || url == null || originalUrl.equals("")){
				return;
			}

			if(sepUrl.length != 2){
				return;
			}

			// extractedUrl = sepUrl[0];
			// extractedRank = sepUrl[1];
			
			// if(sepUrl[1].length() > 10){
			// 	extractedRank = extractedRank.substring(0, 10);
			// }
			// c.write(new Text(extractedUrl), new Text(extractedRank));

			extractedUrl = sepUrl[0];
			extractedRank = "PAGERANK@" + sepUrl[1];
			
			if(sepUrl[1].length() > 20){
				extractedRank = extractedRank.substring(0, 19);
			}
			c.write(new Text(extractedUrl), new Text(extractedRank));
		}
	}

	public static void main(String[] args) throws Exception{
		String jobName = "page rank4";
		String hadoopLoc = "hadoop.tmp.dir";
		String javaLoc = "java.io.tmpdir";
		String mapreduceSeparator = "mapreduce.input.keyvaluelinerecordreader.key.value.separator";
		int jobStatus = 0;
		
		// set up hadoop mapreduce configuration
		Configuration myConfiguration = new Configuration();
		Job myJob = Job.getInstance(myConfiguration, jobName);
		myConfiguration.set(hadoopLoc, System.getProperty(javaLoc));
		myConfiguration.set(mapreduceSeparator, "\t");
		
		// set mapper and reducer classes
		myJob.setJarByClass(MapReduce4.class);
		myJob.setMapperClass(Map_MapReduce4.class);
		myJob.setReducerClass(Reduce_MapReduce4.class);

		// set map reduce key and value output classes to default text class
		myJob.setOutputKeyClass(Text.class);
		myJob.setOutputValueClass(Text.class);
		myJob.setInputFormatClass(KeyValueTextInputFormat.class);

		FileInputFormat.addInputPath(myJob, new Path(args[0]));
		FileOutputFormat.setOutputPath(myJob, new Path(args[1]));

		if(myJob.waitForCompletion(true))
			jobStatus = 0;
		else
			jobStatus = 1;

		System.exit(jobStatus);
	}
}