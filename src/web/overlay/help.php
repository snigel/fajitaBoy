<?php

class Help extends Overlay {
  function renderOverlay() {
    return "overlay:" . render();
  }

  function render() {
    return "help";
  }
}

/*
array(path => 'overlay/help.php',
      descr => 'Get help',
      name => 'Help')
*/

?>