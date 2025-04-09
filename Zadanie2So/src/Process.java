public class Process {
    private String processName;
    private int arrivalTime;
    private int cylinderNumber;
    private int deadline;
    private boolean isStarved;
    private boolean isCompleted;
    private int distance;

    public Process(String processName, int arrivalTime, int cylinderNumber,int deadline) {
        this.processName = processName;
        this.arrivalTime = arrivalTime;
        this.cylinderNumber = cylinderNumber;
        this.deadline = deadline;
        this.isStarved = false;
        this.isCompleted = false;
        this.distance = 0;
    }

    public int getDistance() {
        return distance;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setStarved(boolean starved) {
        isStarved = starved;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public boolean isStarved() {
        return isStarved;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public int getCylinderNumber() {
        return cylinderNumber;
    }

    public void setCylinderNumber(int cylinderNumber) {
        this.cylinderNumber = cylinderNumber;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
}
