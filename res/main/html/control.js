
const STATE_SELECTFIRST = 0;
const STATE_SELECTSECOND = 1;
const STATE_SHOWROUTE = 2;

var state = STATE_SELECTFIRST;

var start = null;
var destination = null;


// Catch back button events
window.onload = function() {
  
  if (typeof history.pushState == "function") {
    history.pushState("", null, null);
    window.onpopstate = function() {
      history.pushState("", null, null);
      back();
    };
  } else {
    var ignore = true;
    window.onhashchange = function() {
      if (!ignore) {
        ignore = true;
        window.location.hash = Math.random();
      } else {
        ignore = false;
      }
    };
  }
};

// Navigate back
function back() {

  switch (state) {
  
  case STATE_SELECTSECOND:
    
    if (start != null) {
      start.marker.remove();
      start.start.marker.remove();
      start.route.remove();
    }
    if (destination != null) {
      destination.marker.remove();
      destination.start.marker.remove();
      destination.route.remove();
    }
    
    document.getElementById("left-button").disabled = true;
    document.getElementById("left-button").innerHTML = "From";
    document.getElementById("right-button").disabled = true;
    document.getElementById("right-button").innerHTML = "To";
    
    document.getElementById("other-node").classList.add("hidden");
    
    clearSelectedNode();
    
    start = null;
    destination = null;
    
    state = STATE_SELECTFIRST;
    break;
    
  case STATE_SHOWROUTE:
    
    unselectRow();
    
    // Remove route from map
    geoRoute.remove();
    geoRoute = null;
    
    // Show/hide ui elements
    document.getElementById("sidebar-select-node").classList.remove("hidden");
    document.getElementById("sidebar-show-route").classList.add("hidden");
    document.getElementById("right-button").classList.remove("hidden");
    
    // Clear start/destination
    if (start == selectedNode)
      start = null;
    else
      destination = null;
    
    state = STATE_SELECTSECOND;
    break;
  }
}

// Process user click on left button
function leftButtonClicked() {
  
  switch (state) {
  
  case STATE_SELECTFIRST:

    // Left is 'From' button
    start = selectedNode;
    document.getElementById("other-title").innerHTML = "Starting point";
    selectSecond();
    break;

  default:

    // Left is 'Back' button
    back();
    break;
  }
}

// Process user click on right button
function rightButtonClicked() {
  
  switch (state) {
  
  case STATE_SELECTFIRST:

    // Right is 'To' button
    destination = selectedNode;
    document.getElementById("other-title").innerHTML = "Destination";
    selectSecond();
    break;
    
  case STATE_SELECTSECOND:

    // Right is 'Start' button
    if (start == null)
      start = selectedNode;
    if (destination == null)
      destination = selectedNode;
    
    document.getElementById("right-button").classList.add("hidden");
    
    calculateDijkstra();
    break;
  }
}

// Switch to selection of second node
function selectSecond() {
  
  state = STATE_SELECTSECOND;
  
  // Disable popup of selected node
  selectedNode.marker.off("popupopen");
  
  // Remove markers and route from neighbor storage
  neighborMarkers.splice(neighborMarkers.indexOf(selectedNode.marker));
  neighborMarkers.splice(neighborMarkers.indexOf(selectedNode.start.marker));
  neighborRoutes.splice(neighborRoutes.indexOf(selectedNode.route));
  
  // Set location labels
  document.getElementById("other-id").innerHTML = selectedNode.id;
  document.getElementById("other-latitude").innerHTML = selectedNode.latitude;
  document.getElementById("other-longitude").innerHTML = selectedNode.longitude;
  
  // Enable/disable ui elements
  document.getElementById("right-button").disabled = true;
  document.getElementById("right-button").innerHTML = "Start";
  document.getElementById("left-button").innerHTML = "Back";
  document.getElementById("other-node").classList.remove("hidden");
  
  clearSelectedNode();
}

// Reset selected node ui elements
function clearSelectedNode() {
  
  removeNeighbors();

  document.getElementById("selected-latitude").innerHTML = "-";
  document.getElementById("selected-longitude").innerHTML = "-";
  document.getElementById("next-id").innerHTML = "-";
  document.getElementById("next-latitude").innerHTML = "-";
  document.getElementById("next-longitude").innerHTML = "-";
  document.getElementById("next-distance").innerHTML = "-";
}
