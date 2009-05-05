<?php
class Header {

  private $pages;
  private $active;
  
  function __construct($pages, $active) {
    $this->pages = $pages;
    $this->active = $active;
  }
  
  function render() {
    return "header";
  }
}
?>