import java.util.*;

public class SSTF extends Algoritm implements Scheduler {
    private final SSTFMode mode;

    public enum SSTFMode {
        NORMAL,
        EDF
    }

    public SSTF(Disk disk, SSTFMode mode) {
        super(disk);
        this.mode = mode;
    }

    @Override
    public void run(List<Process> processes) {
        Queue<Process> queue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        queue.addAll(processes);

        List<Process> readyQueue = new ArrayList<>();

        while (!queue.isEmpty() || !readyQueue.isEmpty()) {
            int currentTime = getDisk().getTotalHeadMovements();

            // Dodaj procesy, które już "przyszły"
            while (!queue.isEmpty() && queue.peek().getArrivalTime() <= currentTime) {
                readyQueue.add(queue.poll());
            }

            if (readyQueue.isEmpty()) {
                // Jeśli nic nie gotowe, przesuń czas do arrivalTime następnego procesu
                if (!queue.isEmpty()) {
                    int nextArrival = queue.peek().getArrivalTime();
                    int delta = nextArrival - currentTime;
                    if (delta > 0) {
                        getDisk().moveTo(getDisk().getCurrentPosition()); // żeby czas ruszył
                    }
                }
                continue;
            }

            Process process = null;

            if (mode == SSTFMode.EDF) {
                // Szukamy real-time'ów z najkrótszym deadline
                List<Process> realTime = new ArrayList<>();
                for (Process p : readyQueue) {
                    if (p.isRealTime()) {
                        realTime.add(p);
                    }
                }

                if (!realTime.isEmpty()) {
                    realTime.sort(Comparator.comparingInt(Process::getDeadline));
                    process = realTime.get(0);
                }
            }

            if (process == null) {
                // Jeśli nie ma real-time (lub tryb NORMAL), wybieramy najbliższy cylinder
                for (Process p : readyQueue) {
                    calculateDistance(p, getDisk());
                }
                readyQueue.sort(Comparator.comparingInt(Process::getDistance));
                process = readyQueue.get(0);
            }

            readyQueue.remove(process);

            int movement = getDisk().moveTo(process.getCylinderNumber());

            int waitTime = getDisk().getTotalHeadMovements() - process.getArrivalTime();
            process.setWaitTime(waitTime);

            if (waitTime >= getStarvationTreshold()) {
                starve();
                process.setCompleted(false);
            } else {
                process.setCompleted(true);
                addDoneProcess();
            }

            addWaitTime(waitTime);
        }
    }

    public int calculateDistance(Process process, Disk disk) {
        int distance = Math.abs(process.getCylinderNumber() - disk.getCurrentPosition());
        process.setDistance(distance);
        return distance;
    }
}

