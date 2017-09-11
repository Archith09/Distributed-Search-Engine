package edu.upenn.cis455.utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import edu.upenn.cis455.configuration.Constants;

public class WorkerHelper {

	public static HttpURLConnection sendJob(String dest, String reqType,
			String job, String parameters) throws IOException {
		try{
			URL url = new URL("http://" + dest + "/" + job);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod(reqType);

			if (reqType.equals("POST")) {
				conn.setRequestProperty("Content-Type", "application/json");
				OutputStream os = conn.getOutputStream();
				byte[] toSend = parameters.getBytes();
				os.write(toSend);
				os.flush();
			} else {
				conn.getOutputStream();
			}
			return conn;
			
		} catch (ConnectException e){
			throw new IOException();
		}
		
		
	}

	public static int getNodeNumber(String key) {

		int nodename = 0;
		try {

			MessageDigest mDigest = MessageDigest.getInstance("SHA1");
			byte[] result = mDigest.digest(key.getBytes());

			BigInteger hashValue = new BigInteger(result);
			BigInteger numWorkers = new BigInteger(
					Integer.toString(Constants.peers.size()));
			BigInteger modulus = hashValue.mod(numWorkers);
			nodename = modulus.intValue();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			
		}
		return Math.abs(nodename);
	}
}
