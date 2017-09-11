package edu.upenn.cis455.index;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

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
 * 
 */
public class TermFrequency {
	
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
		private final static String FIELD_DELIMITER = ";#;";
		

		public void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {

			if (key == null || key.toString() == null || key.toString().trim().isEmpty()) {
				System.out.println("Empty string found");
				return;
			}
			
			String docID = key.toString().trim();
			String content = value.toString().trim();
			
			content = NLP.removeSpace(content);
			content = NLP.process(content);
			
			String[] tokens = content.split("\\s+");
			int docWordCount = getMaxFrequency(tokens);
			
			for(int i = 0; i < tokens.length; i++){
				String word = tokens[i].trim();
				StringBuffer keyOut = new StringBuffer();
				keyOut.append(NLP.stem(word));
				keyOut.append(FIELD_DELIMITER);
				keyOut.append(docID);
				mapKey.set(keyOut.toString());
				
				StringBuffer valueOut = new StringBuffer();
				valueOut.append(Integer.toString(docWordCount));
				valueOut.append(FIELD_DELIMITER);
				valueOut.append("EXCERPT@");
				valueOut.append(NLP.excerpt(i, tokens));
				mapValue.set(valueOut.toString());
				
				context.write(mapKey, mapValue);
			}

		}
		
		public int getMaxFrequency(String[] tokens){
			HashMap<String, Integer> count = new HashMap<String, Integer>();
			
			for(int i = 0; i < tokens.length; i++){
				String word = tokens[i];
				if(count.containsKey(word)){
					int value = count.get(word);
					count.put(word, value + 1);
				} else {
					count.put(word, 1);
				}
			}
			
			int max = 0;
			for(Entry<String, Integer> entry : count.entrySet()){
				if(entry.getValue() > max){
					max = entry.getValue();
				}
			}
			
			return max;
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
		private final static String FIELD_DELIMITER = ";#;";
		
		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {

			if (key == null || key.toString() == null || key.toString().trim().isEmpty()) {
				System.out.println("Empty string found");
				return;
			}
			
			Double wordFrequency = (double) 0;
			Double wordPerDoc = (double) 1; // Although 1 will never be used
			String excerpt = "";
			
			for(Text val : values){
				wordFrequency++;
				String[] temp = val.toString().split(FIELD_DELIMITER);
				wordPerDoc = Double.parseDouble(temp[0]);
				excerpt = temp[1];
			}
			
			Double a = 0.5;
			Double tf = a + (a * (wordFrequency / wordPerDoc)); 
			
			StringBuffer reduceValue = new StringBuffer();
			reduceValue.append(Double.toString(tf));
			reduceValue.append(FIELD_DELIMITER);
			reduceValue.append(excerpt);
			
			result.set(reduceValue.toString());
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", "\t");
		Job job = Job.getInstance(conf, "Term Frequency");
		job.setJarByClass(TermFrequency.class);
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
