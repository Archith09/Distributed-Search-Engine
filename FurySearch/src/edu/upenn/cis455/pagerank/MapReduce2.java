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
* This class determines dangling links
*/

public class MapReduce2{

	// public final static String ATTHERATE = "@~@";
	public final static String DELIMITER = ";#;";
	public final static String SEMICOLON = ";;~;;";

	public static class Map_MapReduce2 extends Mapper<Object, Text, Text, Text>{
		public void map(Object k, Text v, Context c) throws IOException, InterruptedException{
			String url = k.toString();
			String originalUrl = url.trim();
			String rank = v.toString();
			String originalRank = rank.trim();

			if(k == null || url == null || originalUrl.equals("")){
				return;
			}
			c.write(new Text(originalUrl + SEMICOLON + "1"), new Text(originalRank));
		}
	}
	
	public static class Reduce_MapReduce2 extends Reducer<Text, Text, Text, Text>{
		public void reduce(Text k, Iterable<Text> v, Context c) throws IOException, InterruptedException{
			String url = k.toString();
			String originalUrl = url.trim();
			StringBuffer nextUrls = new StringBuffer("");

			if(k == null || url == null || originalUrl.equals("")){
				return;
			}

			for(Text i : v){
				
				nextUrls.append(i.toString());
				if(v.iterator().hasNext()){
//					nextUrls.append(ATTHERATE);
					nextUrls.append(DELIMITER);
					// nextUrls.append(", ");
				} 
			}
			c.write(k, new Text(nextUrls.toString()));
		}
	}

	public static void main(String[] args) throws Exception{
		String jobName = "page rank2";
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
		myJob.setJarByClass(MapReduce2.class);
		myJob.setMapperClass(Map_MapReduce2.class);
		myJob.setReducerClass(Reduce_MapReduce2.class);

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
