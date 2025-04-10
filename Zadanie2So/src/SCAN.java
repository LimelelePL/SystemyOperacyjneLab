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

        while (!arrivalQueue.isEmpty()) {
            Process p = arrivalQueue.poll();
            if (p.getCylinderNumber() < initialHead) {
                descendingQueue.add(p);
            } else {
                // Jeśli nie ma żadnych żądań w dół, to ustawienie kierunku w górę
                ascendingQueue.add(p);
            }
        }

        int currentTime = 0;
        boolean goingUp = false;

        if (!descendingQueue.isEmpty()) {
            currentTime = processQueue(descendingQueue, currentTime);
            if (getDisk().getCurrentPosition() != 0) {
                int movement = getDisk().moveTo(0);
                currentTime += movement;
            }
            goingUp = true;
            returns++;
        } else {
            goingUp = true;
        }

        if (!ascendingQueue.isEmpty()) {
            currentTime = processQueue(ascendingQueue, currentTime);
        }
    }

    private int processQueue(PriorityQueue<Process> queue, int currentTime) {
        while (!queue.isEmpty()) {
            Process p = queue.poll();
            int movement = getDisk().moveTo(p.getCylinderNumber());
            currentTime += movement;
            p.setCompleted(true);
            addDoneProcess();
            addWaitTime(currentTime - p.getArrivalTime());
        }
        return currentTime;
    }

    public int getReturns() {
        return returns;
    }
}
