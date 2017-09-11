// EBAY
/* XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */
function ebaySearch(userQuery) {
	$.ajax({
		url : "http://svcs.ebay.com/services/search/FindingService/v1",
		data : {
			"OPERATION-NAME" : "findItemsByKeywords",
			"SERVICE-VERSION" : "1.0.0",
			"SECURITY-APPNAME" : "manishar-mani455c-PRD-3090738eb-3c225b52",
			"GLOBAL-ID" : "EBAY-US",
			"keywords" : userQuery,
			"paginationInput.entriesPerPage" : 10,
			"RESPONSE-DATA-FORMAT" : "JSON"
		// ,"pageNumber" :
		},
		dataType : 'jsonp',
		success : function(x) {
			ebayDisplay3(x);
		}
	});
}

function ebayDisplay(x) {
	// alert("success");
	var items = x.findItemsByKeywordsResponse[0].searchResult[0].item || [];
	var html = [];
	html
			.push('<table width="100%" border="0" cellspacing="0" cellpadding="3"><tbody>');
	for (var i = 0; i < items.length; ++i) {
		var item = items[i];
		var title = item.title;
		var pic = item.galleryURL;
		var viewitem = item.viewItemURL;
		if (null != title && null != viewitem) {
			html.push("<tr>");
			html.push('<td>' + '<img src="' + pic + '" border="0">'
					+ '</td>' + '<td><a href="' + viewitem
					+ '" target="_blank">' + title + '</a></td>');
			html.push('<td>' + title + '</td>');
			
			html.push("</tr>");
		}
	}
	html.push('</tbody></table>');
	document.getElementById("shopping").innerHTML = html.join("");
}

function ebayDisplay2(x) {
	// alert("success");
	var items = x.findItemsByKeywordsResponse[0].searchResult[0].item || [];
	var html = [];
	for (var i = 0; i < items.length; ++i) {
		var item = items[i];
		var title = item.title;
		var pic = item.galleryURL;
		var viewitem = item.viewItemURL;
		if (null != title && null != viewitem) {
			html.push("<a target =\"_blank\" href =\"" + viewitem + "\">");
			html
					.push('<img width = \"240\" height = \"240\" style=\"padding-left:10px;padding-right:10px;padding-top:10px;padding-bottom:10px;\" src="'
							+ pic + '\" />');
			html.push("</a>");
		}
	}
	document.getElementById("shopping").innerHTML = html.join("");
	$('#aniimated-thumbnials').lightGallery({
		thumbnail : true
	});
	$('#aniimated-thumbnials').lightGallery({
		thumbnail : true,
		animateThumb : false,
		showThumbByDefault : false
	});
}

function ebayDisplay3(x) {
	// alert("success");
	var items = x.findItemsByKeywordsResponse[0].searchResult[0].item || [];
	var html = [];
	html.push("<div class=\"row\">");
	
	for (var i = 0; i < items.length; ++i) {
		var item = items[i];
		var title = item.title;
		var pic = item.galleryURL;
		var viewitem = item.viewItemURL;
		if (null != title && null != viewitem) {
			 html.push("<div class=\"col-xs-12 col-sm-12 col-md-4 col-lg-4\">")
			 
			 html.push("<a class=\"text-justify\"href=\"" + viewitem + "\" target=\"_blank\">");
			 html.push('<img width="100%" src="' + pic + '" border="0">' + title + '</a>');
			 html.push("</div>");			
		}
	}
	html.push('</div>');
	document.getElementById("shopping").innerHTML = html.join("");
}

/* XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */

/* XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */
// Youtube API
function youtubeSearch(userQuery) {
	$.ajax({
		url : "https://www.googleapis.com/youtube/v3/search",
		type : 'GET',
		data : {
			q : userQuery,
			key : "AIzaSyAaFqn_SCdszZ2JbH91nQ-NHyskp21OXj0",
			part : "snippet",
			order : "relevance",
			type : "video",
			maxResults : 50
		},
		dataType : "jsonp",
		success : function(data) {
			youtubeDisplay2(data);
		}

	});
}

function youtubeDisplay(data) {
	var html = [];
	var entries = data.items || [];
	for (var i = 0; i < entries.length; i++) {
		html.push("<article class=\"video\">");

		var entry = entries[i];
		var snippet = entry.snippet;
		var title = snippet['title'];
		var url = "https://www.youtube.com/embed/" + entry.id['videoId'];
		var imageThumbnailSrc = snippet.thumbnails.medium["url"];
		html.push("<figure>");
		html
				.push("<a class=\"fancybox fancybox.iframe\" href=\"" + url
						+ "\">");
		html.push(" <img class=\"videoThumb\" src=\"" + imageThumbnailSrc
				+ "\"></a>");
		html.push("</figure>");
		html.push(" <h4 class=\"videoTitle\">");
		html.push(title);
		html.push("</h4>");
		html.push("</article>");
	}

	document.getElementById('videos').innerHTML = html.join('');

}

