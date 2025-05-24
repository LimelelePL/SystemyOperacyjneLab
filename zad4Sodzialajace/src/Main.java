import java.util.*;
import java.util.stream.*;

class Process {
    int id, lowRef, maxRef;
    List<Integer> pageRefs;
    int physicalMemorySize;
    List<Integer> physicalMemory = new ArrayList<>();
    int pageFaults = 0;

    public Process(int id, int lowRef, int maxRef) {
        this.id = id;
        this.lowRef = lowRef;
        this.maxRef = maxRef;
        Random rnd = new Random();
        int span = maxRef - lowRef;
        int numRefs = rnd.nextInt(span - (int)Math.ceil(span*0.6) + 1) + (int)Math.ceil(span*0.6);
        pageRefs = IntStream.range(0, numRefs)
                .map(i -> rnd.nextInt(span+1) + lowRef)
                .boxed().collect(Collectors.toList());
    }

    public void setFrames(int f) {
        physicalMemorySize = f;
        physicalMemory = new ArrayList<>(Collections.nCopies(f, -1));
    }
}

class Simulation {
    int numProcesses, numFrames, numPages, timeWindow;
    Random rnd = new Random();

    // zbierane statystyki:
    List<Boolean> faultsTable = new ArrayList<>();
    int totalFaults = 0;
    int abortedOrSuspended = 0;

    public Simulation(int numProcesses, int numFrames, int numPages, int timeWindow) {
        this.numProcesses = numProcesses;
        this.numFrames = numFrames;
        this.numPages = numPages;
        this.timeWindow = timeWindow;
    }

    private List<Process> genProcesses(int howMany) {
        List<Process> list = new ArrayList<>();
        int slice = numPages / howMany;
        for(int i=0; i<howMany; i++) {
            int low = i*slice;
            int high = (i==howMany-1 ? numPages : (i+1)*slice);
            list.add(new Process(i, low, high));
        }
        return list;
    }

    private void runLRU(Process p) {
        LinkedList<Integer> useOrder = new LinkedList<>();
        for(int pg : p.pageRefs) {
            boolean fault = !p.physicalMemory.contains(pg);
            faultsTable.add(fault);
            if (fault) {
                totalFaults++;
                p.pageFaults++;
                if (useOrder.size() < p.physicalMemorySize) {
                    // załaduj
                    int idx = useOrder.size();
                    p.physicalMemory.set(idx, pg);
                    useOrder.add(idx);
                } else {
                    int victim = useOrder.removeFirst();
                    p.physicalMemory.set(victim, pg);
                    useOrder.addLast(victim);
                }
            } else {
                // hit: przenieś w kolejności
                int idxInMem = p.physicalMemory.indexOf(pg);
                useOrder.remove((Integer)idxInMem);
                useOrder.addLast(idxInMem);
            }
        }
    }

    private int countThrashing(List<Boolean> table) {
        int window = timeWindow;
        int thr = 0;
        for(int i=0; i<table.size(); i+=window) {
            long faults = table.subList(i, Math.min(i+window, table.size())).stream()
                    .filter(b->b).count();
            if (faults > 0.7*window) thr++;
        }
        return thr;
    }

    public Map<String, Number> equalAllocation() {
        faultsTable.clear(); totalFaults = 0; abortedOrSuspended = 0;
        int perProcFrames = numFrames / numProcesses;
        for(Process p : genProcesses(numProcesses)) {
            p.setFrames(perProcFrames);
            runLRU(p);
        }
        double fpp = totalFaults / (double)numProcesses;
        return Map.of(
                "faultsPerProcess", fpp,
                "thrashing", countThrashing(faultsTable),
                "aborted", abortedOrSuspended
        );
    }

    public Map<String, Number> proportionalAllocation() {
        faultsTable.clear(); totalFaults = 0; abortedOrSuspended = 0;
        List<Process> procs = genProcesses(numProcesses);
        for(Process p : procs) {
            int f = (int)Math.ceil(p.pageRefs.size() / (double)procs.size());
            p.setFrames(f);
            runLRU(p);
        }
        double fpp = totalFaults / (double)numProcesses;
        return Map.of(
                "faultsPerProcess", fpp,
                "thrashing", countThrashing(faultsTable),
                "aborted", abortedOrSuspended
        );
    }

    public Map<String, Number> pageFaultFreqControl(int low, int up, int high) {
        faultsTable.clear(); totalFaults = 0; abortedOrSuspended = 0;
        List<Process> procs = genProcesses(numProcesses);
        for(Process p : procs) {
            p.setFrames(numFrames / numProcesses);
            LinkedList<Integer> useOrder = new LinkedList<>();
            int faults=0;
            for(int pg : p.pageRefs) {
                boolean fault = !p.physicalMemory.contains(pg);
                faultsTable.add(fault);
                if (fault) { faults++; totalFaults++; }
                // adaptacja rozmiaru
                double ppf = faults / (double)timeWindow;
                if (ppf > high) {
                    abortedOrSuspended++;
                    break;
                } else if (ppf > up) {
                    p.physicalMemorySize++;
                } else if (ppf < low && p.physicalMemorySize>1) {
                    p.physicalMemorySize--;
                }
            }
        }
        double fpp = totalFaults / (double)numProcesses;
        return Map.of(
                "faultsPerProcess", fpp,
                "thrashing", countThrashing(faultsTable),
                "aborted", abortedOrSuspended
        );
    }

    public Map<String, Number> workingSetModel() {
        faultsTable.clear(); totalFaults = 0; abortedOrSuspended = 0;
        List<Process> procs = genProcesses(numProcesses);
        int avail = numFrames;
        // ustaw początkowo WSS
        for(Process p: procs) {
            int wss = new HashSet<>(p.pageRefs).size();
            p.setFrames(wss);
            avail -= wss;
        }
        // prościej: przydziel proporcjonalnie jeśli zmieści się
        if (procs.stream().mapToInt(pr->pr.physicalMemorySize).sum() <= numFrames) {
            for(Process p: procs) runLRU(p);
        } else {
            // zawieszenie największego
            Process maxP = Collections.max(procs, Comparator.comparingInt(pr->pr.physicalMemorySize));
            abortedOrSuspended = 1;
            procs.remove(maxP);
            for(Process p: procs) runLRU(p);
        }
        double fpp = totalFaults / (double)numProcesses;
        return Map.of(
                "faultsPerProcess", fpp,
                "thrashing", countThrashing(faultsTable),
                "aborted", abortedOrSuspended
        );
    }
}

public class Main {
    public static void main(String[] args) {
        int numPages = 2000, numFrames = 200, numProcs = 1000, window = 800;
        Simulation sim = new Simulation(numProcs, numFrames, numPages, window);

        List<String> algs = List.of(
                "Equal Allocation",
                "Proportional Allocation",
                "Page Fault Freq Control",
                "Working Set Model"
        );
        List<Map<String, Number>> results = List.of(
                sim.equalAllocation(),
                sim.proportionalAllocation(),
                sim.pageFaultFreqControl(2, 6, 10),
                sim.workingSetModel()
        );

        // Nagłówek tabeli
        System.out.printf("%-30s%20s%20s%20s%n",
                "Algorytm","PageFaults/Proces","Szamotan","Przerwane");

        for(int i=0; i<algs.size(); i++) {
            Map<String, Number> r = results.get(i);
            System.out.printf("%-30s%20.2f%20d%20d%n",
                    algs.get(i),
                    r.get("faultsPerProcess").doubleValue(),
                    r.get("thrashing").intValue(),
                    r.get("aborted").intValue()
            );
        }
    }
}
