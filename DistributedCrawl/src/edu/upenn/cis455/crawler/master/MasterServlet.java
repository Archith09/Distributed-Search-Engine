package edu.upenn.cis455.crawler.master;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.crawler.utilities.WorkerHelper;
import static spark.Spark.setPort;


import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.crawler.utilities.WorkerHelper;
import spark.Route;
import spark.Spark;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Master Servlet to Run the master
 * 
 *
 */

	
	public class MasterServlet  {
		
		private static HashMap<String, Long> workerStatus = new HashMap<String, Long>();
		
		
		public static void main(String[] args) {
			long epoch = (long) 0;
			//workerStatus.put("52.206.12.58:8001", epoch);
			//workerStatus.put("34.204.10.73:8001", epoch);
			//workerStatus.put("52.55.101.123:8001", epoch);
			
			workerStatus.put("107.23.73.225:8000", epoch);
			workerStatus.put("54.208.116.106:8001", epoch);
			workerStatus.put("52.54.251.242:8002", epoch);
			workerStatus.put("54.172.24.81:8003", epoch);
			workerStatus.put("54.205.20.19:8004", epoch);
			
			MasterServlet ms = new MasterServlet(Integer.parseInt(args[0]));
			
			
		}
		
		public MasterServlet(int port){
			
			setPort(port);
			final ObjectMapper om = new ObjectMapper();
			om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
			Spark.post(new Route("/workerstatus") {
				
				
				
				@Override
				public Object handle(Request request, Response response) {
					
					
					int port = Integer.parseInt(request.queryParams("port"));
					String ip = request.ip();
					String status = request.queryParams("alive");
					if(status.equals("true")){
						workerStatus.put(ip + ":" + port, System.currentTimeMillis());
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
				//response(html.getBytes().length);
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
						
						if(status.equals("true")){
							workerStatus.put(ip + ":" + port, System.currentTimeMillis());
						}
					
					response.status(200);
					return response;
				}
									
			});
		
		
		Spark.get(new Route("/start") {
			
			@Override
			public Object handle(Request request, Response response) {
				
				
				
				String html = "<h1 align=\"center\">Run Indexer</h1>" + 
						"<form method=\"post\" align=\"center\" action=\"/start\">" +
						"<button type=\"submit\" align=\"center\" class=\"btn btn-primary btn-block btn-large\">Run</button>" + 
						"</form>";
					
				
				response.type("text/html");
				//response(html.getBytes().length);
				response.body(html);
				return html;
			}
								
		});

			
			
		}
		
		
		
		
		
		/*public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

			String servletPath = request.getServletPath();
			String html = "";

			if (servletPath.equals("/workerstatus")) {
				doGet(request, response);
				return;
			}

			if (servletPath.equals("/start")) {
				startIndexer();
				html = "<h1>Indexer has been started</h1>";
			}

			response.setContentType("text/html");
			response.setContentLength(html.getBytes().length);
			response.getOutputStream().write(html.getBytes());
		}

		public void doGet(HttpServletRequest request, HttpServletResponse response)
				throws java.io.IOException {

			String servletPath = request.getServletPath();
			String html = "";

			if (servletPath.equals("/workerstatus")) {
				updateWorkerStatus(request);
			}

			if (servletPath.equals("/start")) {
				html = "<h1 align=\"center\">Run Indexer</h1>" + 
						"<form method=\"post\" align=\"center\" action=\"/start\">" +
						"<button type=\"submit\" align=\"center\" class=\"btn btn-primary btn-block btn-large\">Run</button>" + 
						"</form>";
			}

			if (servletPath.equals("/shutdown")) {
				shutdown();
				html = "<h1>Bye!</h1>"; // This output however will not be seen,
										// since jetty will be closed with
										// shutdown()
			}

			response.setContentType("text/html");
			response.setContentLength(html.getBytes().length);
			response.getOutputStream().write(html.getBytes());
		}

	*/	private void startIndexer() throws IOException {
			// TODO Auto-generated method stub
			
			for(Entry<String, Long> entry : workerStatus.entrySet()){
				
				if(System.currentTimeMillis() - entry.getValue()  <= 60000){
					int response = WorkerHelper.sendJob(entry.getKey(), "POST", "begin", "being=true").getResponseCode();
					if(response != 200){
						System.out.println(entry.getKey() + " couldn't start indexing with response code " + response);
					}
				}
			}
			
		}

	/*	private void updateWorkerStatus(HttpServletRequest request) {
			// TODO Auto-generated method stub
			int port = Integer.parseInt(request.getParameter("port"));
			String ip = request.getRemoteAddr();
			String status = request.getParameter("alive");
			if(status.equals("true")){
				workerStatus.put(ip + ":" + port, System.currentTimeMillis());
			}
		}*/
		
		private void shutdown() {
			// TODO Auto-generated method stub
			
		}

	}

