import java.util.*;

public class SteeringPffAlgorithm extends BaseAlgorithm {
    public SteeringPffAlgorithm(int framesCount, int requestCount, int maxID, int processesCount,
                                double ppfPercentage, int zoneCoef,
                                double localProbability, int localCount, int localSubset) {
        super(framesCount, requestCount, maxID, processesCount,
                ppfPercentage, zoneCoef,
                localProbability, localCount, localSubset);
    }

    // Parametry algorytmu
    private static final int DELTA_T = 50; // Rozmiar okna dla PPF
    private static final double LOWER = 0.01; // Dolny próg PPF
    private static final int MIN_FRAMES = 1; // Minimalna liczba ramek dla procesu
    
    // Licznik wznowień procesów
    private int resumeCount = 0;
    

    @Override
    public int execute() {
        int errsTotal = 0;
        final double UPPER = ppfPercentage; // Górny próg PPF (z parametru)
        int perProc = Math.max(MIN_FRAMES, framesCount / processesCount);

        List<Proces> copy = deepCopyProcesses();

        int[] frames = new int[copy.size()];
        Arrays.fill(frames, perProc);

        int freeFrames = framesCount - perProc * copy.size();

        // Flagi procesu
        boolean[] finished = new boolean[copy.size()];
        boolean[] suspended = new boolean[copy.size()];

        // Okna błędów stron
        Queue<Boolean>[] pageFaultWindow = new LinkedList[copy.size()];
        for (int i = 0; i < copy.size(); i++) pageFaultWindow[i] = new LinkedList<>();

        // Ramki procesu
        List<Page>[] framesByProc = new ArrayList[copy.size()];
        for (int i = 0; i < copy.size(); i++) framesByProc[i] = new ArrayList<>();

        // Szamotanina (thrashing) – niedawno usunięte strony
        Queue<Integer>[] recentlyEvicted = new LinkedList[copy.size()];
        Set<Integer>[] evictedPageSets = new HashSet[copy.size()];
        for (int i = 0; i < copy.size(); i++) {
            recentlyEvicted[i] = new LinkedList<>();
            evictedPageSets[i] = new HashSet<>();
        }

        int[] idx = new int[copy.size()]; // indeks aktualnego żądania

        int processesAlive = copy.size();

        while (processesAlive > 0) {
            // Spróbuj odwiesić zawieszone procesy, jeśli jest wolna pula ramek
            for (int pid = 0; pid < copy.size(); pid++) {
                if (suspended[pid] && freeFrames >= perProc) {
                    suspended[pid] = false;
                    freeFrames -= perProc;
                    frames[pid] = perProc; // Odwieszenie – przydziel minimum ramek
                    resumeCount++;
                    // Nie inkrementuj processesAlive – nie był odjęty!
                }
            }

            for (int pid = 0; pid < copy.size(); pid++) {
                if (finished[pid] || suspended[pid]) continue; // pomijamy zakończone i wstrzymane

                Proces p = copy.get(pid);
                List<Page> framesList = framesByProc[pid];
                int frameCount = frames[pid];

                // Struktury do szamotaniny
                Queue<Integer> evictedQueue = recentlyEvicted[pid];
                Set<Integer> evictedSet = evictedPageSets[pid];

                int localSteps = 0;
                int localFaults = 0;

                // Symulujemy okno DELTA_T żądań
                while (idx[pid] < p.requests.size() && localSteps < DELTA_T) {
                    Page req = p.requests.get(idx[pid]);
                    boolean hit = false;
                    for (Page f : framesList) {
                        if (f.id == req.id) {
                            f.lastUsed++;
                            hit = true;
                            break;
                        }
                    }
                    // Szamotanina: strona była niedawno usunięta
                    if (!hit && evictedSet.contains(req.id)) {
                        thrashingCount++;
                    }
                    if (!hit) {
                        localFaults++;
                        errsTotal++;
                        if (framesList.size() < frameCount) {
                            framesList.add(new Page(req.id, 0, req.processID));
                        } else {
                            // LRU out
                            framesList.sort(Comparator.comparingInt(x -> x.lastUsed));
                            int evictedPageId = framesList.get(0).id;
                            evictedQueue.add(evictedPageId);
                            evictedSet.add(evictedPageId);

                            if (evictedQueue.size() > THRASHING_WINDOW) {
                                int oldPageId = evictedQueue.poll();
                                if (!evictedQueue.contains(oldPageId)) {
                                    evictedSet.remove(oldPageId);
                                }
                            }
                            framesList.remove(0);
                            framesList.add(new Page(req.id, 0, req.processID));
                        }
                    }
                    idx[pid]++;
                    localSteps++;
                }
                // Zaktualizuj okno błędów stron
                for (int i = 0; i < localSteps; i++) {
                    boolean wasFault = (i < localFaults);
                    pageFaultWindow[pid].add(wasFault);
                    if (pageFaultWindow[pid].size() > DELTA_T) pageFaultWindow[pid].poll();
                }

                // Proces skończył wszystkie żądania
                if (idx[pid] >= p.requests.size()) {
                    freeFrames += frames[pid];
                    finished[pid] = true;
                    processesAlive--;
                    continue;
                }

                // Licz PPF
                int faultsInWindow = 0;
                for (boolean fault : pageFaultWindow[pid]) if (fault) faultsInWindow++;
                double ppf = pageFaultWindow[pid].isEmpty() ? 0.0 : ((double)faultsInWindow / pageFaultWindow[pid].size());

                // Sterowanie ramkami
                if (ppf > UPPER) {
                    if (freeFrames > 0) {
                        frames[pid]++;
                        freeFrames--;
                    } else {
                        // WSTRZYMAJ PROCES (pauza)
                        suspended[pid] = true;
                        suspensionCount++; // używamy pola z klasy bazowej
                        freeFrames += frames[pid]; // oddaj jego ramki
                        // UWAGA: nie zmniejszaj processesAlive!
                        continue;
                    }
                } else if (ppf < LOWER && frames[pid] > MIN_FRAMES) {
                    frames[pid]--;
                    freeFrames++;
                }
            }
        }

        return errsTotal;
    }

    @Override
    public String getName() {
        return "SteeringPFF";
    }
}
