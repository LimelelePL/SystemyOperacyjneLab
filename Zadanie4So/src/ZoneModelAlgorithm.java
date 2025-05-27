import java.util.*;

public class ZoneModelAlgorithm extends BaseAlgorithm {

    // Istniejący konstruktor z preGeneratedProcesses
    public ZoneModelAlgorithm(int framesCount, int requestCount, int maxID, int processesCount,
                              double ppfPercentage, double lower, int deltaT,
                              double localProbability, int localCount, int localSubset,
                              List<Proces> preGeneratedProcesses) {
        super(framesCount, requestCount, maxID, processesCount,
                ppfPercentage, lower, deltaT,
                localProbability, localCount, localSubset, preGeneratedProcesses);
    }

    @Override
    public int execute() {
        int errsTotal = 0; // Zliczam całkowitą liczbę błędów stron – zwracam ją na końcu
        final int c = Math.max(1, deltaT / 2); // Co ile żądań przeliczam WSS
        List<Proces> processes = deepCopyProcesses();

        int[] currentIndex = new int[processesCount]; // Indeks żądania, które aktualnie obsługuje proces
        boolean[] active = new boolean[processesCount]; // Czy proces jest aktywny (może działać)
        boolean[] suspended = new boolean[processesCount]; // Czy proces jest zawieszony (brak ramek)
        boolean[] finished = new boolean[processesCount]; // Czy proces zakończył działanie
        int[] allocatedFrames = new int[processesCount]; // Liczba ramek przydzielonych procesowi
        int[] processWss = new int[processesCount]; // Ostatnio obliczona wartość WSS dla każdego procesu

        // na poczatku każdy proces dostaje jedną ramkę i jest aktywny
        Arrays.fill(active, true);
        for (int i = 0; i < processesCount; i++) {
            allocatedFrames[i] = 1;
            processWss[i] = 1;
        }

        // Bufory z ostatnimi żądaniami do wyliczenia WSS na podstawie unikalnych stron
        List<Integer>[] recentRequests = new ArrayList[processesCount];
        for (int i = 0; i < processesCount; i++) {
            recentRequests[i] = new ArrayList<>();
        }

        // do wykrywania szamotania
        Queue<Integer>[] recentlyEvicted = new LinkedList[processesCount];
        Set<Integer>[] evictedPageSets = new HashSet[processesCount];
        for (int i = 0; i < processesCount; i++) {
            recentlyEvicted[i] = new LinkedList<>();
            evictedPageSets[i] = new HashSet<>();
        }

        // Bufory historii błędów stron żeby móc wykryć nadmiarową częstość błędów
        Queue<Boolean>[] faultHistory = new LinkedList[processesCount];
        for (int i = 0; i < processesCount; i++) {
            faultHistory[i] = new LinkedList<>();
        }

        // Mapa ramek przypisanych do każdego procesu
        Map<Integer, List<Page>> framesByProcess = new HashMap<>();

        boolean allProcessesDone = false;

        while (!allProcessesDone) {

            // Sprawdzam, czy któryś proces zawieszony może wrócić do działania (czy są wolne ramki)
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

            // Każdy aktywny proces wykonuje c żądań stron
            for (int pid = 0; pid < processesCount; pid++) {
                if (!active[pid] || finished[pid]) continue;

                Proces process = processes.get(pid);

                // Jeśli proces nie ma jeszcze ramek  tworzę je
                framesByProcess.putIfAbsent(pid, new ArrayList<>());
                List<Page> frames = framesByProcess.get(pid);

                Queue<Integer> evictedQueue = recentlyEvicted[pid];
                Set<Integer> evictedSet = evictedPageSets[pid];

                int requestsProcessed = 0;

                while (requestsProcessed < c && currentIndex[pid] < process.requests.size()) {
                    Page request = process.requests.get(currentIndex[pid]);

                    // Dodaję żądanie do bufora ostatnich - do obliczania WSS
                    recentRequests[pid].add(request.id);
                    if (recentRequests[pid].size() > deltaT) {
                        recentRequests[pid].remove(0);
                    }

                    // Szukam strony w ramkach – jeśli jest, to hit
                    boolean hit = false;
                    for (Page frame : frames) {
                        if (frame.id == request.id) {
                            frame.lastUsed++;
                            hit = true;
                            break;
                        }
                    }

                    // aktualizuję historię błędów stron
                    boolean wasFault = !hit;
                    faultHistory[pid].add(wasFault);
                    if (faultHistory[pid].size() > deltaT) {
                        faultHistory[pid].poll();
                    }

                    //szamotania
                    if (!hit) {
                        int recentFaults = 0;
                        for (boolean fault : faultHistory[pid]) if (fault) recentFaults++;
                        double faultRate = (double) recentFaults / faultHistory[pid].size();
                        if (faultRate > 0.8 && evictedSet.contains(request.id)) {
                            thrashingCount++;
                        }
                    }

                    if (!hit) {
                        errsTotal++;

                        // Dodaję nową stronę do RAM – po to, żeby zasymulować faktyczne załadowanie jej do pamięci
                        if (frames.size() < allocatedFrames[pid]) {
                            frames.add(new Page(request.id, 0, pid));
                        } else {
                            // Pamięć pełna – usuwam stronę LRU
                            //bierzemy stronę, która była najmniej używana
                            frames.sort(Comparator.comparingInt(p -> p.lastUsed));
                            int evictedPageId = frames.get(0).id;
                            frames.remove(0);

                            // Zapisuję usuniętą stronę do struktur pomocniczych
                            evictedQueue.add(evictedPageId);
                            evictedSet.add(evictedPageId);
                            if (evictedQueue.size() > THRASHING_WINDOW) {
                                int oldPageId = evictedQueue.poll();
                                if (!evictedQueue.contains(oldPageId)) {
                                    evictedSet.remove(oldPageId);
                                }
                            }

                            // Dodajęmy nową stronę do RAM po to, żeby odwzorować jej załadowanie po błędzie strony
                            frames.add(new Page(request.id, 0, pid));
                        }
                    }

                    currentIndex[pid]++;
                    requestsProcessed++;
                }

                // Sprawdzam, czy proces zakończył wszystkie żądania
                if (currentIndex[pid] >= process.requests.size()) {
                    active[pid] = false;
                    finished[pid] = true;
                    allocatedFrames[pid] = 0; // Zwalniam jego ramki
                    framesByProcess.remove(pid); // Usuwam jego dane z mapy
                }
            }

            // Obliczam WSS (Working Set Size) dla każdego procesu ile unikalnych stron używał w ostatnim deltaT
            int totalWSS = 0;
            for (int pid = 0; pid < processesCount; pid++) {
                if (finished[pid]) continue;

                if (!recentRequests[pid].isEmpty()) {
                    Set<Integer> uniquePages = new HashSet<>(recentRequests[pid]);
                    processWss[pid] = Math.max(1, uniquePages.size());
                }

                if (active[pid]) {
                    totalWSS += processWss[pid];
                }
            }

            // Sprawdzam, czy wszystkie procesy zakończyły działanie
            allProcessesDone = true;
            for (int i = 0; i < processesCount; i++) {
                if (!finished[i]) {
                    allProcessesDone = false;
                    break;
                }
            }
            if (allProcessesDone) break;

            // Sprawdzam czy wystarczy ramek na WSS każdego aktywnego procesu
            if (totalWSS <= framesCount) {
                // Przydzielam każdemu procesowi dokładnie tyle ramek, ile potrzebuje (jego WSS)
                for (int pid = 0; pid < processesCount; pid++) {
                    if (active[pid] && !finished[pid]) {
                        allocatedFrames[pid] = processWss[pid];
                    }
                }
            } else {
                // Jeśli ramek jest za mało – wybieram proces o największym WSS do zawieszenia
                int maxWssPid = -1;
                int maxWssValue = -1;

                for (int pid = 0; pid < processesCount; pid++) {
                    if (active[pid] && !finished[pid] && processWss[pid] > maxWssValue) {
                        maxWssValue = processWss[pid];
                        maxWssPid = pid;
                    }
                }

                if (maxWssPid != -1) {
                    // Zawieszam proces z największym WSS
                    active[maxWssPid] = false;
                    suspended[maxWssPid] = true;
                    suspensionCount++;

                    int freedFrames = allocatedFrames[maxWssPid];
                    allocatedFrames[maxWssPid] = 0;

                    // Redystrybuuję jego ramki do innych procesów (proporcjonalnie do ich WSS)
                    if (totalWSS > maxWssValue) {
                        totalWSS -= maxWssValue;
                        redistributeFrames(allocatedFrames, processWss, active, finished, freedFrames, totalWSS);
                    } else {
                        // Jeśli został jeden aktywny proces – daję mu wszystko
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
