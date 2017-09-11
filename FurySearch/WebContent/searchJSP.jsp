<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta HTTP-EQUIV=Expires CONTENT="0">
<meta HTTP-EQUIV="Pragma" CONTENT="no-cache">

<!-- Latest compiled and minified CSS -->
<!-- <link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
	integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
	crossorigin="anonymous"> -->

<link rel="stylesheet" href="css/bootstrap.min.css">
<link rel="stylesheet" href="css/logo.css">

<!-- Hover effects -->
<link rel="stylesheet" href="css/hover.css">

<!-- Voting -->
<link rel="stylesheet" href="css/vote.css">

<link rel="shortcut icon" href="img/logo5.png" type="image/x-icon"
	sizes="16x16 24x24 32x32 64x64" />

<link
	href="https://fonts.googleapis.com/css?family=Arimo|Josefin+Slab|Open+Sans+Condensed:300|Roboto+Condensed"
	rel="stylesheet">

<!-- Style sheet for videoGallery -->
<link href="css/lightgallery.css" rel="stylesheet">
<link href="css/videoGalleryStyling.css" rel="stylesheet">

<!--  Hover JS -->
<script src="js/modernizr.custom.js"></script>

<script type="text/javascript"
	src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>

<!-- libraries for video gallery -->
<script src="js/picturefill.min.js"></script>
<script src="js/lightgallery.min.js"></script>
<script src="js/lg-fullscreen.min.js"></script>
<script src="js/lg-thumbnail.min.js"></script>
<script src="js/lg-video.min.js"></script>
<script src="js/lg-autoplay.min.js"></script>
<script src="js/lg-zoom.min.js"></script>
<script src="js/lg-hash.min.js"></script>
<script src="js/lg-pager.min.js"></script>
<script src="js/jquery.mousewheel.min.js"></script>

<!-- Latest compiled and minified JavaScript -->
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
	integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
	crossorigin="anonymous"></script>

<!-- All api js -->
<script src="js/fury.js"></script>


<title>CIS455/555 Project</title>
</script>
<style type="text/css">
.navbar-default .navbar-nav>.active>a, .navbar-default .navbar-nav>.active>a:focus,
	.navbar-default .navbar-nav>.active>a:hover {
	color: #fff;
	background-color: #d83737;
}

* {
	font-family: 'Arimo', sans-serif;
}

.nav .navbar-nav ul li {
	align: center;
}

#share-buttons img {
	width: 35px;
	padding: 5px;
	border: 0;
	box-shadow: 0;
	display: inline;
}

a {
	color: #d83737;
}

.imageCaption {
	top: 200px;
	left: 0;
}
</style>

<!-- JSP script -->
<%
	int pageNo = 1;
	String path = "/FurySearch/search";
	String location = "";
	String results = "";
	String query = "";
	String imageResult = "";
	String totalTime = "";
	String docCount = "";
	String correctedResults = null;
	if (request.getAttribute("result") != null && request.getAttribute("result").toString().length() > 0) {

		results = request.getAttribute("result").toString();
		System.out.println("Length: " + results.length());
		query = request.getAttribute("query").toString();

		imageResult = request.getAttribute("imageResult").toString();
		docCount = request.getAttribute("documentCount").toString();
		totalTime = request.getAttribute("totalTime").toString();
		System.out.println("IF LOOP FOR REQUEST.GET ATTR" + results.length() + "----" + query.length());
		correctedResults = request.getAttribute("correctedString").toString();
		System.out.println("correct results" + correctedResults);
		pageNo = Integer.parseInt(request.getAttribute("pageNo").toString());
	} else {
		results = "<h4>No results found || empty query</h4>";
		query = "";
		totalTime = null;
		docCount = null;
		imageResult = "<h4>No results found</h4>";
		System.out.println("ELSE LOOP FOR REQUEST.GET ATTR" + results.length() + "----" + query.length());
	}

	String ip = request.getHeader("X-Forwarded-For");
	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getHeader("Proxy-Client-IP");
	}
	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getHeader("WL-Proxy-Client-IP");
	}
	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getHeader("HTTP_CLIENT_IP");
	}
	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getHeader("HTTP_X_FORWARDED_FOR");
	}
	if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getRemoteAddr();
	}
	System.out.println(ip);
