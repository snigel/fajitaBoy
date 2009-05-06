<?php

function fbHeader($pages, $activePage) {
?>
  
  <div id="header">
    <div id="logo"><h1>Fajita Boy</h1></div>
    <div id="nav"><?php links($pages, $activePage); ?></div>
  </div>

<?php
}

function links($pages, $activePage) {
  // $lis = array();
  echo '<ul id="navlinks">';
  foreach($pages as $p) {
    
    if ($p['path'] == $activePage['path']) {
      $class = ' class="active"';
    } else {
      $class = '';
    }
    ?>
    <li><a <?=$class?> href="<?=$p['link']?>"><?=$p['name']?></a>
    <?php
  }
  echo '</ul>';

}
?>