import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Algoritm {
    private double avgWaitime;
    private double worstWaitime;
    private int lostRequests;
    private int Gcs;
    private int time;
    private Disk disk;
    private ArrayList<Integer> waitTimes;

    public Disk getDisk() {
        return disk;
    }

    public Algoritm(int gcLatency, int gcTreshold) {
        disk = new Disk(gcLatency, gcTreshold, 20, 50);
        this.avgWaitime = 0;
        this.worstWaitime = 0;
        this.lostRequests = 0;
        this.Gcs = 0;
        this.time = 0;
        this.waitTimes = new ArrayList<>();
        resetStats();
    }

    public void resetStats(){
        this.avgWaitime = 0;
        this.worstWaitime = 0;
        this.lostRequests = 0;
        this.Gcs = 0;
        this.time = 0;
        getDisk().resetWritesSinceLastGC();
    }

    public void waitTimesAdd(int waitTime){
        this.waitTimes.add(waitTime);
    }

    public void handleProcess(Request request) {
        if (request.getType().equals("WRITE")) {
            time += disk.getWriteLatency();
            getDisk().incrementWritesSinceLastGC();

            if (getDisk().getWritesSinceLastGC() == getDisk().getGcTreshold()) {
                time += disk.getGcLatency();
                incrementGcs();
                getDisk().resetWritesSinceLastGC();
            }
        } else {
            time += disk.getReadLatency();
        }

        waitTimes.add(time - request.getArrivalTime());
    }

    public void handleProcessWithoutGc(Request request) {
        if (request.getType().equals("WRITE")) {
            time += disk.getWriteLatency();
            getDisk().incrementWritesSinceLastGC();
            if (getDisk().getWritesSinceLastGC() == getDisk().getGcTreshold()) {
                incrementGcs();
                getDisk().resetWritesSinceLastGC();
            }
        } else {
            time += disk.getReadLatency();
        }

        waitTimes.add(time - request.getArrivalTime());
    }

    public void calculateAvgWaitTime() {
        double times=0;
        for (int i = 0; i < waitTimes.size(); i++) {
            times += waitTimes.get(i);
        }
        avgWaitime = times / waitTimes.size();
    }

    public void incrementTime(){
        time++;
    }

    public void calculateWorstWaitTime() {
        worstWaitime = Collections.max(waitTimes);
    }

    public void incrementLostRequests(Request request) {
        lostRequests++;
        waitTimes.add(time- request.getArrivalTime());
    }

    public void incrementGcs() {
        Gcs++;
    }

    public double getWorstWaitime() {
        return worstWaitime;
    }

    public void setWorstWaitime(double worstWaitime) {
        this.worstWaitime = worstWaitime;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getLostRequests() {
        return lostRequests;
    }

    public void setLostRequests(int lostRequests) {
        this.lostRequests = lostRequests;
    }

    public int getGcs() {
        return Gcs;
    }

    public void setGcs(int gcs) {
        Gcs = gcs;
    }

    public double getAvgWaitime() {
        return avgWaitime;
    }

    public void setAvgWaitime(double avgWaitime) {
        this.avgWaitime = avgWaitime;
    }

    public abstract void run(List<Request> requests);
}
