import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentLRUCache implements ResponseCache{
    private final Map<String, Node> cacheMap;
    private final int capacity;
    private Node head, tail;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    public static class Node{

        public String startLine;
        public CachedResponse response;
        public Node next;
        public Node prev;

        public Node(String startLine, CachedResponse response){
            this.startLine = startLine;
            this.response = response;
        }
    }

    public ConcurrentLRUCache(int capacity) {
        this.capacity = capacity;
        this.cacheMap = new HashMap<>();
    }
    @Override
    public CachedResponse get(String key) {
        readLock.lock();
        try {
            if (!cacheMap.containsKey(key)) {
                return null;
            }
            Node node = cacheMap.get(key);
            moveToHead(node);
            return node.response;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void put(String key, CachedResponse response) {
        writeLock.lock();
        try {
            if (cacheMap.containsKey(key)) {
                Node node = cacheMap.get(key);
                node.response = response;
                moveToHead(node);
            } else {
                Node newNode = new Node(key, response);
                addCache(newNode);
                cacheMap.put(key, newNode);
                if (cacheMap.size() > capacity) {
                    removeLeastRecentlyUsed();
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean isExpired(String key) {
        readLock.lock();
        try {
            if (!cacheMap.containsKey(key)) {
                return true;
            }
            return cacheMap.get(key).response.isExpired();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void delete(String key) {
        writeLock.lock();
        try {
            if (!cacheMap.containsKey(key)) {
                return;
            }
            removeCache(key);
            cacheMap.remove(key);
        } finally {
            writeLock.unlock();
        }
    }
    public void addCache(Node newNode){
        if(head == null){
            this.head = newNode;
            this.tail = newNode;
        }
        else if(head == tail){
            this.head = newNode;
            newNode.next = tail;
            tail.prev = newNode;
        }
        else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
    }
    public void removeCache(String findStartLine){
        if(head == null) return;
        else if(head == tail){
            if(head.startLine == findStartLine){
                head = null;
                tail = null;
            }
        }
        else if(head.startLine == findStartLine){
            head.next.prev = null;
            head = head.next;
        }
        else if(tail.startLine == findStartLine){
            tail.prev.next = null;
            tail = tail.prev;
        }
        else{
            Node curr = head;
            while(curr != null){
                if(curr.startLine == findStartLine){
                    curr.prev.next = curr.next;
                    curr.next.prev = curr.prev;
                    break;
                }
                curr = curr.next;
            }
        }
    }
    private void moveToHead(Node node) {
        // Remove the node from its current position
        removeCache(node.startLine); // Removes node from the linked list
        // Add the node back at the head
        addCache(node); // Adds node to the head of the linked list
    }
    private void removeLeastRecentlyUsed() {
        if (tail == null) {
            return; // Cache is empty, nothing to remove
        }

        // Remove the node from the cacheMap
        cacheMap.remove(tail.startLine);

        // Remove the node from the doubly linked list
        if (head == tail) {
            // The cache has only one node
            head = null;
            tail = null;
        } else {
            // More than one node in the cache
            tail = tail.prev;
            tail.next = null;
        }
    }
//    public checkCache()
}
