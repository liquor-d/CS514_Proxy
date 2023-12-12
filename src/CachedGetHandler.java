import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CachedGetHandler {
    public static final Logger logger = Logger.getLogger(CachedGetHandler.class.getName());
    private GetHandler getHandler;
    private ResponseCache cache;
    private int threadId;

    public CachedGetHandler(GetHandler getHandler, ResponseCache cache, int threadId) {
        this.getHandler = getHandler;
        this.cache = cache;
        this.threadId = threadId;
    }

    public void get() throws IOException, ParseException {
        String requestKey = getHandler.getHttpRequest().getStartLine();

        // Check if response is in cache
        CachedResponse cachedResponse = cache.get(requestKey);
        // Flag to determine if we use conditional get
        boolean conditionalGet = false;
        if (cachedResponse != null) {
            if (!cachedResponse.isExpired() && !cachedResponse.mustRevalidate()) {
                // Update and serve from cache
                updateAndServeCachedResponse(cachedResponse);
                return;
            } else if (cachedResponse.mustRevalidate()) {
                // Conditional GET
                addConditionalHeadersToRequest(cachedResponse);
            }
            // If expired and not must-revalidate, continue to fetch from server without modifying client request
        }

        // Setup connection and send request
        getHandler.setupConnectionAndSendRequest();

        // Fetch response from server
        String responseString = getHandler.fetchResponseFromServer();

        // Convert the response string into an HTTPResponse object
        HTTPResponse httpResponse = new HTTPResponse(responseString);

        if (conditionalGet && httpResponse.getStatusCode() == 304) {
            // Server responded with 304 Not Modified, use the cached response
            updateAndServeCachedResponse(cachedResponse);
            return;
        }

        // Check if the response should be cached
        if (CachedResponse.shouldBeCached(httpResponse)) {
            // Calculate the expiry time for the cache
            long expiryTime = CachedResponse.calculateExpiryTime(httpResponse);

            // Create a CachedResponse object
            CachedResponse responseToCache = new CachedResponse(httpResponse, expiryTime);

            // Store the CachedResponse in the cache
            String requestKeyToCache = httpResponse.getStartLine();
            cache.put(requestKeyToCache, responseToCache);
            logger.log(Level.INFO, "Response starting with \"{0}\" has been cached, the size of cache is now {1}",
                    new Object[]{requestKey, cache.size()});
        }

        // Flush response to the client
        getHandler.flushResponseToClientAndDisconnect(responseString);
    }
    private void updateAndServeCachedResponse(CachedResponse cachedResponse) throws IOException {
        long ageInSeconds = (System.currentTimeMillis() - cachedResponse.getCachedTime()) / 1000;
        cachedResponse.getHttpResponse().updateHeader("Age", String.valueOf(ageInSeconds));
        String cachedResponseString = cachedResponse.getHttpResponse().toString();
        logger.log(Level.INFO, "Response for the request \"{0}\" is directly served from cache", getHandler.getHttpRequest());
        getHandler.flushResponseToClientAndDisconnect(cachedResponseString);
    }
    private void addConditionalHeadersToRequest(CachedResponse cachedResponse) {
        HTTPRequest httpRequest = getHandler.getHttpRequest();
        if (cachedResponse.getETag() != null) {
            httpRequest.addHeader("If-None-Match", cachedResponse.getETag());
            logger.log(Level.INFO, "Response for the request \"{0}\":" +
                    "\n\tConditional header has been added to the request with the appearance of ETag in cached response",
                    getHandler.getHttpRequest());
        } else if (cachedResponse.getLastModified() != null) {
            httpRequest.addHeader("If-Modified-Since", cachedResponse.getLastModified());
            logger.log(Level.INFO, "Response for the request \"{0}\":" +
                            "\n\tConditional header has been added to the request with the appearance of If-Modified-Since in cached response",
                    getHandler.getHttpRequest());
        }
    }
}