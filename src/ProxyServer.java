import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer implements Runnable {
    private volatile boolean stopped = false;
    private int port;
    private ServerSocket serverSocket;
    public ProxyServer(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
        System.out.println("Server established on port " + port);
    }
    public synchronized void stop() {
        System.out.println("Proxy Server stopping");
        this.stopped = true;
    }
    private synchronized boolean isRunning() {
        return this.stopped == false;
    }

    @Override
    public void run() {
        System.out.println("ProxyServer Running");
        try {
            while (this.isRunning() && serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                RequestHandler newThread = new RequestHandler(socket);
                newThread.start();
            }
        } catch (IOException e) {
            System.out.println("Error creating server socket @PORT:" + port);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Thread proxyServer = new Thread(new ProxyServer(13318));
        proxyServer.run();
    }
}
