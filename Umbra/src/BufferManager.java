import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class BufferManager {
    private final int maxFrames;
    private final Map<Long, Page> frameMap = new ConcurrentHashMap<>();
    private final Deque<Long> fifo = new ArrayDeque<>();
    private final AtomicLong countHit = new AtomicLong(0);
    private final AtomicLong countMiss = new AtomicLong(0);
    private final AtomicLong countEvict = new AtomicLong(0);

    public BufferManager(int maxFrames) {
        this.maxFrames = maxFrames;
    }

    /**
     * Pobieramy stronę o danym pageId i sizeClass. Jeżeli jest w pamięci, to hit i zwraxamy Swip swizzlowany.
     * Jeśli nie, to miss, ładujemy: (jeśli pełno => evict FIFO i countEvict++), potem create new Page(),
     * countMiss++, put do mapy i front FIFO, i zwracamy Swip swizzlowany.
     *
     *tak wiem zapomnialem zwrocic evicty w wynikachchewhcweuchweuchuwechuwehc
     */
    Random rand=new Random();
    public Swip fetchPage(long pageId, int sizeClass) {
        Page p = frameMap.get(pageId);
        if (p != null) {
            // HIT
            countHit.incrementAndGet();
            return new Swip(p);
        }

        // MISS
        countMiss.incrementAndGet();
        synchronized (this) {
            if (frameMap.size() >= maxFrames) {
                // Evicja FIFO
                Long victimId = fifo.removeFirst();
                Page victim = frameMap.remove(victimId);
                // symulujemy zapis na dysk
                countEvict.incrementAndGet();
            }
            // ladujemy nową stronę z "dysku"
            Page newPage = new Page(pageId, sizeClass);
            frameMap.put(pageId, newPage);
            fifo.addLast(pageId);
            // symulujemy pread
            return new Swip(newPage);
        }
    }

    public long getHitCount() {
        return countHit.get();
    }

    public long getMissCount() {
        return countMiss.get();
    }

    public long getEvictCount() {
        return countEvict.get();
    }
}