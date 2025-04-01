import java.util.*;

public class Fcfs implements Scheduler {
    private PriorityQueue<Process> processQueue;
    private double avgWaitingTime;
    private double avgTurnaroundTime;
    private double sumOfProcessSwaps;
    private double medianWaitingTime = 0;
    private double medianTurnaroundTime = 0;
    List<Integer> waitingTimes = new ArrayList<>();
    List<Integer> turnaroundTimes = new ArrayList<>();

    public Fcfs() {
        avgWaitingTime = 0;
        avgTurnaroundTime = 0;
        sumOfProcessSwaps = 0;
    }

    @Override
    public void run(PriorityQueue<Process> inputQueue){
        this.processQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getArrivalTime));
        this.processQueue.addAll(inputQueue);

        int size= processQueue.size();
        int time= 0;
        int sumOfWaitingTime= 0;
        int sumOfTurnaroundTime= 0;

        Process process= processQueue.poll();
        while (process!= null){

            if (time<process.getArrivalTime()){
                time=process.getArrivalTime();
            }

            while (process.getBurstTimeLeft()>0){
                time++;
                process.setBurstTimeLeft(process.getBurstTimeLeft() - 1);
            }

            if (process.getBurstTimeLeft() == 0) {
                process.setCompletionTime(time);
                process.setTurnaroundTime(time - process.getArrivalTime());
                process.setWaitingTime(process.getTurnaroundTime() - process.getBurstTime());

                waitingTimes.add(process.getWaitingTime());
                turnaroundTimes.add(process.getTurnaroundTime());

                sumOfWaitingTime += process.getWaitingTime();
                sumOfTurnaroundTime += process.getTurnaroundTime();
                sumOfProcessSwaps++;

                process = processQueue.poll();
            }
        }

        avgWaitingTime= ((double) sumOfWaitingTime/size);
        avgTurnaroundTime= ((double) sumOfTurnaroundTime/size);
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

    public double getMaxWaitingTime(){
        return Collections.max(waitingTimes);
    }
    public double getMaxTurnaroundTime(){
        return Collections.max(turnaroundTimes);
    }
    public double getMinTurnaroundTime(){
        return Collections.min(turnaroundTimes);
    }

    public double getMedianTurnaroundTime() {
        return medianTurnaroundTime;
    }

    public double getMedianWaitingTime() {
        return medianWaitingTime;
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
}
