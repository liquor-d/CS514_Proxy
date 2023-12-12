import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ProxyServer is starter class, responsible for accept
 *
 */
public class ProxyServer implements Runnable {
    private static final Logger logger = Logger.getLogger(ProxyServer.class.getName());
    private static final int CACHE_CAPACITY = 100;

    private volatile boolean stopped = false;
    private int port;
    private ServerSocket serverSocket;
    private ResponseCache cache;
    public ProxyServer(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
        this.cache = new ConcurrentLRUCache(CACHE_CAPACITY);
        logger.log(Level.INFO, "Server established on port {0}", port);
    }
    public synchronized void stop() {
        logger.log(Level.INFO, "Proxy Server stopping");
        this.stopped = true;
    }
    private synchronized boolean isRunning() {
        return !this.stopped;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "ProxyServer Running");
        try {
            // Start cache cleaning thread
            CacheManager cacheManager = new CacheManager((ConcurrentLRUCache) cache);
            Thread cacheManagerThread = new Thread(cacheManager);
            cacheManagerThread.start();
            while (this.isRunning() && serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                RequestHandler newThread = new RequestHandler(socket, cache);
                newThread.start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating server socket @PORT: {0}", port);
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ConsoleManager consoleManager = new ConsoleManager();
        Thread consoleManagerThread = new Thread(consoleManager);
        consoleManagerThread.start();
    }
}
