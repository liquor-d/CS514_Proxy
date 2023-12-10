import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

public class ConsoleManager implements Runnable {
    private volatile boolean running = true;
    private ProxyServer proxyServer;
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String INSTRUCTIONS = "Enter command to configure the Proxy Server: \n"+
            "\tADD [url] \t\t- block the specified URL\n"+
            "\tREMOVE [url] \t\t- remove URL from blocklist\n"+
            "\tEXIT \t\t\t\t- safely exit program\n"+
            "\tVIEW \t\t\t- print the Blocked List";

    public ConsoleManager() throws IOException {
        proxyServer = new ProxyServer(13318);
        Thread proxyServerThread = new Thread(proxyServer);
        proxyServerThread.start();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (running) {
            printMgmtStyle(INSTRUCTIONS);
            String command = scanner.nextLine().toLowerCase();
            String[] commandParts = command.split(" ");
            switch (commandParts[0]) {
                case "view":
                    viewBlockList();
                    break;
                case "add":
                    printMgmtStyle("Enter URL to block:");
                    String urlToAdd = commandParts[1];
                    BlockListManager.addBlockedSite(urlToAdd);
                    printMgmtStyle(urlToAdd + " added to block list.");
                    break;
                case "remove":
                    printMgmtStyle("Enter URL to unblock:");
                    String urlToRemove = commandParts[1];
                    BlockListManager.removeBlockedSite(urlToRemove);
                    printMgmtStyle(urlToRemove + " removed from block list.");
                    break;
                case "exit":
                    exitProxyServer();
                    break;
                default:
                    printMgmtStyle("Invalid command. Try again.");
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
        // Implement the logic to safely shut down the proxy server
        printMgmtStyle("Shutting down the proxy server...");
        running = false;
        proxyServer.stop();
        // Additional shutdown logic here
        printMgmtStyle("Proxy server shut down successfully.");
    }

    private void printMgmtStyle(String toPrint) {
        System.out.println(ANSI_BLUE + toPrint + ANSI_RESET);
    }

}
