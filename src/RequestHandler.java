import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler extends Thread{
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());
    private Socket clientSocket;
    private static AtomicInteger idCounter = new AtomicInteger(0);
    private int threadId;
    private BlockListManager blockListManager;
    private ResponseCache cache;

    public RequestHandler(Socket socket, ResponseCache cache) throws IOException {
        this.cache = cache;
        this.clientSocket = socket;
        socket.setSoTimeout(100000);    // timeout window
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

            // check blacklist
            if (request.isBlocked()) {
                sendForbidden(outputStream);
                return;
            }

            if(request.getMethod() != null && request.getMethod().equals("CONNECT")){
                ConnectHandler connectHandler = new ConnectHandler(request.getHost(), request.getPort(),
                    inputStream, outputStream, threadId);
                connectHandler.connect();
            }
            else if (request.getMethod() != null && request.getMethod().equals("GET")){
                GetHandler getHandler = new GetHandler(request, outputStream, threadId);
                CachedGetHandler cachedGetHandler = new CachedGetHandler(getHandler, cache, threadId);
                cachedGetHandler.get(); // get request will be enabled with cache
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
        catch (ParseException e){
            logger.log(Level.WARNING, "ParseException in RequestHandler in thread {0}", threadId);
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
