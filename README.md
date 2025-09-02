# Perf Testing
A simple application to test performance of a Ring Buffer without locking.

## Requirements
1. Java-17
2. Python-3
3. A native linux env (or WSL-2 in windows)

## How to run
1. Build the java project
```bash
./gradlew :clean :build :fatJar
```
2. Bring the benchmark script to the root folder in the repo
```bash
cp src/main/shell/benchmark .
```
3. Check if the script has executable permissions. If it doesn't, grant it.
```bash
chmod +x benchmark
```
4. Run the script
```bash
./benchmark
```

## How to analyze
The benchmark script runs the Main.java class in the project under some linux profiling tools and generates a few log 
files for monitoring. It also generates a csv file with the latencies recorded.
1. `pidstat.log` contains information on the number of voluntary and involuntary preemptions made by the different Java 
threads.
2. `ps_threads.log` shows the cores on which the different Java threads ran.
3. `latencies.csv` lists the latencies of all the messages

### For analysis of these three files, check the following:
1. Since there is busy spinning in place, the number of voluntary preemptions of the Producer & Consumer threads should 
be 0.
**To verify this, check the number of cswch/s for the Producer thread id & Consumer thread id in the `pidstat.log`**
2. Check if the Producer & Consumer thread ids actually stay pinned on the cores which they are configured for in the 
`ps_threads.log`
3. The latencies graph shouldn't have sudden peaks or slope up/down after a certain no. of messages. **To take a look 
at the generated graph of the latencies, run the following**:
```bash
python src/main/python/plot.py <path_to_latencies.csv>
```

## Questions
1. Reason for a lot of voluntary preemptions of the Producer
2. Two peaks in the first 500k messages
3. On increase of no. of messages in message pool, avg latency increases
4. JVM settings

## Currently implemented
1. Non-blocking ring buffer
2. Busy spinning
3. Producer Consumer core pinning
4. EpsilonGC
5. Warmup
6. Message pooling
7. Message padding to avoid false sharing

## More Things to Try
1. Isolate cpus (try only after 1st question is answered)
