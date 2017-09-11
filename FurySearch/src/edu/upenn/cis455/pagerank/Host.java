package edu.upenn.cis455.pagerank;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;


/**
 * 
 * This class determines the rank for all hostnames
 */

public class Host{
	
	public final static String DELIMITER = ";#;";
	
	public static class Map_MapReduce2 extends Mapper<Object, Text, Text, Text>{
		public void map(Object k, Text v, Context c) throws IOException, InterruptedException{
			String url = k.toString();
			String rank = v.toString().split("@")[1];
			if(k == null || url == null || url.trim().equals("")){
				return;
			}

			try{
				String hostName = null;
				URL myURL = new URL(url);
				hostName = myURL.getHost();
				c.write(new Text(hostName), new Text(url + DELIMITER + rank));
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public static class Reduce_MapReduce2 extends Reducer<Text, Text, Text, Text>{
		public void reduce(Text k, Iterable<Text> v, Context c) throws IOException, InterruptedException{
			double count = 0;
			String url = k.toString();
			ArrayList<String> urls = new ArrayList<String>();
			
			if(k == null || url == null || url.trim().equals("")){
				return;
			}
			for(Text i : v){
				urls.add(i.toString().split(DELIMITER)[0]);
				String pos = i.toString().split(DELIMITER)[1];
				double rank = Double.parseDouble(pos);
				count += rank;
			}
			
			for(String s : urls){
				c.write(new Text(s), new Text("HOSTRANK@" + String.valueOf(count)));
			}
		}
	}

	/**
	 * Will take a url such as http://www.stackoverflow.com and return www.stackoverflow.com
	 * 
	 * @param url
	 * @return
	 */
	// public static String getHost(String url){
	//     if(url == null || url.length() == 0)
	//         return "";

	//     int doubleslash = url.indexOf("//");
	//     if(doubleslash == -1)
	//         doubleslash = 0;
	//     else
	//         doubleslash += 2;

	//     int end = url.indexOf('/', doubleslash);
	//     end = end >= 0 ? end : url.length();

	//     int port = url.indexOf(':', doubleslash);
	//     end = (port > 0 && port < end) ? port : end;

	//     return url.substring(doubleslash, end);
	// }


	/**  Based on : http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3.3_r1/android/webkit/CookieManager.java#CookieManager.getBaseDomain%28java.lang.String%29
	 * Get the base domain for a given host or url. E.g. mail.google.com will return google.com
	 * @param host 
	 * @return 
	 */
	// public static String getBaseDomain(String url) {
	//     String host = getHost(url);

	//     int startIndex = 0;
	//     int nextIndex = host.indexOf('.');
	//     int lastIndex = host.lastIndexOf('.');
	//     while (nextIndex < lastIndex) {
	//         startIndex = nextIndex + 1;
	//         nextIndex = host.indexOf('.', startIndex);
	//     }
	//     if (startIndex > 0) {
	//         return host.substring(startIndex);
	//     } else {
	//         return host;
	//     }
	// }

	public static void main(String[] args) throws Exception{
		String jobName = "word count";
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
		myJob.setJarByClass(Host.class);
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