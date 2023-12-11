import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheManager implements Runnable {
    private static final Logger logger = Logger.getLogger(CacheManager.class.getName());
    private final ConcurrentLRUCache cache;
    private static final long cleanupIntervalMillis = 60000L;

    public CacheManager(ConcurrentLRUCache cache) {
        this.cache = cache;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(cleanupIntervalMillis);

                logger.log(Level.INFO, "CacheManager starts to clean up expired cache");
                cache.removeExpiredEntries();
                logger.log(Level.INFO, "CacheManager finished to clean up expired cache");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            logger.log(Level.WARNING, "CacheManager thread interrupted");
        }
    }
}