public interface ResponseCache {
    /**
     * Retrieves a cached response based on the request key.
     *
     * @param key The key for the cache entry, typically the first line of the request.
     * @return The cached response, or null if no entry exists or it's expired.
     */
    CachedResponse get(String key);

    /**
     * Adds or updates a response in the cache.
     *
     * @param key The key for the cache entry.
     * @param response The response to be cached.
     */
    void put(String key, CachedResponse response);

    /**
     * Checks if a cache entry is expired.
     *
     * @param key The key for the cache entry.
     * @return true if the entry is expired, false otherwise.
     */
    boolean isExpired(String key);

    /**
     * Deletes a cache entry.
     *
     * @param key The key for the cache entry to be deleted.
     */
    void delete(String key);

    int size();
}