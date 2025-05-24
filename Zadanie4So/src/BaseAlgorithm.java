// BaseAlgorithm.java
import java.util.*;

public abstract class BaseAlgorithm implements PageReplacementAlgorithm {
    protected final int framesCount, requestCount, maxID, processesCount;
    protected final double upper;
    protected final double lower;
    protected final int zoneCoef;
    protected final double localProbability;
    protected final int localCount, localSubset;
    protected final List<Proces> processes;
    protected final Random rnd = new Random();
    
    // Parametry dla liczenia szamotań
    protected static final int THRASHING_WINDOW = 10; // okno czasowe dla szamotań
    protected int thrashingCount = 0; // liczba szamotań
    
    // Licznik wstrzymań procesów
    protected int suspensionCount = 0; // liczba wstrzymań procesów

    public BaseAlgorithm(int framesCount,
                         int requestCount,
                         int maxID,
                         int processesCount,
                         double upper,
                         double lower,
                         int zoneCoef,
                         double localProbability,
                         int localCount,
                         int localSubset) {
        this.framesCount     = framesCount;
        this.requestCount   = requestCount;
        this.maxID          = maxID;
        this.processesCount = processesCount;
        this.upper  = upper;
        this.lower  = lower;

        this.zoneCoef       = zoneCoef;
        this.localProbability = localProbability;
        this.localCount     = localCount;
        this.localSubset    = localSubset;

        this.processes = generateProcesses();
    }

    /**
     * Nowy konstruktor przyjmujący gotową listę procesów.
     */
    public BaseAlgorithm(int framesCount,
                         int requestCount,
                         int maxID,
                         int processesCount,
                         double upper,
                         double lower,
                         int zoneCoef,
                         double localProbability,
                         int localCount,
                         int localSubset,
                         List<Proces> preGeneratedProcesses) {
        this.framesCount     = framesCount;
        this.requestCount   = requestCount; // Może być nieużywane jeśli procesy są pre-generowane
        this.maxID          = maxID; // Może być nieużywane jeśli procesy są pre-generowane
        this.processesCount = processesCount; // Powinno odpowiadać preGeneratedProcesses.size()
        this.upper  = upper;
        this.lower  = lower;
        this.zoneCoef       = zoneCoef;
        this.localProbability = localProbability; // Może być nieużywane jeśli procesy są pre-generowane
        this.localCount     = localCount; // Może być nieużywane jeśli procesy są pre-generowane
        this.localSubset    = localSubset; // Może być nieużywane jeśli procesy są pre-generowane

        // Używamy głębokiej kopii dostarczonych procesów, aby uniknąć modyfikacji oryginalnej listy
        this.processes = deepCopyProcessList(preGeneratedProcesses);
    }

    /**
     * Wspólna metoda generująca listę procesów i wektor żądań,
     * dokładnie jak w Twojej dotychczasowej metodzie generate().
     */
    private List<Proces> generateProcesses() {
        List<Page> allRequests = new ArrayList<>(requestCount);
        int pagesPerProcess = this.maxID; // Przejrzystość: maxID to teraz strony na proces

        // --- generowanie ciągu żądań ---
        for (int i = 0; i < requestCount; i++) {
            int proc = rnd.nextInt(processesCount); // Najpierw losujemy proces
            int pageIdOffset = proc * pagesPerProcess; // Offset dla ID stron tego procesu

            if (rnd.nextDouble() < localProbability) {
                // Generowanie z lokalnością
                int subsetSize = rnd.nextInt(localSubset) + 1;
                List<Integer> subset = new ArrayList<>(subsetSize);
                for (int j = 0; j < subsetSize; j++) {
                    // Lokalne ID strony (0 do pagesPerProcess-1), następnie przesunięte
                    subset.add(pageIdOffset + rnd.nextInt(pagesPerProcess));
                }
                int locCnt = rnd.nextInt(localCount + 1);
                for (int k = 0; k < locCnt; k++) {
                    allRequests.add(new Page(
                            subset.get(rnd.nextInt(subset.size())),
                            0, proc
                    ));
                }
            } else {
                // Generowanie bez lokalności (ale wciąż w ramach stron danego procesu)
                allRequests.add(new Page(pageIdOffset + rnd.nextInt(pagesPerProcess), 0, proc));
            }
        }

        // --- podział na procesy ---
        List<Proces> procList = new ArrayList<>(processesCount);
        for (int i = 0; i < processesCount; i++) {
            List<Page> reqs = new ArrayList<>();
            for (Page p : allRequests)
                if (p.processID == i)
                    reqs.add(p);
            procList.add(new Proces(reqs, 0));
        }
        return procList;
    }

