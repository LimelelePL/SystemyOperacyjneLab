import java.util.List;

public class Process {
    private int processID;
    private List<Page> referenceString;
    private Algoritm algorithm;
    private boolean suspended = false;
    private int suspendCount = 0;

    public Process(int id, List<Page> ref, Algoritm alg) {
        this.processID = id;
        this.referenceString = ref;
        this.algorithm = alg;
    }

    public void suspend() {
        suspended = true;
        suspendCount++;
    }

    public void resume() {
        suspended = false;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public int getSuspendCount() {
        return suspendCount;
    }

    public int getProcessID() {
        return processID;
    }

    public List<Page> getReferenceString() {
        return referenceString;
    }

    public Algoritm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algoritm algorithm) {
        this.algorithm = algorithm;
    }
}
