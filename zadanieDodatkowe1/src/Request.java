public class Request {
    private String name;
    private String type;
    private int arrivalTime;
    private int sectorNumber;
    private int deadline;
    private int predictedServiceTime;
    private int gcPenalty;
    private int startTime;
    private int endTime;

    private double waitTime;

    public Request(String name, String type, int arrivalTime,
                   int sectorNumber, int deadline, int predictedServiceTime, int gcPenalty) {
        this.name = name;
        this.type = type;
        this.arrivalTime = arrivalTime;
        this.sectorNumber = sectorNumber;
        this.deadline = deadline;
        this.predictedServiceTime = predictedServiceTime;
        this.gcPenalty = gcPenalty;
        waitTime = 0.0;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toUpperCase();
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
        this.type = type;
    }
}
