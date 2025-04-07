import java.util.PriorityQueue;

public abstract class Algoritm {
    private int discplacement;
    private int returns;
    private PriorityQueue<Process> processes;
    private int starvedProcesses;

    public Algoritm() {
        this.discplacement = 0;
        this.returns = 0;
        this.processes = new PriorityQueue<>();
        this.starvedProcesses = 0;
    }

    public int getDiscplacement() {
        return discplacement;
    }

    public void setDiscplacement(int discplacement) {
        this.discplacement = discplacement;
    }

    public PriorityQueue<Process> getProcesses() {
        return processes;
    }

    public void setProcesses(PriorityQueue<Process> processes) {
        this.processes = processes;
    }

    public int getReturns() {
        return returns;
    }

    public void setReturns(int returns) {
        this.returns = returns;
    }

    public int getStarvedProcesses() {
        return starvedProcesses;
    }

    public void setStarvedProcesses(int starvedProcesses) {
        this.starvedProcesses = starvedProcesses;
    }
}
