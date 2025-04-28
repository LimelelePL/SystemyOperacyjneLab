import java.util.ArrayList;
import java.util.Queue;

public abstract class Algoritm {
    private int pageFaultCount;
    private int trashingCount;
    private int faultRate;
    private int requestCount;
    private RAM ram;
    private int size;

    private int delta;
    private int tempPageFaults;


    public Algoritm(int size) {
        this.size = size;
        ram=new RAM(size);
        this.pageFaultCount = 0;
        this.trashingCount = 0;
        this.faultRate = 7;
        this.delta = 7;
        this.requestCount = 0;
        this.tempPageFaults = 0;
        resetStats();
    }

    public void resetStats(){
        ram.reset();
        this.pageFaultCount = 0;
        this.trashingCount = 0;
        this.faultRate = 7;
        this.delta = 8;
        this.requestCount = 0;
        this.tempPageFaults = 0;
    }

    public void incrementRequestCount() {
        this.requestCount++;
    }

    public boolean pageFault(Page page){
        if(!ram.contains(page)){
            this.pageFaultCount++;
            this.tempPageFaults++;
            return true;
        }
        return false;
    }

    public boolean checkTrashing() {
        if(requestCount%10==0){
            if(tempPageFaults >=delta ){
                trashingCount++;
                tempPageFaults =0;
                return true;
            } else tempPageFaults=0;
        }
        return false;
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
