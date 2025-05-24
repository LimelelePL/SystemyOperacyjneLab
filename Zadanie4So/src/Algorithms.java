//// Algorithms.java
//import java.util.*;
//
//public class Algorithms {
//    private final int framesCount;
//    private final int requestCount;
//    private final int processesCount;
//    private final int maxID;
//    private final double ppfPercentage;
//    private final int zoneCoef;
//    private final double localProbability;
//    private final int localCount;
//    private final int localSubset;
//
//    private final List<Proces> processes = new ArrayList<>();
//    private final List<Page> requests = new ArrayList<>();
//    private final Random rnd = new Random();
//
//    public Algorithms(int framesCount, int requestCount, int maxID,
//                      int processesCount, double ppfPercentage,
//                      int zoneCoef, double localProbability,
//                      int localCount, int localSubset) {
//        this.framesCount = framesCount;
//        this.requestCount = requestCount;
//        this.maxID = maxID;
//        this.processesCount = processesCount;
//        this.ppfPercentage = ppfPercentage;
//        this.zoneCoef = zoneCoef;
//        this.localProbability = localProbability;
//        this.localCount = localCount;
//        this.localSubset = localSubset;
//        generate();
//    }
//
//    private void generate() {
//        // 1) Generowanie ciągu zapytań
//        for (int i = 0; i < requestCount; i++) {
//            if (rnd.nextDouble() < localProbability) {
//                int proc = rnd.nextInt(processesCount);
//                int subsetCount = rnd.nextInt(localSubset) + 1;
//                List<Integer> subset = new ArrayList<>();
//                for (int j = 0; j < subsetCount; j++)
//                    subset.add(rnd.nextInt(maxID + 1));
//                int locCnt = rnd.nextInt(localCount + 1);
//                for (int k = 0; k < locCnt; k++) {
//                    int pageId = subset.get(rnd.nextInt(subset.size()));
//                    requests.add(new Page(pageId, 0, proc));
//                }
//            }
//            int proc = rnd.nextInt(processesCount);
//            int id = rnd.nextInt(maxID + 1);
//            requests.add(new Page(id, 0, proc));
//        }
//        // 2) Podział na Procesy
//        for (int i = 0; i < processesCount; i++) {
//            List<Page> reqsForProc = new ArrayList<>();
//            for (Page p : requests)
//                if (p.processID == i)
//                    reqsForProc.add(p);
//            processes.add(new Proces(reqsForProc, 0));
//        }
//    }
//
//    /** Równo dzielone ramki między procesy */
//    public int equal() {
//        int errs = 0;
//        int perProc = framesCount / processesCount;
//        for (Proces p : deepCopyProcesses()) {
//            errs += lruList(p.requests, perProc);
//        }
//        return errs;
//    }
//
//    /** Proporcjonalnie do wielkości żądań */
//    public int proportional() {
//        int errs = 0;
//        for (Proces p : deepCopyProcesses()) {
//            int sz = p.requests.size() * framesCount / requestCount;
//            if (sz == 0) sz = 1;  // żeby każdy miał chociaż 1 ramkę
//            errs += lruList(p.requests, sz);
//        }
//        return errs;
//    }
//
//    /** Steering Page Fault Frequency */
//    public int steeringFaultFrequency() {
//        int errsTotal = 0;
//        int errorMax = (int)(ppfPercentage * requestCount);
//        // początkowy przydział
//        int perProc = framesCount / processesCount;
//        List<Proces> copy = deepCopyProcesses();
//        for (Proces p : copy) p.framesCount = perProc;
//        int freeFrames = 0, alive = processesCount;
//
//        while (alive > 0) {
//            int bestIdx = -1, worstIdx = -1;
//            double minCoef = Double.MAX_VALUE, maxCoef = -1;
//
//            // przejście po aktywnych
//            for (int i = 0; i < copy.size(); i++) {
//                Proces p = copy.get(i);
//                if (p == null) continue;
//                if (!p.requests.isEmpty()) {
//                    int e = p.LRU(Collections.singletonList(p.requests.remove(0)));
//                    errsTotal += e;
//                    if (p.errorsCoef < minCoef) {
//                        minCoef = p.errorsCoef; bestIdx = i;
//                    }
//                    if (p.errorsCoef > maxCoef) {
//                        maxCoef = p.errorsCoef; worstIdx = i;
//                    }
//                } else {
//                    // proces skończył – uwolnij ramki
//                    if (worstIdx != -1 && worstIdx != i && copy.get(worstIdx) != null) {
//                        copy.get(worstIdx).framesCount += p.framesCount;
//                    } else {
//                        freeFrames += p.framesCount;
//                    }
//                    copy.set(i, null);
//                    alive--;
//                }
//            }
//            // jeżeli przekroczyliśmy próg – zabierz 1 ramkę od najlepszego, dodaj do najgorszego
//            if (bestIdx >= 0 && worstIdx >= 0
//                    && copy.get(bestIdx).framesCount > 1
//                    && maxCoef > errorMax) {
//                copy.get(bestIdx).framesCount--;
//                copy.get(worstIdx).framesCount += 1 + freeFrames;
//                freeFrames = 0;
//            }
//        }
//        return errsTotal;
//    }
//
//    /** Zone Model */
//    public int zone() {
//        int errsTotal = 0;
//        int freeFrames = framesCount;
//        List<Proces> copy = deepCopyProcesses();
//        // początkowy, proporcjonalny
//        for (Proces p : copy)
//            p.framesCount = Math.max(1, p.requests.size() * framesCount / requestCount);
//
//        int finished = -1;
//        // pierwsze przejście
//        for (int k = 0; k < copy.size(); k++) {
//            Proces p = copy.get(k);
//            if (freeFrames >= p.framesCount) {
//                freeFrames -= p.framesCount;
//                errsTotal += lruList(p.requests, p.framesCount);
//                finished = k;
//            }
//        }
//        freeFrames = framesCount;
//
//        while (finished < processesCount - 1) {
//            // przelicz WS dla każego
//            for (Proces p : copy) {
//                int ws = workingSetSize(p.requests, zoneCoef);
//                p.framesCount = ws > 0 ? ws : 1;
//            }
//            // przydział dla kolejnych
//            for (int k = finished + 1; k < copy.size(); k++) {
//                Proces p = copy.get(k);
//                if (freeFrames >= p.framesCount) {
//                    freeFrames -= p.framesCount;
//                    errsTotal += lruList(p.requests, p.framesCount);
//                    finished = k;
//                }
//            }
//            freeFrames = framesCount;
//        }
//        return errsTotal;
//    }
//
//    // pomocnicze
//
//    private List<Proces> deepCopyProcesses() {
//        List<Proces> cp = new ArrayList<>();
//        for (Proces p : processes) {
//            // kopiujemy listę stron, ale ramki i błędy czyścimy
//            cp.add(new Proces(new ArrayList<>(p.requests), 0));
//        }
//        return cp;
//    }
//
//    private int lruList(List<Page> pagesRef, int frameSize) {
//        int errs = 0;
//        List<Page> frames = new ArrayList<>();
//        for (Page req : pagesRef) {
//            boolean hit = false;
//            for (Page f : frames) {
//                if (f.id == req.id) {
//                    f.lastUsed++;
//                    hit = true; break;
//                }
//            }
//            if (!hit) {
//                errs++;
//                if (frames.size() < frameSize) {
//                    frames.add(new Page(req.id, req.lastUsed, req.processID));
//                } else {
//                    frames.sort(Comparator.comparingInt(p -> p.lastUsed));
//                    frames.remove(0);
//                    frames.add(new Page(req.id, req.lastUsed, req.processID));
//                }
//            }
//        }
//        return errs;
//    }
//
//    private int workingSetSize(List<Page> a, int zone) {
//        Set<Integer> h = new HashSet<>();
//        int sz = Math.min(zone, a.size());
//        for (int i = 1; i <= sz; i++) {
//            h.add(a.get(a.size() - i).id);
//        }
//        return h.size();
//    }
//}


