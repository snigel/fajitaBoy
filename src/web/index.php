<?php
require "FajitaBoy.php";

$f = new FajitaBoy($_GET['page']);
echo $f->render();

?>
