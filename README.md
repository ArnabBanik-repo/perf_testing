# Perf Testing
A simple application to test performance of a Ring Buffer without locking.

## Currently implemented
1. Non-blocking ring buffer
2. Producer Consumer core pinning
3. EpsilonGC
4. Warmup
5. Message pooling

## More Things to Try
1. Isolate cpus (try only after 1st question is answered)

## Questions
1. Reason for a lot of voluntary preemptions of the Producer
2. Two peaks in the first 500k messages
3. On increase of no. of messages in message pool, avg latency increases
4. JVM settings