import java.util.*;

public class Deadline extends Algoritm{
    public Deadline(int gcLatency, int gcTreshold){
        super(gcLatency, gcTreshold);
    }

    @Override
    public void run(List<Request> requests) {
        PriorityQueue<Request> arrivalQueue = new PriorityQueue<>(Comparator.comparing(Request::getArrivalTime));
        arrivalQueue.addAll(requests);

        LinkedList<Request> sectorQueue = new LinkedList<>();
        LinkedList<Request> deadlineQueue = new LinkedList<>();

        while (!arrivalQueue.isEmpty() || !sectorQueue.isEmpty() || !deadlineQueue.isEmpty()) {

            while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <= getTime()) {
                Request req = arrivalQueue.poll();
                sectorQueue.add(req);
                deadlineQueue.add(req);
            }
//sortujemy  kolejki wzgledem sektorów a nastepnie wzgledem deadlinów
            sectorQueue.sort(Comparator.comparing(Request::getSectorNumber));
            deadlineQueue.sort(Comparator.comparing(req -> req.getArrivalTime() + req.getDeadline()));
//zagłodzenie procesu na który po ktory nie zdarzymy
            if(!deadlineQueue.isEmpty()) {
                Request nextDeadlineRequest = deadlineQueue.getFirst();
                if (getTime() > nextDeadlineRequest.getArrivalTime() + nextDeadlineRequest.getDeadline()) {
                    incrementLostRequests(nextDeadlineRequest);
                    deadlineQueue.removeFirst();
                    sectorQueue.remove(nextDeadlineRequest);
                    continue;
                }
            }

// jezeli konczy sie deadline danego procesu to go obslugujemy
            if (shouldRunForDeadline(deadlineQueue)) {
                Request request = deadlineQueue.removeFirst();
                sectorQueue.remove(request);
                handleProcess(request);
                continue;
            }

//normalna obsluga
            if (!sectorQueue.isEmpty() && getTime() >= sectorQueue.getFirst().getArrivalTime()) {
                Request request = sectorQueue.removeFirst();
                deadlineQueue.remove(request);
                handleProcess(request);
                continue;
            }

            incrementTime();
        }
    }


    public boolean shouldRunForDeadline(List<Request> requests) {
        if (requests.isEmpty()) {
            return false;
        }
        Request req = requests.getFirst();
        // Jeśli deadline tego requesta jest blisko lub już na niego czas
        return getTime() >= req.getArrivalTime() + req.getDeadline() - getDisk().getWriteLatency();
    }
}
