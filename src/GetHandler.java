import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class GetHandler {
    private HTTPRequest httpRequest;
    private OutputStream clientOutput;
    private int threadId;
    private HttpURLConnection connection;

    public GetHandler(HTTPRequest httpRequest, OutputStream clientOutput, int threadId) {
        this.httpRequest = httpRequest;
        this.clientOutput = clientOutput;
        this.threadId = threadId;
    }

    public void get() throws IOException {
        try {
            setupConnectionAndSendRequest();
        } catch (IOException e) {
            throw new IOException("Error in setupConnectionAndSendRequest: " + e.getMessage(), e);
        }

        String response;
        try {
            response = fetchResponseFromServer();
        } catch (IOException e) {
            throw new IOException("Error in fetchResponseFromServer: " + e.getMessage(), e);
        }

        try {
            flushResponseToClientAndDisconnect(response);
        } catch (IOException e) {
            throw new IOException("Error in flushResponseToClient: " + e.getMessage(), e);
        }
    }

    public void setupConnectionAndSendRequest() throws IOException {
        URL url = new URL(httpRequest.getUrlString());
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Copy headers from the HTTPRequest to the HttpURLConnection
        Map<String, String> headers = httpRequest.getHeaders();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }
    }

    public String fetchResponseFromServer() throws IOException {
        StringBuilder responseBuilder = new StringBuilder();

        // Append status line
        int responseCode = connection.getResponseCode();
        responseBuilder.append("HTTP/1.1 ").append(responseCode).append(" ").append(connection.getResponseMessage()).append("\r\n");

        // Append headers
        connection.getHeaderFields().forEach((key, valueList) -> {
            if (key != null) { // Exclude the status line entry
                valueList.forEach(value -> responseBuilder.append(key).append(": ").append(value).append("\r\n"));
            }
        });

        // Append body
        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line).append("\r\n");
        }

        return responseBuilder.toString();
    }

    public void flushResponseToClientAndDisconnect(String response) throws IOException {
        clientOutput.write(response.getBytes());
        clientOutput.flush();
        connection.disconnect();
    }

    public HTTPRequest getHttpRequest() {
        return httpRequest;
    }
}