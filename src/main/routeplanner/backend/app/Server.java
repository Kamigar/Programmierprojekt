package routeplanner.backend.app;

import java.util.HashMap;
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
	
	/*
	 * Handler for Dijkstra and next node calculation
	 */
	class CalculationHandler implements HttpHandler {
		
		@Override
		public void handle(HttpExchange t) throws IOException {
			
			try {
				
				String query = t.getRequestURI().getQuery();

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				
				Parameters param = new Parameters();
				param.isTolerant = false;
				param.requestIn = new BufferedReader(new InputStreamReader(t.getRequestBody()));
				param.requestOut = new BufferedWriter(new OutputStreamWriter(out));
				
				if (query.startsWith("oto")) {
					
					param.mode = Mode.OTO;
					_app.runMultipleDijkstra(param, _logger);

				} else if (query.startsWith("nni")) {
					
					param.mode = Mode.NNI;
					_app.runNextNode(param, _logger);

				} else if (query.startsWith("nnf")) {
					
					param.mode = Mode.NNF;
					_app.runNextNode(param, _logger);

				} else {

					throw new FatalFailure(Code.BAD_REQUEST, "Unknown operation");
				}
				
				param.requestOut.flush();

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
			
			byte[] data = _html.get(t.getRequestURI().getPath());
			
			if (data != null) {
			
				t.sendResponseHeaders(200, data.length);
				t.getResponseBody().write(data);
				t.getResponseBody().close();

			} else {
				
				byte[] msg = ("File not found").getBytes();
				
				t.sendResponseHeaders(404, msg.length);
				t.getResponseBody().write(msg);
				t.getResponseBody().close();
			}
		}
	}
	
	
	// Start the server instance
	public void start(int port, int backlog, HashMap<String, byte[]> html, App app, Logger logger) throws IOException {
		
		logger.info("Broadcasting files:");
		for (String path : html.keySet())
			logger.info(path);

		_html = html;
		_logger = logger;
		_app = app;
		
		_server = HttpServer.create(new InetSocketAddress(port), backlog);
		_server.createContext("/", new HtmlHandler());
		_server.createContext("/routeplanner", new CalculationHandler());
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
	private HashMap<String, byte[]> _html;
	
	// Program implementation
	private App _app;

	// Logger
	private Logger _logger;
}
