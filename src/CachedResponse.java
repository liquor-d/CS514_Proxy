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

}

