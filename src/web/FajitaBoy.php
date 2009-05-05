<?php

// require "Renderable.php";
require "header.php";
require "footer.php";

class FajitaBoy {
  private $header;
  private $footer;
  
  private $pages = array(array('page_re' => "/index|fajitaboy|index\.php/",
			       'path' => 'page/index.php',
			       'desc' => 'Play Fajita Boy',
			       'link' => '/fajitaboy'),
			 array('page_re' => '/about/',
			       'path' => 'page/about.php',
			       'desc' => 'About Fajita Boy',
			       'link' => '/about'),
			 array('page_re' => '/contact/',
			       'path' => 'page/contact.php',
			       'desc' => 'Contact the authors',
			       'link' => '/contact')
			 );
  
  private $activePage;
  
  public function __construct($page) {
    $this->initPages($page);
  }

  private function initPages($page) {
    foreach ($this->pages as $p) {
      if (preg_match($p['page_re'], $page)) {
	$activePage = $p;
      }
    }
  }

  private function pageNotFound() {
    header("HTTP/1.0 404 Not Found");
  }
  
  private function getTitle() {
    return "Fajita Boy";
  }

  public function render() {
    if (isset($activePage)) {
      $this->pageNotFound();
    } else {
      $header = new Header($pages, $activePage);
      $footer = new Footer();
      
    ?>

    <html>
  <head>
      <title><?=$this->getTitle()?></title>
    <link rel="icon" type="image/png" href="image/favicon.png">
  </head>
  <body>
      <?php

      $header->render();
      printr( $activePage['path'] );
      // include $activePage['path'];
      $footer->render();

    ?>
  </body>
</html>

<?php
    }
  }
}



  /*
  $overlays = array('report' => 'overlay/report.php',
		    'help'  => 'overlay/help.php'
		    );

  */


?>
