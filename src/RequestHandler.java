import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler extends Thread{
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());
    private Socket clientSocket;
    private static AtomicInteger idCounter = new AtomicInteger(0);
    private int threadId;
    private BlockListManager blockListManager;

    public RequestHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        socket.setSoTimeout(5000);    // timeout window
        threadId = idCounter.incrementAndGet();
        String remoteAddress = socket.getInetAddress().getHostAddress();
        int remotePort = socket.getPort();
        int localPort = socket.getLocalPort();

        logger.log(Level.INFO, "Establishing new RequestHandler in thread: {0}", threadId);
        logger.log(Level.INFO, "Client's remote address: " + remoteAddress +
                ", remote port: " + remotePort + ", local port: " + localPort);
    }

    @Override
    public void run(){
        System.out.println("RequestHandler " + this.threadId + " running");
        try{
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            HTTPRequest request = new HTTPRequest(inputStream, threadId);

            // TODO check blacklist
            if (request.isBlocked()) {
                logger.log(Level.INFO, "Blocked host: {0}", request.getHost());
                sendForbidden(outputStream);
                return;
            }

            // TODO check cache
            // LFU, LRU, Interface // LRU

            if(request.getMethod() != null && request.getMethod().equals("CONNECT")){
                ConnectHandler connectHandler = new ConnectHandler(request.getHost(), request.getPort(),
                    inputStream, outputStream, threadId);
                connectHandler.connect();
            }
            else if (request.getMethod() != null && request.getMethod().equals("GET")){
                GetHandler getHandler = new GetHandler(request.getUrlString(), outputStream, threadId);
                getHandler.get();
            }

            else if (request.getMethod() != null && request.getMethod().equals("POST")){
                PostHandler postHandler = new PostHandler(request, outputStream, threadId);
                postHandler.post();
            }else{
                logger.log(Level.WARNING, "Unsupported request method in thread {0}: " + request.getMethod(), threadId);
            }

            inputStream.close();
            outputStream.close();
            clientSocket.close();
        }
        catch (SocketTimeoutException e) {
            logger.log(Level.WARNING, "SocketTimeoutException in RequestHandler {0}: {1}",
                    new Object[]{threadId, e.getMessage()});
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "IOException in RequestHandler in thread {0}", threadId);
            // e.printStackTrace();
        }
        catch (InterruptedException e) {
            logger.log(Level.SEVERE, "InterruptedException in RequestHandler {0}: {1}", new Object[]{threadId, e.getMessage()});
            logger.log(Level.WARNING, "Thread interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            HTTPUtil.closeQuietly(clientSocket, threadId);
        }
    }

    private void sendForbidden(OutputStream outputStream) throws IOException {
        System.out.println("Sending forbidden...");
        String fullResponse =
                "HTTP/1.1 403 Forbidden\r\n" +
                        "Content-Type: text/html\r\n" +
                        "\r\n";
        outputStream.write(fullResponse.getBytes());
        outputStream.flush();
    }

}
// 1.
