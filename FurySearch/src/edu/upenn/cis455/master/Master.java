package edu.upenn.cis455.master;

import static spark.Spark.setPort;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.utilities.SearchHelper;
import edu.upenn.cis455.utilities.WorkerHelper;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/*
 * This class implements a master which overlooks the distributed cluster creating the index
 * It receives heartbeats from the workers and instruct them to begin distributed computation  
 */
public class Master {

	private static HashMap<String, Long> workerStatus = new HashMap<String, Long>();

	public static void main(String[] args) {
		long epoch = (long) 0;

		for (String worker : Constants.peers) {
			workerStatus.put(worker, epoch);
		}

		Master ms = new Master(Integer.parseInt(args[0]));

	}

	public Master(int port) {

		setPort(port);
		final ObjectMapper om = new ObjectMapper();
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		Spark.post(new Route("/workerstatus") {

			@Override
			public Object handle(Request request, Response response) {

				int port = Integer.parseInt(request.queryParams("port"));
				String ip = request.ip();
				String status = request.queryParams("alive");
				if (status.equals("true")) {
					workerStatus.put(ip + ":" + port,
							System.currentTimeMillis());
				}

				return "workerstatus";

			}

		});

		Spark.post(new Route("/start") {

			@Override
			public Object handle(Request request, Response response) {

				try {
					startIndexer();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String html = "<h1>Indexer has been started</h1>";

				response.type("text/html");
				// response(html.getBytes().length);
				response.body(html);

				return html;

			}

		});

		Spark.get(new Route("/workerstatus") {

			@Override
			public Object handle(Request request, Response response) {

				int port = Integer.parseInt(request.queryParams("port"));
				String ip = request.ip();
				String status = request.queryParams("alive");

				if (status.equals("true")) {
					workerStatus.put(ip + ":" + port,
							System.currentTimeMillis());
				}

				response.status(200);
				return response;
			}

		});

		Spark.get(new Route("/start") {

			@Override
			public Object handle(Request request, Response response) {

				String html = "<h1 align=\"center\">Run Indexer</h1>"
						+ "<form method=\"post\" align=\"center\" action=\"/start\">"
						+ "<button type=\"submit\" align=\"center\" class=\"btn btn-primary btn-block btn-large\">Run</button>"
						+ "</form>";

				response.type("text/html");
				// response(html.getBytes().length);
				response.body(html);
				return html;
			}

		});
		
		Spark.get(new Route("/search") {

			@Override
			public Object handle(Request request, Response response) {

				String html = "<form method=\"post\" align=\"center\" action=\"/search\">" +
						"<input type=\"text\" align=\"center\" name=\"query\" placeholder=\"Search\" required=\"required\" />" +
						"<button type=\"submit\" align=\"center\" class=\"btn btn-primary btn-block btn-large\">Search</button>" +
						"</form>";

				response.type("text/html");
				// response(html.getBytes().length);
				response.body(html);
				return html;
			}

		});
		
		Spark.post(new Route("/search") {

			@Override
			public Object handle(Request request, Response response) {
				long begin = System.currentTimeMillis();
				String html = SearchHelper.getResults(request.queryParams("query").trim(), 20);
				long finish = System.currentTimeMillis();
				html = "</h1> Time elapsed: " +  (finish - begin) + " msec </h1> " + html;
				response.type("text/html");
				// response(html.getBytes().length);
				response.body(html);

				return html;

			}

		});
		
		
		Spark.get(new Route("/shutdown") {

			@Override
			public Object handle(Request request, Response response) {

				try {
					shutdown();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				String html = "<form method=\"post\" align=\"center\" action=\"/search\">" +
						"<input type=\"text\" align=\"center\" name=\"query\" placeholder=\"Search\" required=\"required\" />" +
						"<button type=\"submit\" align=\"center\" class=\"btn btn-primary btn-block btn-large\">Search</button>" +
						"</form>";

				response.type("text/html");
				// response(html.getBytes().length);
				response.body(html);
				return html;
			}

		});
		
	}

	private void startIndexer() throws IOException {
		// TODO Auto-generated method stub

		for (Entry<String, Long> entry : workerStatus.entrySet()) {

			if (System.currentTimeMillis() - entry.getValue() <= 60000) {
				int response = WorkerHelper.sendJob(entry.getKey(), "POST",
						"begin", "being=true").getResponseCode();
				if (response != 200) {
					System.out.println(entry.getKey()
							+ " couldn't start indexing with response code "
							+ response);
				}
			}
		}

	}

	private void shutdown() throws IOException {
		// TODO Auto-generated method stub
		for(Entry<String, Long> entry : workerStatus.entrySet()){
			if(System.currentTimeMillis() - entry.getValue()  <= 60000){
				int response = WorkerHelper.sendJob(entry.getKey(), "POST", "shutdown", null).getResponseCode();
				if(response != 200){
					System.out.println(entry.getKey() + " failed to shutdown " + response);
				}
			}
		}
		
	}

}
