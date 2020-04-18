
const style = {
  "color": "#ff7800",
  "weight": 5,
  "opacity": 1
};

var map;
var neighborMarkers = [];
var neighborRoutes = [];
var geoRoute = null;

var routeMarker = null;
var selectedRow = null;


// Initialize map
function initializeMap() {
  
  // Initialize map
  map = L.map("map").setView({ lon: 0, lat: 0 }, 0);
  
  // Add tiles
  L.tileLayer("https://tiles.fmi.uni-stuttgart.de/{z}/{x}/{y}.png").addTo(map);
  
  // Show scale bar
  L.control.scale().addTo(map);
  
  // Register click event handler
  map.on("click", onMapClick);
}

// Process user click on map
function onMapClick(e) {
  
  switch (state) {
  
  case STATE_SHOWROUTE:
    
    // Remove route marker
    unselectRow();
    break;
    
  case STATE_SELECTFIRST:
  case STATE_SELECTSECOND:

    // Set location labels
    document.getElementById("selected-latitude").innerHTML = e.latlng.lat;
    document.getElementById("selected-longitude").innerHTML = e.latlng.lng;
    
    // Get nearest neighbor to selected point
    calculateNextNode(e.latlng.lng, e.latlng.lat);
    break;
  }
}

// Process popup open (user click or programmatically)
function onPopupOpen(node, distance) {
  
  // Set location labels
  document.getElementById("next-id").innerHTML = node.id;
  document.getElementById("next-latitude").innerHTML = node.latitude;
  document.getElementById("next-longitude").innerHTML = node.longitude;
  document.getElementById("next-distance").innerHTML = distance;
  
  // Enable buttons
  switch (state) {
  
  case STATE_SELECTFIRST:
    
    document.getElementById("from-button").disabled = false;
    document.getElementById("to-button").disabled = false;
    break;
    
  case STATE_SELECTSECOND:
    
    document.getElementById("start-button").disabled = false;
    break;
  }
  
  selectedNode = node;
}

// Show start point marker on map
function showStart(longitude, latitude) {
  
  // Remove old markers and routes from map
  removeNeighbors();
  
  // Set marker
  var marker = L.marker({lon: longitude, lat: latitude}, { riseOnHover: true, opacity: 0.5 })
    .bindPopup("<p>( " + latitude + " | " + longitude + " )</p>").addTo(map);

  neighborMarkers.push(marker);
  return marker;
}

// Show nearest neighbor on map
function showNeighbor(node, distance, start) {
  
  // Set marker
  var marker = L.marker({ lon: node.longitude, lat: node.latitude }, { riseOnHover: true })
    .bindPopup("<p>Node " + node.id + "</p>")
    .on("popupopen", function() { return onPopupOpen(node, distance); }).addTo(map);

  // Draw route (beeline from start to neighbor) on map
  var geoJson = {
      "type": "LineString",
      "coordinates": [[start.longitude, start.latitude], [node.longitude, node.latitude]]
  };
  
  var route = L.geoJson(geoJson, { style: style }).addTo(map);

  node.start = start;
  node.marker = marker;
  node.route = route;
  
  neighborMarkers.push(marker);
  neighborRoutes.push(route);

  marker.openPopup();
}

// Show route on map
function showRoute(route) {
  
  // Draw route (shortest path) on map
  var geoJson = {
      "type": "LineString",
      "coordinates": route.path
  };
  
  geoRoute = L.geoJson(geoJson, { style: style }).addTo(map);

  // Show/hide ui elements
  document.getElementById("sidebar-select-node").classList.add("hidden");
  document.getElementById("sidebar-show-route").classList.remove("hidden");
  
  document.getElementById("route-title").innerHTML =
    start.id + " -> " + destination.id;
  document.getElementById("route-distance").innerHTML =
    "Distance: " + route.distance;
  
  // Clear route table
  var table = document.getElementById("route-table");
  var rows = table.rows.length;
  for (var i = 1; i < rows; i++)
    table.deleteRow(-1);
  
  // Go back if no route was found
  if (route.distance == -1) {
    alert("No route found");
    back();
    return;
  }

  var callback = function(n, r) {
    return function() { selectRow(n, r); }; };

  // Fill table with nodes on route
  for (var i = 0; i < route.nodes.length; i++) {
    
    var row = table.insertRow(1);
    var id = row.insertCell(0);
    var distance = row.insertCell(1);
    
    var node = route.nodes[i];
    
    id.innerHTML = node.id
    id.onclick = callback(node, row);
    distance.innerHTML = node.distance;
    distance.onclick = callback(node, row);
  }
}

// Process user click on node row in route table
function selectRow(node, row) {
  
  unselectRow();
  
  // Only remove marker if row is selected twice
  if (row == selectedRow) {
    selectedMarker = null;
    selectedRow = null;
    return;
  }
  
  selectedRow = row;
  selectedRow.classList.add("selected");
    
  routeMarker = L.marker({ lon: node.longitude, lat: node.latitude }, { riseOnHover: true })
    .bindPopup("<p>Node " + node.id + "<br>Distance " + node.distance + "</p>").addTo(map);
    
  routeMarker.openPopup();
}

// Unselect row in route table and remove route marker
function unselectRow() {
  
  if (routeMarker != null)
    routeMarker.remove();

  if (selectedRow != null)
    selectedRow.classList.remove("selected");
}

// Remove neighbor markers and routes from map
function removeNeighbors() {
  
  for (var i = 0; i < neighborMarkers.length; i++)
    neighborMarkers[i].remove();
  for (var i = 0; i < neighborRoutes.length; i++)
    neighborRoutes[i].remove();
  
  neighborMarkers = [];
  neighborRoutes = [];
}


// Initialize map
initializeMap();

