import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

public class ConsoleManager implements Runnable {
    private volatile boolean running = true;
    private ProxyServer proxyServer;
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String INSTRUCTIONS =
            "+-----------------------------------------------+\n" +
            "| Enter command to configure the Proxy Server:  |\n" +
            "|   ADD [URL]     - block the specified URL     |\n" +
            "|   REMOVE [URL]  - remove URL from blocklist   |\n" +
            "|   EXIT          - safely exit program         |\n" +
            "|   VIEW          - print the Blocked List      |\n" +
            "+-----------------------------------------------+";

    public ConsoleManager() throws IOException {
        proxyServer = new ProxyServer(13318);
        Thread proxyServerThread = new Thread(proxyServer);
        proxyServerThread.start();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        printMgmtStyle(INSTRUCTIONS);
        while (running) {
            String command = scanner.nextLine().toLowerCase();
            String[] commandParts = command.split(" ");
            switch (commandParts[0]) {
                case "view":
                    viewBlockList();
                    break;
                case "add":
                    String urlToAdd = commandParts[1];
                    BlockListManager.addBlockedSite(urlToAdd);
                    printMgmtStyle(urlToAdd + " added to block list.");
                    break;
                case "remove":
                    String urlToRemove = commandParts[1];
                    BlockListManager.removeBlockedSite(urlToRemove);
                    printMgmtStyle(urlToRemove + " removed from block list.");
                    break;
                case "exit":
                    exitProxyServer();
                    break;
                default:
                    printMgmtStyle("Invalid command. Please try again.");
                    printMgmtStyle(INSTRUCTIONS);
                    break;
            }
        }
        scanner.close();
    }

    private void viewBlockList() {
        Set<String> blockList = BlockListManager.getBlockList();
        if (blockList.isEmpty()) {
            printMgmtStyle("Block list is empty.");
        } else {
            printMgmtStyle("Blocked URLs:");
            for (String url : blockList) {
                printMgmtStyle(url);
            }
        }
    }

    private void exitProxyServer() {
        // Safely shut down the proxy server
        printMgmtStyle("Shutting down the proxy server...");
        running = false;
        proxyServer.stop();
        printMgmtStyle("Proxy server shut down successfully.");
    }

    private void printMgmtStyle(String toPrint) {
        System.out.println(ANSI_BLUE + toPrint + ANSI_RESET);
    }

}
