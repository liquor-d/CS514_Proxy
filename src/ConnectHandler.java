import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.lang.InterruptedException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectHandler {
    private static final Logger logger = Logger.getLogger(ConnectHandler.class.getName());
    private int threadId;
    private Socket socket;
    private String host;
    private int port;
    private InputStream proxyIn;
    private OutputStream proxyOut;
    public ConnectHandler(String host, int port, InputStream proxyIn, OutputStream proxyOut, int threadId) throws IOException{
        this.threadId = threadId;
        this.host = host;
        this.port = port;
        this.proxyIn = proxyIn;
        this.proxyOut = proxyOut;
        try{
            this.socket = new Socket(host, port);
        }
        catch (IOException e) {
            throw new IOException("Failed to connect to " + host + ":" + port, e);
        }
    }

    public void connect() throws IOException, InterruptedException{
        try{
            InputStream inputStream = socket.getInputStream(); // response data from server
            OutputStream outputStream = socket.getOutputStream(); // request data to server

            String response = "HTTP/1.1 200 CONNECTION ESTABLISHED\r\n\r\n";
            proxyOut.write(response.getBytes());
            proxyOut.flush();

            Thread clientToProxy = new HTTPConnector(inputStream, proxyOut);
            clientToProxy.start();

            Thread proxyToClient = new HTTPConnector(proxyIn, outputStream);
            proxyToClient.start();

            clientToProxy.join();
            proxyToClient.join();

            inputStream.close();
            outputStream.close();
            socket.close();
        }
        catch (IOException e) {
            throw new IOException("Error during data transfer with " + host + ":" + port, e);
        } catch (InterruptedException e) {
            throw new InterruptedException("InterruptedException during connection with " + host + ":" + port);
        }
    }
}
