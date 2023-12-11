public class CachedResponse {
    private HTTPResponse httpResponse;
    private long expiryTime;

    public CachedResponse(HTTPResponse httpResponse, long expiryTime) {
        this.httpResponse = httpResponse;
        this.expiryTime = expiryTime;
    }
    public HTTPResponse getHttpResponse() {
        return httpResponse;
    }

    public long getExpiryTime() {
        return expiryTime;
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
        if (cacheControl != null && cacheControl.contains("no-store")) {
            return false;
        }
        // Add other checks as necessary
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
                // Convert Expires header to time in milliseconds
                // long expiresTimeMillis = /* Convert expires to milliseconds */;
                // return expiresTimeMillis;
            }
        }

        // Default expiry time of 10 seconds
        return currentTimeMillis + 10000L;
    }
}

