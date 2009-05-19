<?php

require 'header.php';
require 'footer.php';

$pages = array(array('page_re' => "/(^$)|index|fajitaboy|index\.php/",
		     'name' => 'Fajita Boy',
		     'path' => 'page/fajitaboy.php',
		     'desc' => 'Play Fajita Boy',
		     'link' => 'index'),
	       array('page_re' => '/about\/?/',
		     'name' => 'About',
		     'path' => 'page/about.php',
		     'desc' => 'About Fajita Boy',
		     'link' => 'about'),
	       array('page_re' => '/contact/',
		     'name' => 'Contact',
		     'path' => 'page/contact.php',
		     'desc' => 'Contact the authors',
		     'link' => 'contact')
	       );


if (isset($_GET['page'])) {
  $page = $_GET['page'];
} else if (isset($argv[2]))  {
  $page = $argv[2];
} else {
  $page = "";
}

fbRender($pages, $page);

/*
function fbLog($msg) {
  if (defined(STDERR)) {
    fwrite(STDERR, $msg);
  } else {
    echo $msg;
  }
}
*/


function fbPageNotFound($req) {
  // <fbLog('front controller could not find page: "' . $req . "\"\n");
  header("HTTP/1.0 404 Not Found");
}

function fbGetPage($pages, $req) {
  foreach ($pages as $p) {
    if (preg_match($p['page_re'], $req)) {
      $activePage = $p;
    }
  }
  return $activePage;
}

function fbRender($pages, $req) {
  // fbLog("> render\n");
  $activePage = fbGetPage($pages, $req);
  if (isset($activePage)) {
    renderHolder($pages, $activePage);
  } else {
    fbPageNotFound($req);
  }
}

function pageTitle($pages, $activePage) {
  $subtitle = "";
  if ($activePage['path'] != 'page/fajitaboy.php') {
    $subtitle = $activePage['name'] . " - ";
  }
  return $subtitle . "Fajita Boy";
}

function renderHolder($pages, $activePage) {
  include $activePage['path'];


  include "doctype.php";
  ?>
  
  <html>
    <head profile="http://www.w3.org/2005/10/profile">
    <link rel="stylesheet" type="text/css" href="style/fajitaboy.css" >
    <link rel="shortcut icon" type="image/png" href="image/favicon.png" >
    <script type="text/javascript" src="script/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="script/fajitaboy.js"></script>
    <title>
    <?= pageTitle($pages, $activePage); ?>
  </title>
	</head>
	<body>
      <?php				       

      fbHeader($pages, $activePage);

  ?>
  
  <div id="content">
     <?php fbRenderPage(); ?>		       
  </div>
     
  <?php fbFooter($pages, $activePage); ?>
  <script type="text/javascript" src="script/ga.js"></script>     
  <?php include "include/ga.html"; ?>
     </body>
     </html>
	
<?
}
?>
