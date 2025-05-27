// BaseAlgorithm.java
import java.util.*;

public abstract class BaseAlgorithm implements PageReplacementAlgorithm {
    protected final int framesCount, requestCount, maxID, processesCount;
    protected final double upper;
    protected final double lower;
    protected final int deltaT;
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
                         int deltaT,
                         double localProbability,
                         int localCount,
                         int localSubset) {
        this.framesCount     = framesCount;
        this.requestCount   = requestCount;
        this.maxID          = maxID;
        this.processesCount = processesCount;
        this.upper  = upper;
        this.lower  = lower;

        this.deltaT       = deltaT;
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
                         int deltaT,
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
        this.deltaT       = deltaT;
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
        int pagesPerProcess = this.maxID;

        //generowanie maksymalnej liczby unikalnych stron dla każdego procesu
        int[] maxUniquePerProcess = new int[processesCount];
        for (int i = 0; i < processesCount; i++) {
            // Losuj liczbę unikalnych stron dla procesu (50-250)
            maxUniquePerProcess[i] = 50 + rnd.nextInt(maxID - 50 + 1);
        }

        // generowanie globalnego ciągu żądań
        for (int i = 0; i < requestCount; i++) {
            int proc = rnd.nextInt(processesCount);
            int pageIdOffset = proc * pagesPerProcess;

            if (rnd.nextDouble() < localProbability) {
                // Generowanie z lokalnością )
                int subsetSize = rnd.nextInt(localSubset) + 1;
                List<Integer> subset = new ArrayList<>(subsetSize);
                for (int j = 0; j < subsetSize; j++) {
                    subset.add(pageIdOffset + rnd.nextInt(maxUniquePerProcess[proc]));
                }
                int locCnt = rnd.nextInt(localCount + 1);
                for (int k = 0; k < locCnt; k++) {
                    allRequests.add(new Page(
                            subset.get(rnd.nextInt(subset.size())),
                            0, proc
                    ));
                }
            } else {
                // Generowanie bez lokalności (strona z zakresu 0 do maxUniquePerProcess-1)
                int pageId = pageIdOffset + rnd.nextInt(maxUniquePerProcess[proc]);
                allRequests.add(new Page(pageId, 0, proc));
            }
        }

        // podział na procesy
        List<Proces> procList = new ArrayList<>(processesCount);
        for (int i = 0; i < processesCount; i++) {
            List<Page> reqs = new ArrayList<>();
            for (Page p : allRequests)
                if (p.processID == i)
                    reqs.add(p);
            procList.add(new Proces(reqs, 0));
        }

        int totalRequests = 0;
        for (Proces p : procList) {
            totalRequests += p.requests.size();
        }
        System.out.println("liczba żądań: " + totalRequests);
        System.out.println("liczba procesów: " + procList.size());
        return procList;
    }

    // Głęboka kopia procesów
    protected List<Proces> deepCopyProcesses() {
        List<Proces> copy = new ArrayList<>(processesCount);
        for (Proces p : processes) {
            copy.add(new Proces(new ArrayList<>(p.requests), 0));
        }
        return copy;
    }

    private List<Proces> deepCopyProcessList(List<Proces> originalProcesses) {
        if (originalProcesses == null) {
            return generateProcesses(); // Fallback, gdyby null został przekazany
        }
        List<Proces> copy = new ArrayList<>(originalProcesses.size());
        for (Proces p : originalProcesses) {
            copy.add(new Proces(new ArrayList<>(p.requests), p.framesCount));
        }
        return copy;
    }

    /**
     * Pomocnicze LRU dla listy zapytań całego procesu.
     */
    protected int lruList(List<Page> pagesRef, int frameSize) {
        int errs = 0; // Licznik błędów stron
        List<Page> frames = new ArrayList<>(); // Lista ramek trzymanych aktualnie

        // Bufor usuniętych stron – potrzebny do sprawdzenia, czy dana strona była niedawno usunięta
        Queue<Integer> recentlyEvicted = new LinkedList<>();
        Set<Integer> evictedSet = new HashSet<>();

        // Bufor historii błędów – potrzebny do sprawdzenia, czy mamy dużą częstość błędów (PPF)
        Queue<Boolean> faultHistory = new LinkedList<>();

        for (Page req : pagesRef) {
            boolean hit = false;

            // Szukam strony w aktualnie załadowanych ramkach – jeśli jest, to trafienie
            for (Page f : frames) {
                if (f.id == req.id) {
                    f.lastUsed++;
                    hit = true;
                    break;
                }
            }

            // Aktualizuję historię błędów – dodaję true/false zależnie od tego, czy był błąd
            boolean wasFault = !hit;
            faultHistory.add(wasFault);
            if (faultHistory.size() > THRASHING_WINDOW) {
                faultHistory.poll(); // Usuwam najstarszy wpis, żeby utrzymać rozmiar bufora
            }

            // Sprawdzam, czy mamy do czynienia z szamotaniem
            // – tylko jeśli jest błąd, strona była usunięta i mamy duży procent błędów w ostatnim czasie
            if (!hit) {
                int recentFaults = 0;
                for (boolean b : faultHistory) if (b) recentFaults++;
                double faultRate = (double) recentFaults / faultHistory.size();

                if (faultRate > 0.8 && evictedSet.contains(req.id)) {
                    thrashingCount++; // Szamotanie wykryte!
                }
            }

            // Obsługa błędu strony
            if (!hit) {
                errs++; // Zwiększam licznik błędów

                if (frames.size() < frameSize) {
                    // Jest miejsce – po prostu dodaję stronę
                    frames.add(new Page(req.id, req.lastUsed, req.processID));
                } else {
                    // Ramki pełne – muszę usunąć najmniej używaną (LRU)
                    frames.sort(Comparator.comparingInt(p -> p.lastUsed));
                    int evictedId = frames.get(0).id;

                    // Dodaję usuniętą stronę do bufora do śledzenia
                    recentlyEvicted.add(evictedId);
                    evictedSet.add(evictedId);

                    // Utrzymuję THRASHING_WINDOW – jeśli jest za dużo, usuwam najstarsze
                    if (recentlyEvicted.size() > THRASHING_WINDOW) {
                        int oldId = recentlyEvicted.poll();
                        if (!recentlyEvicted.contains(oldId)) {
                            evictedSet.remove(oldId);
                        }
                    }

                    // Usuwam LRU i dodaję nową stronę
                    frames.remove(0);
                    frames.add(new Page(req.id, req.lastUsed, req.processID));
                }
            }
        }
        return errs; // Zwracam liczbę błędów stron
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

}
