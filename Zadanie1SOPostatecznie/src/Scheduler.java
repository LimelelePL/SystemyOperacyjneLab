import java.util.PriorityQueue;

public interface Scheduler {
    void run(PriorityQueue<Process> processes);
}
