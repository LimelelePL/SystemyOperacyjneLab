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

        // Pętla główna działa dopóki mamy jakieś zadania do przetworzenia
        while (!readyQueue.isEmpty() || !arrivalQueue.isEmpty() || process != null || target != null) {
            // Dodaj nowe przybycia do kolejki gotowych procesów
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

            // Zaktualizuj pozycję głowicy zgodnie z kierunkiem ruchu
            if (goingUp) {
                getDisk().increaseCurrentPosition();
            } else {
                getDisk().decreaseCurrentPosition();
            }

            // Wybór procesu real-time w zależności od kierunku
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

            // Jeśli nie ma docelowego procesu real-time, wybierz zwykły proces
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

            // Obsługa procesu real-time (target)
            if (target != null) {
                if (completeProcesses(target)) {
                    int waitTime = getDisk().getTotalHeadMovements() - target.getArrivalTime();
                    target.setWaitTime(waitTime);
                    addWaitTime(waitTime);
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

            // Obsługa zwykłego procesu
            if (process != null) {
                if (completeProcesses(process)) {
                    int waitTime = getDisk().getTotalHeadMovements() - process.getArrivalTime();
                    process.setWaitTime(waitTime);
                    addWaitTime(waitTime);
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

            // Sprawdzenie warunków zmiany kierunku, jeżeli głowica osiągnie krawędź dysku
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

    // Sprawdza, czy zadanie jest wykonalne przed upływem deadline'u
    private boolean isFeasible(Process req, int headPosition, int totalHeadMovements) {
        int timeToReach = Math.abs(req.getCylinderNumber() - headPosition);
        int finishTime = totalHeadMovements + timeToReach;
        return finishTime <= req.getDeadline();
    }
}
