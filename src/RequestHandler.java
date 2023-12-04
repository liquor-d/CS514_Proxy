import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class RequestHandler extends Thread{
    private Socket socket;
    private static int idCounter;
    private int threadId;
    private InputStream inputStream;
    private OutputStream outputStream;

    public RequestHandler(Socket socket) throws IOException {
        this.socket = socket;
//        socket.setSoTimeout(3000);    // timeout window
        idCounter++;
        threadId = idCounter;
        System.out.println("Establishing new RequestHandler in thread:" + threadId);
        System.out.println("\tClient info: " + socket.toString());
    }

    @Override
    public void run(){
        System.out.println("RequestHandler " + this.threadId + " running");
        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            HTTPRequest request = new HTTPRequest(inputStream, threadId);

            // TODO check blacklist
            // TODO check cache

            if(request.getMethod() != null && request.getMethod().equals("CONNECT")){
                ConnectHandler connectHandler = new ConnectHandler(request.getHost(), request.getPort(),
                    inputStream, outputStream, threadId);
                connectHandler.connect();
            }
            else if (request.getMethod() != null && request.getMethod().equals("GET")){
                GetHandler getHandler = new GetHandler(request.getUrlString(), outputStream, threadId);
                getHandler.get();
            }
            // TODO: handle POST request
            else{
                System.out.println("request not implemented, :" + request + " in thread: ");
            }

            inputStream.close();
            outputStream.close();
            socket.close();
        }
//        catch (SocketTimeoutException e) {}
        catch (IOException e) {
            System.out.println("IOException in RequestHandler " + threadId);
            e.printStackTrace();
        }
    }
}
