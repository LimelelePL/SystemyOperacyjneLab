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

        while(!queue.isEmpty() || !readyQueue.isEmpty()) {
            while(!queue.isEmpty() && queue.peek().getArrivalTime()<=getDisk().getTotalHeadMovements()) {
                readyQueue.add(queue.poll());
            }
            getDisk().increaseCurrentPosition();
            readyQueue.sort(Comparator.comparingInt(Process::getCylinderNumber));

            for (Process p : readyQueue) {
                    if(p.getCylinderNumber()==getDisk().getCurrentPosition()){
                        process=p;
                        break;
                    }
                }
                readyQueue.remove(process);

                if(process!=null) {
                    if(completeProcesses(process)) {
                        process.setWaitTime(getDisk().getTotalHeadMovements() - process.getArrivalTime());
                        if (process.getWaitTime() >= getStarvationTreshold()) {
                            starve();
                            process.setCompleted(false);
                        }
                        process.setCompleted(true);
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
