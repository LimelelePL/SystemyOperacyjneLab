import java.util.PriorityQueue;

public class Algoritm {
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

}
