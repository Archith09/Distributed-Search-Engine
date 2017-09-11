package edu.upenn.cis.crawler.checkpointing;

/**
 *Class to push the BDB data in the producer queue to S3
 * 
 */
 

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class CopyOfConsumer extends Thread {

	final ArrayList<String> queue;
	Object lock;
	int queueLimit;
	String bucketName;
	int poolNo;

	final AmazonS3 s3;

	CopyOfConsumer(ArrayList<String> queue, Object lock, int queueLimit,
			String bucketName, int poolNo) {
		this.queue = queue;
		this.lock = lock;
		this.queueLimit = queueLimit;
		this.bucketName = bucketName;
		this.poolNo = poolNo;
		s3 = getS3Client();
		createBucket(bucketName); // if bucket exists then do nothing else
									// create bucket

	}

	public synchronized int getQueueSize() {
		return queue.size();
	}

	public void run() {

		while (!CopyOfConsumerPool.getShutdown(poolNo)) {
			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out.println("BEGININIG OF CONSUMER" + poolNo
					+ ".........................");
			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			String item = "";
			try {
				
				synchronized (queue) {
					if (queue.size() == 0
							&& !CopyOfConsumerPool.getShutdown(poolNo)) {
						try {
							queue.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						if(!queue.isEmpty())
							item = queue.remove(0);
						else
							queue.notifyAll();
					}
					queue.notifyAll();
				
				}
				// save item to S3
				
				if (item.length() > 0 && item.indexOf(" ") != -1) {
					String key = item.substring(0, item.indexOf(" "));
					byte[] bytes = item.substring(item.indexOf(" ")+1)
							.getBytes();
					System.out.println("Pushing to S3 " + key + "  " + poolNo);
					pushObjectToS3(bucketName,getSanidhyaPath(poolNo)+ "/"+key, bytes);
				} else {
					//e.printStackTrace();
					throw new Exception("Input request error" + item);
//					System.out.println("Error with the input request");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}          
		}

		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");
		System.out.println("Exiting storage consument threads");
		System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");
		synchronized (queue) {
			queue.clear();
			queue.notifyAll();
		}

		CopyOfConsumerPool.setShutdown(poolNo);
		System.out.println("=================================");
		System.out.println("Shut consumer thread pool completely");
		CopyOfConsumerPool.shut(poolNo);
		System.out.println("=================================");
		this.interrupt();

	}

	public String getSanidhyaPath(int i){
		String s = "";
		switch(i){
		case 1:
			s = "input/base";
			break;
		case 2:
			s = "temp/many/tfidf_title/title";
			break;
		case 3:
			s = "temp/one/rc_title/title";
			break;
		case 4:
			s = "temp/one/rc_title_keyword/keyword";
			break;
		case 5:
		
			s = "input/pagerank";
			break;
		case 6:
			s = "backup";
			break;
		case 7:
			s = "input/image";
			break;
		}
		
		return s;
	}
	
	public Bucket createBucket(String bucketName) {
		Bucket b = null;
		if (s3.doesBucketExist(bucketName)) {
			System.out.println(" Bucket already exists " + bucketName);
		} else {

			try {
				b = s3.createBucket(bucketName);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

		}
		return b;
	}

	public AmazonS3 getS3Client() {
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default")
					.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. "
							+ "Please make sure that your credentials file is at the correct "
							+ "location (~/.aws/credentials), and is in valid format.",
					e);
		}

		@SuppressWarnings("deprecation")
		AmazonS3 s3 = new AmazonS3Client(credentials);
		// Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		// s3.setRegion(usWest2);
		return s3;
	}

	public void pushObjectToS3(String bucketName, String key, byte[] data) {
		InputStream value = new ByteArrayInputStream(data);

		System.out.println("===========================================");
		System.out.println("Uploading to S3 with key " + key + " in s3 bucket "
				+ bucketName);
		System.out.println("===========================================\n");

		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentLength(data.length);

		s3.putObject(new PutObjectRequest(bucketName, key, value, meta));
		CopyOfConsumerPool.incrementCount(poolNo);
		System.out.println("=========================================");
		System.out.println("Consumer Count"
				+ CopyOfConsumerPool.getCount(poolNo));
		System.out.println("Consumer Max Count" + CopyOfConsumerPool.maxLimit);
		System.out.println("=========================================");
	}

	public void removeSingleObjectsFromS3(Object key) {

		try {
			s3.deleteObject(bucketName, key.toString());
			System.out.println("===========================================");
			System.out.println("Deleted key " + key.toString()
					+ " from s3 bucket " + bucketName);
			System.out.println("===========================================\n");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Key: " + key.toString()
					+ " not present in the bucket " + bucketName);
		}
	}

	public void getAndDeleteObjectsFromS3() {
		ObjectListing o1 = s3.listObjects(bucketName);
		List<S3ObjectSummary> objects = o1.getObjectSummaries();
		for (S3ObjectSummary os : objects) {
			removeSingleObjectsFromS3(os.getKey());
		}
	}

	public void saveLastBatchInS3() {
		String item;
		item = queue.remove(0);
		// save item to S3
		if (item.indexOf("\t") != -1) {
			String key = item.substring(0, item.indexOf("\t"));
			byte[] bytes = item.substring(item.indexOf("\t")).getBytes();
			System.out.println("Pushing to S3 " + key);
			pushObjectToS3(bucketName, key, bytes);
		} else {
			System.out.println("Error with the input request");
		}
	}
}
