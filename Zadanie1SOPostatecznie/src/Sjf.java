import java.util.*;

public class Sjf implements Scheduler {
    private PriorityQueue<Process> processQueue;
    private double avgWaitingTime;
    private double avgTurnaroundTime;
    private double sumOfProcessSwaps;
    private double medianWaitingTime = 0;
    private double medianTurnaroundTime = 0;
    private int starvedProcesses = 0;

    public Sjf() {
        avgWaitingTime = 0;
        avgTurnaroundTime = 0;
        sumOfProcessSwaps = 0;
    }

    List<Integer> waitingTimes = new ArrayList<>();
    List<Integer> turnaroundTimes = new ArrayList<>();

    public void run(PriorityQueue<Process> processQueue) {
        this.processQueue = new PriorityQueue<>(processQueue);

        int size = processQueue.size();
        int time = 0;
        int sumOfWaitingTime = 0;
        int sumOfTurnaroundTime = 0;
        int starvationTreshold = 20000;

        PriorityQueue<Process> readyQueue = new PriorityQueue<>(Comparator.comparing(Process::getBurstTime));

        Process currentProcess = null;

        while (!processQueue.isEmpty() || !readyQueue.isEmpty() || currentProcess != null) {
            //tworzymy kolejke gotowych procesow
            while (!processQueue.isEmpty() && processQueue.peek().getArrivalTime() <= time) {
                readyQueue.add(processQueue.poll());
            }

            if (currentProcess == null) {
                if (!readyQueue.isEmpty()) {
                    currentProcess = readyQueue.poll();
                    sumOfProcessSwaps++;
                } else if (!processQueue.isEmpty()) {
                    time = processQueue.peek().getArrivalTime();
                    continue;
                }
            }

            for (Process p : readyQueue) {
                if (!p.isCountedAsStarved() && (time - p.getArrivalTime() > starvationTreshold)) {
                    starvedProcesses++;
                    p.setCountedAsStarved(true);
                }
            }

            time++;
            currentProcess.setBurstTimeLeft(currentProcess.getBurstTimeLeft() - 1);

            if (currentProcess.isCountedAsStarved()) {
                currentProcess.setCompletionTime(starvationTreshold);
                currentProcess.setTurnaroundTime(starvationTreshold - currentProcess.getArrivalTime());
                currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());

                sumOfWaitingTime += currentProcess.getWaitingTime();
                sumOfTurnaroundTime += currentProcess.getTurnaroundTime();

                waitingTimes.add(currentProcess.getWaitingTime());
                turnaroundTimes.add(currentProcess.getTurnaroundTime());

                currentProcess = null;
                continue;
            }

            if (currentProcess.getBurstTimeLeft() == 0) {

                currentProcess.setCompletionTime(time);
                currentProcess.setTurnaroundTime(time - currentProcess.getArrivalTime());
                currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());

                sumOfWaitingTime += currentProcess.getWaitingTime();
                sumOfTurnaroundTime += currentProcess.getTurnaroundTime();

                waitingTimes.add(currentProcess.getWaitingTime());
                turnaroundTimes.add(currentProcess.getTurnaroundTime());

                currentProcess = null;
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

    public int getStarvedProcesses() {
        return starvedProcesses;
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


    public double getAvgTurnaroundTime() {
        return avgTurnaroundTime;
    }

    public double getAvgWaitingTime() {
        return avgWaitingTime;
    }

    public PriorityQueue<Process> getProcessQueue() {
        return processQueue;
    }

    public double getSumOfProcessSwaps() {
        return sumOfProcessSwaps;
    }

    public double getMedianTurnaroundTime() {
        return medianTurnaroundTime;
    }

    public double getMedianWaitingTime() {
        return medianWaitingTime;
    }

}

