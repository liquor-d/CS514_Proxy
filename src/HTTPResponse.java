import java.util.HashMap;
import java.util.Map;

public class HTTPResponse {
    private String startLine;
    private Map<String, String> headers;
    private String body;

    public HTTPResponse(String responseContent) {
        this.headers = new HashMap<>();
        parseResponse(responseContent);
    }

    private void parseResponse(String responseContent) {
        String[] lines = responseContent.split("\r\n");


        StringBuilder bodyBuilder = new StringBuilder();
        boolean isStartLine = true;
        boolean isHeaderSection = false;
        for (String line : lines) {
            if(isStartLine){
                startLine = line;
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
                    String headerName = line.substring(0, colonIndex).trim();
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

    public String getHeader(String name) {
        return headers.get(name);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}