public class CacheCleanupTask implements Runnable {
    private ConcurrentLRUCache cache;

    public CacheCleanupTask(ConcurrentLRUCache cache) {
        this.cache = cache;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(60000); // Sleep for 1 minute between each cleanup
                cache.removeExpiredEntries();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            System.out.println("Cache cleanup thread interrupted");
        }
    }
}