package Processor;

public class Task {
    private boolean wasSuspended = false;
    private int load;
    private int remainingTime;
    private int arrivalTime;

    public Task(int load, int remainingTime, int arrivalTime) {
        this.load = load;
        this.remainingTime = remainingTime;
        this.arrivalTime = arrivalTime;

    }

    public Task(Task task) {
        this.load = task.load;
        this.remainingTime = task.remainingTime;
        this.arrivalTime = task.arrivalTime;
    }

    public boolean wasSuspended() { return wasSuspended; }
    public void setSuspended(boolean value) { wasSuspended = value; }

    public int getLoad() {
        return load;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
}
