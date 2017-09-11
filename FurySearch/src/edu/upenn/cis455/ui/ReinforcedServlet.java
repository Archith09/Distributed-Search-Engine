package edu.upenn.cis455.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.configuration.Constants;
import edu.upenn.cis455.configuration.ReinforcedResponse;
import edu.upenn.cis455.index.NLP;
import edu.upenn.cis455.search.ImageSearch;
import edu.upenn.cis455.search.Search;
import edu.upenn.cis455.search.UrlInformation;
import edu.upenn.cis455.storage.CountDataAccessor;
import edu.upenn.cis455.storage.ImageDataAccessor;
import edu.upenn.cis455.storage.ReinforcedLearningDataAccessor;
import edu.upenn.cis455.utilities.IndexEntry;
import edu.upenn.cis455.utilities.ReinforcedEntry;
import edu.upenn.cis455.utilities.SpellCheck;
/*
 * Receive input from user about the ranking of the displayed results
 */
public class ReinforcedServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		String url = request.getParameter("url");
		String voteType = request.getParameter("vote");

		ReinforcedResponse rr = ReinforcedLearningDataAccessor.getResults(url);

		ReinforcedEntry re = new ReinforcedEntry();
		re.setUrl(url);

		if (rr.getCount() == 0) {
			// new url
			re.setCount(0);
		} else {
			// url exist, retrieve value
			re.setCount(rr.getCount());
		}

		if (voteType.equalsIgnoreCase("up")) {
			re.incrementCount();
		} else {
			re.decrementCount();
		}

		ReinforcedLearningDataAccessor.put(re);
		request.setAttribute("response", "Feedback received");
//		try {
//			request.getRequestDispatcher("searchJSP.jsp").forward(request,
//					response);
//		} catch (ServletException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public void destroy() {
		ReinforcedLearningDataAccessor.closeDB();
	}
}
