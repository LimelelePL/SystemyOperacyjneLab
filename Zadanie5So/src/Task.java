public class Task {
    private int arrivalTime;
    private int duration;
    private int IdOfCPU;
    private float demand;
    private int timeLeft;

    public Task(int arrivalTime, int duration, int IdOfCPU, float demand) {
        this.arrivalTime = arrivalTime;
        this.duration = duration;
        this.IdOfCPU = IdOfCPU;
        this.demand = demand;
        this.timeLeft = duration;
    }

    public void decrementTimeLeft() {
        if (timeLeft > 0) {
            timeLeft--;
        }
    }
    public boolean isFinished() {
        return timeLeft <= 0;
    }
    public int getArrivalTime() {
        return arrivalTime;
    }

    public float getDemand() {
        return demand;
    }

    public int getDuration() {
        return duration;
    }

    public int getIdOfCPU() {
        return IdOfCPU;
    }
}
