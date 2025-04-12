import java.util.*;

public class SCAN extends Algoritm implements Scheduler {

    public enum ScanMode {
        NORMAL,
        FD_SCAN
    }
    private int returns = 0;
    private final ScanMode mode;
    private boolean goingUp = false;

    public SCAN(Disk disk, ScanMode mode) {
        super(disk);
        this.mode = mode;
    }

    @Override
    public void run(List<Process> processes) {
        Queue<Process> arrivalQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        arrivalQueue.addAll(processes);

        PriorityQueue<Process> ascendingQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getCylinderNumber));
        PriorityQueue<Process> descendingQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getCylinderNumber).reversed());

        while (!arrivalQueue.isEmpty() || !ascendingQueue.isEmpty() || !descendingQueue.isEmpty()) {

            int currentTime = getDisk().getTotalHeadMovements();

            while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <= currentTime) {
                Process p = arrivalQueue.poll();

                if (mode == ScanMode.FD_SCAN && p.isRealTime()) {
                    int distance = Math.abs(getDisk().getCurrentPosition() - p.getCylinderNumber());
                    if (currentTime + distance <= p.getDeadline()) {
                        // feasible real-time
                        addToDirectionQueue(p, ascendingQueue, descendingQueue);
                    } else {
                        starve();
                        p.setCompleted(false); // zignorowany
                    }
                } else {
                    addToDirectionQueue(p, ascendingQueue, descendingQueue);
                }
            }

            Process process = null;

            if (goingUp) {
                if (!ascendingQueue.isEmpty()) {
                    process = ascendingQueue.poll();
                } else {
                    if (getDisk().getCurrentPosition() < getDisk().getMaxPosition()) {
                        getDisk().moveTo(getDisk().getMaxPosition());
                    }
                    goingUp = false;
                    returns++;
                    continue;
                }
            } else {
                if (!descendingQueue.isEmpty()) {
                    process = descendingQueue.poll();
                } else {
                    if (getDisk().getCurrentPosition() > 0) {
                        getDisk().moveTo(0);
                    }
                    goingUp = true;
                    returns++;
                    continue;
                }
            }

            if (process != null) {
                getDisk().moveTo(process.getCylinderNumber());
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
    }

    private void addToDirectionQueue(Process p, PriorityQueue<Process> ascending, PriorityQueue<Process> descending) {
        if (p.getCylinderNumber() >= getDisk().getCurrentPosition()) {
            ascending.add(p);
        } else {
            descending.add(p);
        }
    }

    public int getReturns() {
        return returns;
    }

    public ScanMode getMode() {
        return mode;
    }
}
