import java.util.*;

public class CSCAN extends Algoritm implements Scheduler{
    private int returns=0;
    public CSCAN(Disk disk) {
        super(disk);
    }

    @Override
    public void run(List<Process> processes) {
        Queue<Process> queue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        queue.addAll(processes);
        ArrayList<Process> readyQueue = new ArrayList<>();

        while(!queue.isEmpty() || !readyQueue.isEmpty()) {
            while(!queue.isEmpty() && queue.peek().getArrivalTime()<=getDisk().getTotalHeadMovements()) {
                readyQueue.add(queue.poll());
                System.out.println("aktualna pozcyja: " + getDisk().getCurrentPosition() + " dodaje proces " +readyQueue.getLast().getProcessName());
            }
            readyQueue.sort(Comparator.comparingInt(Process::getCylinderNumber));
            Process process=null;

            for (Process p : readyQueue) {
                    if(p.getCylinderNumber()>=getDisk().getCurrentPosition()){
                        process=p;
                        System.out.println("wyszukano proces " + process.getProcessName());
                        break;
                    }
                }
                readyQueue.remove(process);
            if(process!=null) {
                System.out.println("pobrano proces " + process.getProcessName());
            }

                if(process!=null) {
                    int movement = getDisk().moveTo(process.getCylinderNumber());
                    process.setWaitTime(getDisk().getTotalHeadMovements()-process.getArrivalTime());
                    if(process.getWaitTime()>=getStarvationTreshold()){
                        starve();
                        process.setCompleted(false);
                    }
                    process.setCompleted(true);
                    System.out.println("Ukończono proces " + process.getProcessName());
                    addDoneProcess();
                }

                if(getDisk().getCurrentPosition() == getDisk().getMaxPosition()) {
                    getDisk().setCurrentPosition(0);
                    returns++;
                    System.out.println("RETURN");
                    System.out.println("STAN PO RETURN " + getDisk().getTotalHeadMovements());
                } else if(process==null){
                    getDisk().moveTo(getDisk().getMaxPosition());
                    getDisk().setCurrentPosition(0);
                    returns++;
                    System.out.println("RETURN");
                    System.out.println("STAN PO RETURN " + getDisk().getTotalHeadMovements());
                }
            process=null;
            }

        }
    public int getReturns() {
        return returns;
    }
}
