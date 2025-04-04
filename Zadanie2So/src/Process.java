public class Process {
    private String processName;
    private int ArrivalTime;
    private boolean isStarved=false;

    public void setStarved(boolean starved) {
        isStarved = starved;
    }
}
