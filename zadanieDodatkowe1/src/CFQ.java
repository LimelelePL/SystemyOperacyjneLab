import java.util.*;

public class CFQ extends Algoritm {

    public CFQ() {
        super();
    }

    @Override
    public void run(List<Request> requests) {
        PriorityQueue<Request> arrivalQueue = new PriorityQueue<>(Comparator.comparing(Request::getArrivalTime));
        arrivalQueue.addAll(requests);

        Map<Integer, Queue<Request>> processQueuesMap = new HashMap<>();
        List<Queue<Request>> processQueues = new ArrayList<>();

        while (!arrivalQueue.isEmpty() || !allQueuesEmpty(processQueues)) {

            // Przenoszenie nowych requestów do odpowiednich kolejek
            while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <= getTime()) {
                Request req = arrivalQueue.poll();
                processQueuesMap.putIfAbsent(req.getID(), new LinkedList<>());
                processQueuesMap.get(req.getID()).add(req);

                if (!processQueues.contains(processQueuesMap.get(req.getID()))) {
                    processQueues.add(processQueuesMap.get(req.getID()));
                }
            }

            for (Queue<Request> queue : processQueues) {
                if (!queue.isEmpty()) {
                    Request request = queue.peek();
                    if (getTime() >= request.getArrivalTime()) {
                        request = queue.poll();

                        if (getTime() > request.getArrivalTime() + request.getDeadline()) {
                            incrementLostRequests(request);
                        }
                        handleProcess(request);
                    }
                }
            }

            incrementTime();
        }
    }

    private boolean allQueuesEmpty(List<Queue<Request>> queues) {
        for (Queue<Request> queue : queues) {
            if (!queue.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}

