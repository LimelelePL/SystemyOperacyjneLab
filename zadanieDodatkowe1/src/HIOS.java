import java.util.*;

public class HIOS extends Algoritm{
    public HIOS(int gcLatency, int gcTreshold){
       super(gcLatency,gcTreshold);
   }

    @Override
    public void run(List<Request> requests) {
        PriorityQueue<Request> arrivalQueue = new PriorityQueue<>(Comparator.comparing(Request::getArrivalTime));
        arrivalQueue.addAll(requests);

        LinkedList<Request> pendingQueue = new LinkedList<>();
        Request currentRequest = null;

        while (!arrivalQueue.isEmpty() || !pendingQueue.isEmpty() || currentRequest != null) {
            while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <= getTime()) {
                pendingQueue.add(arrivalQueue.poll());
            }

            if (pendingQueue.isEmpty() && currentRequest == null) {
                incrementTime();
                continue;
            }

            if (currentRequest == null && !pendingQueue.isEmpty()) {
                pendingQueue.sort(Comparator.comparing(Request::getArrivalTime));
                currentRequest = pendingQueue.pollFirst();
            }

            if (currentRequest != null) {
                if (isNegative(currentRequest)) {
                    if (!tryRedistributeGC(currentRequest, pendingQueue)) {
                        incrementLostRequests(currentRequest);
                        currentRequest = null;
                        continue;
                    }
                }

                handleProcessWithoutGc(currentRequest);
                checkDeadline(currentRequest);
                currentRequest = null;
            }

            incrementTime();
        }
    }

    private boolean isNegative(Request request) {
        int predictedService = request.getType().equals("WRITE") ? getDisk().getWriteLatency() : getDisk().getReadLatency();

        if (request.getType().equals("WRITE") && (getDisk().getWritesSinceLastGC() + 1 >= getDisk().getGcTreshold())) {
            predictedService += getDisk().getGcLatency();
        }

        int predictedFinish = getTime() + predictedService;
        int absoluteDeadline = request.getArrivalTime() + request.getDeadline();

        return predictedFinish > absoluteDeadline;
    }

    private boolean tryRedistributeGC(Request criticalRequest, LinkedList<Request> pendingQueue) {
        if (!(criticalRequest.getType().equals("WRITE"))) {
            return false;
        }

        if (getDisk().getWritesSinceLastGC() + 1 < getDisk().getGcTreshold()) {
            return true;
        }

        int gcLatency = getDisk().getGcLatency();
        List<Request> candidates = new ArrayList<>();
        int accumulatedSlack = 0;

        for (Request r : pendingQueue) {
            if (calculateSlack(r) > 0) {
                candidates.add(r);
                accumulatedSlack += calculateSlack(r);
                if (accumulatedSlack >= gcLatency) {
                    break;
                }
            }
        }
        if (accumulatedSlack < gcLatency) {
            return false;
        }

        int gcPerRequest = gcLatency / candidates.size();

        for (Request r : candidates) {
            setTime(getTime()+gcPerRequest);

            handleProcessWithoutGc(r);
            checkDeadline(r);

            pendingQueue.remove(r);
        }

        getDisk().resetWritesSinceLastGC();

        return true;
    }

    private int calculateSlack(Request request) {
        int serviceTime = request.getType().equals("WRITE") ? getDisk().getWriteLatency() : getDisk().getReadLatency();
        int absoluteDeadline = request.getArrivalTime() + request.getDeadline();
        return absoluteDeadline - (getTime() + serviceTime);
    }

    private void checkDeadline(Request request) {
        int finishTime = getTime();
        int responseTime = finishTime - request.getArrivalTime();

        if (responseTime > request.getDeadline()) {
            incrementLostRequests(request);
        }
    }
}

