public class Process implements Comparable<Process> {
    private String processName;
    private int arrivalTime;
    private int cylinderNumber;
    private int deadline;
    private boolean isRealTime;
    private boolean isStarved;
    private boolean isCompleted;
    private int distance;
    private int waitTime;

    public Process(String processName, int arrivalTime, int cylinderNumber, int deadline, boolean isRealTime) {
        this.processName = processName;
        this.arrivalTime = arrivalTime;
        this.cylinderNumber = cylinderNumber;
        this.deadline = deadline;
        this.isRealTime = isRealTime;
        this.isStarved = false;
        this.isCompleted = false;
        this.distance = 0;
        this.waitTime = 0;
    }

    public Process(String processName, int arrivalTime, int cylinderNumber) {
        this(processName, arrivalTime, cylinderNumber, Integer.MAX_VALUE, false);
    }

    public String getProcessName() {
        return processName;
    }
    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }
    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getCylinderNumber() {
        return cylinderNumber;
    }
    public void setCylinderNumber(int cylinderNumber) {
        this.cylinderNumber = cylinderNumber;
    }

    public int getDeadline() {
        return deadline;
    }
    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public boolean isRealTime() {
        return isRealTime;
    }
    public void setRealTime(boolean realTime) {
        isRealTime = realTime;
    }

    public boolean isStarved() {
        return isStarved;
    }
    public void setStarved(boolean starved) {
        isStarved = starved;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getDistance() {
        return distance;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getWaitTime() {
        return waitTime;
    }
    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public boolean isFeasible(int currentTime, int headPosition) {
        int travelTime = Math.abs(headPosition - this.cylinderNumber);
        return (currentTime + travelTime) <= deadline;
    }

    @Override
    public int compareTo(Process other) {
        return Integer.compare(this.arrivalTime, other.arrivalTime);
    }
}
