package messaging.queues;

import messaging.Message;

public interface RingBuffer {

    final int BUFFER_SIZE = 1024;

    void push(Message message) throws InterruptedException;
    Message poll() throws InterruptedException;
}