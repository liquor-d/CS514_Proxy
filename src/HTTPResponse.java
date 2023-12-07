public class HTTPResponse {
    private String content;
    private String startLine;
    private boolean noStore;
    private boolean noCache;
    private boolean mustRevalidate;
    private int maxAge;
    private String expire;
    private String lastModified;
    public HTTPResponse(String startLine, String content){
        this.startLine = startLine;
        this.content = content;
        noStore = false;
        noCache = false;
        mustRevalidate = false;
        maxAge = -1;
        expire = null;

        this.parse();
    }
    public void parse(){
        if(content.indexOf("no-cache") != -1){
            noCache = true;
        }

        if(content.indexOf("no-store") != -1){
            noStore = true;
        }

        if(content.indexOf("must-revalidate") != -1){
            mustRevalidate = true;
        }

        int maxAgePos = content.indexOf("max-age");
        if(maxAgePos != -1){
            maxAge = Integer.parseInt(content.substring(maxAgePos+8));
        }

        int expPos = content.indexOf("Expires: ");
        if(expPos != -1){
            int expEndPos = content.indexOf(" GMT");
            expire = content.substring(expPos+9, expEndPos+4);
//            expire = content.substring(expPos+9, expPos+38); // not GMT
        }

        int lastModifyPos = content.indexOf("Last-Modified: ");
        if(lastModifyPos != -1){
            int lastModifyEndPos = lastModifyPos + content.substring(lastModifyPos).indexOf("\r\n");
            lastModified = content.substring(lastModifyPos + 15, lastModifyEndPos);
        }

        //TODO: if cookie
    }

}
