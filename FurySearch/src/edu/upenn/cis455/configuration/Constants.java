package edu.upenn.cis455.configuration;

import java.util.ArrayList;
import java.util.Arrays;

public class Constants {

	public static int PACKET_SIZE = 10000;
	public static int THREADS = 32;
	public static int BATCH_SIZE = 1000;
	public static int READ_SLEEP = 100;
	public static int WRITE_SLEEP = 100;
//	public static String imageDB = "/home/ec2-user/imageDb/output";
//	public static String countDB = "/home/ec2-user/countDb/output";
//	public static String reinforcedDB = "/home/ec2-user/reinforcedDb/";
	
	public static String imageDB = "C:\\Users\\Rishi Solanki\\Desktop\\data\\imageDb\\output";
	public static String countDB = "C:\\Users\\Rishi Solanki\\Desktop\\data\\countDb\\output";
	public static String reinforcedDB = "C:\\Users\\Rishi Solanki\\Desktop\\data\\countDb";
	
	public static String SPELL_SERVER = "/home/ec2-user/spellcheck/spell.py";
	public static String FORM_PATH = "/FurySearch/search";
	
	public static final ArrayList<String> peers = new ArrayList<String>(
			Arrays.asList("54.204.182.74:8000", "52.90.28.68:8000",
					"54.221.10.192:8000", "52.201.252.27:8000",
					"54.210.188.100:8000", "54.174.108.3:8000",
					"34.207.99.156:8000", "52.90.123.150:8000",
					"54.83.141.160:8000", "54.91.161.116:8000",
					"54.157.34.176:8000", "54.173.233.70:8000",
					"54.85.139.145:8000", "52.91.69.244:8000",
					"54.172.55.181:8000")); // IP:PORT of peers
}
