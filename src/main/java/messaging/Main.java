package messaging;

import messaging.queues.RingBuffer;
import messaging.queues.SPSCRingBuffer;

import java.io.FileReader;

public class Main {

    private static int SPSC_BUFFER_SIZE = 1024;
    private static int NUM_MESSAGES = 100_000;
    private static int PERF_BUFFER_SIZE = 100_000;
    private static int PRODUCER_CORE = 1;
    private static int CONSUMER_CORE = 2;
    private static int WARMUP_MESSAGE_PERCENTAGE = 10;
    private static int MESSAGE_POOL_SIZE = 1000;

    private static void loadConfig() {
        System.out.println("\n************ Loading Configuration ************");
        String configPath = System.getProperty("configPath");
        try (FileReader reader = new FileReader(configPath)) {
            java.util.Properties properties = new java.util.Properties();
            properties.load(reader);

            SPSC_BUFFER_SIZE = Integer.parseInt(properties.getProperty("SPSC_BUFFER_SIZE", String.valueOf(SPSC_BUFFER_SIZE)));
            NUM_MESSAGES = Integer.parseInt(properties.getProperty("NUM_MESSAGES", String.valueOf(NUM_MESSAGES)));
            PERF_BUFFER_SIZE = Integer.parseInt(properties.getProperty("PERF_BUFFER_SIZE", String.valueOf(PERF_BUFFER_SIZE)));
            PRODUCER_CORE = Integer.parseInt(properties.getProperty("PRODUCER_CORE", String.valueOf(PRODUCER_CORE)));
            CONSUMER_CORE = Integer.parseInt(properties.getProperty("CONSUMER_CORE", String.valueOf(CONSUMER_CORE)));
            WARMUP_MESSAGE_PERCENTAGE = Integer.parseInt(properties.getProperty("WARMUP_MESSAGE_PERCENTAGE", String.valueOf(WARMUP_MESSAGE_PERCENTAGE)));
            MESSAGE_POOL_SIZE = Integer.parseInt(properties.getProperty("MESSAGE_POOL_SIZE", String.valueOf(MESSAGE_POOL_SIZE)));

            System.out.println("Configuration loaded from " + configPath);

        } catch (Exception ex) {
            System.out.println("Unable to find configuration file at " + configPath + ", using default settings.");
        }
    }


    public static void main(String[] args) {

        loadConfig();

        int spscBufferSize = SPSC_BUFFER_SIZE;
        int numMessage = NUM_MESSAGES;
        int numWarmUpMessage = (int) (numMessage * 0.01 * WARMUP_MESSAGE_PERCENTAGE);
        int perfBufferSize = PERF_BUFFER_SIZE;
        int messagePoolSize = MESSAGE_POOL_SIZE;

        int producerCore = PRODUCER_CORE;
        int consumerCore = CONSUMER_CORE;

        System.out.println("SPSC Buffer Size: " + spscBufferSize);
        System.out.println("Number of Messages: " + numMessage);
        System.out.println("Number of Warm-up Messages: " + numWarmUpMessage);
        System.out.println("Performance Buffer Size: " + perfBufferSize);
        System.out.println("Message Pool Size: " + messagePoolSize);
        System.out.println("Producer Core: " + producerCore);
        System.out.println("Consumer Core: " + consumerCore);

        System.out.println("\n************ Creating Buffers ************");
        RingBuffer buffer = new SPSCRingBuffer(spscBufferSize);
//        RingBuffer performanceBuffer = new SPSCRingBuffer(perfBufferSize);
        MessagePool messagePool = new MessagePool(messagePoolSize);

        System.out.println("\n************ Creating Threads ************");
        Producer producer = new Producer(buffer, numMessage, numWarmUpMessage, producerCore, messagePool);
        Consumer consumer = new Consumer(buffer, numMessage, numWarmUpMessage, consumerCore, messagePool);
//        PerformanceTester performanceTester = new PerformanceTester(performanceBuffer, numMessage);

        Thread t1 = new Thread(producer);
        Thread t2 = new Thread(consumer);
//        Thread t3 = new Thread(performanceTester);

        System.out.println("\n************ Starting Threads ************");
        t1.start();
        t2.start();
//        t3.start();

        try {
            t1.join();
            t2.join();
//            t3.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
