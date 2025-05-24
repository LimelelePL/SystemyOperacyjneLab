import java.util.*;

public class ZoneModelAlgorithm extends BaseAlgorithm {

    // Nowy konstruktor
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
        final int c = Math.max(1, deltaT/2); // Częstość przeliczania WSS (c < delta_t)
        
        List<Proces> processes = deepCopyProcesses();
        
        // Struktury danych potrzebne do symulacji
        int[] currentIndex = new int[processesCount];     // Indeks aktualnego żądania dla każdego procesu
        boolean[] active = new boolean[processesCount];   // Aktywne procesy
        int[] allocatedFrames = new int[processesCount];  // Przydzielone ramki dla każdego procesu
        Arrays.fill(active, true);                        // Na początku wszystkie procesy są aktywne
        
        // Bufor ostatnich deltaT żądań dla każdego procesu (do obliczania WSS)
        List<Integer>[] recentRequests = new ArrayList[processesCount];
        for (int i = 0; i < processesCount; i++) {
            recentRequests[i] = new ArrayList<>();
        }
        
        // Struktury dla szamotania - używane do wykrywania szamotań
        Queue<Integer>[] recentlyEvicted = new LinkedList[processesCount];
        Set<Integer>[] evictedPageSets = new HashSet[processesCount];
        for (int i = 0; i < processesCount; i++) {
            recentlyEvicted[i] = new LinkedList<>();
            evictedPageSets[i] = new HashSet<>();
        }
        
        // Struktury dla ramek pamięci
        Map<Integer, List<Page>> framesByProcess = new HashMap<>(); // Ramki pamięci dla każdego procesu
        
