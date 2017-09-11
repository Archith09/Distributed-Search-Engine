package edu.upenn.cis455.index;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/*
 * This job creates tf-idf for the image index
 * 
 * 
 */
public class ImageTfIdf {
	
	private static Double totalCrawl = 901229.0;
	
	/*
	 * Input to the Map function: 
	 * word;#;DocID		tf
	 * 
	 * Map Emits:
	 * word		DocID;#;tf
	 */
	public static class TokenizerMapper extends
			Mapper<Text, Text, Text, Text> {

		private final static String FIELD_DELIMITER = ";#;";
		private Text mapKey = new Text();
		private Text mapValue = new Text();

		public void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {

			if (key == null || key.toString() == null || key.toString().trim().isEmpty()) {
				System.out.println("Empty string found");
				return;
			}
			
			String[] temp = key.toString().trim().split(FIELD_DELIMITER);
			if(temp.length != 2){
				System.out.println(Arrays.toString(temp));
				return;
			}
			
			String word = temp[0];
			String docID = temp[1];
			String tf = value.toString();
			StringBuffer valueBuffer = new StringBuffer();
			valueBuffer.append(docID);
			valueBuffer.append(FIELD_DELIMITER);
			valueBuffer.append(tf);
			mapKey.set(word);
			mapValue.set(valueBuffer.toString());
			context.write(mapKey, mapValue);
			
		}
	}

	/*
	 * Input to Reduce function:
	 * word		{docID1;#;tf1, docID2;#;tf2, ..., docIDn;#;tfn}
	 * 
	 * Reduce emits:
	 * word;#;DocID tf-idf;#;tf1;#;idf
	 */
	public static class IntSumReducer extends Reducer<Text, Text, Text, Text> {
		
		private Text reduceKey = new Text();
		private Text reduceValue = new Text();
		private final static String FIELD_DELIMITER = ";#;";
		
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			if (key == null || key.toString() == null || key.toString().trim().isEmpty()) {
				System.out.println("Empty string found");
				return;
			}
			
			//System.out.println("Reduce Key:" + key.toString());
			String word = key.toString();
			Double docPerWord = (double) 0;
			ArrayList<String> occurence = new ArrayList<String>();
			for (Text val : values) {
				docPerWord++;
				occurence.add(val.toString());
			}
			
			Double idf = Math.log10(totalCrawl / docPerWord);
			
			for (String val : occurence) {
				String[] temp = val.toString().trim().split(FIELD_DELIMITER);
				StringBuffer keyBuffer = new StringBuffer();
				keyBuffer.append(word);
				keyBuffer.append(FIELD_DELIMITER);
				keyBuffer.append(temp[0]); // DocID
				reduceKey.set(keyBuffer.toString());
				
				Double tf = Double.parseDouble(temp[1]);
				Double tfIdf = tf * idf;
				
				StringBuffer valueBuffer = new StringBuffer();
				valueBuffer.append(word);
				valueBuffer.append(FIELD_DELIMITER);
				valueBuffer.append(Double.toString(tfIdf));
				valueBuffer.append(FIELD_DELIMITER);
				valueBuffer.append(Double.toString(tf));
				valueBuffer.append(FIELD_DELIMITER);
				valueBuffer.append(Double.toString(idf));
				
				reduceValue.set(valueBuffer.toString());
				context.write(reduceKey, reduceValue);
			}
			
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", "\t");
		Job job = Job.getInstance(conf, "TF-IDF");
		job.setJarByClass(ImageTfIdf.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		// totalCrawl = Double.parseDouble(args[2].trim());
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
