import java.util.PriorityQueue;

public abstract class Algoritm {
    private int returns;
    private int starvedProcesses;
    private int starvationTreshold;

    public Algoritm() {
        this.returns = 0;
        this.starvedProcesses = 0;
        this.starvationTreshold = 100000;
    }


    public int getStarvationTreshold() {
        return starvationTreshold;
    }

    public void setStarvationTreshold(int starvationTreshold) {
        this.starvationTreshold = starvationTreshold;
    }

    public int getReturns() {
        return returns;
    }

    public void setReturns(int returns) {
        this.returns = returns;
    }

    public int getStarvedProcesses() {
        return starvedProcesses;
    }

    public void setStarvedProcesses(int starvedProcesses) {
        this.starvedProcesses = starvedProcesses;
    }
}
