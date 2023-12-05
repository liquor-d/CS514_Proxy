import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;

public class HTTPRequest {
    private int threadId;
    private String rawData;
    private String method;
    private String host;
    private String urlString;
    private int port;

    public HTTPRequest(InputStream inputStream, int threadId){
        this.threadId = threadId;
        System.out.println("Begin parsing HTTPRequest in thread: " + this.threadId);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String request = "";
            String inputLine;
            while ((inputLine = in.readLine()) != null && !inputLine.equals("")) {
                request += inputLine+"\n";
            }
            this.rawData = request;
//            System.out.println("Request rawdata is : " + rawData);
        }
        catch (IOException e){
            System.out.println("IOException in RequestHandler " + threadId);
            e.printStackTrace();
        }
        this.method = null;
        this.host = null;
        this.port = -1;
        parseMethod();
        parseHostAndPort();
    }

    private void parseMethod(){
        int methodPos = rawData.indexOf(" ");
        if(methodPos != -1) this.method = rawData.substring(0, methodPos);
    }
    private void parseHostAndPort(){
        int methodPos = rawData.indexOf(" ");
        if(methodPos == -1) return;

        int urlPos = rawData.substring(methodPos+1).indexOf(" ");
        if(urlPos != -1){
            String url = rawData.substring(methodPos + 1, methodPos + 1 + urlPos);
            this.urlString = url;
//            System.out.println("url: " + url);
            if (this.method.equals("CONNECT")) {
                String[] urls = url.split(":");
                this.host = urls[0];
                this.port = Integer.parseInt(urls[1]);
                System.out.println("Request: " + method + " " + host);
            }
            else if (this.method.equals("GET")) {
                System.out.println("GET Request url: " + url);
            }
            //TODO
            else if (this.method.equals("POST")) {
                System.out.println("POST method not implemented yet!!");
            }
            else {
                System.out.println("*****Unknown request method!!");
            }
        }
    }
    public String getMethod() { return this.method;}
    public int getThreadId() { return this.threadId;}
    public String getHost() { return this.host;}
    public int getPort() { return this.port;}
    public String getUrlString() {
        return this.urlString;
    }

    // TODO: handle 404
    public static void handle404Error() {
        System.out.println("404 Error occurred!");
    }

    @Override
    public String toString(){
        return "method: " + method + " host: " + host + " port: " + port;
    }
}
