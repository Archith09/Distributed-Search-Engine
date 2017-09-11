package edu.upenn.cis455.configuration;

import java.util.ArrayList;
import java.util.Arrays;

public class Constants {

	public static int PACKET_SIZE = 10000;
	public static int THREADS = 32;
	public static int BATCH_SIZE = 1000;
	public static int READ_SLEEP = 1;
	public static int WRITE_SLEEP = 10;
	public static final ArrayList<String> peers = new ArrayList<String>(
			Arrays.asList("54.227.111.238:8000", "34.203.229.34:8000",
					"34.203.207.226:8000", "52.205.44.29:8000",
					"54.210.37.104:8000", "52.90.219.76:8000",
					"54.89.234.37:8000", "54.164.23.155:8000",
					"52.55.21.62:8000", "184.73.141.32:8000",
					"52.90.208.144:8000", "52.90.42.47:8000",
					"34.207.170.112:8000", "52.71.14.78:8000",
					"34.201.245.92:8000")); // IP:PORT of peers
}
