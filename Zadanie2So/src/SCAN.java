import java.util.*;

public class SCAN extends Algoritm {
    private int returns = 0;
    private boolean goingUp = false;

    public SCAN(Disk disk) {
        super(disk);
    }

    @Override
    public void run(List<Process> processes) {
        Queue<Process> arrivalQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        arrivalQueue.addAll(processes);

        ArrayList<Process> readyQueue = new ArrayList<>();

        Process process = null;
        while(!readyQueue.isEmpty() || !arrivalQueue.isEmpty() || process!=null) {

            while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime()<=getDisk().getTotalHeadMovements()) {
                readyQueue.add(arrivalQueue.poll());
            }

            readyQueue.sort(Comparator.comparingInt(Process::getCylinderNumber));

            //poruszanie glowicy w zaleznosci czy ma isc w gore czy w dol
            if(goingUp) {
                getDisk().increaseCurrentPosition();
            } else {
                getDisk().decreaseCurrentPosition();
            }

            //pobieram tutaj proces

            if(process==null && !readyQueue.isEmpty()) {
                if(goingUp) {
                    for (int i=0; i<readyQueue.size(); i++) {
                        if(getDisk().getCurrentPosition()<=readyQueue.get(i).getCylinderNumber()){
                            process=readyQueue.remove(i);
                            break;
                        }
                    }
                } else {
                    for (int i=0; i<readyQueue.size(); i++) {
                        if(getDisk().getCurrentPosition()>=readyQueue.get(i).getCylinderNumber()){
                            process=readyQueue.remove(i);
                            break;
                        }
                    }
                }
            }

            if(process!=null) {
                if(completeProcesses(process)) {
                    int waittime=getDisk().getTotalHeadMovements() - process.getArrivalTime();
                    process.setWaitTime(waittime);
                    addWaitTime(waittime);
                    if (process.getWaitTime() > getStarvationTreshold()) {
                            starve();
                            process.setCompleted(false);
                    }
                    process.setCompleted(true);
                    addDoneProcess();
                    process=null;
                }
            }

            //dodawanie returnów oraz zmiana warunku czy idzie w gore czy w dol
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

}
