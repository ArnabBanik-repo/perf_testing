package messaging;

import messaging.queues.RingBuffer;
import net.openhft.affinity.AffinityLock;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Consumer implements Runnable {

    private final RingBuffer buffer;
    private final int messageCount;
    private final int numWarmUpMessage;
    private final int coreId;
    private final List<Long> latencies;

    private final MessagePool messagePool;

    public Consumer(RingBuffer buffer, int messageCount, int numWarmUpMessage, int coreId, MessagePool messagePool) {
        this.buffer = buffer;
        this.messageCount = messageCount;
        this.latencies = new ArrayList<>(messageCount * 2);
        this.numWarmUpMessage = numWarmUpMessage;
        this.coreId = coreId;

        this.messagePool = messagePool;
    }

    public void consume() throws InterruptedException {
        int i = 0;
        while (i < numWarmUpMessage + messageCount) {
            Message message = buffer.poll();
            message.setDequeueTime(System.nanoTime() / 1000);
            if (i >= numWarmUpMessage) {
                latencies.add(message.getDequeueTime() - message.getEnqueueTime());
                if (message.getDequeueTime() < message.getEnqueueTime()) {
                    System.out.println("Overflow detected for message id: " + message.getId());
                    System.out.println("Enqueue Time: " + message.getEnqueueTime() + ", Dequeue Time: " + message.getDequeueTime());
                }
            }
            i++;
            messagePool.returnMessage(message);
        }
    }

    public void analyze() {
        writeToCsv();
        Collections.sort(latencies);
        int idx_99 = (int) Math.ceil(0.99 * messageCount) - 1;
        long percentile_99 = latencies.get(Math.max(idx_99, 0));

        int idx_9999 = (int) Math.ceil(0.9999 * messageCount) - 1;
        long percentile_9999 = latencies.get(Math.max(idx_9999, 0));
        double averageLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);

        System.out.println("\n************ Results ************");
        System.out.println("Processed " + messageCount + " messages.");
        System.out.println("Minimum Latency: " + latencies.get(0) + " us");
        System.out.println("Maximum Latency: " + latencies.get(latencies.size() - 1) + " us");
        System.out.println("Average Latency: " + averageLatency + " us");
        System.out.println("99th Percentile Latency: " + percentile_99 + " us");
        System.out.println("99.99th Percentile Latency: " + percentile_9999 + " us");
    }

    public void writeToCsv() {
        try (FileWriter writer = new FileWriter("latencies.csv")) {
            writer.write("MessageIndex,Latency(us)\n");
            for (int i = 0; i < latencies.size(); i++) {
                writer.write(i + "," + latencies.get(i) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try (AffinityLock lock = AffinityLock.acquireLock(coreId)) {
            long tid = net.openhft.affinity.Affinity.getThreadId();
            System.out.printf("Consumer: %s native tid=%d%n", Thread.currentThread().getName(), tid);
            consume();
            analyze();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
