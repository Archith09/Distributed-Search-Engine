package edu.upenn.cis455.pagerank;

import java.io.*;
import java.util.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/**
 * 
 * This class determines dangling links
 */

public class MapReduce1{
	public final static String DNGL = "DNGL";
	public final static String NDNGL = "NDNGL";
	public final static String ID = "null";
	// public final static String ATTHERATE = "@~@";
	public final static String DELIMITER = ";#;";
	public final static String SEMICOLON = ";;~;;";

	public static class Map_MapReduce1 extends Mapper<Object, Text, Text, Text>{

		public void map(Object k, Text v, Context c) throws IOException, InterruptedException{
			String url = k.toString();
			String originalUrl = url.trim();
			String rank = v.toString();
			String originalRank = rank.trim();

			if(k == null || url == null || originalUrl.equals("")){
				return;
			}

			if(rank == null || rank.equals("") || rank.contains(ID)){
				c.write(new Text(originalUrl), new Text(DNGL));
				return;
			}

			// String children[] = originalRank.split(ATTHERATE);
			String children[] = originalRank.split(DELIMITER);
			for(String i : children){
				i = i.trim();
				c.write(new Text(i), new Text(originalUrl));
			}
		}
	}

	public static class Reduce_MapReduce1 extends Reducer<Text, Text, Text, Text>{

		public void reduce(Text k, Iterable<Text> v, Context c) throws IOException, InterruptedException{
			String url = k.toString();
			String nextUrl = url.trim();
			ArrayList<String> originalUrls = new ArrayList<String>();
			StringBuffer nextUrlType;
			boolean isValid = false;

			if(k == null || url == null || nextUrl.equals("")){
				return;
			}

			for(Text i : v){
				String seperateUrl = i.toString();
//				if(i == null){
//					continue;
//				}
				if(seperateUrl == null || seperateUrl.equals("")){
					continue;
				}
				if(seperateUrl.equals(DNGL)){
					isValid = true;
				} else{
					originalUrls.add(seperateUrl);
				}
			}

			for(String i : originalUrls){
				nextUrlType = new StringBuffer(nextUrl);
				nextUrlType.append(SEMICOLON);
				if(isValid){
					nextUrlType.append(DNGL);
				} else{
					nextUrlType.append(NDNGL);
				}
				c.write(new Text(i), new Text(nextUrlType.toString()));
			}
		}
	}

	public static void main(String[] args) throws Exception{
		String jobName = "page rank";
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
		myJob.setJarByClass(MapReduce1.class);
		myJob.setMapperClass(Map_MapReduce1.class);
		myJob.setReducerClass(Reduce_MapReduce1.class);

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