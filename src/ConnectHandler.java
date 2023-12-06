import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.lang.InterruptedException;

public class ConnectHandler {
    private int threadId;
    private Socket socket;
    private String host;
    private int port;
    private InputStream proxyIn;
    private OutputStream proxyOut;
    public ConnectHandler(String host, int port, InputStream proxyIn, OutputStream proxyOut, int threadId){
        this.threadId = threadId;
        this.host = host;
        this.port = port;
        this.proxyIn = proxyIn;
        this.proxyOut = proxyOut;
        try{
            this.socket = new Socket(host, port);
        }
        catch (IOException e) {
            System.out.println("IOException when connecting to host: "+host + " in thread: " + threadId);
            e.printStackTrace();
        }
    }

    public void connect(){
        try{
            InputStream inputStream = socket.getInputStream(); // response data from server
            OutputStream outputStream = socket.getOutputStream(); // request data to server

            String response = "HTTP/1.1 200 CONNECTION ESTABLISHED\r\n\r\n";
            proxyOut.write(response.getBytes());
            proxyOut.flush();

            Thread clientToProxy = new HTTPConnector(inputStream, proxyOut);
            clientToProxy.start();

            Thread ProxyToClient = new HTTPConnector(proxyIn, outputStream);
            ProxyToClient.start();

            clientToProxy.join();
            ProxyToClient.join();

            inputStream.close();
            outputStream.close();
            socket.close();
        }
        catch (IOException e) {
            System.out.println("IOException when connecting to host: "+host + " in thread: " + threadId);
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            System.out.println("InterruptedException when connecting to host: "+host + " in thread: " + threadId);
            e.printStackTrace();
        }
    }
}
