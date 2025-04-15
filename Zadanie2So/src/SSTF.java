import java.util.*;

public class SSTF extends Algoritm {
    public SSTF(Disk disk) {
        super(disk);
    }

    @Override
    public void run(List<Process> processes) {
        Queue<Process> queue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        queue.addAll(processes);
        List<Process> readyQueue = new ArrayList<>();
        Process process=null;

        while (!queue.isEmpty() || !readyQueue.isEmpty() || process != null) {

            while (!queue.isEmpty() && getDisk().getTotalHeadMovements() >= queue.peek().getArrivalTime()) {
                int distance = calculateDistance(queue.peek());
                readyQueue.add(queue.poll());
            }

            if (readyQueue.isEmpty() && !queue.isEmpty()) { //niestety trzeba troche oszukac w pewnych przypadkach
                getDisk().advanceTime(queue.peek().getArrivalTime() - getDisk().getTotalHeadMovements());
            }

            readyQueue.sort(Comparator.comparingInt(Process::getDistance));

            if(!readyQueue.isEmpty()) { //kieruje glowice do elementu o najkrótszym dystansie
                if(readyQueue.getFirst().getCylinderNumber()<getDisk().getCurrentPosition()){
                    getDisk().decreaseCurrentPosition();
                } else if (readyQueue.getFirst().getCylinderNumber()>getDisk().getCurrentPosition()){
                    getDisk().increaseCurrentPosition();
                } else {
                    process=readyQueue.removeFirst();//tutaj glowica jest na miejscu szukanego procesu- poberam go i usuwam z kolejki
                }

                if(process!=null) {
                    if (completeProcesses(process)) {
                        int waitTime = getDisk().getTotalHeadMovements() - process.getArrivalTime();
                        process.setWaitTime(waitTime);
                        if(getAverageWaitTime()>getStarvationTreshold()) {
                            setStarvationTreshold((int) (getAverageWaitTime()*120));
                            addWaitTime((int)getAverageWaitTime());
                        } else addWaitTime(waitTime);
                        if (process.getWaitTime()>getStarvationTreshold()){
                            starve();
                        }
                        process.setCompleted(true);
                        addDoneProcess();
                        process=null;
                    }
                }
            }
        }
    }

    public int calculateDistance(Process process) {
        int distance = Math.abs(process.getCylinderNumber() - getDisk().getCurrentPosition());
        process.setDistance(distance);
        return distance;
    }
}

