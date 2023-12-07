public class Cache {
    public static class Node{
        public String startLine;
        public HTTPResponse response;
        public Node next;
        public Node prev;

        public Node(String startLine, HTTPResponse response){
            this.startLine = startLine;
            this.response = response;
            this.next = null;
            this.prev = null;
        }
    }

    private Node head;
    private Node tail;
    private int numCache;

    public Cache(){
        this.head = null;
        this.tail = null;
        this.numCache = 0;
    }
    public HTTPResponse findCache(String findStartLine){
        Node curr = head;
        if(curr == null) {
            return null;
        }
        while(curr != null){
            if(curr.startLine == findStartLine){
                removeCache(findStartLine);         // TODO: remove node not line
                addCache(findStartLine, curr.response);
                return curr.response;
            }
            curr = curr.next;
        }
        return null;
    }
    public void addCache(String startLine, HTTPResponse response){
        numCache++;
        Node newNode = new Node(startLine, response);
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
                numCache--;
            }
        }
        else if(head.startLine == findStartLine){
            head.next.prev = null;
            head = head.next;
            numCache--;
        }
        else if(tail.startLine == findStartLine){
            tail.prev.next = null;
            tail = tail.prev;
            numCache--;
        }
        else{
            Node curr = head;
            while(curr != null){
                if(curr.startLine == findStartLine){
                    curr.prev.next = curr.next;
                    curr.next.prev = curr.prev;
                    numCache--;
                    break;
                }
                curr = curr.next;
            }
        }
    }
//    public checkCache()
}
