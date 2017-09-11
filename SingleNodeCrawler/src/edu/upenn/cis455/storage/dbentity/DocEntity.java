package edu.upenn.cis455.storage.dbentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * class to make entity for documents
 */

@Entity
public class DocEntity {

	@PrimaryKey
	private String DocCode;
	private ArrayList<String> content; // the raw content
	private String address;
	// private HashMap<String, String> doctype = new HashMap<String, String>();
	private String doctype;
	private ArrayList<String> parsedlinks;
	private String type;
	private long lastaccessed;
	private ArrayList<String> contentseenurls = new ArrayList<String>();
	
	private HashMap<String, String> imageMap = new HashMap<String, String>();

	public HashMap<String, String> getImageMap() {
		return imageMap;
	}
	public void setImageMap(HashMap<String, String> imageResult) {
		this.imageMap = imageResult;
	}
	public void addToImageMap(String key, String value){
		imageMap.put(key, value);
	}
	

	private String metadata;
	private String title;
	private String body;
	private String headings;
	
	/* Rishi*/
	 private String person;
	 private String location;
	 private String organization;
	 
	 
	 public String getPerson(){
		 return this.person;
	}
	public String getLocation(){
		return this.location;
	} 
	public String getOrganization(){
		return this.organization;
	}
	public void setPerson(LinkedHashSet<String> t){
		this.person = t.toString();		
	}
	public void setLocation(LinkedHashSet<String> t){
		this.location = t.toString();		
	}
	public void setOrganization(LinkedHashSet<String> t){
		this.organization = t.toString();		
	}
	 /* Rishi */
		

	public String getHeadings() {
	if(headings == null)
		return "";
	else
		return headings;
	}

	public void setHeadings(String headings) {
		this.headings = headings;
	}
	
	
	

	public String getDoctype() {
		return doctype;
	}

	public void setDoctype(String doctype) {
		this.doctype = doctype;
	}

	public String getMetadata() {
	if(metadata == null)
		return "";
	else
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public String getTitle() {
		if(title == null)
			return "";
		else		
			return title;
	}

	public void setTitle(String headings) {
		this.title = headings;
	}

	public String getBody() {
		if(body == null)
			return "";
		else
			return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public ArrayList<String> getContentseenurls() {
		if(contentseenurls == null)
			return new ArrayList(Arrays.asList(""));
		else
			return contentseenurls;
	}

	public void setContentseenurls(ArrayList<String> contentseenurls) {
		this.contentseenurls = contentseenurls;
	}

	public long getLastaccessed() {
		return lastaccessed;
	}

	public void setLastaccessed(long lastaccessed) {
		this.lastaccessed = lastaccessed;
	}

	public DocEntity() {

	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<String> getParsedlinks() {
		if(parsedlinks == null)
			return new ArrayList(Arrays.asList(""));
		else
			return parsedlinks;
	}

	public void setParsedlinks(ArrayList<String> parsedlinks) {
		this.parsedlinks = parsedlinks;
	}

	public String getDocCode() {
		return DocCode;
	}

	public void setDocCode(String docCode) {
		DocCode = docCode;
	}

	public ArrayList<String> getContent() {
		if(content == null)
			return new ArrayList(Arrays.asList(""));
		else
			return content;
	}

	public void setContent(ArrayList<String> content) {
		this.content = content;
	}

	public String getAddress() {
		if(address == null)
			return "";
		else
			return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
