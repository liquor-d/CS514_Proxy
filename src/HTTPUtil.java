import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPUtil {

    private static final Logger logger = Logger.getLogger(HTTPUtil.class.getName());

    public static void logResponseCode(HTTPRequest httpRequest, int responseCode, int threadId) {
        String methodType = httpRequest != null ? httpRequest.getMethod() : "Unknown";
        String url = httpRequest != null ? httpRequest.getUrlString() : "Unknown URL";
        String responseMessage = getResponseMessage(responseCode);

        String logMessage = String.format("Thread ID: %d - Method: %s - URL: %s - Response: %s",
                threadId, methodType, url, responseMessage);

        logger.log(Level.INFO, logMessage);
    }

    private static String getResponseMessage(int responseCode) {
        switch (responseCode) {
            case HttpURLConnection.HTTP_OK:
                return "Request succeeded (200 OK)";
            case HttpURLConnection.HTTP_NOT_FOUND:
                return "Resource not found (404)";
            case HttpURLConnection.HTTP_BAD_REQUEST:
                return "Bad Request (400)";
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                return "Internal Server Error (500)";
            // Add other cases as needed
            default:
                return "Unhandled response code: " + responseCode;
        }
    }
}