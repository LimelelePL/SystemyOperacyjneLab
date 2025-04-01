public class Process {
    private String name;
    private int arrivalTime;
    private int burstTime;
    private int burstTimeLeft;
    private int completionTime;
    private int waitingTime;
    private int turnaroundTime;
    private boolean countedAsStarved = false;

    public Process(String name, int arrivalTime, int burstTime) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.burstTimeLeft = burstTime;
    }

    public void setCountedAsStarved(boolean countedAsStarved) {
        this.countedAsStarved = countedAsStarved;
    }
    public boolean isCountedAsStarved() {
        return countedAsStarved;
    }


    public String getName() {
        return name;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getBurstTimeLeft() {
        return burstTimeLeft;
    }

    public void setBurstTimeLeft(int burstTimeLeft) {
        this.burstTimeLeft = burstTimeLeft;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }
}