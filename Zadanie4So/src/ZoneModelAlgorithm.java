import java.util.*;

public class ZoneModelAlgorithm extends BaseAlgorithm {

    // Istniejący konstruktor z preGeneratedProcesses
    public ZoneModelAlgorithm(int framesCount, int requestCount, int maxID, int processesCount,
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

        // Parametry algorytmu
        final int deltaT = zoneCoef;        // Rozmiar okna dla WSS (delta t)
        final int c = Math.max(1, deltaT/2);
        // Częstość przeliczania WSS (c < delta_t)

        List<Proces> processes = deepCopyProcesses();

        // Struktury danych potrzebne do symulacji
        int[] currentIndex = new int[processesCount];     // Indeks aktualnego żądania dla każdego procesu
        boolean[] active = new boolean[processesCount];   // Aktywne procesy
        boolean[] suspended = new boolean[processesCount]; // Wstrzymane procesy
        boolean[] finished = new boolean[processesCount]; // Zakończone procesy
        int[] allocatedFrames = new int[processesCount];  // Przydzielone ramki dla każdego procesu
        int[] processWss = new int[processesCount];       // Przechowuje ostatni obliczony WSS dla każdego procesu

        // Inicjalizacja - wszystkie procesy aktywne i mają minimum 1 ramkę
        Arrays.fill(active, true);
        for (int i = 0; i < processesCount; i++) {
            allocatedFrames[i] = 1;
            processWss[i] = 1; // Początkowe WSS = 1 (minimum)
        }

        // Bufor ostatnich deltaT żądań dla każdego procesu (do obliczania WSS)
        List<Integer>[] recentRequests = new ArrayList[processesCount];
        for (int i = 0; i < processesCount; i++) {
            recentRequests[i] = new ArrayList<>();
        }

        // Struktury dla szamotania
        Queue<Integer>[] recentlyEvicted = new LinkedList[processesCount];
        Set<Integer>[] evictedPageSets = new HashSet[processesCount];
        for (int i = 0; i < processesCount; i++) {
            recentlyEvicted[i] = new LinkedList<>();
            evictedPageSets[i] = new HashSet<>();
        }

        // Struktury dla ramek pamięci
        Map<Integer, List<Page>> framesByProcess = new HashMap<>();

        // Główna pętla symulacji

        boolean allProcessesDone = false;

        while (true) {

            // Sprawdź, czy możemy wznowić wstrzymane procesy
            int availableFrames = calculateAvailableFrames(allocatedFrames);
            for (int pid = 0; pid < processesCount; pid++) {
                if (suspended[pid] && !finished[pid]) {
                    if (availableFrames > 0) {
                        int allocated = Math.min(processWss[pid], availableFrames);
                        suspended[pid] = false;
                        active[pid] = true;
                        allocatedFrames[pid] = allocated;
                        availableFrames -= allocated;
                    }
                }
            }

            // Krok 1: Wykonaj c żądań dla każdego aktywnego procesu
            for (int pid = 0; pid < processesCount; pid++) {
                if (!active[pid] || finished[pid]) continue;

                Proces process = processes.get(pid);

                // Lista ramek dla procesu
                if (!framesByProcess.containsKey(pid)) {
                    framesByProcess.put(pid, new ArrayList<>());
                }
                List<Page> frames = framesByProcess.get(pid);

                // Pobierz struktury szamotania
                Queue<Integer> evictedQueue = recentlyEvicted[pid];
                Set<Integer> evictedSet = evictedPageSets[pid];

                // Wykonaj c żądań lub dopóki proces ma żądania
                int requestsProcessed = 0;
                while (requestsProcessed < c && currentIndex[pid] < process.requests.size()) {



                    Page request = process.requests.get(currentIndex[pid]);

                    // Dodaj to żądanie do bufora ostatnich deltaT żądań
                    recentRequests[pid].add(request.id);
                    if (recentRequests[pid].size() > deltaT) {
                        recentRequests[pid].remove(0);
                    }

                    // Symulujemy odwołanie do strony
                    boolean hit = false;
                    for (Page frame : frames) {
                        if (frame.id == request.id) {
                            frame.lastUsed++;
                            hit = true;
                            break;
                        }
                    }

                    // Sprawdź czy to szamotanie
                    if (!hit && evictedSet.contains(request.id)) {
                        thrashingCount++;
                    }

                    if (!hit) {
                        errsTotal++;

                        // Obsługa błędu strony
                        if (frames.size() < allocatedFrames[pid]) {
                            frames.add(new Page(request.id, 0, pid));
                        } else if (allocatedFrames[pid] > 0) {
                            // LRU - usuń najmniej ostatnio używaną stronę
                            frames.sort(Comparator.comparingInt(p -> p.lastUsed));
                            int evictedPageId = frames.get(0).id;
                            frames.remove(0);

                            // Dodaj usuniętą stronę do bufora
                            evictedQueue.add(evictedPageId);
                            evictedSet.add(evictedPageId);

                            if (evictedQueue.size() > THRASHING_WINDOW) {
                                int oldPageId = evictedQueue.poll();
                                if (!evictedQueue.contains(oldPageId)) {
                                    evictedSet.remove(oldPageId);
                                }
                            }

                            frames.add(new Page(request.id, 0, pid));
                        }
                    }
                    currentIndex[pid]++;
                    requestsProcessed++;

                }


                // Sprawdź, czy proces zakończył wszystkie żądania
                if (currentIndex[pid] >= process.requests.size()) {
                    active[pid] = false;
                    finished[pid] = true;
                    // Zwolnij ramki procesu
                    allocatedFrames[pid] = 0;
                    framesByProcess.remove(pid);
                }
            }

            // Krok 2: Oblicz WSS dla każdego procesu
            int totalWSS = 0;
            for (int pid = 0; pid < processesCount; pid++) {
                if (finished[pid]) continue;

                // Oblicz WSS tylko jeśli mamy wystarczająco danych
                if (!recentRequests[pid].isEmpty()) {
                    Set<Integer> uniquePages = new HashSet<>(recentRequests[pid]);
                    processWss[pid] = Math.max(1, uniquePages.size());
                }

                if (active[pid]) {
                    totalWSS += processWss[pid];
                }
            }

            // Sprawdź, czy wszystkie procesy zakończyły pracę
            allProcessesDone = true;
            for (int i = 0; i < processesCount; i++) {
                if (!finished[i]) {
                    allProcessesDone = false;
                    break;
                }
            }
            if (allProcessesDone) break;

            // Krok 3: Sprawdź, czy suma WSS przekracza dostępne ramki
            if (totalWSS <= framesCount) {
                // Jeśli wystarczy ramek, każdy proces dostaje tyle, ile wynosi jego WSS
                for (int pid = 0; pid < processesCount; pid++) {
                    if (active[pid] && !finished[pid]) {
                        allocatedFrames[pid] = processWss[pid];
                    }
                }
            } else {
                // Jeśli brakuje ramek, wstrzymaj proces o największym WSS
                int maxWssPid = -1;
                int maxWssValue = -1;

                for (int pid = 0; pid < processesCount; pid++) {
                    if (active[pid] && !finished[pid] && processWss[pid] > maxWssValue) {
                        maxWssValue = processWss[pid];
                        maxWssPid = pid;
                    }
                }

                if (maxWssPid != -1) {
                    // Wstrzymaj proces o największym WSS
                    active[maxWssPid] = false;
                    suspended[maxWssPid] = true;
                    suspensionCount++;

                    // Zwolnij ramki tego procesu
                    int freedFrames = allocatedFrames[maxWssPid];
                    allocatedFrames[maxWssPid] = 0;

                    // Redystrybuuj zwolnione ramki
                    if (totalWSS > maxWssValue) { // Zapobiega dzieleniu przez zero
                        totalWSS -= maxWssValue;
                        redistributeFrames(allocatedFrames, processWss, active, finished, freedFrames, totalWSS);
                    } else {
                        // Jeśli został tylko jeden proces, daj mu wszystkie zwolnione ramki
                        for (int pid = 0; pid < processesCount; pid++) {
                            if (active[pid] && !finished[pid]) {
                                allocatedFrames[pid] += freedFrames;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return errsTotal;
    }

    /**
     * Oblicza liczbę dostępnych (wolnych) ramek pamięci
     */
    private int calculateAvailableFrames(int[] allocatedFrames) {
        int usedFrames = 0;
        for (int frames : allocatedFrames) {
            usedFrames += frames;
        }
        return Math.max(0, framesCount - usedFrames); // Zabezpieczenie przed ujemną wartością
    }

    /**
     * Redystrybuuje ramki między aktywne procesy proporcjonalnie do ich WSS
     */
    private void redistributeFrames(int[] allocatedFrames, int[] processWss, boolean[] active,
                                   boolean[] finished, int availableFrames, int totalWSS) {
        // Zabezpieczenie przed dzieleniem przez zero
        if (totalWSS <= 0 || availableFrames <= 0) return;

        // Najpierw przydziel proporcjonalnie według WSS
        int remainingFrames = availableFrames;
        int framesDistributed = 0;

        for (int pid = 0; pid < allocatedFrames.length; pid++) {
            if (active[pid] && !finished[pid]) {
                // Oblicz dodatkowe ramki proporcjonalnie do WSS
                int additionalFrames = (int)Math.floor(((double)processWss[pid] / totalWSS) * availableFrames);
                allocatedFrames[pid] += additionalFrames;
                framesDistributed += additionalFrames;
            }
        }

        // Rozdystrybuuj pozostałe ramki
        remainingFrames = availableFrames - framesDistributed;
        while (remainingFrames > 0) {
            for (int pid = 0; pid < allocatedFrames.length && remainingFrames > 0; pid++) {
                if (active[pid] && !finished[pid]) {
                    allocatedFrames[pid]++;
                    remainingFrames--;
                }
            }
        }
    }

    @Override
    public String getName() {
        return "ZoneModel";
    }
}
