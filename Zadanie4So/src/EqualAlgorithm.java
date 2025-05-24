// EqualAlgorithm.java
import java.util.*;

public class EqualAlgorithm extends BaseAlgorithm {
    public EqualAlgorithm(int framesCount, int requestCount, int maxID, int processesCount,
                          double ppfPercentage,double lower, int zoneCoef,
                          double localProbability, int localCount, int localSubset) {
        super(framesCount, requestCount, maxID, processesCount,
                ppfPercentage, lower, zoneCoef,
                localProbability, localCount, localSubset);
    }

    // Nowy konstruktor
    public EqualAlgorithm(int framesCount, int requestCount, int maxID, int processesCount,
                          double ppfPercentage, double lower, int zoneCoef,
                          double localProbability, int localCount, int localSubset,
                          List<Proces> preGeneratedProcesses) {
        super(framesCount, requestCount, maxID, processesCount,
                ppfPercentage, lower, zoneCoef,
                localProbability, localCount, localSubset, preGeneratedProcesses);
    }

    @Override
    public int execute() {
        int errs = 0;
        int perProc = framesCount / processesCount;
        for (Proces p : deepCopyProcesses()) {
            errs += lruList(p.requests, perProc);
        }
        return errs;
    }

    @Override
    public String getName() {
        return "Equal";
    }
}

