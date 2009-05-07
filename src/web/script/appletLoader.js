$(document).ready(function() {
    $('#loadAppletLink').click(function () {
	$('#appletHolder').load("simple.html");
	//make sure the links is not loaded
	return false; 
      });
  });

