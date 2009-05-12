
var appletWrapper = {
	addLoadHook : function() {
		var that = this;

		// gör om länken till en js-länk?
	$('#loadAppletLink').click( function() {
		$('#appletHolder').load('applet.php', '', function() {
			that.onAppletLoad.call(that);
		});
		return false; //make sure the links is not loaded
		});
},

onAppletLoad : function() {
	this.addSizeControls();

	var applet = document.fajitaBoy;
	applet.setAttribute("height", "288");
	applet.setAttribute("width", "320");
},

addSizeControls : function() {
	var applet = document.fajitaBoy;
	$("#appletSizeControls").show();
	$("#resizeAppletX2Link").click( function() {
		applet.setAttribute("width", "320");
		applet.setAttribute("height", "288");
	});
	$("#resizeAppletX3Link").click( function() {
		applet.setAttribute("width", "480");
		applet.setAttribute("height", "432");
	});
	$("#resizeAppletX4Link").click( function() {
		applet.setAttribute("width", "640");
		applet.setAttribute("height", "576");
	});
	$("#fullscreenAppletLink").click( function() {
		applet.toggleFullScreen();
	});
}
};

$(document).ready( function() {
	appletWrapper.addLoadHook.call(appletWrapper);
});
