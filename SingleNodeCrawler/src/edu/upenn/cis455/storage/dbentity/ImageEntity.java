package edu.upenn.cis455.storage.dbentity;

import java.net.URL;
import java.net.URLDecoder;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 *Entity to hold image data src and alt text extracted from the html pages 
 */

@Entity
public class ImageEntity {


    @PrimaryKey
    private String imageURL;
    
    private String imageALT;
    
    private String imageName;
    
    public String getImageName(){
    	return imageName;
    }

    public void setImageName(String s){
    	this.imageName = URLDecoder.decode(s);
    }
    
    
    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getImageSRC() {
        return imageALT;
    }

    public void setImageSRC(String imageSRC) {
    	
        this.imageALT = imageSRC;
    }
    
    

}