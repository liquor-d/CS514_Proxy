import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPResponse {
    private static final Logger logger = Logger.getLogger(HTTPResponse.class.getName());
    private String startLine;
    private Map<String, String> headers;
    private String body;
    private int statusCode;
    public HTTPResponse(String responseContent) throws ParseException{
        this.headers = new HashMap<>();
        parseResponse(responseContent);
    }
    // Constructor exclusively used for testing LRU cache implementation
    public HTTPResponse(String startLine, String body){
        this.headers = new HashMap<>();
        this.startLine = startLine;
        this.body = body;
    }

    private void parseResponse(String responseContent) throws ParseException{
        String[] lines = responseContent.split("\r\n");


        StringBuilder bodyBuilder = new StringBuilder();
        boolean isStartLine = true;
        boolean isHeaderSection = false;
        for (String line : lines) {
            if(isStartLine){
                startLine = line;
                try {
                    statusCode = Integer.parseInt(startLine.split(" ")[1]);
                }catch (Exception e){
                    throw new ParseException("The start line of a HTTP response is erroneous: " + startLine, startLine.length());
                }
                isStartLine = false;
                isHeaderSection = true;
                continue;
            }
            if (line.isEmpty()) {
                isHeaderSection = false; // End of headers, start of body
                continue;
            }

            if (isHeaderSection) {
                int colonIndex = line.indexOf(":");
                if (colonIndex != -1) {
                    // Case-insensitive store of name
                    String headerName = line.substring(0, colonIndex).trim().toLowerCase();
                    String headerValue = line.substring(colonIndex + 1).trim(); // Trim to remove the first empty space
                    headers.put(headerName, headerValue);
                } else {
                    startLine = line; // The first line is the start line
                }
            } else {
                bodyBuilder.append(line).append("\r\n");
            }
        }

        body = bodyBuilder.toString().trim(); // Trim to remove the last new line
    }

    public String getStartLine() {
        return startLine;
    }

    // case-insensitive fetching
    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    public String getBody() {
        return body;
    }

    public void updateHeader(String key, String value) {
        headers.put(key.toLowerCase(), value);
    }
    @Override
    public String toString() {
        StringBuilder responseBuilder = new StringBuilder();

        // Append start line
        responseBuilder.append(startLine).append("\r\n");

        // Append headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            responseBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        // Append an extra new line
        responseBuilder.append("\r\n");

        // Append body
        responseBuilder.append(body);

        return responseBuilder.toString();
    }
    int getStatusCode(){
        return statusCode;
    }
}