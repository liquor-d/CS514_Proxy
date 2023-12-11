import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
public class HTTPRequest {
    private static final Logger logger = Logger.getLogger(HTTPRequest.class.getName());
    private int threadId;
    private String rawData;
    private String method;
    private String host;
    private String urlString;
    private int port;
    private String postBody;
    private String startLine;
    private boolean isBlocked;

    private Map<String, String> headers;

    public HTTPRequest(InputStream inputStream, int threadId) throws IOException{
        isBlocked = false;
        this.threadId = threadId;
        this.headers = new HashMap<>();
        logger.log(Level.INFO, "Begin parsing HTTPRequest in thread: {0}", this.threadId);

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder requestBuilder = new StringBuilder();
        String inputLine;
        boolean hasContentLength = false;
        int contentLength = 0;
        boolean firstLine = true;
        while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {
            // For debugging the parser
            if(firstLine){
                firstLine = false;
                this.startLine = inputLine;
                logger.log(Level.INFO, "The first line for the HttpRequest in thread {0}: " + inputLine, threadId);
            }else{
                // load the headers
                int colonIndex = inputLine.indexOf(":");
                if (colonIndex != -1) {
                    String headerName = inputLine.substring(0, colonIndex).trim();
                    String headerValue = inputLine.substring(colonIndex + 1).trim();
                    headers.put(headerName, headerValue);
                }
            }
            requestBuilder.append(inputLine).append("\n");
            if (inputLine.startsWith("Content-Length: ")) {
                hasContentLength = true;
                contentLength = Integer.parseInt(inputLine.split(" ")[1]);
            }
        }
        if(requestBuilder.isEmpty()){
            throw new IOException("Fail to read any data for the HTTP Request in thread " + threadId
                    + ", maybe the sender doesn't send any");
        }
        // Handling scenarios for POST (and other methods with a body)
        if (hasContentLength) {
            requestBuilder.append("\n");
            char[] buffer = new char[contentLength];
            int bytesRead = in.read(buffer, 0, contentLength);

            if (bytesRead != contentLength) {
                logger.log(Level.WARNING, "Content length mismatch in thread {0}", this.threadId);
            }
            requestBuilder.append(buffer, 0, bytesRead);
        }


        this.rawData = requestBuilder.toString();
        logger.log(Level.INFO, "Request raw data is : \n{0}", rawData);


        this.method = null;
        this.host = null;
        this.port = -1;
        parseMethod();
        parseHostAndPort();
    }

    private void parseMethod() {
        if (rawData == null || rawData.isEmpty()) {
            logger.log(Level.WARNING, "Empty or null rawData in HTTPRequest in thread {0}", this.threadId);
            // Handle the empty or null rawData case, e.g., set method to null or throw an exception
            return;
        }
        int methodPos = rawData.indexOf(" ");
        if (methodPos > 0) {
            String potentialMethod = rawData.substring(0, methodPos);
            // Validate that potentialMethod is a valid HTTP method
            if (isValidHttpMethod(potentialMethod)) {
                this.method = potentialMethod;
            } else {
                logger.log(Level.WARNING, "Invalid HTTP method received: " + potentialMethod);
                // Handle the invalid method case, e.g., set to a default or throw an exception
            }
        } else {
            logger.log(Level.WARNING, "Malformed HTTP request found in thread {0} when parsing its HTTP method: \n"
                    + rawData, this.threadId);
            // Handle the malformed request case, e.g., set method to null or throw an exception
        }
    }

    // Utility method to check if the method is valid
    private boolean isValidHttpMethod(String method) {
        return method.equals("GET") || method.equals("POST") || method.equals("PUT") ||
                method.equals("DELETE") || method.equals("HEAD") || method.equals("OPTIONS") ||
                method.equals("PATCH") || method.equals("CONNECT");
    }

    private void parseHostAndPort() {
        try {
            if (rawData == null || rawData.isEmpty()) {
                logger.log(Level.WARNING, "Empty or null rawData in HTTPRequest in thread {0}", this.threadId);
                // Handle the empty or null rawData case, e.g., set method to null or throw an exception
                return;
            }
            int methodPos = rawData.indexOf(" ");
            if (methodPos == -1) {
                logger.log(Level.WARNING, "No space found after HTTP method in raw data in thread {0}.", this.threadId);
                return;
            }

            int urlPos = rawData.indexOf(" ", methodPos + 1);
            if (urlPos == -1) {
                logger.log(Level.WARNING, "No space found after URL in raw data in thread {0}.", this.threadId);
                return;
            }

            String url = rawData.substring(methodPos + 1, urlPos).trim();
            this.urlString = url;

            // check BlockList
            if (BlockListManager.isBlocked(url)) {
                isBlocked = true;
                logger.log(Level.INFO, "Host: {0}", url + " is blocked successfully !");
                return;
            }
            System.out.println("The " + url + " is not blocked");

            switch (method.toUpperCase()) {
                case "CONNECT":
                    parseConnectMethod();
                    break;
                case "POST":
                    parsePostMethod();
                    logger.log(Level.INFO, "POST Request URL: {0}", urlString);
                    break;
                case "GET":
                    // TODO: move the logic of parsing getMethod here parseGetMethod();
                    parseGetMethod();
                    logger.log(Level.INFO, "GET Request URL: {0}", urlString);
                case "PUT":
                case "DELETE":
                case "HEAD":
                case "OPTIONS":
                case "PATCH":
                    logger.log(Level.INFO, "{0} Request URL: {1}", new Object[]{method, url});
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing host and port: {0}", e.getMessage());
        }
    }

    private void parseGetMethod() {
        logger.log(Level.INFO, "GET Request URL: {0}", urlString);
    }

    private void parseConnectMethod() {
        String[] urlParts = urlString.split(":");
        if (urlParts.length == 2) {
            this.host = urlParts[0];
            try {
                this.port = Integer.parseInt(urlParts[1]);
                logger.log(Level.INFO, "CONNECT Request: Host={0}, Port={1}", new Object[]{host, port});
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Invalid port number in CONNECT request: {0}", urlParts[1]);
            }
        } else {
            logger.log(Level.WARNING, "Invalid CONNECT request format: {0}", urlString);
        }
    }

    private void parsePostMethod() {
        // Assuming the body is separated by a blank line after headers
        int bodyIndex = rawData.indexOf("\n\n");
        if(bodyIndex == -1){
            rawData.indexOf("\r\n\r\n");
        }
        if (bodyIndex != -1) {
            this.postBody = rawData.substring(bodyIndex).trim();
            logger.log(Level.INFO, "POST Body: {0}", postBody);
        } else {
            logger.log(Level.WARNING, "POST request does not contain a body or is improperly formatted in thread {0}.",
                    threadId);
        }
    }
    public String getMethod() { return this.method;}
    public String getHost() { return this.host;}
    public int getPort() { return this.port;}
    public String getUrlString() {
        return this.urlString;
    }
    public String getPostBody(){
        return this.postBody;
    }
    public String getStartLine() {return this.startLine; }
    public boolean isBlocked() {
        return this.isBlocked;
    }

    // TODO: handle 404
    public static void handle404Error() {
        System.out.println("404 Error occurred");
    }

    @Override
    public String toString(){
        return "method: " + method + " host: " + host + " port: " + port;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String key, String val) {
        headers.put(key, val);
    }
}
