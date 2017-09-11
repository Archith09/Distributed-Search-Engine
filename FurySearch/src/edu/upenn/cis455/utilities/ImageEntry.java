package edu.upenn.cis455.utilities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class ImageEntry {

	@PrimaryKey
	String wordUrl;
	
	@SecondaryKey(relate = Relationship.MANY_TO_ONE)
	String tfIdf;
	
	Double tf;
	Double idf;
	
	String word;
	String url;
	
	double totalScore = 0.0;
	boolean wordExistsInURL;
	int urlOccurenceCount = 1;
	
	final String FIELD_DELIMITER = ";#;";
	
	public ImageEntry(){}
	
	public ImageEntry(String entry){
		
		String[] temp = entry.split("\t", 2);
		String key = temp[0].trim();
		String value = temp[1].trim();
		
		// Key Parameters
		this.wordUrl = key;
		this.word = key.split(FIELD_DELIMITER, 2)[0].trim();
		this.url = key.split(FIELD_DELIMITER, 2)[1].trim();
		
		String[] field = value.split(FIELD_DELIMITER);
		this.tfIdf = this.word + FIELD_DELIMITER + field[1].trim();
		this.tf = Double.parseDouble(field[2].trim());
		this.idf = Double.parseDouble(field[3].trim());
		
		if(this.url.contains(this.word)){
			this.wordExistsInURL = true;
		} else {
			this.wordExistsInURL = false;
		}
		
	}
	
	public String getWordUrl() {
		return wordUrl;
	}

	public void setWordUrl(String wordUrl) {
		this.wordUrl = wordUrl;
	}

	public String getTfIdf() {
		return tfIdf;
	}

	public void setTfIdf(String tfIdf) {
		this.tfIdf = tfIdf;
	}

	public Double getTf() {
		return tf;
	}

	public void setTf(Double tf) {
		this.tf = tf;
	}

	public Double getIdf() {
		return idf;
	}

	public void setIdf(Double idf) {
		this.idf = idf;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public double getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(double totalScore) {
		this.totalScore = totalScore;
	}

	public boolean isWordExistsInURL() {
		return wordExistsInURL;
	}

	public void setWordExistsInURL(boolean wordExistsInURL) {
		this.wordExistsInURL = wordExistsInURL;
	}

	public int getUrlOccurenceCount() {
		return urlOccurenceCount;
	}

	public void setUrlOccurenceCount(int urlOccurenceCount) {
		this.urlOccurenceCount = urlOccurenceCount;
	}
	
}