function youtubeDisplay2(data) {
	var html = [];
	var entries = data.items || [];
	for (var i = 0; i < entries.length; i++) {
		var entry = entries[i];
		var snippet = entry.snippet;
		var title = snippet['title'];
		var url = "https://www.youtube.com/embed/" + entry.id['videoId'];
		var imageThumbnailSrc = snippet.thumbnails.medium["url"];
		html.push("<a data-sub-html=\""+title+"\" href=\"" + url + "\">");
			html
					.push("<img width=\"320\" style=\"padding-left:10px;padding-right:10px;padding-top:10px;padding-bottom:10px;\" src = \""
							+ imageThumbnailSrc + "\">");
		html.push("</a>");
		
	}

	document.getElementById('video-gallery').innerHTML = html.join('');

	$('#video-gallery').lightGallery();
	$('#video-player-param').lightGallery({
		youtubePlayerParams : {
			modestbranding : 1,
			showinfo : 0,
			rel : 0,
			controls : 0
		},
		vimeoPlayerParams : {
			byline : 0,
			portrait : 0,
			color : 'A90707'
		}
	});
	$('#video-thumbnails').lightGallery({
		loadYoutubeThumbnail : true,
		youtubeThumbSize : 'default',
		loadVimeoThumbnail : true,
		vimeoThumbSize : 'thumbnail_medium',
	});

}

function youtubeDisplay3(data) {
	var html = [];
	var entries = data.items || [];
	for (var i = 0; i < entries.length; i++) {
		var entry = entries[i];
		var snippet = entry.snippet;
		var title = snippet['title'];
		var url = "https://www.youtube.com/embed/" + entry.id['videoId'];
		var imageThumbnailSrc = snippet.thumbnails.medium["url"];
			html.push("<a data-sub-html=\""+title+"\" href=\"" + url + "\">");
			html
					.push("<img width=\"320\" style=\"padding-left:10px;padding-right:10px;padding-top:10px;padding-bottom:10px;\" src = \""
							+ imageThumbnailSrc + "\">");
			
			html.push("<p style=\"width:25%;\">");
			html.push(title);
			html.push("</p>");
		html.push("</a>");
		
		
	}

	document.getElementById('video-gallery').innerHTML = html.join('');

	$('#video-gallery').lightGallery();
	$("#relative-caption").lightGallery({
		subHtmlSelectorRelative: true
	});
	
	
	$('#video-player-param').lightGallery({
		youtubePlayerParams : {
			modestbranding : 1,
			showinfo : 0,
			rel : 0,
			controls : 0
		},
		vimeoPlayerParams : {
			byline : 0,
			portrait : 0,
			color : 'A90707'
		}
	});
	$('#video-thumbnails').lightGallery({
		loadYoutubeThumbnail : true,
		youtubeThumbSize : 'default',
		loadVimeoThumbnail : true,
		vimeoThumbSize : 'thumbnail_medium',
	});

}

/* XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */

/* XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */
// Weather call
function weatherSearch(location) {
	$.ajax({
		url : "http://api.openweathermap.org/data/2.5/weather",
		type : "GET",
		data : {
			q : location,
			APPID : "70141c48edf0cd7d4f8365bed865303e"
		},
		dataType : "jsonp",
		success : function(data) {
			weatherDisplay(data);
		}

	});
}

function weatherDisplay(data) {
	var html = [];
	var locationName = data.name;

	var humidity = data.main["humidity"];
	var temp = data.main["temp"];
	html.push("<strong><p style = \"font-size:12px\" class=\"navbar-text\">");
	html.push(data.name + "<br> ");
	html.push("Temp: " + temp + " ");
	html.push("Humidity: " + humidity + " ");
	html.push("</p></strong>");

	console.log(html);
	document.getElementById('climate').innerHTML = html.join('');

}
/* XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */
function iplocation(ip) {
	$.ajax({
		url : "http://ip-api.com/json/" + ip,
		type : "GET",
		data : {

		},
		dataType : "jsonp",
		success : function(data) {
			locationDisplay(data);
		}

	});

}
function locationDisplay(data) {
	var status = data["status"];
	var city = "Philadelphia";
	if (status == "success") {
		city = data["city"];
	}
	console.log(city);
	$('input[name="location"]:hidden').val(city);
	weatherSearch(city);
}
/* XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */
function wikipediaSearch(query) {
	$.ajax({
		url : "http://en.wikipedia.org/w/api.php",
		type : "GET",
		data : { // how to get images in wikipedia
			action : 'query',
			format : 'json',
			prop : 'images',
			prop : 'extracts',
			list : 'search',
			srsearch : query,
			srlimit : 1,
		},
		dataType : "jsonp",
		success : function(data) {
			wikipediaDisplay(data);
		}

	});
}

