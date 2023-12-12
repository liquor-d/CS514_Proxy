import java.util.HashMap;
import java.util.Iterator;
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
            Node node = cacheMap.get(key);
            if (node != null && node.response.isExpired()) {
                writeLock.lock();
                try {
                    if (node.response.isExpired()) {
                        removeCache(key);
                        cacheMap.remove(key);
                        return null;
                    }
                } finally {
                    writeLock.unlock();
                }
            }
            if (node != null) {
                moveToHead(node);
            }
            return node != null ? node.response : null;
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
        writeLock.lock();
        try {
            if (head == null) {
                this.head = newNode;
                this.tail = newNode;
            } else if (head == tail) {
                this.head = newNode;
                newNode.next = tail;
                tail.prev = newNode;
            } else {
                newNode.next = head;
                head.prev = newNode;
                head = newNode;
            }
        }finally {
            writeLock.unlock();
        }
    }
    @Override
    public int size(){
        return cacheMap.size();
    }
    public void removeCache(String findStartLine) {
        writeLock.lock();
        try {
            if (head == null) return;
            else if (head == tail) {
                if (head.startLine.equals(findStartLine)) {
                    head = null;
                    tail = null;
                }
            } else if (head.startLine.equals(findStartLine)) {
                head.next.prev = null;
                head = head.next;
            } else if (tail.startLine.equals(findStartLine)) {
                tail.prev.next = null;
                tail = tail.prev;
            } else {
                Node curr = head;
                while (curr != null) {
                    if (curr.startLine.equals(findStartLine)) {
                        curr.prev.next = curr.next;
                        curr.next.prev = curr.prev;
                        break;
                    }
                    curr = curr.next;
                }
            }
        }finally {
            writeLock.unlock();
        }
    }
    private void moveToHead(Node node) {
        writeLock.lock();
        try {
            removeCache(node.startLine);
            addCache(node);
        }finally {
            writeLock.unlock();
        }
    }
    private void removeLeastRecentlyUsed() {
        writeLock.lock();
        try {
            if (tail == null) {
                return;
            }
            cacheMap.remove(tail.startLine);

            if (head == tail) {
                // The cache has only one node
                head = null;
                tail = null;
            } else {
                // More than one node in the cache
                tail = tail.prev;
                tail.next = null;
            }
        }finally {
            writeLock.unlock();
        }
    }
    public void removeExpiredEntries() {
        writeLock.lock();
        try {
            Iterator<Map.Entry<String, Node>> it = cacheMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Node> entry = it.next();
                if (entry.getValue().response.isExpired()) {
                    it.remove(); // Remove from the cacheMap
                    // Remove it from the linked list
                    removeNodeFromLinkedList(entry.getValue());
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void removeNodeFromLinkedList(Node node) {
        if (node.prev != null) node.prev.next = node.next;
        if (node.next != null) node.next.prev = node.prev;
        if (node == head) head = node.next;
        if (node == tail) tail = node.prev;
    }
}
