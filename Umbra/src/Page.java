import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Page {
    public final long pageId;
    public final int sizeClass;
    public final byte[] data; // TODO ALE TEZ NIE TRZEBA RACZEJ
    private final ReentrantReadWriteLock latch = new ReentrantReadWriteLock(true);

    public Page(long pageId, int sizeClass) {
        this.pageId = pageId;
        this.sizeClass = sizeClass;
        int sz = (sizeClass == 0) ? 4 * 1024 : 8 * 1024;
        this.data = new byte[sz];
    }


    public void latchShared() {
        latch.readLock().lock();
    }

    public void unlatchShared() {
        latch.readLock().unlock();
    }


    public void latchExclusive() {
        latch.writeLock().lock();
    }

    public void unlatchExclusive() {
        latch.writeLock().unlock();
    }
}