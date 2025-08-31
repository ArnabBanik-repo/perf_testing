package messaging;

public class MessagePool {

    private final Message[] pool;
    private final int size;
    private volatile int head;
    private long p1, p2, p3, p4, p5, p6, p7;
    private int p8;
    private volatile int tail;

    public MessagePool(int capacity) {
        size = capacity + 1;
        pool = new Message[size];
        for (int i = 0; i < capacity; i++)
            pool[i] = new Message();
        head = 0;
        tail = capacity;
    }

    public Message getMessage() {
        while (tail == head) {
            Thread.onSpinWait();
        }
        Message message = pool[head];
        head = (head + 1) % size;
        return message;
    }

    public void returnMessage(Message message) {
        message.reset();
        int nextTail = (tail + 1) % size;
        while (nextTail == head) {
            Thread.onSpinWait();
        }
        pool[tail] = message;
        tail = nextTail;
    }

}
