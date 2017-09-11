package edu.upenn.cis455.utilities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

/*
 * 
 */
@Entity
public class IndexEntry{

	@PrimaryKey
	String wordUrl;

	// This is a composite key for sorting
	@SecondaryKey(relate = Relationship.MANY_TO_ONE)
	String tfIdf;

	Double tf;
	Double idf;
	Double pageRank;
	Double hostRank;
	
	String word;
	String url;
	String title;
	String excerpt;
	
	int titleCount;
	int anchorCount;
	
	double totalScore = 0.0;
	boolean wordExistsInURL;
	int urlOccurenceCount = 1;
	
	final String FIELD_DELIMITER = ";#;";
	
	public IndexEntry(){}
	
	public IndexEntry(String entry){
		String[] temp = entry.split("\t", 2); 
		String key = temp[0].trim();
		String value = temp[1].trim();
		
		// Key Parameters
		this.wordUrl = key;
		this.word = key.split(FIELD_DELIMITER, 2)[0].trim();
		this.url = key.split(FIELD_DELIMITER, 2)[1].trim();
		
		// Value Parameters
		String[] field = value.split(FIELD_DELIMITER);
		this.tfIdf = this.word + FIELD_DELIMITER + field[1].trim(); // Composite Key
		this.tf = Double.parseDouble(field[2].trim());
		this.idf = Double.parseDouble(field[3].trim());
		this.excerpt = field[4].replace("EXCERPT@", "");
		this.title = field[5].replace("TITLE@", "");
		this.pageRank = Double.parseDouble(field[6].replace("PAGERANK@", ""));
		this.hostRank = Double.parseDouble(field[7].replace("HOSTRANK@", ""));
		this.titleCount = Integer.parseInt(field[8].replace("TITLECOUNT@", ""));
		this.anchorCount = Integer.parseInt(field[9].replace("KEYWORD@", ""));
		if(this.url.contains(this.word)){
			this.wordExistsInURL = true;
		} else {
			this.wordExistsInURL = false;
		}
	}
	
	public IndexEntry(String wordUrl, String word, String score){
		this.wordUrl = wordUrl;
		this.word = word;
		this.tfIdf = score;
	}

	public String getWordUrl() {
		return wordUrl;
	}

	public void setWordUrl(String wordUrl) {
		this.wordUrl = wordUrl;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
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

	public Double getPageRank() {
		return pageRank;
	}

	public void setPageRank(Double pageRank) {
		this.pageRank = pageRank;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getExcerpt() {
		return excerpt;
	}

	public void setExcerpt(String excerpt) {
		this.excerpt = excerpt;
	}

	public int getTitleCount() {
		return titleCount;
	}

	public void setTitleCount(int titleCount) {
		this.titleCount = titleCount;
	}

	public int getAnchorCount() {
		return anchorCount;
	}

	public void setAnchorCount(int anchorCount) {
		this.anchorCount = anchorCount;
	}

	public void setTotalScore(Double totalScore){
//		this.totalScore = Double.parseDouble(this.tfIdf) * this.pageRank;
		this.totalScore = totalScore;
	}
	
	public Double getTotalScore(){
		return totalScore;
	}
	
	public void setWordExistsInURL(boolean isExists){
		this.wordExistsInURL = isExists;
	}
	
	public boolean getWordExistsInURL(){
		return wordExistsInURL;
	}
	
	public void setUrlOccurenceCount(){
		this.urlOccurenceCount += 1;
	}
	
	public int getUrlOccurenceCount(){
		return urlOccurenceCount;
	}
	
	public String toString(){
		return (wordUrl + " ->-> " + word + " ->-> " + url + " ->-> " + tfIdf + " ->-> " + excerpt + " ->-> " + pageRank + 
				" ->-> " + hostRank + " ->-> " + title + " ->-> " + " ->-> " + titleCount + 
				" ->-> " + anchorCount + " ->-> " + wordExistsInURL);
	}

}
