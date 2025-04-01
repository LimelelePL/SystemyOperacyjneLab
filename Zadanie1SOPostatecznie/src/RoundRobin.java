import java.util.*;

public class RoundRobin implements Scheduler {
    private PriorityQueue<Process> processQueue;
    private double avgWaitingTime;
    private double avgTurnaroundTime;
    private double sumOfProcessSwaps;
    private int quantum;
    private int starvedProcesses = 0;
    private double medianWaitingTime = 0;
    private double medianTurnaroundTime = 0;

    public RoundRobin() {
        processQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        avgWaitingTime = 0;
        avgTurnaroundTime = 0;
        sumOfProcessSwaps = 0;
    }

    List<Integer> waitingTimes = new ArrayList<>();
    List<Integer> turnaroundTimes = new ArrayList<>();

    @Override
    public void run(PriorityQueue<Process> inputQueue) {
        this.processQueue.addAll(inputQueue);

        Queue<Process> readyQueue = new LinkedList<>();
        int size = processQueue.size();
        int time = 0;
        int sumOfWaitingTime = 0;
        int sumOfTurnaroundTime = 0;
        int timeCounter = 0;

        Process currentProcess = null;

        while (!readyQueue.isEmpty() || currentProcess != null || !processQueue.isEmpty()) {
//dodawanie do kolejki gotowych procesów
            while (!processQueue.isEmpty() && processQueue.peek().getArrivalTime() <= time) {
                readyQueue.add(processQueue.poll());
            }
// jezeli proces sie skonczyl lub musial zostac zmieniont to pobieramy nowy
            if (currentProcess == null) {
                if (!readyQueue.isEmpty()) { //z kolejki procesów i ustawiamy czas odliczania do uzyskania wymaganego kwantu na 0
                    currentProcess = readyQueue.poll();
                    timeCounter = 0;
                    sumOfProcessSwaps++;
                } else if (!processQueue.isEmpty()) { // jak kolejka gotowych procesow jest pusta, czekamy na kolejny rpoces
                    time = processQueue.peek().getArrivalTime();
                    continue;
                }
            }

            time++;
            timeCounter++;
            currentProcess.setBurstTimeLeft(currentProcess.getBurstTimeLeft() - 1);

            if (currentProcess.getBurstTimeLeft() == 0) {

                currentProcess.setCompletionTime(time);
                currentProcess.setTurnaroundTime(time - currentProcess.getArrivalTime());
                currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());

                sumOfWaitingTime += currentProcess.getWaitingTime();
                sumOfTurnaroundTime += currentProcess.getTurnaroundTime();

                waitingTimes.add(currentProcess.getWaitingTime());
                turnaroundTimes.add(currentProcess.getTurnaroundTime());

                currentProcess = null;
                timeCounter = 0;
            } else if (timeCounter == quantum) { // gdy mija kwant to bierzemy kolejny proces
                readyQueue.add(currentProcess);
                currentProcess = null;
                timeCounter = 0;
            }
        }
        avgWaitingTime = ((double) sumOfWaitingTime / size);
        avgTurnaroundTime = ((double) sumOfTurnaroundTime / size);
        medianWaitingTime = calculateMedian(waitingTimes);
        medianTurnaroundTime = calculateMedian(turnaroundTimes);
    }

    private double calculateMedian(List<Integer> times) {
        Collections.sort(times);
        int n = times.size();
        if (n % 2 == 0) {
            return (times.get(n / 2 - 1) + times.get(n / 2)) / 2.0;
        } else {
            return times.get(n / 2);
        }
    }

    public double getMaxWaitingTime() {
        return Collections.max(waitingTimes);
    }

    public double getMaxTurnaroundTime() {
        return Collections.max(turnaroundTimes);
    }

    public double getMinTurnaroundTime() {
        return Collections.min(turnaroundTimes);
    }

    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }

    public int getStarvedProcesses() {
        return starvedProcesses;
    }

    public double getMedianTurnaroundTime() {
        return medianTurnaroundTime;
    }

    public double getMedianWaitingTime() {
        return medianWaitingTime;
    }

    public double getSumOfProcessSwaps() {
        return sumOfProcessSwaps;
    }

    public int getQuantum() {
        return quantum;
    }

    public PriorityQueue<Process> getProcessQueue() {
        return processQueue;
    }

    public double getAvgWaitingTime() {
        return avgWaitingTime;
    }

    public double getAvgTurnaroundTime() {
        return avgTurnaroundTime;
    }
}
