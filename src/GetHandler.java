import com.sun.net.httpserver.HttpHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class GetHandler {
    private int threadId;
    private HttpURLConnection connection;
    private String url;
    private InputStream inputStream;
    private OutputStream proxyOut;
    final String CRLF = "\r\n";
    private final String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg"};



    public GetHandler(String url, OutputStream proxyOut, int threadId){
        this.threadId = threadId;
        this.url = url;
        this.proxyOut = proxyOut;
    }

    public String checkImageType(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath().toLowerCase();

            // check if the path ends with an image extension
            for (String ext : imageExtensions) {
                if (path.contains(ext)) {
                    return ext.substring(1);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // handle GET request
    public void get() {
        try{
            // set up connection
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            inputStream = connection.getInputStream();
            int responseCode = connection.getResponseCode();
            String type = connection.getContentType();
            String responseMessage = connection.getResponseMessage();

            // handle image stream
            String extension = checkImageType(url);
            System.out.println("extension: " + extension);
            if (extension != null) {
                // read image from url
                BufferedImage imgResource = ImageIO.read(inputStream);
                System.out.println("imgResource: " + imgResource);
                if (imgResource != null) {
                    String responseHeader = "HTTP/1.1 200 OK" + CRLF + CRLF;
                    proxyOut.write(responseHeader.getBytes());
                    ImageIO.write(imgResource, extension, proxyOut);
                } else {
                    HTTPRequest.handle404Error();
                }
            }
            // handle text stream
            else if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                System.out.println("Response Code: " + responseCode + ", responseMessage: " + responseMessage + ", in thread: " + threadId);
                // get response body
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                StringBuffer responseBody = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {      // read response line by line
                    responseBody.append(inputLine);
                }
                in.close();
                // combine response header, message, and body
                String response = null;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    response = "HTTP/1.1 " + responseCode + " " + responseMessage
                            + CRLF
                            + "Content-Type: " + type
                            + CRLF
                            + "Content-Length: " + responseBody.toString().getBytes().length + CRLF
                            + CRLF
                            + responseBody.toString()
                            + CRLF + CRLF;
                }
                else {
                    response = "HTTP/1.1 " + responseCode + " " + responseMessage
                            + CRLF
                            + responseBody.toString()
                            + CRLF;
                }
//                System.out.println("Response from GET: " + CRLF + response+ " in thread: " + threadId + "\n");

                proxyOut.write(response.getBytes());
                proxyOut.flush();

                // TODO cache response

            }
            // handle 404 error or other errors
            else {
                HTTPRequest.handle404Error();
                System.out.println("! NOT OK FOR Response Code: " + responseCode + ", responseMessage: " + responseMessage + " in thread: " + threadId);
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("IOException when connecting to url: "+ url  + " in thread: " + threadId);
            HTTPRequest.handle404Error();
        }
        catch (IOException e) {
            System.out.println("IOException when connecting to url: "+ url  + " in thread: " + threadId);
            e.printStackTrace();
        }

    }
}