import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPConnector extends Thread {
    private static final Logger logger = Logger.getLogger(HTTPConnector.class.getName());
    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private InputStream inputStream;
    private OutputStream outputStream;
    private int threadId;
    public HTTPConnector(InputStream inputStream, OutputStream outputStream){
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.threadId = idCounter.incrementAndGet();
    }

    @Override
    public void run(){
        try {
            byte[] responseBuffer = new byte[65536]; // 64KB buffer
            int amountBytesToSend;

            while ((amountBytesToSend = inputStream.read(responseBuffer, 0 , responseBuffer.length)) != -1) {
                outputStream.write(responseBuffer, 0, amountBytesToSend);
                outputStream.flush();
            }
        }catch(SocketTimeoutException e){
            logger.log(Level.WARNING, "SocketTimeoutException in HTTPConnector {0}: {1}",
                    new Object[]{threadId, e.getMessage()});
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "HTTPConnector with thread id " + this.threadId + " experienced IOException: " + e.getMessage(), e);
        }
    }
}
