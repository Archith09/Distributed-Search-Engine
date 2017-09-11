package edu.upenn.cis455.storage.dbentity;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Class to make the secondary document entity that holds the doc data for the whole crawl
 */

@Entity
public class DocSecondaryEntity {

	/**
	 * 
	 */

	@PrimaryKey
	private String DocCode;

	private String address;
	// private HashMap<String, String> doctype = new HashMap<String, String>();
	private String doctype;
	private ArrayList<String> parsedlinks;
	private String type;
	private long lastaccessed;
	private ArrayList<String> contentseenurls = new ArrayList<String>();

	public DocSecondaryEntity() {

	}

	public String getDoctype() {
		return doctype;
	}

	public void setDoctype(String doctype) {
		this.doctype = doctype;
	}

	public ArrayList<String> getContentseenurls() {
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<String> getParsedlinks() {
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
