package messaging;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import messaging.queues.RingBuffer;

public class PerformanceTester implements Runnable {

    private final RingBuffer buffer;
    private final int messageCount;
    private long totalLatency;
    private int messagesProcessed;
    private final List<Long> latencies = new ArrayList<>();

    public PerformanceTester(RingBuffer buffer, int messageCount) {
        this.buffer = buffer;
        this.messageCount = messageCount;
        this.totalLatency = 0;
        this.messagesProcessed = 0;
    }

    public void run() {
        while (messagesProcessed < messageCount) {
            Message message = null;
            try {
                message = buffer.poll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            long latency = message.getDequeueTime() - message.getEnqueueTime();
            if (latency < 0) {
                System.out.println("Overflow detected for message id: " + message.getId());
                System.out.println("Enqueue Time: " + message.getEnqueueTime() + ", Dequeue Time: " + message.getDequeueTime());
            }
            totalLatency += latency;
            latencies.add(latency);
            messagesProcessed++;
        }
        double averageLatency = (double) totalLatency / messagesProcessed;

        writeToCsv();
        Collections.sort(latencies);
        int idx_99 = (int) Math.ceil(0.99 * messagesProcessed) - 1;
        long percentile_99 = latencies.get(Math.max(idx_99, 0));

        int idx_9999 = (int) Math.ceil(0.9999 * messagesProcessed) - 1;
        long percentile_9999 = latencies.get(Math.max(idx_9999, 0));

        System.out.println("\n************ Results ************");
        System.out.println("Processed " + messagesProcessed + " messages.");
        System.out.println("Minimum Latency: " + latencies.get(0) + " ns");
        System.out.println("Maximum Latency: " + latencies.get(latencies.size() - 1) + " ns");
        System.out.println("Average Latency: " + averageLatency + " ns");
        System.out.println("99th Percentile Latency: " + percentile_99 + " ns");
        System.out.println("99.99th Percentile Latency: " + percentile_9999 + " ns");
    }

    public void writeToCsv() {
        try (FileWriter writer = new FileWriter("latencies.csv")) {
            writer.write("MessageIndex,Latency(ns)\n");
            for (int i = 0; i < latencies.size(); i++) {
                writer.write(i + "," + latencies.get(i) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}