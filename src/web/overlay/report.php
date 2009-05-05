<?php

class Report extends Overlay {
  function renderOverlay() {
    return "overlay:" . render();
  }

  function render() {
    return "Report";
  }
}

/*
  array(path => 'overlay/error.php',
  descr => 'Report an error',
  name => 'Error report')
*/
				       

?>