import java.util.ArrayList;
import java.util.Queue;

public abstract class Algoritm {
    private int pageFaultCount;
    private int trashingCount;
    private int requestCount;
    private RAM ram;
    private int size;

    private int windowSize = 10;        // co ile sprawdzamy
    private int faultThreshold = 7;     // ile błędów to szamotanie

    private int recentRequests = 0;
    private int recentFaults = 0;


    public Algoritm(int size) {
        this.size = size;
        ram=new RAM(size);
        this.pageFaultCount = 0;
        this.trashingCount = 0;
        this.requestCount = 0;
        resetStats();
    }

    public void resetStats(){
        ram.reset();
        this.pageFaultCount = 0;
        this.trashingCount = 0;
        this.requestCount = 0;
        this. recentRequests = 0;
        this. recentFaults = 0;
    }

    public void incrementRequestCount() {
        this.requestCount++;
        this.recentRequests++;

        if (recentRequests >= windowSize) {
            if (recentFaults >= faultThreshold) {
                trashingCount++;
            }
            recentRequests = 0;
            recentFaults = 0;
        }
    }


    public boolean pageFault(Page page){
        if(!ram.contains(page)){
            this.pageFaultCount++;
            this.recentFaults++;
            return true;
        }
        return false;
    }

    public void setRamSize(int newSize) {
        this.ram = new RAM(newSize);
        this.resetStats(); // lub tylko RAM.reset()
    }
    public int getRamSize() {
        return ram.getSize();
    }
    public int getRecentFaults() {
        return recentFaults;
    }

    public int getRecentRequests() {
        return recentRequests;
    }

    public void resetWindowStats() {
        recentFaults = 0;
        recentRequests = 0;
    }


    public abstract void handleRequest(Page request, Queue<Page> queue);
    public abstract void run(ArrayList<Page> pages);

    public int getPageFaultCount() {
        return pageFaultCount;
    }

    public int getTrashingCount() {
        return trashingCount;
    }

    public RAM getRam() {
        return ram;
    }
}
