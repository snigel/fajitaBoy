function saveCookie() {
	var data = document.fajitaBoy._JS_getCookie();
	var exdate = new Date();
	exdate.setDate(exdate.getDate()+30);

	data = data + ";expires=" + exdate.toGMTString();
						
	document.cookie= data;
}

function loadCookie() {
	document.fajitaBoy._JS_setCookie(unescape(document.cookie));
}