package messaging.queues;

import messaging.Message;

public class SPSCRingBuffer implements RingBuffer {

    private final Message[] buffer;
    private final int size;
    private volatile int head;
    private long p1, p2, p3, p4, p5, p6, p7;
    private int p8;
    private volatile int tail;

    SPSCRingBuffer() {
        this(BUFFER_SIZE);
    }

    public SPSCRingBuffer(int capacity) {
        this.size = capacity + 1;
        buffer = new Message[this.size];
        head = tail = 0;
    }

    @Override
    public void push(Message message) throws InterruptedException {
        int nextTail = (tail + 1) % size;
        while (nextTail == head) {
            Thread.onSpinWait();
        }
        buffer[tail] = message;
        tail = nextTail;
    }

    @Override
    public Message poll() throws InterruptedException {
        Message message = null;
        while (head == tail) {
            Thread.onSpinWait();
        }
        message = buffer[head];
        buffer[head] = null;
        head = (head + 1) % size;
        return message;
    }
}
