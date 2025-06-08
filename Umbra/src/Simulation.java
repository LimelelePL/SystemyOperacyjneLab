import java.util.*;
import java.util.concurrent.*;

public class Simulation {
    private static class Result {

        final double hitRate;
        final long elapsedMs;
        final double misses;
        final double evictionRate;

        Result(double missRate, double hitRate, double evictionRate, long elapsedMs) {
            this.misses=missRate;
            this.hitRate = hitRate;
            this.evictionRate=evictionRate;
            this.elapsedMs = elapsedMs;
        }
    }


    public static void main(String[] args) throws InterruptedException {
        int[] bufferSizes = {256, 512, 1024, 2048, 4096};
        int[] bTreeOrders = {16, 32, 64, 128};
        final int repetitions = 5;
        final int totalInserts = 200000;
        final int searchesPerThread = 20000;
        int[] threadCounts = {1, 8, 16};

        for (int numSearchThreads : threadCounts) {
            System.out.println("\n==== Wyniki dla liczby wątków: " + numSearchThreads + " ====");
            System.out.println("bufferSize | TreeOrder | sumTime | meanHitRate|  MeanMissRate| meanEvictions | meanElapsedMs |");

            for (int maxFrames : bufferSizes) {
                for (int bTreeOrder : bTreeOrders) {
                    List<Result> allResults = new ArrayList<>();

                    for (int rep = 0; rep < repetitions; rep++) {
                        long seedInsert = 42L + rep * 1000 + maxFrames + bTreeOrder;
                        long seedSearch = 99L + rep * 1000 + maxFrames + bTreeOrder;

                        BufferManager bm = new BufferManager(maxFrames);
                        BPlusTree tree = new BPlusTree(bTreeOrder, bm);

                        long startTime = System.currentTimeMillis();

                        Random rndIns = new Random(seedInsert);
                        for (int i = 1; i <= totalInserts; i++) {
                            long key = rndIns.nextInt(1000000);
                            long value = key * 10L;
                            tree.insert(key, value);
                        }

                        tree.unswizzleTree();

                        ExecutorService searchPool = Executors.newFixedThreadPool(numSearchThreads);
                        CountDownLatch searchLatch = new CountDownLatch(numSearchThreads);
                        Random globalRand = new Random(seedSearch);

                        int hotSetSize = 1000; // rozmiar hot setu
                        int hotSetSwitch = 50; // co ile wyszukiwań zmieniamy hoyset

                        for (int t = 0; t < numSearchThreads; t++) {
                            searchPool.submit(() -> {
                                Random rnd = new Random(globalRand.nextLong());
                                int currentHotSetBase = rnd.nextInt(1000000 - hotSetSize);
                                for (int j = 0; j < searchesPerThread; j++) {
                                    if (j % hotSetSwitch == 0) {
                                        currentHotSetBase = rnd.nextInt(1000000 - hotSetSize);
                                    }
                                    long key = currentHotSetBase + rnd.nextInt(hotSetSize);
                                    tree.search(key);
                                }
                                searchLatch.countDown();
                            });
                        }

                        // tutaj bez lokalnosci bylo
//                        for (int t = 0; t < numSearchThreads; t++) {
//                            searchPool.submit(() -> {
//                                Random rnd = new Random(globalRand.nextLong());
//                                for (int j = 0; j < searchesPerThread; j++) {
//                                    long key = rnd.nextInt(1000000);
//                                    tree.search(key);
//                                }
//                                searchLatch.countDown();
//                            });
//                        }
                        searchLatch.await();
                        searchPool.shutdown();

                        long endTime = System.currentTimeMillis();
                        long elapsedMs = endTime - startTime;

                        long hits = bm.getHitCount();
                        long misses = bm.getMissCount();
                        long evictions=bm.getEvictCount();
                        double hitRate = (double) hits / (double) (hits + misses);
                        double missRate=(double) misses/ (hits+misses);
                        double evictionRate= (double)evictions/(hits+misses);

                        allResults.add(new Result(missRate, hitRate, evictionRate, elapsedMs));
                    }

                    double sumHit = 0, sumHitSq = 0;
                    long sumTime = 0;
                    long hits = 0;
                    double sumMiss = 0;
                    double sumEvictons=0;
                    for (Result r : allResults) {
                        sumMiss+=r.misses;
                        sumHit += r.hitRate;
                        sumHitSq += r.hitRate * r.hitRate;
                        sumTime += r.elapsedMs;
                        sumEvictons+=r.evictionRate;
                    }
                    double meanHit = sumHit / repetitions;
                    double varHit = (sumHitSq / repetitions) - (meanHit * meanHit);
                    double stdDevHit = Math.sqrt(Math.max(varHit, 0));
                    double meanMiss=(double)sumMiss/repetitions;
                    double meanEvictions=(double)sumEvictons/repetitions;

                    double meanTime = (double) sumTime / repetitions;
                    double sumTimeSq = 0;
                    for (Result r : allResults) {
                        sumTimeSq += (r.elapsedMs - meanTime) * (r.elapsedMs - meanTime);
                    }
                    double stdDevTime = Math.sqrt(Math.max(sumTimeSq / repetitions, 0));

                    System.out.printf(
                            "%11d| %9d | %7d | %10.4f  |%9.4f |%9.4f | %9.4f %n",
                            maxFrames,
                            bTreeOrder,
                            sumTime,
                            meanHit * 100.0,
                            meanMiss *100.0,
                            meanEvictions*100,
                            meanTime
                    );
                }
            }
        }
    }
}




