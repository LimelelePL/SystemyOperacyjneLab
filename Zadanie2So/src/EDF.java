import java.util.*;

public class EDF extends Algoritm {

    public EDF(Disk disk) {
        super(disk);
    }

    @Override
    public void run(List<Process> processes) {
        Queue<Process> queue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        queue.addAll(processes);
        List<Process> readyQueue = new ArrayList<>();
        Process currentProcess = null;

        while (!queue.isEmpty() || !readyQueue.isEmpty() || currentProcess != null) {

            while (!queue.isEmpty() && getDisk().getTotalHeadMovements() >= queue.peek().getArrivalTime()) {
                Process p = queue.poll();
                calculateDistance(p);
                readyQueue.add(p);
            }

            if (readyQueue.isEmpty() && !queue.isEmpty()) {
                int shift = queue.peek().getArrivalTime() - getDisk().getTotalHeadMovements();
                getDisk().advanceTime(shift);
            }

           // starve(readyQueue);

            readyQueue.sort((p1, p2) -> {

                if (p1.isRealTime() && !p2.isRealTime()) return -1;
                if (!p1.isRealTime() && p2.isRealTime()) return 1;

                if (p1.isRealTime() && p2.isRealTime()) {
                    return Integer.compare(p1.getDeadline(), p2.getDeadline());
                }

                return Integer.compare(p1.getDistance(), p2.getDistance());
            });


            //bierzemy 1 proces z readyqueue
            if (!readyQueue.isEmpty()) {
                Process first = readyQueue.get(0);


             //przesuwany glowice
                if (first.getCylinderNumber() < getDisk().getCurrentPosition()) {
                    getDisk().decreaseCurrentPosition();
                } else if (first.getCylinderNumber() > getDisk().getCurrentPosition()) {
                    getDisk().increaseCurrentPosition();
                } else {
                    // Głowica dotarła do cylindra
                    currentProcess = readyQueue.remove(0);
                }

                if (currentProcess != null) {
                    if (completeProcesses(currentProcess)) {
                        currentProcess.setCompleted(true);
                        int waitTime = getDisk().getTotalHeadMovements() - currentProcess.getArrivalTime();
                        currentProcess.setWaitTime(waitTime);

                        if(currentProcess.isRealTime()){
                            if(waitTime>currentProcess.getDeadline()){
                                starve();
                            }
                        }
                        if(getAverageWaitTime()>getStarvationTreshold()) {
                            setStarvationTreshold((int) (getAverageWaitTime()*100));
                        }
                        if(currentProcess.getWaitTime()>getStarvationTreshold()){
                            starve();
                        }

                        addWaitTime(waitTime);
                        addDoneProcess();

                        currentProcess = null;
                    }
                }
            }
        }
    }

    private void calculateDistance(Process process) {
        int distance = Math.abs(process.getCylinderNumber() - getDisk().getCurrentPosition());
        process.setDistance(distance);
    }


}
