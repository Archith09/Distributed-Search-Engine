package edu.upenn.cis455.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.index.NLP;
import edu.upenn.cis455.search.ImageSearch;
import edu.upenn.cis455.search.Search;
import edu.upenn.cis455.search.UrlInformation;
import edu.upenn.cis455.storage.CountDataAccessor;
import edu.upenn.cis455.storage.ImageDataAccessor;
import edu.upenn.cis455.storage.ReinforcedLearningDataAccessor;
import edu.upenn.cis455.utilities.IndexEntry;
import edu.upenn.cis455.utilities.SpellCheck;

public class SubmitSearchQueryServlet extends HttpServlet {
	Search s;
	ImageSearch imageSearchObj;
	boolean isSpellCheckOn = true;
	NLP nlp = new NLP();
	String path= Constants.FORM_PATH;
	
	private static final long serialVersionUID = 1L;


	public void init() {
		try {
			// Initializing search servlet
			s = new Search();
			imageSearchObj = new ImageSearch();
			CountDataAccessor.openDB(Constants.countDB);
			ReinforcedLearningDataAccessor.openDB(Constants.reinforcedDB);
			// Initialization complete for search
			} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void destroy(){
		// Close Database
		CountDataAccessor.closeDB();
		ImageDataAccessor.closeDB();
		ReinforcedLearningDataAccessor.closeDB();
	} 

	/*
	 * @input
	 * 	String[] arr => Input query from user
	 * 	String location => User location
	 * @output
	 * 	String correctedString
	 * The method gives a corrected string to the user in the form of a suggestion
	 * e.g. input => chesse output => cheese
	 * 
	 */
	public String getCorrectedString(String[] queryArr, String location) {
		System.out.println("Get request received");
		StringBuffer buffer = new StringBuffer();
		StringBuffer didYouMean = new StringBuffer();
		buffer.append("<a color=\"#d83737;\" href=\""+path+"?query=");
		if (isSpellCheckOn) {
			for (String s : queryArr) {
				String tmp = SpellCheck.spellCheck(s).split(" ")[0];
				didYouMean.append(tmp + " ");
			}
		} else {
			for (String s : queryArr) {
				didYouMean.append(s + " ");
			}
		}
		buffer.append(didYouMean.toString());
		buffer.append("&pageNo=1&location="+location+"\">");
		buffer.append(didYouMean.toString());
		buffer.append("</a>");
		return buffer.toString().trim();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		long queryReceived = System.currentTimeMillis();
		String query = request.getParameter("query");
		String[] queryArr = query.split(" ");
		String location = request.getParameter("location");
		int pageNo = 1;
		
		try{
				pageNo = Integer.parseInt(request.getParameter("pageNo"));
		}catch(Exception e){
			// catch exception
		}

		if (query.length() > 0) {
			LinkedHashMap<String, UrlInformation> results = s.queryFromUI(
					query, location, pageNo, 10);

			long searchResponseReceived = System.currentTimeMillis();
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("SEARCH PROCESSING TIME:  "
					+ (searchResponseReceived - queryReceived));
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");

			// Query Web results
			StringBuffer html = new StringBuffer();
			int count = 0;
			if (results != null && query != null && query.length() > 0) {
				html = getHtmlResults(results, queryArr);
			} else {
				html.append("");
			}

			// Query image search results
			LinkedHashMap<String, UrlInformation> imageResults = new LinkedHashMap<String, UrlInformation>();
			imageResults = imageSearchObj.queryFromUI(query, location, pageNo, 20);

			String imageHtml = "";
			imageHtml = getImageHTML(imageResults);

			// total document count
			int docCount = 0;
			for (int k = 0; k < queryArr.length; k++) {
				String word = queryArr[k];
				word = nlp.removeStopWords(word);
				word = nlp.process(word);
				word = nlp.stem(word);
				docCount += CountDataAccessor.getResults(word)
						.getCount();
			}

			
			// setting the attribute
			request.setAttribute("result", html.toString());
			request.setAttribute("imageResult", imageHtml);
			request.setAttribute("documentCount", docCount);
			request.setAttribute("totalTime",
					(System.currentTimeMillis() - queryReceived));
			request.setAttribute("query", query);
			request.setAttribute("correctedString", getCorrectedString(queryArr,location));
			request.setAttribute("pageNo", pageNo);
			

			long queryResponseSent = System.currentTimeMillis();
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("TIME TO CREATE HTML RESPONSE: "
					+ (queryResponseSent - searchResponseReceived));
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println(query + ":::" + results.toString());
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		}
		try {
			request.getRequestDispatcher("searchJSP.jsp").forward(request,
					response);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * @input
	 * 	LinkedHashMap<String, UrlInformation> results => Contains sorted list of the results to be displayed
	 *	String[] queryArr => Input received from the user
	 * @output
	 * 	StringBuffer output => html response
	 * 
	 */
	private StringBuffer getHtmlResults(
			LinkedHashMap<String, UrlInformation> results, String[] queryArr) {
		// TODO Auto-generated method stub
		StringBuffer html = new StringBuffer();
		int count = 0;
		html.append("<ul class=\"media-list\">\n");
		for (String key : results.keySet()) {
			count++;
			if (count >= 10)
				break;
			UrlInformation ie = results.get(key);

			html.append("<li class = \"media\">\n");

			// voting 
			html.append("<div class=\"media-body\">");
			
			
			// declaring the title
			html.append("<a target=\"_blank\" class=\"bg-primary\" href = \""
					+ ie.getUrl() + "\">\n");
			html.append("<strong>");
			html.append("<h4 style = \"color:#337ab7;\" class=\"media-heading\">\n");
			html.append(ie.getTitle());
			html.append("</h4>\n");
			html.append("</strong>");
			html.append("</a>\n");

			html.append("<a style=\"color: #3c763d\"class=\"text-justify\" href=\""
					+ ie.getUrl() + "\">");
			html.append(ie.getUrl());
			html.append("</a>");

			// declaring the body
			html.append("<p class=\"text-justify\">\n");
			String op = ie.getExcerpt();
			for (int j = 0; j < queryArr.length; j++)
				if (op.toLowerCase().contains(queryArr[j].toLowerCase())) {
					op = op.replaceAll(queryArr[j].toLowerCase(), "<strong>"
							+ queryArr[j].toLowerCase() + "</strong>");
				}

			op = (op.charAt(0) + "").toUpperCase() + op.substring(1);

			html.append(op);
			html.append("</p>\n");

			// adding sharing links
			html.append("<div id=\"share-buttons\">\n");

			// <!-- Buffer -->
			html.append("<a target=\"_blank\" href=\"https://bufferapp.com/add?url="
					+ ie.getUrl() + "&amp;text=" + ie.getTitle() + "\">\n");
			html.append("<img src=\"img/somacro/buffer.png\" alt=\"Buffer\" />");
			html.append("</a>");

			// Digg
			html.append("<a  target=\"_blank\" href=\"http://www.digg.com/submit?url="
					+ ie.getUrl() + "\">");
			html.append("<img src=\"img/somacro/diggit.png\" alt=\"Digg\" />");
			html.append("</a>");

			// Email
			html.append("<a target=\"_blank\" href=\"mailto:?Subject="
					+ ie.getTitle()
					+ "&amp;Body=I%20saw%20this%20and%20thought%20of%20you!%20"
					+ ie.getUrl()
					+ "\"> <img src=\"img/somacro/email.png\" alt=\"Email\" /> </a>");

			// Facebook
			html.append("<a target=\"_blank\" href=\"http://www.facebook.com/sharer.php?u="
					+ ie.getUrl()
					+ "\"> <img src=\"img/somacro/facebook.png\" alt=\"Facebook\" /> </a>");

			// Google+
			html.append("<a target=\"_blank\" href=\"https://plus.google.com/share?url="
					+ ie.getUrl()
					+ "\"> <img src=\"img/somacro/google.png\" alt=\"Google\" /> </a>");

			// LinkedIn
			html.append("<a target=\"_blank\" href=\"http://www.linkedin.com/shareArticle?mini=true&amp;url="
					+ ie.getUrl()
					+ "\"> <img src=\"img/somacro/linkedin.png\" alt=\"LinkedIn\" /> </a>");

			// Reddit
			html.append("<a target=\"_blank\" href=\"http://reddit.com/submit?url="
					+ ie.getUrl()
					+ "&amp;title="
					+ ie.getTitle()
					+ "\"> <img src=\"img/somacro/reddit.png\" alt=\"Reddit\" /> </a>");

			// StumbleUpon
			html.append("<a target=\"_blank\" href=\"http://www.stumbleupon.com/submit?url="
					+ ie.getUrl()
					+ "&amp;title="
					+ ie.getTitle()
					+ "\"> <img src=\"img/somacro/stumbleupon.png\" alt=\"StumbleUpon\" /> </a>");

			// Tumblr
			html.append("<a target=\"_blank\" href=\"http://www.tumblr.com/share/link?url="
					+ ie.getUrl()
					+ "&amp;title="
					+ ie.getTitle()
					+ "\"> <img src=\"img/somacro/tumblr.png\" alt=\"Tumblr\" /> </a>");

			html.append("<button  name =\"up\" value=\""+ie.getUrl()+"\" style =\"float:right\"type=\"button\" class=\"btn btn-default voting\">");
			html.append("<span class=\"glyphicon glyphicon-plus\" aria-hidden=\"true\"></span>");
			html.append("</button>");
			html.append("&nbsp &nbsp");
			html.append("<button  name = \"down\"value=\""+ie.getUrl()+"\"type=\"button\" style =\"float:right\" class=\"down btn btn-default voting\">");
			html.append("<span class=\"glyphicon glyphicon-minus\" aria-hidden=\"true\"></span>");
			html.append("</button>");
			
			
			html.append("</div>\n");

			html.append("</div>");

			html.append("</li>");
		}
		html.append("</ul>\n");
		return html;
	}
	/*
	 * @input 
	 * LinkedHashMap<String, UrlInformation> imageResults => Top results from the image search
	 * 
	 * @output
	 * 	String output => Html embedded strings
	 */
	private String getImageHTML(
			LinkedHashMap<String, UrlInformation> imageResults) {
		
		int count = 0;

		if (imageResults != null && imageResults.size() > 0) {
			StringBuffer imageHtml = new StringBuffer();
			imageHtml.append("<div class=\"demo-gallery\">");
			imageHtml
					.append("<ul id=\"lightgallery\" class=\"list-unstyled row\">");
			for (String key : imageResults.keySet()) {
				count++;
				if(count >= 20)
					break;
				UrlInformation u = imageResults.get(key);
				imageHtml
						.append("<li class=\"col-xs-6 col-sm-4 col-md-3\" data-src=\""
								+ u.getUrl()
								+ "\" data-sub-html=\""
								+ "exceprt"
								+ u.getExcerpt()
								+ " title "
								+ u.getTitle() + "\">");
				imageHtml.append("<a  href = \"" + u.getUrl()+ "\" target=\"_blank\" >");
				imageHtml.append("<img style=\"padding-top:10px; padding-left:10px;padding-bottom:10px;padding-right:10px;\"class = \"img-responsive\" src=\""
						+ u.getUrl() + "\">");
				imageHtml.append("</a>");

				imageHtml.append("</li>");
			}
			imageHtml.append("</ul>");
			imageHtml.append("</div>");

			return imageHtml.toString();

		} else {
			return "";
		}

	}
}
