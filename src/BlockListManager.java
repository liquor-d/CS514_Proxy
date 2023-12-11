import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class BlockListManager {
    private static final Set<String> blockList = new HashSet<>();

    public static synchronized void addBlockedSite(String domain) {
        blockList.add(domain);
    }

    public static synchronized void removeBlockedSite(String domain) {
        blockList.remove(domain);
    }

    public static synchronized boolean isBlocked(String url) {
        // remove port number
        if (url.contains(":")) {
            url = url.substring(0, url.indexOf(":"));
        }
        System.out.println("BlockList Manager is checking url: " + url);
        return blockList.contains(url);
    }


    public static synchronized Set<String> getBlockList() {
        return new HashSet<>(blockList);
    }
}
