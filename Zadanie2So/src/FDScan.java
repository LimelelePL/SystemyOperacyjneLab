import java.util.*;

public class FDScan extends Algoritm {
    private int returns = 0;
    private boolean goingUp = false;

    public FDScan(Disk disk) {
        super(disk);
    }

    @Override
    public void run(List<Process> processes) {

        Queue<Process> arrivalQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        arrivalQueue.addAll(processes);


        ArrayList<Process> readyQueue = new ArrayList<>();
        ArrayList<Process> deadlineQueue = new ArrayList<>();

        Process process = null;
        Process target = null;

        while (!readyQueue.isEmpty() || !arrivalQueue.isEmpty() || process != null || target != null) {

            while (!arrivalQueue.isEmpty() &&
                    arrivalQueue.peek().getArrivalTime() <= getDisk().getTotalHeadMovements()) {
                readyQueue.add(arrivalQueue.poll());
            }

            readyQueue.sort(Comparator.comparingInt(Process::getCylinderNumber));

            for (int i = readyQueue.size() - 1; i >= 0; i--) {
                Process p = readyQueue.get(i);
                if (p.isRealTime()) {
                    deadlineQueue.add(p);
                    readyQueue.remove(i);
                }
            }

            deadlineQueue.sort(Comparator.comparingInt(Process::getDeadline));

            if (goingUp) {
                getDisk().increaseCurrentPosition();
            } else {
                getDisk().decreaseCurrentPosition();
            }


            // Sprawdzamy czy na bieżącym cylindrze znajdują się jakiekolwiek zadania z obu kolejek.
            List<Process> currentCylinderTasks = new ArrayList<>();
            for (Iterator<Process> it = deadlineQueue.iterator(); it.hasNext();) {
                Process p = it.next();
                if (p.getCylinderNumber() == getDisk().getCurrentPosition()) {
                    currentCylinderTasks.add(p);
                    it.remove();
                }
            }
            // Normalne zadania:
            for (Iterator<Process> it = readyQueue.iterator(); it.hasNext();) {
                Process p = it.next();
                if (p.getCylinderNumber() == getDisk().getCurrentPosition()) {
                    currentCylinderTasks.add(p);
                    it.remove();
                }
            }
            // Obsłużmy wszystkie zadania, które znalazły się na bieżącym cylindrze
            for (Process p : currentCylinderTasks) {
                if (completeProcesses(p)) {
                    int waitTime = getDisk().getTotalHeadMovements() - p.getArrivalTime();
                    p.setWaitTime(waitTime);
                    addWaitTime(waitTime);
                    if(getAverageWaitTime() > getStarvationTreshold()) {
                        setStarvationTreshold((int)(getAverageWaitTime() * 10));
                    }
                    if (p.getWaitTime() > getStarvationTreshold()) {
                        starve();
                        p.setCompleted(false);
                    } else {
                        p.setCompleted(true);
                        addDoneProcess();
                    }
                }
            }

            if (target == null && !deadlineQueue.isEmpty()) {
                if (goingUp) {
                    for (int i = 0; i < deadlineQueue.size(); i++) {
                        Process dt = deadlineQueue.get(i);
                        if (getDisk().getCurrentPosition() <= dt.getCylinderNumber()) {
                            if (isFeasible(dt, getDisk().getCurrentPosition(), getDisk().getTotalHeadMovements())) {
                                target = deadlineQueue.remove(i);
                                break;
                            } else {
                                deadlineQueue.remove(i);
                                starve();
                                i--;
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < deadlineQueue.size(); i++) {
                        Process dt = deadlineQueue.get(i);
                        if (getDisk().getCurrentPosition() >= dt.getCylinderNumber()) {
                            if (isFeasible(dt, getDisk().getCurrentPosition(), getDisk().getTotalHeadMovements())) {
                                target = deadlineQueue.remove(i);
                                break;
                            } else {
                                deadlineQueue.remove(i);
                                starve();
                                i--;
                            }
                        }
                    }
                }
            }

           // Jeśli nie ma RT procesu, wybieramy normalny proces ze readyQueue
            if (process == null && !readyQueue.isEmpty()) {
                if (goingUp) {
                    for (int i = 0; i < readyQueue.size(); i++) {
                        Process p = readyQueue.get(i);
                        if (getDisk().getCurrentPosition() <= p.getCylinderNumber()) {
                            process = readyQueue.remove(i);
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < readyQueue.size(); i++) {
                        Process p = readyQueue.get(i);
                        if (getDisk().getCurrentPosition() >= p.getCylinderNumber()) {
                            process = readyQueue.remove(i);
                            break;
                        }
                    }
                }
            }

            //obsługa procesu realtime
            if (target != null) {
                if (completeProcesses(target)) {
                    int waitTime = getDisk().getTotalHeadMovements() - target.getArrivalTime();
                    target.setWaitTime(waitTime);
                    addWaitTime(waitTime);
                    if(getAverageWaitTime() > getStarvationTreshold()) {
                        setStarvationTreshold((int)(getAverageWaitTime() * 100));
                    }
                    if (target.getWaitTime() > getStarvationTreshold()) {
                        starve();
                        target.setCompleted(false);
                    } else {
                        target.setCompleted(true);
                        addDoneProcess();
                    }
                    target = null;
                }
            }

            if (process != null) {
                if (completeProcesses(process)) {
                    int waitTime = getDisk().getTotalHeadMovements() - process.getArrivalTime();
                    process.setWaitTime(waitTime);
                    addWaitTime(waitTime);
                    if(getAverageWaitTime() > getStarvationTreshold()) {
                        setStarvationTreshold((int)(getAverageWaitTime() * 100));
                    }
                    if (process.getWaitTime() > getStarvationTreshold()) {
                        starve();
                        process.setCompleted(false);
                    } else {
                        process.setCompleted(true);
                        addDoneProcess();
                    }
                    process = null;
                }
            }

            if (goingUp && getDisk().getCurrentPosition() >= getDisk().getMaxPosition()) {
                goingUp = false;
                returns++;
            } else if (!goingUp && getDisk().getCurrentPosition() <= 0) {
                goingUp = true;
                returns++;
            }
        }
    }

    public int getReturns() {
        return returns;
    }

    public boolean isGoingUp() {
        return goingUp;
    }

    private boolean isFeasible(Process req, int headPosition, int totalHeadMovements) {
        int timeToReach = Math.abs(req.getCylinderNumber() - headPosition);
        int finishTime = totalHeadMovements + timeToReach;
        return finishTime <= req.getDeadline();
    }
}
