import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConcurrentLRUCacheTest {

    private ConcurrentLRUCache cache;

    @Before
    public void setUp() {
        cache = new ConcurrentLRUCache(2); // Small size for testing
    }

    @Test
    public void testPutAndGet() {
        // Setup test data
        HTTPResponse httpResponse1 = new HTTPResponse("HTTP/1.1 200 OK", "Response body for key1");
        HTTPResponse httpResponse2 = new HTTPResponse("HTTP/1.1 200 OK", "Response body for key2");
        CachedResponse cachedResponse1 = new CachedResponse(httpResponse1, System.currentTimeMillis() + 60000);
        CachedResponse cachedResponse2 = new CachedResponse(httpResponse2, System.currentTimeMillis() + 60000);

        // Perform operations
        cache.put("key1", cachedResponse1);
        cache.put("key2", cachedResponse2);
        assertTrue("Determining size of cache", cache.size() == 2);
        // Assertions
        assertSame("Retrieving key1", cachedResponse1, cache.get("key1"));
        assertSame("Retrieving key2", cachedResponse2, cache.get("key2"));
    }

    @Test
    public void testEviction() {
        // Setup test data
        HTTPResponse httpResponse1 = new HTTPResponse("HTTP/1.1 200 OK", "Response body for key1");
        HTTPResponse httpResponse2 = new HTTPResponse("HTTP/1.1 200 OK", "Response body for key2");
        HTTPResponse httpResponse3 = new HTTPResponse("HTTP/1.1 200 OK", "Response body for key3");
        CachedResponse cachedResponse1 = new CachedResponse(httpResponse1, System.currentTimeMillis() + 60000);
        CachedResponse cachedResponse2 = new CachedResponse(httpResponse2, System.currentTimeMillis() + 60000);
        CachedResponse cachedResponse3 = new CachedResponse(httpResponse3, System.currentTimeMillis() + 60000);

        // Perform operations
        cache.put("key1", cachedResponse1);
        cache.put("key2", cachedResponse2);
        cache.put("key3", cachedResponse3); // Should evict key1

        // Assertions
        assertNull("key1 should be evicted", cache.get("key1"));
        assertSame("Retrieving key2", cachedResponse2, cache.get("key2"));
        assertSame("Retrieving key3", cachedResponse3, cache.get("key3"));
    }

    @Test
    public void testUpdate() {
        // Setup test data
        HTTPResponse httpResponse1 = new HTTPResponse("HTTP/1.1 200 OK", "Response body for key1");
        HTTPResponse httpResponse1Updated = new HTTPResponse("HTTP/1.1 200 OK", "Updated body for key1");
        CachedResponse cachedResponse1 = new CachedResponse(httpResponse1, System.currentTimeMillis() + 60000);
        CachedResponse cachedResponse1Updated = new CachedResponse(httpResponse1Updated, System.currentTimeMillis() + 60000);

        // Perform operations
        cache.put("key1", cachedResponse1);
        cache.put("key1", cachedResponse1Updated); // Update the value for key1

        // Assertions
        assertSame("Retrieving updated key1", cachedResponse1Updated, cache.get("key1"));
    }

    @Test
    public void testDelete() {
        // Setup test data
        HTTPResponse httpResponse1 = new HTTPResponse("HTTP/1.1 200 OK", "Response body for key1");
        CachedResponse cachedResponse1 = new CachedResponse(httpResponse1, System.currentTimeMillis() + 60000);

        // Perform operations
        cache.put("key1", cachedResponse1);
        cache.delete("key1");

        // Assertions
        assertNull("key1 should be deleted", cache.get("key1"));
    }

    @Test
    public void testExpiration() {
        // Setup test data
        HTTPResponse httpResponse1 = new HTTPResponse("HTTP/1.1 200 OK", "Response body for key1");
        CachedResponse cachedResponse1 = new CachedResponse(httpResponse1, System.currentTimeMillis() - 1000); // Already expired

        // Perform operations
        cache.put("key1", cachedResponse1);

        // Assertions
        assertTrue("key1 should be expired", cache.isExpired("key1"));
    }
}