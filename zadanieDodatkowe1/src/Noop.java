import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Noop extends Algoritm{

    public Noop(int gcLatency, int gcTreshold) {
        super(gcLatency, gcTreshold);
    }
    @Override
    public void run(List<Request> requests) {
        PriorityQueue<Request> queue = new PriorityQueue<>(Comparator.comparing(Request::getArrivalTime));
        queue.addAll(requests);

        Request request = null;

        while (!queue.isEmpty() || request != null) {

            if (!queue.isEmpty() && getTime() >= queue.peek().getArrivalTime()) {
                request = queue.poll();
            }

            if (request != null) {
                handleProcess(request);

                int finishTime   = getTime();
                int responseTime = finishTime - request.getArrivalTime();
                if (responseTime > request.getDeadline()) {
                    incrementLostRequests(request);
                }

                request = null;
            }

            incrementTime();
        }
        }
    }

