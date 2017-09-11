package edu.upenn.cis455.master;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.utilities.SearchHelper;
import edu.upenn.cis455.utilities.WorkerHelper;

/*
 * This is the servlet implementation of the Master.java class
 * This class was not used to run the final jobs
 * 
 * Its suggested to use Master.java
 * 
 * 
 */
public class MasterServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static HashMap<String, Long> workerStatus = new HashMap<String, Long>();
	
	public void init(){
		long epoch = (long) 0;
		
		for(String worker : Constants.peers){
			workerStatus.put(worker, epoch);
		}
		
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

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
		
		if (servletPath.equals("/search")) {
			// TODO
			html = SearchHelper.getResults(request.getParameter("query").trim(), 10);
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
		
		if (servletPath.equals("/search")) {
			html = "<form method=\"post\" align=\"center\" action=\"/search\">" +
					"<input type=\"text\" align=\"center\" name=\"query\" placeholder=\"Search\" required=\"required\" />" +
					"<button type=\"submit\" align=\"center\" class=\"btn btn-primary btn-block btn-large\">Search</button>" +
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

	private void startIndexer() throws IOException {
		// TODO Auto-generated method stub
		
		for(Entry<String, Long> entry : workerStatus.entrySet()){
			
			if(System.currentTimeMillis() - entry.getValue()  <= 60000){
				int response = WorkerHelper.sendJob(entry.getKey(), "POST", "begin", "begin=true").getResponseCode();
				if(response != 200){
					System.out.println(entry.getKey() + " couldn't start indexing with response code " + response);
				}
			}
		}
		
	}

	private void updateWorkerStatus(HttpServletRequest request) {
		// TODO Auto-generated method stub
		int port = Integer.parseInt(request.getParameter("port"));
		String ip = request.getRemoteAddr();
		String status = request.getParameter("alive");
		if(status.equals("true")){
			workerStatus.put(ip + ":" + port, System.currentTimeMillis());
		}
	}
	
	private void shutdown() throws IOException {
		
		for(Entry<String, Long> entry : workerStatus.entrySet()){
			if(System.currentTimeMillis() - entry.getValue()  <= 60000){
				int response = WorkerHelper.sendJob(entry.getKey(), "POST", "shudown", null).getResponseCode();
				if(response != 200){
					System.out.println(entry.getKey() + " failed to shutdown " + response);
				}
			}
		}
		
		this.destroy();
		System.exit(0);
	}
	
	
	
}