        // Główna pętla symulacji - wykonujemy dopóki wszystkie procesy nie zakończą pracy
        boolean allProcessesDone = false;
        while (!allProcessesDone) {
            // Krok 1: Wykonaj c żądań dla każdego aktywnego procesu
            for (int pid = 0; pid < processesCount; pid++) {
                if (!active[pid]) continue; // Pomijamy wstrzymane procesy
                
                Proces process = processes.get(pid);
                
                // Lista ramek dla procesu (inicjalizacja, jeśli nie istnieje)
                if (!framesByProcess.containsKey(pid)) {
                    framesByProcess.put(pid, new ArrayList<>());
                }
                List<Page> frames = framesByProcess.get(pid);
                
                // Pobierz struktury szamotania dla tego procesu
                Queue<Integer> evictedQueue = recentlyEvicted[pid];
                Set<Integer> evictedSet = evictedPageSets[pid];
                
                // Wykonaj c żądań lub dopóki proces ma żądania
                int requestsProcessed = 0;
                while (requestsProcessed < c && currentIndex[pid] < process.requests.size()) {
                    // Pobierz aktualne żądanie
                    Page request = process.requests.get(currentIndex[pid]);
                    
                    // Dodaj to żądanie do bufora ostatnich deltaT żądań (do obliczenia WSS)
                    recentRequests[pid].add(request.id);
                    if (recentRequests[pid].size() > deltaT) {
                        recentRequests[pid].remove(0); // Usuń najstarsze, gdy przekroczymy rozmiar okna
                    }
                    
                    // Symulujemy odwołanie do strony przy aktualnym przydziale ramek
                    boolean hit = false;
                    for (Page frame : frames) {
                        if (frame.id == request.id) {
                            frame.lastUsed++; // Aktualizacja LRU
                            hit = true;
                            break;
                        }
                    }
                    
                    // Sprawdź czy to szamotanie - strona była niedawno usunięta i teraz jest potrzebna ponownie
                    if (!hit && evictedSet.contains(request.id)) {
                        thrashingCount++; // To jest szamotanie
                    }
                    
                    if (!hit) {
                        errsTotal++; // Zwiększamy licznik błędów stron
                        
                        // Jeśli jest miejsce w przydzielonych ramkach, dodajemy stronę
                        if (frames.size() < allocatedFrames[pid]) {
                            frames.add(new Page(request.id, 0, pid));
                        } else if (allocatedFrames[pid] > 0) { // Jeśli mamy jakiekolwiek ramki
                            // LRU - usuń najmniej ostatnio używaną stronę
                            frames.sort(Comparator.comparingInt(p -> p.lastUsed));
                            int evictedPageId = frames.get(0).id;
                            frames.remove(0);
                            
                            // Dodaj usuniętą stronę do bufora niedawno usuniętych (dla wykrywania szamotań)
                            evictedQueue.add(evictedPageId);
                            evictedSet.add(evictedPageId);
                            
                            // Utrzymuj bufor szamotań o stałym rozmiarze
                            if (evictedQueue.size() > THRASHING_WINDOW) {
                                int oldPageId = evictedQueue.poll();
                                // Usuwamy z setu tylko jeśli nie ma więcej wystąpień w kolejce
                                if (!evictedQueue.contains(oldPageId)) {
                                    evictedSet.remove(oldPageId);
                                }
                            }
                            
                            // Dodajemy nową stronę
                            frames.add(new Page(request.id, 0, pid));
                        }
                        // Jeśli nie mamy żadnych przydzielonych ramek, pomijamy wstawianie
                    }
                    
                    currentIndex[pid]++;
                    requestsProcessed++;
                }
            }
            
            // Krok 2: Oblicz WSS dla każdego procesu
            int[] wss = new int[processesCount];
            int totalWSS = 0;
            int activeProcessesCount = 0;
            
            for (int pid = 0; pid < processesCount; pid++) {
                if (!active[pid]) continue;
                
                // Jeśli proces nie zakończył pracy, oblicz jego WSS
                if (currentIndex[pid] < processes.get(pid).requests.size()) {
                    // WSS to liczba unikalnych stron w oknie deltaT
                    Set<Integer> uniquePages = new HashSet<>(recentRequests[pid]);
                    wss[pid] = Math.max(1, uniquePages.size());
                    totalWSS += wss[pid];
                    activeProcessesCount++;
                } else {
                    // Proces zakończył wszystkie żądania
                    active[pid] = false;
                    allocatedFrames[pid] = 0; // Zwolnij ramki
                    framesByProcess.remove(pid); // Usuń strukturę stron
                }
            }
            
            // Sprawdź, czy wszystkie procesy zakończyły pracę
            allProcessesDone = (activeProcessesCount == 0);
            if (allProcessesDone) break;
            
            // Krok 3: Sprawdź, czy suma WSS przekracza dostępne ramki
            if (totalWSS <= framesCount) {
                // Jeśli wystarczy ramek, każdy proces dostaje tyle, ile wynosi jego WSS
                for (int pid = 0; pid < processesCount; pid++) {
                    if (active[pid]) {
                        allocatedFrames[pid] = wss[pid];
                    }
                }
            } else {
                // Jeśli brakuje ramek, musimy kogoś wstrzymać
                
                // Strategia: wstrzymaj proces o największym WSS
                int maxWssPid = -1;
                int maxWssValue = -1;
                
                for (int pid = 0; pid < processesCount; pid++) {
                    if (active[pid] && wss[pid] > maxWssValue) {
                        maxWssValue = wss[pid];
                        maxWssPid = pid;
                    }
                }
                
                if (maxWssPid != -1) {
                    // Wstrzymaj proces o największym WSS
                    active[maxWssPid] = false;
                    suspensionCount++; // Zwiększ licznik wstrzymań
                    
                    // Zwolnij ramki tego procesu
                    int freedFrames = allocatedFrames[maxWssPid];
                    allocatedFrames[maxWssPid] = 0;
                    framesByProcess.remove(maxWssPid);
                    
                    // Przelicz sumę WSS bez wstrzymanego procesu
                    totalWSS -= wss[maxWssPid];
                    
                    // Redystrybuuj zwolnione ramki proporcjonalnie do WSS pozostałych procesów
                    int remainingFrames = freedFrames;
                    
                    // Najpierw przydziel proporcjonalnie według WSS
                    for (int pid = 0; pid < processesCount; pid++) {
                        if (active[pid]) {
                            // Oblicz dodatkowe ramki proporcjonalnie do WSS
                            int additionalFrames = (totalWSS > 0) ? 
                                    (int)Math.round(((double)wss[pid] / totalWSS) * freedFrames) : 0;
                            
                            allocatedFrames[pid] += additionalFrames;
                            remainingFrames -= additionalFrames;
                        }
                    }
                    
                    // Rozdystrybuuj pozostałe ramki (jeśli są)
                    while (remainingFrames > 0) {
                        for (int pid = 0; pid < processesCount && remainingFrames > 0; pid++) {
                            if (active[pid]) {
                                allocatedFrames[pid]++;
                                remainingFrames--;
                            }
                        }
                    }
                }
                else {
                    // Jeśli nie możemy wstrzymać żadnego procesu, przydziel ramki proporcjonalnie
                    int remainingFrames = framesCount;
                    
                    // Przydziel minimum po 1 ramce każdemu aktywnemu procesowi
                    for (int pid = 0; pid < processesCount; pid++) {
                        if (active[pid]) {
                            allocatedFrames[pid] = 1;
                            remainingFrames--;
                        }
                    }
                    
                    // Resztę przydziel proporcjonalnie do WSS
                    for (int pid = 0; pid < processesCount; pid++) {
                        if (active[pid] && remainingFrames > 0 && totalWSS > 0) {
                            // Proporcjonalny przydział pozostałych ramek
                            int additionalFrames = (int)Math.round(((double)wss[pid] / totalWSS) * remainingFrames);
                            additionalFrames = Math.min(additionalFrames, remainingFrames); // Nie więcej niż zostało
                            
                            allocatedFrames[pid] += additionalFrames;
                            remainingFrames -= additionalFrames;
                        }
                    }
                    
                    // Rozdystrybuuj pozostałe ramki (jeśli są)
                    while (remainingFrames > 0) {
                        for (int pid = 0; pid < processesCount && remainingFrames > 0; pid++) {
                            if (active[pid]) {
                                allocatedFrames[pid]++;
                                remainingFrames--;
                            }
                        }
                    }
                }
            }
        }
        
        return errsTotal;
    }

    @Override
    public String getName() {
        return "ZoneModel";
    }
}
