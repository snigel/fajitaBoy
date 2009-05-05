require "../page/Page.php"

abstract class Overlay extends Page {
  abstract function renderOverlay();
  abstract function render();
}