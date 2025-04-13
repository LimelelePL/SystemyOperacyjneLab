import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class FCFS extends Algoritm {

    public FCFS(Disk disk) {
        super(disk);
    }

    @Override
    public void run(List<Process> processes) {
        Queue<Process> queue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        queue.addAll(processes);
        Process process = null;

        while (!queue.isEmpty() || process != null) {

        if(getDisk().getTotalHeadMovements()>=queue.peek().getArrivalTime() && process==null) { //pobieram najwczesniejszy proces
            process = queue.poll();
            System.out.println("Pobieram proces " + process.getProcessName());
            System.out.println("aktualna pozycja " + getDisk().getCurrentPosition());
        }

            if(process!=null) { //przesuwamy glowice w odpowiednie miejsce
                if(process.getCylinderNumber()<getDisk().getCurrentPosition()){
                    getDisk().decreaseCurrentPosition();
                }else {
                    getDisk().increaseCurrentPosition();
                }
            }

            if(process==null) {
                getDisk().increaseHeadMovements();
            }

        if(process!=null) {
            if (completeProcesses(process)) {
                process.setCompleted(true);
                int waitTime = getDisk().getTotalHeadMovements() - process.getArrivalTime();
                process.setWaitTime(waitTime);
                addWaitTime(waitTime);
                addDoneProcess();
                process=null;
            }
        }

        }
    }
}
