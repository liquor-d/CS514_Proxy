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
    private String startLine; // TODO: parse startLine -> url, remove input 'url'
    private final String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg"};



    public GetHandler(String startLine, String url, OutputStream proxyOut, int threadId){
        this.startLine = startLine;
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
            String ImageExtension = checkImageType(url);
            System.out.println("extension: " + ImageExtension); // test
            if (ImageExtension != null) {
                // read image from url
                BufferedImage imgResource = ImageIO.read(inputStream);
                System.out.println("imgResource: " + imgResource);
                if (imgResource != null) {
                    String responseHeader = "HTTP/1.1 200 OK" + CRLF + CRLF;
                    proxyOut.write(responseHeader.getBytes());
                    ImageIO.write(imgResource, ImageExtension, proxyOut);
                } else {
                    HTTPRequest.handle404Error();
                }
                return;
            }

            // handle text stream
            System.out.println("Response Code: " + responseCode + ", responseMessage: " + responseMessage + ", in thread: " + threadId);
            // get response body
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
            StringBuffer responseBody = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {      // read response line by line
                responseBody.append(inputLine);
            }
            in.close();

            // ==================================
            // add cache
            // ==================================

//                System.out.println("Response context\n\n"); // test
//                System.out.println(responseBody.toString());
//                System.out.println("\n\n");

            HTTPResponse classResponse = new HTTPResponse(responseBody.toString());
            // TODO: cache operation

            // ==================================

            // combine response header, message, and body
            String response;
            if (responseCode >= 200 && responseCode < 300) {
                response = "HTTP/1.1 " + responseCode + " " + responseMessage
                        + CRLF
                        + "Content-Type: " + type
                        + CRLF
                        + "Content-Length: " + responseBody.toString().getBytes().length + CRLF
                        + CRLF
                        + responseBody
                        + CRLF + CRLF;
            }
            else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                response = "HTTP/1.1 " + responseCode + " " + responseMessage
                        + CRLF
                        + responseBody
                        + CRLF;
            }
//                System.out.println("Response from GET: " + CRLF + response+ " in thread: " + threadId + "\n"); //test
            // handle 404 error or other errors
            else {
                HTTPRequest.handle404Error();
                System.out.println("! NOT OK FOR Response Code: " + responseCode + ", responseMessage: " + responseMessage + " in thread: " + threadId);
                return;
            }

            proxyOut.write(response.getBytes());
            proxyOut.flush();
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