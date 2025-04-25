import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Algoritm {
    private double avgWaitime;
    private double worstWaitime;
    private double lostRequests;

    public void calculateAvgWaitTime(ArrayList<Integer> waitTimes) {
        double times=0;
        for (int i = 0; i < waitTimes.size(); i++) {
            times += waitTimes.get(i);
        }

        avgWaitime = times / waitTimes.size();
    }

    public void calculateWorstWaitTime(ArrayList<Integer> waitTimes) {
        worstWaitime = Collections.max(waitTimes);
    }

    public void incrementLostRequests() {
        lostRequests++;
    }

    public abstract void run(List<Request> requests);
}
