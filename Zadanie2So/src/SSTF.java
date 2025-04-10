import java.util.*;

public class SSTF extends Algoritm implements Scheduler{

    public SSTF(Disk disk) {
        super(disk);
    }

    @Override
    public void run(List<Process> processes) {
        Queue<Process> queue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        queue.addAll(processes);

        int currentTime = 0;
        List<Process> readyQueue=new ArrayList<>();

        while(!queue.isEmpty() || !readyQueue.isEmpty()) {
        while(!queue.isEmpty() && queue.peek().getArrivalTime()<=currentTime) {
                readyQueue.add(queue.poll());
        }

            if (readyQueue.isEmpty()) {
                if (!queue.isEmpty()) {
                    currentTime = queue.peek().getArrivalTime();
                } else {
                    break;
                }
                continue;
            }

            for (Process p : readyQueue) {
               calculateDistance(p,getDisk());
            }


            readyQueue.sort(Comparator.comparingInt(Process::getDistance));
            Process process = readyQueue.remove(0);

            int movement = getDisk().moveTo(process.getCylinderNumber());
            currentTime += movement;

            process.setCompleted(true);
            addDoneProcess();
            addWaitTime(currentTime - process.getArrivalTime());
        }
    }

    public int calculateDistance(Process process, Disk disk) {
        int distance= Math.abs(process.getCylinderNumber()-disk.getCurrentPosition());
        process.setDistance(distance);
        return distance;
    }
}
