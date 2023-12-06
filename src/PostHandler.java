import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class PostHandler {
    private HTTPRequest httpRequest;
    private OutputStream clientOutput;
    private int threadId;

    public PostHandler(HTTPRequest httpRequest, OutputStream clientOutput, int threadId) {
        this.httpRequest = httpRequest;
        this.clientOutput = clientOutput;
        this.threadId = threadId;
    }

    public void post() throws IOException { // 5-- //
        URL url;
        try {
            url = new URL(httpRequest.getUrlString());
        } catch (MalformedURLException e) {
            throw new IOException("Malformed URL: " + httpRequest.getUrlString(), e);
        }
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new IOException("Error opening connection to " + url, e);
        }

        // Set up the request properties
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        // copy essential headers
        Map<String, String> headers = httpRequest.getHeaders();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }
        // Forward the POST body
        try (OutputStream os = connection.getOutputStream()) {
            os.write(httpRequest.getPostBody().getBytes());
            os.flush();
        } catch (IOException e) {
            throw new IOException("Error sending POST data to " + url, e);
        }
        // Log the response code when error happened
        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            HTTPUtil.logResponseCode(httpRequest, connection.getResponseCode(), threadId);
        }
        // Read the response from the server
        InputStream serverInput;
        try {
            serverInput = connection.getInputStream();
        } catch (IOException e) {
            throw new IOException("Error reading response from " + url, e);
        }

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[65536];
            int nRead;

            while ((nRead = serverInput.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            clientOutput.write(buffer.toByteArray());
            clientOutput.flush();
        } catch (IOException e) {
            throw new IOException("IO Error while handling POST request: " + e.getMessage(), e);
        } finally {
            HTTPUtil.closeQuietly(serverInput, threadId);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
