public interface ResponseCache {
    CachedResponse get(String key);

    void put(String key, CachedResponse response);

    boolean isExpired(String key);

    void delete(String key);

    int size();
}