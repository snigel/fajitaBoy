var applet = {
 addLoadHook: function() {
    console.log(this);
    // gör om länken till en js-länk?
    $('#loadAppletLink').click(function () {
	$('#appletHolder').load('applet.php', '', function () { this.onAppletLoad.call(this); });
	return false; //make sure the links is not loaded
      });
  },

 onAppletLoad: function() {
    this.addSizeControls();
  },

 addSizeControls: function() {
    $("#appletSizeControls").show();
    $("#enlargeApplet").click(function() {
	console.log('larger');
      });
    $("#reduceApplet").click(function() {
	console.log('smaller');
      });
    $("#fullscreenApplet").click(function() {
	console.log('full');
      });
  }
};

$(document).ready(function() {
    applet.addLoadHook.call(applet);
  });
