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
        int currentTime=0;

        while (!queue.isEmpty() || process != null) {

        if(process == null && !queue.isEmpty() && getDisk().getTotalHeadMovements()>=queue.peek().getArrivalTime()) { //pobieram najwczesniejszy proces
            process = queue.poll();
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
                int waitTime = getDisk().getTotalHeadMovements() - process.getArrivalTime();
                process.setWaitTime(waitTime);

                if(getAverageWaitTime()>getStarvationTreshold()) {
                    setStarvationTreshold((int) (getAverageWaitTime()*100));
                    addWaitTime((int)getAverageWaitTime());
                }else addWaitTime(waitTime);

                if(process.getWaitTime() >getStarvationTreshold()){
                    starve();
                    process.setCompleted(false);
                }
                    process.setCompleted(true);
                    addWaitTime(waitTime);
                    addDoneProcess();
                process=null;
            }
        }

        }
    }
}
