package edu.upenn.cis455.index;
import java.io.IOException;

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
 * This class is not used, just for test purpose
 * 
 * 
 */
public class WordCount {
	
	/*
	 * Input to the Map function:
	 * DocID	Content
	 * 
	 * Map Emits
	 * Content.word;#;DocID	docWordCount
	 */
	public static class TokenizerMapper extends
			Mapper<Text, Text, Text, Text> {

		private Text mapKey = new Text();
		private Text mapValue = new Text();
		
		public void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {

			if (key == null || key.toString() == null || key.toString().trim().isEmpty()) {
				System.out.println("Empty string found");
				return;
			}
			
			String content = value.toString().trim();
			
			content = NLP.removeSpace(content);
			content = NLP.process(content);
			String[] tokens = content.split("\\s+");
			
			for(int i = 0; i < tokens.length; i++){
				String word = tokens[i].trim();
				word = NLP.stem(word);
				mapKey.set(word);
				mapValue.set("1");
				context.write(mapKey, mapValue);
			}

		}
		
	}
	
	/*
	 * Input to Reduce function:
	 * word;#;DocID	{docWordCount;#;Excerpt, docWordCount;#;Excerpt, ..., docWordCount;#;Excerpt} <word frequency>
	 * 
	 * Reduce emits:
	 * word;#;DocID	tf(term-frequency);#;Excerpt
	 */
	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {
		
		private Text result = new Text();
		
		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {

			if (key == null || key.toString() == null || key.toString().trim().isEmpty()) {
				System.out.println("Empty string found");
				return;
			}
			
			int wordCount = 0;
			
			for(Text val : values){
				wordCount++;
			}
			
			result.set(Integer.toString(wordCount));
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", "\t");
		Job job = Job.getInstance(conf, "Term Frequency");
		job.setJarByClass(WordCount.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
