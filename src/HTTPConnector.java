import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HTTPConnector extends Thread {
    private InputStream inputStream;
    private OutputStream outputStream;
    private static int idCounter;
    private int threadId;
    public HTTPConnector(InputStream inputStream, OutputStream outputStream){
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        idCounter++;
        threadId = idCounter;
    }

    @Override
    public void run(){
        try {
            byte[] responseBuffer = new byte[65536];
            int amountBytesToSend;

            while ((amountBytesToSend = inputStream.read(responseBuffer, 0 , responseBuffer.length)) != -1) {
                outputStream.write(responseBuffer, 0, amountBytesToSend);          // send them
                outputStream.flush();
            }
        } catch (IOException e) {
            System.out.println("HTTPConnector " + this.threadId + " experienced IOException");
//            e.printStackTrace();
        }
    }
}