    /** Głęboka kopia procesów (żeby nie mieszać referencji) */
    protected List<Proces> deepCopyProcesses() {
        List<Proces> copy = new ArrayList<>(processesCount);
        for (Proces p : processes) {
            copy.add(new Proces(new ArrayList<>(p.requests), 0));
        }
        return copy;
    }

    /**
     * Pomocnicza metoda do głębokiego kopiowania listy procesów.
     * Używana przez nowy konstruktor.
     */
    private List<Proces> deepCopyProcessList(List<Proces> originalProcesses) {
        if (originalProcesses == null) {
            return generateProcesses(); // Fallback, gdyby null został przekazany
        }
        List<Proces> copy = new ArrayList<>(originalProcesses.size());
        for (Proces p : originalProcesses) {
            // Zakładamy, że Proces ma konstruktor, który tworzy głęboką kopię żądań
            // lub że lista żądań w Proces jest kopiowana przy tworzeniu nowego Procesu.
            // W obecnej implementacji Proces(List<Page> requests, int framesCount) tworzy nową ArrayList<>(requests).
            copy.add(new Proces(new ArrayList<>(p.requests), p.framesCount));
        }
        return copy;
    }

    /**
     * Pomocnicze LRU dla listy zapytań całego procesu.
     * Zmodyfikowane aby śledzić szamotania.
     */
    protected int lruList(List<Page> pagesRef, int frameSize) {
        int errs = 0;
        List<Page> frames = new ArrayList<>();
        
        // Lista ostatnio usuniętych stron dla wykrywania szamotań
        Queue<Integer> recentlyEvicted = new LinkedList<>();
        Set<Integer> evictedSet = new HashSet<>();
        
        for (Page req : pagesRef) {
            boolean hit = false;
            for (Page f : frames) {
                if (f.id == req.id) {
                    f.lastUsed++;
                    hit = true;
                    break;
                }
            }
            
            // Sprawdź czy żądany element był niedawno usunięty (szamotanie)
            if (!hit && evictedSet.contains(req.id)) {
                thrashingCount++;
            }
            
            if (!hit) {
                errs++;
                if (frames.size() < frameSize) {
                    frames.add(new Page(req.id, req.lastUsed, req.processID));
                } else {
                    frames.sort(Comparator.comparingInt(p -> p.lastUsed));
                    
                    // Dodaj usunięty element do listy ostatnio usuniętych
                    int evictedId = frames.get(0).id;
                    recentlyEvicted.add(evictedId);
                    evictedSet.add(evictedId);
                    
                    // Usuwamy stare wartości z okna czasowego
                    if (recentlyEvicted.size() > THRASHING_WINDOW) {
                        int oldId = recentlyEvicted.poll();
                        // Usuwamy z setu jeśli nie ma więcej wystąpień w kolejce
                        if (!recentlyEvicted.contains(oldId)) {
                            evictedSet.remove(oldId);
                        }
                    }
                    
                    frames.remove(0);
                    frames.add(new Page(req.id, req.lastUsed, req.processID));
                }
            }
        }
        return errs;
    }

    /** Zwraca liczbę szamotań (thrashing) */
    @Override
    public int getThrashing() {
        return thrashingCount;
    }
    
    /** Zwraca liczbę wstrzymań procesów */
    @Override
    public int getSuspensions() {
        return suspensionCount;
    }
    
    /** Każda klasa musi zaimplementować tę metodę */
    @Override
    public abstract int execute();

    public int getZoneCoef() {
        return zoneCoef;
    }
}
