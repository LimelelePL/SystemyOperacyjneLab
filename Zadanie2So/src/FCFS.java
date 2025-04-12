import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class FCFS extends Algoritm implements Scheduler {

    public FCFS(Disk disk) {
        super(disk);
    }

    @Override
    public void run(List<Process> processes) {
        Queue<Process> queue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        queue.addAll(processes);

        while (!queue.isEmpty()) {
            Process process = queue.poll();
            int movement = getDisk().moveTo(process.getCylinderNumber());
            process.setCompleted(true);
            process.setWaitTime(getDisk().getTotalHeadMovements()-process.getArrivalTime());
            addDoneProcess();
        }
    }
}
