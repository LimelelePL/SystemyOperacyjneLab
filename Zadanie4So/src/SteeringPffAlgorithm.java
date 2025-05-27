import java.util.*;

public class SteeringPffAlgorithm extends BaseAlgorithm {
    private static final int DELTA_T = 50;

    public SteeringPffAlgorithm(int framesCount, int requestCount, int maxID, int processesCount,
                                double ppfPercentage, double lower, int zoneCoef,
                                double localProbability, int localCount, int localSubset,
                                List<Proces> preGeneratedProcesses) {
        super(framesCount, requestCount, maxID, processesCount,
                ppfPercentage, lower, zoneCoef,
                localProbability, localCount, localSubset, preGeneratedProcesses);
    }

    @Override
    public int execute() {
        int errsTotal = 0;

        List<Proces> copy = deepCopyProcesses();
        // Tablica z liczbą ramek przydzielonych każdemu procesowi do dynamicznego zarządzania
        int[] frames = new int[copy.size()];

        // ile unikalnych stron potrzebuje każdy proces żeby przydzielić ramki proporcjonalnie
        int totalUniquePagesSum = 0;
        List<Integer> uniquePagesPerProcess = new ArrayList<>();
        for (Proces p : copy) {
            Set<Integer> uniquePageIds = new HashSet<>();
            for (Page pageRequest : p.requests) {
                uniquePageIds.add(pageRequest.id);
            }
            uniquePagesPerProcess.add(uniquePageIds.size());
            totalUniquePagesSum += uniquePageIds.size();
        }

        int allocatedFramesSum = 0;

        // Jeśli wszystkie procesy mają 0 unikalnych stron dzielę ramki po równo (raczej sie nie zdarzy)
        if (totalUniquePagesSum == 0) {
            int equalShare = Math.max(1, framesCount / (!copy.isEmpty() ? copy.size() : 1));
            for (int i = 0; i < copy.size(); i++) {
                frames[i] = equalShare;
                allocatedFramesSum += equalShare;
            }
        } else {
            // Przydzielam ramki proporcjonalnie
            for (int i = 0; i < copy.size(); i++) {
                int si = uniquePagesPerProcess.get(i);
                int framesForProcess = (int) Math.round(((double) si / totalUniquePagesSum) * framesCount);
                frames[i] = Math.max(1, framesForProcess); // każdy musi mieć przynajmniej 1 ramkę
                allocatedFramesSum += frames[i];
            }
        }

        // Sprawdzam czy zostały jakieś wolne ramki albo przydzieliłem za dużo
        int freeFrames = framesCount - allocatedFramesSum;

        // Jeśli zostały wolne ramki  rozdaję je po jednej kolejnym procesom
        if (freeFrames > 0) {
            for (int i = 0; i < copy.size() && freeFrames > 0; i = (i + 1) % copy.size()) {
                frames[i]++;
                freeFrames--;
            }
        }
        // Jeśli przydzieliłem za dużo odbieram po jednej, dopóki się nie zgadza
        else if (freeFrames < 0) {
            for (int i = 0; i < copy.size() && freeFrames < 0; i = (i + 1) % copy.size()) {
                if (frames[i] > 1) {
                    frames[i]--;
                    freeFrames++;
                }
            }
        }

        //czy zakończony i czy zawieszony żeby wiedzieć, które działają
        boolean[] finished = new boolean[copy.size()];
        boolean[] suspended = new boolean[copy.size()];

        //  dla każdego procesu kolejkę true/false do liczenia PPF
        Queue<Boolean>[] pageFaultWindow = new LinkedList[copy.size()];
        for (int i = 0; i < copy.size(); i++) {
            pageFaultWindow[i] = new LinkedList<>();
        }

        // Lista stron w ramkach dla każdego procesu czyli co ma aktualnie załadowane
        List<Page>[] framesByProc = new ArrayList[copy.size()];
        for (int i = 0; i < copy.size(); i++) {
            framesByProc[i] = new ArrayList<>();
        }

        // Bufory do wykrywania szamotania do zpisywania  usuniętych stron
        Queue<Integer>[] recentlyEvicted = new LinkedList[copy.size()];
        Set<Integer>[] evictedPageSets = new HashSet[copy.size()];
        for (int i = 0; i < copy.size(); i++) {
            recentlyEvicted[i] = new LinkedList<>();
            evictedPageSets[i] = new HashSet<>();
        }

        int[] idx = new int[copy.size()]; // który indeks żądania stron jest aktualny dla każdego procesu
        int processesAlive = copy.size(); // ile procesów jeszcze działa

        while (processesAlive > 0) {

            // Sprawdzam, czy można odwiesić zawieszony proces jesli są wolne ramki
            for (int pid = 0; pid < copy.size(); pid++) {
                if (suspended[pid] && freeFrames >= frames[pid]) {
                    suspended[pid] = false;
                    freeFrames -= frames[pid];
                }
            }

            // Dla każdego procesu wykonuję żądania
            for (int pid = 0; pid < copy.size(); pid++) {
                if (finished[pid] || suspended[pid]) continue; // o ile nie jest zawieszony lub zakończony

                Proces p = copy.get(pid);
                List<Page> framesList = framesByProc[pid];
                int frameCount = frames[pid];

                Queue<Integer> evictedQueue = recentlyEvicted[pid];
                Set<Integer> evictedSet = evictedPageSets[pid];

                int localSteps = 0;
                int localFaults = 0;

                // obslugujemy DELTA_T żądań stron dla danego procesu
                while (idx[pid] < p.requests.size() && localSteps < DELTA_T) {
                    Page req = p.requests.get(idx[pid]);
                    boolean hit = false;

                    // Sprawdzam, czy strona już jest w ramkach – jeśli tak, to trafienie hit
                    for (Page f : framesList) {
                        if (f.id == req.id) {
                            f.lastUsed++; // Zwiększam licznik użycia (do LRU)
                            hit = true;
                            break;
                        }
                    }

                    //szamotanie
                    if (!hit && evictedSet.contains(req.id) && pageFaultWindow[pid].size() > 10) {
                        int recentFaults = 0;
                        for (boolean f : pageFaultWindow[pid]) if (f) recentFaults++;
                        if ((double)recentFaults / pageFaultWindow[pid].size() > 0.8) {
                            thrashingCount++;
                        }
                    }

                    // Jeśli strona nie była trafieniem  błąd strony
                    if (!hit) {
                        localFaults++;
                        errsTotal++;

                        // Jeśli mamy jeszcze miejsce – dodajemy stronę
                        if (framesList.size() < frameCount) {
                            framesList.add(new Page(req.id, 0, req.processID));
                        } else {
                            // Ramki pełne – usuwam stronę najmniej używaną (LRU) i dodaję nową
                            framesList.sort(Comparator.comparingInt(x -> x.lastUsed));
                            int evictedPageId = framesList.get(0).id;
                            evictedQueue.add(evictedPageId);
                            evictedSet.add(evictedPageId);

                            // Usuwam stare wpisy z bufora – żeby kontrolować rozmiar
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

                // Zapisuję czy wystąpiły błędy stron do kolejki PPF  żeby później obliczyć częstotliwość
                for (int i = 0; i < localSteps; i++) {
                    boolean wasFault = (i < localFaults);
                    pageFaultWindow[pid].add(wasFault);
                    if (pageFaultWindow[pid].size() > DELTA_T) pageFaultWindow[pid].poll(); // usuwam stare wpisy
                }

                // Jeśli proces skończył wszystkie żądania  oznaczam go jako zakończony i zwalniam ramki
                if (idx[pid] >= p.requests.size()) {
                    freeFrames += frames[pid];
                    finished[pid] = true;
                    processesAlive--;
                    continue;
                }

                // Liczę PPF – czyli ile błędów stron było w ostatnim oknie
                int faultsInWindow = 0;
                for (boolean fault : pageFaultWindow[pid]) {
                    if (fault) faultsInWindow++;
                }
                double ppf = pageFaultWindow[pid].isEmpty() ? 0.0 : ((double)faultsInWindow / pageFaultWindow[pid].size());

                // Decyzja: co zrobić z przydziałem ramek na podstawie PPF

                if (ppf > upper) {
                    // PPF za wysokie – dodajemy ramkę, jeśli są wolne
                    if (freeFrames > 0) {
                        frames[pid]++;
                        freeFrames--;
                    } else {
                        // Nie ma ramek – zawieszamy proces i oddajemy jego ramki
                        suspended[pid] = true;
                        suspensionCount++;
                        freeFrames += frames[pid];
                        continue;
                    }
                } else if (ppf < lower && frames[pid] > 1) {
                    // PPF bardzo niskie – proces marnuje pamięć, odbieramy jedną ramkę
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

