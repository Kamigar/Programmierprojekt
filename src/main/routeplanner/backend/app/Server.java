package routeplanner.backend.app;

import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import routeplanner.backend.app.App.FatalFailure;
import routeplanner.backend.app.App.Code;
import routeplanner.backend.app.App.Mode;
import routeplanner.backend.app.App.Parameters;

/*
 * Simple HTTP server
 */
public class Server {
  
  // File MIME types
	static final String defaultMimeType = "text/plain";
	static final Map<String, String> mimeTypes = Map.ofEntries(
	  Map.entry("html", "text/html"),
	  Map.entry("js", "text/javascript"),
	  Map.entry("css", "text/css"));
	
	// MIME type of Dijkstra and next node response
	static final String responseMimeType = "text/plain";


	/*
	 * File in the HTML directory
	 */
  public static class HttpFile {
    
    HttpFile(String path, byte[] data) {
      
      _path = path;
      _data = data;

      // Get MIME type by extension

      int p = path.lastIndexOf('.');
      int s = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
      String ext = p <= s ? "" : path.substring(p + 1);

      String type = mimeTypes.get(ext);
      
      _type = type == null ? defaultMimeType : type;
    }

    // Path to serve
    public String path() {
      return _path;
    }

    // Mime type
    public String type() {
      return _type;
    }

    // File content
    public byte[] data() {
      return _data;
    }
    
    private String _path;
    private String _type;
    private byte[] _data;
  };
	
	/*
	 * Handler for Dijkstra and next node calculation
	 */
	class CalculationHandler implements HttpHandler {
		
		@Override
		public void handle(HttpExchange t) throws IOException {
			
			try {
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				
				Parameters param = new Parameters();
				param.isTolerant = false;
				param.requestIn = new BufferedReader(new InputStreamReader(t.getRequestBody()));
				param.requestOut = new BufferedWriter(new OutputStreamWriter(out));
				

				// Parse parameters

				if (t.getRequestURI().getQuery() == null)
					throw new FatalFailure(Code.BAD_REQUEST, "Request query is empty");
				
				String[] options = t.getRequestURI().getQuery().split("&");
				for (String option : options) {
					
					switch (option) {
					
					case "oto":
						
						param.mode = Mode.OTO;
						break;
						
					case "ota":
						
						param.mode = Mode.OTA;
						break;
						
					case "otm":
						
						param.mode = Mode.OTM;
						break;
						
					case "nni":
						
						param.mode = Mode.NNI;
						break;
						
					case "nnf":
						
						param.mode = Mode.NNF;
						break;
						
					case "pl":
						
						param.printLocation = true;
						break;
						
					case "pd":
						
						param.printDistance = true;
						break;
						
					case "pp":
						
						param.printPath = true;
						break;
						
					default:
						
						try {
							// Parse start point for single Dijkstra calculation
							param.start = Integer.parseUnsignedInt(option);

						} catch (NumberFormatException ex) {
							
							throw new FatalFailure(Code.BAD_REQUEST, "Unknown operation");
						}
					}
				}
				
				
				// Run calculation
				
				switch (param.mode) {
				
				case OTO:
					
					_app.runMultipleDijkstra(param, _logger);
					break;
					
				case OTA:
				case OTM:
					
					_app.runSingleDijkstra(param, _logger);
					break;
					
				case NNI:
				case NNF:
					
					_app.runNextNode(param, _logger);
					break;
					
				default:
					
					throw new FatalFailure(Code.BAD_PARAMETER, "No operation provided");
				}
				
				
				// Send HTTP response

				param.requestOut.flush();
				
				t.getResponseHeaders().add("Content-Type", responseMimeType);
				t.sendResponseHeaders(200, out.size());

				out.writeTo(t.getResponseBody());

				t.getResponseBody().close();

			} catch (FatalFailure ex) {
				
				byte[] msg = ("Error in request\n\n" + ex.getMessage() + "\n\n").getBytes();

				t.sendResponseHeaders(400, msg.length);
				t.getResponseBody().write(msg);
				t.getResponseBody().close();
			}
		}
	}
	
	/*
	 * Handler for file serving
	 */
	class HtmlHandler implements HttpHandler {
		
		@Override
		public void handle(HttpExchange t) throws IOException {
			
			// Check if request should be redirected
			
			String redirection = _redirections.get(t.getRequestURI().getPath());
			
			if (redirection != null) {
				
				t.getResponseHeaders().add("Location", redirection);
				t.sendResponseHeaders(302, 0);
				t.getResponseBody().close();

			} else {

				// Check if file could be served
				
				HttpFile file = _html.get(t.getRequestURI().getPath());
				
				if (file != null) {
				
				  t.getResponseHeaders().add("Content-Type", file.type());
					t.sendResponseHeaders(200, file.data().length);
					t.getResponseBody().write(file.data());
					t.getResponseBody().close();

				} else {
					
					byte[] msg = ("File not found").getBytes();
					
					t.sendResponseHeaders(404, msg.length);
					t.getResponseBody().write(msg);
					t.getResponseBody().close();
				}	
			}
		}
	}
	
	
	// Start the server instance
	public void start(int port, int backlog, String routeplannerPath, HashMap<String, HttpFile> html, HashMap<String, String> redirections, App app, Logger logger) throws IOException {
		
		logger.info("Broadcasting files:");
		for (String path : html.keySet())
			logger.info(path);

		_html = html;
		_redirections = redirections;
		_app = app;
		_logger = logger;
		
		_server = HttpServer.create(new InetSocketAddress(port), backlog);
		_server.createContext("/", new HtmlHandler());
		_server.createContext(routeplannerPath, new CalculationHandler());
		_server.setExecutor(null);
		_server.start();
	}
	
	// Stop the server instance
	public void stop(int timeout) {
		
		_server.stop(timeout);
	}


	// The server object
	private HttpServer _server;
	
	// Files to serve (URI-path -> data)
	private HashMap<String, HttpFile> _html;
	
	// Redirections (URI-path -> URI-path)
	private HashMap<String, String> _redirections;
	
	// Program implementation
	private App _app;

	// Logger
	private Logger _logger;
}
