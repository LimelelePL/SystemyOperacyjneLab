public class Request {
    private Integer ID;
    private String type;
    private int arrivalTime;
    private int sectorNumber;
    private int deadline;
    private int predictedServiceTime;
    private int gcPenalty;
    private int startTime;
    private int endTime;
    private boolean slacked=false;

    private double waitTime;

    public Request(Integer ID, String type, int arrivalTime,
                   int sectorNumber, int deadline) {
        this.ID = ID;
        this.type = type;
        this.arrivalTime = arrivalTime;
        this.sectorNumber = sectorNumber;
        this.deadline = deadline;
        this.predictedServiceTime = 0;
        this.gcPenalty = 0;
        waitTime = 0.0;
    }

    public boolean isSlacked() {
        return slacked;
    }

    public void setSlacked(boolean slacked) {
        this.slacked = slacked;
    }

    public double getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(double waitTime) {
        this.waitTime = waitTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getGcPenalty() {
        return gcPenalty;
    }

    public void setGcPenalty(int gcPenalty) {
        this.gcPenalty = gcPenalty;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public int getPredictedServiceTime() {
        return predictedServiceTime;
    }

    public void setPredictedServiceTime(int predictedServiceTime) {
        this.predictedServiceTime = predictedServiceTime;
    }

    public int getSectorNumber() {
        return sectorNumber;
    }

    public void setSectorNumber(int sectorNumber) {
        this.sectorNumber = sectorNumber;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type.toUpperCase();
    }
}
