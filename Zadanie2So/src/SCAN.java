import java.util.*;

public class SCAN extends Algoritm implements Scheduler {
    private int returns = 0; // liczba odbić

    public SCAN(Disk disk) {
        super(disk);
    }

    @Override
    public void run(List<Process> processes) {
        Queue<Process> arrivalQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        arrivalQueue.addAll(processes);

        PriorityQueue<Process> ascendingQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getCylinderNumber));
        PriorityQueue<Process> descendingQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getCylinderNumber).reversed());

        int initialHead = getDisk().getCurrentPosition();
    }
}
