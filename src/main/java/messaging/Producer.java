package messaging;

import messaging.queues.RingBuffer;
import net.openhft.affinity.AffinityLock;

public class Producer implements Runnable {

    private final RingBuffer buffer;
    private final int messageCount;
    private final int numWarmUpMessage;
    private final int coreId;

    private final MessagePool messagePool;

    public Producer(RingBuffer buffer, int messageCount, int numWarmUpMessage, int coreId, MessagePool messagePool) {
        this.buffer = buffer;
        this.messageCount = messageCount;
        this.numWarmUpMessage = numWarmUpMessage;
        this.coreId = coreId;

        this.messagePool = messagePool;
    }

    public void produce() throws InterruptedException {
        int i = 0;
        while (i < numWarmUpMessage + messageCount) {
            Message message = messagePool.getMessage();
            message.setId(i);
            message.setEnqueueTime(System.nanoTime() / 1000);
            buffer.push(message);
            i++;
        }
    }

    @Override
    public void run() {
        try (AffinityLock lock = AffinityLock.acquireLock(coreId)) {
            long tid = net.openhft.affinity.Affinity.getThreadId();
            System.out.printf("Producer: %s native tid=%d%n", Thread.currentThread().getName(), tid);
            produce();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