function wikipediaDisplay(data) {
	console.log(data);
	var html = [];
	var entry = data.query.search[0];
	var snippet = entry['snippet'];
	var title = entry['title'];
	
	html.push("<h4 style = \"margin-top:8px; color:#337ab7;\" class=\"media-heading\">");
	html.push(title);
	html.push("</h4>");
	
	html.push("<p style=\"color: #3c763d\"class=\"text-justify\" class=\"bg-info\">");
	html.push(snippet);
	html.push("</p>");
	html.push("<h6 style=\"color: #3c763d\"><strong> Source: Wikipedia</strong></h6>");
	
	document.getElementById('wikiGraph').innerHTML = html.join('');
	console.log(snippet);
	console.log(title);
}
/* XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */
function knowledgeSearchGraph(query){
	$.ajax({
		url : "https://kgsearch.googleapis.com/v1/entities:search",
		type : "GET",
		data : { // how to get images in wikipedia
			query : query,
			key : 'AIzaSyCThxjS5cPvhl5LDIGIqhpw6aJXQZ3YeAE',
			limit : 1,
			indent : 'true',
		},
		dataType : "jsonp",
		success : function(data) {
			googleKnowledgeSearchDisplay(data);
		}
	});
}

function googleKnowledgeSearchDisplay(data){
	console.log(data);
	var html = [];
	var entry = data.itemListElement[0].result;
	var name = entry["name"];
	console.log("Name" + name);
	var description = entry["description"];
	console.log("Description" + description);
	var image = entry.image;
	var imageUrl = image["contentUrl"];
	console.log("img url" + imageUrl);
	var detailedDescription = entry.detailedDescription;
	
	var body = detailedDescription["articleBody"];
	console.log("body Descp" + body);
	console.log(entry);
	console.log(image);
	console.log(detailedDescription);
	
	html.push("<h4 style = \"margin-top:8px;color:#337ab7;\" class=\"media-heading\">");
	html.push(name);
	html.push("</h4>");
	html.push("<img style=\"padding-right:5px;\"src=\""+imageUrl+"\" height= \"75\" width=\"75\" align=\"left\"/>");
	html.push("<p style=\"color: #3c763d;\">");
	html.push(body);
	html.push("</p>");
	html.push("<h6 style=\"color: #3c763d\"><strong> Source: Google Knowledge Graph</strong></h6>");
	
	document.getElementById('googleGraph').innerHTML = html.join('');
	
}
/* XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */
function vote(requestString){
	$.ajax({
		url: requestString,
		type : "GET",
		success: function(data){
			console.log("Voting received");
		}
	});
}
/* XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX */

// Important Jquery function which loads with page and calls other AJAX calls
$(document).ready(function() {
	$("#ip").hide();
	var web = $("#web");
	var graph = $('#graph');
	var pdf = $("#pdf");
	var images = $("#images");
	var videos = $("#videos");
	var shopping = $("#shopping");
	var climate = $("#climate");
	var userQuery = $(searchBox).val();
	var submit = $("#submit");
	var searchBox = $("#searchBox");

	// console.log(userQuery + "userQuery");
	web.show();
	web.addClass('active');
	pdf.hide();
	images.hide();
	videos.hide();
	shopping.hide();

	var ip = $("#ip").val();
	console.log(ip);
	iplocation(ip);
	
	var location = $('input[name="location"]:hidden').val();
	weatherSearch(location);
	
	$("li a").click(function() {
		web.hide();
		graph.hide();
		pdf.hide();
		images.hide();
		videos.hide();
		shopping.hide();
		
		$("li").removeClass('active');
		$(this).parent().addClass('active');
		
		var t = $(this).attr("href");
		display_id = $(t);
		display_id.fadeIn();
		if(t=='#web')
			$(graph).fadeIn();
	});
	
	$(".voting").click(function(){
		$(this).attr('disabled', true);
		var name = $(this).attr('name');
		var value = $(this).attr('value');
		var requestString = "/FurySearch/reinforced?vote="+name+"&url="+value;
		vote(requestString);
		
	});
	
	// Ebay API Trigger
	$(searchBox).change(function() {
		userQuery = $(searchBox).val();
		// Ebay Api call
		ebaySearch(userQuery);
		// Youtube Api search
		youtubeSearch(userQuery);
	});
	
	// Ebay API function call done

	userQuery = $(searchBox).val();
	if (userQuery != "" || userQuery != "undefined") {
		wikipediaSearch(userQuery);
		knowledgeSearchGraph(userQuery);
		// ebay api call
		ebaySearch(userQuery);
		// youtube api call
		youtubeSearch(userQuery);
	}
});