
// Calculate nearest neighbor
function calculateNextNode(longitude, latitude) {
  
  showNextNodeLoadingScreen();
  
  var marker = showStart(longitude, latitude);
  
  // Send HTTP request to server
  var request = new XMLHttpRequest();
  
  request.onload = function() {

    if (request.status == 200) {

      onLoadNextNode(request.response,
        { latitude: latitude, longitude: longitude, marker: marker });

    } else {

      alert("Bad reply from server: " + request.response);
      location.reload();
    }
  }
  
  request.ontimeout = function() {
    
    alert("A timeout occured... Please try again");
    location.reload();
  };

  request.timeout = 10000;
  request.open("POST", "routeplanner?nnf&pl");
  request.send(latitude + " " + longitude);
}

// Calculate shortest route (Dijkstra)
function calculateDijkstra() {
  
  showDijkstraLoadingScreen();
  state = STATE_SHOWROUTE;

  // Prepare request
  var text = start.id + " " + destination.id;
  
  // Send HTTP request to server
  var request = new XMLHttpRequest();
  
  request.onload = function() {

    if (request.status == 200) {

      onLoadDijkstra(request.response);

    } else {

      alert("Bad reply from server: " + request.response);
      location.reload();
    }
  }

  request.ontimeout = function() {
    
    alert("A timeout occured... Please try again");
    location.reload();
  };

  request.timeout = 30000;
  request.open("POST", "routeplanner?oto&pl&pd&pp");
  request.send(text);
}

// Process result of nearest neighbor calculation
function onLoadNextNode(response, start) {
  
  var result = parseNextNodes(response);
  
  for (var i = 0; i < result.nodes.length; i++)
    showNeighbor(result.nodes[i], result.distance, start);
  
  hideLoadingScreen();
}

// Process result of Dijkstra calculation
function onLoadDijkstra(response) {
  
  var route = parseDijkstra(response);
  
  showRoute(route);
  
  hideLoadingScreen();
}

// Parse nearest neighbor response message
function parseNextNodes(string) {
  
  var values = string.split(" ");

  var result = {};
  result.distance = parseFloat(values[0]);
  result.nodes = [];

  for (var i = 1; i < values.length; i += 3) {
    
    var node = {};
    node.id = parseInt(values[i]);
    node.latitude = parseFloat(values[i + 1]);
    node.longitude = parseFloat(values[i + 2]);
    
    result.nodes.push(node);
  }
  return result;
}

// Parse Dijkstra response message
function parseDijkstra(string) {
  
  var values = string.split(" ");
  
  var result = {};
  result.distance = parseInt(values[0]);
  result.nodes = [];
  result.path = [];
  
  for (var i = 1; i < values.length; i += 4) {
    
    var node = {};
    node.id = parseInt(values[i]);
    node.latitude = parseFloat(values[i + 1]);
    node.longitude = parseFloat(values[i + 2]);
    node.distance = parseFloat(values[i + 3]);
    
    result.nodes.push(node);
    result.path.push([ node.longitude, node.latitude ]);
  }
  return result;
}

// Show next node loading screen
function showNextNodeLoadingScreen() {
  
  var screen = document.getElementById("loading-screen");
  
  if (screen.classList.contains("dijkstra"))
    screen.classList.remove("dijkstra");
  
  screen.classList.add("nextnode");
  screen.classList.remove("hidden");
}

// Show Dijkstra loading screen
function showDijkstraLoadingScreen() {
  
  var screen = document.getElementById("loading-screen");
  
  if (screen.classList.contains("nextnode"))
    screen.classList.remove("nextnode");
  
  screen.classList.add("dijkstra");
  screen.classList.remove("hidden");
}

// Hide loading screen
function hideLoadingScreen() {
  
  document.getElementById("loading-screen").classList.add("hidden");
}
