<?php

class Faq extends Overlay {
  function renderOverlay() {
    return "overlay:" . render();
  }

  function render() {
    return "faq";
  }
}

?>