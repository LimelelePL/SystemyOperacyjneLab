import java.util.*;

public class HIOS extends Algoritm {
    public HIOS(int gcLatency, int gcTreshold) {
        super(gcLatency, gcTreshold);
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

            // sortujemy kolejke po slacku i bierzemy z niej pierwszy proces, tzn najpilniejszy
            if (currentRequest == null && !pendingQueue.isEmpty()) {
                pendingQueue.sort(Comparator.comparing(this::calculateSlack));
                currentRequest = pendingQueue.pollFirst();
            }

            //jezeli slack procesu jest ujemny to rozbijamy GC latency na reszte procesów
            if (currentRequest != null) {
                if (isNegative(currentRequest)) {
                    if (!tryRedistributeGC(currentRequest, pendingQueue)) {
                      incrementLostRequests(currentRequest);
                        currentRequest = null;
                        continue;
                    }
                }
//jezeli proces jest sie w stanie wykonac bez reinstrybucji lub proces jest read
                handleProcessWithoutGc(currentRequest);
                checkDeadline(currentRequest);
                currentRequest = null;
            }

            incrementTime();
        }
    }

    //metoda ktora sprawdza ktore procesy w ogole sie oplaca wykonywac procedure podzialu gc dla danego proces
    private boolean isNegative(Request request) {
        int predictedService = request.getType().equals("WRITE") ? getDisk().getWriteLatency() : getDisk().getReadLatency();

        if (request.getType().equals("WRITE") && (getDisk().getWritesSinceLastGC() + 1 >= getDisk().getGcTreshold())) {
            predictedService += getDisk().getGcLatency();
        }

        int predictedFinish = getTime() + predictedService;
        int absoluteDeadline = request.getArrivalTime() + request.getDeadline();

        // slack bedzie ujemny czyli zwracamy true
        return predictedFinish > absoluteDeadline;
    }


    private boolean tryRedistributeGC(Request criticalRequest, LinkedList<Request> pendingQueue) {
        //gc dziala tylko dla writeow
        if (!(criticalRequest.getType().equals("WRITE"))) {
            return false;
        }

        //sprawdamy czy gc jest blisko jak nie to mozna sobie wykonywac bez reinstrybucji critical request
        if (getDisk().getWritesSinceLastGC() + 1 < getDisk().getGcTreshold()) {
            return true;
        }

//zbieramy procesy ktore po przesunieciu nie zlamia deadlinów i sumujemy ich slacki az przekorcza gc latency
        //w przeciwnym przypadku nie da sie miedzy nimi reinstrybuowac tego gc latency
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
        if (accumulatedSlack>0 && accumulatedSlack < gcLatency) {
            return false;
        }

        if(!candidates.isEmpty()) {
            int gcPerRequest = gcLatency / candidates.size();

            for (Request r : candidates) {
                setTime(getTime() + gcPerRequest);
                handleProcessWithoutGc(r);
                checkDeadline(r);
                pendingQueue.remove(r);
            }
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

