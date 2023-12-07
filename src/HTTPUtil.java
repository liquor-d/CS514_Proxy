import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPUtil {

    private static final Logger logger = Logger.getLogger(HTTPUtil.class.getName());

    public static void closeQuietly(AutoCloseable resource, int threadId) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to close resource in RequestHandler {0}", new Object[]{threadId, e.getMessage()});
            }
        }
    }
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
            case HttpURLConnection.HTTP_MOVED_PERM:
                return "Resource permanently moved (301)";
            case HttpURLConnection.HTTP_MOVED_TEMP:
                return "Temporary redirect (302)";
            case HttpURLConnection.HTTP_SEE_OTHER:
                return "See other resource (303)";
            case HttpURLConnection.HTTP_NOT_MODIFIED:
                return "Not modified (304)";
            case HttpURLConnection.HTTP_BAD_GATEWAY:
                return "Bad Gateway (502)";
            case HttpURLConnection.HTTP_UNAVAILABLE:
                return "Service Unavailable (503)";
            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                return "Gateway Timeout (504)";
            default:
                return "Unhandled response code: " + responseCode;
        }
    }
}