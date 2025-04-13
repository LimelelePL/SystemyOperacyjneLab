import java.util.*;

public class CSCAN extends Algoritm{
    private int returns=0;
    public CSCAN(Disk disk) {
        super(disk);
    }

    @Override
    public void run(List<Process> processes) {
        Queue<Process> queue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        queue.addAll(processes);
        ArrayList<Process> readyQueue = new ArrayList<>();
        Process process=null;


        while(!queue.isEmpty() || !readyQueue.isEmpty() || process!=null)  {
            while(!queue.isEmpty() && queue.peek().getArrivalTime()<=getDisk().getTotalHeadMovements()) {
                readyQueue.add(queue.poll());
            }

            readyQueue.sort(Comparator.comparingInt(Process::getCylinderNumber));

            if (readyQueue.isEmpty() && !queue.isEmpty()) { //niestety trzeba troche oszukac w pewnych przypadkach
                getDisk().advanceTime(queue.peek().getArrivalTime() - getDisk().getTotalHeadMovements());
            }


            for (int k=0; k<readyQueue.size(); k++) {
                Process p = readyQueue.get(k);
                    if(p.getCylinderNumber()==getDisk().getCurrentPosition()){
                        process=readyQueue.remove(k);
                        break;
                    }
                }
            getDisk().increaseCurrentPosition();
                if(process!=null) {
                    if(completeProcesses(process)) {
                        process.setWaitTime(getDisk().getTotalHeadMovements() - process.getArrivalTime());
                        if (process.getWaitTime() >= getStarvationTreshold()) {
                            starve();
                        }
                        process.setCompleted(true);
                        addWaitTime(getDisk().getTotalHeadMovements() - process.getArrivalTime());
                        addDoneProcess();
                        process=null;
                    }
                }

                if(getDisk().getCurrentPosition() == getDisk().getMaxPosition()) {
                    getDisk().setCurrentPosition(0);
                    returns++;
                }
            }

        }
    public int getReturns() {
        return returns;
    }
}
