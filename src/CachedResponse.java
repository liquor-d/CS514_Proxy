import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CachedResponse {
    private static final Logger logger = Logger.getLogger(CachedResponse.class.getName());
    private HTTPResponse httpResponse;
    private long expiryTime;

    private long cachedTime;

    public CachedResponse(HTTPResponse httpResponse, long expiryTime) {
        this.httpResponse = httpResponse;
        this.expiryTime = expiryTime;
        this.cachedTime = System.currentTimeMillis();
    }

    public HTTPResponse getHttpResponse() {
        return httpResponse;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public long getCachedTime() {
        return cachedTime;
    }

    public void setCachedTime(long cachedTime) {
        this.cachedTime = cachedTime;
    }

    // Checks if the cached response is expired
    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

    public boolean isNoCache() {
        String cacheControl = httpResponse.getHeader("Cache-Control");
        return cacheControl != null && cacheControl.contains("no-cache");
    }

    // Check if the response has a 'must-revalidate' directive
    public boolean mustRevalidate() {
        String cacheControl = httpResponse.getHeader("Cache-Control");
        return cacheControl != null && cacheControl.contains("must-revalidate");
    }

    // Get ETag value
    public String getETag() {
        return httpResponse.getHeader("ETag");
    }

    // Get Last-Modified value
    public String getLastModified() {
        return httpResponse.getHeader("Last-Modified");
    }

    // Static method to check if the response should be cached
    public static boolean shouldBeCached(HTTPResponse httpResponse) {
        String cacheControl = httpResponse.getHeader("Cache-Control");
        // for convenience, we treat no-cache as no-store
        if (cacheControl != null && cacheControl.contains("no-store")) {
            return false;
        }
        return true;
    }

    // Static method to calculate the expiry time
    public static long calculateExpiryTime(HTTPResponse httpResponse) {
        String cacheControl = httpResponse.getHeader("Cache-Control");
        long currentTimeMillis = System.currentTimeMillis();

        if (cacheControl != null && cacheControl.contains("max-age")) {
            int maxAgeIndex = cacheControl.indexOf("max-age");
            String maxAgeValue = cacheControl.substring(maxAgeIndex).split(",")[0].split("=")[1].trim();
            int maxAgeSeconds = Integer.parseInt(maxAgeValue);
            return currentTimeMillis + (maxAgeSeconds * 1000L); // Convert to milliseconds
        } else {
            String expires = httpResponse.getHeader("Expires");
            if (expires != null) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                    formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
                    return formatter.parse(expires).getTime();
                } catch (ParseException e) {
                    logger.log(Level.WARNING, "Failure to parse Expires field, use Default expiration time instead.");
                }
            }

            // Default expiry time of 10 seconds
            return currentTimeMillis + 10000L;
        }
    }
}

