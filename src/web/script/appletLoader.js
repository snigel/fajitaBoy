

var applet = {
 addLoadHook: function() {
    // console.log(this);
    var that = this;
    // gör om länken till en js-länk?
    $('#loadAppletLink').click(function () {
	$('#appletHolder').load('applet.php', '', function () { that.onAppletLoad.call(that); });
	return false; //make sure the links is not loaded
      });
  },

 onAppletLoad: function() {
    this.addSizeControls();
  },

 addSizeControls: function() {
    var applet = document.fajitaBoy;
    $("#appletSizeControls").show();
    $("#enlargeApplet").click(function() {
	// console.log('larger');
      });
    $("#reduceApplet").click(function() {
	// console.log('smaller');
      });
    $("#fullscreenApplet").click(function() {
	applet.toggleFullScreen();
	// console.log('full');
      });
  }
};

$(document).ready(function() {
    applet.addLoadHook.call(applet);
  });
