// ProportionalAlgorithm.java
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ProportionalAlgorithm extends BaseAlgorithm {
//    public ProportionalAlgorithm(int framesCount, int requestCount, int maxID, int processesCount,
//                                 double ppfPercentage, int zoneCoef,
//                                 double localProbability, int localCount, int localSubset) {
//        super(framesCount, requestCount, maxID, processesCount,
//                ppfPercentage, zoneCoef,
//                localProbability, localCount, localSubset);
//    }

    // Nowy konstruktor
    public ProportionalAlgorithm(int framesCount, int requestCount, int maxID, int processesCount,
                                 double ppfPercentage, double lower, int zoneCoef,
                                 double localProbability, int localCount, int localSubset,
                                 List<Proces> preGeneratedProcesses) {
        super(framesCount, requestCount, maxID, processesCount,
                ppfPercentage, lower, zoneCoef,
                localProbability,  localCount, localSubset, preGeneratedProcesses);
    }

    @Override
    public int execute() {
        int errs = 0;
        // Obliczanie sumy unikalnych stron dla wszystkich procesów
        long totalUniquePagesSum = 0;
        List<Proces> currentProcesses = deepCopyProcesses();
        List<Integer> uniquePagesPerProcess = new ArrayList<>();

        for (Proces p : currentProcesses) {
            Set<Integer> uniquePageIds = new HashSet<>();
            for (Page pageRequest : p.requests) {
                uniquePageIds.add(pageRequest.id);
            }
            uniquePagesPerProcess.add(uniquePageIds.size());
            totalUniquePagesSum += uniquePageIds.size();
        }

        if (totalUniquePagesSum == 0) { // Zabezpieczenie przed dzieleniem przez zero
            // Jeśli nie ma żadnych żądań lub unikalnych stron, przydziel równo lub minimalnie
            int equalShare = Math.max(1, framesCount / (processesCount > 0 ? processesCount : 1));
            for (Proces p : currentProcesses) {
                 errs += lruList(p.requests, equalShare);
            }
            return errs;
        }

        for (int i = 0; i < currentProcesses.size(); i++) {
            Proces p = currentProcesses.get(i);
            int si = uniquePagesPerProcess.get(i);
            // Przydział proporcjonalny do liczby unikalnych stron
            int framesForProcess = (int) Math.round(((double) si / totalUniquePagesSum) * framesCount);
            framesForProcess = Math.max(1, framesForProcess); // Każdy proces dostaje co najmniej 1 ramkę
            errs += lruList(p.requests, framesForProcess);
        }
        return errs;
    }

    @Override
    public String getName() {
        return "Proportional";
    }
}
