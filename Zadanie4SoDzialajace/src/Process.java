import java.util.ArrayList;
import java.util.List;

public class Process {
    private int pID;
    private ArrayList<Page> referenceString;
    private ApproxLRU algorithm;
    private boolean suspended;
    private int suspendCount;

    public Process(int pid, ArrayList<Page> ref) {
        this.pID = pid;
        this.referenceString = ref;
        this.suspended = false;
        this.suspendCount = 0;
    }
    public void suspend() {
        this.suspended = true;
        this.suspendCount++;
    }
    public void resume() {
        this.suspended = false;
    }
    public boolean isSuspended() {
        return suspended;
    }
    public void handle(Page request) {
        if (suspended) {
            System.out.println("Process " + pID + " is suspended. Cannot handle request.");
            return;
        }
        algorithm.handleRequest(request);
    }

    public void assignApproxLRUWithFrames(int frames) {
        this.algorithm = new ApproxLRU(frames);
    }

    public int getPageFaults() {
        return algorithm.getPageFaultCount();
    }

    public int getTrashings() {
        return algorithm.getTrashingCount();
    }

    public void reset() {
        algorithm.resetStats();
    }

    public ArrayList<Page> getReferenceString() {
        return referenceString;
    }

    public int getNumberOfPages() {
        int sum = 0;
        ArrayList<Integer> visitedNumbers = new ArrayList<>();
        for (Page page : referenceString) {
            if (!visitedNumbers.contains(page.getID())) {
                visitedNumbers.add(page.getID());
                sum++;
            }
        }
        return sum;
    }

    public int getPid() { return pID; }
}
