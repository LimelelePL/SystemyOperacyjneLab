import java.util.ArrayList;
import java.util.Queue;

public abstract class Algoritm {
    private int pageFaultCount;
    private int trashingCount;

    private int requestCount;
    private RAM ram;
    private int size;

    private int windowSize; // co ile sprawdzamy czy wystapilo szamotanie
    private int faultRate;// prog szamotania


    private int recentRequests = 0;
    private int recentFaults = 0;


    public Algoritm(int size) {
        this.size = size;
        ram=new RAM(size);
        this.pageFaultCount = 0;
        this.trashingCount = 0;

        this.faultRate = 7;
        this. windowSize = 10;

        this.requestCount = 0;
    }

    public void resetStats(){
        ram.reset();
        this.pageFaultCount = 0;
        this.trashingCount = 0;
        this.faultRate = 7;
        this.requestCount = 0;
        this. recentRequests = 0;
        this. recentFaults = 0;
    }

    public void incrementRequestCount() {
        this.requestCount++;
        this.recentRequests++;

        if (recentRequests >= windowSize) {
            if (recentFaults >= faultRate) {
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



    public abstract void handleRequest(Page request);
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
