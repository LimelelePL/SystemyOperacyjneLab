// Proces.java
import java.util.*;

public class Proces {
    public int processFrame;
    public List<Page> requests;
    public double errorsCoef = 0;
    public int framesCount = 0;
    public int errors = 0;
    public List<Page> frames = new ArrayList<>();

    public Proces(List<Page> requests, int framesCount) {
        this.requests = new ArrayList<>(requests);
        this.framesCount = framesCount;
    }

    /**
     * Jednoprzebiegowy LRU – zwraca liczbę błędów stron.
     */
    public int LRU(List<Page> pageRefs) {
        if (pageRefs.isEmpty()) return 0;
        Page temp = pageRefs.get(0);
        boolean hit = false;
        int errs = 0;

        // jeżeli są jeszcze wolne ramki
        if (frames.size() < framesCount) {
            for (Page f : frames) {
                if (f.id == temp.id) {
                    f.lastUsed++;
                    hit = true;
                    break;
                }
            }
            if (!hit) {
                errs++;
                frames.add(new Page(temp.id, temp.lastUsed, temp.processID));
            }
        } else {
            for (Page f : frames) {
                if (f.id == temp.id) {
                    f.lastUsed++;
                    hit = true;
                    break;
                }
            }
            if (!hit) {
                // wyrzucenie najmniej używanej
                frames.sort(Comparator.comparingInt(p -> p.lastUsed));
                frames.remove(0);
                frames.add(new Page(temp.id, temp.lastUsed, temp.processID));
                errs++;
            }
        }
        errorsCoef += errs;
        return errs;
    }
}
