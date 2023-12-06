import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostHandler {
    private HTTPRequest httpRequest;
    private OutputStream clientOutput;
    private int threadId;

    public PostHandler(HTTPRequest httpRequest, OutputStream clientOutput, int threadId) {
        this.httpRequest = httpRequest;
        this.clientOutput = clientOutput;
        this.threadId = threadId;
    }

    public void post() throws IOException {
        URL url = new URL(httpRequest.getUrlString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set up the request properties
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        // Add more headers based on httpRequest object

        // Forward the POST body
        try (OutputStream os = connection.getOutputStream()) {
            os.write(httpRequest.getPostBody().getBytes());
            os.flush();
        }

        // Read the response from the server
        InputStream serverInput = connection.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];

        while ((nRead = serverInput.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        // Send the response back to the client
        clientOutput.write(buffer.toByteArray());
        clientOutput.flush();

        // Close connections
        serverInput.close();
        clientOutput.close();
        connection.disconnect();
    }
}
