import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class CSCAN extends Algoritm implements Scheduler{
    public CSCAN(Disk disk) {
        super(disk);
    }

    @Override
    public void run(List<Process> processes) {
        Queue<Process> queue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        queue.addAll(processes);

        PriorityQueue<Process> ascendingQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getCylinderNumber));
        PriorityQueue<Process> descendingQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getCylinderNumber).reversed());

        int initialHead = getDisk().getCurrentPosition();


    }
}
