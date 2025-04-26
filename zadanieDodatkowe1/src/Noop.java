import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Noop extends Algoritm{

    public Noop() {
        super();
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
                if (getTime() > request.getArrivalTime() + request.getDeadline()) {
                    incrementLostRequests(request);
                }
                handleProcess(request);
                request = null;
            }

            incrementTime();
        }
    }
}

