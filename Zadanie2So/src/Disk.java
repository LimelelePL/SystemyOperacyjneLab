public class Disk {
    private int currentPosition;
    private final int maxPosition;
    private int totalHeadMovements;

    public Disk(int maxPosition) {
        this.maxPosition = maxPosition;
        this.currentPosition = 0;
        this.totalHeadMovements = 0;
    }

    public int moveTo(int newPosition) {
        int movement = Math.abs(newPosition - currentPosition);

        totalHeadMovements += movement;
        currentPosition = newPosition;

        return movement;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getTotalHeadMovements() {
        return totalHeadMovements;
    }

    public int getMaxPosition() {
        return maxPosition;
    }

    public void reset() {
        currentPosition = 0;
        totalHeadMovements = 0;
    }
}