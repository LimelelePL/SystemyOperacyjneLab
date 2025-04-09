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
        int currentTime = 0;

        while (!queue.isEmpty()) {
            Process process = queue.poll();
//czekam na proces jesli trzeba
            currentTime = Math.max(currentTime, process.getArrivalTime());

            int movement=getDisk().moveTo(process.getCylinderNumber());
            currentTime += movement;

            process.setCompleted(true);
            addDoneProcess();

            addWaitTime(currentTime - process.getArrivalTime());

        }
    }
}
