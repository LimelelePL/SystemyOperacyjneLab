import java.util.*;

public class ZoneModelAlgorithm extends BaseAlgorithm {
//    public ZoneModelAlgorithm(int framesCount, int requestCount, int maxID, int processesCount,
//                              double ppfPercentage, int zoneCoef,
//                              double localProbability, int localCount, int localSubset) {
//        super(framesCount, requestCount, maxID, processesCount,
//                ppfPercentage, zoneCoef,
//                localProbability, localCount, localSubset);
//    }

    // Nowy konstruktor
    public ZoneModelAlgorithm(int framesCount, int requestCount, int maxID, int processesCount,
                              double ppfPercentage, int zoneCoef,
                              double localProbability, int localCount, int localSubset,
                              List<Proces> preGeneratedProcesses) {
        super(framesCount, requestCount, maxID, processesCount,
                ppfPercentage, zoneCoef,
                localProbability, localCount, localSubset, preGeneratedProcesses);
    }

    @Override
    public int execute() {
        int errsTotal = 0;

        List<Proces> copy = deepCopyProcesses();
        boolean[] active = new boolean[processesCount];
        Arrays.fill(active, true);
        
        // Do śledzenia, które procesy były już wcześniej wstrzymane
        boolean[] wasEverSuspended = new boolean[processesCount];

        while (true) {
            int[] wss = new int[processesCount];
            int totalNeeded = 0;

            // Wyznacz WSS tylko dla aktywnych procesów
            for (int i = 0; i < processesCount; i++) {
                if (!active[i]) continue;
                Proces p = copy.get(i);
                int sz = Math.min(zoneCoef, p.requests.size());
                Set<Integer> set = new HashSet<>();
                for (int j = 1; j <= sz; j++) {
                    if (p.requests.size() - j >= 0) {
                        set.add(p.requests.get(p.requests.size() - j).id);
                    }
                }
                wss[i] = Math.max(1, set.size());
                totalNeeded += wss[i];
            }

            if (totalNeeded <= framesCount) {
                // Każdy aktywny proces dostaje tyle ramek ile wynosi jego WSS
                for (int i = 0; i < processesCount; i++) {
                    if (!active[i]) continue;
                    Proces p = copy.get(i);
                    errsTotal += lruList(p.requests, wss[i]);
                }
                break; // skończone, wszystko się mieści
            }

            // Musimy kogoś wstrzymać: wybierz aktywny proces o najmniejszym WSS
            int minWss = Integer.MAX_VALUE, minIdx = -1;
            for (int i = 0; i < processesCount; i++) {
                if (active[i] && wss[i] < minWss) {
                    minWss = wss[i];
                    minIdx = i;
                }
            }
            if (minIdx == -1) break; // nie powinno się zdarzyć

            active[minIdx] = false;
            suspensionCount++;
        }
        return errsTotal;
    }


    @Override
    public String getName() {
        return "ZoneModel";
    }
}
