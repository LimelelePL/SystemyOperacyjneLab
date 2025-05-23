// ProportionalAlgorithm.java
public class ProportionalAlgorithm extends BaseAlgorithm {
    public ProportionalAlgorithm(int framesCount, int requestCount, int maxID, int processesCount,
                                 double ppfPercentage, int zoneCoef,
                                 double localProbability, int localCount, int localSubset) {
        super(framesCount, requestCount, maxID, processesCount,
                ppfPercentage, zoneCoef,
                localProbability, localCount, localSubset);
    }

    @Override
    public int execute() {
        int errs = 0;
        for (Proces p : deepCopyProcesses()) {
            int sz = p.requests.size() * framesCount / requestCount;
            if (sz == 0) sz = 1;
            errs += lruList(p.requests, sz);
        }
        return errs;
    }

    @Override
    public String getName() {
        return "Proportional";
    }
}