%>


</head>
<body style="background-color: #fff;">

	<nav class="navbar navbar-default navbar-fixed-top">
	<div class="container-fluid">
		<div class="row">
			<div class="col-lg-1 col-md-4 col-sm-12 navbar-header">
				<img src="img/logo5.png" class="thumbnail" width="100%"
					style="margin-bottom: 0px; margin-top: 5px;"></img>
			</div>

			<div class="col-lg-4 col-md-12 col-sm-12">
				<ul class="nav navbar-nav ">
					<li class="active"><a href="#web">Web</a></li>
					<!-- <li><a href="#pdf">PDF</a></li>-->
					<li><a href="#images">Images</a></li>
					<li><a href="#videos">Videos</a></li>
					<li><a href="#shopping">Shopping</a></li>
				</ul>

			</div>

			<div class="col-lg-5 col-md-12 col-sm-12">
				<form id="searchForm" action="<%=path%>" method="GET"
					class="navbar-form" role="search">
					<div class="col-lg-10 col-md-6 col-sm-6">
						<label for="search" class="sr-only col-form-label">Search
							Box</label>
						<%
							System.out.println("QUERYY" + query);
							if (query != null && query.length() > 0) {
						%>
						<input id="searchBox" type="text" style="width: 100%" name="query"
							value="<%=query%>" class="form-control" placeholder="Search">
						<%
							} else {
								System.out.println("ELSE LOOP FOR SEARHC BOX");
						%>
						<input id="searchBox" type="text" style="width: 100%" name="query"
							class="form-control" placeholder="Search">
						<%
							}
						%>


					</div>
					<div id="location">
						<input type="hidden" name="location" value="" />
					</div>

					<div id="pageNo">
						<input type="hidden" name="pageNo" value="1" />
					</div>

					<div class="col-lg-1 col-md-6 col-sm-2">
						<button type="submit" style="position: relative;" align="center"
							class="btn btn-default" id="submit">Submit</button>
					</div>

				</form>
			</div>

			<div id="climate" class="col-lg-2 col-md-12 col-sm-12 navbar-header">
				<!-- <p class="navbar-text">Climate API Output</p> -->
			</div>

		</div>
	</div>
	</nav>

	<div class="row" class="text-success">
		<div class="col-lg-12 col-md-12 col-sm-12"></div>
	</div>

	<div id="content" class="container-fluid"
		style="margin-top: 60px; margin-bottom: 60px;">

		<div id="top"
			class="col-lg-6 col-lg-offset-1 col-md-6 col-md-offset-1 col-sm-6 col-sm-offset-1"
			style="border: 1px light blue;">
			<%
				if (docCount == null) {
			%><br />
			<div>
				<br />
			</div>
			<%
				} else {
			%><br />
			<div>
				Total documents queried
				<%=docCount%>
				(<%=totalTime%>) millisecond.
				<%
				if (correctedResults != null && !correctedResults.toLowerCase().contains(query.trim())) {
			%>
				<br />
				<p>
					Did you mean :
					<%=correctedResults%>
				</p>
				<br>
				<%
					}
				%>
			</div>
			<%
				}
			%>
		</div>
		
		<br> <br>
		
		<div id="web"
			class="col-lg-6 col-lg-offset-1 col-md-6 col-md-offset-1 col-sm-6 col-sm-offset-1"
			style="border: 1px light blue;">
			<%=results%>
		</div>

		<div id="graph"
			class="col-lg-4 col-lg-offset-1 col-md-4 col-md-offset-1 col-sm-4 col-sm-offset-1 "
			style="border: 1px solid #b7b2b2;">
			<div id="wikiGraph" class="media-body"></div>
			<div id="googleGraph"></div>
		</div>

		<div id="images"
			class="col-lg-6 col-lg-offset-1 col-md-6 col-md-offset-1 col-sm-6 col-sm-offset-1"
			style="border: 1px light blue;">
			<%=imageResult%>
		</div>

		<div id="pdf" class="col-lg-12 col-md-12 col-sm-12">
		</div>

		<!-- Youtube  -->
		<div id="videos"
			class="col-lg-10 col-lg-offset-1 col-md-10 col-md-offset-1  col-sm-10 col-sm-offset-1 ">
			<div id="video-gallery"></div>
		</div>

		<!-- Shopping -->
		<div id="shopping"
			class="col-lg-10 col-lg-offset-1  col-md-10 col-md-offset-1 col-sm-10 col-sm-offset-1 ">
			<div id="aniimated-thumbnials"></div>
		</div>
	</div>

	<br>

	<nav class="navbar navbar-default navbar-fixed-bottom">
	<div class="container-fluid">
		<div class="row">

			<div class="col-lg-12 col-md-12 col-sm-12">
				<ul class="pager">
					<%
						if (pageNo <= 1) {
					%>
					<%
						String link0 = path + "?query=" + query + "&pageNo=" + pageNo + "&location=" + location;
							System.out.println(link0);
					%>
					<li><a href="<%=link0%>"><%=pageNo%></a></li>

					<%
						pageNo++;
							String link1 = path + "?query=" + query + "&pageNo=" + pageNo + "&location=" + location;
							System.out.println(link1);
					%>
					<li><a href="<%=link1%>"><%=pageNo%></a></li>

					<%
						pageNo++;
							String link2 = path + "?query=" + query + "&pageNo=" + pageNo + "&location=" + location;
							System.out.println(link2);
					%>
					<li><a href="<%=link2%>"><%=pageNo%></a></li>

					<%
						pageNo++;
							String link3 = path + "?query=" + query + "&pageNo=" + pageNo + "&location=" + location;
							System.out.println(link3);
					%>
					<li><a href="<%=link3%>"><%=pageNo%></a></li>

					<%
						pageNo++;
							String link4 = path + "?query=" + query + "&pageNo=" + pageNo + "&location=" + location;
							System.out.println(link4);
					%>
					<li><a href="<%=link4%>"><%=pageNo%></a></li>

					<%
						pageNo++;
							String link5 = path + "?query=" + query + "&pageNo=" + pageNo + "&location=" + location;
					%>
					<li><a href="<%=link5%>"><%=pageNo%></a></li>
					<%
						} else {
							// show 1 before
					%>
					<%
						pageNo--;
							String link0 = path + "?query=" + query + "&pageNo=" + pageNo + "&location=" + location;
							//System.out.println(link1);
					%>
					<li><a href="<%=link0%>"><%=pageNo%></a></li>

					<%
						pageNo++;
							String link1 = path + "?query=" + query + "&pageNo=" + pageNo + "&location=" + location;
							//System.out.println(link0);
					%>
					<li><a href="<%=link1%>"><%=pageNo%></a></li>


					<%
						pageNo++;
							String link2 = path + "?query=" + query + "&pageNo=" + pageNo + "&location=" + location;
							//System.out.println(link2);
					%>
					<li><a href="<%=link2%>"><%=pageNo%></a></li>

					<%
						pageNo++;
							String link3 = path + "?query=" + query + "&pageNo=" + pageNo + "&location=" + location;
							//System.out.println(link3);
					%>
					<li><a href="<%=link3%>"><%=pageNo%></a></li>

					<%
						pageNo++;
							String link4 = path + "?query=" + query + "&pageNo=" + pageNo + "&location=" + location;
							//System.out.println(link4);
					%>
					<li><a href="<%=link4%>"><%=pageNo%></a></li>

					<%
						pageNo++;
							String link5 = path + "?query=" + query + "&pageNo=" + pageNo + "&location=" + location;
					%>
					<li><a href="<%=link5%>"><%=pageNo%></a></li>
					<%
						}
					%>
				</ul>
			</div>
		</div>
	</div>


	</nav>

	<input type="hidden" id="ip" value="<%=ip%>" />

</body>
</html>