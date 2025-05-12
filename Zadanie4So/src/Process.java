import java.util.List;

public class Process {
    private int processID;
    private List<Page> referenceString;
    private Algoritm algorithm;
    private boolean suspended = false;

    public Process(int processID, List<Page> referenceString, Algoritm algorithm) {
        this.processID = processID;
        this.referenceString = referenceString;
        this.algorithm = algorithm;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void suspend() {
        suspended = true;
    }

    public void resume() {
        suspended = false;
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

    public void reset() {
        algorithm.resetStats();
    }

    public void setAlgorithm(Algoritm algorithm) {
        this.algorithm = algorithm;
        algorithm.resetStats();
    }

}
