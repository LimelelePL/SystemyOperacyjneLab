import java.util.List;
import java.util.PriorityQueue;

public abstract class Algoritm {
    private int starvedProcesses;
    private int starvationTreshold;
    private Disk disk;
    private double totalWaitingTime;
    private int processesDone;

    public Algoritm(Disk disk) {
        this.starvedProcesses = 0;
        this.disk = disk;
        this.totalWaitingTime = 0;
        this.starvationTreshold = 1000000;
        this.processesDone = 0;
    }

    public abstract void run(List<Process> processes);

    public boolean completeProcesses(Process process) {
        return (getDisk().getCurrentPosition()==process.getCylinderNumber());
    }

    public double getTotalWaitTime() {
        return totalWaitingTime;
    }

    public double getAverageWaitTime() {
        return processesDone == 0 ? 0 : (double) totalWaitingTime / processesDone;
    }

    public void addWaitTime(int waitTime) {
        totalWaitingTime += waitTime;
    }

    public void addDoneProcess() {
        processesDone++;
    }

    public Disk getDisk() {
        return disk;
    }

    public int getStarvationTreshold() {
        return starvationTreshold;
    }

    public void setStarvationTreshold(int starvationTreshold) {
        this.starvationTreshold = starvationTreshold;
    }


    public int getStarvedProcesses() {
        return starvedProcesses;
    }

    public void starve() {
        this.starvedProcesses++;
    }

    public void reset(){
        this.starvedProcesses = 0;
        this.starvationTreshold = 20000;
        this.totalWaitingTime = 0;
        this.processesDone = 0;
    }
}
