import java.util.*;

public class CFQ extends Algoritm {

    private final int timeSlice;

    public CFQ(int gcLatency, int gcTreshold) {
        super(gcLatency, gcTreshold);
        this.timeSlice = 100;
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
                int timeSpent = 0;
                while (!queue.isEmpty() && timeSpent < timeSlice) {
                    Request request = queue.peek();
                    if (request != null && getTime() >= request.getArrivalTime()) {
                        request = queue.poll();

                        if (getTime() > request.getArrivalTime() + request.getDeadline()) {
                            incrementLostRequests(request);
                        }
                        int processTime = handleProcessWithReturn(request);

                        timeSpent += processTime;
                    } else {
                        break; // Request jeszcze nie dotarł
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

    // Nowa metoda handleProcess z czasem obsługi
    public int handleProcessWithReturn(Request request) {
        int duration = 0;
        if (request.getType().equals("WRITE")) {
            duration = getDisk().getWriteLatency();
            setTime(getTime() + duration);
            getDisk().incrementWritesSinceLastGC();

            if (getDisk().getWritesSinceLastGC() == getDisk().getGcTreshold()) {
                setTime(getTime() + getDisk().getGcLatency());
                getDisk().resetWritesSinceLastGC();
                duration += getDisk().getGcLatency();
            }
        } else {
            duration = getDisk().getReadLatency();
            setTime(getTime() + duration);
        }
        waitTimesAdd(getTime() - request.getArrivalTime());
        return duration;
    }
}

